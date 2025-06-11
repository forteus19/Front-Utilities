package red.vuis.frontutil.mixin;

import java.util.Map;

import com.boehmod.blockfront.common.gun.GunMagType;
import com.boehmod.blockfront.common.item.GunItem;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GunItem.class)
public interface GunItemAccessor {
	@Accessor("ID_TO_MAG_TYPE")
	@NotNull Map<String, GunMagType> getIdToMagTypeMap();
}
