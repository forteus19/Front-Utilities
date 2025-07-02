package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.dom.DominationGameClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.client.data.AddonClientConfig;

@Mixin(AbstractGameClient.class)
public abstract class AbstractGameClientMixin {
	@Inject(
		method = "method_2712",
		at = @At("HEAD"),
		cancellable = true
	)
	private void disableGameElementRendering(GuiGraphics graphics, Font font, PoseStack poseStack, int width, float renderTime, CallbackInfo ci) {
		if ((AbstractGameClient<?, ?>) (Object) this instanceof DominationGameClient && AddonClientConfig.isNostalgiaMode()) {
			ci.cancel();
		}
	}
}
