package red.vuis.frontutil.mixin;

import java.util.List;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AmmoPoint;
import com.boehmod.blockfront.game.GameStageManager;
import com.boehmod.blockfront.game.dom.DominationCapturePoint;
import com.boehmod.blockfront.game.dom.DominationGame;
import com.boehmod.blockfront.game.dom.DominationPlayerManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.command.bf.AddonAssetCommands;
import red.vuis.frontutil.command.bf.GameCommands;
import red.vuis.frontutil.command.bf.InfoFunctions;

@Mixin(DominationGame.class)
public abstract class DominationGameMixin extends AbstractGame<DominationGame, DominationPlayerManager, GameStageManager<DominationGame, DominationPlayerManager>> {
	@Shadow
	@Final
	private @NotNull List<DominationCapturePoint> capturePoints;
	@Shadow
	@Final
	private @NotNull List<AmmoPoint> ammoPoints;
	@Shadow
	@Final
	private AssetCommandBuilder command;
	
	public DominationGameMixin(@NotNull BFAbstractManager<?, ?, ?> manager) {
		super(manager);
	}
	
	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void addCommands(BFAbstractManager<?, ?, ?> manager, CallbackInfo ci) {
		GameCommands.capturePointCommands(
			command.subCommands.get("cpoint"),
			capturePoints,
			(player, name) -> new DominationCapturePoint(playerManager, player, name)
		);
		
		AssetCommandBuilder apointCommand = command.subCommands.get("apoint");
		
		apointCommand.subCommand("list", AddonAssetCommands.genericList(
			"frontutil.message.command.game.apoint.list.none",
			"frontutil.message.command.game.apoint.list.header",
			ammoPoints,
			InfoFunctions::pose
		));
		
		apointCommand.subCommand("remove", AddonAssetCommands.genericRemove(
			"frontutil.message.command.game.apoint.remove.success",
			ammoPoints
		));
		
		apointCommand.subCommand("tp", AddonAssetCommands.genericTeleport(
			(ammoPoint, indexComponent) -> Component.translatable("frontutil.message.command.game.apoint.tp.success", indexComponent),
			ammoPoints
		));
	}
}
