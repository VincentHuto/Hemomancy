package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.OfferedDialogues;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server packet sent when a player selects a dialogue option that
 * carries an event id. The server fires a {@link DialogueEvent} so that any
 * listener can react to the player's choice.
 */
public class DialogueOptionPacket implements CustomPacketPayload {

	public static final Type<DialogueOptionPacket> TYPE = new Type<>(Hemomancy.rloc("dialogue_option_packet"));
	public static final StreamCodec<FriendlyByteBuf, DialogueOptionPacket> STREAM_CODEC = StreamCodec.of(DialogueOptionPacket::encode, DialogueOptionPacket::decode);

	private final String eventId;
	private final int entityId;

	public DialogueOptionPacket(String eventId, int entityId) {
		this.eventId = eventId;
		this.entityId = entityId;
	}

	public static void encode(FriendlyByteBuf buf, DialogueOptionPacket msg) {
		buf.writeUtf(msg.eventId);
		buf.writeInt(msg.entityId);
	}

	public static DialogueOptionPacket decode(FriendlyByteBuf buf) {
		return new DialogueOptionPacket(buf.readUtf(), buf.readInt());
	}

	public static void handle(final DialogueOptionPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer sender) accept(sender, msg.eventId, msg.entityId);
		});
	}

	/** The server's whole response to a chosen option; the payload handler and tests call this. */
	public static void accept(ServerPlayer sender, String eventId, int entityId) {
		if (eventId == null || eventId.isEmpty()) return;
		var speaking = sender.level().getEntity(entityId);
		if (speaking instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.ProfessionalHarbingerEntity npc
				&& (npc.isMisbegotten() || npc.isSuccessor()
				&& !com.vincenthuto.hemomancy.common.entity.npc.harbinger.succession.SuccessionResidents.mayServe(sender, npc))) return;
		if (MemoHelper.isMemoEvent(eventId)) {
			// A memo is noted only from the whisper that is open or the speaker in reach who offers it (RT-18).
			if (ServerboundGuards.activePlayer(sender) && offeredNow(sender, eventId, entityId)) {
				MemoHelper.handleMemoEvent(sender, eventId);
			}
			return;
		}
		if (LiberKnowledgeHelper.handleDialogueEvent(sender, eventId)) {
			return;
		}
		dispatch(sender, eventId, entityId);
	}

	/** The tree last sent to the player, or the whisper they have open, lists {@code eventId}; a speaker is in reach. */
	private static boolean offeredNow(ServerPlayer sender, String eventId, int entityId) {
		if (entityId != 0 && ServerboundGuards.nearEntity(sender, entityId, 8.0D) == null) return false;
		return OfferedDialogues.offers(sender, entityId, eventId)
				|| entityId == 0 && PendingWhispers.openWhisperOffers(sender, eventId);
	}

	public static DialogueEvent dispatch(ServerPlayer sender, String eventId, int entityId) {
		// A dead or spectating player cannot take a lesson or a reward; the screen they chose it from is stale.
		if (!ServerboundGuards.activePlayer(sender)) return null;
		// An event with no speaker is taken only when the server offered it: the tree it last sent, or an open whisper
		// (RT-20). Everything else names a speaker, and that speaker must be in reach below.
		if (entityId == 0 && !OfferedDialogues.offers(sender, 0, eventId) && !PendingWhispers.openWhisperOffers(sender, eventId)) {
			return null;
		}
		var entity = sender.level().getEntity(entityId);
		if (HarbingerVicarDialogueTrees.EVENT_CONSECRATION_KIT.equals(eventId)) return null;
		if (SanguineMonolithDialogueTrees.EVENT_CORNERSTONE.equals(eventId)
				|| SanguineMonolithDialogueTrees.EVENT_SHATTER.equals(eventId)) {
			if (entityId != SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID
					|| !MonolithDialogueContext.permits(sender, eventId)) return null;
		} else {
			if (entityId != 0 && ServerboundGuards.nearEntity(sender, entityId, 8.0D) == null) return null;
			var stationClaim = StationUpgradeDialogue.parseClaim(eventId);
			if (stationClaim.isPresent() && !StationUpgradeDialogue.allowedSpeaker(stationClaim.get().station(), entity))
				return null;
			if (!DialogueEventSpeakers.allows(eventId, entity)) return null;
			if ((HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD.equals(eventId)
					|| EarlyInitiationDialogue.BEGIN.equals(eventId)
					|| EarlyInitiationDialogue.RELEASE.equals(eventId)
					|| VoyagerIntroductionDialogue.REPORT.equals(eventId)
					|| VoyagerIntroductionDialogue.BEARING.equals(eventId))
					&& !(entity instanceof HarbingerVicarEntity)) return null;
			if (VoyagerIntroductionDialogue.OBSERVE.equals(eventId)
					&& !(entity instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVoyagerEntity)) return null;
			if (HarbingerArtificerDialogueTrees.EVENT_CONSECRATION_KIT.equals(eventId)
					&& !(entity instanceof HarbingerArtificerEntity)) return null;
			if ((MnemonistStarterMemoryChoice.fromEventId(eventId).isPresent()
					|| com.vincenthuto.hemomancy.common.entity.npc.dialogue.VagrantMindInquiryDialogue.MEMORY_REPORT.equals(eventId)
					|| com.vincenthuto.hemomancy.common.entity.npc.dialogue.VagrantMindInquiryDialogue.BEARING.equals(eventId)
					|| HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN.equals(eventId)
					|| HarbingerMnemonistDialogueTrees.EVENT_SCRIPTORIUM_LESSON.equals(eventId)
					|| HarbingerMnemonistDialogueTrees.EVENT_GUIDED_CHAMBER.equals(eventId))
					&& !(entity instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity)) return null;
			if ((HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON.equals(eventId)
					|| HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD.equals(eventId))
					&& !(entity instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity)) return null;
			if ((HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM.equals(eventId)
					|| com.vincenthuto.hemomancy.common.entity.npc.dialogue.VagrantMindInquiryDialogue.BIOLOGY_REPORT.equals(eventId)
					|| MorphlingHandlingDialogue.INSPECT.equals(eventId)
					|| com.vincenthuto.hemomancy.common.entity.npc.dialogue.PallidVesselDialogue.BRIEF.equals(eventId)
					|| com.vincenthuto.hemomancy.common.entity.npc.dialogue.PallidVesselDialogue.REPORT.equals(eventId)
					|| DeepDarkCommissionDialogue.REPORT.equals(eventId)
					|| DeepDarkCommissionDialogue.BEARING.equals(eventId)
					|| PhlegethonticCommissionDialogue.REPORT.equals(eventId)
					|| PhlegethonticCommissionDialogue.BEARING.equals(eventId)
					|| OverworldFungalSurveyDialogue.REPORT.equals(eventId))
					&& !(entity instanceof HarbingerAlchemistEntity)) return null;
		}
        if (entity instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.ProfessionalHarbingerEntity npc && (npc.isSuccessor() || npc.isMisbegotten())) {
            if (npc.isMisbegotten() || !com.vincenthuto.hemomancy.common.entity.npc.harbinger.succession.SuccessionResidents.mayServe(sender, npc)
                    || "recruit_harbinger".equals(eventId) || "expel_harbinger".equals(eventId)
                    || eventId.startsWith("succession_")) return null;
        }
        if (com.vincenthuto.hemomancy.common.entity.npc.dialogue.SuccessionDialogue.handle(sender, entity, eventId)) return null;
		DialogueEvent event = new DialogueEvent(sender, eventId, entityId);
		NeoForge.EVENT_BUS.post(event);
		return event;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
