package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.client.render.block.BFBlockRenderer;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BFBlockRenderer.class)
public abstract class BFBlockRendererMixin {
	@Definition(id = "method_1284", method = "Lcom/boehmod/blockfront/client/render/block/BFBlockRenderer;method_1284(Lnet/minecraft/block/entity/BlockEntity;)Z")
	@Expression("this.method_1284(?) == 0")
	@ModifyExpressionValue(
		method = "render",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private boolean fixRenderCondition(boolean original) {
		return !original;
	}
}
