package red.vuis.frontutil.mixin;

import com.boehmod.blockfront.client.mapeffect.FallingArtilleryMapEffect;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallingArtilleryMapEffect.class)
public interface FallingArtilleryMapEffectAccessor {
	@Accessor("field_3050")
	Vec2 getMin();
	
	@Accessor("field_3051")
	Vec2 getMax();
}
