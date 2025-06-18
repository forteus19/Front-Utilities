package red.vuis.frontutil.setup;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.world.item.ItemStack;

public final class LoadoutIndex {
	public static final Map<BFCountry, List<String>> SKINS = new EnumMap<>(BFCountry.class);
	public static final Map<Identifier, Loadout> DEFAULT = new Object2ObjectArrayMap<>();
	
	private LoadoutIndex() {}
	
	public static void init() {
		for (BFCountry country : BFCountry.values()) {
			SKINS.put(country, new ArrayList<>());
		}
		
		for (DivisionData division : DivisionData.INSTANCES) {
			BFCountry country = division.getCountry();
			String skin = division.getSkin();
			
			SKINS.get(country).add(skin);
			
			for (Map.Entry<MatchClass, List<Loadout>> entry : division.getLoadouts().entrySet()) {
				MatchClass matchClass = entry.getKey();
				List<Loadout> loadouts = entry.getValue();
				
				for (int i = 0; i < loadouts.size(); i++) {
					DEFAULT.put(new Identifier(country, skin, matchClass, i), cloneLoadout(loadouts.get(i)));
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
	
	public record Identifier(BFCountry country, String skin, MatchClass matchClass, int level) {
	}
}
