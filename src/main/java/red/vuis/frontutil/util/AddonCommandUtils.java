package red.vuis.frontutil.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class AddonCommandUtils {
	private AddonCommandUtils() {
	}
	
	public static @Nullable ServerPlayer getContextPlayer(CommandContext<CommandSourceStack> context) {
		CommandSourceStack stack = context.getSource();
		ServerPlayer player = stack.getPlayer();
		if (player == null) {
			stack.source.sendSystemMessage(Component.translatable("frontutil.message.command.error.player"));
			return null;
		}
		return player;
	}
}
