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
		boolean vesselFilled,
		boolean liberSanguinumCrafted,
		boolean hematicIronBlockCrafted,
		boolean firstRemnant,
		boolean ledgerGranted,
		boolean hasVialCentrifuge,
		boolean hasSampledBloodVial,
		boolean firstSeparationStarted,
		boolean hasAnyEnzyme,
		int redTaxonomyCount,
		boolean redTaxonomyComplete,
		int enzymeMasteryCount,
		boolean enzymeMasteryComplete,
		int livingBestiaryCount,
		int livingBestiaryTotal,
		int morphlingLayerCount,
		boolean hasBlankHematicMemory,
		boolean mnemonistWovenVesselComplete,
		boolean mnemonistFirstWeaveComplete,
		boolean vicarMasonsRespiteDirective,
		boolean veinMasonFirstLesson,
		boolean veinMasonFirstScarCarved,
		boolean veinMasonFirstScarLearned,
		boolean veinMasonFirstEffigyPattern,
		boolean veinMasonFirstEffigyLoadout) implements CustomPacketPayload {
	public static final Type<OpenHarbingerAssignmentLedgerPacket> TYPE =
			new Type<>(Hemomancy.rloc("open_harbinger_assignment_ledger"));
	public static final StreamCodec<FriendlyByteBuf, OpenHarbingerAssignmentLedgerPacket> STREAM_CODEC =
			StreamCodec.of(OpenHarbingerAssignmentLedgerPacket::encode, OpenHarbingerAssignmentLedgerPacket::decode);

	public static void encode(FriendlyByteBuf buf, OpenHarbingerAssignmentLedgerPacket msg) {
		buf.writeVarInt(msg.degree);
		buf.writeBoolean(msg.firstAwakening);
		buf.writeBoolean(msg.degreeOne);
		buf.writeBoolean(msg.vesselFilled);
		buf.writeBoolean(msg.liberSanguinumCrafted);
		buf.writeBoolean(msg.hematicIronBlockCrafted);
		buf.writeBoolean(msg.firstRemnant);
		buf.writeBoolean(msg.ledgerGranted);
		buf.writeBoolean(msg.hasVialCentrifuge);
		buf.writeBoolean(msg.hasSampledBloodVial);
		buf.writeBoolean(msg.firstSeparationStarted);
		buf.writeBoolean(msg.hasAnyEnzyme);
		buf.writeVarInt(msg.redTaxonomyCount);
		buf.writeBoolean(msg.redTaxonomyComplete);
		buf.writeVarInt(msg.enzymeMasteryCount);
		buf.writeBoolean(msg.enzymeMasteryComplete);
		buf.writeVarInt(msg.livingBestiaryCount);
		buf.writeVarInt(msg.livingBestiaryTotal);
		buf.writeVarInt(msg.morphlingLayerCount);
		buf.writeBoolean(msg.hasBlankHematicMemory);
		buf.writeBoolean(msg.mnemonistWovenVesselComplete);
		buf.writeBoolean(msg.mnemonistFirstWeaveComplete);
		buf.writeBoolean(msg.vicarMasonsRespiteDirective);
		buf.writeBoolean(msg.veinMasonFirstLesson);
		buf.writeBoolean(msg.veinMasonFirstScarCarved);
		buf.writeBoolean(msg.veinMasonFirstScarLearned);
		buf.writeBoolean(msg.veinMasonFirstEffigyPattern);
		buf.writeBoolean(msg.veinMasonFirstEffigyLoadout);
	}

	public static OpenHarbingerAssignmentLedgerPacket decode(FriendlyByteBuf buf) {
		return new OpenHarbingerAssignmentLedgerPacket(
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean());
	}

	public static void handle(final OpenHarbingerAssignmentLedgerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> HarbingerAssignmentLedgerScreen.open(
				msg.degree, msg.firstAwakening, msg.degreeOne,
				msg.vesselFilled, msg.liberSanguinumCrafted, msg.hematicIronBlockCrafted,
				msg.firstRemnant, msg.ledgerGranted,
				msg.hasVialCentrifuge, msg.hasSampledBloodVial,
				msg.firstSeparationStarted, msg.hasAnyEnzyme,
				msg.redTaxonomyCount, msg.redTaxonomyComplete,
				msg.enzymeMasteryCount, msg.enzymeMasteryComplete,
				msg.livingBestiaryCount, msg.livingBestiaryTotal, msg.morphlingLayerCount,
				msg.hasBlankHematicMemory,
				msg.mnemonistWovenVesselComplete, msg.mnemonistFirstWeaveComplete, msg.vicarMasonsRespiteDirective,
				msg.veinMasonFirstLesson, msg.veinMasonFirstScarCarved,
				msg.veinMasonFirstScarLearned, msg.veinMasonFirstEffigyPattern,
				msg.veinMasonFirstEffigyLoadout));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
