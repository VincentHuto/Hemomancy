package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.alchemist.PallidVesselProgress;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Alchemist's Pallid Vessel quest. The Degree 3 lesson node becomes the briefing, then the progress check; a
 * later-Degree player who never finished it is still offered it. A finished quest leaves only the lesson at Degree 3.
 */
public final class PallidVesselDialogue {
	public static final String NODE = "pallid_vessel";
	public static final String BRIEF = "alchemist_pallid_vessel_brief";
	public static final String REPORT = "alchemist_pallid_vessel_report";
	static final String OPTION = "hemomancy.dialogue.alchemist.option.pallid_vessel";
	static final List<String> BRIEFING = List.of("hemomancy.alchemist.pallid_vessel.offer",
			"hemomancy.alchemist.initiate.pallid_vessel.rite", "hemomancy.alchemist.pallid_vessel.steps");

	private PallidVesselDialogue() {}

	public static DialogueTree withAlchemistQuest(DialogueTree tree, PallidVesselProgress progress) {
		PallidVesselProgress.Stage stage = progress.stage();
		boolean offered = tree.getNode(NODE) != null;
		if (stage == PallidVesselProgress.Stage.COMPLETE && !offered) return tree;
		var nodes = new LinkedHashMap<>(tree.nodes());
		nodes.put(NODE, node(stage));
		if (tree.getNode("gourd_use") == null) nodes.put("gourd_use", gourdUse());
		DialogueNode start = tree.getStartNode();
		var options = new ArrayList<DialogueOption>();
		for (DialogueOption option : start.options()) if (!NODE.equals(option.nextNodeId())) options.add(option);
		options.add(Math.max(0, options.size() - 1), option(stage));
		nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}

	static DialogueOption option(PallidVesselProgress.Stage stage) {
		return switch (stage) {
			case NOT_BRIEFED -> new DialogueOption(OPTION, NODE, null,
					DialogueOptionPresentation.attention(DialogueAttention.NOTICE).withQuest());
			case READY -> new DialogueOption(OPTION, NODE, null,
					DialogueOptionPresentation.attention(DialogueAttention.URGENT).withQuest());
			case COMPLETE -> new DialogueOption(OPTION, NODE, null);
			default -> new DialogueOption(OPTION, NODE, null).withQuest();
		};
	}

	static DialogueNode node(PallidVesselProgress.Stage stage) {
		var options = new ArrayList<DialogueOption>();
		if (stage == PallidVesselProgress.Stage.NOT_BRIEFED) {
			options.add(new DialogueOption("hemomancy.dialogue.alchemist.option.accept_pallid_vessel", null, BRIEF));
		} else if (stage == PallidVesselProgress.Stage.READY) {
			options.add(new DialogueOption("hemomancy.dialogue.alchemist.option.report_pallid_vessel", null, REPORT,
					DialogueOptionPresentation.prompt("hemomancy.alchemist.pallid_vessel.report.prompt")));
		}
		options.add(new DialogueOption("hemomancy.dialogue.alchemist.option.gourd_use", "gourd_use", null));
		options.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
		List<String> lines = switch (stage) {
			case NOT_BRIEFED -> BRIEFING;
			case NEEDS_RITE -> List.of("hemomancy.alchemist.pallid_vessel.progress.rite",
					"hemomancy.alchemist.initiate.pallid_vessel.rite");
			case NEEDS_FILL -> List.of("hemomancy.alchemist.pallid_vessel.progress.fill");
			case READY -> List.of("hemomancy.alchemist.pallid_vessel.progress.ready");
			case COMPLETE -> List.of("hemomancy.alchemist.pallid_vessel.progress.complete");
		};
		return new DialogueNode(NODE, lines, options);
	}

	private static DialogueNode gourdUse() {
		return new DialogueNode("gourd_use", List.of("hemomancy.alchemist.gourd_use.fill",
				"hemomancy.alchemist.gourd_use.withdraw"),
				List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)));
	}
}
