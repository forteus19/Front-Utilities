package red.vuis.frontutil.data;

import java.util.List;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;

import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.FrontUtil;
import red.vuis.frontutil.setup.LoadoutIndex;

public class AddonLevelData extends SavedData {
	private static final Factory<AddonLevelData> FACTORY = new Factory<>(AddonLevelData::new, AddonLevelData::load, null);
	
	public final Map<LoadoutIndex.Identifier, List<Loadout>> loadouts;
	
	private AddonLevelData() {
		loadouts = new Object2ObjectOpenHashMap<>();
	}
	
	@Override
	@ParametersAreNonnullByDefault
	public @NotNull CompoundTag save(CompoundTag root, HolderLookup.Provider lookupProvider) {
		root.put("loadouts", saveLoadouts());
		return root;
	}
	
	private CompoundTag saveLoadouts() {
		CompoundTag root = new CompoundTag();
		
		for (Map.Entry<LoadoutIndex.Identifier, List<Loadout>> entry : loadouts.entrySet()) {
			LoadoutIndex.Identifier id = entry.getKey();
			List<Loadout> value = entry.getValue();
			
			String countryName = id.country().getTag();
			String skinName = id.skin();
			String matchClassName = id.matchClass().getKey();
			
			CompoundTag countryTag = root.contains(countryName) ?
				root.getCompound(countryName) : new CompoundTag();
			CompoundTag skinTag = countryTag.contains(skinName) ?
				countryTag.getCompound(skinName) : new CompoundTag();
			
			ListTag matchClassTag = new ListTag();
			value.forEach(loadout ->
				AddonCodecs.LOADOUT.encodeStart(NbtOps.INSTANCE, loadout)
					.resultOrPartial(FrontUtil.LOGGER::error)
					.ifPresent(matchClassTag::add)
			);
			
			skinTag.put(matchClassName, matchClassTag);
			countryTag.put(skinName, skinTag);
			root.put(countryName, countryTag);
		}
		
		return root;
	}
	
	public static AddonLevelData load(CompoundTag root, HolderLookup.Provider lookupProvider) {
		AddonLevelData data = new AddonLevelData();
		if (root.contains("loadouts", Tag.TAG_COMPOUND)) {
			loadLoadouts(root.getCompound("loadouts"), data.loadouts);
		}
		return data;
	}
	
	public static void loadLoadouts(CompoundTag root, Map<LoadoutIndex.Identifier, List<Loadout>> target) {
		for (String countryName : root.getAllKeys()) {
			if (root.getTagType(countryName) != Tag.TAG_COMPOUND || !LoadoutIndex.COUNTRIES.containsKey(countryName)) {
				continue;
			}
			CompoundTag countryTag = root.getCompound(countryName);
			BFCountry country = LoadoutIndex.COUNTRIES.get(countryName);
			
			for (String skinName : countryTag.getAllKeys()) {
				if (countryTag.getTagType(skinName) != Tag.TAG_COMPOUND || !LoadoutIndex.SKINS.get(country).contains(skinName)) {
					continue;
				}
				CompoundTag skinTag = countryTag.getCompound(skinName);
				
				for (String matchClassName : skinTag.getAllKeys()) {
					if (skinTag.getTagType(matchClassName) != Tag.TAG_LIST || !LoadoutIndex.MATCH_CLASSES.containsKey(matchClassName)) {
						continue;
					}
					ListTag matchClassTag = skinTag.getList(matchClassName, Tag.TAG_COMPOUND);
					MatchClass matchClass = LoadoutIndex.MATCH_CLASSES.get(matchClassName);
					
					List<Loadout> loadouts = new ObjectArrayList<>();
					matchClassTag.forEach(loadoutTag ->
						AddonCodecs.LOADOUT.parse(NbtOps.INSTANCE, loadoutTag)
							.resultOrPartial(FrontUtil.LOGGER::error)
							.ifPresent(loadouts::add)
					);
					
					target.put(new LoadoutIndex.Identifier(country, skinName, matchClass), loadouts);
				}
			}
		}
	}
	
	public static AddonLevelData get(MinecraftServer server) {
		ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);
		assert level != null;
		AddonLevelData data = level.getDataStorage().computeIfAbsent(FACTORY, FrontUtil.MOD_ID);
		data.setDirty();
		return data;
	}
}
