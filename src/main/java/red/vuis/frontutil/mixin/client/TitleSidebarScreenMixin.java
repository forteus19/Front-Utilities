package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.client.gui.widget.BFButton;
import com.boehmod.blockfront.client.screen.SidebarScreen;
import com.boehmod.blockfront.client.screen.title.sidebar.TitleSidebarScreen;
import com.boehmod.blockfront.util.BFStyles;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ModListScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleSidebarScreen.class)
public abstract class TitleSidebarScreenMixin extends SidebarScreen {
	public TitleSidebarScreenMixin(@Nullable Screen screen, @NotNull Component component) {
		super(screen, component);
	}
	
	@Inject(
		method = "method_758",
		at = @At("TAIL")
	)
	private void addModsButton(CallbackInfo ci, @Local(ordinal = 1) int offsetX, @Local(ordinal = 3) int offsetY, @Local(ordinal = 2) int width) {
		addRenderableWidget(
			new BFButton(
				offsetX + 4, offsetY + 40, width, 18,
				Component.translatable("fml.menu.mods").withStyle(BFStyles.BOLD),
				button -> minecraft.setScreen(new ModListScreen((TitleSidebarScreen) (Object) this))
			)
				.method_383(Component.translatable("frontutil.menu.button.mods.tip"))
				.alignment(BFButton.Alignment.LEFT)
		);
	}
}
