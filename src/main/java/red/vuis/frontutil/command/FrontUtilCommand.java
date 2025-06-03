package red.vuis.frontutil.command;

import java.util.concurrent.CompletableFuture;

import com.boehmod.blockfront.registry.BFDataComponents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import red.vuis.frontutil.setup.GunSkinIndex;
import red.vuis.frontutil.util.CommandUtils;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class FrontUtilCommand {
	private FrontUtilCommand() {
	}
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		var root = literal("frontutil");
		
		root.then(
			literal("gun")
				.requires(stack -> stack.hasPermission(2))
				.then(
					argument("id", ResourceLocationArgument.id())
						.suggests(FrontUtilCommand::suggestGunId)
						.then(
							argument("skin", StringArgumentType.word())
								.suggests(FrontUtilCommand::suggestGunSkin)
								.executes(FrontUtilCommand::runGun)
						)
				)
		);
		
		dispatcher.register(root);
	}
	
	private static CompletableFuture<Suggestions> suggestGunId(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestions) {
		return SharedSuggestionProvider.suggest(GunSkinIndex.SKINS.keySet().stream().map(ResourceLocation::toString), suggestions);
	}
	
	private static CompletableFuture<Suggestions> suggestGunSkin(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestions) {
		ResourceLocation id = ResourceLocationArgument.getId(context, "id");
		if (!GunSkinIndex.SKINS.containsKey(id)) {
			return Suggestions.empty();
		}
		return SharedSuggestionProvider.suggest(GunSkinIndex.SKINS.get(id).keySet(), suggestions);
	}
	
	private static int runGun(CommandContext<CommandSourceStack> context) {
		ServerPlayer player = CommandUtils.getContextPlayer(context);
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
}
