package red.vuis.frontutil.client.render;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public abstract class RenderObject {
	protected final MinecraftClient client;
	protected VertexConsumerProvider.Immediate vertexConsumers;
	protected DrawContext context;
	protected MatrixStack matrices;
	protected Camera camera;
	protected Frustum frustum;
	
	protected RenderObject(MinecraftClient client) {
		this.client = client;
		vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
		camera = client.gameRenderer.getCamera();
		frustum = client.worldRenderer.getFrustum();
	}
	
	@OverridingMethodsMustInvokeSuper
	public void render() {
		context = new DrawContext(client, vertexConsumers);
		matrices = context.getMatrices();
	}
	
	protected void cameraAsOrigin() {
		AddonRendering.cameraAsOrigin(matrices, camera);
	}
	
	protected void billboardString(String text, Vec3d position, float scale) {
		AddonRendering.billboardString(matrices, camera, client.textRenderer, vertexConsumers, text, position, scale, true);
	}
	
	protected void billboardTexture(Identifier texture, Vec3d position, float width, float height) {
		AddonRendering.billboardTexture(matrices, camera, texture, position, width, height, true);
	}
	
	protected void boxOutline(Box box, int color) {
		AddonRendering.boxOutline(matrices, box, color, false);
	}
}
