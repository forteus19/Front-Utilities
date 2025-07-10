package red.vuis.frontutil.data;

import com.boehmod.blockfront.common.item.GunItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WeaponExtraSettings {
	public boolean scope = false;
	public String magType = "default";
	
	public ItemStack setItemStackComponents(@Nullable ItemStack itemStack) {
		if (itemStack != null && itemStack.getItem() instanceof GunItem) {
			GunItem.setScope(itemStack, scope);
			GunItem.setMagType(itemStack, magType);
		}
		return itemStack;
	}
	
	public @NotNull ItemStack getItemStack(@NotNull GunItem item) {
		return setItemStackComponents(new ItemStack(item));
	}
}
