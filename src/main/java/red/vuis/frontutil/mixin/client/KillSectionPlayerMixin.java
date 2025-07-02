package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.common.match.kill.KillSectionPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import red.vuis.frontutil.client.data.config.AddonClientConfig;
import red.vuis.frontutil.client.data.config.MatchHudStyle;

@Mixin(KillSectionPlayer.class)
public abstract class KillSectionPlayerMixin {
	@SuppressWarnings("deprecation")
	@Redirect(
		method = "method_3220",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/client/render/BFRendering;centeredComponent2dWithShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/network/chat/Component;II)V"
		)
	)
	private void replaceDrawCall(PoseStack poseStack, Font font, GuiGraphics graphics, Component component, int x, int y) {
		if (AddonClientConfig.getMatchHudStyle() == MatchHudStyle.MODERN) {
			BFRendering.centeredComponent2dWithShadow(poseStack, font, graphics, component, x, y);
		} else {
			BFRendering.centeredComponent2d(poseStack, font, graphics, component, x, y);
		}
	}
}
