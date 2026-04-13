package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.worldevent.BloodMoonClientState;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server → Client: Syncs blood moon active state so the client can check it
 * in {@link com.vincenthuto.hemomancy.common.manipulation.BloodManipulation}.
 */
public class PacketSyncBloodMoon {

	private final boolean active;

	public PacketSyncBloodMoon(boolean active) {
		this.active = active;
	}

	public static void encode(PacketSyncBloodMoon msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.active);
	}

	public static PacketSyncBloodMoon decode(FriendlyByteBuf buf) {
		return new PacketSyncBloodMoon(buf.readBoolean());
	}

	public static void handle(PacketSyncBloodMoon msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> BloodMoonClientState.set(msg.active));
		ctx.get().setPacketHandled(true);
	}
}
