package com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.EnumOrgan;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.VisceralMirrorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server → Client packet that carries ritual progress updates so the
 * Visceral Mirror screen can show a smooth progress bar.
 */
public class VisceralMirrorUpdatePacket implements CustomPacketPayload {

	public static final Type<VisceralMirrorUpdatePacket> TYPE = new Type<>(Hemomancy.rloc("visceral_mirror_update_packet"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VisceralMirrorUpdatePacket> STREAM_CODEC = StreamCodec.of(VisceralMirrorUpdatePacket::encode, VisceralMirrorUpdatePacket::decode);

	private final BlockPos pos;
	private final int phaseOrdinal;
	private final int ritualTicks;
	private final int totalRitualTicks;
	private final int organOrdinal; // -1 if no organ
	private final Component statusMessage;

	public VisceralMirrorUpdatePacket(BlockPos pos, VisceralMirrorBlockEntity.RitualPhase phase,
			int ritualTicks, int totalRitualTicks, EnumOrgan organ, Component statusMessage) {
		this.pos = pos;
		this.phaseOrdinal = phase.ordinal();
		this.ritualTicks = ritualTicks;
		this.totalRitualTicks = totalRitualTicks;
		this.organOrdinal = organ != null ? organ.ordinal() : -1;
		this.statusMessage = statusMessage != null ? statusMessage : Component.empty();
	}

	public static void encode(RegistryFriendlyByteBuf buf, VisceralMirrorUpdatePacket msg) {
		buf.writeBlockPos(msg.pos);
		buf.writeInt(msg.phaseOrdinal);
		buf.writeInt(msg.ritualTicks);
		buf.writeInt(msg.totalRitualTicks);
		buf.writeInt(msg.organOrdinal);
		ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, msg.statusMessage);
	}

	public static VisceralMirrorUpdatePacket decode(RegistryFriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int phaseOrd = buf.readInt();
		int ritualTicks = buf.readInt();
		int totalRitualTicks = buf.readInt();
		int organOrd = buf.readInt();
		Component statusMsg = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);

		VisceralMirrorBlockEntity.RitualPhase phase =
				phaseOrd >= 0 && phaseOrd < VisceralMirrorBlockEntity.RitualPhase.values().length
				? VisceralMirrorBlockEntity.RitualPhase.values()[phaseOrd]
				: VisceralMirrorBlockEntity.RitualPhase.IDLE;
		EnumOrgan organ = organOrd >= 0 && organOrd < EnumOrgan.values().length
				? EnumOrgan.values()[organOrd] : null;

		return new VisceralMirrorUpdatePacket(pos, phase, ritualTicks, totalRitualTicks, organ, statusMsg);
	}

	public BlockPos getPos() { return pos; }
	public int getPhaseOrdinal() { return phaseOrdinal; }
	public int getRitualTicks() { return ritualTicks; }
	public int getTotalRitualTicks() { return totalRitualTicks; }
	public int getOrganOrdinal() { return organOrdinal; }
	public Component getStatusMessage() { return statusMessage; }

	public VisceralMirrorBlockEntity.RitualPhase getPhase() {
		return phaseOrdinal >= 0 && phaseOrdinal < VisceralMirrorBlockEntity.RitualPhase.values().length
				? VisceralMirrorBlockEntity.RitualPhase.values()[phaseOrdinal]
				: VisceralMirrorBlockEntity.RitualPhase.IDLE;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
