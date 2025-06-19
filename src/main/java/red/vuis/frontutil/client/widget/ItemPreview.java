package red.vuis.frontutil.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemPreview implements Renderable {
	private final int x, y, size;
	private @Nullable ItemStack itemStack;
	public boolean visible = true;
	
	public ItemPreview(int x, int y, int size) {
		this.x = x;
		this.y = y;
		this.size = size;
	}
	
	public ItemPreview setItemStack(@Nullable ItemStack itemStack) {
		this.itemStack = itemStack;
		return this;
	}
	
	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (!visible || itemStack == null) {
			return;
		}
		
		RenderSystem.disableDepthTest();
		
		PoseStack pose = guiGraphics.pose();
		pose.pushPose();
		pose.translate(x, y, 0);
		pose.scale(size / 16f, size / 16f, 1);
		guiGraphics.renderFakeItem(itemStack, 0, 0);
		pose.popPose();
		
		RenderSystem.enableDepthTest();
	}
}
