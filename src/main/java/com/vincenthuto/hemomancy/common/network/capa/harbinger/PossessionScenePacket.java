package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

/**
 * Server to the possessor and everyone near: one half of a possession's cinematic starts. {@code scene} is a
 * {@code PossessionSceneRules.Scene} ordinal, or -1 to clear the player's scene (a skip or an abandoned sequence).
 * {@code anchor} is the entity the scene frames (surrogate, scene facsimile, the possessed player or the echo),
 * {@code echo} the echo when it plays a part (-1 otherwise), {@code site} the rite's Focus and {@code daemon} where the
 * rite's daemon hung when it completed.
 */
public record PossessionScenePacket(int player, int scene, int anchor, int echo, BlockPos site, Vec3 daemon)
		implements CustomPacketPayload {
	public static final Type<PossessionScenePacket> TYPE = new Type<>(Hemomancy.rloc("possession_scene"));
	public static final StreamCodec<FriendlyByteBuf, PossessionScenePacket> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> {
				buffer.writeVarInt(packet.player());
				buffer.writeVarInt(packet.scene());
				buffer.writeVarInt(packet.anchor());
				buffer.writeVarInt(packet.echo());
				buffer.writeBlockPos(packet.site());
				buffer.writeDouble(packet.daemon().x);
				buffer.writeDouble(packet.daemon().y);
				buffer.writeDouble(packet.daemon().z);
			},
			buffer -> new PossessionScenePacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
					buffer.readVarInt(), buffer.readBlockPos(),
					new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())));

	public static PossessionScenePacket clear(int player) {
		return new PossessionScenePacket(player, -1, -1, -1, BlockPos.ZERO, Vec3.ZERO);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
