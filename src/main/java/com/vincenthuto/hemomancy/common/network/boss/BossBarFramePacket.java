package com.vincenthuto.hemomancy.common.network.boss;

import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Tells a viewer which frame draws the boss bar with this id. Sent each time the bar is shown to them. */
public record BossBarFramePacket(UUID bar, ResourceLocation frame) implements CustomPacketPayload {
	public static final Type<BossBarFramePacket> TYPE = new Type<>(Hemomancy.rloc("boss_bar_frame"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BossBarFramePacket> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, BossBarFramePacket::bar,
			ResourceLocation.STREAM_CODEC, BossBarFramePacket::frame,
			BossBarFramePacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
