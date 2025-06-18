package red.vuis.frontutil.client.widget;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;

public class ItemEditContainer {
	private static final Supplier<Component> C_ITEM_ID_HINT = () -> Component.translatable("frontutil.widget.itemStack.itemId.hint");
	
	public final EditBox itemIdBox;
	public final EditBox countBox;
	public final ItemPreview preview;
	
	public ItemEditContainer(Font font, int x, int y, int width, int height) {
		itemIdBox = new EditBox(font, x, y, width - 40, height, Component.empty());
		countBox = new EditBox(font, x + width - 40, y, 20, height, Component.empty());
		preview = new ItemPreview(x + width - 20, y, 20);
		
		itemIdBox.setResponder(strId -> {
			ResourceLocation rl = ResourceLocation.tryParse(strId);
			if (rl == null) {
				preview.setItemStack(null);
				return;
			}
			Optional<Holder.Reference<Item>> item = BuiltInRegistries.ITEM.getHolder(rl);
			if (item.isEmpty()) {
				preview.setItemStack(null);
				return;
			}
			preview.setItemStack(new ItemStack(item.get().value()));
		});
		
		itemIdBox.setHint(C_ITEM_ID_HINT.get());
		countBox.setHint(Component.literal("#"));
		countBox.setFilter(str -> (str != null && str.isEmpty()) || StringUtils.isNumeric(str));
	}
	
	public void setValue(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item == Items.AIR) {
			clear();
			return;
		}
		itemIdBox.setValue(BuiltInRegistries.ITEM.getKey(item).toString());
		itemIdBox.moveCursorToStart(false);
		countBox.setValue(Integer.toString(itemStack.getCount()));
		countBox.moveCursorToStart(false);
	}
	
	public void setActive(boolean active) {
		itemIdBox.active = active;
		countBox.active = active;
	}
	
	public void clear() {
		itemIdBox.setValue("");
		countBox.setValue("");
	}
}
