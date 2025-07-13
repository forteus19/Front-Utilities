package red.vuis.frontutil.client.render;

import java.util.List;

import com.boehmod.blockfront.client.event.BFRenderFrameSubscriber;
import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.game.AbstractCapturePoint;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.util.BFRes;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import red.vuis.frontutil.mixin.GameStageTimerAccessor;

import static red.vuis.frontutil.util.AddonAccessors.applyGameStageTimer;
import static red.vuis.frontutil.util.ColorUtils.alphaFloat;
import static red.vuis.frontutil.util.ColorUtils.blueFloat;
import static red.vuis.frontutil.util.ColorUtils.greenFloat;
import static red.vuis.frontutil.util.ColorUtils.redFloat;

public final class AddonRendering {
	public static final Identifier CPOINT_ARROW_RIGHT_BLACK = BFRes.loc("textures/gui/game/domination/cpoint_arrow_right_black.png");
	public static final Identifier CPOINT_ARROW_LEFT_BLACK = BFRes.loc("textures/gui/game/domination/cpoint_arrow_left_black.png");
	
	private AddonRendering() {
	}
	
	public static void cameraAsOrigin(MatrixStack matrices, Camera camera) {
		Vec3d pos = camera.getPos();
		matrices.translate(-pos.x, -pos.y, -pos.z);
	}
	
	public static void translate(MatrixStack matrices, Vec3d vec) {
		matrices.translate(vec.x, vec.y, vec.z);
	}
	
	public static void depthTest(boolean enabled) {
		if (enabled) {
			RenderSystem.enableDepthTest();
		} else {
			RenderSystem.disableDepthTest();
		}
	}
	
	public static void billboard(MatrixStack matrices, Camera camera) {
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
	}
	
	public static void billboardString(MatrixStack matrices, Camera camera, TextRenderer textRenderer, VertexConsumerProvider.Immediate vertexConsumers, String text, Vec3d position, float scale, boolean depthTest) {
		matrices.push();
		
		translate(matrices, position);
		billboard(matrices, camera);
		float s = -0.025f * scale;
		matrices.scale(s, s, s);
		
		depthTest(depthTest);
		textRenderer.draw(text, -textRenderer.getWidth(text) / 2f, 0, 0xFFFFFFFF, true, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		
		matrices.pop();
	}
	
	public static void texture(MatrixStack matrices, Identifier texture, float x, float y, float z, float width, float height, boolean depthTest) {
		RenderSystem.depthMask(true);
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		
		float x2 = x + width;
		float y2 = y + height;
		builder.vertex(matrix, x, y, z).texture(0, 0);
		builder.vertex(matrix, x, y2, z).texture(0, 1);
		builder.vertex(matrix, x2, y2, z).texture(1, 1);
		builder.vertex(matrix, x2, y, z).texture(1, 0);
		
		depthTest(depthTest);
		BufferRenderer.drawWithGlobalProgram(builder.end());
	}
	
	public static void billboardTexture(MatrixStack matrices, Camera camera, Identifier texture, Vec3d position, float width, float height, boolean depthTest) {
		matrices.push();
		
		translate(matrices, position);
		billboard(matrices, camera);
		matrices.scale(-1, -1, 1);
		
		texture(matrices, texture, -width / 2f, -height / 2f, 0, width, height, depthTest);
		
		matrices.pop();
	}
	
	public static void boxOutline(MatrixStack matrices, Box box, int color, boolean depthTest) {
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
		
		BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		
		WorldRenderer.drawBox(matrices, builder, box, redFloat(color), greenFloat(color), blueFloat(color), alphaFloat(color));
		
		depthTest(depthTest);
		BufferRenderer.drawWithGlobalProgram(builder.end());
	}
	
	@SuppressWarnings("deprecation")
	public static void oldCapturePoints(MatrixStack matrices, DrawContext context, TextRenderer textRenderer, AbstractGame<?, ?, ?> game, List<? extends AbstractCapturePoint<?>> capturePoints, int x, float renderTime) {
		int numCapturePoints = capturePoints.size();
		
		int totalWidth = 16 * numCapturePoints + 2 * numCapturePoints;
		int startX = x - totalWidth / 2;
		float flicker = MathHelper.sin(renderTime / 5f);
		
		for (int i = 0; i < numCapturePoints; i++) {
			AbstractCapturePoint<?> capturePoint = capturePoints.get(i);
			
			int cpX = startX + i * 18 + 1;
			float alpha = Math.max(capturePoint.isBeingCaptured ? 0.5f * flicker : 0.5f, 0.01f);
			
			BFRendering.tintedTexture(matrices, context, BFRenderFrameSubscriber.NEUTRAL_ICON_TEXTURE, cpX, 27f, 14f, 14f, 0f, alpha, capturePoint.method_3143(game));
			
			Identifier cpIcon = capturePoint.icon;
			if (cpIcon != null) {
				BFRendering.texture(matrices, context, cpIcon, cpX, 27, 14, 14, alpha);
			}
			
			BFRendering.centeredComponent2d(matrices, textRenderer, context, Text.literal(capturePoint.name), cpX + 8f, 44f, 1f);
		}
	}
	
	public static Text oldTimerText(GameStageTimer timer) {
		int secondsRemaining = timer.getSecondsRemaining();
		return Text.literal(BFRendering.formatTime(secondsRemaining))
			.formatted(secondsRemaining <= applyGameStageTimer(timer, GameStageTimerAccessor::getWarningThreshold) ?
				Formatting.RED : Formatting.WHITE)
			.formatted(Formatting.BOLD);
	}
	
	@SuppressWarnings("deprecation")
	public static void oldTimer(MatrixStack matrices, TextRenderer textRenderer, DrawContext context, int x, GameStageTimer timer) {
		BFRendering.rectangle(matrices, context, x - 19f, 1f, 38f, 13f, BFRendering.translucentBlack());
		BFRendering.centeredComponent2d(matrices, textRenderer, context, oldTimerText(timer), x, 4f);
	}
}
