package red.vuis.frontutil.mixin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.boehmod.bflib.fds.tag.FDSTagCompound;
import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.assets.impl.MapAsset;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.map.MapEnvironment;
import com.boehmod.blockfront.map.effect.AbstractMapEffect;
import com.boehmod.blockfront.util.BFStyles;
import com.boehmod.blockfront.util.CommandUtils;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
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
import red.vuis.frontutil.command.bf.MapCommands;
import red.vuis.frontutil.data.bf.AddonMapAssetData;
import red.vuis.frontutil.util.AddonUtils;
import red.vuis.frontutil.util.property.PropertyHandleResult;

@Mixin(MapAsset.class)
public abstract class MapAssetMixin {
	@Shadow
	public @NotNull Map<String, MapEnvironment> environments;
	@Shadow
	private @NotNull String name;
	@Shadow
	private @NotNull DivisionData alliesDivision;
	@Shadow
	private @NotNull DivisionData axisDivision;
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
		command.subCommand("color", frontutil$addColorCommands(new AssetCommandBuilder()));
		command.subCommand("env", frontutil$addEnvironmentCommands(new AssetCommandBuilder()));
		command.subCommand("mapEffect", frontutil$addMapEffectCommands(new AssetCommandBuilder()));
		frontutil$addTeamsCommands(command.subCommands.get("teams"));
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
	private AssetCommandBuilder frontutil$addColorCommands(AssetCommandBuilder baseCommand) {
		BiFunction<BiConsumer<MapEnvironment, Integer>, String, AssetCommandBuilder> commonSetter = (setter, message) ->
			new AssetCommandBuilder((context, fixedArgs) -> {
				Text nameText = Text.literal(name).fillStyle(BFStyles.LIME);
				CommandOutput output = context.getSource().output;
				
				List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
				
				var envResult = frontutil$getEnvironment(context, args);
				if (envResult == null) return;
				MapEnvironment env = environments.get(envResult.first());
				
				String colorArg = args.getFirst();
				Text colorArgText = Text.literal(colorArg).fillStyle(BFStyles.LIME);
				
				try {
					int color = Color.decode(colorArg).getRGB();
					setter.accept(env, color);
				} catch (NumberFormatException e) {
					CommandUtils.sendBfa(output, Text.translatable(
						"frontutil.message.command.map.color.set.error.format",
						colorArgText
					));
					return;
				}
				
				CommandUtils.sendBfa(output, Text.translatable(message, colorArgText, nameText));
			});
		
		BiFunction<Consumer<MapEnvironment>, String, AssetCommandBuilder> commonClearer = (clearer, message) ->
			new AssetCommandBuilder((context, fixedArgs) -> {
				Text nameText = Text.literal(name).fillStyle(BFStyles.LIME);
				CommandOutput output = context.getSource().output;
				
				List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
				
				var envResult = frontutil$getEnvironment(context, args);
				if (envResult == null) return;
				MapEnvironment env = environments.get(envResult.first());
				
				clearer.accept(env);
				
				CommandUtils.sendBfa(output, Text.translatable(message, nameText));
			});
		
		AssetCommandBuilder setCommand = new AssetCommandBuilder();
		
		setCommand.subCommand("fog", commonSetter.apply(MapEnvironment::setCustomFogColor, "frontutil.message.command.map.color.set.fog")
			.validator(AssetCommandValidatorsEx.count("environment", "color")));
		setCommand.subCommand("light", commonSetter.apply(MapEnvironment::setCustomLightColor, "frontutil.message.command.map.color.set.light")
			.validator(AssetCommandValidatorsEx.count("environment", "color")));
		setCommand.subCommand("sky", commonSetter.apply(MapEnvironment::setCustomSkyColor, "frontutil.message.command.map.color.set.sky")
			.validator(AssetCommandValidatorsEx.count("environment", "color")));
		setCommand.subCommand("water", commonSetter.apply(MapEnvironment::setCustomWaterColor, "frontutil.message.command.map.color.set.water")
			.validator(AssetCommandValidatorsEx.count("environment", "color")));
		
		baseCommand.subCommand("set", setCommand);
		
		AssetCommandBuilder clearCommand = new AssetCommandBuilder();
		
		clearCommand.subCommand("fog", commonClearer.apply(MapEnvironment::clearCustomFogColor, "frontutil.message.command.map.color.clear.fog")
			.validator(AssetCommandValidatorsEx.count("environment")));
		clearCommand.subCommand("light", commonClearer.apply(MapEnvironment::clearCustomLightColor, "frontutil.message.command.map.color.clear.light")
			.validator(AssetCommandValidatorsEx.count("environment")));
		clearCommand.subCommand("sky", commonClearer.apply(MapEnvironment::clearCustomSkyColor, "frontutil.message.command.map.color.clear.sky")
			.validator(AssetCommandValidatorsEx.count("environment")));
		clearCommand.subCommand("water", commonClearer.apply(MapEnvironment::clearCustomWaterColor, "frontutil.message.command.map.color.clear.water")
			.validator(AssetCommandValidatorsEx.count("environment")));
		
		baseCommand.subCommand("clear", clearCommand);
		
		return baseCommand;
	}
	
	@Unique
	private AssetCommandBuilder frontutil$addEnvironmentCommands(AssetCommandBuilder baseCommand) {
		baseCommand.subCommand("list", new AssetCommandBuilder((context, args) -> {
			CommandUtils.sendBfa(context.getSource().output, Text.translatable(
				"frontutil.message.command.map.env.list.success",
				Text.literal(name).fillStyle(BFStyles.LIME),
				AddonUtils.listify(environments.keySet())
			));
		}));
		
		baseCommand.subCommand("new", new AssetCommandBuilder((context, args) -> {
			Text nameText = Text.literal(name).fillStyle(BFStyles.LIME);
			CommandOutput output = context.getSource().output;
			String envName = args[0];
			Text envNameText = Text.literal(envName).fillStyle(BFStyles.LIME);
			
			if (environments.containsKey(envName)) {
				CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.map.env.new.error.exists",
					envNameText, nameText
				));
				return;
			}
			
			environments.put(envName, new MapEnvironment(envName));
			
			CommandUtils.sendBfa(output, Text.translatable(
				"frontutil.message.command.map.env.new.success",
				envNameText, nameText
			));
		}).validator(
			AssetCommandValidatorsEx.count("name")
		));
		
		baseCommand.subCommand("remove", new AssetCommandBuilder((context, args) -> {
			Text nameText = Text.literal(name).fillStyle(BFStyles.LIME);
			CommandOutput output = context.getSource().output;
			
			var envResult = frontutil$getEnvironment(context, args);
			if (envResult == null) return;
			
			String envName = envResult.first();
			Text envNameText = envResult.second();
			
			if (MapEnvironment.DEFAULT_NAME.equals(envName)) {
				CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.map.env.remove.error.default"
				));
				return;
			}
			
			environments.remove(envName);
			
			CommandUtils.sendBfa(output, Text.translatable(
				"frontutil.message.command.map.env.remove.success",
				envNameText, nameText
			));
		}).validator(
			AssetCommandValidatorsEx.count("environment")
		));
		
		return baseCommand;
	}
	
	@Unique
	private AssetCommandBuilder frontutil$addMapEffectCommands(AssetCommandBuilder baseCommand) {
		baseCommand.subCommand("edit", new AssetCommandBuilder((context, fixedArgs) -> {
			CommandOutput output = context.getSource().output;
			
			List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
			
			var envResult = frontutil$getEnvironment(context, args);
			if (envResult == null) return;
			MapEnvironment env = environments.get(envResult.first());
			
			var indexParse = AddonAssetCommands.parseIndex(output, args.getFirst(), env.getMapEffects(), false);
			if (indexParse == null) {
				return;
			}
			int index = indexParse.leftInt();
			Text indexText = indexParse.right();
			AbstractMapEffect mapEffect = env.getMapEffects().get(index);
			
			String property = args.get(1);
			String value = args.get(2);
			
			PropertyHandleResult result = MapCommands.MAP_EFFECT_PROPERTIES.handle(mapEffect, property, value);
			switch (result) {
				case SUCCESS -> CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.map.mapEffect.edit.success",
					Text.literal(property).fillStyle(BFStyles.LIME),
					Text.literal(value).fillStyle(BFStyles.LIME),
					indexText
				));
				case ERROR_PROPERTY -> CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.map.mapEffect.edit.error.property",
					Text.literal(property).fillStyle(BFStyles.LIME),
					indexText
				));
				case ERROR_PARSE -> CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.map.mapEffect.edit.error.parse",
					Text.literal(value).fillStyle(BFStyles.LIME),
					Text.literal(property).fillStyle(BFStyles.LIME),
					indexText
				));
				case ERROR_TYPE -> CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.map.mapEffect.edit.error.type",
					indexText
				));
			}
		}).validator(
			AssetCommandValidatorsEx.count("environment", "index", "property", "value")
		));
		
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
		
		baseCommand.subCommand("properties", new AssetCommandBuilder((context, fixedArgs) -> {
			CommandOutput output = context.getSource().output;
			
			List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
			
			var envResult = frontutil$getEnvironment(context, args);
			if (envResult == null) return;
			MapEnvironment env = environments.get(envResult.first());
			
			var indexParse = AddonAssetCommands.parseIndex(output, args.getFirst(), env.getMapEffects(), false);
			if (indexParse == null) {
				return;
			}
			int index = indexParse.leftInt();
			Text indexText = indexParse.right();
			AbstractMapEffect mapEffect = env.getMapEffects().get(index);
			
			List<String> properties = MapCommands.MAP_EFFECT_PROPERTIES.getProperties(mapEffect);
			
			if (properties.isEmpty()) {
				CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.map.mapEffect.properties.none",
					indexText
				));
			} else {
				CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.map.mapEffect.properties.header",
					indexText
				));
				CommandUtils.sendBfa(output, Text.literal(AddonUtils.listify(properties)));
			}
		}).validator(
			AssetCommandValidatorsEx.count("environment", "index")
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
		
		baseCommand.subCommand("types", new AssetCommandBuilder((context, fixedArgs) -> {
			List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
			
			var envResult = frontutil$getEnvironment(context, args);
			if (envResult == null) return;
			MapEnvironment env = environments.get(envResult.first());
			
			AddonAssetCommands.genericList(
				context,
				this::getName,
				"frontutil.message.command.map.mapEffect.list.none",
				"frontutil.message.command.map.mapEffect.types.header",
				env.getMapEffects(),
				InfoFunctions::mapEffectType
			);
		}).validator(
			AssetCommandValidatorsEx.count("environment")
		));
		
		AssetCommandBuilder addCommand = new AssetCommandBuilder();
		
		addCommand.subCommand(
			"bulletTracerSpawner",
			new AssetCommandBuilder((context, fixedArgs) -> {
				Text nameText = Text.literal(name).fillStyle(BFStyles.LIME);
				CommandOutput source = context.getSource().output;
				
				List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
				
				var envResult = frontutil$getEnvironment(context, args);
				if (envResult == null) return;
				MapEnvironment env = environments.get(envResult.first());
				
				if (!(args.size() == 6 || args.size() == 11)) {
					CommandUtils.sendBfaWarn(source, Text.translatable("frontutil.message.command.error.args.count"));
					CommandUtils.sendBfaWarn(source, Text.literal("environment, x, y, z, endPosX, endPosY, endPosZ"));
					CommandUtils.sendBfaWarn(source, Text.literal("environment, x, y, z, endPosX, endPosY, endPosZ, chance, playSound, spreadX, spreadY, spreadZ"));
					return;
				}
				
				var mapEffect = MapCommands.parseBulletTracerSpawner(args);
				if (mapEffect == null) {
					CommandUtils.sendBfa(source, Text.translatable("frontutil.message.command.map.mapEffect.add.bulletTracerSpawner.error", nameText));
					return;
				}
				env.addMapEffect(mapEffect);
				
				CommandUtils.sendBfa(source, Text.translatable("frontutil.message.command.map.mapEffect.add.bulletTracerSpawner.success", nameText));
			}));
		
		addCommand.subCommand(
			"loopingSoundPoint",
			new AssetCommandBuilder((context, fixedArgs) -> {
				Text nameText = Text.literal(name).fillStyle(BFStyles.LIME);
				CommandOutput source = context.getSource().output;
				
				List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
				
				var envResult = frontutil$getEnvironment(context, args);
				if (envResult == null) return;
				MapEnvironment env = environments.get(envResult.first());
				
				var mapEffect = MapCommands.parseLoopingSoundPoint(args);
				if (mapEffect == null) {
					CommandUtils.sendBfa(source, Text.translatable("frontutil.message.command.map.mapEffect.add.loopingSoundPoint.error", nameText));
					return;
				}
				env.addMapEffect(mapEffect);
				
				CommandUtils.sendBfa(source, Text.translatable("frontutil.message.command.map.mapEffect.add.loopingSoundPoint.success", nameText));
			}).validator(
				AssetCommandValidatorsEx.count("environment", "sound", "maxTick", "x", "y", "z")
			));
		
		addCommand.subCommand(
			"particleEmitter",
			new AssetCommandBuilder((context, fixedArgs) -> {
				Text nameText = Text.literal(name).fillStyle(BFStyles.LIME);
				CommandOutput source = context.getSource().output;
				
				List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
				
				var envName = frontutil$getEnvironment(context, args);
				if (envName == null) return;
				MapEnvironment env = environments.get(envName.first());
				
				if (!(args.size() == 5 || args.size() == 7)) {
					CommandUtils.sendBfaWarn(source, Text.translatable("frontutil.message.command.error.args.count"));
					CommandUtils.sendBfaWarn(source, Text.literal("environment, particle, maxTick, x, y, z"));
					CommandUtils.sendBfaWarn(source, Text.literal("environment, particle, maxTick, x, y, z, sound, soundVolume"));
					return;
				}
				
				var mapEffect = MapCommands.parseParticleEmitter(args);
				if (mapEffect == null) {
					CommandUtils.sendBfa(source, Text.translatable("frontutil.message.command.map.mapEffect.add.particleEmitter.error", nameText));
					return;
				}
				env.addMapEffect(mapEffect);
				
				CommandUtils.sendBfa(source, Text.translatable("frontutil.message.command.map.mapEffect.add.particleEmitter.success", nameText));
			}));
		
		baseCommand.subCommand("add", addCommand);
		
		return baseCommand;
	}
	
	@Unique
	private AssetCommandBuilder frontutil$addTeamsCommands(AssetCommandBuilder baseCommand) {
		baseCommand.subCommand("list", new AssetCommandBuilder((context, args) -> {
			Text nameText = Text.literal(name).fillStyle(BFStyles.LIME);
			CommandOutput source = context.getSource().output;
			
			CommandUtils.sendBfa(source, Text.translatable(
				"frontutil.message.command.map.teams.list.header",
				nameText
			));
			
			CommandUtils.sendBfa(source, Text.literal(String.format("Allies: %s (%s)", alliesDivision.getCountry().getName(), alliesDivision.getSkin())));
			CommandUtils.sendBfa(source, Text.literal(String.format("Axis: %s (%s)", axisDivision.getCountry().getName(), axisDivision.getSkin())));
		}));
		
		return baseCommand;
	}
	
	@Unique
	private @Nullable Pair<String, Text> frontutil$getEnvironment(CommandContext<ServerCommandSource> context, List<String> args, boolean pop) {
		if (args.isEmpty()) {
			CommandUtils.sendBfa(context.getSource().output, Text.translatable(
				"frontutil.message.command.map.env.error.none"
			));
			return null;
		}
		
		String envName = pop ? args.removeFirst() : args.getFirst();
		Text envNameText = Text.literal(envName).fillStyle(BFStyles.LIME);
		
		if (!environments.containsKey(envName)) {
			CommandUtils.sendBfaWarn(context.getSource().output, Text.translatable(
				"frontutil.message.command.map.env.error.notFound",
				envNameText,
				Text.literal(name).fillStyle(BFStyles.LIME)
			));
			return null;
		}
		
		return new ObjectObjectImmutablePair<>(envName, envNameText);
	}
	
	@Unique
	private @Nullable Pair<String, Text> frontutil$getEnvironment(CommandContext<ServerCommandSource> context, List<String> args) {
		return frontutil$getEnvironment(context, args, true);
	}
	
	@Unique
	private @Nullable Pair<String, Text> frontutil$getEnvironment(CommandContext<ServerCommandSource> context, String[] args) {
		return frontutil$getEnvironment(context, List.of(args), false);
	}
}
