package red.vuis.frontutil.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.client.widget.CompoundWidget;

public abstract class AddonScreen extends ImmediateScreen {
	public AddonScreen(Text title) {
		super(title);
	}
	
	protected @NotNull MinecraftClient client() {
		if (client == null) {
			throw new NullPointerException("Client not present");
		}
		return client;
	}
	
	protected void setScreen(@Nullable Screen screen) {
		client().setScreen(screen);
	}
	
	protected <T extends CompoundWidget> T addCompoundWidget(T compoundWidget) {
		for (Object widget : compoundWidget.getWidgets()) {
			if (widget instanceof Element && widget instanceof Selectable) {
				addSelectableChild((Element & Selectable) widget);
			}
			if (widget instanceof Drawable) {
				addDrawable((Drawable) widget);
			}
		}
		return compoundWidget;
	}
}
