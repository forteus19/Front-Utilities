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
import red.vuis.frontutil.data.WeaponExtraSettings;
import red.vuis.frontutil.util.AddonUtils;

import static red.vuis.frontutil.client.widget.WidgetDim.dim;

public class WeaponEditContainer extends ItemEditContainer {
	private boolean active = true;
	
	protected final Button extraButton;
	
	public WeaponExtraSettings extra = new WeaponExtraSettings();
	
	public WeaponEditContainer(Screen screen, Font font, int x, int y, int width, int height) {
		super(font, x + 10, y, width - 10, height);
		extraButton = Widgets.button(
			Component.literal("*"),
			dim(x, y, 10, height),
			button -> {
				if (getItem() instanceof GunItem gunItem) {
					Minecraft.getInstance().setScreen(
						new WeaponExtraScreen(screen, gunItem, extra, this)
					);
				}
			}
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
	protected @Nullable ItemStack getPreviewItemStack(String strId) {
		return extra.setItemStackComponents(super.getPreviewItemStack(strId));
	}
	
	@Override
	public ItemStack getValue() {
		return extra.setItemStackComponents(super.getValue());
	}
	
	@Override
	public void setValue(ItemStack itemStack) {
		super.setValue(itemStack);
		retrieveItemStackComponents(itemStack);
		refresh();
	}
	
	@Override
	public WeaponEditContainer setActive(boolean active) {
		super.setActive(active);
		if (!active && extraButton.active) {
			extraButton.active = false;
		}
		this.active = active;
		return this;
	}
	
	@Override
	public WeaponEditContainer setVisible(boolean visible) {
		super.setVisible(visible);
		extraButton.visible = visible;
		return this;
	}
	
	@Override
	public WeaponEditContainer clear() {
		super.clear();
		extra = new WeaponExtraSettings();
		return this;
	}
	
	public void refresh() {
		preview.setItemStack(extra.setItemStackComponents(super.getPreviewItemStack(itemIdBox.getValue())));
	}
	
	protected void retrieveItemStackComponents(@Nullable ItemStack itemStack) {
		if (itemStack != null && itemStack.getItem() instanceof GunItem) {
			extra.scope = GunItem.getScope(itemStack);
			extra.magType = GunItem.getMagType(itemStack);
		} else {
			extra = new WeaponExtraSettings();
		}
	}
}
