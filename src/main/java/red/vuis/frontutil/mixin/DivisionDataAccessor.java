package red.vuis.frontutil.mixin;

import java.util.List;
import java.util.Map;

import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DivisionData.class)
public interface DivisionDataAccessor {
	@Accessor("loadouts")
	Map<MatchClass, List<Loadout>> getRawLoadouts();
}
