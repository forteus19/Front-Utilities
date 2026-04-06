package red.vuis.frontutil.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class AddonCommandUtils {
	private AddonCommandUtils() {
	}
	
	public static @Nullable ServerPlayerEntity getContextPlayer(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayer();
		if (player == null) {
			source.sendError(Text.translatable("frontutil.message.command.error.player"));
			return null;
		}
		return player;
	}
}
