package red.vuis.frontutil.setup;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.UnmodifiableView;

import red.vuis.frontutil.FrontUtil;
import red.vuis.frontutil.data.AddonCodecs;
import red.vuis.frontutil.mixin.DivisionDataAccessor;

import static red.vuis.frontutil.util.AddonAccessors.accessDivisionData;
import static red.vuis.frontutil.util.AddonAccessors.applyDivisionData;

public final class LoadoutIndex {
	public static final Map<String, BFCountry> COUNTRIES = new Object2ObjectOpenHashMap<>();
	public static final Map<BFCountry, List<String>> SKINS = new EnumMap<>(BFCountry.class);
	public static final Map<String, MatchClass> MATCH_CLASSES = new Object2ObjectOpenHashMap<>();
	public static final Map<Identifier, @UnmodifiableView List<Loadout>> DEFAULT = new Object2ObjectOpenHashMap<>();
	
	public static final List<Function<Loadout, ItemStack>> SLOT_FUNCS = List.of(
		Loadout::getPrimary, Loadout::getSecondary, Loadout::getMelee, Loadout::getOffHand,
		Loadout::getHead, Loadout::getChest, Loadout::getLegs, Loadout::getFeet
	);
	
	public static final String DEFAULT_LOADOUTS_PATH_NAME = "loadouts.dat";
	
	private LoadoutIndex() {}
	
	public static void init() {
		for (DivisionData division : DivisionData.INSTANCES) {
			BFCountry country = division.getCountry();
			String skin = division.getSkin();
			
			SKINS.get(country).add(skin);
			
			for (Map.Entry<MatchClass, List<Loadout>> entry : division.getLoadouts().entrySet()) {
				MatchClass matchClass = entry.getKey();
				List<Loadout> loadouts = entry.getValue();
				Identifier id = new Identifier(country, skin, matchClass);
				
				List<Loadout> clonedLoadouts = new ObjectArrayList<>(loadouts.size());
				for (Loadout loadout : loadouts) {
					clonedLoadouts.add(cloneLoadout(loadout));
				}
				DEFAULT.put(id, Collections.unmodifiableList(clonedLoadouts));
			}
		}
	}
	
	public static Loadout cloneLoadout(Loadout original) {
		return new Loadout(
			original.getPrimary().copy(),
			original.getSecondary().copy(),
			original.getMelee().copy(),
			original.getOffHand().copy(),
			original.getHead().copy(),
			original.getChest().copy(),
			original.getLegs().copy(),
			original.getFeet().copy()
		)
			.addExtra(original.getExtra().stream().map(ItemStack::copy).toList())
			.method_3154(original.method_3160())
			.setMinimumXp(original.getMinimumXp());
	}
	
	public static Map<Identifier, List<Loadout>> copyFlat(Map<Identifier, List<Loadout>> original) {
		Map<Identifier, List<Loadout>> result = new Object2ObjectOpenHashMap<>();
		
		for (Map.Entry<Identifier, List<Loadout>> entry : original.entrySet()) {
			List<Loadout> clonedLoadouts = new ObjectArrayList<>();
			entry.getValue().forEach(loudout -> clonedLoadouts.add(cloneLoadout(loudout)));
			result.put(entry.getKey(), clonedLoadouts);
		}
		
		return result;
	}
	
	public static void apply(Map<Identifier, List<Loadout>> loadouts) {
		for (DivisionData divisionData : DivisionData.INSTANCES) {
			accessDivisionData(divisionData, accessor -> accessor.getRawLoadouts().clear());
		}
		
		for (Map.Entry<Identifier, List<Loadout>> entry : loadouts.entrySet()) {
			Identifier id = entry.getKey();
			List<Loadout> value = entry.getValue();
			
			DivisionData divisionData = DivisionData.getByCountryAndSkin(id.country(), id.skin());
			if (divisionData == null) {
				continue;
			}
			Map<MatchClass, List<Loadout>> rawLoadouts = applyDivisionData(divisionData, DivisionDataAccessor::getRawLoadouts);
			
			List<Loadout> clonedLoadouts = new ObjectArrayList<>();
			value.forEach(loudout -> clonedLoadouts.add(cloneLoadout(loudout)));
			
			rawLoadouts.put(id.matchClass(), clonedLoadouts);
		}
	}
	
	public static Map<Identifier, List<Loadout>> currentFlat() {
		Map<Identifier, List<Loadout>> result = new Object2ObjectOpenHashMap<>();
		
		for (DivisionData divisionData : DivisionData.INSTANCES) {
			BFCountry country = divisionData.getCountry();
			String skin = divisionData.getSkin();
			
			for (Map.Entry<MatchClass, List<Loadout>> loadoutData : divisionData.getLoadouts().entrySet()) {
				List<Loadout> clonedLoadouts = new ObjectArrayList<>();
				loadoutData.getValue().forEach(loudout -> clonedLoadouts.add(cloneLoadout(loudout)));
				result.put(new Identifier(country, skin, loadoutData.getKey()), clonedLoadouts);
			}
		}
		
		return result;
	}
	
	public static Map<BFCountry, Map<String, Map<MatchClass, List<Loadout>>>> flatToNested(Map<Identifier, List<Loadout>> flat) {
		Map<BFCountry, Map<String, Map<MatchClass, List<Loadout>>>> nested = new EnumMap<>(BFCountry.class);
		
		for (Map.Entry<LoadoutIndex.Identifier, List<Loadout>> entry : flat.entrySet()) {
			LoadoutIndex.Identifier id = entry.getKey();
			nested.computeIfAbsent(id.country(), k -> new Object2ObjectOpenHashMap<>())
				.computeIfAbsent(id.skin(), k -> new EnumMap<>(MatchClass.class))
				.put(id.matchClass(), entry.getValue());
		}
		
		return nested;
	}
	
	public static Map<Identifier, List<Loadout>> nestedToFlat(Map<BFCountry, Map<String, Map<MatchClass, List<Loadout>>>> nested) {
		Map<LoadoutIndex.Identifier, List<Loadout>> flat = new Object2ObjectOpenHashMap<>();
		
		for (Map.Entry<BFCountry, Map<String, Map<MatchClass, List<Loadout>>>> countryEntry : nested.entrySet()) {
			BFCountry country = countryEntry.getKey();
			for (Map.Entry<String, Map<MatchClass, List<Loadout>>> skinEntry : countryEntry.getValue().entrySet()) {
				String skin = skinEntry.getKey();
				for (Map.Entry<MatchClass, List<Loadout>> matchClassEntry : skinEntry.getValue().entrySet()) {
					MatchClass matchClass = matchClassEntry.getKey();
					flat.put(new LoadoutIndex.Identifier(country, skin, matchClass), matchClassEntry.getValue());
				}
			}
		}
		
		return flat;
	}
	
	public static boolean parseAndApply(Path indexPath) {
		FrontUtil.LOGGER.info("Parsing and applying loadout data from disk...");
		long startNs = Util.getNanos();
		
		CompoundTag indexTag;
		try {
			indexTag = NbtIo.read(new DataInputStream(Files.newInputStream(indexPath)));
		} catch (NoSuchFileException e) {
			FrontUtil.LOGGER.info("No loadout file.");
			return true;
		} catch (Exception e) {
			FrontUtil.LOGGER.error("Error while reading loadout data from disk!", e);
			return false;
		}
		
		AddonCodecs.LOADOUT_INDEX.parse(NbtOps.INSTANCE, indexTag)
			.resultOrPartial(FrontUtil.LOGGER::error)
			.ifPresent(LoadoutIndex::apply);
		
		long endNs = Util.getNanos();
		FrontUtil.LOGGER.info("Loadout data loaded in {} ms.", String.format("%.3f", (endNs - startNs) / 1.0E6));
		return true;
	}
	
	public static boolean saveCurrent(Path indexPath) {
		FrontUtil.LOGGER.info("Saving loadout data to disk...");
		long startNs = Util.getNanos();
		
		CompoundTag encodeResult = (CompoundTag) AddonCodecs.LOADOUT_INDEX.encodeStart(NbtOps.INSTANCE, currentFlat())
			.resultOrPartial(FrontUtil.LOGGER::error)
			.orElseThrow();
		
		try {
			NbtIo.write(encodeResult, new DataOutputStream(Files.newOutputStream(indexPath)));
		} catch (Exception e) {
			FrontUtil.LOGGER.error("Error while writing loadout data to disk!", e);
			return false;
		}
		
		long endNs = Util.getNanos();
		FrontUtil.LOGGER.info("Loadout data saved in {} ms.", String.format("%.3f", (endNs - startNs) / 1.0E6));
		return true;
	}
	
	static {
		for (BFCountry country : BFCountry.values()) {
			COUNTRIES.put(country.getTag(), country);
			SKINS.put(country, new ObjectArrayList<>());
		}
		for (MatchClass matchClass : MatchClass.values()) {
			MATCH_CLASSES.put(matchClass.getKey(), matchClass);
		}
	}
	
	public record Identifier(BFCountry country, String skin, MatchClass matchClass) {
	}
}
