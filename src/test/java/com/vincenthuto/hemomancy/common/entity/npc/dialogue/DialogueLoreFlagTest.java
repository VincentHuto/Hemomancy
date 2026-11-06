package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The lore flag on root options matches {@code src/test/resources/dialogue/lore-options.txt}. The golden was seeded from
 * what the old keyword classifier put in the Lore box, so the Conversation merge moved no line in or out of lore.
 * Every hub tree builder is swept with sampled arguments; options only a live player can reach are covered by the
 * golden alone.
 */
class DialogueLoreFlagTest {
	private static final Map<Class<?>, String> TREES = new LinkedHashMap<>();
	private static final int SAMPLES_PER_METHOD = 400;
	/**
	 * Golden lines the sweep reaches today. Raise it when the sweep reaches more; never lower it to pass. Lower it only
	 * when lines leave the golden on purpose (2026-10-31: the Anchorite's replace_pattern and accept_referral stopped
	 * being lore, 66 to 64; then the Hermit's what_next and the Alchemist's tell_me_about_chisel_station became quests,
	 * 64 to 62. The Artificer's teach_armature also left, but the sweep never reached it: its workshop tree needs an
	 * assignment Step, and sampled enums are null). 2026-11-05: the Monolith's ten lore cards joined, 62 to 72.
	 */
	private static final int MIN_REACHED = 72;

	static {
		TREES.put(HarbingerVicarDialogueTrees.class, "vicar");
		TREES.put(HarbingerAlchemistDialogueTrees.class, "alchemist");
		TREES.put(HarbingerArtificerDialogueTrees.class, "artificer");
		TREES.put(HarbingerMnemonistDialogueTrees.class, "mnemonist");
		TREES.put(HarbingerHermitDialogueTrees.class, "hermit");
		TREES.put(HarbingerCicatrixAnchoriteDialogueTrees.class, "cicatrix_anchorite");
		TREES.put(HarbingerVoyagerDialogueTrees.class, "voyager");
		TREES.put(HarbingerVotaryWayfarerDialogueTrees.class, "votary_wayfarer");
		TREES.put(AcolyteDialogueTrees.class, "acolyte");
		TREES.put(ZealotDialogueTrees.class, "zealot");
		TREES.put(GuardianDialogueTrees.class, "guardian");
		TREES.put(SanguineMonolithDialogueTrees.class, "monolith");
	}

	@Test
	void flaggedRootOptionsMatchTheGolden() throws IOException {
		Set<String> golden = golden();
		Set<String> flagged = new TreeSet<>();
		Set<String> unflaggedGolden = new TreeSet<>();
		Random random = new Random(0x10E);
		int trees = 0;
		for (Map.Entry<Class<?>, String> entry : TREES.entrySet()) {
			for (Method method : entry.getKey().getDeclaredMethods()) {
				if (!buildsTree(method)) continue;
				for (int sample = 0; sample < SAMPLES_PER_METHOD; sample++) {
					DialogueTree tree = invoke(method, random);
					if (tree == null || tree.getStartNode() == null) continue;
					trees++;
					for (DialogueOption option : tree.getStartNode().options()) {
						String line = entry.getValue() + " " + option.text();
						if (option.presentation().lore()) flagged.add(line);
						else if (golden.contains(line)) unflaggedGolden.add(line);
					}
				}
			}
		}

		assertTrue(trees > 0, "the sweep built no trees");
		Set<String> unexpected = new TreeSet<>(flagged);
		unexpected.removeAll(golden);
		assertEquals(Set.of(), unexpected,
				"lore options missing from lore-options.txt (add the line when you write a lore option)");
		assertEquals(Set.of(), unflaggedGolden, "lore-options.txt lines that lost their withLore() flag");
		assertTrue(flagged.size() >= MIN_REACHED,
				"the sweep reached only " + flagged.size() + " golden lore lines; it used to reach " + MIN_REACHED);
	}

	@Test
	void goldenHasNoDuplicatesAndNamesKnownSpeakers() throws IOException {
		Set<String> speakers = golden().stream().map(line -> line.substring(0, line.indexOf(' ')))
				.collect(Collectors.toCollection(TreeSet::new));
		Set<String> known = new TreeSet<>(TREES.values());
		known.add("circus_ringmaster");
		known.add("circus_performer");
		// Rapport lore lines (DialogueRapportLore) speak as their trade speaker.
		known.addAll(com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides.all().keySet());
		speakers.removeAll(known);
		assertEquals(Set.of(), speakers);
	}

	private static Set<String> golden() throws IOException {
		Set<String> lines = new TreeSet<>();
		for (String line : Files.readAllLines(RepoPaths.resolve("src/test/resources/dialogue/lore-options.txt"))) {
			String trimmed = line.strip();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
			assertTrue(lines.add(trimmed), "duplicate golden line " + trimmed);
		}
		return lines;
	}

	private static boolean buildsTree(Method method) {
		if (!Modifier.isStatic(method.getModifiers()) || method.getReturnType() != DialogueTree.class) return false;
		for (Class<?> type : method.getParameterTypes()) {
			// Decorators and builders that need a game object (an item, a player) are out of reach here.
			if (type == DialogueTree.class || type.getName().startsWith("net.minecraft.")) return false;
		}
		return true;
	}

	private static DialogueTree invoke(Method method, Random random) {
		Class<?>[] types = method.getParameterTypes();
		Object[] args = new Object[types.length];
		for (int i = 0; i < types.length; i++) args[i] = sample(types[i], random, 0);
		try {
			method.setAccessible(true);
			return (DialogueTree) method.invoke(null, args);
		} catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
			return null;
		}
	}

	private static Object sample(Class<?> type, Random random, int depth) {
		if (type == int.class || type == Integer.class) return random.nextInt(9);
		if (type == long.class) return (long) random.nextInt(9);
		if (type == float.class) return random.nextFloat();
		if (type == double.class) return random.nextDouble();
		if (type == boolean.class || type == Boolean.class) return random.nextBoolean();
		// Enums stay null: reading their constants can initialise registries a unit test has no bootstrap for.
		if (type.isRecord() && depth < 3) {
			RecordComponent[] components = type.getRecordComponents();
			Class<?>[] componentTypes = new Class<?>[components.length];
			Object[] values = new Object[components.length];
			for (int i = 0; i < components.length; i++) {
				componentTypes[i] = components[i].getType();
				values[i] = sample(componentTypes[i], random, depth + 1);
			}
			try {
				var constructor = type.getDeclaredConstructor(componentTypes);
				constructor.setAccessible(true);
				return constructor.newInstance(values);
			} catch (ReflectiveOperationException | RuntimeException e) {
				return null;
			}
		}
		return null;
	}
}
