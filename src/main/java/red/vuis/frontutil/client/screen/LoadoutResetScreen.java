package red.vuis.frontutil.client.screen;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.boehmod.blockfront.common.match.Loadout;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import red.vuis.frontutil.client.data.AddonClientData;
import red.vuis.frontutil.client.widget.Widgets;
import red.vuis.frontutil.setup.LoadoutIndex;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;

public class LoadoutResetScreen extends AddonScreen {
	private static final Supplier<MutableComponent> C_BUTTON_APPLY = () -> Component.translatable("frontutil.screen.generic.button.apply");
	private static final Supplier<MutableComponent> C_BUTTON_BACK = () -> Component.translatable("frontutil.screen.generic.button.back");
	private static final Function<Object, MutableComponent> C_BUTTON_MODE = i -> Component.translatable("frontutil.screen.loadout.reset.button.mode", i);
	private static final Supplier<MutableComponent> C_HEADER = () -> Component.translatable("frontutil.screen.loadout.reset.header");
	
	private final LoadoutEditorScreen editor;
	private Mode selectedMode = Mode.LEVEL;
	
	public LoadoutResetScreen(LoadoutEditorScreen editor) {
		super(C_HEADER.get());
		this.editor = editor;
	}
	
	@Override
	protected void init() {
		super.init();
		
		addRenderableWidget(Widgets.button(
			C_BUTTON_MODE.apply(selectedMode.component),
			centeredDim(width / 2, 80, 140, 20),
			button -> {
				Mode[] values = Mode.values();
				selectedMode = values[(selectedMode.ordinal() + 1) % values.length];
				button.setMessage(C_BUTTON_MODE.apply(selectedMode.component));
			}
		));
		
		addRenderableWidget(Widgets.button(
			C_BUTTON_BACK.get(),
			centeredDim(width / 2 - 50, height - 20, 90, 20),
			button -> Minecraft.getInstance().setScreen(editor)
		));
		addRenderableWidget(Widgets.button(
			C_BUTTON_APPLY.get(),
			centeredDim(width / 2 + 50, height - 20, 90, 20),
			button -> {
				selectedMode.action.accept(editor.selection);
				Minecraft.getInstance().setScreen(new LoadoutEditorScreen(editor.selection));
			}
		));
	}
	
	@Override
	protected void render(int mouseX, int mouseY, float partialTick) {
		drawText(C_HEADER.get(), width / 2, 20, true);
	}
	
	private enum Mode {
		LEVEL(
			Component.translatable("frontutil.screen.loadout.reset.mode.level"),
			selection -> AddonClientData.getInstance().resetTempLoadout(
				selection.country, selection.getSkin(), selection.matchClass, selection.level
			)
		),
		CLASS(
			Component.translatable("frontutil.screen.loadout.reset.mode.class"),
			selection -> AddonClientData.getInstance().resetTempLoadoutAllLevels(
				selection.country, selection.getSkin(), selection.matchClass
			)
		),
		LEVEL_ALL_SKINS(
			Component.translatable("frontutil.screen.loadout.reset.mode.levelAllSkins"),
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
			Component.translatable("frontutil.screen.loadout.reset.mode.classAllSkins"),
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
			Component.translatable("frontutil.screen.loadout.reset.mode.skin"),
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
			Component.translatable("frontutil.screen.loadout.reset.mode.nation"),
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
			Component.translatable("frontutil.screen.loadout.reset.mode.everything").withStyle(ChatFormatting.RED),
			selection -> AddonClientData.getInstance().resetLoadouts()
		);
		
		private final Component component;
		private final Consumer<LoadoutEditorScreen.Selection> action;
		
		Mode(Component component, Consumer<LoadoutEditorScreen.Selection> action) {
			this.component = component;
			this.action = action;
		}
	}
}
