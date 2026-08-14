package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinition;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Static factory producing {@link DialogueTree} instances for Our Lady of Still
 * Waters' whisper events. These are the counterpart to the Fungal Whisper system
 * — soft, cold, and personal messages directed at players progressing along the
 * Unstained path.
 *
 * <p>Whisper content is organised by purity/clarity stage. The voice begins as
 * barely perceptible — the faint sound of running water — and gradually becomes
 * clearer and more urgent as the player purifies themselves. At PURIFIED stage
 * she guides the player toward the Rite of Clarity Ascension. After clarity is
 * unlocked her tone shifts: warmer at first, then increasingly serious as the
 * player approaches ENLIGHTENED, where she reveals the price of her gift.
 */
public final class OurLadyWhisperDialogueTrees {

	private static final ResourceLocation LADY_ICON = Hemomancy.rloc("textures/gui/mystery_speaker.png");
	private static final String SPEAKER = "hemomancy.lady.speaker_name";
	private static final String MAKE_NOTE = "hemomancy.dialogue.memo.make_note";

	private OurLadyWhisperDialogueTrees() {}

	private static DialogueTree.Builder ladyBuilder() {
		return DialogueTree.builder(SPEAKER, LADY_ICON, 0).theme(OurLadyDialogueThemeRules.theme());
	}

	public static MemoDefinition memoForPurityStage(int purityLevel, int variant) {
		return switch (purityLevel) {
			case 1 -> MemoDefinitions.PALE_LADY_TAINTED;
			case 2 -> MemoDefinitions.PALE_LADY_CLEANSING;
			case 3 -> MemoDefinitions.PALE_LADY_ABSOLVED;
			default -> MemoDefinitions.PALE_LADY_PURIFIED;
		};
	}

	public static MemoDefinition memoForClarityStage(int clarityLevel, int variant) {
		return switch (clarityLevel) {
			case 1 -> MemoDefinitions.PALE_LADY_DISCERNING;
			case 2 -> MemoDefinitions.PALE_LADY_VIGILANT;
			case 3 -> MemoDefinitions.PALE_LADY_RESOLUTE;
			default -> MemoDefinitions.PALE_LADY_ENLIGHTENED;
		};
	}

	public static boolean shouldOfferMemoWhisper(Player player, MemoDefinition definition) {
		return definition != null && !LiberKnowledgeHelper.knowsMemo(player, definition.id());
	}

	private static List<DialogueOption> noteOptions(MemoDefinition definition, DialogueOption... options) {
		ArrayList<DialogueOption> withMemo = new ArrayList<>(options.length + 1);
		withMemo.add(memoOption(definition));
		withMemo.addAll(Arrays.asList(options));
		return List.copyOf(withMemo);
	}

	private static DialogueOption memoOption(MemoDefinition definition) {
		return new DialogueOption(MAKE_NOTE, null, MemoHelper.memoEvent(definition.id()));
	}

	private static DialogueTree withStartMemo(DialogueTree tree, MemoDefinition definition) {
		DialogueNode start = tree.getStartNode();
		if (start == null) {
			return tree;
		}
		Map<String, DialogueNode> nodes = new LinkedHashMap<>(tree.nodes());
		nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), noteOptions(definition,
				start.options().toArray(DialogueOption[]::new))));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes, tree.entityId(),
				tree.theme());
	}

	// ════════════════════════════════════════════════════════════
	//  PURITY WHISPERS  (Phase 1 — before clarity is unlocked)
	// ════════════════════════════════════════════════════════════

	/**
	 * Returns a purity-phase whisper tree for the given purity level (0–3)
	 * and variant (0–2).
	 *
	 * @param purityLevel  The numeric purity stage level (1=TAINTED, 2=CLEANSING,
	 *                     3=ABSOLVED, 4=PURIFIED).
	 * @param variant      A variant index (0–2) for message variety.
	 */
	public static DialogueTree forPurityStage(int purityLevel, int variant) {
		DialogueTree tree = switch (purityLevel) {
			case 1 -> taintedWhisper(variant);
			case 2 -> cleansingWhisper(variant);
			case 3 -> absolvedWhisper(variant);
			default -> purifiedWhisper(variant); // level 4+
		};
		return withStartMemo(tree, memoForPurityStage(purityLevel, variant));
	}

	// ── TAINTED (purity 25–49) — faint, barely audible, like distant water ──

	private static DialogueTree taintedWhisper(int variant) {
		return switch (variant) {
			case 0 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.tainted.v0.line1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "follow", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.lady.tainted.v0.follow"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			case 1 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.tainted.v1.line1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			default -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.tainted.v2.line1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "follow", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.lady.tainted.v2.follow"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── CLEANSING (purity 50–74) — more audible, gentle encouragement ──

	private static DialogueTree cleansingWhisper(int variant) {
		return switch (variant) {
			case 0 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.cleansing.v0.line1",
							"hemomancy.lady.cleansing.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.who_are_you", "reveal", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("reveal", List.of(
							"hemomancy.lady.cleansing.v0.reveal"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			case 1 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.cleansing.v1.line1",
							"hemomancy.lady.cleansing.v1.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			default -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.cleansing.v2.line1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "follow", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.lady.cleansing.v2.follow"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── ABSOLVED (purity 75–99) — clear, personal, slightly urgent ──

	private static DialogueTree absolvedWhisper(int variant) {
		return switch (variant) {
			case 0 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.absolved.v0.line1",
							"hemomancy.lady.absolved.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "follow", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.lady.absolved.v0.follow"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			case 1 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.absolved.v1.line1",
							"hemomancy.lady.absolved.v1.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			default -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.absolved.v2.line1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.who_are_you", "reveal", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("reveal", List.of(
							"hemomancy.lady.absolved.v2.reveal1",
							"hemomancy.lady.absolved.v2.reveal2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── PURIFIED (purity 100, pre-clarity) — direct, guides toward the Rite ──

	private static DialogueTree purifiedWhisper(int variant) {
		return switch (variant) {
			case 0 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.purified.v0.line1",
							"hemomancy.lady.purified.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "rite", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("rite", List.of(
							"hemomancy.lady.purified.v0.rite1",
							"hemomancy.lady.purified.v0.rite2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, "lady_clarity_rite_memory")
					)))
					.build();
			case 1 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.purified.v1.line1",
							"hemomancy.lady.purified.v1.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			default -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.purified.v2.line1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "rite", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("rite", List.of(
							"hemomancy.lady.purified.v2.rite1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ════════════════════════════════════════════════════════════
	//  CLARITY WHISPERS  (Phase 2 — after clarity is unlocked)
	// ════════════════════════════════════════════════════════════

	/**
	 * Returns a clarity-phase whisper tree for the given clarity level and variant.
	 *
	 * @param clarityLevel The numeric clarity stage level (1=DISCERNING, 2=VIGILANT,
	 *                     3=RESOLUTE, 4=ENLIGHTENED).
	 * @param variant      A variant index (0–2) for variety.
	 */
	public static DialogueTree forClarityStage(int clarityLevel, int variant) {
		DialogueTree tree = switch (clarityLevel) {
			case 1 -> discerningWhisper(variant);
			case 2 -> vigilantWhisper(variant);
			case 3 -> resoluteWhisper(variant);
			default -> enlightenedWhisper(variant); // level 4+
		};
		return withStartMemo(tree, memoForClarityStage(clarityLevel, variant));
	}

	// ── DISCERNING clarity (25–49) — relief, warmth, early revelations ──

	private static DialogueTree discerningWhisper(int variant) {
		return switch (variant) {
			case 0 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.discerning.v0.line1",
							"hemomancy.lady.discerning.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "follow", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.lady.discerning.v0.follow"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			case 1 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.discerning.v1.line1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			default -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.discerning.v2.line1",
							"hemomancy.lady.discerning.v2.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.who_are_you", "reveal", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("reveal", List.of(
							"hemomancy.lady.discerning.v2.reveal"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── VIGILANT clarity (50–74) — deeper, hints about the world's true state ──

	private static DialogueTree vigilantWhisper(int variant) {
		return switch (variant) {
			case 0 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.vigilant.v0.line1",
							"hemomancy.lady.vigilant.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "follow", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.lady.vigilant.v0.follow1",
							"hemomancy.lady.vigilant.v0.follow2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			case 1 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.vigilant.v1.line1",
							"hemomancy.lady.vigilant.v1.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			default -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.vigilant.v2.line1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "follow", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.lady.vigilant.v2.follow"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── RESOLUTE clarity (75–99) — serious, begins to reveal her own limits ──

	private static DialogueTree resoluteWhisper(int variant) {
		return switch (variant) {
			case 0 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.resolute.v0.line1",
							"hemomancy.lady.resolute.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "follow", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.lady.resolute.v0.follow1",
							"hemomancy.lady.resolute.v0.follow2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			case 1 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.resolute.v1.line1",
							"hemomancy.lady.resolute.v1.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			default -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.resolute.v2.line1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.who_are_you", "reveal", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("reveal", List.of(
							"hemomancy.lady.resolute.v2.reveal1",
							"hemomancy.lady.resolute.v2.reveal2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── ENLIGHTENED clarity (100) — final truths, she is ancient and cold ──

	private static DialogueTree enlightenedWhisper(int variant) {
		return switch (variant) {
			case 0 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.enlightened.v0.line1",
							"hemomancy.lady.enlightened.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "truth", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("truth", List.of(
							"hemomancy.lady.enlightened.v0.truth1",
							"hemomancy.lady.enlightened.v0.truth2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, "lady_enlightened_truth_1")
					)))
					.build();
			case 1 -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.enlightened.v1.line1",
							"hemomancy.lady.enlightened.v1.line2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.build();
			default -> ladyBuilder()
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.lady.enlightened.v2.line1"
					), List.of(
							new DialogueOption("hemomancy.lady.option.listen", "truth", null),
							new DialogueOption("hemomancy.lady.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("truth", List.of(
							"hemomancy.lady.enlightened.v2.truth1",
							"hemomancy.lady.enlightened.v2.truth2"
					), List.of(
							new DialogueOption("hemomancy.lady.option.dismiss", null, "lady_enlightened_truth_2")
					)))
					.build();
		};
	}
}
