package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.network.NetworkEvent;

public class PacketUpdatePlayerAvatarPose {

	public PacketUpdatePlayerAvatarPose() {

	}

	public static void handle(final PacketUpdatePlayerAvatarPose msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft.getInstance().player.setForcedPose(Pose.SPIN_ATTACK);
			Minecraft.getInstance().player.setPose(Pose.STANDING);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketUpdatePlayerAvatarPose msg, final FriendlyByteBuf buf) {

	}

	public static PacketUpdatePlayerAvatarPose decode(final FriendlyByteBuf buf) {

		return new PacketUpdatePlayerAvatarPose();
	}
}