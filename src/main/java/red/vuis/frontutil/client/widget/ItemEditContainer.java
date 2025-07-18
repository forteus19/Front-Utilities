package red.vuis.frontutil.client.widget;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class ItemEditContainer implements CompoundWidget {
	private static final Component C_ITEM_ID_HINT = Component.translatable("frontutil.widget.itemStack.itemId.hint");
	
	protected final EditBox itemIdBox;
	protected final IntegerEditBox countBox;
	protected final ItemPreview preview;
	
	public ItemEditContainer(Font font, int x, int y, int width, int height) {
		itemIdBox = new EditBox(font, x, y, width - 40, height, Component.empty());
		countBox = new IntegerEditBox(font, x + width - 40, y, 20, height, Component.empty());
		preview = new ItemPreview(x + width - 20, y, 20);
		
		itemIdBox.setResponder(this::onItemIdChanged);
		itemIdBox.setMaxLength(128);
		itemIdBox.setHint(C_ITEM_ID_HINT);
		
		countBox.setMaxLength(2);
		countBox.setHint(Component.literal("#"));
	}
	
	public ItemEditContainer(Font font, WidgetDim dim) {
		this(font, dim.x(), dim.y(), dim.width(), dim.height());
	}
	
	@Override
	public Iterable<Object> getWidgets() {
		return List.of(itemIdBox, countBox, preview);
	}
	
	protected void onItemIdChanged(String strId) {
		preview.setItemStack(getPreviewItemStack(strId));
	}
	
	public @Nullable Item getItem() {
		return Optional.ofNullable(ResourceLocation.tryParse(itemIdBox.getValue()))
			.map(BuiltInRegistries.ITEM::get)
			.orElse(null);
	}
	
	protected @Nullable ItemStack getPreviewItemStack(String strId) {
		ResourceLocation rl = ResourceLocation.tryParse(strId);
		if (rl == null) {
			preview.setItemStack(null);
			return null;
		}
		Optional<Holder.Reference<Item>> itemHolder = BuiltInRegistries.ITEM.getHolder(rl);
		if (itemHolder.isEmpty()) {
			preview.setItemStack(null);
			return null;
		}
		return new ItemStack(itemHolder.get().value());
	}
	
	public ItemStack getValue() {
		Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(itemIdBox.getValue()));
		return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, countBox.getIntValue().orElse(1));
	}
	
	public void setValue(ItemStack itemStack) {
		if (itemStack == null) {
			clear();
			return;
		}
		Item item = itemStack.getItem();
		if (item == Items.AIR) {
			clear();
			return;
		}
		itemIdBox.setValue(BuiltInRegistries.ITEM.getKey(item).toString());
		itemIdBox.moveCursorToStart(false);
		countBox.setIntValue(itemStack.getCount());
		countBox.moveCursorToStart(false);
	}
	
	public ItemEditContainer setActive(boolean active) {
		itemIdBox.active = active;
		countBox.active = active;
		return this;
	}
	
	public ItemEditContainer setVisible(boolean visible) {
		itemIdBox.visible = visible;
		countBox.visible = visible;
		preview.visible = visible;
		return this;
	}
	
	public ItemEditContainer clear() {
		itemIdBox.setValue("");
		countBox.setValue("");
		return this;
	}
}
