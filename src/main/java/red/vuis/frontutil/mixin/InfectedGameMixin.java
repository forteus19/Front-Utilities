package red.vuis.frontutil.mixin;

import com.boehmod.blockfront.game.impl.inf.InfectedGame;
import com.boehmod.blockfront.registry.BFBlocks;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InfectedGame.class)
public abstract class InfectedGameMixin {
	@Definition(id = "getBlock", method = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;")
	@Definition(id = "IRON_DOOR", field = "Lnet/minecraft/block/Blocks;IRON_DOOR:Lnet/minecraft/block/Block;")
	@Expression("?.getBlock() != IRON_DOOR")
	@ModifyExpressionValue(
		method = "method_3657",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private boolean addBlastDoor(boolean original, @Local BlockState target) {
		return original && target.getBlock() != BFBlocks.DOOR_BLAST.get();
	}
	
	@ModifyArg(
		method = "method_3657",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/text/Text;literal(Ljava/lang/String;)Lnet/minecraft/text/MutableText;",
			ordinal = 1
		),
		index = 0
	)
	private String changeIronDoorMessage(String string) {
		return "Failed to create new door! (Invalid door position! No iron or blast door found.)";
	}
}
