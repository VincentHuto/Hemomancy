package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * NQ-10-6: while an assigned First Separation spin waits for its output, the Alchemist offers to strike it, so a lost
 * output never locks the second Degree (PT-18).
 */
public final class FirstSeparationSpinDialogue {
	public static final String CLEAR = "alchemist_clear_assigned_spin";
	private static final String NODE = "assigned_spin_cleared";

	private FirstSeparationSpinDialogue() {}

	public static DialogueTree append(DialogueTree tree, ServerPlayer player) {
		DialogueNode root = tree.getStartNode();
		if (root == null || !FirstSeparationAssignment.hasPendingAssignmentSpin(player)) return tree;
		var nodes = new LinkedHashMap<>(tree.nodes());
		nodes.put(NODE, new DialogueNode(NODE, List.of("hemomancy.alchemist.first_separation.spin_cleared"),
				List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))));
		var options = new ArrayList<>(root.options());
		options.add(Math.max(0, options.size() - 1),
				new DialogueOption("hemomancy.dialogue.alchemist.option.clear_assigned_spin", NODE, CLEAR));
		nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}
}
