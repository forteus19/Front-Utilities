package red.vuis.frontutil.client.screen;

import java.util.ArrayList;
import java.util.List;

import com.boehmod.blockfront.common.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.client.widget.ItemPreview;
import red.vuis.frontutil.client.widget.WeaponEditContainer;
import red.vuis.frontutil.client.widget.Widgets;
import red.vuis.frontutil.data.WeaponExtraSettings;
import red.vuis.frontutil.setup.GunSkinIndex;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;
import static red.vuis.frontutil.client.widget.WidgetDim.dim;
import static red.vuis.frontutil.client.widget.WidgetDim.sqrCenteredDim;
import static red.vuis.frontutil.client.widget.WidgetDim.sqrDim;
import static red.vuis.frontutil.util.AddonAccessors.accessGunItem;

public class WeaponExtraScreen extends AddonScreen {
	private static final Component C_BUTTON_BACK = Component.translatable("frontutil.screen.generic.button.back");
	private static final Component C_HEADER = Component.translatable("frontutil.screen.weapon.extra.header");
	private static final Component C_LABEL_BARRELTYPE = Component.translatable("frontutil.screen.weapon.label.barrelType");
	private static final Component C_LABEL_MAGTYPE = Component.translatable("frontutil.screen.weapon.label.magType");
	private static final Component C_LABEL_SCOPE = Component.translatable("frontutil.screen.weapon.label.scope");
	private static final Component C_LABEL_SKIN = Component.translatable("frontutil.screen.weapon.label.skin");
	
	private final @Nullable Screen parent;
	private final GunItem item;
	private final WeaponExtraSettings settings;
	private final @Nullable WeaponEditContainer container;
	
	private final List<String> magTypeNames = new ArrayList<>();
	private final List<String> barrelTypeNames = new ArrayList<>();
	private final List<String> skinNames = new ArrayList<>();
	
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
		
		accessGunItem(item, accessor -> {
			magTypeNames.addAll(accessor.getMagIdMap().keySet());
			barrelTypeNames.addAll(accessor.getBarrelIdMap().keySet());
		});
		
		GunSkinIndex.getSkinNames(item).ifPresent(skinNames::addAll);
	}
	
	@Override
	protected void init() {
		super.init();
		
		addRenderableWidget(Widgets.checkbox(
			font,
			sqrCenteredDim(width / 2 - 70, 80, 20),
			settings.scope,
			(checkbox, b) -> {
				settings.scope = b;
				preview.setItemStack(settings.getItemStack(item));
			}
		));
		
		EditBox magTypeBox = addRenderableWidget(Widgets.editBox(
			font,
			dim(width / 2 - 110, 110, 90, 20)
		));
		magTypeBox.setValue(settings.magType);
		magTypeBox.setResponder(s -> {
			settings.magType = s;
			preview.setItemStack(settings.getItemStack(item));
		});
		
		addRenderableWidget(Widgets.button(
			Component.literal("+"),
			dim(width / 2 - 120, 110, 10, 20),
			button -> {
				int currentIndex = magTypeNames.indexOf(magTypeBox.getValue());
				if (currentIndex < 0) {
					currentIndex = 0;
				}
				magTypeBox.setValue(magTypeNames.get((currentIndex + 1) % magTypeNames.size()));
			}
		));
		
		EditBox barrelTypeBox = addRenderableWidget(Widgets.editBox(
			font,
			dim(width / 2 - 110, 150, 90, 20)
		));
		barrelTypeBox.setValue(settings.barrelType);
		barrelTypeBox.setResponder(s -> {
			settings.barrelType = s;
			preview.setItemStack(settings.getItemStack(item));
		});
		
		addRenderableWidget(Widgets.button(
			Component.literal("+"),
			dim(width / 2 - 120, 150, 10, 20),
			button -> {
				int currentIndex = barrelTypeNames.indexOf(barrelTypeBox.getValue());
				if (currentIndex < 0) {
					currentIndex = 0;
				}
				barrelTypeBox.setValue(barrelTypeNames.get((currentIndex + 1) % barrelTypeNames.size()));
			}
		));
		
		EditBox skinBox = addRenderableWidget(Widgets.editBox(
			font,
			dim(width / 2 - 110, 190, 90, 20)
		));
		skinBox.setValue(settings.skin);
		skinBox.setResponder(s -> {
			settings.skin = "".equals(s) ? null : s;
			preview.setItemStack(settings.getItemStack(item));
		});
		
		Button skinNextButton = addRenderableWidget(Widgets.button(
			Component.literal("+"),
			dim(width / 2 - 120, 190, 10, 20),
			button -> {
				int currentIndex = skinNames.indexOf(skinBox.getValue());
				if (currentIndex < 0) {
					currentIndex = 0;
				}
				skinBox.setValue(skinNames.get((currentIndex + 1) % skinNames.size()));
			}
		));
		
		addRenderableWidget(Widgets.checkbox(
			font,
			sqrDim(width / 2 - 140, 190, 20),
			settings.skin != null,
			(checkbox, b) -> {
				if (b) {
					skinBox.active = true;
					skinNextButton.active = true;
					if (!skinNames.isEmpty()) {
						skinBox.setValue(skinNames.getFirst());
					}
				} else {
					skinBox.active = false;
					skinNextButton.active = false;
					skinBox.setValue("");
				}
			}
		));
		
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
		drawText(C_LABEL_BARRELTYPE, width / 2 - 70, 140, true);
		drawText(C_LABEL_SKIN, width / 2 - 70, 180, true);
	}
}
