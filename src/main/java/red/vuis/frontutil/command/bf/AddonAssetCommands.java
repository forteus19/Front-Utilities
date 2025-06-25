package red.vuis.frontutil.command.bf;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.util.BFStyles;
import com.boehmod.blockfront.util.CommandUtils;
import com.boehmod.blockfront.util.math.FDSPose;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import red.vuis.frontutil.util.AddonCommandUtils;
import red.vuis.frontutil.util.AddonUtils;

public final class AddonAssetCommands {
	private AddonAssetCommands() {
	}
	
	public static @Nullable IntObjectPair<Component> parseIndex(CommandSource source, String arg, Collection<?> items, boolean includeEnd) {
		var index = AddonUtils.parse(Integer::parseInt, arg);
		if (index.isEmpty()) {
			CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.error.index.number"));
			return null;
		}
		
		Component indexComponent = Component.literal(Integer.toString(index.get())).withStyle(BFStyles.LIME);
		if (index.get() < 0 || index.get() >= items.size() + (includeEnd ? 1 : 0)) {
			CommandUtils.sendBfa(source, Component.translatable("frontutil.message.command.error.index.bounds", indexComponent));
			return null;
		}
		
		return new IntObjectImmutablePair<>(index.get(), indexComponent);
	}
	
	public static <T> void genericList(CommandContext<CommandSourceStack> context, Supplier<Component> noneMessage, Supplier<Component> headerMessage, List<T> items, Function<? super T, String> infoFunc) {
		CommandSource source = context.getSource().source;
		
		if (items.isEmpty()) {
			CommandUtils.sendBfa(source, noneMessage.get());
			return;
		}
		
		CommandUtils.sendBfa(source, headerMessage.get());
		for (int i = 0; i < items.size(); i++) {
			T item = items.get(i);
			CommandUtils.sendBfa(source, Component.literal(String.format("%d: %s", i, infoFunc.apply(item))));
		}
	}
	
	public static <T> void genericList(CommandContext<CommandSourceStack> context, @Nullable Supplier<String> name, String noneMessage, String headerMessage, List<T> items, Function<? super T, String> infoFunc) {
		if (name != null) {
			Supplier<Component> nameComponent = () -> Component.literal(name.get()).withStyle(BFStyles.LIME);
			genericList(
				context,
				() -> Component.translatable(noneMessage, nameComponent.get()),
				() -> Component.translatable(headerMessage, nameComponent.get()),
				items, infoFunc
			);
		} else {
			genericList(
				context,
				() -> Component.translatable(noneMessage),
				() -> Component.translatable(headerMessage),
				items, infoFunc
			);
		}
	}
	
	public static <T> void genericList(CommandContext<CommandSourceStack> context, String noneMessage, String headerMessage, List<T> items, Function<? super T, String> infoFunc) {
		genericList(context, null, noneMessage, headerMessage, items, infoFunc);
	}
	
	public static <T> AssetCommandBuilder genericList(String noneMessage, String headerMessage, List<T> items, Function<? super T, String> infoFunc) {
		return new AssetCommandBuilder(
			(context, args) -> genericList(context, noneMessage, headerMessage, items, infoFunc)
		);
	}
	
	public static void genericRemove(CommandContext<CommandSourceStack> context, @UnmodifiableView List<String> args, Function<Component, Component> successMessage, List<?> items) {
		CommandSource source = context.getSource().source;
		
		var indexParse = parseIndex(source, args.getFirst(), items, false);
		if (indexParse == null) {
			return;
		}
		int index = indexParse.leftInt();
		Component indexComponent = indexParse.right();
		
		items.remove(index);
		CommandUtils.sendBfa(source, successMessage.apply(indexComponent));
	}
	
	public static void genericRemove(CommandContext<CommandSourceStack> context, @UnmodifiableView List<String> args, @Nullable Supplier<String> name, String successMessage, List<?> items) {
		if (name != null) {
			Supplier<Component> nameComponent = () -> Component.literal(name.get()).withStyle(BFStyles.LIME);
			genericRemove(
				context, args,
				indexComponent -> Component.translatable(successMessage, indexComponent, nameComponent.get()),
				items
			);
		} else {
			genericRemove(
				context, args,
				indexComponent -> Component.translatable(successMessage, indexComponent),
				items
			);
		}
	}
	
	public static void genericRemove(CommandContext<CommandSourceStack> context, @UnmodifiableView List<String> args, String successMessage, List<?> items) {
		genericRemove(context, args, null, successMessage, items);
	}
	
	public static AssetCommandBuilder genericRemove(String successMessage, List<?> items) {
		return new AssetCommandBuilder(
			(context, args) -> genericRemove(context, List.of(args), successMessage, items)
		).validator(AssetCommandValidatorsEx.count("index"));
	}
	
	public static <T extends FDSPose> void genericTeleport(CommandContext<CommandSourceStack> context, @UnmodifiableView List<String> args, BiFunction<? super T, Component, Component> successMessage, List<T> items) {
		ServerPlayer player = AddonCommandUtils.getContextPlayer(context);
		if (player == null) {
			return;
		}
		
		var indexParse = AddonAssetCommands.parseIndex(player, args.getFirst(), items, false);
		if (indexParse == null) {
			return;
		}
		int index = indexParse.leftInt();
		Component indexComponent = indexParse.right();
		T item = items.get(index);
		
		AddonUtils.teleportBf(player, items.get(index));
		CommandUtils.sendBfa(player, successMessage.apply(item, indexComponent));
	}
	
	public static <T extends FDSPose> AssetCommandBuilder genericTeleport(BiFunction<? super T, Component, Component> successMessage, List<T> items) {
		return new AssetCommandBuilder(
			(context, args) -> genericTeleport(context, List.of(args), successMessage, items)
		).validator(AssetCommandValidatorsEx.count("index"));
	}
}
