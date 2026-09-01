package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Adds stage-appropriate Observance work to an Unstained NPC's normal dialogue. */
public final class UnstainedObservanceDialogueDecorator {
	private UnstainedObservanceDialogueDecorator() {
	}

	public static DialogueTree decorate(DialogueTree tree, ServerPlayer player,
			UnstainedObservances.Issuer issuer) {
		var progress = HemoCapabilityAccess.getUnstainedProgress(player).orElse(null);
		if (progress == null || !progress.hasBegunPurification()
				&& ((progress.getAcceptedObservances() | progress.getClaimedObservances())
						& UnstainedAccessRules.NOVITIATE_MASK) == 0) {
			return tree;
		}

		List<UnstainedObservances.Observance> visible = new ArrayList<>();
		for (UnstainedObservances.Observance observance : UnstainedObservances.Observance.values()) {
			if (observance.issuer() == issuer
					&& UnstainedObservances.isAvailable(progress, observance)
					&& (progress.getClaimedObservances() & observance.mask()) == 0) {
				visible.add(observance);
			}
		}
		boolean daggerReplacement = issuer == UnstainedObservances.Issuer.GUARDIAN && progress.hasClarityUnlocked();
		if (visible.isEmpty() && !daggerReplacement) {
			return tree;
		}
		DialogueNode start = tree.getStartNode();
		List<DialogueOption> startOptions = new ArrayList<>();
		for (DialogueOption option : start.options()) {
			if (UnstainedObservances.Observance.fromEventId(option.eventId()) == null) startOptions.add(option);
		}
		int insertIndex = startOptions.size() > 0 ? startOptions.size() - 1 : 0;
		for (UnstainedObservances.Observance observance : visible) {
			boolean accepted = (progress.getAcceptedObservances() & observance.mask()) != 0;
			DialogueAttention attention = observanceAttention(accepted,
					accepted && UnstainedObservances.isReady(player, observance));
			startOptions.add(insertIndex++, new DialogueOption(optionKey(observance), null, observance.eventId(),
					attention == DialogueAttention.NONE ? DialogueOptionPresentation.normal()
							: DialogueOptionPresentation.attention(attention)));
		}
		if (daggerReplacement) {
			startOptions.add(insertIndex, new DialogueOption(
					"hemomancy.dialogue.guardian.option.replace_absolution_dagger", null,
					"guardian_replace_absolution_dagger"));
		}

		var nodes = new LinkedHashMap<>(tree.nodes());
		nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), List.copyOf(startOptions)));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}

	static DialogueAttention observanceAttention(boolean accepted, boolean ready) {
		return ready ? DialogueAttention.URGENT : accepted ? DialogueAttention.NONE : DialogueAttention.NOTICE;
	}

	private static String optionKey(UnstainedObservances.Observance observance) {
		return switch (observance) {
			case GATHER_GHOST_PIPE -> "hemomancy.dialogue.acolyte.option.task_gather";
			case WEAVE_WREATH -> "hemomancy.dialogue.acolyte.option.task_wreath";
			case PREPARE_HEMOLYTIC -> "hemomancy.dialogue.acolyte.option.task_hemolytic";
			case CONSECRATE_COPPER -> "hemomancy.dialogue.acolyte.option.task_consecrate";
			case OFFER_CHALICE -> "hemomancy.dialogue.acolyte.option.task_chalice";
			case CONDENSE_STILL_WATERS -> "hemomancy.dialogue.zealot.option.task_still_waters";
			case BEAR_PALLID_ICON -> "hemomancy.dialogue.zealot.option.task_pallid_icon";
			case PLATE_THE_WARD -> "hemomancy.dialogue.guardian.option.task_plating";
			case RING_THE_PALE_WATCH -> "hemomancy.dialogue.guardian.option.task_bell";
			case NOVITIATE_GATHER_REMEDIES -> "hemomancy.dialogue.acolyte.option.vow_gather_remedies";
			case NOVITIATE_GENTLE_SEPARATION -> "hemomancy.dialogue.acolyte.option.vow_gentle_separation";
			case NOVITIATE_STILLWATER_LABOR -> "hemomancy.dialogue.zealot.option.vow_stillwater_labor";
			case NOVITIATE_CLEAN_LABOR -> "hemomancy.dialogue.zealot.option.vow_clean_labor";
			case NOVITIATE_SHELTER_AFFLICTED -> "hemomancy.dialogue.guardian.option.vow_shelter_afflicted";
		};
	}
}
