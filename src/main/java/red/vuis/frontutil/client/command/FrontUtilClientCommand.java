package red.vuis.frontutil.client.command;

import com.boehmod.blockfront.client.BFClientManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.client.screen.LoadoutEditorScreen;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.setup.LoadoutIndex;

import static net.minecraft.commands.Commands.literal;

public final class FrontUtilClientCommand {
	private FrontUtilClientCommand() {
	}
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		var root = literal("frontutil").requires(stack -> stack.hasPermission(2));
		
		root.then(
			literal("loadout").then(
				literal("openEditor").executes(FrontUtilClientCommand::loadoutOpenEditor)
			).then(
				literal("sync").executes(FrontUtilClientCommand::loadoutSync)
			)
		);
		
		dispatcher.register(root);
	}
	
	private static int loadoutOpenEditor(CommandContext<CommandSourceStack> context) {
		BFClientManager manager = BFClientManager.getInstance();
		if (manager == null) {
			return 0;
		}
		if (manager.getGame() != null) {
			context.getSource().sendFailure(Component.translatable("frontutil.message.command.client.loadout.openEditor.error.match"));
			return -1;
		}
		
		Minecraft.getInstance().setScreen(new LoadoutEditorScreen());
		return 1;
	}
	
	private static int loadoutSync(CommandContext<CommandSourceStack> context) {
		BFClientManager manager = BFClientManager.getInstance();
		if (manager == null) {
			return 0;
		}
		if (manager.getGame() != null) {
			context.getSource().sendFailure(Component.translatable("frontutil.message.command.client.loadout.sync.error.match"));
			return -1;
		}
		
		PacketDistributor.sendToServer(new LoadoutsPacket(LoadoutIndex.getAsMap()));
		return 1;
	}
}
