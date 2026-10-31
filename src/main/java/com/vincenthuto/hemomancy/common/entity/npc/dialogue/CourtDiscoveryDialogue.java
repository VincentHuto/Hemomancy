package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtDiscoveryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtIntroductionRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Brined Court's three discovery routes in dialogue (Track 5): the Vicar's fifth-Degree referral, the Voyager's
 * explanation and Relay Chart, the Alchemist's lead, and the Brine-Sealed Letter's item inquiries. A route that
 * arrives after another acknowledges it with a {@code court.known.<route>} line instead of repeating it.
 */
public final class CourtDiscoveryDialogue {
	public static final String VICAR_LETTER = "vicar_court_letter";
	public static final String ALCHEMIST_LETTER = "alchemist_court_letter";
	public static final String VOYAGER_LETTER = "voyager_court_letter";
	public static final String VOYAGER_LEARN = "voyager_court_learn";
	public static final String VOYAGER_CHART = "voyager_relay_chart";
	public static final String ALCHEMIST_LEAD = "alchemist_court_lead";
	static final String NODE = "court";
	static final ResourceLocation LETTER = Hemomancy.rloc("brine_sealed_letter");
	private static final String READ_LETTER = "hemomancy.dialogue.court.option.read_letter";
	private static final String LOST_LETTER = "hemomancy.dialogue.court.option.lost_letter";

	private CourtDiscoveryDialogue() {
	}

	/** What the three speakers need to know about one player. */
	public record View(int degree, boolean known, @Nullable CourtProgress.Route knownBy, boolean letterLost,
			boolean carryingChart, int highestStrainMaturity) {
		public static View of(ServerPlayer player) {
			CourtProgress progress = CourtDiscoveryProgress.progress(player);
			return new View(HemoCapabilityAccess.getPlayerDegreeNumber(player), progress.known(), progress.knownBy(),
					CourtDiscoveryProgress.letterLost(player), CourtDiscoveryProgress.carriesChart(player),
					CourtDiscoveryProgress.highestStrainMaturity(player));
		}

		String knownLine(String speaker) {
			CourtProgress.Route route = knownBy == null ? CourtProgress.Route.FOUND : knownBy;
			return "hemomancy." + speaker + ".court.known." + route.serializedName();
		}
	}

	/** The Vicar refers a fifth-Degree player to the Voyagers, after the Reef Watch bearing if it isn't done. */
	public static DialogueTree withVicar(DialogueTree tree, View view, boolean bearingAvailable) {
		if (!CourtIntroductionRules.vicarRefers(view.degree()) && !view.letterLost()) return tree;
		String leave = "hemomancy.dialogue.vicar.option.leave";
		List<String> lines;
		List<DialogueOption> options = new ArrayList<>();
		if (view.letterLost()) {
			lines = List.of("hemomancy.vicar.court.letter_lost");
			options.add(new DialogueOption(LOST_LETTER, null, VICAR_LETTER));
		} else if (view.known()) {
			lines = List.of(view.knownLine("vicar"), "hemomancy.vicar.court.chart");
		} else {
			lines = List.of("hemomancy.vicar.court.lead", "hemomancy.vicar.court.voyager");
			if (bearingAvailable) options.add(new DialogueOption("hemomancy.dialogue.vicar.option.voyager_bearing",
					null, VoyagerIntroductionDialogue.BEARING));
		}
		options.add(new DialogueOption(leave, null, null));
		return withTopic(tree, "hemomancy.dialogue.vicar.option.ask_about_court", lines, options);
	}

	/** A Voyager explains the Court at a cordial distance and issues, then reissues, the Relay Chart. */
	public static DialogueTree withVoyager(DialogueTree tree, View view) {
		if (!CourtIntroductionRules.voyagerSpeaks(view.degree(), view.known(), view.letterLost())) return tree;
		List<String> lines;
		List<DialogueOption> options = new ArrayList<>();
		if (view.letterLost()) {
			lines = List.of("hemomancy.voyager.court.letter_lost", "hemomancy.voyager.court.line2");
			options.add(new DialogueOption(LOST_LETTER, null, VOYAGER_LETTER));
		} else if (view.known()) {
			lines = List.of(view.knownLine("voyager"), "hemomancy.voyager.court.line2");
			if (CourtIntroductionRules.canIssueChart(true, view.carryingChart()))
				options.add(new DialogueOption("hemomancy.dialogue.voyager.option.relay_chart", null, VOYAGER_CHART));
		} else {
			lines = List.of("hemomancy.voyager.court.line1", "hemomancy.voyager.court.line2");
			options.add(new DialogueOption("hemomancy.dialogue.voyager.option.court_bearing", null, VOYAGER_LEARN));
		}
		options.add(new DialogueOption("hemomancy.dialogue.voyager.option.leave", null, null));
		return withTopic(tree, "hemomancy.dialogue.voyager.option.ask_about_court", lines, options);
	}

	/** Past the fifth Degree with a Mature strain, the Alchemist admits the best cultivators aren't in the Order. */
	public static DialogueTree withAlchemist(DialogueTree tree, View view) {
		boolean points = CourtIntroductionRules.alchemistPoints(view.degree(), view.highestStrainMaturity());
		if (!points && !view.letterLost()) return tree;
		List<String> lines;
		List<DialogueOption> options = new ArrayList<>();
		if (view.letterLost()) {
			lines = List.of("hemomancy.alchemist.court.letter_lost");
			options.add(new DialogueOption(LOST_LETTER, null, ALCHEMIST_LETTER));
		} else if (view.known()) {
			lines = List.of(view.knownLine("alchemist"));
		} else {
			lines = List.of("hemomancy.alchemist.court.lead.line1", "hemomancy.alchemist.court.lead.line2");
			options.add(new DialogueOption("hemomancy.dialogue.alchemist.option.court_lead", null, ALCHEMIST_LEAD));
		}
		options.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
		return withTopic(tree, "hemomancy.dialogue.alchemist.option.ask_about_court", lines, options);
	}

	/**
	 * The letter's item inquiry: unknown, it offers to record the letter; known, the speaker acknowledges the route
	 * that came first. Run after {@code DialogueItemInquiryNodes} and before the hub decoration.
	 */
	public static DialogueTree withLetterInquiry(DialogueTree tree, String speaker, String eventId, View view) {
		String id = DialogueItemInquiryNodes.inventoryNodeId(LETTER);
		DialogueNode node = tree.getNode(id);
		if (node == null) return tree;
		var nodes = new LinkedHashMap<>(tree.nodes());
		if (view.known()) {
			var lines = new ArrayList<>(node.lines());
			lines.add(view.knownLine(speaker));
			nodes.put(id, new DialogueNode(id, lines, node.options()));
		} else {
			var options = new ArrayList<DialogueOption>();
			options.add(new DialogueOption(READ_LETTER, null, eventId));
			options.addAll(node.options());
			nodes.put(id, new DialogueNode(id, node.lines(), options));
		}
		return copy(tree, nodes);
	}

	private static DialogueTree withTopic(DialogueTree tree, String optionKey, List<String> lines,
			List<DialogueOption> nodeOptions) {
		DialogueNode start = tree.getStartNode();
		if (start == null) return tree;
		var nodes = new LinkedHashMap<>(tree.nodes());
		var options = new ArrayList<>(start.options());
		options.add(Math.max(0, options.size() - 1), new DialogueOption(optionKey, NODE, null));
		nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
		nodes.put(NODE, new DialogueNode(NODE, lines, List.copyOf(nodeOptions)));
		return copy(tree, nodes);
	}

	private static DialogueTree copy(DialogueTree tree, LinkedHashMap<String, DialogueNode> nodes) {
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}
}
