package red.vuis.frontutil.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.boehmod.bflib.fds.tag.FDSTagCompound;
import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.assets.AssetCommandValidators;
import com.boehmod.blockfront.assets.impl.MapAsset;
import com.boehmod.blockfront.client.render.effect.WeatherEffectType;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.map.MapEnvironment;
import com.boehmod.blockfront.map.effect.AbstractMapEffect;
import com.boehmod.blockfront.map.effect.MapEffectRegistry;
import com.boehmod.blockfront.util.BFStyles;
import com.boehmod.blockfront.util.CommandUtils;
import com.boehmod.blockfront.util.RegistryUtils;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
	private void addEnvironmentCommands(String name, String author, DivisionData alliesDivision, DivisionData axisDivision, CallbackInfo ci) {
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
		
		env.setTime(root.getInteger("mapTime", env.getTime()));
		if (root.getBoolean("hasCustomFog")) {
			env.setCustomFogDensity(
				root.getFloat("fogDensityNear", env.getNearFogDensity()),
				root.getFloat("fogDensityFar", env.getFarFogDensity())
			);
			env.setCustomFogColor(root.getInteger("fogColor", env.getCustomFogColor()));
		}
		if (root.getBoolean("hasCustomSky")) {
			env.setCustomSkyColor(root.getInteger("skyColor", env.getCustomSkyColor()));
		}
		if (root.getBoolean("hasCustomWater")) {
			env.setCustomWaterColor(root.getInteger("waterColor", env.getCustomWaterColor()));
		}
		if (root.getBoolean("hasCustomLightColor")) {
			env.setCustomLightColor(root.getInteger("lightColor", env.getCustomLightColor()));
		}
		env.setExteriorSound(RegistryUtils.retrieveSoundEvent(root.getString("soundOutdoors", RegistryUtils.getSoundEventId(env.getExteriorSound().get()))));
		env.setInteriorSound(RegistryUtils.retrieveSoundEvent(root.getString("soundIndoors", RegistryUtils.getSoundEventId(env.getInteriorSound().get()))));
		env.setDisableClouds(root.getBoolean("disableClouds", env.getDisableClouds()));
		env.setDisableSky(root.getBoolean("disableSky", env.getDisableSky()));
		if (root.hasTag("clientShader")) {
			String shaderStr = root.getString("clientShader");
			assert shaderStr != null;
			env.setShader(ResourceLocation.tryParse(shaderStr));
		}
		
		int weatherEffectsSize = root.getInteger("weatherEffectsSize");
		for (int i = 0; i < weatherEffectsSize; i++) {
			try {
				env.addParticleEffect(WeatherEffectType.values()[root.getInteger("weatherEffect" + i)]);
			} catch (IndexOutOfBoundsException ignored) {
			}
		}
		
		int mapEffectSize = root.getInteger("mapEffectSize");
		for (int i = 0; i < mapEffectSize; i++) {
			Class<?> mapEffectClass = MapEffectRegistry.getEffect(root.getByte("mapEffectType" + i));
			if (mapEffectClass == null) continue;
			
			AbstractMapEffect mapEffect;
			try {
				mapEffect = (AbstractMapEffect) mapEffectClass.getDeclaredConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
			
			FDSTagCompound mapEffectRoot = root.getTagCompound("mapEffectTag" + i);
			if (mapEffectRoot == null) continue;
			mapEffect.readFromFDS(mapEffectRoot);
			
			env.addMapEffect(mapEffect);
		}
		
		environments.put(MapEnvironment.DEFAULT_NAME, env);
		ci.cancel();
	}
	
	@Unique
	private AssetCommandBuilder frontutil$addMapEffectCommands(AssetCommandBuilder baseCommand) {
		baseCommand.subCommand("list", new AssetCommandBuilder((context, fixedArgs) -> {
			List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
			MapEnvironment env = frontutil$getEnvironment(context, args);
			if (env == null) return;
			
			AddonAssetCommands.genericList(
				context,
				this::getName,
				"frontutil.message.command.mapEffect.list.none",
				"frontutil.message.command.mapEffect.list.header",
				env.getMapEffects(),
				InfoFunctions::mapEffect
			);
		}).validator(
			AssetCommandValidatorsEx.count("environment")
		));
		
		baseCommand.subCommand("remove", new AssetCommandBuilder((context, fixedArgs) -> {
			List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
			MapEnvironment env = frontutil$getEnvironment(context, args);
			if (env == null) return;
			
			AddonAssetCommands.genericRemove(
				context, args,
				this::getName,
				"frontutil.message.command.mapEffect.remove.success",
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
				MapEnvironment env = frontutil$getEnvironment(context, args);
				if (env == null) return;
				
				if (!(args.size() == 6 || args.size() == 11)) {
					CommandUtils.sendBfaWarn(source, Component.translatable("frontutil.message.command.error.args.count"));
					CommandUtils.sendBfaWarn(source, Component.literal("environment, x, y, z, endPosX, endPosY, endPosZ"));
					CommandUtils.sendBfaWarn(source, Component.literal("environment, x, y, z, endPosX, endPosY, endPosZ, chance, playSound, spreadX, spreadY, spreadZ"));
					return;
				}
				
				var mapEffect = MapEffectCommands.parseBulletTracerSpawner(args);
				if (mapEffect == null) {
					CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.bulletTracerSpawner.error", nameComponent));
					return;
				}
				env.addMapEffect(mapEffect);
				
				CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.bulletTracerSpawner.success", nameComponent));
			}));
		
		addCommand.subCommand(
			"loopingSoundPoint",
			new AssetCommandBuilder((context, fixedArgs) -> {
				Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
				CommandSource source = context.getSource().source;
				
				List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
				MapEnvironment env = frontutil$getEnvironment(context, args);
				if (env == null) return;
				
				var mapEffect = MapEffectCommands.parseLoopingSoundPoint(args);
				if (mapEffect == null) {
					CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.loopingSoundPoint.error", nameComponent));
					return;
				}
				env.addMapEffect(mapEffect);
				
				CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.loopingSoundPoint.success", nameComponent));
			}).validator(
				AssetCommandValidators.count(new String[]{"environment", "count", "maxTick", "x", "y", "z"})
			));
		
		addCommand.subCommand(
			"particleEmitter",
			new AssetCommandBuilder((context, fixedArgs) -> {
				Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
				CommandSource source = context.getSource().source;
				
				List<String> args = new ArrayList<>(Arrays.asList(fixedArgs));
				MapEnvironment env = frontutil$getEnvironment(context, args);
				if (env == null) return;
				
				if (!(args.size() == 5 || args.size() == 7)) {
					CommandUtils.sendBfaWarn(source, Component.translatable("frontutil.message.command.error.args.count"));
					CommandUtils.sendBfaWarn(source, Component.literal("environment, particle, maxTick, x, y, z"));
					CommandUtils.sendBfaWarn(source, Component.literal("environment, particle, maxTick, x, y, z, sound, soundVolume"));
					return;
				}
				
				var mapEffect = MapEffectCommands.parseParticleEmitter(args);
				if (mapEffect == null) {
					CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.particleEmitter.error", nameComponent));
					return;
				}
				env.addMapEffect(mapEffect);
				
				CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.particleEmitter.success", nameComponent));
			}));
		
		baseCommand.subCommand("add", addCommand);
		
		return baseCommand;
	}
	
	@Unique
	private @Nullable MapEnvironment frontutil$getEnvironment(CommandContext<CommandSourceStack> context, List<String> args) {
		if (args.isEmpty()) {
			CommandUtils.sendBfa(context.getSource().source, Component.translatable(
				"frontutil.message.command.map.env.error.none"
			));
			return null;
		}
		MapEnvironment env = environments.get(args.removeFirst());
		if (env == null) {
			CommandUtils.sendBfaWarn(context.getSource().source, Component.translatable(
				"frontutil.message.command.map.env.error.notFound",
				Component.literal(name).withStyle(BFStyles.LIME)
			));
			return null;
		}
		return env;
	}
}
