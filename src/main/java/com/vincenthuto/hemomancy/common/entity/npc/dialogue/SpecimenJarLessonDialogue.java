package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.alchemist.SpecimenJarLesson;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public final class SpecimenJarLessonDialogue {
	public static final String TEACH = "alchemist_specimen_jar_lesson";
	private static final String INTRO = "living_bestiary_intro";

	private SpecimenJarLessonDialogue() {}

	/**
	 * Hangs the lesson on the Living Bestiary's introduction. A later tree whose greeting does not lead there gets the
	 * Degree 2 introduction and its card, so a player who reached Degree 3 untaught is still offered the lesson (PT-33).
	 */
	public static DialogueTree append(DialogueTree tree, ServerPlayer player) {
		DialogueNode root = tree.getStartNode();
		if (!SpecimenJarLesson.canTeach(player) || root == null) return tree;
		var nodes = new LinkedHashMap<>(tree.nodes());
		DialogueNode intro = tree.getNode(INTRO);
		if (intro == null) {
			DialogueTree votary = HarbingerAlchemistDialogueTrees.votary(tree.entityId(), null);
			intro = votary.getNode(INTRO);
			for (DialogueOption option : intro.options()) {
				if (option.nextNodeId() != null) nodes.putIfAbsent(option.nextNodeId(), votary.getNode(option.nextNodeId()));
			}
		}
		if (root.options().stream().noneMatch(option -> INTRO.equals(option.nextNodeId()))) {
			var rootOptions = new ArrayList<>(root.options());
			rootOptions.add(Math.max(0, rootOptions.size() - 1),
					new DialogueOption("hemomancy.dialogue.alchemist.option.begin_living_bestiary", INTRO, null));
			nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), rootOptions));
		}
		var options = new ArrayList<>(intro.options());
		options.add(0, new DialogueOption("hemomancy.dialogue.alchemist.option.specimen_jar_lesson", null, TEACH));
		nodes.put(intro.id(), new DialogueNode(intro.id(), intro.lines(), options));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}
}
