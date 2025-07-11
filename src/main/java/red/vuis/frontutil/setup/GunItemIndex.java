package red.vuis.frontutil.setup;

import java.util.List;

import com.boehmod.blockfront.common.item.GunItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class GunItemIndex {
	public static final List<ResourceLocation> GUN_ITEMS = new ObjectArrayList<>();
	
	private GunItemIndex() {
	}
	
	public static void init() {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof GunItem) {
				GUN_ITEMS.add(BuiltInRegistries.ITEM.getKey(item));
			}
		}
	}
}
