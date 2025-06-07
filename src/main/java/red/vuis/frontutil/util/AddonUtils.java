package red.vuis.frontutil.util;

import java.util.Optional;
import java.util.function.Function;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.util.BFUtils;
import com.boehmod.blockfront.util.math.FDSPose;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class AddonUtils {
	private AddonUtils() {
	}
	
	public static <T> Optional<T> parse(Function<String, ? extends T> parser, String arg) {
		try {
			return Optional.ofNullable(parser.apply(arg));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
	public static boolean anyEmpty(Optional<?>... optionals) {
		for (Optional<?> optional : optionals) {
			if (optional.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	public static Vec3 copyVec3(Vec3 other) {
		return new Vec3(other.x, other.y, other.z);
	}
	
	public static String formatVec3(Vec3 vec) {
		return String.format("%.2f, %.2f, %.2f", vec.x, vec.y, vec.z);
	}
	
	public static void setPoseFromEntity(FDSPose pose, Entity entity) {
		pose.position = copyVec3(entity.position());
		pose.rotation = new Vec2(entity.getYHeadRot(), entity.getXRot());
	}
	
	public static void teleportBf(ServerPlayer player, FDSPose pose) {
		var manager = BlockFront.getInstance().getManager();
		if (manager != null) {
			BFUtils.teleportPlayer(manager.getPlayerDataHandler(), player, pose);
		}
	}
}
