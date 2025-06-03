package red.vuis.frontutil.mixin;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.conq.ConquestCapturePoint;
import com.boehmod.blockfront.game.conq.ConquestGame;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.command.bf.GameCommands;

@Mixin(ConquestGame.class)
public abstract class ConquestGameMixin {
	@Shadow
	@Final
	@NotNull
	public ObjectList<ConquestCapturePoint> capturePoints;
	@Shadow
	@Final
	private AssetCommandBuilder command;
	
	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void addCommands(BFAbstractManager<?, ?, ?> par1, CallbackInfo ci) {
		GameCommands.capturePointCommands(
			command.subCommands.get("cpoint"),
			capturePoints,
			(player, name) -> new ConquestCapturePoint(((ConquestGame) (Object) this).getGameManager(), player, name)
		);
	}
}
