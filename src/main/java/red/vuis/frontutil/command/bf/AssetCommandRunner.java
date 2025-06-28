package red.vuis.frontutil.command.bf;

import java.util.function.BiConsumer;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

public interface AssetCommandRunner extends BiConsumer<CommandContext<CommandSourceStack>, String[]> {
}
