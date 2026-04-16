package com.vincenthuto.hemomancy.common.network.capa.visceral;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.client.screen.tile.functional.VisceralMirrorScreen;
import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;
import com.vincenthuto.hemomancy.common.tile.functional.VisceralMirrorBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server → Client packet that carries ritual progress updates so the
 * Visceral Mirror screen can show a smooth progress bar.
 */
public class VisceralMirrorUpdatePacket {

	private final BlockPos pos;
	private final int phaseOrdinal;
	private final int ritualTicks;
	private final int totalRitualTicks;
	private final int organOrdinal; // -1 if no organ
	private final String statusMessage;

	public VisceralMirrorUpdatePacket(BlockPos pos, VisceralMirrorBlockEntity.RitualPhase phase,
			int ritualTicks, int totalRitualTicks, EnumOrgan organ, String statusMessage) {
		this.pos = pos;
		this.phaseOrdinal = phase.ordinal();
		this.ritualTicks = ritualTicks;
		this.totalRitualTicks = totalRitualTicks;
		this.organOrdinal = organ != null ? organ.ordinal() : -1;
		this.statusMessage = statusMessage != null ? statusMessage : "";
	}

	public static void encode(VisceralMirrorUpdatePacket msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
		buf.writeInt(msg.phaseOrdinal);
		buf.writeInt(msg.ritualTicks);
		buf.writeInt(msg.totalRitualTicks);
		buf.writeInt(msg.organOrdinal);
		buf.writeUtf(msg.statusMessage);
	}

	public static VisceralMirrorUpdatePacket decode(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int phaseOrd = buf.readInt();
		int ritualTicks = buf.readInt();
		int totalRitualTicks = buf.readInt();
		int organOrd = buf.readInt();
		String statusMsg = buf.readUtf();

		VisceralMirrorBlockEntity.RitualPhase phase =
				phaseOrd >= 0 && phaseOrd < VisceralMirrorBlockEntity.RitualPhase.values().length
				? VisceralMirrorBlockEntity.RitualPhase.values()[phaseOrd]
				: VisceralMirrorBlockEntity.RitualPhase.IDLE;
		EnumOrgan organ = organOrd >= 0 && organOrd < EnumOrgan.values().length
				? EnumOrgan.values()[organOrd] : null;

		return new VisceralMirrorUpdatePacket(pos, phase, ritualTicks, totalRitualTicks, organ, statusMsg);
	}

	public static void handle(VisceralMirrorUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.screen instanceof VisceralMirrorScreen screen) {
				screen.handleServerUpdate(msg);
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public BlockPos getPos() { return pos; }
	public int getPhaseOrdinal() { return phaseOrdinal; }
	public int getRitualTicks() { return ritualTicks; }
	public int getTotalRitualTicks() { return totalRitualTicks; }
	public int getOrganOrdinal() { return organOrdinal; }
	public String getStatusMessage() { return statusMessage; }

	public VisceralMirrorBlockEntity.RitualPhase getPhase() {
		return phaseOrdinal >= 0 && phaseOrdinal < VisceralMirrorBlockEntity.RitualPhase.values().length
				? VisceralMirrorBlockEntity.RitualPhase.values()[phaseOrdinal]
				: VisceralMirrorBlockEntity.RitualPhase.IDLE;
	}
}
