package red.vuis.frontutil.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class RenderObject {
	protected final Minecraft minecraft;
	protected MultiBufferSource.BufferSource buffer;
	protected GuiGraphics graphics;
	protected PoseStack poseStack;
	protected Camera camera;
	protected Frustum frustum;
	
	protected RenderObject(Minecraft minecraft) {
		this.minecraft = minecraft;
		buffer = minecraft.renderBuffers().bufferSource();
		camera = minecraft.gameRenderer.getMainCamera();
		frustum = minecraft.levelRenderer.getFrustum();
	}
	
	@MustBeInvokedByOverriders
	public void render() {
		graphics = new GuiGraphics(minecraft, buffer);
		poseStack = graphics.pose();
	}
	
	protected void cameraAsOrigin() {
		AddonRendering.cameraAsOrigin(poseStack, camera);
	}
	
	protected void billboardString(String text, Vec3 position, float scale) {
		AddonRendering.billboardString(poseStack, camera, minecraft.font, buffer, text, position, scale, true);
	}
	
	protected void billboardTexture(ResourceLocation texture, Vec3 position, float width, float height) {
		AddonRendering.billboardTexture(poseStack, camera, texture, position, width, height, true);
	}
	
	protected void boxOutline(AABB aabb, int color) {
		AddonRendering.boxOutline(poseStack, aabb, color, false);
	}
}
