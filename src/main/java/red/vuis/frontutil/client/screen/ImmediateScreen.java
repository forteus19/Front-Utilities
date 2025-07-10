package red.vuis.frontutil.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public abstract class ImmediateScreen extends Screen {
	private GuiGraphics guiGraphics;
	
	public ImmediateScreen(Component title) {
		super(title);
	}
	
	@Override
	@MustBeInvokedByOverriders
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		this.guiGraphics = guiGraphics;
	}
	
	protected void drawText(Component text, int x, int y, float scale, boolean centeredX, boolean centeredY) {
		PoseStack pose = guiGraphics.pose();
		pose.pushPose();
		int dx = centeredX ? x - font.width(text) / 2 : x;
		int dy = centeredY ? y - font.lineHeight / 2 : y;
		pose.translate(dx, dy, 0);
		if (scale != 1f) {
			pose.scale(scale, scale, 1);
		}
		guiGraphics.drawString(font, text, 0, 0, 0xFFFFFFFF, true);
		pose.popPose();
	}
	
	protected void drawText(Component text, int x, int y, boolean centeredX, boolean centeredY) {
		drawText(text, x, y, 1f, centeredX, centeredY);
	}
	
	protected void drawText(Component text, int x, int y, boolean centered) {
		drawText(text, x, y, centered, centered);
	}
}
