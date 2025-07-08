package red.vuis.frontutil.client.render;

import java.util.List;

import com.boehmod.blockfront.client.event.BFRenderFrameSubscriber;
import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.game.AbstractCapturePoint;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.util.BFRes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import red.vuis.frontutil.mixin.GameStageTimerAccessor;

import static red.vuis.frontutil.util.AddonAccessors.applyGameStageTimer;

public final class AddonRendering {
	public static final ResourceLocation CPOINT_ARROW_RIGHT_BLACK = BFRes.loc("textures/gui/game/domination/cpoint_arrow_right_black.png");
	public static final ResourceLocation CPOINT_ARROW_LEFT_BLACK = BFRes.loc("textures/gui/game/domination/cpoint_arrow_left_black.png");
	
	private AddonRendering() {
	}
	
	@SuppressWarnings("deprecation")
	public static void oldCapturePoints(PoseStack poseStack, GuiGraphics graphics, Font font, AbstractGame<?, ?, ?> game, List<? extends AbstractCapturePoint<?>> capturePoints, int x, float renderTime) {
		int numCapturePoints = capturePoints.size();
		
		int totalWidth = 16 * numCapturePoints + 2 * numCapturePoints;
		int startX = x - totalWidth / 2;
		float flicker = Mth.sin(renderTime / 5f);
		
		for (int i = 0; i < numCapturePoints; i++) {
			AbstractCapturePoint<?> capturePoint = capturePoints.get(i);
			
			int cpX = startX + i * 18 + 1;
			float alpha = Math.max(capturePoint.isBeingCaptured ? 0.5f * flicker : 0.5f, 0.01f);
			
			BFRendering.tintedTexture(poseStack, graphics, BFRenderFrameSubscriber.NEUTRAL_ICON_TEXTURE, cpX, 27f, 14f, 14f, 0f, alpha, capturePoint.method_3143(game));
			
			ResourceLocation cpIcon = capturePoint.icon;
			if (cpIcon != null) {
				BFRendering.texture(poseStack, graphics, cpIcon, cpX, 27, 14, 14, alpha);
			}
			
			BFRendering.centeredComponent2d(poseStack, font, graphics, Component.literal(capturePoint.name), cpX + 8f, 44f, 1f);
		}
	}
	
	public static Component oldTimerComponent(GameStageTimer timer) {
		int secondsRemaining = timer.getSecondsRemaining();
		return Component.literal(BFRendering.formatTime(secondsRemaining))
			.withStyle(secondsRemaining <= applyGameStageTimer(timer, GameStageTimerAccessor::getWarningThreshold) ?
				ChatFormatting.RED : ChatFormatting.WHITE)
			.withStyle(ChatFormatting.BOLD);
	}
	
	@SuppressWarnings("deprecation")
	public static void oldTimer(PoseStack poseStack, Font font, GuiGraphics graphics, int x, GameStageTimer timer) {
		BFRendering.rectangle(poseStack, graphics, x - 19f, 1f, 38f, 13f, BFRendering.translucentBlack());
		BFRendering.centeredComponent2d(poseStack, font, graphics, oldTimerComponent(timer), x, 4f);
	}
}
