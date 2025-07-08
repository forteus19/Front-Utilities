package red.vuis.frontutil.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.boehmod.blockfront.registry.BFDataComponents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.data.GunModifierTarget;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.setup.GunSkinIndex;
import red.vuis.frontutil.setup.LoadoutIndex;
import red.vuis.frontutil.util.AddonCommandUtils;
import red.vuis.frontutil.util.AddonUtils;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class FrontUtilCommand {
	private FrontUtilCommand() {
	}
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		var root = literal("frontutil").requires(stack -> stack.hasPermission(3));
		
		root.then(
			literal("gun").then(
				literal("giveWithSkin").then(
					argument("id", ResourceLocationArgument.id()).suggests(FrontUtilCommand::suggestGunGiveId).then(
						argument("skin", StringArgumentType.word()).suggests(FrontUtilCommand::suggestGunGiveSkin).executes(FrontUtilCommand::gunGiveWithSkin)
					)
				)
			).then(
				literal("modifier").then(
					literal("listAll").executes(FrontUtilCommand::gunModifierListAll)
				)
			)
		).then(
			literal("loadout").then(
				literal("list").executes(FrontUtilCommand::loadoutList)
			).then(
				literal("read").then(
					argument("filename", StringArgumentType.word()).executes(FrontUtilCommand::loadoutRead)
				)
			).then(
				literal("write").then(
					argument("filename", StringArgumentType.word()).executes(FrontUtilCommand::loadoutWrite)
				)
			)
		);
		
		dispatcher.register(root);
	}
	
	private static CompletableFuture<Suggestions> suggestGunGiveId(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestions) {
		return SharedSuggestionProvider.suggest(GunSkinIndex.SKINS.keySet().stream().map(ResourceLocation::toString), suggestions);
	}
	
	private static CompletableFuture<Suggestions> suggestGunGiveSkin(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestions) {
		ResourceLocation id = ResourceLocationArgument.getId(context, "id");
		if (!GunSkinIndex.SKINS.containsKey(id)) {
			return Suggestions.empty();
		}
		return SharedSuggestionProvider.suggest(GunSkinIndex.SKINS.get(id).keySet(), suggestions);
	}
	
	private static int gunGiveWithSkin(CommandContext<CommandSourceStack> context) {
		ServerPlayer player = AddonCommandUtils.getContextPlayer(context);
		if (player == null) {
			return -1;
		}
		
		ResourceLocation id = ResourceLocationArgument.getId(context, "id");
		String skin = StringArgumentType.getString(context, "skin");
		
		Item item = BuiltInRegistries.ITEM.get(id);
		if (item == Items.AIR) {
			return -1;
		}
		if (!GunSkinIndex.SKINS.get(id).containsKey(skin)) {
			return -1;
		}
		
		ItemStack itemStack = new ItemStack(item);
		itemStack.set(BFDataComponents.SKIN_ID.get(), GunSkinIndex.SKINS.get(id).getFloat(skin));
		
		player.addItem(itemStack);
		return 1;
	}
	
	private static int gunModifierListAll(CommandContext<CommandSourceStack> context) {
		CommandSource source = context.getSource().source;
		
		for (GunModifierTarget target : GunModifierTarget.ACTIVE) {
			source.sendSystemMessage(Component.literal(target.toString()));
		}
		
		return 1;
	}
	
	private static int loadoutList(CommandContext<CommandSourceStack> context) {
		CommandSourceStack sourceStack = context.getSource();
		
		Path loadoutsPath = AddonUtils.getServerDataPath(sourceStack.getServer()).resolve("loadouts");
		if (!Files.isDirectory(loadoutsPath)) {
			sourceStack.sendFailure(Component.translatable("frontutil.message.command.loadout.list.none"));
			return 0;
		}
		
		String filenameList;
		try (Stream<Path> filesList = Files.list(loadoutsPath)) {
			filenameList = AddonUtils.listify(
				filesList
					.map(path -> path.getFileName().toString())
					.iterator()
			);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		sourceStack.sendSuccess(() -> Component.translatable("frontutil.message.command.loadout.list.success", filenameList), false);
		return 1;
	}
	
	private static int loadoutRead(CommandContext<CommandSourceStack> context) {
		CommandSourceStack sourceStack = context.getSource();
		
		if (AddonUtils.anyGamesActive()) {
			sourceStack.sendFailure(Component.translatable("frontutil.message.command.loadout.read.game"));
			return -1;
		}
		
		String filename = StringArgumentType.getString(context, "filename") + ".dat";
		Path indexPath = AddonUtils.getServerDataPath(sourceStack.getServer()).resolve("loadouts").resolve(filename);
		
		if (!LoadoutIndex.parseAndApply(indexPath)) {
			sourceStack.sendFailure(Component.translatable("frontutil.message.command.loadout.read.error"));
			return -1;
		}
		
		PacketDistributor.sendToAllPlayers(new LoadoutsPacket(LoadoutIndex.currentFlat()));
		
		sourceStack.sendSuccess(() -> Component.translatable("frontutil.message.command.loadout.read.success", filename), true);
		return 1;
	}
	
	private static int loadoutWrite(CommandContext<CommandSourceStack> context) {
		CommandSourceStack sourceStack = context.getSource();
		
		String filename = StringArgumentType.getString(context, "filename") + ".dat";
		
		Path loadoutsPath = AddonUtils.getServerDataPath(sourceStack.getServer()).resolve("loadouts");
		if (!Files.isDirectory(loadoutsPath)) {
			try {
				Files.createDirectory(loadoutsPath);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		Path indexPath = loadoutsPath.resolve(filename);

		if (!LoadoutIndex.saveCurrent(indexPath)) {
			sourceStack.sendFailure(Component.translatable("frontutil.message.command.loadout.write.error"));
			return -1;
		}
		
		return 1;
	}
}
