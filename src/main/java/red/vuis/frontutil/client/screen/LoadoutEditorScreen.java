package red.vuis.frontutil.client.screen;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.client.widget.ItemEditContainer;
import red.vuis.frontutil.client.widget.Widgets;
import red.vuis.frontutil.setup.LoadoutIndex;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;
import static red.vuis.frontutil.client.widget.WidgetDim.dim;

public class LoadoutEditorScreen extends AddonScreen {
	private static final Supplier<Component> C_BUTTON_CLOSE = () -> Component.translatable("frontutil.screen.generic.button.close");
	private static final Function<Object, Component> C_BUTTON_LEVEL = i -> Component.translatable("frontutil.screen.loadout.editor.button.level", i);
	private static final Supplier<Component> C_HEADER = () -> Component.translatable("frontutil.screen.loadout.editor.header");
	
	private static final String[] SLOT_LABELS = new String[]{
		"primary", "secondary", "melee", "offHand", "head", "chest", "legs", "feet"
	};
	private static final List<Function<Loadout, ItemStack>> SLOT_FUNCS = List.of(
		Loadout::getPrimary, Loadout::getSecondary, Loadout::getMelee, Loadout::getOffHand,
		Loadout::getHead, Loadout::getChest, Loadout::getLegs, Loadout::getFeet
	);
	
	private Button countryButton;
	private Button skinButton;
	private Button matchClassButton;
	private Button levelButton;
	
	private final ItemEditContainer[] slotContainers = new ItemEditContainer[8];
	
	private BFCountry selectedCountry = BFCountry.UNITED_STATES;
	private int selectedSkinIndex = 0;
	private MatchClass selectedMatchClass = MatchClass.CLASS_RIFLEMAN;
	private int selectedLevel = 0;
	
	public LoadoutEditorScreen() {
		super(C_HEADER.get());
	}
	
	@Override
	protected void init() {
		super.init();
		
		addSelectionButtons();
		addSlotContainers();
		
		addRenderableWidget(Widgets.button(
			C_BUTTON_CLOSE.get(),
			centeredDim(width / 2, height - 20, 120, 20),
			button -> Minecraft.getInstance().setScreen(null)
		));
		
		updateSelectionState();
		loadLoadoutInfo();
	}
	
	@Override
	protected void render(int mouseX, int mouseY, float partialTick) {
		drawText(C_HEADER.get(), width / 2, 20, true);
		
		for (int i = 0; i < SLOT_LABELS.length; i++) {
			int y = 80 + i * 20;
			drawText(getLabel(SLOT_LABELS[i]), width / 2 - 230, y, false, true);
		}
	}
	
	private void addSelectionButtons() {
		countryButton = addRenderableWidget(Widgets.button(
			Component.empty(),
			centeredDim(width / 2 - 150, 45, 90, 20),
			button -> {
				saveLoadoutInfo();
				
				BFCountry[] values = BFCountry.values();
				selectedCountry = values[(selectedCountry.ordinal() + 1) % values.length];
				selectedSkinIndex = 0;
				selectedLevel = 0;
				
				updateSelectionState();
				loadLoadoutInfo();
			}
		));
		
		skinButton = addRenderableWidget(Widgets.button(
			Component.empty(),
			centeredDim(width / 2 - 50, 45, 90, 20),
			button -> {
				saveLoadoutInfo();
				
				List<String> skins = LoadoutIndex.SKINS.get(selectedCountry);
				selectedSkinIndex = (selectedSkinIndex + 1) % skins.size();
				
				updateSelectionState();
				loadLoadoutInfo();
			}
		));
		
		matchClassButton = addRenderableWidget(Widgets.button(
			Component.empty(),
			centeredDim(width / 2 + 50, 45, 90, 20),
			button -> {
				saveLoadoutInfo();
				
				MatchClass[] values = MatchClass.values();
				selectedMatchClass = values[(selectedMatchClass.ordinal() + 1) % values.length];
				selectedLevel = 0;
				
				updateSelectionState();
				loadLoadoutInfo();
			}
		));
		
		levelButton = addRenderableWidget(Widgets.button(
			Component.empty(),
			centeredDim(width / 2 + 150, 45, 90, 20),
			button -> {
				saveLoadoutInfo();
				
				DivisionData divisionData = getSelectedDivisionData();
				if (divisionData != null) {
					List<Loadout> loadouts = divisionData.getLoadouts().get(selectedMatchClass);
					if (loadouts != null && !loadouts.isEmpty()) {
						selectedLevel = (selectedLevel + 1) % divisionData.getLoadouts().get(selectedMatchClass).size();
					}
				}
				
				updateSelectionState();
				loadLoadoutInfo();
			}
		));
	}
	
	private void addSlotContainers() {
		for (int i = 0; i < slotContainers.length; i++) {
			int y = 70 + i * 20;
			slotContainers[i] = addItemEdit(
				font, dim(width / 2 - 170, y, 160, 20)
			);
		}
	}
	
	private void updateSelectionState() {
		countryButton.setMessage(Component.literal(selectedCountry.getName()));
		skinButton.setMessage(Component.literal(StringUtils.capitalize(LoadoutIndex.SKINS.get(selectedCountry).get(selectedSkinIndex))));
		matchClassButton.setMessage(Component.translatable(selectedMatchClass.getDisplayTitle()));
		
		DivisionData divisionData = getSelectedDivisionData();
		if (divisionData != null) {
			List<Loadout> loadouts = divisionData.getLoadouts().get(selectedMatchClass);
			if (loadouts != null && !loadouts.isEmpty()) {
				levelButton.setMessage(C_BUTTON_LEVEL.apply(selectedLevel + 1));
				levelButton.active = true;
				for (ItemEditContainer container : slotContainers) {
					container.setActive(true);
				}
			} else {
				levelButton.setMessage(C_BUTTON_LEVEL.apply("N/A"));
				levelButton.active = false;
				for (ItemEditContainer container : slotContainers) {
					container.setActive(false);
				}
				selectedLevel = -1;
			}
		}
	}
	
	private Component getLabel(String suffix) {
		return Component.translatable("frontutil.screen.loadout.editor.label." + suffix);
	}
	
	private void saveLoadoutInfo() {
	}
	
	private void loadLoadoutInfo() {
		if (selectedLevel < 0) {
			for (ItemEditContainer container : slotContainers) {
				container.clear();
			}
		}
		
		DivisionData divisionData = getSelectedDivisionData();
		if (divisionData == null) {
			return;
		}
		
		List<Loadout> loadouts = divisionData.getLoadouts().get(selectedMatchClass);
		if (loadouts != null) {
			for (int i = 0; i < slotContainers.length; i++) {
				slotContainers[i].setValue(SLOT_FUNCS.get(i).apply(loadouts.get(selectedLevel)));
			}
		}
	}
	
	private @Nullable DivisionData getSelectedDivisionData() {
		return DivisionData.getByCountryAndSkin(selectedCountry, LoadoutIndex.SKINS.get(selectedCountry).get(selectedSkinIndex));
	}
}
