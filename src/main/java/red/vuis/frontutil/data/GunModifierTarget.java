package red.vuis.frontutil.data;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record GunModifierTarget(List<ResourceLocation> targets, ResourceLocation modifier) {
	public static final List<GunModifierTarget> ACTIVE = new ObjectArrayList<>();
	public static final Codec<GunModifierTarget> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			ResourceLocation.CODEC.listOf().fieldOf("targets").forGetter(GunModifierTarget::targets),
			ResourceLocation.CODEC.fieldOf("modifier").forGetter(GunModifierTarget::modifier)
		).apply(instance, GunModifierTarget::new)
	);
	
	@Override
	public @NotNull String toString() {
		return "modifier=" + modifier + ",numTargets=" + targets.size();
	}
}
