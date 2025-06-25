package red.vuis.frontutil.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.boehmod.bflib.fds.tag.FDSTagCompound;
import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.assets.AssetCommandValidators;
import com.boehmod.blockfront.assets.impl.MapAsset;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.map.MapEnvironment;
import com.boehmod.blockfront.util.BFStyles;
import com.boehmod.blockfront.util.CommandUtils;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.command.bf.AddonAssetCommands;
import red.vuis.frontutil.command.bf.AssetCommandValidatorsEx;
import red.vuis.frontutil.command.bf.InfoFunctions;
import red.vuis.frontutil.command.bf.MapEffectCommands;
import red.vuis.frontutil.data.bf.AddonMapAssetData;
import red.vuis.frontutil.util.AddonUtils;

@Mixin(MapAsset.class)
public abstract class MapAssetMixin {
	@Shadow
	public @NotNull Map<String, MapEnvironment> environments;
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
	private void addCommands(String name, String author, DivisionData alliesDivision, DivisionData axisDivision, CallbackInfo ci) {
		command.subCommand("env", frontutil$addEnvironmentCommands(new AssetCommandBuilder()));
		command.subCommand("mapEffect", frontutil$addMapEffectCommands(new AssetCommandBuilder()));
	}
	
	@Inject(
		method = "readFDS",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;isEmpty()Z",
			ordinal = 0
		),
		cancellable = true
	)
	private void migrateOldData(FDSTagCompound root, CallbackInfo ci) {
		if (!environments.isEmpty()) {
			return;
		}
		
		MapEnvironment env = new MapEnvironment(MapEnvironment.DEFAULT_NAME);
		AddonMapAssetData.readOldFDS(root, env);
		environments.put(env.getName(), env);
		
		ci.cancel();
	}
	
	@Unique
	private AssetCommandBuilder frontutil$addEnvironmentCommands(AssetCommandBuilder baseCommand) {
		baseCommand.subCommand("list", new AssetCommandBuilder((context, args) -> {
			CommandUtils.sendBfa(context.getSource().source, Component.translatable(
				"frontutil.message.command.map.env.list.success",
				Component.literal(name).withStyle(BFStyles.LIME),
				AddonUtils.listify(environments.keySet())
			));
		}));
		
		baseCommand.subCommand("new", new AssetCommandBuilder((context, args) -> {
			Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
			CommandSource source = context.getSource().source;
			String envName = args[0];
			Component envNameComponent = Component.literal(envName).withStyle(BFStyles.LIME);
			
			if (environments.containsKey(envName)) {
				CommandUtils.sendBfa(source, Component.translatable(
					"frontutil.message.command.map.env.new.error.exists",
					envNameComponent, nameComponent
				));
				return;
			}
			
			environments.put(envName, new MapEnvironment(envName));
			
			CommandUtils.sendBfa(source, Component.translatable(
				"frontutil.message.command.map.env.new.success",
				envNameComponent, nameComponent
			));
		}).validator(
			AssetCommandValidatorsEx.count("name")
		));
		
		baseCommand.subCommand("remove", new AssetCommandBuilder((context, args) -> {
			Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
			CommandSource source = context.getSource().source;
			
			var envResult = frontutil$getEnvironment(context, args);
			if (envResult == null) return;
			
			String envName = envResult.first();
			Component envNameComponent = envResult.second();
			
			if (MapEnvironment.DEFAULT_NAME.equals(envName)) {
				CommandUtils.sendBfa(source, Component.translatable(
					"frontutil.message.command.map.env.remove.error.default"
				));
				return;
			}
			
			environments.remove(envName);
			
			CommandUtils.sendBfa(source, Component.translatable(
				"frontutil.message.command.map.env.remove.success",
				envNameComponent, nameComponent
			));
		}).validator(
			AssetCommandValidatorsEx.count("environment")
		));
		
		return baseCommand;
	}
	
	@Unique
	private AssetCommandBuilder frontutil$addMapEffectCommands(AssetCommandBuilder baseCommand) {
		baseCommand.subCommand("list", new AssetCommandBuilder((context, fixedArgs) -> {
			List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
			
			var envResult = frontutil$getEnvironment(context, args);
			if (envResult == null) return;
			MapEnvironment env = environments.get(envResult.first());
			
			AddonAssetCommands.genericList(
				context,
				this::getName,
				"frontutil.message.command.map.mapEffect.list.none",
				"frontutil.message.command.map.mapEffect.list.header",
				env.getMapEffects(),
				InfoFunctions::mapEffect
			);
		}).validator(
			AssetCommandValidatorsEx.count("environment")
		));
		
		baseCommand.subCommand("remove", new AssetCommandBuilder((context, fixedArgs) -> {
			List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
			
			var envResult = frontutil$getEnvironment(context, args);
			if (envResult == null) return;
			MapEnvironment env = environments.get(envResult.first());
			
			AddonAssetCommands.genericRemove(
				context, args,
				this::getName,
				"frontutil.message.command.map.mapEffect.remove.success",
				env.getMapEffects()
			);
		}).validator(
			AssetCommandValidatorsEx.count("environment", "index")
		));
		
		AssetCommandBuilder addCommand = new AssetCommandBuilder();
		
		addCommand.subCommand(
			"bulletTracerSpawner",
			new AssetCommandBuilder((context, fixedArgs) -> {
				Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
				CommandSource source = context.getSource().source;
				
				List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
				
				var envResult = frontutil$getEnvironment(context, args);
				if (envResult == null) return;
				MapEnvironment env = environments.get(envResult.first());
				
				if (!(args.size() == 6 || args.size() == 11)) {
					CommandUtils.sendBfaWarn(source, Component.translatable("frontutil.message.command.error.args.count"));
					CommandUtils.sendBfaWarn(source, Component.literal("environment, x, y, z, endPosX, endPosY, endPosZ"));
					CommandUtils.sendBfaWarn(source, Component.literal("environment, x, y, z, endPosX, endPosY, endPosZ, chance, playSound, spreadX, spreadY, spreadZ"));
					return;
				}
				
				var mapEffect = MapEffectCommands.parseBulletTracerSpawner(args);
				if (mapEffect == null) {
					CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.map.mapEffect.add.bulletTracerSpawner.error", nameComponent));
					return;
				}
				env.addMapEffect(mapEffect);
				
				CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.map.mapEffect.add.bulletTracerSpawner.success", nameComponent));
			}));
		
		addCommand.subCommand(
			"loopingSoundPoint",
			new AssetCommandBuilder((context, fixedArgs) -> {
				Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
				CommandSource source = context.getSource().source;
				
				List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
				
				var envResult = frontutil$getEnvironment(context, args);
				if (envResult == null) return;
				MapEnvironment env = environments.get(envResult.first());
				
				var mapEffect = MapEffectCommands.parseLoopingSoundPoint(args);
				if (mapEffect == null) {
					CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.map.mapEffect.add.loopingSoundPoint.error", nameComponent));
					return;
				}
				env.addMapEffect(mapEffect);
				
				CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.map.mapEffect.add.loopingSoundPoint.success", nameComponent));
			}).validator(
				AssetCommandValidators.count(new String[]{"environment", "count", "maxTick", "x", "y", "z"})
			));
		
		addCommand.subCommand(
			"particleEmitter",
			new AssetCommandBuilder((context, fixedArgs) -> {
				Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
				CommandSource source = context.getSource().source;
				
				List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
				
				var envName = frontutil$getEnvironment(context, args);
				if (envName == null) return;
				MapEnvironment env = environments.get(envName.first());
				
				if (!(args.size() == 5 || args.size() == 7)) {
					CommandUtils.sendBfaWarn(source, Component.translatable("frontutil.message.command.error.args.count"));
					CommandUtils.sendBfaWarn(source, Component.literal("environment, particle, maxTick, x, y, z"));
					CommandUtils.sendBfaWarn(source, Component.literal("environment, particle, maxTick, x, y, z, sound, soundVolume"));
					return;
				}
				
				var mapEffect = MapEffectCommands.parseParticleEmitter(args);
				if (mapEffect == null) {
					CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.map.mapEffect.add.particleEmitter.error", nameComponent));
					return;
				}
				env.addMapEffect(mapEffect);
				
				CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.map.mapEffect.add.particleEmitter.success", nameComponent));
			}));
		
		baseCommand.subCommand("add", addCommand);
		
		return baseCommand;
	}
	
	@Unique
	private @Nullable Pair<String, Component> frontutil$getEnvironment(CommandContext<CommandSourceStack> context, List<String> args, boolean pop) {
		if (args.isEmpty()) {
			CommandUtils.sendBfa(context.getSource().source, Component.translatable(
				"frontutil.message.command.map.env.error.none"
			));
			return null;
		}
		
		String envName = pop ? args.removeFirst() : args.getFirst();
		Component envNameComponent = Component.literal(envName).withStyle(BFStyles.LIME);
		
		if (!environments.containsKey(envName)) {
			CommandUtils.sendBfaWarn(context.getSource().source, Component.translatable(
				"frontutil.message.command.map.env.error.notFound",
				envNameComponent,
				Component.literal(name).withStyle(BFStyles.LIME)
			));
			return null;
		}
		
		return new ObjectObjectImmutablePair<>(envName, envNameComponent);
	}
	
	@Unique
	private @Nullable Pair<String, Component> frontutil$getEnvironment(CommandContext<CommandSourceStack> context, List<String> args) {
		return frontutil$getEnvironment(context, args, true);
	}
	
	@Unique
	private @Nullable Pair<String, Component> frontutil$getEnvironment(CommandContext<CommandSourceStack> context, String[] args) {
		return frontutil$getEnvironment(context, List.of(args), false);
	}
}
