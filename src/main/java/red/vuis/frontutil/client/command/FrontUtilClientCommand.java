package red.vuis.frontutil.client.command;

import java.util.concurrent.CompletableFuture;

import com.boehmod.blockfront.assets.impl.MapAsset;
import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.common.item.GunItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.client.data.AddonClientData;
import red.vuis.frontutil.client.screen.GunModifierEditorScreen;
import red.vuis.frontutil.client.screen.LoadoutEditorScreen;
import red.vuis.frontutil.client.screen.WeaponExtraScreen;
import red.vuis.frontutil.command.arg.AssetArgument;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.setup.GunItemIndex;
import red.vuis.frontutil.setup.LoadoutIndex;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class FrontUtilClientCommand {
	private FrontUtilClientCommand() {
	}
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		var root = literal("frontutil").requires(stack -> stack.hasPermissionLevel(3));
		
		root
//			.then(
//			literal("editorMode").then(
//				literal("off").executes(FrontUtilClientCommand::editorModeOff)
//			).then(
//				literal("on").then(
//					argument("mapAsset", AssetArgument.asset(MapAsset.class)).then(
//						argument("environment", StringArgumentType.word()).suggests(FrontUtilClientCommand::suggestMapEnvironments).executes(FrontUtilClientCommand::editorModeOn)
//					)
//				)
//			)
//		)
			.then(
			literal("gun").then(
				literal("giveMenu").then(
					argument("item", IdentifierArgumentType.identifier()).suggests(FrontUtilClientCommand::suggestGunItems).executes(FrontUtilClientCommand::gunGiveMenu)
				)
			).then(
				literal("modifier").then(
					literal("editor").then(
						argument("item", IdentifierArgumentType.identifier()).suggests(FrontUtilClientCommand::suggestGunItems).executes(FrontUtilClientCommand::gunModifierEditor)
					)
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
	
	private static int editorModeOff(CommandContext<ServerCommandSource> context) {
		AddonClientData clientData = AddonClientData.getInstance();
		clientData.editingMapName = null;
		clientData.editingEnv = null;
		
		return 1;
	}
	
	private static CompletableFuture<Suggestions> suggestMapEnvironments(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestions) {
		MapAsset asset = AssetArgument.getAsset(context, "mapAsset", MapAsset.class);
		return CommandSource.suggestMatching(asset.getEnvironments().keySet(), suggestions);
	}
	
	private static int editorModeOn(CommandContext<ServerCommandSource> context) {
		PlayerEntity player = MinecraftClient.getInstance().player;
		assert player != null;
		
		if (!(player.isCreative() || player.isSpectator())) {
			context.getSource().sendError(Text.translatable("frontutil.message.command.editorMode.error.mode"));
			return -1;
		}
		
		MapAsset asset = AssetArgument.getAsset(context, "mapAsset", MapAsset.class);
		String envStr = StringArgumentType.getString(context, "environment");
		
		if (!asset.environments.containsKey(envStr)) {
			context.getSource().sendError(Text.translatable("frontutil.message.command.editorMode.error.environment"));
			return -1;
		}
		
		AddonClientData clientData = AddonClientData.getInstance();
		clientData.editingMapName = asset.getName();
		clientData.editingEnv = asset.environments.get(envStr);
		
		return 1;
	}
	
	private static CompletableFuture<Suggestions> suggestGunItems(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestions) {
		return CommandSource.suggestMatching(GunItemIndex.GUN_ITEMS.stream().map(Identifier::toString), suggestions);
	}
	
	private static int gunGiveMenu(CommandContext<ServerCommandSource> context) {
		Item item = Registries.ITEM.get(IdentifierArgumentType.getIdentifier(context, "item"));
		if (!(item instanceof GunItem gunItem)) {
			context.getSource().sendError(Text.translatable("frontutil.message.command.error.gunItem"));
			return -1;
		}
		
		MinecraftClient.getInstance().setScreen(new WeaponExtraScreen(null, gunItem).sendGivePacket());
		
		return 1;
	}
	
	private static int gunModifierEditor(CommandContext<ServerCommandSource> context) {
		Item item = Registries.ITEM.get(IdentifierArgumentType.getIdentifier(context, "item"));
		if (!(item instanceof GunItem gunItem)) {
			context.getSource().sendError(Text.translatable("frontutil.message.command.error.gunItem"));
			return -1;
		}
		
		MinecraftClient.getInstance().setScreen(new GunModifierEditorScreen(gunItem));
		
		return 1;
	}
	
	private static int loadoutOpenEditor(CommandContext<ServerCommandSource> context) {
		BFClientManager manager = BFClientManager.getInstance();
		if (manager == null) {
			return 0;
		}
		if (manager.getGame() != null) {
			context.getSource().sendError(Text.translatable("frontutil.message.command.loadout.openEditor.error.client.match"));
			return -1;
		}
		
		MinecraftClient.getInstance().setScreen(new LoadoutEditorScreen());
		return 1;
	}
	
	private static int loadoutSync(CommandContext<ServerCommandSource> context) {
		BFClientManager manager = BFClientManager.getInstance();
		if (manager == null) {
			return 0;
		}
		if (manager.getGame() != null) {
			context.getSource().sendError(Text.translatable("frontutil.message.command.loadout.sync.error.client.match"));
			return -1;
		}
		
		PacketDistributor.sendToServer(new LoadoutsPacket(LoadoutIndex.currentFlat()));
		return 1;
	}
}
