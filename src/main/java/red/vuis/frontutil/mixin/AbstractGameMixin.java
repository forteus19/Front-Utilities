package red.vuis.frontutil.mixin;

import com.boehmod.bflib.fds.tag.FDSTagCompound;
import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.util.BFStyles;
import com.boehmod.blockfront.util.CommandUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.client.data.config.AddonClientConfig;
import red.vuis.frontutil.client.data.config.MatchHudStyle;
import red.vuis.frontutil.command.bf.AssetCommandValidatorsEx;
import red.vuis.frontutil.ex.AbstractGameEx;
import red.vuis.frontutil.util.AddonUtils;
import red.vuis.frontutil.util.property.PropertyHandleResult;

@Mixin(AbstractGame.class)
public abstract class AbstractGameMixin implements AbstractGameEx {
	@Shadow
	@Final
	@NotNull
	public AssetCommandBuilder baseCommand;
	
	@Shadow
	@NotNull
	protected String type;
	@Unique
	private boolean frontutil$forceClientConfig = false;
	@Unique
	private AddonClientConfig.Data frontutil$clientConfig = new AddonClientConfig.Data();
	
	@Inject(
		method = "<init>(Lcom/boehmod/blockfront/common/BFAbstractManager;Ljava/lang/String;Ljava/lang/String;)V",
		at = @At("TAIL")
	)
	private void addCommands(BFAbstractManager<?, ?, ?> manager, String type, String displayName, CallbackInfo ci) {
		baseCommand.subCommand("frontutil", frontutil$addFrontutilCommands(new AssetCommandBuilder()));
	}
	
	@Unique
	private AssetCommandBuilder frontutil$addFrontutilCommands(AssetCommandBuilder baseCommand) {
		baseCommand.subCommand("forceClientConfig", new AssetCommandBuilder((context, args) -> {
			CommandOutput output = context.getSource().output;
			
			var value = AddonUtils.parse(Boolean::parseBoolean, args[0]);
			if (value.isEmpty()) {
				CommandUtils.sendBfa(output, Text.translatable("frontutil.message.command.error.value.boolean"));
			}
			
			frontutil$forceClientConfig = value.orElseThrow();
			CommandUtils.sendBfa(output,
				frontutil$forceClientConfig ?
					Text.translatable("frontutil.message.command.game.frontutil.forceClientConfig.success.enabled") :
					Text.translatable("frontutil.message.command.game.frontutil.forceClientConfig.success.disabled")
			);
		}).validator(
			AssetCommandValidatorsEx.count("value")
		));
		
		baseCommand.subCommand("clientConfig", new AssetCommandBuilder((context, args) -> {
			CommandOutput output = context.getSource().output;
			
			String property = args[0];
			String value = args[1];
			
			PropertyHandleResult result = AddonClientConfig.Data.PROPERTIES.handle(frontutil$clientConfig, property, value);
			assert result != PropertyHandleResult.ERROR_TYPE;
			switch (result) {
				case SUCCESS -> CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.game.clientConfig.success",
					Text.literal(property).fillStyle(BFStyles.LIME),
					Text.literal(value).fillStyle(BFStyles.LIME)
				));
				case ERROR_PROPERTY -> CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.error.property.missing",
					Text.literal(property).fillStyle(BFStyles.LIME)
				));
				case ERROR_PARSE -> CommandUtils.sendBfa(output, Text.translatable(
					"frontutil.message.command.error.property.parse",
					Text.literal(value).fillStyle(BFStyles.LIME),
					Text.literal(property).fillStyle(BFStyles.LIME)
				));
			}
		}).validator(
			AssetCommandValidatorsEx.count("property", "value")
		));
		
		return baseCommand;
	}
	
	@Inject(
		method = "writeFDS",
		at = @At("TAIL")
	)
	private void writeCustomFDS(FDSTagCompound root, CallbackInfo ci) {
		root.setBoolean("frontutil_forceClientConfig", frontutil$forceClientConfig);
		root.setTagCompound("frontutil_clientConfig", Util.make(new FDSTagCompound(), clientConfigRoot -> {
			clientConfigRoot.setString("matchHudStyle", frontutil$clientConfig.getMatchHudStyle().toString());
			clientConfigRoot.setBoolean("renderCorpses", frontutil$clientConfig.isRenderCorpses());
			clientConfigRoot.setBoolean("enableDeathFade", frontutil$clientConfig.isEnableDeathFade());
			clientConfigRoot.setInteger("killFeedLines", frontutil$clientConfig.getKillFeedLines());
		}));
	}
	
	@Inject(
		method = "readFDS",
		at = @At("TAIL")
	)
	private void readCustomFDS(FDSTagCompound root, CallbackInfo ci) {
		frontutil$forceClientConfig = root.getBoolean("frontutil_forceClientConfig", false);
		FDSTagCompound clientConfigRoot = root.getTagCompound("frontutil_clientConfig", new FDSTagCompound());
		frontutil$clientConfig = new AddonClientConfig.Data(
			AddonUtils.retrieveEnumOrDefault(MatchHudStyle.values(), clientConfigRoot.getString("matchHudStyle"), AddonClientConfig.Data.MATCH_HUD_STYLE_DEFAULT),
			clientConfigRoot.getBoolean("renderCorpses", AddonClientConfig.Data.RENDER_CORPSES_DEFAULT),
			clientConfigRoot.getBoolean("enableDeathFade", AddonClientConfig.Data.ENABLE_DEATH_FADE_DEFAULT),
			clientConfigRoot.getInteger("killFeedLines", AddonClientConfig.Data.KILL_FEED_LINES_DEFAULT)
		);
	}
	
	@Inject(
		method = "write",
		at = @At("TAIL")
	)
	private void writeCustomBuf(ByteBuf buf, CallbackInfo ci) {
		buf.writeBoolean(frontutil$forceClientConfig);
		AddonClientConfig.Data.PACKET_CODEC.encode(buf, frontutil$clientConfig);
	}
	
	@Inject(
		method = "read",
		at = @At("TAIL")
	)
	private void readCustomBuf(ByteBuf buf, CallbackInfo ci) {
		frontutil$forceClientConfig = buf.readBoolean();
		frontutil$clientConfig = AddonClientConfig.Data.PACKET_CODEC.decode(buf);
	}
	
	@Override
	public boolean frontutil$isForceClientConfig() {
		return frontutil$forceClientConfig;
	}
	
	@Override
	public AddonClientConfig.Data frontutil$getClientConfig() {
		return frontutil$clientConfig;
	}
}
