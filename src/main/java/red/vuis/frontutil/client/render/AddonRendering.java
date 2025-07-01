package red.vuis.frontutil.client.render;

import java.util.List;

import com.boehmod.blockfront.client.event.BFRenderFrameSubscriber;
import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.game.AbstractCapturePoint;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.base.IHasCapturePoints;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class AddonRendering {
	private AddonRendering() {
	}
	
	public static <G extends AbstractGame<?, ?, ?> & IHasCapturePoints<?, ?>> void oldCapturePoints(PoseStack poseStack, GuiGraphics graphics, Font font, G game, int x, float renderTime) {
		List<? extends AbstractCapturePoint<?>> capturePoints = game.getCapturePoints();
		int numCapturePoints = capturePoints.size();
		
		int totalWidth = 16 * numCapturePoints + 2 * numCapturePoints;
		int startX = x - totalWidth / 2;
		float flicker = Mth.sin(renderTime / 5f);
		
		for (int i = 0; i < numCapturePoints; i++) {
			AbstractCapturePoint<?> capturePoint = capturePoints.get(i);
			
			int cpX = startX + i * 18 + 1;
			float alpha = Math.max(capturePoint.field_3222 ? 0.5f * flicker : 0.5f, 0.01f);
			
			BFRendering.tintedTexture(poseStack, graphics, BFRenderFrameSubscriber.NEUTRAL_ICON_TEXTURE, cpX, 27, 14, 14, 0f, alpha, capturePoint.method_3143(game));
			
			ResourceLocation cpIcon = capturePoint.icon;
			if (cpIcon != null) {
				BFRendering.texture(poseStack, graphics, cpIcon, cpX, 27, 14, 14, alpha);
			}
			
			BFRendering.centeredComponent2d(poseStack, font, graphics, Component.literal(capturePoint.name), cpX + 8, 44, 1f);
		}
	}
}
