package red.vuis.frontutil.setup;

import java.util.List;
import java.util.Map;

import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.world.item.ItemStack;

import red.vuis.frontutil.mixin.DivisionDataAccessor;

public final class LoadoutIndex {
	private static final Map<BFCountry, String> PRIMARY_SKINS = Map.of(
		BFCountry.UNITED_STATES, "airborne",
		BFCountry.GERMANY, "wehrmacht",
		BFCountry.GREAT_BRITAIN, "infantry",
		BFCountry.SOVIET_UNION, "infantry",
		BFCountry.POLAND, "infantry",
		BFCountry.JAPAN, "infantry",
		BFCountry.ITALY, "infantry",
		BFCountry.FRANCE, "infantry"
	);
	public static final Map<Identifier, Loadout> DEFAULT = new Object2ObjectArrayMap<>();
	
	private LoadoutIndex() {}
	
	public static void init() {
		for (DivisionData division : DivisionData.INSTANCES) {
			BFCountry country = division.getCountry();
			
			if (!division.getSkin().equals(PRIMARY_SKINS.get(country))) {
				continue;
			}
			
			for (Map.Entry<MatchClass, List<Loadout>> entry : division.getLoadouts().entrySet()) {
				MatchClass matchClass = entry.getKey();
				List<Loadout> loadouts = entry.getValue();
				
				for (int i = 0; i < loadouts.size(); i++) {
					DEFAULT.put(new Identifier(country, matchClass, i), cloneLoadout(loadouts.get(i)));
				}
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
	
	private static void redoDivisionCopies() {
		for (DivisionData division : DivisionData.INSTANCES) {
			BFCountry country = division.getCountry();
			String primarySkin = PRIMARY_SKINS.get(country);
			
			if (division.getSkin().equals(primarySkin)) {
				continue;
			}
			
			DivisionData primaryDivision = DivisionData.getByCountryAndSkin(country, primarySkin);
			if (primaryDivision == null) {
				continue;
			}
			
			((DivisionDataAccessor) (Object) division).copyLoadouts(primaryDivision);
		}
	}
	
	public record Identifier(BFCountry country, MatchClass matchClass, int level) {
	}
}
