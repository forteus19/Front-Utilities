package red.vuis.frontutil.data;

import com.boehmod.blockfront.common.item.GunItem;
import com.boehmod.blockfront.registry.BFDataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.setup.GunSkinIndex;

public class WeaponExtraSettings {
	public boolean scope = false;
	public String magType = "default";
	public String barrelType = "default";
	public String skin = null;
	
	public void getComponents(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return;
		}
		
		scope = GunItem.getScope(itemStack);
		magType = GunItem.getMagType(itemStack);
		barrelType = GunItem.getBarrelType(itemStack);
		skin = GunSkinIndex.getSkinName(itemStack).orElse(null);
	}
	
	public ItemStack setComponents(@Nullable ItemStack itemStack) {
		if (itemStack != null && itemStack.getItem() instanceof GunItem) {
			GunItem.setScope(itemStack, scope);
			GunItem.setMagType(itemStack, magType);
			GunItem.setBarrelType(itemStack, barrelType);
			GunSkinIndex.getSkinId(itemStack.getItem(), skin)
				.ifPresent(id -> itemStack.set(BFDataComponents.SKIN_ID, id));
		}
		return itemStack;
	}
	
	public @NotNull ItemStack getItemStack(@NotNull GunItem item) {
		return setComponents(new ItemStack(item));
	}
}
