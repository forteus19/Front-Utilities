package red.vuis.frontutil.command.bf;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.assets.AssetCommandValidators;
import com.boehmod.blockfront.util.BFAdminUtils;
import com.boehmod.blockfront.util.BFStyles;
import com.boehmod.blockfront.util.math.FDSPose;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.util.AddonUtils;
import red.vuis.frontutil.util.CommandUtils;

public final class AddonAssetCommands {
	private AddonAssetCommands() {
	}
	
	public static @Nullable IntObjectPair<Component> parseIndex(CommandSource source, String arg, Collection<?> items, boolean includeEnd) {
		var index = AddonUtils.parse(Integer::parseInt, arg);
		if (index.isEmpty()) {
			BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.error.index.number"));
			return null;
		}
		
		Component indexComponent = Component.literal(Integer.toString(index.get())).withStyle(BFStyles.LIME);
		if (index.get() < 0 || index.get() >= items.size() + (includeEnd ? 1 : 0)) {
			BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.error.index.bounds", indexComponent));
			return null;
		}
		
		return new IntObjectImmutablePair<>(index.get(), indexComponent);
	}
	
	public static <T> AssetCommandBuilder genericList(Supplier<Component> noneMessage, Supplier<Component> headerMessage, List<T> items, Function<? super T, String> infoFunc) {
		return new AssetCommandBuilder((context, args) -> {
			CommandSource source = context.getSource().source;
			
			if (items.isEmpty()) {
				BFAdminUtils.sendBfa(source, noneMessage.get());
				return;
			}
			
			BFAdminUtils.sendBfa(source, headerMessage.get());
			
			for (int i = 0; i < items.size(); i++) {
				T item = items.get(i);
				BFAdminUtils.sendBfa(source, Component.literal(String.format("%d: %s", i, infoFunc.apply(item))));
			}
		});
	}
	
	public static <T> AssetCommandBuilder genericList(@Nullable Supplier<String> name, String noneMessage, String headerMessage, List<T> items, Function<? super T, String> infoFunc) {
		if (name != null) {
			Supplier<Component> nameComponent = () -> Component.literal(name.get()).withStyle(BFStyles.LIME);
			return genericList(
				() -> Component.translatable(noneMessage, nameComponent.get()),
				() -> Component.translatable(headerMessage, nameComponent.get()),
				items, infoFunc
			);
		} else {
			return genericList(
				() -> Component.translatable(noneMessage),
				() -> Component.translatable(headerMessage),
				items, infoFunc
			);
		}
	}
	
	public static <T> AssetCommandBuilder genericList(String noneMessage, String headerMessage, List<T> items, Function<? super T, String> infoFunc) {
		return genericList(null, noneMessage, headerMessage, items, infoFunc);
	}
	
	public static AssetCommandBuilder genericRemove(Function<Component, Component> successMessage, List<?> items) {
		return new AssetCommandBuilder((context, args) -> {
			CommandSource source = context.getSource().source;
			
			var indexParse = parseIndex(source, args[0], items, false);
			if (indexParse == null) {
				return;
			}
			int index = indexParse.leftInt();
			Component indexComponent = indexParse.right();
			
			items.remove(index);
			BFAdminUtils.sendBfa(source, successMessage.apply(indexComponent));
		}).validator(
			AssetCommandValidators.count(new String[]{"index"})
		);
	}
	
	public static AssetCommandBuilder genericRemove(@Nullable Supplier<String> name, String successMessage, List<?> items) {
		if (name != null) {
			Supplier<Component> nameComponent = () -> Component.literal(name.get()).withStyle(BFStyles.LIME);
			return genericRemove(
				indexComponent -> Component.translatable(successMessage, indexComponent, nameComponent.get()),
				items
			);
		} else {
			return genericRemove(
				indexComponent -> Component.translatable(successMessage, indexComponent),
				items
			);
		}
	}
	
	public static AssetCommandBuilder genericRemove(String successMessage, List<?> items) {
		return genericRemove(null, successMessage, items);
	}
	
	public static <T extends FDSPose> AssetCommandBuilder genericTeleport(BiFunction<T, Component, Component> successMessage, List<T> items) {
		return new AssetCommandBuilder((context, args) -> {
			ServerPlayer player = CommandUtils.getContextPlayer(context);
			if (player == null) {
				return;
			}
			
			var indexParse = AddonAssetCommands.parseIndex(player, args[0], items, false);
			if (indexParse == null) {
				return;
			}
			int index = indexParse.leftInt();
			Component indexComponent = indexParse.right();
			T item = items.get(index);
			
			AddonUtils.teleportBf(player, items.get(index));
			BFAdminUtils.sendBfa(player, successMessage.apply(item, indexComponent));
		}).validator(
			AssetCommandValidators.count(new String[]{"index"})
		);
	}
}
