package com.vincenthuto.hemomancy.client.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen;
import com.vincenthuto.hemomancy.client.screen.discovery.DiscoveryInscriptionScreen;
import com.vincenthuto.hemomancy.client.screen.item.BloodlinePoolScreen;
import com.vincenthuto.hemomancy.client.screen.item.BookOfObservancesScreen;
import com.vincenthuto.hemomancy.client.screen.item.HarbingerAssignmentLedgerScreen;
import com.vincenthuto.hemomancy.client.screen.item.SuccessionResidentsScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.SSCScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.VisceralMirrorScreen;
import com.vincenthuto.hemomancy.client.data.EarthenVeinTravelClientState;
import com.vincenthuto.hemomancy.common.network.ClientPayloadHandlers;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.OpenArborSkillsPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.OpenSSCScreenPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketOpenBloodlinePoolScreen;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.OpenVisceralMirrorPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.VisceralMirrorUpdatePacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueRewardResultPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenPendingWhisperPacket;
import com.vincenthuto.hemomancy.common.network.discovery.OpenInscriptionPacket;
import com.vincenthuto.hemomancy.common.network.mission.OpenBookOfObservancesPacket;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hemomancy.common.network.vein.OpenEarthenVeinDisplayPacket;
import com.vincenthuto.hemomancy.common.network.succession.ResidentsSnapshotPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client halves of client-bound payloads, registered with {@link ClientPayloadHandlers}. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientboundScreenHandlers {

	private ClientboundScreenHandlers() {
	}

	@SubscribeEvent
	public static void register(FMLClientSetupEvent event) {
		ClientPayloadHandlers.register(DialogueRewardResultPacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(OpenArborSkillsPacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(OpenBookOfObservancesPacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(OpenDialoguePacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(OpenEarthenVeinDisplayPacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(OpenHarbingerAssignmentLedgerPacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(OpenInscriptionPacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(OpenPendingWhisperPacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(OpenSSCScreenPacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(OpenVisceralMirrorPacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(PacketOpenBloodlinePoolScreen.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(ResidentsSnapshotPacket.TYPE, ClientboundScreenHandlers::handle);
		ClientPayloadHandlers.register(VisceralMirrorUpdatePacket.TYPE, ClientboundScreenHandlers::handle);
	}

	static void handle(DialogueRewardResultPacket msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> DialogueScreen
				.receiveRewardResult(msg.requestId(), msg.delivered()));
	}

	static void handle(OpenArborSkillsPacket msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> HarbingerProgressScreen.openScreen(msg.skillId()));
	}

	static void handle(OpenBookOfObservancesPacket msg, IPayloadContext context) {
		context.enqueueWork(() -> BookOfObservancesScreen.open(msg.acceptedMask(), msg.claimedMask(),
				msg.availableMask(), msg.readyMask(), msg.purity(), msg.clarity(), msg.clarityUnlocked()));
	}

	static void handle(final OpenDialoguePacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() != null) {
				DialogueScreen.open(msg.tree());
			}
		});
	}

	static void handle(OpenEarthenVeinDisplayPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> EarthenVeinTravelClientState.open(packet));
	}

	static void handle(final OpenHarbingerAssignmentLedgerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> HarbingerAssignmentLedgerScreen.open(
				msg.degree(), msg.firstAwakening(), msg.degreeOne(),
				msg.firstBloodcraft(),
				msg.firstRemnant(), msg.ledgerGranted(),
				msg.firstDrawsBriefed(), msg.firstDrawsSamples(), msg.firstDrawsSpecies(),
				msg.fungalSurvey(),
				msg.voyagerIntroduction(),
				msg.circusDiscovered(),
				msg.firstSeparation(),
				msg.bodyAnswersBriefed(), msg.bodyAnswersComplete(), msg.muscleMemoryCount(),
				msg.redTaxonomyCount(), msg.redTaxonomyComplete(),
				msg.enzymeMasteryCount(), msg.enzymeMasteryComplete(),
				msg.firstCultureComplete(),
				msg.morphlingHandling(),
				msg.deepDarkCommission(),
				msg.phlegethonticCommission(),
				msg.vagrantMindInquiry(),
				msg.livingBestiaryCount(), msg.livingBestiaryTotal(), msg.morphlingLayerCount(),
				msg.hasBlankHematicMemory(),
				msg.mnemonistWovenVesselComplete(), msg.mnemonistFirstWeaveComplete(), msg.vicarMasonsRespiteDirective(),
				msg.veinMasonFirstLesson(), msg.veinMasonFirstScarCarved(),
				msg.veinMasonFirstScarLearned(), msg.veinMasonFirstEffigyPattern(),
				msg.veinMasonFirstEffigyLoadout(),
				msg.anchoriteD5Progress(), msg.anchoriteD6Progress(),
				msg.artificerArmaturePlaced(), msg.artificerFirstHematicUpgrade(),
				msg.artificerHematicIronFitting(), msg.artificerFirstForkUpgrade(), msg.artificerForkFitting(),
				msg.artificerFrameConsecrated(), msg.artificerFirstBloodLustUpgrade(),
				msg.artificerBloodLustFitting(), msg.artificerMonolithicFrame(),
				msg.artificerFirstD7Upgrade(), msg.artificerD7Fitting(), msg.artificerFirstLivingGraft(),
				msg.artificerLivingWeaponFormCount(), msg.artificerLivingArsenalFitting(),
				msg.artificerProgressSteps(),
				msg.foundedBloodline(), msg.foundingFaneEstablished(), msg.chamberReturned(),
				msg.covenantThroneBound(), msg.covenantVigilCompleted(), msg.livingCovenantComplete(),
				msg.pomesConsumed(), msg.qliphothCommunionComplete(), msg.archonPath(), msg.severedPortalOpen()));
	}

	static void handle(final OpenInscriptionPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> DiscoveryInscriptionScreen.open(msg.title(), msg.lines(), msg.revealed(), msg.riteId(),
				msg.requiresAbocipherLiteracy()));
	}

	static void handle(OpenPendingWhisperPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> DialogueScreen
				.openWhisper(packet.tree(), packet.id()));
	}

	static void handle(final OpenSSCScreenPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> SSCScreen.open(msg.pos(), msg.sscBlood(), msg.sscMaxBlood(), msg.drudges()));
	}

	static void handle(final OpenVisceralMirrorPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() != null) {
				VisceralMirrorScreen.open(
						msg.pos(), msg.organLevels(), msg.hasEcho(),
						msg.bloodVolume(), msg.maxBloodVolume(), msg.degree());
			}
		});
	}

	static void handle(final PacketOpenBloodlinePoolScreen msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() != null) {
				BloodlinePoolScreen.openScreen();
			}
		});
	}

	static void handle(ResidentsSnapshotPacket msg, IPayloadContext context) {
		context.enqueueWork(() -> SuccessionResidentsScreen.update(msg.data()));
	}

	static void handle(final VisceralMirrorUpdatePacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() != null) {
				VisceralMirrorScreen.applyServerUpdate(msg);
			}
		});
	}
}
