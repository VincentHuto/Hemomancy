package com.vincenthuto.hemomancy.common.network.mission;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.item.HarbingerAssignmentLedgerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenHarbingerAssignmentLedgerPacket(
		int degree,
		boolean firstAwakening,
		boolean degreeOne,
		boolean firstRemnant,
		boolean ledgerGranted,
		int redTaxonomyCount,
		boolean redTaxonomyComplete,
		boolean hasBlankHematicMemory,
		boolean mnemonistWovenVesselComplete) implements CustomPacketPayload {
	public static final Type<OpenHarbingerAssignmentLedgerPacket> TYPE =
			new Type<>(Hemomancy.rloc("open_harbinger_assignment_ledger"));
	public static final StreamCodec<FriendlyByteBuf, OpenHarbingerAssignmentLedgerPacket> STREAM_CODEC =
			StreamCodec.of(OpenHarbingerAssignmentLedgerPacket::encode, OpenHarbingerAssignmentLedgerPacket::decode);

	public static void encode(FriendlyByteBuf buf, OpenHarbingerAssignmentLedgerPacket msg) {
		buf.writeVarInt(msg.degree);
		buf.writeBoolean(msg.firstAwakening);
		buf.writeBoolean(msg.degreeOne);
		buf.writeBoolean(msg.firstRemnant);
		buf.writeBoolean(msg.ledgerGranted);
		buf.writeVarInt(msg.redTaxonomyCount);
		buf.writeBoolean(msg.redTaxonomyComplete);
		buf.writeBoolean(msg.hasBlankHematicMemory);
		buf.writeBoolean(msg.mnemonistWovenVesselComplete);
	}

	public static OpenHarbingerAssignmentLedgerPacket decode(FriendlyByteBuf buf) {
		return new OpenHarbingerAssignmentLedgerPacket(
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean());
	}

	public static void handle(final OpenHarbingerAssignmentLedgerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> HarbingerAssignmentLedgerScreen.open(
				msg.degree, msg.firstAwakening, msg.degreeOne, msg.firstRemnant, msg.ledgerGranted,
				msg.redTaxonomyCount, msg.redTaxonomyComplete, msg.hasBlankHematicMemory,
				msg.mnemonistWovenVesselComplete));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
