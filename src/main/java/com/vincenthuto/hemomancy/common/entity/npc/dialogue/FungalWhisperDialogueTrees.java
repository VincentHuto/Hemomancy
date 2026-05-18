package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinition;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Static factory that produces {@link DialogueTree} variants for the
 * mysterious "fungal whisper" events that plague players at initiation
 * tiers 4–7 (Adept through Archon). Each tier receives progressively more
 * disturbing revelations about the true fungal origins of hemomancy.
 * <p>
 * Degree 4 whispers are subliminal — fleeting intrusive sensations that the
 * player might dismiss. They plant the first seeds of doubt without revealing
 * anything explicit.
 * <p>
 * Three additional one-shot whispers fire at specific world events:
 * <ul>
 *   <li>{@link #postMonolithShatter()} — fires immediately after an Archon
 *       shatters the Sanguine Monolith and the Qliphoth Seed falls out.</li>
 *   <li>{@link #postBloom()} — fires once after a player completes the Bloom
 *       of the Qliphoth rite and the dark tree takes root.</li>
 *   <li>{@link #qliphothCommunion()} — fires when a player has consumed all
 *       nine pomes from a single bloom's lifecycle, completing the Communion.</li>
 * </ul>
 * <p>
 * The speaker is anonymous ("???") with a mystery question-mark icon.
 */
public final class FungalWhisperDialogueTrees {

	private static final ResourceLocation MYSTERY_ICON = Hemomancy.rloc("textures/gui/mystery_speaker.png");
	private static final String SPEAKER = "hemomancy.whisper.speaker_name";
	private static final String MAKE_NOTE = "hemomancy.dialogue.memo.make_note";

	private FungalWhisperDialogueTrees() {}

	/**
	 * Returns a dialogue tree for the given degree. Tiers 4–7 have whisper
	 * content; lower tiers should never call this.
	 *
	 * @param degree   The player's current initiatory degree (4, 5, 6, or 7).
	 * @param variant  A variant index (0–2) for message variety within a tier.
	 */
	public static DialogueTree forDegree(int degree, int variant) {
		// entityId of 0 means no entity — this is a disembodied voice
		return switch (degree) {
			case 4 -> adeptWhisper(variant);
			case 5 -> illuminatusWhisper(variant);
			case 6 -> sanctifiedWhisper(variant);
			default -> archonWhisper(variant); // degree 7+
		};
	}

	// ── Degree 4 — Adept whispers: subliminal intrusions, barely perceptible ──

	public static MemoDefinition memoForDegree(int degree, int variant) {
		return switch (degree) {
			case 4 -> MemoDefinitions.FUNGAL_WHISPER_ADEPT;
			case 5 -> MemoDefinitions.FUNGAL_WHISPER_ILLUMINATUS;
			case 6 -> MemoDefinitions.FUNGAL_WHISPER_SANCTIFIED;
			default -> variant == 2
					? MemoDefinitions.FUNGAL_WHISPER_TRUTH
					: MemoDefinitions.FUNGAL_WHISPER_ARCHON;
		};
	}

	public static boolean shouldOfferMemoWhisper(Player player, MemoDefinition definition) {
		return definition != null && !LiberKnowledgeHelper.knowsMemo(player, definition.id());
	}

	private static DialogueTree adeptWhisper(int variant) {
		return switch (variant) {
			case 0 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.adept.v0.line1"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_ADEPT),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			case 1 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.adept.v1.line1"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.what_was_that", "follow", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.whisper.adept.v1.follow"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_ADEPT),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			default -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.adept.v2.line1"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_ADEPT),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── Degree 5 — Illuminatus whispers: first hints of something fungal ──

	private static DialogueTree illuminatusWhisper(int variant) {
		return switch (variant) {
			case 0 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.illuminatus.v0.line1",
							"hemomancy.whisper.illuminatus.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.what_was_that", "follow", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, "whisper_dismiss")
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.whisper.illuminatus.v0.follow"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_ILLUMINATUS),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			case 1 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.illuminatus.v1.line1",
							"hemomancy.whisper.illuminatus.v1.line2"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_ILLUMINATUS),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			default -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.illuminatus.v2.line1"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.who_are_you", "reveal", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("reveal", List.of(
							"hemomancy.whisper.illuminatus.v2.reveal"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_ILLUMINATUS),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── Degree 6 — Sanctified whispers: clearer fungal revelations ──

	private static DialogueTree sanctifiedWhisper(int variant) {
		return switch (variant) {
			case 0 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.sanctified.v0.line1",
							"hemomancy.whisper.sanctified.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.what_was_that", "follow", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, "whisper_dismiss")
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.whisper.sanctified.v0.follow"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_SANCTIFIED),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			case 1 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.sanctified.v1.line1",
							"hemomancy.whisper.sanctified.v1.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.who_are_you", "follow", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.whisper.sanctified.v1.follow"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_SANCTIFIED),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			default -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.sanctified.v2.line1",
							"hemomancy.whisper.sanctified.v2.line2"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_SANCTIFIED),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── Degree 7 — Archon whispers: the full truth emerges ──

	private static DialogueTree archonWhisper(int variant) {
		return switch (variant) {
			case 0 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.archon.v0.line1",
							"hemomancy.whisper.archon.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.what_was_that", "follow", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.whisper.archon.v0.follow"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_ARCHON),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			case 1 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.archon.v1.line1",
							"hemomancy.whisper.archon.v1.line2",
							"hemomancy.whisper.archon.v1.line3"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_ARCHON),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			default -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.theme(DialogueTheme.FUNGAL)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.archon.v2.line1",
							"hemomancy.whisper.archon.v2.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.who_are_you", "truth", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("truth", List.of(
							"hemomancy.whisper.archon.v2.truth1",
							"hemomancy.whisper.archon.v2.truth2"
					), List.of(
							memoOption(MemoDefinitions.FUNGAL_WHISPER_TRUTH),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, "whisper_truth_acknowledged")
					)))
					.build();
		};
	}

	// ── One-shot event whispers ──

	public static DialogueTree spineGrowth(int degree) {
		String langKey = switch (degree) {
			case 5 -> "hemomancy.whisper.spine_growth.degree5";
			case 6 -> "hemomancy.whisper.spine_growth.degree6";
			case 7 -> "hemomancy.whisper.spine_growth.degree7";
			case 8 -> "hemomancy.whisper.spine_growth.degree8";
			default -> "hemomancy.whisper.spine_growth.degree5";
		};

		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(langKey), List.of(
						memoOption(MemoDefinitions.QLIPHOTH_COMMUNION),
						new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
				)))
				.build();
	}

	public static DialogueTree fungalSpineEmerged() {
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.whisper.spine_emerged.line1",
						"hemomancy.whisper.spine_emerged.line2",
						"hemomancy.whisper.spine_emerged.line3"
				), List.of(
						memoOption(MemoDefinitions.QLIPHOTH_COMMUNION),
						new DialogueOption("hemomancy.whisper.option.i_am_listening", null, null)
				)))
				.build();
	}

	/**
	 * Fires when a degree-7 Archon interacts with the morphic pool (fungal podium)
	 * in the Fungal Gardens for the first time. The Entity's voice presents the
	 * Archon with two mutually exclusive paths: carry the truth in silence and
	 * walk away, or commit to the Eighth Degree and become its finest spore.
	 * <p>
	 * Selecting an option stamps {@code hemomancy:archon_choice_made} in the
	 * player's persistent data and triggers the return journey to the overworld.
	 */
	public static DialogueTree coreWitnessDialogue() {
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.whisper.core_witness.line1",
						"hemomancy.whisper.core_witness.line2",
						"hemomancy.whisper.core_witness.line3"
				), List.of(
						memoOption(MemoDefinitions.FUNGAL_WHISPER_TRUTH),
						new DialogueOption("hemomancy.whisper.core_witness.option.silence",
								null, "archon_choice_silence"),
						new DialogueOption("hemomancy.whisper.core_witness.option.eighth_degree",
								null, "archon_choice_eighth_degree")
				)))
				.build();
	}

	/**
	 * One-shot whisper that fires when a Qliphoth Pome falls from the tree.
	 * Sent only to the player who owns the bloom, so they know which husk to retrieve.
	 *
	 * @param huskIndex  The sequential husk index (0–8).
	 */
	public static DialogueTree pomeDropped(int huskIndex) {
		return pomeDropped(huskIndex, true);
	}

	public static DialogueTree pomeDropped(int huskIndex, boolean offerMemo) {
		/** Lang key suffixes for each husk pome-drop whisper, in consumption order (0–8). */
		final String[] POME_DROP_LANG_KEYS = {
				"hemomancy.whisper.pome_drop.nahemoth",
				"hemomancy.whisper.pome_drop.samael",
				"hemomancy.whisper.pome_drop.gamaliel",
				"hemomancy.whisper.pome_drop.harab_serapel",
				"hemomancy.whisper.pome_drop.golachab",
				"hemomancy.whisper.pome_drop.thagirion",
				"hemomancy.whisper.pome_drop.aarab_zaraq",
				"hemomancy.whisper.pome_drop.satariel",
				"hemomancy.whisper.pome_drop.ghagiel"
		};
		String langKey = (huskIndex >= 0 && huskIndex < POME_DROP_LANG_KEYS.length)
				? POME_DROP_LANG_KEYS[huskIndex]
				: "hemomancy.whisper.pome_drop.nahemoth";
		List<DialogueOption> options = offerMemo
				? List.of(
						memoOption(MemoDefinitions.QLIPHOTH_COMMUNION),
						new DialogueOption("hemomancy.whisper.option.dismiss", null, null))
				: List.of(new DialogueOption("hemomancy.whisper.option.dismiss", null, null));
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(langKey), options))
				.build();
	}

	/**
	 * Fires immediately after an Archon shatters the Sanguine Monolith and
	 * the Qliphoth Seed falls out. The voice comments on what was always inside.
	 */
	public static DialogueTree postMonolithShatter() {
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.whisper.post_shatter.line1",
						"hemomancy.whisper.post_shatter.line2"
				), List.of(
						memoOption(MemoDefinitions.FUNGAL_WHISPER_TRUTH),
						new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
				)))
				.build();
	}

	/**
	 * Fires once for the caster after a Bloom of the Qliphoth rite completes
	 * and the tree takes root. The Entity acknowledges the first fruit.
	 */
	public static DialogueTree postBloom() {
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.whisper.post_bloom.line1",
						"hemomancy.whisper.post_bloom.line2"
				), List.of(
						memoOption(MemoDefinitions.QLIPHOTH_COMMUNION),
						new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
				)))
				.build();
	}

	/**
	 * Fires when a player has consumed all nine Qliphoth Pomes from a single
	 * bloom's lifecycle, completing the Qliphoth Communion. Sets the
	 * {@code qliphoth_communion_done} event flag and opens the path to the
	 * Eighth Degree.
	 */
	public static DialogueTree qliphothCommunion() {
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.whisper.communion.line1",
						"hemomancy.whisper.communion.line2",
						"hemomancy.whisper.communion.line3"
				), List.of(
						memoOption(MemoDefinitions.QLIPHOTH_COMMUNION),
						new DialogueOption("hemomancy.whisper.option.i_am_listening", null,
								"qliphoth_communion_done")
				)))
				.build();
	}

	private static DialogueOption memoOption(MemoDefinition definition) {
		return new DialogueOption(MAKE_NOTE, null, MemoHelper.memoEvent(definition.id()));
	}
}

