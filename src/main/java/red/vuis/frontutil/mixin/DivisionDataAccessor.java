package red.vuis.frontutil.mixin;

import com.boehmod.blockfront.common.match.DivisionData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DivisionData.class)
public interface DivisionDataAccessor {
	@Invoker("copyLoadouts")
	void copyLoadouts(@NotNull DivisionData data);
}
