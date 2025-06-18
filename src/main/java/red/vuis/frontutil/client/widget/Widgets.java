package red.vuis.frontutil.client.widget;

import net.minecraft.client.gui.components.Button;
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
}
