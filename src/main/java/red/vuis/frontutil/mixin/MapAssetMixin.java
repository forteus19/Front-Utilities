package red.vuis.frontutil.mixin;

import java.util.List;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.assets.AssetCommandValidators;
import com.boehmod.blockfront.assets.impl.MapAsset;
import com.boehmod.blockfront.client.mapeffect.AbstractMapEffect;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.util.BFAdminUtils;
import com.boehmod.blockfront.util.BFStyles;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.command.MapEffectCommands;
import red.vuis.frontutil.util.AddonUtils;

@Mixin(MapAsset.class)
public class MapAssetMixin {
    @Shadow @Final @NotNull public List<AbstractMapEffect> mapEffects;
    @Shadow private @NotNull String name;
    @Shadow @Final private @NotNull AssetCommandBuilder command;

    @Inject(
            method = "<init>(Ljava/lang/String;Ljava/lang/String;Lcom/boehmod/blockfront/common/match/DivisionData;Lcom/boehmod/blockfront/common/match/DivisionData;)V",
            at = @At("TAIL")
    )
    private void addMapEffectCommands(String par1, String par2, DivisionData par3, DivisionData par4, CallbackInfo ci) {
        AssetCommandBuilder baseCommand = command.subCommands.get("mapEffect");

        baseCommand.subCommand(
                "list",
                new AssetCommandBuilder((context, args) -> {
                    Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
                    CommandSource source = context.getSource().source;

                    if (mapEffects.isEmpty()) {
                        BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.list.none", nameComponent));
                        return;
                    }

                    BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.list.header", nameComponent));

                    for (int i = 0; i < mapEffects.size(); i++) {
                        AbstractMapEffect mapEffect = mapEffects.get(i);
                        BFAdminUtils.sendBfa(source, Component.literal(String.format("%d: %s", i, MapEffectCommands.getInfo(mapEffect))));
                    }
                }));

        baseCommand.subCommand(
                "remove",
                new AssetCommandBuilder((context, args) -> {
                    Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
                    CommandSource source = context.getSource().source;

                    var index = AddonUtils.parse(Integer::valueOf, args[0]);
                    if (index.isEmpty()) {
                        BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.error.index.number"));
                        return;
                    }

                    Component indexComponent = Component.literal(Integer.toString(index.get())).withStyle(BFStyles.LIME);
                    if (index.get() < 0 || index.get() >= mapEffects.size()) {
                        BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.error.index.bounds", indexComponent));
                        return;
                    }

                    mapEffects.remove((int) index.get());
                    BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.remove.success", indexComponent, nameComponent));
                }).validator(
                        AssetCommandValidators.count(new String[]{"index"})
                ));

        AssetCommandBuilder addCommand = baseCommand.subCommands.get("add");

        addCommand.subCommand(
                "loopingSoundPoint",
                new AssetCommandBuilder((context, args) -> {
                    Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
                    CommandSource source = context.getSource().source;

                    var mapEffect = MapEffectCommands.parseLoopingSoundPoint(args);
                    if (mapEffect == null) {
                        BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.loopingSoundPoint.error", nameComponent));
                        return;
                    }
                    mapEffects.add(mapEffect);

                    BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.loopingSoundPoint.success", nameComponent));
                }).validator(
                        AssetCommandValidators.count(new String[]{"count", "maxTick", "x", "y", "z"})
                ));

        addCommand.subCommand(
                "particleEmitter",
                new AssetCommandBuilder((context, args) -> {
                    Component nameComponent = Component.literal(name).withStyle(BFStyles.LIME);
                    CommandSource source = context.getSource().source;

                    if (!(args.length == 5 || args.length == 7)) {
                        BFAdminUtils.sendBfaWarn(source, Component.translatable("frontutil.message.command.error.args.count"));
                        BFAdminUtils.sendBfaWarn(source, Component.literal("particle, maxTick, x, y, z"));
                        BFAdminUtils.sendBfaWarn(source, Component.literal("particle, maxTick, x, y, z, sound, soundVolume"));
                        return;
                    }

                    var mapEffect = MapEffectCommands.parseParticleEmitter(args);
                    if (mapEffect == null) {
                        BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.particleEmitter.error", nameComponent));
                        return;
                    }
                    mapEffects.add(mapEffect);

                    BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.mapEffect.add.particleEmitter.success", nameComponent));
                }));
    }
}
