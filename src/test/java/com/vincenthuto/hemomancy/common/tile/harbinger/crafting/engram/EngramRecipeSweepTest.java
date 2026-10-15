package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every real weaving recipe, seeded as the loom seeds it (world, recipe id, weaver), at every rank and for many worlds
 * and weavers, with fungal pressure boiled over before anything is threaded: the engram's reference always threads
 * home within the vials' capacity. Prints how often an engram asks for more enzymes than its recipe lists.
 */
class EngramRecipeSweepTest {
	private static final String[] TENDENCIES = {"animus", "flammeus", "ductilis", "lux", "mortem", "congeatio", "ferric", "tenebris"};
	private static final int WEAVES = Integer.getInteger("engramSweep.weaves", 200);

	@Test
	void everyRecipeIsCompletableForAnyWorldAndWeaver() throws Exception {
		List<String> ids = new ArrayList<>();
		List<int[]> counts = new ArrayList<>();
		try (var files = Files.list(Path.of("src/main/resources/data/hemomancy/recipe/memory_weaving"))) {
			for (Path file : files.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
				JsonObject enzymes = JsonParser.parseString(Files.readString(file)).getAsJsonObject().getAsJsonObject("enzymes");
				int[] c = new int[8];
				for (int t = 0; t < 8; t++) c[t] = enzymes.has(TENDENCIES[t]) ? enzymes.get(TENDENCIES[t]).getAsInt() : 0;
				ids.add("hemomancy:memory_weaving/" + file.getFileName().toString().replace(".json", ""));
				counts.add(c);
			}
		}
		SplittableRandom random = new SplittableRandom(20261015L);
		long boards = 0, failures = 0, overRecipe = 0, overCapacity = 0;
		int worstExtraEnzymes = 0;
		long[] overByRank = new long[5], boardsByRank = new long[5];
		List<String> examples = new ArrayList<>();
		long started = System.nanoTime();
		for (int r = 0; r < ids.size(); r++) {
			int[] c = counts.get(r);
			for (EnumManipulationRank rank : EnumManipulationRank.values())
				for (int w = 0; w < WEAVES; w++) {
					long seed = EngramBoardGenerator.seed(random.nextLong(), ids.get(r), new UUID(random.nextLong(), random.nextLong()));
					boards++;
					String where = ids.get(r) + " " + rank + " seed " + seed;
					try {
						EngramBoard board = EngramBoardGenerator.generate(seed, c, rank);
						// Fungal pressure boiling over before anything is threaded, as many times as a careless weaver might.
						int boil = rank == EnumManipulationRank.SUMMA || rank == EnumManipulationRank.MAGISTER ? 6 : 0;
						Random pressure = new Random(seed ^ 0x5DEECE66DL);
						for (int i = 0; i < boil; i++) EngramWeaveRules.applyPressure(board, pressure, firstColour(c), "decoy");
						int[] reference = board.reference();
						String problem = solve(board, reference, c);
						if (problem != null) {
							failures++;
							if (examples.size() < 10) examples.add(where + ": " + problem);
						}
						int extra = 0;
						for (int t = 0; t < 8; t++) {
							if (reference[t] > EngramWeaveRules.SPAN_CAPACITY) overCapacity++;
							extra = Math.max(extra, Math.ceilDiv(Math.max(0, reference[t] - c[t] * EngramWeaveRules.SPANS_PER_ENZYME),
									EngramWeaveRules.SPANS_PER_ENZYME));
						}
						boardsByRank[rank.ordinal()]++;
						if (extra > 0) { overRecipe++; overByRank[rank.ordinal()]++; }
						worstExtraEnzymes = Math.max(worstExtraEnzymes, extra);
					} catch (RuntimeException e) {
						failures++;
						if (examples.size() < 10) examples.add(where + ": " + e);
					}
				}
		}
		System.out.printf("SWEEP recipes=%d boards=%d failures=%d overCapacity=%d needMoreThanRecipe=%d (%.1f%%) worstExtraEnzymes=%d time=%.1fs%n",
				ids.size(), boards, failures, overCapacity, overRecipe, 100.0 * overRecipe / boards, worstExtraEnzymes,
				(System.nanoTime() - started) / 1e9);
		for (EnumManipulationRank k : EnumManipulationRank.values()) System.out.printf("SWEEP rank %s needMore=%.1f%%%n", k, 100.0 * overByRank[k.ordinal()] / boardsByRank[k.ordinal()]);
		examples.forEach(e -> System.out.println("SWEEP fail " + e));
		assertTrue(failures == 0 && overCapacity == 0, failures + " failures, " + overCapacity + " over capacity: " + examples);
	}

	private static int firstColour(int[] c) {
		for (int t = 0; t < 8; t++) if (c[t] > 0) return t;
		return 0;
	}

	/** Threads the board's reference from empty lanes with exactly its reference stock; null if it completes. */
	private static String solve(EngramBoard board, int[] stock, int[] counts) {
		for (int i = 0; i < board.fragmentCount(); i++) {
			if (board.fragment(i).intrusion()) continue;
			int[] path = board.solution(i);
			if (path == null) return "fragment " + i + " has no reference";
			for (int k = 1; k < path.length; k++) {
				EngramWeaveRules.Step step = EngramWeaveRules.extend(board, i, path[k], stock);
				if (step != EngramWeaveRules.Step.OK) return "fragment " + i + " step " + k + " refused: " + step;
			}
		}
		if (!EngramWeaveRules.complete(board, counts)) return "not complete after threading";
		if (!EngramWeaveRules.connectedIntrusions(board).isEmpty()) return "an intrusion was threaded home";
		return null;
	}
}
