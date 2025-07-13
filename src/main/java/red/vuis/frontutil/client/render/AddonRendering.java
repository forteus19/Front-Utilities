package red.vuis.frontutil.client.render;

import java.util.List;

import com.boehmod.blockfront.client.event.BFRenderFrameSubscriber;
import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.game.AbstractCapturePoint;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.util.BFRes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import red.vuis.frontutil.mixin.GameStageTimerAccessor;

import static red.vuis.frontutil.util.AddonAccessors.applyGameStageTimer;
import static red.vuis.frontutil.util.ColorUtils.alphaFloat;
import static red.vuis.frontutil.util.ColorUtils.blueFloat;
import static red.vuis.frontutil.util.ColorUtils.greenFloat;
import static red.vuis.frontutil.util.ColorUtils.redFloat;

public final class AddonRendering {
	public static final ResourceLocation CPOINT_ARROW_RIGHT_BLACK = BFRes.loc("textures/gui/game/domination/cpoint_arrow_right_black.png");
	public static final ResourceLocation CPOINT_ARROW_LEFT_BLACK = BFRes.loc("textures/gui/game/domination/cpoint_arrow_left_black.png");
	
	private AddonRendering() {
	}
	
	public static void cameraAsOrigin(PoseStack poseStack, Camera camera) {
		Vec3 pos = camera.getPosition();
		poseStack.translate(-pos.x, -pos.y, -pos.z);
	}
	
	public static void translate(PoseStack poseStack, Vec3 vec) {
		poseStack.translate(vec.x, vec.y, vec.z);
	}
	
	public static void depthTest(boolean enabled) {
		if (enabled) {
			RenderSystem.enableDepthTest();
		} else {
			RenderSystem.disableDepthTest();
		}
	}
	
	public static void billboard(PoseStack poseStack, Camera camera) {
		poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
		poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
	}
	
	public static void billboardString(PoseStack poseStack, Camera camera, Font font, MultiBufferSource.BufferSource buffer, String string, Vec3 position, float scale, boolean depthTest) {
		poseStack.pushPose();
		
		translate(poseStack, position);
		billboard(poseStack, camera);
		float s = -0.025f * scale;
		poseStack.scale(s, s, s);
		
		depthTest(depthTest);
		font.drawInBatch(string, -font.width(string) / 2f, 0, 0xFFFFFFFF, true, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
		
		poseStack.popPose();
	}
	
	public static void texture(PoseStack poseStack, ResourceLocation texture, float x, float y, float z, float width, float height, boolean depthTest) {
		RenderSystem.depthMask(true);
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		
		float x2 = x + width;
		float y2 = y + height;
		builder.addVertex(matrix, x, y, z).setUv(0, 0);
		builder.addVertex(matrix, x, y2, z).setUv(0, 1);
		builder.addVertex(matrix, x2, y2, z).setUv(1, 1);
		builder.addVertex(matrix, x2, y, z).setUv(1, 0);
		
		depthTest(depthTest);
		BufferUploader.drawWithShader(builder.buildOrThrow());
	}
	
	public static void billboardTexture(PoseStack poseStack, Camera camera, ResourceLocation texture, Vec3 position, float width, float height, boolean depthTest) {
		poseStack.pushPose();
		
		translate(poseStack, position);
		billboard(poseStack, camera);
		poseStack.scale(-1, -1, 1);
		
		texture(poseStack, texture, -width / 2f, -height / 2f, 0, width, height, depthTest);
		
		poseStack.popPose();
	}
	
	public static void boxOutline(PoseStack poseStack, AABB box, int color, boolean depthTest) {
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
		
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
		
		LevelRenderer.renderLineBox(poseStack, builder, box, redFloat(color), greenFloat(color), blueFloat(color), alphaFloat(color));
		
		depthTest(depthTest);
		BufferUploader.drawWithShader(builder.buildOrThrow());
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
