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
		boolean bodyAnswersBriefed,
		boolean bodyAnswersComplete,
		int muscleMemoryCount,
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
		boolean veinMasonFirstEffigyLoadout,
		int anchoriteD5Progress,
		int anchoriteD6Progress,
		boolean artificerArmaturePlaced,
		boolean artificerFirstHematicUpgrade,
		boolean artificerHematicIronFitting,
		boolean artificerFirstForkUpgrade,
		boolean artificerForkFitting,
		boolean artificerFrameConsecrated,
		boolean artificerFirstBloodLustUpgrade,
		boolean artificerBloodLustFitting,
		boolean artificerMonolithicFrame,
		boolean artificerFirstD7Upgrade,
		boolean artificerD7Fitting,
		boolean artificerFirstLivingGraft,
		int artificerLivingWeaponFormCount,
		boolean artificerLivingArsenalFitting,
		int artificerProgressSteps,
		boolean foundedBloodline,
		boolean foundingFaneEstablished,
		boolean chamberReturned,
		boolean covenantThroneBound,
		boolean covenantVigilCompleted,
		boolean livingCovenantComplete,
		int pomesConsumed,
		boolean qliphothCommunionComplete,
		boolean silentPending,
		boolean severedPortalOpen,
		boolean silentArchon) implements CustomPacketPayload {
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
		buf.writeBoolean(msg.bodyAnswersBriefed);
		buf.writeBoolean(msg.bodyAnswersComplete);
		buf.writeVarInt(msg.muscleMemoryCount);
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
		buf.writeVarInt(msg.anchoriteD5Progress);
		buf.writeVarInt(msg.anchoriteD6Progress);
		buf.writeBoolean(msg.artificerArmaturePlaced);
		buf.writeBoolean(msg.artificerFirstHematicUpgrade);
		buf.writeBoolean(msg.artificerHematicIronFitting);
		buf.writeBoolean(msg.artificerFirstForkUpgrade);
		buf.writeBoolean(msg.artificerForkFitting);
		buf.writeBoolean(msg.artificerFrameConsecrated);
		buf.writeBoolean(msg.artificerFirstBloodLustUpgrade);
		buf.writeBoolean(msg.artificerBloodLustFitting);
		buf.writeBoolean(msg.artificerMonolithicFrame);
		buf.writeBoolean(msg.artificerFirstD7Upgrade);
		buf.writeBoolean(msg.artificerD7Fitting);
		buf.writeBoolean(msg.artificerFirstLivingGraft);
		buf.writeVarInt(msg.artificerLivingWeaponFormCount);
		buf.writeBoolean(msg.artificerLivingArsenalFitting);
		buf.writeVarInt(msg.artificerProgressSteps);
		buf.writeBoolean(msg.foundedBloodline);
		buf.writeBoolean(msg.foundingFaneEstablished);
		buf.writeBoolean(msg.chamberReturned);
		buf.writeBoolean(msg.covenantThroneBound);
		buf.writeBoolean(msg.covenantVigilCompleted);
		buf.writeBoolean(msg.livingCovenantComplete);
		buf.writeVarInt(msg.pomesConsumed);
		buf.writeBoolean(msg.qliphothCommunionComplete);
		buf.writeBoolean(msg.silentPending);
		buf.writeBoolean(msg.severedPortalOpen);
		buf.writeBoolean(msg.silentArchon);
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
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readVarInt(),
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
				buf.readBoolean(),
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
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readVarInt(),
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
				msg.bodyAnswersBriefed, msg.bodyAnswersComplete, msg.muscleMemoryCount,
				msg.redTaxonomyCount, msg.redTaxonomyComplete,
				msg.enzymeMasteryCount, msg.enzymeMasteryComplete,
				msg.livingBestiaryCount, msg.livingBestiaryTotal, msg.morphlingLayerCount,
				msg.hasBlankHematicMemory,
				msg.mnemonistWovenVesselComplete, msg.mnemonistFirstWeaveComplete, msg.vicarMasonsRespiteDirective,
				msg.veinMasonFirstLesson, msg.veinMasonFirstScarCarved,
				msg.veinMasonFirstScarLearned, msg.veinMasonFirstEffigyPattern,
				msg.veinMasonFirstEffigyLoadout,
				msg.anchoriteD5Progress, msg.anchoriteD6Progress,
				msg.artificerArmaturePlaced, msg.artificerFirstHematicUpgrade,
				msg.artificerHematicIronFitting, msg.artificerFirstForkUpgrade, msg.artificerForkFitting,
				msg.artificerFrameConsecrated, msg.artificerFirstBloodLustUpgrade,
				msg.artificerBloodLustFitting, msg.artificerMonolithicFrame,
				msg.artificerFirstD7Upgrade, msg.artificerD7Fitting, msg.artificerFirstLivingGraft,
				msg.artificerLivingWeaponFormCount, msg.artificerLivingArsenalFitting,
				msg.artificerProgressSteps,
				msg.foundedBloodline, msg.foundingFaneEstablished, msg.chamberReturned,
				msg.covenantThroneBound, msg.covenantVigilCompleted, msg.livingCovenantComplete,
				msg.pomesConsumed, msg.qliphothCommunionComplete, msg.silentPending,
				msg.severedPortalOpen, msg.silentArchon));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
