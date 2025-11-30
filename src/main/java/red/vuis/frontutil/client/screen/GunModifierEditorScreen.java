package red.vuis.frontutil.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.boehmod.blockfront.common.item.GunItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.client.data.AddonClientData;
import red.vuis.frontutil.client.widget.FloatFieldWidget;
import red.vuis.frontutil.client.widget.IntegerFieldWidget;
import red.vuis.frontutil.client.widget.ItemPreview;
import red.vuis.frontutil.client.widget.Widgets;
import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.setup.GunModifierIndex;
import red.vuis.frontutil.util.math.FloatBounds;
import red.vuis.frontutil.util.math.IntBounds;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;
import static red.vuis.frontutil.client.widget.WidgetDim.sqrCenteredDim;

public class GunModifierEditorScreen extends AddonScreen {
	private static final Text C_BUTTON_BACK = Text.translatable("gui.back");
	private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.header");
	
	private final GunItem item;
	private final String itemId;
	private final GunModifier defaultModifier;
	private GunModifier modifier;
	
	public GunModifierEditorScreen(GunItem item) {
		super(C_HEADER);
		this.item = item;
		this.itemId = Registries.ITEM.getId(item).toString();
		RegistryEntry<Item> entry = Registries.ITEM.getEntry(item);
		this.defaultModifier = GunModifierIndex.DEFAULT.get(entry);
		this.modifier = AddonClientData.getInstance().tempGunModifiers.getOrDefault(entry, defaultModifier);
	}
	
	@Override
	protected void init() {
		super.init();
		assert client != null;
		
		addDrawable(new ItemPreview(sqrCenteredDim(width / 2, 80, 40)).setItemStack(new ItemStack(item)));
		
		addDrawableChild(Widgets.button(
			Ammo.C_HEADER,
			centeredDim(width / 2, 120, 100, 20),
			button -> client.setScreen(new Ammo())
		));
		addDrawableChild(Widgets.button(
			Damage.C_HEADER,
			centeredDim(width / 2, 145, 100, 20),
			button -> client.setScreen(new Damage())
		));
		addDrawableChild(Widgets.button(
			FireModes.C_HEADER,
			centeredDim(width / 2, 170, 100, 20),
			button -> client.setScreen(new FireModes())
		));
		addDrawableChild(Widgets.button(
			Spread.C_HEADER,
			centeredDim(width / 2, 195, 100, 20),
			button -> client.setScreen(new Spread())
		));
		addDrawableChild(Widgets.button(
			Other.C_HEADER,
			centeredDim(width / 2, 220, 100, 20),
			button -> client.setScreen(new Other())
		));
	}
	
	@Override
	public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		
		drawText(C_HEADER, width / 2, 20, true);
		
		drawText(Text.literal(itemId), width / 2, 60, true);
	}
	
	private <T> T modifierPart(Function<GunModifier, Optional<T>> getter) {
		return getter.apply(modifier).orElseGet(() -> getter.apply(defaultModifier).orElseThrow());
	}
	
	private class Ammo extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.ammo.header");
		private static final Text C_LABEL_MAGAZINE = Text.translatable("frontutil.screen.gun.modifier.editor.ammo.label.magazine");
		private static final Text C_LABEL_RESERVE = Text.translatable("frontutil.screen.gun.modifier.editor.ammo.label.reserve");
		private static final IntBounds AMMO_BOUNDS = IntBounds.ofMin(0);
		
		private IntegerFieldWidget magazineField;
		private IntegerFieldWidget reserveField;
		
		public Ammo() {
			super(C_HEADER);
		}
		
		@Override
		protected void init() {
			super.init();
			
			GunModifier.Ammo ammo = modifierPart(GunModifier::ammo);
			
			magazineField = addDrawableChild(new IntegerFieldWidget(
				textRenderer,
				centeredDim(width / 2, 80, 100, 20),
				Text.empty(),
				AMMO_BOUNDS
			));
			magazineField.setInt(ammo.magazine());
			
			reserveField = addDrawableChild(new IntegerFieldWidget(
				textRenderer,
				centeredDim(width / 2, 120, 100, 20),
				Text.empty(),
				AMMO_BOUNDS
			));
			reserveField.setInt(ammo.reserve());
			
			addDrawableChild(Widgets.button(
				C_BUTTON_BACK,
				centeredDim(width / 2, height - 20, 90, 20),
				button -> close()
			));
		}
		
		@Override
		public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
			super.render(context, mouseX, mouseY, delta);
			
			drawText(C_HEADER, width / 2, 20, true);
			
			drawText(C_LABEL_MAGAZINE, width / 2, 60, true);
			drawText(C_LABEL_RESERVE, width / 2, 100, true);
		}
		
		@Override
		public void close() {
			GunModifier.Ammo newAmmo = new GunModifier.Ammo(magazineField.getInt(), reserveField.getInt());
			if (!newAmmo.equals(defaultModifier.ammo().orElseThrow())) {
				modifier = modifier.withAmmo(newAmmo);
			}
			
			assert client != null;
			client.setScreen(GunModifierEditorScreen.this);
		}
	}
	
	private class Damage extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.damage.header");
		private static final Text C_LABEL_BODY = Text.translatable("frontutil.screen.gun.modifier.editor.damage.label.body");
		private static final Text C_LABEL_DISTANCE = Text.translatable("frontutil.screen.gun.modifier.editor.damage.label.distance");
		private static final Text C_LABEL_HEAD = Text.translatable("frontutil.screen.gun.modifier.editor.damage.label.head");
		private static final FloatBounds DISTANCE_BOUNDS = FloatBounds.ofMin(0);
		private static final FloatBounds BODY_BOUNDS = FloatBounds.ofMin(0);
		private static final FloatBounds HEAD_BOUNDS = FloatBounds.ofMin(0);
		
		private final List<EntryWidgetHolder> entries = new ArrayList<>();
		private ButtonWidget addButton;
		private ButtonWidget removeButton;
		
		public Damage() {
			super(C_HEADER);
		}
		
		@Override
		protected void init() {
			super.init();
			
			modifierPart(GunModifier::damage).forEach(this::addEntry);
			
			addButton = addDrawableChild(Widgets.button(
				Text.literal("+"),
				centeredDim(width / 2 - 60, 76 + entries.size() * 20, 120, 12),
				button -> {
					float distance = entries.isEmpty() ? 0 : entries.getLast().distance.getFloat();
					addEntry(new GunModifier.Damage(distance, 0, 0));
					updateNumButtons();
				}
			));
			removeButton = addDrawableChild(Widgets.button(
				Text.literal("-"),
				centeredDim(width / 2 + 60, 76 + entries.size() * 20, 120, 12),
				button -> {
					entries.getLast().forEach(this::remove);
					entries.removeLast();
					updateNumButtons();
				}
			));
			updateNumButtons();
			
			addDrawableChild(Widgets.button(
				C_BUTTON_BACK,
				centeredDim(width / 2, height - 20, 90, 20),
				button -> close()
			));
		}
		
		@Override
		public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
			super.render(context, mouseX, mouseY, delta);
			
			drawText(C_HEADER, width / 2, 20, true);
			
			drawText(C_LABEL_DISTANCE, width / 2 - 80, 60, true);
			drawText(C_LABEL_BODY, width / 2, 60, true);
			drawText(C_LABEL_HEAD, width / 2 + 80, 60, true);
		}
		
		@Override
		public void close() {
			List<GunModifier.Damage> newDamage = entries.stream().map(holder -> new GunModifier.Damage(
				holder.head.getFloat(), holder.body.getFloat(), holder.distance.getFloat())
			).toList();
			if (!newDamage.equals(defaultModifier.damage().orElseThrow())) {
				modifier = modifier.withDamage(newDamage);
			}
			
			assert client != null;
			client.setScreen(GunModifierEditorScreen.this);
		}
		
		private void addEntry(GunModifier.Damage damage) {
			FloatFieldWidget distanceField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				centeredDim(width / 2 - 80, 80 + entries.size() * 20, 80, 20),
				Text.empty(),
				DISTANCE_BOUNDS
			));
			distanceField.setFloat(damage.minDist());
			
			FloatFieldWidget bodyField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				centeredDim(width / 2, 80 + entries.size() * 20, 80, 20),
				Text.empty(),
				BODY_BOUNDS
			));
			bodyField.setFloat(damage.body());
			
			FloatFieldWidget headField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				centeredDim(width / 2 + 80, 80 + entries.size() * 20, 80, 20),
				Text.empty(),
				HEAD_BOUNDS
			));
			headField.setFloat(damage.head());
			
			entries.add(new EntryWidgetHolder(distanceField, bodyField, headField));
		}
		
		private void updateNumButtons() {
			centeredDim(width / 2 - 60, 76 + entries.size() * 20, 120, 12).apply(addButton);
			centeredDim(width / 2 + 60, 76 + entries.size() * 20, 120, 12).apply(removeButton);
			removeButton.active = entries.size() > 1;
		}
		
		private record EntryWidgetHolder(FloatFieldWidget distance, FloatFieldWidget body, FloatFieldWidget head) {
			public void forEach(Consumer<FloatFieldWidget> consumer) {
				consumer.accept(distance);
				consumer.accept(body);
				consumer.accept(head);
			}
		}
	}
	
	private class FireModes extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.fireModes.header");
		
		public FireModes() {
			super(C_HEADER);
		}
		
		@Override
		protected void init() {
			super.init();
			
			addDrawableChild(Widgets.button(
				C_BUTTON_BACK,
				centeredDim(width / 2, height - 20, 90, 20),
				button -> close()
			));
		}
		
		@Override
		public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
			super.render(context, mouseX, mouseY, delta);
			
			drawText(C_HEADER, width / 2, 20, true);
		}
		
		@Override
		public void close() {
			assert client != null;
			client.setScreen(GunModifierEditorScreen.this);
		}
	}
	
	private class Spread extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.spread.header");
		private static final Text C_LABEL_CRAWLING = Text.translatable("frontutil.screen.gun.modifier.editor.spread.label.crawling");
		private static final Text C_LABEL_CRAWLING_ADS = Text.translatable("frontutil.screen.gun.modifier.editor.spread.label.crawlingAds");
		private static final Text C_LABEL_IDLE = Text.translatable("frontutil.screen.gun.modifier.editor.spread.label.idle");
		private static final Text C_LABEL_IDLE_ADS = Text.translatable("frontutil.screen.gun.modifier.editor.spread.label.idleAds");
		private static final Text C_LABEL_JUMPING = Text.translatable("frontutil.screen.gun.modifier.editor.spread.label.jumping");
		private static final Text C_LABEL_WALKING = Text.translatable("frontutil.screen.gun.modifier.editor.spread.label.walking");
		private static final Text C_LABEL_WALKING_ADS = Text.translatable("frontutil.screen.gun.modifier.editor.spread.label.walkingAds");
		private static final FloatBounds SPREAD_BOUNDS = FloatBounds.ofMin(0);
		
		private FloatFieldWidget
			crawlingField, crawlingAdsField,
			idleField, idleAdsField,
			walkingField, walkingAdsField,
			jumpingField;
		
		public Spread() {
			super(C_HEADER);
		}
		
		@Override
		protected void init() {
			super.init();
			
			GunModifier.Spread spread = modifierPart(GunModifier::spread);
			
			crawlingField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				centeredDim(width / 2 - 60, 80, 100, 20),
				Text.empty(),
				SPREAD_BOUNDS
			));
			crawlingField.setFloat(spread.crawling());
			
			crawlingAdsField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				centeredDim(width / 2 + 60, 80, 100, 20),
				Text.empty(),
				SPREAD_BOUNDS
			));
			crawlingAdsField.setFloat(spread.crawlingAds());
			
			idleField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				centeredDim(width / 2 - 60, 120, 100, 20),
				Text.empty(),
				SPREAD_BOUNDS
			));
			idleField.setFloat(spread.idle());
			
			idleAdsField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				centeredDim(width / 2 + 60, 120, 100, 20),
				Text.empty(),
				SPREAD_BOUNDS
			));
			idleAdsField.setFloat(spread.idleAds());
			
			walkingField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				centeredDim(width / 2 - 60, 160, 100, 20),
				Text.empty(),
				SPREAD_BOUNDS
			));
			walkingField.setFloat(spread.walking());
			
			walkingAdsField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				centeredDim(width / 2 + 60, 160, 100, 20),
				Text.empty(),
				SPREAD_BOUNDS
			));
			walkingAdsField.setFloat(spread.walkingAds());
			
			jumpingField = addDrawableChild(new FloatFieldWidget(
				textRenderer,
				centeredDim(width / 2, 200, 100, 20),
				Text.empty(),
				SPREAD_BOUNDS
			));
			jumpingField.setFloat(spread.jumping());
			
			addDrawableChild(Widgets.button(
				C_BUTTON_BACK,
				centeredDim(width / 2, height - 20, 90, 20),
				button -> close()
			));
		}
		
		@Override
		public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
			super.render(context, mouseX, mouseY, delta);
			
			drawText(C_HEADER, width / 2, 20, true);
			
			drawText(C_LABEL_CRAWLING, width / 2 - 60, 60, true);
			drawText(C_LABEL_CRAWLING_ADS, width / 2 + 60, 60, true);
			drawText(C_LABEL_IDLE, width / 2 - 60, 100, true);
			drawText(C_LABEL_IDLE_ADS, width / 2 + 60, 100, true);
			drawText(C_LABEL_WALKING, width / 2 - 60, 140, true);
			drawText(C_LABEL_WALKING_ADS, width / 2 + 60, 140, true);
			drawText(C_LABEL_JUMPING, width / 2, 180, true);
		}
		
		@Override
		public void close() {
			GunModifier.Spread newSpread = new GunModifier.Spread(
				jumpingField.getFloat(),
				walkingField.getFloat(), walkingAdsField.getFloat(),
				idleField.getFloat(), idleAdsField.getFloat(),
				crawlingField.getFloat(), crawlingAdsField.getFloat()
			);
			if (!newSpread.equals(defaultModifier.spread().orElseThrow())) {
				modifier = modifier.withSpread(newSpread);
			}
			
			assert client != null;
			client.setScreen(GunModifierEditorScreen.this);
		}
	}
	
	private class Other extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.other.header");
		private static final Text C_LABEL_WEIGHT = Text.translatable("frontutil.screen.gun.modifier.editor.other.label.weight");
		
		public Other() {
			super(C_HEADER);
		}
		
		@Override
		protected void init() {
			super.init();
			
			addDrawableChild(Widgets.button(
				C_BUTTON_BACK,
				centeredDim(width / 2, height - 20, 90, 20),
				button -> close()
			));
		}
		
		@Override
		public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
			super.render(context, mouseX, mouseY, delta);
			
			drawText(C_HEADER, width / 2, 20, true);
		}
		
		@Override
		public void close() {
			assert client != null;
			client.setScreen(GunModifierEditorScreen.this);
		}
	}
}
