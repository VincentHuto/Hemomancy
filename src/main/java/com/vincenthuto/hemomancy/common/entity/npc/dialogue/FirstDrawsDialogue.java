package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.alchemist.FirstDrawsAssignment;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class FirstDrawsDialogue {
	public static final String ACCEPT = "alchemist_first_draws_accept";

	private FirstDrawsDialogue() {}

	public static DialogueTree append(DialogueTree tree, ServerPlayer player) {
		if (!FirstDrawsAssignment.eligible(player)) return tree;
		boolean offer = FirstDrawsAssignment.canBrief(player);
		if (!offer && FirstDrawsAssignment.progress(player).complete()) return tree;
		var nodes = new LinkedHashMap<>(tree.nodes());
		String nodeId = offer ? "first_draws_offer" : "first_draws_progress";
		nodes.put(nodeId, new DialogueNode(nodeId,
				List.of(offer ? "hemomancy.alchemist.first_draws.offer" : "hemomancy.alchemist.first_draws.progress"),
				offer ? List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.accept_first_draws", null, ACCEPT),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))
						: List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))));
		DialogueNode start = tree.getStartNode();
		var options = new ArrayList<>(start.options());
		options.add(Math.max(0, options.size() - 1), new DialogueOption(
				"hemomancy.dialogue.alchemist.option.first_draws_assignment", nodeId, null));
		nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}
}
