package red.vuis.frontutil.mixin;

import com.boehmod.blockfront.map.effect.FallingArtilleryMapEffect;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallingArtilleryMapEffect.class)
public interface FallingArtilleryMapEffectAccessor {
	@Accessor("field_3050")
	Vec2 getStart();
	
	@Accessor("field_3050")
	void setStart(Vec2 start);
	
	@Accessor("field_3051")
	Vec2 getEnd();
	
	@Accessor("field_3051")
	void setEnd(Vec2 start);
}
