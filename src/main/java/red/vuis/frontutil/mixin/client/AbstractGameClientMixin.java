package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.impl.dom.DominationGameClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.client.data.config.AddonClientConfig;
import red.vuis.frontutil.client.data.config.MatchHudStyle;

@Mixin(AbstractGameClient.class)
public abstract class AbstractGameClientMixin {
	@Inject(
		method = "renderGameElements",
		at = @At("HEAD"),
		cancellable = true
	)
	private void disableGameElementRendering(DrawContext context, TextRenderer textRenderer, MatrixStack matrices, int width, float renderTime, CallbackInfo ci) {
		if ((AbstractGameClient<?, ?>) (Object) this instanceof DominationGameClient && AddonClientConfig.getMatchHudStyle() != MatchHudStyle.MODERN) {
			ci.cancel();
		}
	}
	
	@ModifyConstant(
		method = "addKillFeedEntry",
		constant = @Constant(
			intValue = 5
		)
	)
	private int customKillFeedEntryMax(int constant) {
		return AddonClientConfig.getKillFeedLines();
	}
}
