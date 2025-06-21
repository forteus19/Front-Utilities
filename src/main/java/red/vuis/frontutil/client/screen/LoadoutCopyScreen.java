package red.vuis.frontutil.client.screen;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import red.vuis.frontutil.client.data.AddonClientData;
import red.vuis.frontutil.client.widget.Widgets;
import red.vuis.frontutil.setup.LoadoutIndex;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;
import static red.vuis.frontutil.client.widget.WidgetDim.sqrCenteredDim;

public class LoadoutCopyScreen extends AddonScreen {
	private static final Supplier<MutableComponent> C_BUTTON_APPLY = () -> Component.translatable("frontutil.screen.generic.button.apply");
	private static final Supplier<MutableComponent> C_BUTTON_BACK = () -> Component.translatable("frontutil.screen.generic.button.back");
	private static final Supplier<MutableComponent> C_CHECKBOX_EXCLUDE = () -> Component.translatable("frontutil.screen.loadout.copy.checkbox.exclude");
	private static final Supplier<MutableComponent> C_HEADER = () -> Component.translatable("frontutil.screen.loadout.copy.header");
	private static final Supplier<MutableComponent> C_MESSAGE = () -> Component.translatable("frontutil.screen.loadout.copy.message");
	
	private final LoadoutEditorScreen editor;
	private Checkbox excludeCheckbox;
	
	public LoadoutCopyScreen(LoadoutEditorScreen editor) {
		super(C_HEADER.get());
		this.editor = editor;
	}
	
	@Override
	protected void init() {
		super.init();
		
		excludeCheckbox = addRenderableOnly(Widgets.checkbox(
			font, sqrCenteredDim(width / 2, 100, 20), true
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
				AddonClientData clientData = AddonClientData.getInstance();
				Map<MatchClass, List<Loadout>> loadoutsToCopy = new EnumMap<>(MatchClass.class);
				List<String> targetSkins = new ObjectArrayList<>();
				
				for (Map.Entry<LoadoutIndex.Identifier, List<Loadout>> entry : clientData.tempLoadouts.entrySet()) {
					LoadoutIndex.Identifier id = entry.getKey();
					BFCountry country = id.country();
					String skin = id.skin();
					
					if (country == editor.selection.country) {
						if (skin.equals(editor.selection.getSkin())) {
							loadoutsToCopy.put(id.matchClass(), entry.getValue());
						} else if (
							!(excludeCheckbox.selected() &&
								((country == BFCountry.UNITED_STATES && skin.equals("continental")) ||
									(country == BFCountry.GREAT_BRITAIN && skin.equals("regulars"))))
						) {
							targetSkins.add(skin);
						}
					}
				}
				
				loadoutsToCopy.forEach((matchClass, loadouts) -> {
					for (String skin : targetSkins) {
						List<Loadout> originalLoadouts = clientData.tempLoadouts.computeIfAbsent(
							new LoadoutIndex.Identifier(editor.selection.country, skin, matchClass),
							k -> new ObjectArrayList<>()
						);
						originalLoadouts.clear();
						loadouts.stream().map(LoadoutIndex::cloneLoadout).forEach(originalLoadouts::add);
					}
				});
				
				Minecraft.getInstance().setScreen(new LoadoutEditorScreen());
			}
		));
	}
	
	@Override
	protected void render(int mouseX, int mouseY, float partialTick) {
		drawText(C_HEADER.get(), width / 2, 20, true);
		drawText(C_MESSAGE.get(), width / 2, 40, true);
		
		drawText(C_CHECKBOX_EXCLUDE.get(), width / 2, 80, true);
	}
}
