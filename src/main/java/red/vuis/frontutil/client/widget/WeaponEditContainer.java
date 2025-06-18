package red.vuis.frontutil.client.widget;

import com.boehmod.blockfront.common.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.client.screen.WeaponExtraScreen;
import red.vuis.frontutil.util.AddonUtils;

import static red.vuis.frontutil.client.widget.WidgetDim.dim;

public class WeaponEditContainer extends ItemEditContainer {
	private boolean active = true;
	
	protected final Button extraButton;
	
	public boolean scope = false;
	public String magType = "default";
	
	public WeaponEditContainer(Screen screen, Font font, int x, int y, int width, int height) {
		super(font, x + 10, y, width - 10, height);
		extraButton = Widgets.button(
			Component.literal("*"),
			dim(x, y, 10, height),
			button -> Minecraft.getInstance().setScreen(
				new WeaponExtraScreen(screen, this)
			)
		);
	}
	
	public WeaponEditContainer(Screen screen, Font font, WidgetDim dim) {
		this(screen, font, dim.x(), dim.y(), dim.width(), dim.height());
	}
	
	@Override
	public Iterable<Object> getWidgets() {
		return AddonUtils.concat(super.getWidgets(), extraButton);
	}
	
	@Override
	protected void onItemIdChanged(String strId) {
		ItemStack itemStack = getPreviewItemStack(strId);
		preview.setItemStack(itemStack);
		extraButton.active = itemStack != null && itemStack.getItem() instanceof GunItem && active;
	}
	
	@Override
	protected ItemStack getPreviewItemStack(String strId) {
		return setItemStackComponents(super.getPreviewItemStack(strId));
	}
	
	@Override
	public ItemStack getValue() {
		return setItemStackComponents(super.getValue());
	}
	
	@Override
	public void setValue(ItemStack itemStack) {
		super.setValue(itemStack);
		retrieveItemStackComponents(itemStack);
		refresh();
	}
	
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		if (!active && extraButton.active) {
			extraButton.active = false;
		}
		this.active = active;
	}
	
	@Override
	public void clear() {
		super.clear();
		scope = false;
		magType = "default";
	}
	
	public void refresh() {
		preview.setItemStack(setItemStackComponents(super.getPreviewItemStack(itemIdBox.getValue())));
	}
	
	protected void retrieveItemStackComponents(@Nullable ItemStack itemStack) {
		if (itemStack != null && itemStack.getItem() instanceof GunItem) {
			scope = GunItem.getScope(itemStack);
			magType = GunItem.getMagType(itemStack);
		} else {
			scope = false;
			magType = "default";
		}
	}
	
	protected ItemStack setItemStackComponents(@Nullable ItemStack itemStack) {
		if (itemStack != null && itemStack.getItem() instanceof GunItem) {
			GunItem.setScope(itemStack, scope);
			GunItem.setMagType(itemStack, magType);
		}
		return itemStack;
	}
}
