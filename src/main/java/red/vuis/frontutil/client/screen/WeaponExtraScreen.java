package red.vuis.frontutil.client.screen;

import com.boehmod.blockfront.common.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.client.widget.ItemPreview;
import red.vuis.frontutil.client.widget.WeaponEditContainer;
import red.vuis.frontutil.client.widget.Widgets;
import red.vuis.frontutil.data.WeaponExtraSettings;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;
import static red.vuis.frontutil.client.widget.WidgetDim.sqrCenteredDim;

public class WeaponExtraScreen extends AddonScreen {
	private static final Component C_BUTTON_BACK = Component.translatable("frontutil.screen.generic.button.back");
	private static final Component C_HEADER = Component.translatable("frontutil.screen.weapon.extra.header");
	private static final Component C_LABEL_MAGTYPE = Component.translatable("frontutil.screen.weapon.label.magType");
	private static final Component C_LABEL_SCOPE = Component.translatable("frontutil.screen.weapon.label.scope");
	
	private final @Nullable Screen parent;
	private final GunItem item;
	private final WeaponExtraSettings settings;
	private final @Nullable WeaponEditContainer container;
	
	protected Checkbox scopeCheckbox;
	protected EditBox magTypeBox;
	private ItemPreview preview;
	
	public WeaponExtraScreen(@Nullable Screen parent, GunItem item, WeaponExtraSettings settings) {
		this(parent, item, settings, null);
	}
	
	public WeaponExtraScreen(@Nullable Screen parent, GunItem item, WeaponExtraSettings settings, @Nullable WeaponEditContainer container) {
		super(C_HEADER);
		this.parent = parent;
		this.item = item;
		this.settings = settings;
		this.container = container;
	}
	
	@Override
	protected void init() {
		super.init();
		
		scopeCheckbox = addRenderableWidget(Widgets.checkbox(
			font,
			sqrCenteredDim(width / 2 - 70, 80, 20),
			settings.scope,
			(checkbox, b) -> {
				settings.scope = b;
				preview.setItemStack(settings.getItemStack(item));
			}
		));
		
		magTypeBox = addRenderableWidget(Widgets.editBox(
			font,
			centeredDim(width / 2 - 70, 120, 100, 20)
		));
		magTypeBox.setValue(settings.magType);
		magTypeBox.setResponder(s -> {
			settings.magType = s;
			preview.setItemStack(settings.getItemStack(item));
		});
		
		preview = new ItemPreview(width / 2 + 20, 50, 100)
			.setItemStack(settings.getItemStack(item));
		
		addRenderableWidget(Widgets.button(
			C_BUTTON_BACK,
			centeredDim(width / 2, height - 20, 120, 20),
			button -> {
				if (container != null) {
					container.refresh();
				}
				Minecraft.getInstance().setScreen(parent);
			}
		));
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		preview.render(graphics, mouseX, mouseY, partialTick);
		
		drawText(C_HEADER, width / 2, 20, true);
		
		drawText(C_LABEL_SCOPE, width / 2 - 70, 60, true);
		drawText(C_LABEL_MAGTYPE, width / 2 - 70, 100, true);
	}
}
