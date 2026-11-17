package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to the players tracking a channelling caster (and the caster): what an onlooker's client cannot work out for
 * itself, so another player's stream lands where the caster's does. Both verbs' reach (skills, Staff focus and rings
 * are not synced to other players), the active verb the Staff channels, and the registry name of the equipped,
 * selected manipulation ({@code ""} for none). Sent only while the caster channels, from {@code VerbPresence}.
 */
public record PacketSyncVerbPresence(int entityId, float projectReach, float absorbReach, Verb activeVerb, String selected)
		implements CustomPacketPayload {
	public static final Type<PacketSyncVerbPresence> TYPE = new Type<>(Hemomancy.rloc("sync_verb_presence"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncVerbPresence> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> {
				buffer.writeVarInt(packet.entityId());
				buffer.writeFloat(packet.projectReach());
				buffer.writeFloat(packet.absorbReach());
				buffer.writeEnum(packet.activeVerb());
				buffer.writeUtf(packet.selected(), 128);
			},
			buffer -> new PacketSyncVerbPresence(buffer.readVarInt(), buffer.readFloat(), buffer.readFloat(),
					buffer.readEnum(Verb.class), buffer.readUtf(128)));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
