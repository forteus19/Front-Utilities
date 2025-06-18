package red.vuis.frontutil.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import red.vuis.frontutil.client.widget.ItemEditContainer;
import red.vuis.frontutil.client.widget.WidgetDim;

public abstract class AddonScreen extends ImmediateScreen {
	public AddonScreen(Component title) {
		super(title);
	}
	
	protected ItemEditContainer addItemEdit(Font font, int x, int y, int width, int height) {
		ItemEditContainer container = new ItemEditContainer(font, x, y, width, height);
		addRenderableWidget(container.itemIdBox);
		addRenderableWidget(container.countBox);
		addRenderableOnly(container.preview);
		return container;
	}
	
	protected ItemEditContainer addItemEdit(Font font, WidgetDim dim) {
		return addItemEdit(font, dim.x(), dim.y(), dim.width(), dim.height());
	}
}
