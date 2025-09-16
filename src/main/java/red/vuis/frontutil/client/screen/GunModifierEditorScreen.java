package red.vuis.frontutil.client.screen;

import com.boehmod.blockfront.common.item.GunItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.client.widget.ItemPreview;
import red.vuis.frontutil.client.widget.Widgets;

import static red.vuis.frontutil.client.widget.WidgetDim.centeredDim;
import static red.vuis.frontutil.client.widget.WidgetDim.sqrCenteredDim;

public class GunModifierEditorScreen extends AddonScreen {
	private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.header");
	
	private final GunItem item;
	private final String itemId;
	
	public GunModifierEditorScreen(GunItem item) {
		super(C_HEADER);
		this.item = item;
		this.itemId = Registries.ITEM.getId(item).toString();
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
		
		System.out.println(drawables.size());
	}
	
	@Override
	public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		
		drawText(C_HEADER, width / 2, 20, true);
		
		drawText(Text.literal(itemId), width / 2, 60, true);
	}
	
	private class Ammo extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.ammo.header");
		
		public Ammo() {
			super(C_HEADER);
		}
		
		@Override
		public void close() {
			assert client != null;
			client.setScreen(GunModifierEditorScreen.this);
		}
	}
	
	private class Damage extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.damage.header");
		
		public Damage() {
			super(C_HEADER);
		}
		
		@Override
		public void close() {
			assert client != null;
			client.setScreen(GunModifierEditorScreen.this);
		}
	}
	
	private class FireModes extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.fireModes.header");
		
		public FireModes() {
			super(C_HEADER);
		}
		
		@Override
		public void close() {
			assert client != null;
			client.setScreen(GunModifierEditorScreen.this);
		}
	}
	
	private class Spread extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.spread.header");
		
		public Spread() {
			super(C_HEADER);
		}
		
		@Override
		public void close() {
			assert client != null;
			client.setScreen(GunModifierEditorScreen.this);
		}
	}
	
	private class Other extends AddonScreen {
		private static final Text C_HEADER = Text.translatable("frontutil.screen.gun.modifier.editor.other.header");
		
		public Other() {
			super(C_HEADER);
		}
		
		@Override
		public void close() {
			assert client != null;
			client.setScreen(GunModifierEditorScreen.this);
		}
	}
}
