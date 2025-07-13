package red.vuis.frontutil.setup;

import java.util.Map;

import com.boehmod.blockfront.common.item.GunItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.mixin.GunItemAccessor;

import static red.vuis.frontutil.util.AddonAccessors.applyGunItem;

public final class GunModifierIndex {
	public static final Map<RegistryEntry<Item>, GunModifier> DEFAULT = new Object2ObjectOpenHashMap<>();
	
	private GunModifierIndex() {
	}
	
	public static void init() {
		for (Item item : Registries.ITEM) {
			if (!(item instanceof GunItem gunItem)) {
				continue;
			}
			DEFAULT.put(
				Registries.ITEM.getEntry(gunItem),
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
		for (Map.Entry<RegistryEntry<Item>, GunModifier> entry : DEFAULT.entrySet()) {
			if (entry.getKey().value() instanceof GunItem gunItem) {
				entry.getValue().apply(gunItem);
			}
		}
	}
}
