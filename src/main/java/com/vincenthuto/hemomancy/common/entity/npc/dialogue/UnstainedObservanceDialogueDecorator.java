package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.mission.UnstainedObservanceHelper;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Adds stage-appropriate Observance work to an Unstained NPC's normal dialogue. */
public final class UnstainedObservanceDialogueDecorator {
	private static final String NODE_ID = "directed_observances";

	private UnstainedObservanceDialogueDecorator() {
	}

	public static DialogueTree decorate(DialogueTree tree, ServerPlayer player,
			UnstainedObservanceHelper.Issuer issuer) {
		var progress = HemoCapabilityAccess.getUnstainedProgress(player).orElse(null);
		if (progress == null || !progress.hasBegunPurification()) {
			return tree;
		}

		List<UnstainedObservanceHelper.Observance> visible = new ArrayList<>();
		for (UnstainedObservanceHelper.Observance observance : UnstainedObservanceHelper.Observance.values()) {
			if (observance.issuer() == issuer
					&& UnstainedObservanceHelper.isAvailable(progress, observance)
					&& (progress.getClaimedObservances() & observance.mask()) == 0) {
				visible.add(observance);
			}
		}
		if (visible.isEmpty()) {
			return tree;
		}

		DialogueNode start = tree.getStartNode();
		List<DialogueOption> startOptions = new ArrayList<>(start.options());
		startOptions.add(startOptions.size() > 0 ? startOptions.size() - 1 : 0,
				new DialogueOption("hemomancy.dialogue.unstained.option.observances", NODE_ID, null));

		List<DialogueOption> taskOptions = new ArrayList<>();
		for (UnstainedObservanceHelper.Observance observance : visible) {
			taskOptions.add(new DialogueOption(optionKey(observance), null, observance.eventId()));
		}
		taskOptions.add(new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null));

		var nodes = new LinkedHashMap<>(tree.nodes());
		nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), List.copyOf(startOptions)));
		nodes.put(NODE_ID, new DialogueNode(NODE_ID,
				List.of("hemomancy.dialogue.unstained.observances.line1"), List.copyOf(taskOptions)));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}

	private static String optionKey(UnstainedObservanceHelper.Observance observance) {
		return switch (observance) {
			case CONDENSE_STILL_WATERS -> "hemomancy.dialogue.zealot.option.task_still_waters";
			case BEAR_PALLID_ICON -> "hemomancy.dialogue.zealot.option.task_pallid_icon";
			case PLATE_THE_WARD -> "hemomancy.dialogue.guardian.option.task_plating";
			case RING_THE_PALE_WATCH -> "hemomancy.dialogue.guardian.option.task_bell";
			default -> throw new IllegalArgumentException("Unsupported non-NPC observance " + observance);
		};
	}
}
