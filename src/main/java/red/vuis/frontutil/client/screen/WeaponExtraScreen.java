package red.vuis.frontutil.client.screen;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import red.vuis.frontutil.client.widget.WeaponEditContainer;
import red.vuis.frontutil.client.widget.Widgets;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;
import static red.vuis.frontutil.client.widget.WidgetDim.sqrCenteredDim;

public class WeaponExtraScreen extends AddonScreen {
	private static final Supplier<MutableComponent> C_BUTTON_BACK = () -> Component.translatable("frontutil.screen.generic.button.back");
	private static final Supplier<MutableComponent> C_HEADER = () -> Component.translatable("frontutil.screen.weapon.extra.header");
	private static final Supplier<MutableComponent> C_LABEL_MAGTYPE = () -> Component.translatable("frontutil.screen.weapon.label.magType");
	private static final Supplier<MutableComponent> C_LABEL_SCOPE = () -> Component.translatable("frontutil.screen.weapon.label.scope");
	
	private final Screen parent;
	private final WeaponEditContainer container;
	
	private Checkbox scopeCheckbox;
	private EditBox magTypeBox;
	
	public WeaponExtraScreen(Screen parent, WeaponEditContainer container) {
		super(C_HEADER.get());
		this.parent = parent;
		this.container = container;
	}
	
	@Override
	protected void init() {
		super.init();
		
		scopeCheckbox = addRenderableWidget(Widgets.checkbox(
			font,
			sqrCenteredDim(width / 2, 80, 20),
			container.scope
		));
		magTypeBox = addRenderableWidget(Widgets.editBox(
			font,
			centeredDim(width / 2, 120, 100, 20)
		));
		magTypeBox.setValue(container.magType);
		
		addRenderableWidget(Widgets.button(
			C_BUTTON_BACK.get(),
			centeredDim(width / 2, height - 20, 120, 20),
			button -> {
				container.scope = scopeCheckbox.selected();
				container.magType = magTypeBox.getValue();
				container.refresh();
				Minecraft.getInstance().setScreen(parent);
			}
		));
	}
	
	@Override
	protected void render(int mouseX, int mouseY, float partialTick) {
		drawText(C_HEADER.get(), width / 2, 20, true);
		
		drawText(C_LABEL_SCOPE.get(), width / 2, 60, true);
		drawText(C_LABEL_MAGTYPE.get(), width / 2, 100, true);
	}
}
