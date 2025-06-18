package red.vuis.frontutil.client.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class Widgets {
	private Widgets() {
	}
	
	public static Button button(Component label, int x, int y, int width, int height, Button.OnPress onPress) {
		return Button.builder(label, onPress).bounds(x, y, width, height).build();
	}
	
	public static Button button(Component label, WidgetDim dim, Button.OnPress onPress) {
		return button(label, dim.x(), dim.y(), dim.width(), dim.height(), onPress);
	}
	
	public static Checkbox checkbox(Font font, int x, int y, int size, boolean selected) {
		return Checkbox.builder(Component.empty(), font).pos(x, y).maxWidth(size).selected(selected).build();
	}
	
	public static Checkbox checkbox(Font font, WidgetDim dim, boolean selected) {
		return checkbox(font, dim.x(), dim.y(), dim.width(), selected);
	}
	
	public static EditBox editBox(Font font, WidgetDim dim) {
		return new EditBox(font, dim.x(), dim.y(), dim.width(), dim.height(), Component.empty());
	}
}
