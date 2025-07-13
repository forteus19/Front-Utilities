package red.vuis.frontutil.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

import com.boehmod.blockfront.common.item.GunItem;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.AddonConstants;

public record GunModifierTarget(
	List<Identifier> targets,
	Identifier modifier
) {
	public static final List<GunModifierTarget> ACTIVE = new ObjectArrayList<>();
	public static final Codec<GunModifierTarget> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Identifier.CODEC.listOf().fieldOf("targets").forGetter(GunModifierTarget::targets),
			Identifier.CODEC.fieldOf("modifier").forGetter(GunModifierTarget::modifier)
		).apply(instance, GunModifierTarget::new)
	);
	private static final String GUN_MODIFIER_TARGETS_FILENAME = "gun_modifier_targets.json";
	
	@Override
	public @NotNull String toString() {
		return "modifier=" + modifier + ",numTargets=" + targets.size();
	}
	
	public static void parseAndApply(ResourceManager resourceManager) {
		List<Resource> targetResources = resourceManager.getAllResources(AddonConstants.id(GUN_MODIFIER_TARGETS_FILENAME));
		
		for (Resource targetResource : targetResources) {
			try (BufferedReader targetReader = targetResource.getReader()) {
				List<GunModifierTarget> targets = parseGunModifierTargets(targetResource, targetReader);
				if (targets == null || !targets.stream().allMatch(GunModifierTarget::checkGunModifierTarget)) {
					continue;
				}
				GunModifierTarget.ACTIVE.addAll(targets);
				
				for (GunModifierTarget target : targets) {
					Optional<Resource> modifierResource = resourceManager.getResource(target.modifier());
					if (modifierResource.isEmpty()) {
						AddonConstants.LOGGER.error("Modifier '{}' does not exist!", target.modifier());
						continue;
					}
					try (BufferedReader modifierReader = modifierResource.get().getReader()) {
						GunModifier modifier = parseGunModifier(target.modifier(), modifierReader);
						if (modifier == null) {
							continue;
						}
						
						for (Identifier itemRes : target.targets()) {
							GunItem item = (GunItem) Registries.ITEM.get(itemRes);
							GunModifier.ACTIVE.put(Registries.ITEM.getEntry(item), modifier);
							modifier.apply(item);
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static @Nullable List<GunModifierTarget> parseGunModifierTargets(Resource targetResource, Reader targetReader) {
		DataResult<List<GunModifierTarget>> targetResult = GunModifierTarget.CODEC.listOf().parse(JsonOps.INSTANCE, JsonParser.parseReader(targetReader));
		if (targetResult.isError()) {
			AddonConstants.LOGGER.error("Failed to parse gun modifier targets for pack id '{}'!", targetResource.getPackId());
			return null;
		}
		return targetResult.getOrThrow();
	}
	
	private static boolean checkGunModifierTarget(GunModifierTarget target) {
		for (Identifier itemRes : target.targets()) {
			if (!Registries.ITEM.containsId(itemRes) || !(Registries.ITEM.get(itemRes) instanceof GunItem)) {
				AddonConstants.LOGGER.error("Modifier target '{}' is not a modifiable item!", itemRes);
				return false;
			}
		}
		return true;
	}
	
	private static @Nullable GunModifier parseGunModifier(Identifier id, Reader reader) {
		DataResult<GunModifier> result = GunModifier.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader));
		if (result.isError()) {
			AddonConstants.LOGGER.error("Failed to parse gun modifier '{}'!", id);
		}
		return result.getOrThrow();
	}
}
