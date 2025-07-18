package red.vuis.frontutil.client.screen;

import java.util.List;
import java.util.function.Function;

import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.client.data.AddonClientData;
import red.vuis.frontutil.client.widget.IntegerEditBox;
import red.vuis.frontutil.client.widget.WeaponEditContainer;
import red.vuis.frontutil.client.widget.Widgets;
import red.vuis.frontutil.setup.LoadoutIndex;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;
import static red.vuis.frontutil.client.widget.WidgetDim.dim;
import static red.vuis.frontutil.client.widget.WidgetDim.sqrCenteredDim;

public class LoadoutEditorScreen extends AddonScreen {
	private static final Component C_BUTTON_COPY = Component.translatable("frontutil.screen.generic.button.copy");
	private static final Component C_BUTTON_RESET = Component.translatable("frontutil.screen.generic.button.reset");
	private static final Component C_BUTTON_CLOSE_AND_SYNC = Component.translatable("frontutil.screen.loadout.editor.button.closeAndSync");
	private static final Function<Object, Component> C_BUTTON_LEVEL = i -> Component.translatable("frontutil.screen.loadout.editor.button.level", i);
	private static final Component C_FOOTER_MULTIPLAYER = Component.translatable("frontutil.screen.loadout.editor.footer.multiplayer")
		.withStyle(ChatFormatting.GOLD);
	private static final Component C_HEADER = Component.translatable("frontutil.screen.loadout.editor.header");
	
	private static final String[] SLOT_LABELS = new String[]{
		"primary", "secondary", "melee", "offHand", "head", "chest", "legs", "feet"
	};
	
	private boolean initialized = false;
	
	private Button countryButton;
	private Button skinButton;
	private Button matchClassButton;
	private Button levelButton;
	
	// 0 - 7 = slots, 8 - 11 = extra
	private final WeaponEditContainer[] weaponContainers = new WeaponEditContainer[12];
	private Button addExtraButton;
	private Button removeExtraButton;
	
	private static final int EXTRA_OFFSET = 8;
	private static final int EXTRA_MAX = 4;
	private int numExtra = 0;
	
	private IntegerEditBox minimumXpBox;
	private int lastSlotY;
	
	Selection selection;
	
	LoadoutEditorScreen(Selection selection) {
		super(C_HEADER);
		this.selection = selection;
	}
	
	public LoadoutEditorScreen() {
		this(new Selection());
	}
	
	@Override
	protected void init() {
		super.init();
		
		addSelectionButtons();
		addSlotContainers(!initialized);
		addOtherWidgets(!initialized);
		addFooterButtons();
		
		if (!initialized) {
			loadLoadoutInfo();
		}
		updateSelectionState();
		
		initialized = true;
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		
		drawText(C_HEADER, width / 2, 20, true);
		if (Minecraft.getInstance().isLocalServer()) {
			drawText(C_FOOTER_MULTIPLAYER, width / 2, height - 40, true);
		}
		
		for (int i = 0; i < SLOT_LABELS.length; i++) {
			int y = 80 + i * 20;
			drawText(getLabel(SLOT_LABELS[i]), width / 2 - 230, y, false, true);
		}
		
		drawText(getLabel("extra", numExtra, EXTRA_MAX), width / 2 + 10, 80, false, true);
		drawText(getLabel("minimumXp"), width / 2 + 10, lastSlotY + 10, false, true);
	}
	
	private void addSelectionButtons() {
		countryButton = addRenderableWidget(Widgets.button(
			Component.empty(),
			centeredDim(width / 2 - 170, 45, 90, 20),
			button -> {
				saveLoadoutInfo();
				
				BFCountry[] values = BFCountry.values();
				selection.country = values[(selection.country.ordinal() + 1) % values.length];
				selection.skinIndex = 0;
				selection.level = 0;
				
				updateSelectionState();
				loadLoadoutInfo();
			}
		));
		
		skinButton = addRenderableWidget(Widgets.button(
			Component.empty(),
			centeredDim(width / 2 - 70, 45, 90, 20),
			button -> {
				saveLoadoutInfo();
				
				List<String> skins = LoadoutIndex.SKINS.get(selection.country);
				selection.skinIndex = (selection.skinIndex + 1) % skins.size();
				selection.level = 0;
				
				updateSelectionState();
				loadLoadoutInfo();
			}
		));
		
		matchClassButton = addRenderableWidget(Widgets.button(
			Component.empty(),
			centeredDim(width / 2 + 30, 45, 90, 20),
			button -> {
				saveLoadoutInfo();
				
				MatchClass[] values = MatchClass.values();
				selection.matchClass = values[(selection.matchClass.ordinal() + 1) % values.length];
				selection.level = 0;
				
				updateSelectionState();
				loadLoadoutInfo();
			}
		));
		
		levelButton = addRenderableWidget(Widgets.button(
			Component.empty(),
			centeredDim(width / 2 + 130, 45, 90, 20),
			button -> {
				saveLoadoutInfo();
				
				List<Loadout> loadouts = getSelectedTempLoadouts();
				if (loadouts != null && !loadouts.isEmpty()) {
					selection.level = (selection.level + 1) % loadouts.size();
				}
				
				updateSelectionState();
				loadLoadoutInfo();
			}
		));
		addRenderableWidget(Widgets.button(
			Component.literal("+"),
			sqrCenteredDim(width / 2 + 185, 45, 20),
			button -> {
				saveLoadoutInfo();
				
				AddonClientData clientData = AddonClientData.getInstance();
				List<Loadout> loadouts = clientData.tempLoadouts.computeIfAbsent(selection.getIdentifier(), k -> new ObjectArrayList<>());
				loadouts.add(++selection.level, new Loadout());
				
				updateSelectionState();
				loadLoadoutInfo();
			}
		));
		addRenderableWidget(Widgets.button(
			Component.literal("-"),
			sqrCenteredDim(width / 2 + 205, 45, 20),
			button -> {
				AddonClientData clientData = AddonClientData.getInstance();
				if (selection.level >= 0) {
					clientData.tempLoadouts.get(selection.getIdentifier()).remove(selection.level--);
				}
				
				updateSelectionState();
				loadLoadoutInfo();
			}
		));
	}
	
	private void addSlotContainers(boolean create) {
		for (int i = 0; i < 8; i++) {
			if (create) {
				lastSlotY = 70 + i * 20;
				weaponContainers[i] = addCompoundWidget(new WeaponEditContainer(
					this, font, dim(width / 2 - 170, lastSlotY, 160, 20)
				));
			} else {
				addCompoundWidget(weaponContainers[i]);
			}
		}
		for (int i = 0; i < EXTRA_MAX; i++) {
			if (create) {
				int y = 70 + i * 20;
				WeaponEditContainer container = addCompoundWidget(new WeaponEditContainer(
					this, font, dim(width / 2 + 70, y, 160, 20)
				));
				container.setVisible(false);
				weaponContainers[i + EXTRA_OFFSET] = container;
			} else {
				addCompoundWidget(weaponContainers[i + EXTRA_OFFSET]);
			}
		}
		
		addExtraButton = addRenderableWidget(Widgets.button(
			Component.literal("+"),
			dim(width / 2 + 70, 70 + numExtra * 20, 80, 12),
			button -> {
				weaponContainers[numExtra + EXTRA_OFFSET].setVisible(true);
				numExtra++;
				updateExtraButtons();
			}
		));
		removeExtraButton = addRenderableWidget(Widgets.button(
			Component.literal("-"),
			dim(width / 2 + 150, 70 + numExtra * 20, 80, 12),
			button -> {
				weaponContainers[(numExtra - 1) + EXTRA_OFFSET].setVisible(false).clear();
				numExtra--;
				updateExtraButtons();
			}
		));
	}
	
	private void addOtherWidgets(boolean create) {
		if (create) {
			minimumXpBox = addRenderableWidget(new IntegerEditBox(
				font,
				dim(width / 2 + 70, lastSlotY, 120, 20),
				Component.empty()
			));
		} else {
			addRenderableWidget(minimumXpBox);
		}
	}
	
	private void addFooterButtons() {
		addRenderableWidget(Widgets.button(
			C_BUTTON_RESET,
			centeredDim(width / 2 - 100, height - 20, 90, 20),
			button -> Minecraft.getInstance().setScreen(new LoadoutResetScreen(this))
		));
		
		addRenderableWidget(Widgets.button(
			C_BUTTON_COPY,
			centeredDim(width / 2, height - 20, 90, 20),
			button -> Minecraft.getInstance().setScreen(new LoadoutCopyScreen(this))
		));
		
		addRenderableWidget(Widgets.button(
			C_BUTTON_CLOSE_AND_SYNC,
			centeredDim(width / 2 + 100, height - 20, 90, 20),
			button -> {
				saveLoadoutInfo();
				AddonClientData.getInstance().syncTempLoadouts();
				Minecraft.getInstance().setScreen(null);
			}
		));
	}
	
	private @Nullable List<Loadout> getSelectedTempLoadouts() {
		return AddonClientData.getInstance().getTempLoadouts(selection.country, selection.getSkin(), selection.matchClass);
	}
	
	private void updateSelectionState() {
		countryButton.setMessage(Component.literal(selection.country.getName()));
		skinButton.setMessage(Component.literal(StringUtils.capitalize(LoadoutIndex.SKINS.get(selection.country).get(selection.skinIndex))));
		matchClassButton.setMessage(Component.translatable(selection.matchClass.getDisplayTitle()));
		
		List<Loadout> loadouts = getSelectedTempLoadouts();
		if (loadouts != null && !loadouts.isEmpty()) {
			if (selection.level < 0) {
				selection.level = 0;
			}
			
			levelButton.setMessage(C_BUTTON_LEVEL.apply(selection.level + 1));
			levelButton.active = true;
			for (WeaponEditContainer container : weaponContainers) {
				container.setActive(true);
			}
			
			addExtraButton.active = true;
			removeExtraButton.active = true;
		} else {
			selection.level = -1;
			
			levelButton.setMessage(C_BUTTON_LEVEL.apply("N/A"));
			levelButton.active = false;
			for (WeaponEditContainer container : weaponContainers) {
				container.setActive(false);
			}
			
			addExtraButton.active = false;
			removeExtraButton.active = false;
		}
	}
	
	private void updateExtraButtons() {
		addExtraButton.setPosition(width / 2 + 70, 70 + numExtra * 20);
		addExtraButton.active = numExtra < EXTRA_MAX;
		removeExtraButton.setPosition(width / 2 + 150, 70 + numExtra * 20);
		removeExtraButton.active = numExtra > 0;
	}
	
	private Component getLabel(String suffix, Object... args) {
		return Component.translatable("frontutil.screen.loadout.editor.label." + suffix, args);
	}
	
	private void saveLoadoutInfo() {
		if (selection.level < 0) {
			return;
		}
		
		Loadout loadout = new Loadout(
			weaponContainers[0].getValue(),
			weaponContainers[1].getValue(),
			weaponContainers[2].getValue(),
			weaponContainers[3].getValue(),
			weaponContainers[4].getValue(),
			weaponContainers[5].getValue(),
			weaponContainers[6].getValue(),
			weaponContainers[7].getValue()
		);
		for (int i = 0; i < EXTRA_MAX; i++) {
			ItemStack extra = weaponContainers[i + EXTRA_OFFSET].getValue();
			if (extra != ItemStack.EMPTY) {
				loadout.addExtra(extra);
			}
		}
		loadout.setMinimumXp(minimumXpBox.getIntValue().orElse(0));
		
		AddonClientData.getInstance().setTempLoadout(
			selection.country, selection.getSkin(), selection.matchClass, selection.level, loadout);
	}
	
	private void loadLoadoutInfo() {
		if (selection.level < 0) {
			for (WeaponEditContainer container : weaponContainers) {
				container.clear();
			}
			for (int i = 0; i < EXTRA_MAX; i++) {
				weaponContainers[i + EXTRA_OFFSET].setVisible(false);
			}
			
			numExtra = 0;
			addExtraButton.setPosition(width / 2 + 70, 70);
			removeExtraButton.setPosition(width / 2 + 150, 70);
			
			return;
		}
		
		List<Loadout> loadouts = getSelectedTempLoadouts();
		if (loadouts == null) {
			return;
		}
		Loadout loadout = loadouts.get(selection.level);
		
		for (int i = 0; i < 8; i++) {
			weaponContainers[i].setValue(LoadoutIndex.SLOT_FUNCS.get(i).apply(loadout));
		}
		
		List<ItemStack> extra = loadout.getExtra();
		if (extra.size() > EXTRA_MAX) {
			AddonConstants.LOGGER.warn("Loadout has more than {} extra items!", EXTRA_MAX);
		}
		
		numExtra = 0;
		for (int i = 0; i < EXTRA_MAX; i++) {
			WeaponEditContainer container = weaponContainers[i + EXTRA_OFFSET];
			
			if (i >= extra.size()) {
				container.clear();
				container.setVisible(false);
				continue;
			}
			
			ItemStack itemStack = extra.get(i);
			if (itemStack != null && !itemStack.isEmpty()) {
				container.setValue(extra.get(i));
				container.setVisible(true);
				numExtra++;
			} else {
				container.clear();
				container.setVisible(false);
			}
		}
		
		updateExtraButtons();
		
		minimumXpBox.setIntValue(loadout.getMinimumXp());
	}
	
	public static final class Selection {
		public BFCountry country = BFCountry.UNITED_STATES;
		public int skinIndex = 0;
		public MatchClass matchClass = MatchClass.CLASS_RIFLEMAN;
		public int level = 0;
		
		public String getSkin() {
			return LoadoutIndex.SKINS.get(country).get(skinIndex);
		}
		
		public LoadoutIndex.Identifier getIdentifier() {
			return new LoadoutIndex.Identifier(country, getSkin(), matchClass);
		}
	}
}
