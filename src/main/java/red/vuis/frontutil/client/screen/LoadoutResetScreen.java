package red.vuis.frontutil.client.screen;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.boehmod.blockfront.common.match.Loadout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.client.data.AddonClientData;
import red.vuis.frontutil.client.widget.Widgets;
import red.vuis.frontutil.setup.LoadoutIndex;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;

public class LoadoutResetScreen extends AddonScreen {
	private static final Text C_BUTTON_APPLY = Text.translatable("frontutil.screen.generic.button.apply");
	private static final Text C_BUTTON_BACK = Text.translatable("frontutil.screen.generic.button.back");
	private static final Function<Object, Text> C_BUTTON_MODE = i -> Text.translatable("frontutil.screen.loadout.reset.button.mode", i);
	private static final Text C_HEADER = Text.translatable("frontutil.screen.loadout.reset.header");
	
	private final LoadoutEditorScreen editor;
	private Mode selectedMode = Mode.LEVEL;
	
	public LoadoutResetScreen(LoadoutEditorScreen editor) {
		super(C_HEADER);
		this.editor = editor;
	}
	
	@Override
	protected void init() {
		super.init();
		
		addDrawableChild(Widgets.button(
			C_BUTTON_MODE.apply(selectedMode.component),
			centeredDim(width / 2, 80, 140, 20),
			button -> {
				Mode[] values = Mode.values();
				selectedMode = values[(selectedMode.ordinal() + 1) % values.length];
				button.setMessage(C_BUTTON_MODE.apply(selectedMode.component));
			}
		));
		
		addDrawableChild(Widgets.button(
			C_BUTTON_BACK,
			centeredDim(width / 2 - 50, height - 20, 90, 20),
			button -> MinecraftClient.getInstance().setScreen(editor)
		));
		addDrawableChild(Widgets.button(
			C_BUTTON_APPLY,
			centeredDim(width / 2 + 50, height - 20, 90, 20),
			button -> {
				selectedMode.action.accept(editor.selection);
				MinecraftClient.getInstance().setScreen(new LoadoutEditorScreen(editor.selection));
			}
		));
	}
	
	@Override
	public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		
		drawText(C_HEADER, width / 2, 20, true);
	}
	
	private enum Mode {
		LEVEL(
			Text.translatable("frontutil.screen.loadout.reset.mode.level"),
			selection -> AddonClientData.getInstance().resetTempLoadout(
				selection.country, selection.getSkin(), selection.matchClass, selection.level
			)
		),
		CLASS(
			Text.translatable("frontutil.screen.loadout.reset.mode.class"),
			selection -> AddonClientData.getInstance().resetTempLoadoutAllLevels(
				selection.country, selection.getSkin(), selection.matchClass
			)
		),
		LEVEL_ALL_SKINS(
			Text.translatable("frontutil.screen.loadout.reset.mode.levelAllSkins"),
			selection -> {
				AddonClientData clientData = AddonClientData.getInstance();
				for (Map.Entry<LoadoutIndex.Identifier, List<Loadout>> entry : clientData.tempLoadouts.entrySet()) {
					LoadoutIndex.Identifier id = entry.getKey();
					if (id.country() == selection.country && id.matchClass() == selection.matchClass) {
						clientData.resetTempLoadout(id, selection.level);
					}
				}
			}
		),
		CLASS_ALL_SKINS(
			Text.translatable("frontutil.screen.loadout.reset.mode.classAllSkins"),
			selection -> {
				AddonClientData clientData = AddonClientData.getInstance();
				for (Map.Entry<LoadoutIndex.Identifier, List<Loadout>> entry : clientData.tempLoadouts.entrySet()) {
					LoadoutIndex.Identifier id = entry.getKey();
					if (id.country() == selection.country && id.matchClass() == selection.matchClass) {
						clientData.resetTempLoadoutAllLevels(id);
					}
				}
			}
		),
		SKIN(
			Text.translatable("frontutil.screen.loadout.reset.mode.skin"),
			selection -> {
				AddonClientData clientData = AddonClientData.getInstance();
				for (Map.Entry<LoadoutIndex.Identifier, List<Loadout>> entry : clientData.tempLoadouts.entrySet()) {
					LoadoutIndex.Identifier id = entry.getKey();
					if (id.country() == selection.country && id.skin().equals(selection.getSkin())) {
						clientData.resetTempLoadoutAllLevels(id);
					}
				}
			}
		),
		NATION(
			Text.translatable("frontutil.screen.loadout.reset.mode.nation"),
			selection -> {
				AddonClientData clientData = AddonClientData.getInstance();
				for (Map.Entry<LoadoutIndex.Identifier, List<Loadout>> entry : clientData.tempLoadouts.entrySet()) {
					LoadoutIndex.Identifier id = entry.getKey();
					if (id.country() == selection.country) {
						clientData.resetTempLoadoutAllLevels(id);
					}
				}
			}
		),
		EVERYTHING(
			Text.translatable("frontutil.screen.loadout.reset.mode.everything").formatted(Formatting.RED),
			selection -> AddonClientData.getInstance().resetLoadouts()
		);
		
		private final Text component;
		private final Consumer<LoadoutEditorScreen.Selection> action;
		
		Mode(Text component, Consumer<LoadoutEditorScreen.Selection> action) {
			this.component = component;
			this.action = action;
		}
	}
}
