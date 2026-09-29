package com.vincenthuto.hemomancy.common.network.mission;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.item.HarbingerAssignmentLedgerScreen;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationLedgerProgress;
import com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingProgress;
import com.vincenthuto.hemomancy.common.mission.alchemist.DeepDarkCommissionProgress;
import com.vincenthuto.hemomancy.common.mission.alchemist.PhlegethonticCommissionProgress;
import com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurveyProgress;
import com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiryProgress;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftLedgerProgress;
import com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroductionProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenHarbingerAssignmentLedgerPacket(
		int degree,
		boolean firstAwakening,
		boolean degreeOne,
		FirstBloodcraftLedgerProgress firstBloodcraft,
		boolean firstRemnant,
		boolean ledgerGranted,
		boolean firstDrawsBriefed,
		int firstDrawsSamples,
		int firstDrawsSpecies,
		OverworldFungalSurveyProgress fungalSurvey,
		VoyagerIntroductionProgress voyagerIntroduction,
		boolean circusDiscovered,
		FirstSeparationLedgerProgress firstSeparation,
		boolean bodyAnswersBriefed,
		boolean bodyAnswersComplete,
		int muscleMemoryCount,
		int redTaxonomyCount,
		boolean redTaxonomyComplete,
		int enzymeMasteryCount,
		boolean enzymeMasteryComplete,
		boolean firstCultureComplete,
		MorphlingHandlingProgress morphlingHandling,
		DeepDarkCommissionProgress deepDarkCommission,
		PhlegethonticCommissionProgress phlegethonticCommission,
		VagrantMindInquiryProgress vagrantMindInquiry,
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
		writeFirstBloodcraft(buf, msg.firstBloodcraft);
		buf.writeBoolean(msg.firstRemnant);
		buf.writeBoolean(msg.ledgerGranted);
		buf.writeBoolean(msg.firstDrawsBriefed);
		buf.writeVarInt(msg.firstDrawsSamples);
		buf.writeVarInt(msg.firstDrawsSpecies);
		buf.writeBoolean(msg.fungalSurvey.visited());
		buf.writeVarInt(msg.fungalSurvey.specimens());
		buf.writeBoolean(msg.fungalSurvey.reported());
		buf.writeBoolean(msg.voyagerIntroduction.observed());
		buf.writeBoolean(msg.voyagerIntroduction.reported());
		buf.writeBoolean(msg.circusDiscovered);
		writeFirstSeparation(buf, msg.firstSeparation);
		buf.writeBoolean(msg.bodyAnswersBriefed);
		buf.writeBoolean(msg.bodyAnswersComplete);
		buf.writeVarInt(msg.muscleMemoryCount);
		buf.writeVarInt(msg.redTaxonomyCount);
		buf.writeBoolean(msg.redTaxonomyComplete);
		buf.writeVarInt(msg.enzymeMasteryCount);
		buf.writeBoolean(msg.enzymeMasteryComplete);
		buf.writeBoolean(msg.firstCultureComplete);
		buf.writeBoolean(msg.morphlingHandling.proof());
		buf.writeBoolean(msg.morphlingHandling.inspected());
		buf.writeBoolean(msg.deepDarkCommission.sampleProof());
		buf.writeBoolean(msg.deepDarkCommission.reported());
		buf.writeBoolean(msg.phlegethonticCommission.sampleProof());
		buf.writeVarInt(msg.phlegethonticCommission.scyphusCount());
		buf.writeBoolean(msg.phlegethonticCommission.reported());
		buf.writeBoolean(msg.vagrantMindInquiry.mindVisited());
		buf.writeBoolean(msg.vagrantMindInquiry.biologyObserved());
		buf.writeBoolean(msg.vagrantMindInquiry.memoryReported());
		buf.writeBoolean(msg.vagrantMindInquiry.biologyReported());
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
				readFirstBloodcraft(buf),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readVarInt(),
				new OverworldFungalSurveyProgress(buf.readBoolean(), buf.readVarInt(), buf.readBoolean()),
				new VoyagerIntroductionProgress(buf.readBoolean(), buf.readBoolean()),
				buf.readBoolean(),
				readFirstSeparation(buf),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readBoolean(),
				new MorphlingHandlingProgress(buf.readBoolean(), buf.readBoolean()),
				new DeepDarkCommissionProgress(buf.readBoolean(), buf.readBoolean()),
				new PhlegethonticCommissionProgress(buf.readBoolean(), buf.readVarInt(), buf.readBoolean()),
				new VagrantMindInquiryProgress(buf.readBoolean(), buf.readBoolean(),
						buf.readBoolean(), buf.readBoolean()),
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
				msg.firstBloodcraft,
				msg.firstRemnant, msg.ledgerGranted,
				msg.firstDrawsBriefed, msg.firstDrawsSamples, msg.firstDrawsSpecies,
				msg.fungalSurvey,
				msg.voyagerIntroduction,
				msg.circusDiscovered,
				msg.firstSeparation,
				msg.bodyAnswersBriefed, msg.bodyAnswersComplete, msg.muscleMemoryCount,
				msg.redTaxonomyCount, msg.redTaxonomyComplete,
				msg.enzymeMasteryCount, msg.enzymeMasteryComplete,
				msg.firstCultureComplete,
				msg.morphlingHandling,
				msg.deepDarkCommission,
				msg.phlegethonticCommission,
				msg.vagrantMindInquiry,
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

	private static void writeFirstSeparation(FriendlyByteBuf buf, FirstSeparationLedgerProgress progress) {
		buf.writeBoolean(progress.briefed());
		buf.writeBoolean(progress.centrifugeAcquired());
		buf.writeBoolean(progress.sampleAcquired());
		buf.writeBoolean(progress.separationStarted());
		buf.writeBoolean(progress.enzymeRecovered());
		buf.writeBoolean(progress.rewardClaimed());
		buf.writeBoolean(progress.distillationRecovered());
		buf.writeBoolean(progress.concentratedBloodPending());
		buf.writeBoolean(progress.initiateReached());
	}

	private static void writeFirstBloodcraft(FriendlyByteBuf buf, FirstBloodcraftLedgerProgress progress) {
		buf.writeDouble(progress.absorbedMl());
		buf.writeBoolean(progress.formationProjected());
		buf.writeBoolean(progress.venousStoneProjected());
		buf.writeBoolean(progress.structureCrafted());
		buf.writeBoolean(progress.votaryReached());
	}

	private static FirstBloodcraftLedgerProgress readFirstBloodcraft(FriendlyByteBuf buf) {
		return new FirstBloodcraftLedgerProgress(buf.readDouble(), buf.readBoolean(), buf.readBoolean(),
				buf.readBoolean(), buf.readBoolean());
	}

	private static FirstSeparationLedgerProgress readFirstSeparation(FriendlyByteBuf buf) {
		return new FirstSeparationLedgerProgress(
				buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
				buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
