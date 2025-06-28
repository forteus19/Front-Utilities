package red.vuis.frontutil.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.util.BFUtils;
import com.boehmod.blockfront.util.math.FDSPose;
import net.minecraft.server.MinecraftServer;
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
	
	@SafeVarargs
	public static <T> List<T> concat(Iterable<T> parent, T... other) {
		return Stream.concat(StreamSupport.stream(parent.spliterator(), false), Arrays.stream(other)).toList();
	}
	
	public static Vec2 vec2WithX(Vec2 original, float x) {
		return new Vec2(x, original.y);
	}
	
	public static Vec2 vec2WithY(Vec2 original, float y) {
		return new Vec2(original.x, y);
	}
	
	public static Vec3 vec3WithX(Vec3 original, double x) {
		return new Vec3(x, original.y, original.z);
	}
	
	public static Vec3 vec3WithY(Vec3 original, double y) {
		return new Vec3(original.x, y, original.z);
	}
	
	public static Vec3 vec3WithZ(Vec3 original, double z) {
		return new Vec3(original.x, original.y, z);
	}
	
	public static Vec3 copyVec3(Vec3 other) {
		return new Vec3(other.x, other.y, other.z);
	}
	
	public static String formatVec3(Vec3 vec) {
		return String.format("%.2f, %.2f, %.2f", vec.x, vec.y, vec.z);
	}
	
	public static String listify(Iterable<String> strings) {
		StringBuilder builder = new StringBuilder();
		for (String str : strings) {
			builder.append(str);
			builder.append(", ");
		}
		builder.setLength(builder.length() - 2);
		return builder.toString();
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
	
	public static Path getServerDataPath(MinecraftServer server) {
		Path basePath = server.getFile("frontutil");
		if (!Files.isDirectory(basePath)) {
			try {
				Files.createDirectory(basePath);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return basePath;
	}
}
