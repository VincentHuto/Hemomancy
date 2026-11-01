package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The lore lines trading opens: per speaker, two Conversation cards gated on rapport tier (Known, then Trusted), about
 * the speaker, their past or their side. Each line is keyed {@code hemomancy.dialogue.rapport.<speaker>.<slug>} (the
 * player's question), with {@code .teaser} for its locked card and {@code .line1..n} for the answer. Every option is
 * also listed in {@code src/test/resources/dialogue/lore-options.txt}.
 */
public final class DialogueRapportLore {
	public static final String KEY_PREFIX = "hemomancy.dialogue.rapport.";
	private static final String NODE_PREFIX = "rapport_";
	private static final String BACK = "hemomancy.dialogue.ui.back";

	/** One rapport-gated line: {@code lines} answer keys, opened at rapport {@code tier}. */
	public record Line(String speaker, String slug, int tier, int lines) {
		public String textKey() {
			return KEY_PREFIX + speaker + "." + slug;
		}

		public String teaserKey() {
			return textKey() + DialogueHubFactory.TEASER_SUFFIX;
		}

		public List<String> lineKeys() {
			List<String> keys = new ArrayList<>(lines);
			for (int i = 1; i <= lines; i++) keys.add(textKey() + ".line" + i);
			return keys;
		}

		public String nodeId() {
			return NODE_PREFIX + slug;
		}
	}

	private static final Map<String, List<Line>> LINES = build();

	private DialogueRapportLore() {}

	private static Map<String, List<Line>> build() {
		Map<String, List<Line>> lines = new LinkedHashMap<>();
		// Harbinger Outpost and the Reef
		add(lines, "hermit", "the_vigil", 1, "the_heart", 2);
		add(lines, "vicar", "the_record", 2, "doubt", 2);
		add(lines, "alchemist", "the_bench", 1, "failed_assay", 2);
		add(lines, "mnemonist", "first_voice", 1, "what_it_cost", 2);
		add(lines, "artificer", "first_fitting", 1, "the_vow", 2);
		add(lines, "cicatrix_anchorite", "the_respite", 1, "own_scars", 2);
		add(lines, "voyager", "the_cog", 1, "the_whales", 2);
		add(lines, "votary_wayfarer", "why_aboard", 1, "afraid", 2);
		// The Unstained
		add(lines, "zealot", "why_church", 1, "the_one_lost", 2);
		add(lines, "acolyte", "the_people", 1, "the_lady", 2);
		add(lines, "guardian", "the_hammer", 1, "the_line", 2);
		add(lines, "scout", "why_scouting", 1, "anyone_waiting", 2);
		// The Brined Court: the roles whose house custom earns rapport
		add(lines, "court_gardener", "first_bed", 1, "when_you_go", 2);
		add(lines, "court_tidewarden", "the_pole", 1, "the_passage", 2);
		add(lines, "court_nursery_tender", "the_cups", 1, "own_cup", 2);
		add(lines, "court_linesman", "first_line", 1, "letting_go", 2);
		add(lines, "court_host", "the_screen", 1, "own_room", 2);
		add(lines, "court_bellwarden", "the_stores", 1, "the_sleepers", 2);
		add(lines, "court_marrow_tender", "the_hook", 1, "rooting", 2);
		add(lines, "court_offering_bearer", "the_descent", 1, "worthy", 2);
		// The Circus
		add(lines, "circus_ringmaster", "the_hat", 1, "the_understudies", 2);
		add(lines, "circus_acrobat", "first_fall", 1, "no_net", 2);
		add(lines, "circus_fire_eater", "first_flame", 1, "the_burn", 2);
		add(lines, "circus_knife_thrower", "first_knife", 1, "the_miss", 2);
		add(lines, "circus_stilt_walker", "the_view", 1, "the_carousel", 2);
		add(lines, "circus_strongman", "the_anvil", 1, "good_for", 2);
		add(lines, "circus_beast_tamer", "first_animal", 1, "the_spider", 2);
		add(lines, "circus_threadkeeper", "the_ledger", 1, "own_debt", 2);
		add(lines, "circus_understudy", "best_role", 1, "the_missing", 2);
		return Map.copyOf(lines);
	}

	/** A Known line and a Trusted line, with their answer lengths. */
	private static void add(Map<String, List<Line>> lines, String speaker, String known, int knownLines,
			String trusted, int trustedLines) {
		lines.put(speaker, List.of(new Line(speaker, known, NpcTradeRules.KNOWN, knownLines),
				new Line(speaker, trusted, NpcTradeRules.TRUSTED, trustedLines)));
	}

	public static List<Line> lines(String speaker) {
		return LINES.getOrDefault(speaker, List.of());
	}

	public static Map<String, List<Line>> all() {
		return LINES;
	}

	/**
	 * Adds the speaker's rapport lines to the end of the greeting's options as lore options gated on their tier. Only
	 * the lines open at {@code tier} bring their answer node; a locked line is an option with nowhere to go, which
	 * {@link DialogueHubFactory} draws as a locked lore card.
	 */
	public static DialogueTree with(DialogueTree tree, String speaker, int tier) {
		List<Line> lines = lines(speaker);
		DialogueNode root = tree.getStartNode();
		if (lines.isEmpty() || root == null) return tree;
		Map<String, DialogueNode> nodes = new LinkedHashMap<>(tree.nodes());
		List<DialogueOption> options = new ArrayList<>(root.options());
		int insertAt = options.size();
		while (insertAt > 0 && isLeave(options.get(insertAt - 1))) insertAt--;
		for (Line line : lines) {
			options.add(insertAt++, new DialogueOption(line.textKey(), line.nodeId(), null).withLore()
					.withRapportTier(line.tier()));
			if (line.tier() <= tier) {
				nodes.put(line.nodeId(), new DialogueNode(line.nodeId(), line.lineKeys(),
						List.of(new DialogueOption(BACK, root.id(), null))));
			}
		}
		nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}

	private static boolean isLeave(DialogueOption option) {
		return option.nextNodeId() == null && option.eventId() == null;
	}
}
