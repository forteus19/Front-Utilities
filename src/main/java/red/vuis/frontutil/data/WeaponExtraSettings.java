package red.vuis.frontutil.data;

import java.util.Optional;

import com.boehmod.blockfront.common.item.GunItem;
import com.boehmod.blockfront.registry.BFDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.setup.GunSkinIndex;

public class WeaponExtraSettings {
	public static final StreamCodec<ByteBuf, WeaponExtraSettings> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL,
		e -> e.scope,
		ByteBufCodecs.STRING_UTF8,
		e -> e.magType,
		ByteBufCodecs.STRING_UTF8,
		e -> e.barrelType,
		ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional),
		e -> Optional.ofNullable(e.skin),
		(scope, magType, barrelType, skin) -> new WeaponExtraSettings(scope, magType, barrelType, skin.orElse(null))
	);
	
	public boolean scope;
	public String magType;
	public String barrelType;
	public String skin;
	
	public WeaponExtraSettings() {
		scope = false;
		magType = "default";
		barrelType = "default";
		skin = null;
	}
	
	public WeaponExtraSettings(boolean scope, String magType, String barrelType, String skin) {
		this.scope = scope;
		this.magType = magType;
		this.barrelType = barrelType;
		this.skin = skin;
	}
	
	public void getComponents(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return;
		}
		
		scope = GunItem.getScope(itemStack);
		magType = GunItem.getMagType(itemStack);
		barrelType = GunItem.getBarrelType(itemStack);
		skin = GunSkinIndex.getSkinName(itemStack).orElse(null);
	}
	
	public ItemStack setComponents(@Nullable ItemStack itemStack) {
		if (itemStack != null && itemStack.getItem() instanceof GunItem) {
			if (scope) {
				GunItem.setScope(itemStack, true);
			}
			if (!magType.equals("default")) {
				GunItem.setMagType(itemStack, magType);
			}
			if (!barrelType.equals("default")) {
				GunItem.setBarrelType(itemStack, barrelType);
			}
			GunSkinIndex.getSkinId(itemStack.getItem(), skin)
				.ifPresent(id -> itemStack.set(BFDataComponents.SKIN_ID, id));
		}
		return itemStack;
	}
	
	public @NotNull ItemStack getItemStack(@NotNull GunItem item) {
		return setComponents(new ItemStack(item));
	}
}
