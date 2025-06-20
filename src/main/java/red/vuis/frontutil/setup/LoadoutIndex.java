package red.vuis.frontutil.setup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.UnmodifiableView;

import red.vuis.frontutil.data.AddonStreamCodecs;
import red.vuis.frontutil.mixin.DivisionDataAccessor;

public final class LoadoutIndex {
	public static final Map<String, BFCountry> COUNTRIES = new Object2ObjectOpenHashMap<>();
	public static final Map<BFCountry, List<String>> SKINS = new EnumMap<>(BFCountry.class);
	public static final Map<String, MatchClass> MATCH_CLASSES = new Object2ObjectOpenHashMap<>();
	public static final Map<Identifier, @UnmodifiableView List<Loadout>> DEFAULT = new Object2ObjectOpenHashMap<>();
	
	public static final List<Function<Loadout, ItemStack>> SLOT_FUNCS = List.of(
		Loadout::getPrimary, Loadout::getSecondary, Loadout::getMelee, Loadout::getOffHand,
		Loadout::getHead, Loadout::getChest, Loadout::getLegs, Loadout::getFeet
	);
	
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
	
	private static Loadout cloneLoadout(Loadout original) {
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
	
	public static void applyDefaults() {
		apply(DEFAULT);
	}
	
	public static void apply(Map<Identifier, List<Loadout>> loadouts) {
		for (DivisionData divisionData : DivisionData.INSTANCES) {
			DivisionDataAccessor accessor = (DivisionDataAccessor) (Object) divisionData;
			accessor.getRawLoadouts().clear();
		}
		
		for (Map.Entry<Identifier, List<Loadout>> entry : loadouts.entrySet()) {
			Identifier id = entry.getKey();
			List<Loadout> value = entry.getValue();
			
			DivisionData divisionData = DivisionData.getByCountryAndSkin(id.country(), id.skin());
			if (divisionData == null) {
				continue;
			}
			DivisionDataAccessor accessor = (DivisionDataAccessor) (Object) divisionData;
			Map<MatchClass, List<Loadout>> rawLoadouts = accessor.getRawLoadouts();
			
			List<Loadout> clonedLoadouts = new ObjectArrayList<>();
			value.forEach(loudout -> clonedLoadouts.add(cloneLoadout(loudout)));
			
			rawLoadouts.put(id.matchClass(), clonedLoadouts);
		}
	}
	
	public static Map<Identifier, List<Loadout>> getAsMap() {
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
	
	static {
		for (BFCountry country : BFCountry.values()) {
			COUNTRIES.put(country.getTag(), country);
			SKINS.put(country, new ArrayList<>());
		}
		for (MatchClass matchClass : MatchClass.values()) {
			MATCH_CLASSES.put(matchClass.getKey(), matchClass);
		}
	}
	
	public record Identifier(BFCountry country, String skin, MatchClass matchClass) {
		public static final StreamCodec<ByteBuf, Identifier> STREAM_CODEC = StreamCodec.composite(
			AddonStreamCodecs.BF_COUNTRY, Identifier::country,
			ByteBufCodecs.STRING_UTF8, Identifier::skin,
			AddonStreamCodecs.MATCH_CLASS, Identifier::matchClass,
			Identifier::new
		);
	}
}
