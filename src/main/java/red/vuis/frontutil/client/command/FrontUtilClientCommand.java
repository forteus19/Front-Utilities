package red.vuis.frontutil.client.command;

import java.util.concurrent.CompletableFuture;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.common.item.GunItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.client.FrontUtilClient;
import red.vuis.frontutil.client.screen.LoadoutEditorScreen;
import red.vuis.frontutil.client.screen.WeaponExtraScreen;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.setup.GunItemIndex;
import red.vuis.frontutil.setup.LoadoutIndex;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class FrontUtilClientCommand {
	private FrontUtilClientCommand() {
	}
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		var root = literal("frontutil").requires(stack -> stack.hasPermission(3));
		
		root.then(
			literal("config").executes(FrontUtilClientCommand::config)
		).then(
			literal("gun").then(
				literal("giveMenu").then(
					argument("item", ResourceLocationArgument.id()).suggests(FrontUtilClientCommand::suggestGunItems).executes(FrontUtilClientCommand::gunGiveMenu)
				)
			)
		).then(
			literal("loadout").then(
				literal("openEditor").executes(FrontUtilClientCommand::loadoutOpenEditor)
			).then(
				literal("sync").executes(FrontUtilClientCommand::loadoutSync)
			)
		);
		
		dispatcher.register(root);
	}
	
	@SuppressWarnings("DataFlowIssue")
	private static int config(CommandContext<CommandSourceStack> context) {
		// shut up intellij it can be null >:(
		Minecraft.getInstance().setScreen(new ConfigurationScreen(FrontUtilClient.getInstance().container, null));
		
		return 1;
	}
	
	private static CompletableFuture<Suggestions> suggestGunItems(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestions) {
		return SharedSuggestionProvider.suggest(GunItemIndex.GUN_ITEMS.stream().map(ResourceLocation::toString), suggestions);
	}
	
	private static int gunGiveMenu(CommandContext<CommandSourceStack> context) {
		Item item = BuiltInRegistries.ITEM.get(ResourceLocationArgument.getId(context, "item"));
		if (!(item instanceof GunItem gunItem)) {
			context.getSource().sendFailure(Component.translatable("frontutil.message.command.gun.giveMenu.error.item"));
			return -1;
		}
		
		Minecraft.getInstance().setScreen(new WeaponExtraScreen(null, gunItem).sendGivePacket());
		
		return 1;
	}
	
	private static int loadoutOpenEditor(CommandContext<CommandSourceStack> context) {
		BFClientManager manager = BFClientManager.getInstance();
		if (manager == null) {
			return 0;
		}
		if (manager.getGame() != null) {
			context.getSource().sendFailure(Component.translatable("frontutil.message.command.loadout.openEditor.error.client.match"));
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
			context.getSource().sendFailure(Component.translatable("frontutil.message.command.loadout.sync.error.client.match"));
			return -1;
		}
		
		PacketDistributor.sendToServer(new LoadoutsPacket(LoadoutIndex.currentFlat()));
		return 1;
	}
}
