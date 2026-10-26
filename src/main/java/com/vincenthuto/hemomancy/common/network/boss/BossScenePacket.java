package com.vincenthuto.hemomancy.common.network.boss;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Starts a boss scene on the client: the scene's id, the boss it frames, and how many ticks in it starts (a scene
 * started late, or one whose server window began before the packet, picks up where the server is).
 */
public record BossScenePacket(ResourceLocation scene, int entityId, int offset) implements CustomPacketPayload {
	public static final Type<BossScenePacket> TYPE = new Type<>(Hemomancy.rloc("boss_scene"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BossScenePacket> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, BossScenePacket::scene,
			ByteBufCodecs.VAR_INT, BossScenePacket::entityId,
			ByteBufCodecs.VAR_INT, BossScenePacket::offset,
			BossScenePacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
