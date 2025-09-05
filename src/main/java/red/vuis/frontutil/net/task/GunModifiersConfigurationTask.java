package red.vuis.frontutil.net.task;

import java.util.function.Consumer;

import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.net.packet.GunModifiersPacket;

public record GunModifiersConfigurationTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
	public static final Key KEY = new Key(AddonConstants.id("gun_modifiers"));
	
	@Override
	public void run(@NotNull Consumer<CustomPayload> sender) {
		sender.accept(new GunModifiersPacket(GunModifier.ACTIVE));
		listener.onTaskFinished(KEY);
	}
	
	@Override
	public Key getKey() {
		return null;
	}
}
