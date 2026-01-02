package red.vuis.frontutil.mixin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.boehmod.bflib.fds.tag.FDSTagCompound;
import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.match.MatchClass;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameStageManager;
import com.boehmod.blockfront.game.impl.ffa.FreeForAllGame;
import com.boehmod.blockfront.game.impl.ffa.FreeForAllPlayerManager;
import com.boehmod.blockfront.util.CommandUtils;
import com.boehmod.blockfront.util.math.FDSPose;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import red.vuis.frontutil.command.bf.AssetCommandValidatorsEx;
import red.vuis.frontutil.util.AddonUtils;

@Mixin(FreeForAllGame.class)
public abstract class FreeForAllGameMixin extends AbstractGame<FreeForAllGame, FreeForAllPlayerManager, GameStageManager<FreeForAllGame, FreeForAllPlayerManager>> {
	@Shadow
	@Final
	private @NotNull AssetCommandBuilder command;
	
	@Unique
	private boolean frontutil$allowAntiTank = false;
	
	public FreeForAllGameMixin(@NotNull BFAbstractManager<?, ?, ?> manager) {
		super(manager);
	}
	
	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void addCommands(BFAbstractManager<?, ?, ?> manager, CallbackInfo ci) {
		command.subCommand("allowAntiTank", new AssetCommandBuilder((context, args) -> {
			CommandOutput output = context.getSource().output;
			
			var value = AddonUtils.parse(Boolean::parseBoolean, args[0]);
			if (value.isEmpty()) {
				CommandUtils.sendBfa(output, Text.translatable("frontutil.message.command.error.value.boolean"));
			}
			
			frontutil$allowAntiTank = value.orElseThrow();
			CommandUtils.sendBfa(output,
				frontutil$allowAntiTank ?
					Text.translatable("frontutil.message.command.game.allowAntiTank.success.enabled") :
					Text.translatable("frontutil.message.command.game.allowAntiTank.success.disabled")
			);
		}).validator(
			AssetCommandValidatorsEx.count("value")
		));
		
		frontutil$addSpawnCommands(command.subCommands.get("spawn"));
	}
	
	@Unique
	private AssetCommandBuilder frontutil$addSpawnCommands(AssetCommandBuilder baseCommand) {
		baseCommand.subCommand("moveAll", new AssetCommandBuilder((context, args) -> {
			CommandOutput output = context.getSource().output;
			
			var offsetXParse = AddonUtils.parse(Double::parseDouble, args[0]);
			var offsetYParse = AddonUtils.parse(Double::parseDouble, args[1]);
			var offsetZParse = AddonUtils.parse(Double::parseDouble, args[2]);
			
			if (AddonUtils.anyEmpty(offsetXParse, offsetYParse, offsetZParse)) {
				CommandUtils.sendBfa(output, Text.translatable("frontutil.message.command.error.offset.number"));
				return;
			}
			
			Vec3d offset = new Vec3d(offsetXParse.orElseThrow(), offsetYParse.orElseThrow(), offsetZParse.orElseThrow());
			
			List<FDSPose> oldPoses = new ArrayList<>(playerManager.method_3566());
			playerManager.method_3570();
			
			for (FDSPose oldPose : oldPoses) {
				playerManager.method_3571(new FDSPose(oldPose.position.add(offset), oldPose.rotation.x, oldPose.rotation.y));
			}
			
			CommandUtils.sendBfa(output, Text.translatable("frontutil.message.command.game.spawn.moveAll.success", offset.toString()));
		}).validator(
			AssetCommandValidatorsEx.count("offsetX", "offsetY", "offsetZ")
		));
		
		return baseCommand;
	}
	
	@Inject(
		method = "getBannedClasses",
		at = @At("HEAD"),
		cancellable = true
	)
	private void overrideBannedClasses(CallbackInfoReturnable<Set<MatchClass>> cir) {
		if (frontutil$allowAntiTank) {
			cir.setReturnValue(EnumSet.of(MatchClass.CLASS_COMMANDER));
		}
	}
	
	@Inject(
		method = "writeSpecificFDS",
		at = @At("TAIL")
	)
	private void writeCustomFDS(FDSTagCompound root, CallbackInfo ci) {
		root.setBoolean("allowAntiTank", frontutil$allowAntiTank);
	}
	
	@Inject(
		method = "readSpecificFDS",
		at = @At("TAIL")
	)
	private void readCustomFDS(FDSTagCompound root, CallbackInfo ci) {
		frontutil$allowAntiTank = root.getBoolean("allowAntiTank", false);
	}
	
	@Override
	public void method_2624(@NotNull ByteBuf buf, boolean hasMapAsset) throws IOException {
		super.method_2624(buf, hasMapAsset);
		buf.writeBoolean(frontutil$allowAntiTank);
	}
	
	@Override
	public void method_2664(@NotNull ByteBuf buf) throws IOException {
		super.method_2664(buf);
		frontutil$allowAntiTank = buf.readBoolean();
	}
}
