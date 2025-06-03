package red.vuis.frontutil.mixin;

import java.util.List;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.assets.AssetCommandValidators;
import com.boehmod.blockfront.assets.impl.MapAsset;
import com.boehmod.blockfront.client.mapeffect.AbstractMapEffect;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.util.BFAdminUtils;
import com.boehmod.blockfront.util.BFStyles;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.command.bf.AddonAssetCommands;
import red.vuis.frontutil.command.bf.InfoFunctions;
import red.vuis.frontutil.command.bf.MapEffectCommands;

@Mixin(MapAsset.class)
public abstract class MapAssetMixin {
	@Shadow
	@Final
	@NotNull
	public List<AbstractMapEffect> mapEffects;
	@Shadow
	private @NotNull String name;
	@Shadow
	@Final
	private @NotNull AssetCommandBuilder command;
	
	@Shadow
	public abstract @NotNull String getName();
	
	@Inject(
		method = "<init>(Ljava/lang/String;Ljava/lang/String;Lcom/boehmod/blockfront/common/match/DivisionData;Lcom/boehmod/blockfront/common/match/DivisionData;)V",
		at = @At("TAIL")
	)
	private void addMapEffectCommands(String par1, String par2, DivisionData par3, DivisionData par4, CallbackInfo ci) {
		AssetCommandBuilder baseCommand = command.subCommands.get("mapEffect");
		
		baseCommand.subCommand("list", AddonAssetCommands.genericList(
			this::getName,
			"frontutil.message.command.mapEffect.list.none",
			"frontutil.message.command.mapEffect.list.header",
			mapEffects,
			InfoFunctions::mapEffect
		));
		
		baseCommand.subCommand("remove", AddonAssetCommands.genericRemove(
			this::getName,
			"frontutil.message.command.mapEffect.remove.success",
			mapEffects
		));
		
		AssetCommandBuilder addCommand = baseCommand.subCommands.get("add");
		
		addCommand.subCommand(
			"loopingSoundPoint",
			new AssetCommandBuilder((context, args) -> {
				Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
				CommandSource source = context.getSource().source;
				
				var mapEffect = MapEffectCommands.parseLoopingSoundPoint(args);
				if (mapEffect == null) {
					BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.loopingSoundPoint.error", nameComponent));
					return;
				}
				mapEffects.add(mapEffect);
				
				BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.loopingSoundPoint.success", nameComponent));
			}).validator(
				AssetCommandValidators.count(new String[]{"count", "maxTick", "x", "y", "z"})
			));
		
		addCommand.subCommand(
			"particleEmitter",
			new AssetCommandBuilder((context, args) -> {
				Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
				CommandSource source = context.getSource().source;
				
				if (!(args.length == 5 || args.length == 7)) {
					BFAdminUtils.sendBfaWarn(source, Component.translatable("frontutil.message.command.error.args.count"));
					BFAdminUtils.sendBfaWarn(source, Component.literal("particle, maxTick, x, y, z"));
					BFAdminUtils.sendBfaWarn(source, Component.literal("particle, maxTick, x, y, z, sound, soundVolume"));
					return;
				}
				
				var mapEffect = MapEffectCommands.parseParticleEmitter(args);
				if (mapEffect == null) {
					BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.particleEmitter.error", nameComponent));
					return;
				}
				mapEffects.add(mapEffect);
				
				BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.particleEmitter.success", nameComponent));
			}));
	}
}
