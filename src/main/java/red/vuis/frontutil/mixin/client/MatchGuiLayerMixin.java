package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.gui.layer.BFAbstractGuiLayer;
import com.boehmod.blockfront.client.gui.layer.MatchGuiLayer;
import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.tag.IHasCapturePoints;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.client.data.config.AddonClientConfig;
import red.vuis.frontutil.client.data.config.MatchHudStyle;
import red.vuis.frontutil.client.render.AddonRendering;

@Mixin(MatchGuiLayer.class)
public abstract class MatchGuiLayerMixin extends BFAbstractGuiLayer {
	@ModifyConstant(
		method = "renderKillFeed",
		constant = @Constant(
			intValue = 16
		)
	)
	private int killFeedSpacing(int constant) {
		return AddonClientConfig.getMatchHudStyle() == MatchHudStyle.MODERN ? constant : 12;
	}
	
	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lcom/boehmod/blockfront/game/AbstractGamePlayerManager;getPlayerUUIDs()Ljava/util/Set;"
		)
	)
	private void addOldCapturePointRendering(DrawContext context, RenderTickCounter tick, BFClientManager manager, CallbackInfo ci, @Local AbstractGame<?, ?, ?> game, @Local MatrixStack matrices, @Local TextRenderer textRenderer) {
		if (AddonClientConfig.getMatchHudStyle() == MatchHudStyle.OLD && game instanceof IHasCapturePoints<?, ?> cpGame) {
			AddonRendering.oldCapturePoints(matrices, context, textRenderer, game, cpGame.getCapturePoints(), context.getScaledWindowWidth() / 2, BFRendering.getRenderTime());
		}
	}
}
