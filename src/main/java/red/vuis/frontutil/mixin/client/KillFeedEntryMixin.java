package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.common.match.kill.KillEntryType;
import com.boehmod.blockfront.common.match.kill.KillFeedEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.client.data.config.AddonClientConfig;

@Mixin(KillFeedEntry.class)
public abstract class KillFeedEntryMixin {
	@Shadow
	private @NotNull KillEntryType type;
	
	@SuppressWarnings("deprecation")
	@Inject(
		method = "method_3214",
		at = @At("HEAD"),
		cancellable = true
	)
	private void renderOldBackground(DrawContext context, MatrixStack matrices, float width, CallbackInfo ci) {
		if (!AddonClientConfig.getMatchHudStyle().isOldKillFeed()) {
			return;
		}
		ci.cancel();
		
		int color = BFRendering.translucentBlack() - (type == KillEntryType.DEFAULT ? 0 : 0x22000000);
		
		BFRendering.rectangle(matrices, context, 0f, 0f, width, 11f, color);
		BFRendering.orderedRectangle(matrices, width, 0f, 2f, 11f, color, 1);
		BFRendering.orderedRectangle(matrices, -2f, 0f, 2f, 11f, color, 3);
	}
}
