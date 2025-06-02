package red.vuis.frontutil.mixin;

import java.util.List;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.dom.DominationCapturePoint;
import com.boehmod.blockfront.game.dom.DominationGame;
import com.boehmod.blockfront.unnamed.BF_691;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.command.AddonCommands;
import red.vuis.frontutil.command.InfoFunctions;

@Mixin(DominationGame.class)
public abstract class DominationGameMixin {
	@Shadow
	@Final
	private @NotNull List<DominationCapturePoint> capturePoints;
	@Shadow
	@Final
	private @NotNull List<BF_691> field_3389;
	@Shadow
	@Final
	private AssetCommandBuilder command;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void addCommands(BFAbstractManager<?, ?, ?> par1, CallbackInfo ci) {
		AssetCommandBuilder cpointCommand = command.subCommands.get("cpoint");

		cpointCommand.subCommand("list", AddonCommands.genericList(
			"frontutil.message.command.game.cpoint.list.none",
			"frontutil.message.command.game.cpoint.list.header",
			capturePoints,
			InfoFunctions::capturePoint
		));

		AssetCommandBuilder apointCommand = command.subCommands.get("apoint");

		apointCommand.subCommand("list", AddonCommands.genericList(
			"frontutil.message.command.game.apoint.list.none",
			"frontutil.message.command.game.apoint.list.header",
			field_3389,
			InfoFunctions::pose
		));
	}
}
