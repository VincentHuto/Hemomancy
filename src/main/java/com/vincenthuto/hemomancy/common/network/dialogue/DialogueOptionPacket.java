package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
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
			Player player = ctx.player();
			if (player instanceof ServerPlayer sender && msg.eventId != null && !msg.eventId.isEmpty()) {
				var speaking = sender.level().getEntity(msg.entityId);
                if (speaking instanceof com.vincenthuto.hemomancy.common.succession.ProfessionalHarbingerEntity npc
                        && (npc.isMisbegotten() || npc.isSuccessor()
                        && !com.vincenthuto.hemomancy.common.succession.SuccessionResidents.mayServe(sender, npc))) return;
                if (MemoHelper.isMemoEvent(msg.eventId)) {
					MemoHelper.handleMemoEvent(sender, msg.eventId);
					return;
				}
				if (LiberKnowledgeHelper.handleDialogueEvent(sender, msg.eventId)) {
					return;
				}
				dispatch(sender, msg.eventId, msg.entityId);
			}
		});
	}

	public static DialogueEvent dispatch(ServerPlayer sender, String eventId, int entityId) {
		var entity = sender.level().getEntity(entityId);
		if (SanguineMonolithDialogueTrees.EVENT_CORNERSTONE.equals(eventId)
				|| SanguineMonolithDialogueTrees.EVENT_SHATTER.equals(eventId)) {
			if (entityId != SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID
					|| !MonolithDialogueContext.permits(sender, eventId)) return null;
		} else {
			if (entityId != 0 && (entity == null || sender.distanceTo(entity) > 8.0)) return null;
			if ((HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD.equals(eventId)
					|| HarbingerVicarDialogueTrees.EVENT_CONSECRATION_KIT.equals(eventId))
					&& !(entity instanceof HarbingerVicarEntity)) return null;
			if ((MnemonistStarterMemoryChoice.fromEventId(eventId).isPresent()
					|| HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN.equals(eventId))
					&& !(entity instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity)) return null;
			if ((HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON.equals(eventId)
					|| HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD.equals(eventId))
					&& !(entity instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity)) return null;
			if ((HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM.equals(eventId)
					|| com.vincenthuto.hemomancy.common.entity.npc.dialogue.AdvancedBrewingDialogue.CONDENSER_CLAIM.equals(eventId)
					|| com.vincenthuto.hemomancy.common.entity.npc.dialogue.AdvancedBrewingDialogue.ATHANOR_CLAIM.equals(eventId))
					&& !(entity instanceof HarbingerAlchemistEntity)) return null;
		}
        if (entity instanceof com.vincenthuto.hemomancy.common.succession.ProfessionalHarbingerEntity npc && (npc.isSuccessor() || npc.isMisbegotten())) {
            if (npc.isMisbegotten() || !com.vincenthuto.hemomancy.common.succession.SuccessionResidents.mayServe(sender, npc)
                    || "recruit_harbinger".equals(eventId) || "expel_harbinger".equals(eventId)
                    || eventId.startsWith("succession_")) return null;
        }
        if (com.vincenthuto.hemomancy.common.succession.SuccessionDialogue.handle(sender, entity, eventId)) return null;
		DialogueEvent event = new DialogueEvent(sender, eventId, entityId);
		NeoForge.EVENT_BUS.post(event);
		return event;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
