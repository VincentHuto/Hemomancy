package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.alchemist.SpecimenJarLesson;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public final class SpecimenJarLessonDialogue {
	public static final String TEACH = "alchemist_specimen_jar_lesson";

	private SpecimenJarLessonDialogue() {}

	public static DialogueTree append(DialogueTree tree, ServerPlayer player) {
		if (!SpecimenJarLesson.canTeach(player)) return tree;
		DialogueNode intro = tree.getNode("living_bestiary_intro");
		if (intro == null) return tree;
		var options = new ArrayList<>(intro.options());
		options.add(0, new DialogueOption("hemomancy.dialogue.alchemist.option.specimen_jar_lesson", null, TEACH));
		var nodes = new LinkedHashMap<>(tree.nodes());
		nodes.put(intro.id(), new DialogueNode(intro.id(), intro.lines(), options));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}
}
