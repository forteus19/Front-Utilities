package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.gui.layer.BFAbstractGuiLayer;
import com.boehmod.blockfront.client.gui.layer.MatchGuiLayer;
import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.base.IHasCapturePoints;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.client.data.AddonClientConfig;
import red.vuis.frontutil.client.render.AddonRendering;

@Mixin(MatchGuiLayer.class)
public abstract class MatchGuiLayerMixin extends BFAbstractGuiLayer {
	@ModifyConstant(
		method = "method_495",
		constant = @Constant(
			intValue = 16
		)
	)
	private int killFeedSpacing(int constant) {
		return AddonClientConfig.isNostalgiaMode() ? 12 : constant;
	}
	
	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lcom/boehmod/blockfront/game/AbstractGamePlayerManager;getPlayerUUIDs()Ljava/util/Set;"
		)
	)
	private void addOldCapturePointRendering(GuiGraphics graphics, DeltaTracker delta, BFClientManager manager, CallbackInfo ci, @Local AbstractGame<?, ?, ?> game, @Local PoseStack poseStack, @Local Font font) {
		if (AddonClientConfig.isNostalgiaMode() && game instanceof IHasCapturePoints<?, ?> cpGame) {
			AddonRendering.oldCapturePoints(poseStack, graphics, font, game, cpGame.getCapturePoints(), graphics.guiWidth() / 2, BFRendering.getRenderTime());
		}
	}
}
