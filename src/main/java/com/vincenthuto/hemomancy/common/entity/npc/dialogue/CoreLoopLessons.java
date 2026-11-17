package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The core-loop lessons (round two P10): which outpost speaker teaches which system, at which Degrees, in which
 * branches, and which Liber pages the telling opens. {@link CoreLoopLessonDialogue} turns a row into a lore card and
 * its nodes; {@code route/CoreLoopLessonRoutes} opens the pages when a branch is asked.
 *
 * <p>Ids never contain a word {@code DialogueHubFactory} reads as Quest Work (lesson, brief, task, report, claim,
 * bestiary, reward, quest...), so every card stays in Conversation.
 */
public final class CoreLoopLessons {
	/** One question the player can ask inside a lesson, answered in {@code lines} lines. */
	public record Branch(String id, int lines) {
	}

	/**
	 * One lesson. {@code speaker} is the hub id the speaker's tree is decorated under; {@code npcKey} the lang prefix
	 * of their lines and options. Offered while the player's Degree lies in {@code minDegree..maxDegree}.
	 */
	public record Lesson(String speaker, String npcKey, String id, int minDegree, int maxDegree, int lines,
			List<Branch> branches, List<ResourceLocation> pages) {
		public String rootOption() {
			return "hemomancy.dialogue." + npcKey + ".option.craft." + id;
		}

		public String branchOption(Branch branch) {
			return rootOption() + "." + branch.id();
		}

		public String backOption() {
			return CoreLoopLessons.backOption(npcKey);
		}

		public String leaveOption() {
			return "hemomancy.dialogue." + npcKey + ".option.leave";
		}

		public String node() {
			return "craft_" + id;
		}

		public String node(Branch branch) {
			return node() + "_" + branch.id();
		}

		public String event() {
			return npcKey + "_craft_" + id;
		}

		public List<String> lineKeys() {
			return lineKeys("hemomancy." + npcKey + ".craft." + id, lines);
		}

		public List<String> lineKeys(Branch branch) {
			return lineKeys("hemomancy." + npcKey + ".craft." + id + "." + branch.id(), branch.lines());
		}

		public boolean offeredAt(int degree) {
			return degree >= minDegree && degree <= maxDegree;
		}

		private static List<String> lineKeys(String prefix, int count) {
			String[] keys = new String[count];
			for (int i = 0; i < count; i++) keys[i] = prefix + ".line" + (i + 1);
			return List.of(keys);
		}
	}

	public static final String HERMIT = "hermit";
	public static final String VICAR = "vicar";
	public static final String ALCHEMIST = "alchemist";
	public static final String MNEMONIST = "mnemonist";
	public static final String ARTIFICER = "artificer";
	public static final String VEIN_MASON = "cicatrix_anchorite";
	/** The highest Degree a Hermit teaches at: from about the fifth he knows less than the player (NPC_VOICES.md). */
	public static final int HERMIT_LAST_DEGREE = 4;
	/** The summary every lesson card shows in the hub, in place of the Conversation category's (UN-10). */
	public static final String CARD_SUMMARY = "hemomancy.dialogue.topic.craft.summary";
	private static final int ANY = Integer.MAX_VALUE;

	private static final List<Lesson> ALL = List.of(
			// The Hermit, from his own body, for blood that has just woken.
			lesson(HERMIT, "hermit", "verbs", 0, HERMIT_LAST_DEGREE, 2,
					List.of(b("out", 2), b("in", 2), b("spilled", 2)),
					LiberEntryDefinitions.THE_TWO_VERBS),
			lesson(HERMIT, "hermit", "answer", 1, HERMIT_LAST_DEGREE, 2,
					List.of(b("ground", 1), b("night", 2), b("drinkers", 2)),
					LiberEntryDefinitions.THE_WORLD_ANSWERS),
			// The Vicar, from the record: the verbs and their order, the day, the world's count, the kept creature.
			lesson(VICAR, "vicar", "verbs", 1, ANY, 2,
					List.of(b("key", 2), b("order", 2), b("draw", 2)),
					LiberEntryDefinitions.THE_TWO_VERBS, LiberEntryDefinitions.THE_DRAW_AND_THE_MARK),
			lesson(VICAR, "vicar", "day", 1, ANY, 2,
					List.of(b("food", 2), b("rest", 2), b("meats", 2)),
					LiberEntryDefinitions.THE_BODY_AND_THE_DAY),
			lesson(VICAR, "vicar", "answers", 2, ANY, 2,
					List.of(b("count", 2), b("moon", 2), b("water", 2)),
					LiberEntryDefinitions.THE_WORLD_ANSWERS, LiberEntryDefinitions.CHUM),
			lesson(VICAR, "vicar", "keeping", 3, ANY, 2,
					List.of(b("lead", 2), b("feed", 2), b("brood", 2)),
					LiberEntryDefinitions.MENAGERIE, LiberEntryDefinitions.THE_BROOD),
			// The Alchemist, as procedure: blood that lives, Temper, reading a creature, fed ground, strain beds.
			lesson(ALCHEMIST, "alchemist", "living_blood", 1, ANY, 2,
					List.of(b("keepers", 2), b("clot", 2), b("gauge", 2)),
					LiberEntryDefinitions.LIVING_BLOOD, LiberEntryDefinitions.SANGUINE_CONDENSER),
			lesson(ALCHEMIST, "alchemist", "fed", 1, ANY, 2,
					List.of(b("drip", 2), b("basin", 2), b("effusion", 2)),
					LiberEntryDefinitions.FED_GROUND),
			lesson(ALCHEMIST, "alchemist", "temper", 2, ANY, 2,
					List.of(b("fill", 2), b("dry", 2), b("catalysts", 2)),
					LiberEntryDefinitions.TEMPER, LiberEntryDefinitions.TEMPERAMENT),
			lesson(ALCHEMIST, "alchemist", "humours", 2, ANY, 2,
					List.of(b("sample", 2), b("study", 2), b("dose", 2)),
					LiberEntryDefinitions.UNREAD_HUMOURS),
			lesson(ALCHEMIST, "alchemist", "beds", 3, ANY, 2,
					List.of(b("strains", 2), b("timber", 2), b("crops", 2)),
					LiberEntryDefinitions.STRAIN_BEDS),
			// The Mnemonist, stating: what a memory does to a held verb, the strands, the ground that answers.
			lesson(MNEMONIST, "mnemonist", "sculptings", 1, ANY, 2,
					List.of(b("first", 2), b("work", 2), b("mend", 2), b("draw", 2)),
					LiberEntryDefinitions.LIGNUM_MORTIS, LiberEntryDefinitions.PYRETIC_FORGE,
					LiberEntryDefinitions.BRAZIER_TRANSMUTATION, LiberEntryDefinitions.SECOND_SCULPTINGS),
			lesson(MNEMONIST, "mnemonist", "strands", 2, ANY, 2,
					List.of(b("forms", 2), b("stain", 2), b("brand", 2)),
					LiberEntryDefinitions.STRANDS, LiberEntryDefinitions.STRAND_FORMS,
					LiberEntryDefinitions.SECOND_STRANDS, LiberEntryDefinitions.BLOOD_BINDING),
			lesson(MNEMONIST, "mnemonist", "land", 3, ANY, 2,
					List.of(b("strike", 2), b("dark", 2), b("light", 2)),
					LiberEntryDefinitions.LIVING_LAND, LiberEntryDefinitions.GROUND_SCULPTINGS),
			// The Artificer, in the order the hands do it: the Staff, its strand line, climbing, a hot run.
			lesson(ARTIFICER, "artificer", "hot_runs", 1, ANY, 2,
					List.of(b("uses", 2), b("heat", 2), b("feeder", 2)),
					LiberEntryDefinitions.THE_GROUND_NETWORK, LiberEntryDefinitions.GROUND_SCULPTINGS),
			lesson(ARTIFICER, "artificer", "staff", 2, ANY, 2,
					List.of(b("rings", 2), b("dig", 2), b("forms", 2)),
					LiberEntryDefinitions.THE_STAFF_GROWS, LiberEntryDefinitions.MINING_SCULPTINGS),
			lesson(ARTIFICER, "artificer", "strand_line", 2, ANY, 2,
					List.of(b("reach", 2), b("crack", 2), b("twin", 2)),
					LiberEntryDefinitions.SECOND_STRANDS, LiberEntryDefinitions.STRAND_FORMS),
			lesson(ARTIFICER, "artificer", "climb", 2, ANY, 2,
					List.of(b("root", 2), b("cleats", 2), b("line", 2)),
					LiberEntryDefinitions.VENOUS_CLING, LiberEntryDefinitions.TENDON_LINE),
			// The Vein-Mason, from the fourth Degree: the ground as a body, the carried crossing, walls and scars.
			lesson(VEIN_MASON, "anchorite", "ground_marks", 4, ANY, 2,
					List.of(b("cut", 2), b("vein", 2), b("basin", 2)),
					LiberEntryDefinitions.THE_GROUND_NETWORK, LiberEntryDefinitions.LIVING_VEINS_AND_BASINS),
			lesson(VEIN_MASON, "anchorite", "carried", 4, ANY, 2,
					List.of(b("join", 2), b("price", 2), b("limits", 2)),
					LiberEntryDefinitions.LIVING_VEINS_AND_BASINS),
			lesson(VEIN_MASON, "anchorite", "walls", 4, ANY, 2,
					List.of(b("ice", 2), b("iron", 2), b("scars", 2)),
					LiberEntryDefinitions.SECOND_SCULPTINGS_WALLS, LiberEntryDefinitions.TEMPER));

	private CoreLoopLessons() {
	}

	public static List<Lesson> all() {
		return ALL;
	}

	/** The speaker's lessons, in the order their cards show. */
	public static List<Lesson> forSpeaker(String speaker) {
		return ALL.stream().filter(lesson -> lesson.speaker().equals(speaker)).toList();
	}

	@Nullable
	public static Lesson byEvent(String eventId) {
		if (eventId == null) return null;
		for (Lesson lesson : ALL) if (lesson.event().equals(eventId)) return lesson;
		return null;
	}

	/** Whether {@code optionKey} opens a lesson's card. */
	public static boolean isRootOption(String optionKey) {
		for (Lesson lesson : ALL) if (lesson.rootOption().equals(optionKey)) return true;
		return false;
	}

	/** The back option of a speaker's lesson nodes, keyed by their lang prefix. */
	public static String backOption(String npcKey) {
		return "hemomancy.dialogue." + npcKey + ".option.craft.back";
	}

	private static Lesson lesson(String speaker, String npcKey, String id, int minDegree, int maxDegree, int lines,
			List<Branch> branches, ResourceLocation... pages) {
		return new Lesson(speaker, npcKey, id, minDegree, maxDegree, lines, branches, List.of(pages));
	}

	private static Branch b(String id, int lines) {
		return new Branch(id, lines);
	}
}
