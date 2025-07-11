package red.vuis.frontutil.setup;

import java.util.Map;

import com.boehmod.blockfront.common.item.GunItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.mixin.GunItemAccessor;

import static red.vuis.frontutil.util.AddonAccessors.applyGunItem;

public final class GunModifierIndex {
	public static final Map<Holder<Item>, GunModifier> DEFAULT = new Object2ObjectOpenHashMap<>();
	
	private GunModifierIndex() {
	}
	
	public static void init() {
		for (Item item : BuiltInRegistries.ITEM) {
			if (!(item instanceof GunItem gunItem)) {
				continue;
			}
			DEFAULT.put(
				BuiltInRegistries.ITEM.wrapAsHolder(gunItem),
				new GunModifier(
					GunModifier.Ammo.of(applyGunItem(gunItem, GunItemAccessor::getMagIdMap).get("default")),
					GunModifier.Damage.of(gunItem.getDamageConfig()),
					GunModifier.FireMode.of(gunItem.getFireConfigs()),
					gunItem.getWeight(null) // Unused parameter
				)
			);
		}
	}
	
	public static void applyDefaults() {
		for (Map.Entry<Holder<Item>, GunModifier> entry : DEFAULT.entrySet()) {
			if (entry.getKey().value() instanceof GunItem gunItem) {
				entry.getValue().apply(gunItem);
			}
		}
	}
}
