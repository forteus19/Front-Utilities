package red.vuis.frontutil.client.screen;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import red.vuis.frontutil.client.widget.CompoundWidget;

public abstract class AddonScreen extends ImmediateScreen {
	public AddonScreen(Component title) {
		super(title);
	}
	
	protected <T extends CompoundWidget> T addCompoundWidget(T compoundWidget) {
		for (Object widget : compoundWidget.getWidgets()) {
			if (widget instanceof GuiEventListener && widget instanceof NarratableEntry) {
				addWidget((GuiEventListener & NarratableEntry) widget);
			}
			if (widget instanceof Renderable) {
				addRenderableOnly((Renderable) widget);
			}
		}
		return compoundWidget;
	}
}
