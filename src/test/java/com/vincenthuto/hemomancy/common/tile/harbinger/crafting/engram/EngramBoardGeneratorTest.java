package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard.NodeKind;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EngramBoardGeneratorTest {
	private static final String[] TENDENCIES = {"animus", "flammeus", "ductilis", "lux", "mortem", "congeatio", "ferric", "tenebris"};

	/** Every distinct enzyme profile the authored weaving recipes ask for. */
	private static List<int[]> profiles() throws IOException {
		Set<String> seen = new LinkedHashSet<>();
		List<int[]> out = new ArrayList<>();
		try (var files = Files.list(Path.of("src/main/resources/data/hemomancy/recipe/memory_weaving"))) {
			for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
				if (!json.has("enzymes")) continue;
				JsonObject enzymes = json.getAsJsonObject("enzymes");
				int[] counts = new int[8];
				for (int t = 0; t < 8; t++) counts[t] = enzymes.has(TENDENCIES[t]) ? enzymes.get(TENDENCIES[t]).getAsInt() : 0;
				if (seen.add(java.util.Arrays.toString(counts))) out.add(counts);
			}
		}
		assertFalse(out.isEmpty());
		return out;
	}

	private static int total(int[] counts) {
		int sum = 0;
		for (int c : counts) sum += c;
		return sum;
	}

	/** Threads the generator's own solution from empty lanes with exactly the reference stock. */
	private static void solve(EngramBoard board) {
		int[] stock = board.reference();
		for (int i = 0; i < board.fragmentCount(); i++) {
			int[] path = board.solution(i);
			if (board.fragment(i).intrusion()) {
				assertNull(path, "intrusions have no reference path");
				continue;
			}
			assertNotNull(path, "fragment " + i + " has a reference path");
			for (int k = 1; k < path.length; k++)
				assertEquals(EngramWeaveRules.Step.OK, EngramWeaveRules.extend(board, i, path[k], stock),
						"step " + k + " of fragment " + i);
		}
	}

	@Test
	void everyRecipeProfileAtEveryRankGeneratesASolvableBoard() throws IOException {
		for (int[] counts : profiles())
			for (EnumManipulationRank rank : EnumManipulationRank.values())
				for (long seed = 0; seed < 16; seed++) {
					EngramBoard board = EngramBoardGenerator.generate(seed, counts, rank);
					String where = java.util.Arrays.toString(counts) + " " + rank + " seed " + seed;
					solve(board);
					assertTrue(EngramWeaveRules.complete(board, counts), "solved: " + where);
					assertTrue(EngramWeaveRules.connectedIntrusions(board).isEmpty(), "no intrusion threaded: " + where);
					int[] reference = board.reference();
					for (int t = 0; t < 8; t++) {
						assertEquals(EngramWeaveRules.committed(board, t), reference[t], "reference spans: " + where);
						assertTrue(reference[t] <= EngramWeaveRules.SPAN_CAPACITY, where);
					}
				}
	}

	@Test
	void aHumilisEngramCostsNoMoreThanItsShortestLanes() throws IOException {
		// The first lessons hand over exactly the recipe's enzymes: three spans per fragment must always suffice.
		for (int[] counts : profiles())
			for (long seed = 0; seed < 16; seed++) {
				EngramBoard board = EngramBoardGenerator.generate(seed, counts, EnumManipulationRank.HUMILIS);
				for (int t = 0; t < 8; t++)
					assertTrue(board.reference()[t] <= counts[t] * EngramWeaveRules.SPANS_PER_ENZYME,
							java.util.Arrays.toString(counts) + " seed " + seed + " tendency " + t + " costs " + board.reference()[t]);
			}
	}

	@Test
	void mostEngramsAreAffordableWithTheRecipesOwnEnzymes() throws IOException {
		// The reference gates payment, so it is shortened lane by lane once the board is dressed. A long walk keeps a
		// puzzle's fragments apart, so some engrams ask an enzyme or two more; the loom names the exact need first.
		int boards = 0, short_ = 0;
		for (int[] counts : profiles())
			for (EnumManipulationRank rank : EnumManipulationRank.values())
				for (long seed = 0; seed < 16; seed++) {
					EngramBoard board = EngramBoardGenerator.generate(seed * 7919L + 13L, counts, rank);
					boards++;
					for (int t = 0; t < 8; t++)
						if (board.reference()[t] > counts[t] * EngramWeaveRules.SPANS_PER_ENZYME) {
							short_++;
							break;
						}
				}
		assertTrue(short_ <= boards / 4, short_ + " of " + boards + " engrams need more than the recipe's enzymes");
	}

	@Test
	void theSameSeedLaysTheSameEngram() {
		int[] counts = {2, 0, 1, 0, 0, 0, 0, 0};
		for (EnumManipulationRank rank : EnumManipulationRank.values())
			assertEquals(EngramBoardGenerator.generate(7, counts, rank).save(), EngramBoardGenerator.generate(7, counts, rank).save());
		assertNotEquals(EngramBoardGenerator.generate(7, counts, EnumManipulationRank.SUMMA).save(),
				EngramBoardGenerator.generate(8, counts, EnumManipulationRank.SUMMA).save());
	}

	@Test
	void intrusionsGrowWithRankAndWearARecipeColour() throws IOException {
		for (int[] counts : profiles())
			for (long seed = 0; seed < 8; seed++) {
				assertEquals(0, intrusions(EngramBoardGenerator.generate(seed, counts, EnumManipulationRank.HUMILIS)));
				for (EnumManipulationRank rank : EnumManipulationRank.values()) {
					EngramBoard board = EngramBoardGenerator.generate(seed, counts, rank);
					int n = intrusions(board);
					int[] range = EngramBoardGenerator.intrusionRange(rank);
					assertTrue(n <= range[1], rank + " " + n);
					if (total(counts) <= 6) assertTrue(n >= range[0], rank + " has its intrusions on a small engram: " + n);
					for (EngramBoard.Fragment f : board.fragments())
						if (f.intrusion()) {
							assertTrue(f.colour() >= 0 && counts[f.colour()] > 0, "an intrusion looks like a real fragment");
							assertEquals(f.colour(), f.trueColour());
						}
				}
			}
	}

	private static int intrusions(EngramBoard board) {
		return (int) board.fragments().stream().filter(EngramBoard.Fragment::intrusion).count();
	}

	@Test
	void lowRanksAreOpenAndHighRanksKeyTheirFaces() {
		int[] counts = {1, 1, 0, 0, 0, 0, 0, 0};
		for (long seed = 0; seed < 16; seed++) {
			EngramBoard humilis = EngramBoardGenerator.generate(seed, counts, EnumManipulationRank.HUMILIS);
			assertFalse(humilis.keyed());
			for (int n = 0; n < EngramSigilGeometry.NODES; n++)
				assertTrue(humilis.kind(n) == NodeKind.OPEN || humilis.kind(n) == NodeKind.SCAR, "Humilis has only scars");
			assertTrue(EngramBoardGenerator.generate(seed, counts, EnumManipulationRank.SUMMA).keyed());
			assertTrue(EngramBoardGenerator.generate(seed, counts, EnumManipulationRank.PERFECTUS).keyed());
		}
	}

	@Test
	void scarsNeverFallOnTheReferencePath() throws IOException {
		for (int[] counts : profiles())
			for (EnumManipulationRank rank : EnumManipulationRank.values()) {
				EngramBoard board = EngramBoardGenerator.generate(3, counts, rank);
				int scars = 0;
				for (int n = 0; n < EngramSigilGeometry.NODES; n++)
					if (board.kind(n) == NodeKind.SCAR) {
						scars++;
						assertFalse(board.onSolution(n), "scar on the solution at " + n);
					}
				if (total(counts) <= 6) assertTrue(scars >= 1, "a small engram has at least one scar: " + rank);
			}
	}

	@Test
	void magisterAndPerfectusHideAFragmentsColourBehindAPrism() {
		int[] counts = {2, 0, 0, 1, 0, 0, 0, 0};
		for (EnumManipulationRank rank : new EnumManipulationRank[]{EnumManipulationRank.MAGISTER, EnumManipulationRank.PERFECTUS})
			for (long seed = 0; seed < 16; seed++) {
				EngramBoard board = EngramBoardGenerator.generate(seed, counts, rank);
				int greys = 0;
				for (int i = 0; i < board.fragmentCount(); i++) {
					EngramBoard.Fragment f = board.fragment(i);
					if (!f.grey()) continue;
					greys++;
					boolean dyed = false;
					for (int node : board.solution(i))
						dyed |= board.kind(node) == NodeKind.PRISM && board.tint(node) == f.trueColour();
					assertTrue(dyed, "a grey fragment's path passes its own prism");
				}
				assertTrue(greys >= 1 && greys <= 2, rank + " greys " + greys);
			}
		assertEquals(0, EngramBoardGenerator.generate(1, counts, EnumManipulationRank.SUMMA).fragments().stream()
				.filter(EngramBoard.Fragment::grey).count());
	}

	@Test
	void largeRecipesSpillOntoTheNotches() {
		int[] counts = {2, 2, 1, 1, 2, 1, 2, 1};
		EngramBoard board = EngramBoardGenerator.generate(4, counts, EnumManipulationRank.HUMILIS);
		assertTrue(board.fragments().stream().anyMatch(f -> EngramSigilGeometry.isNotch(f.node())));
	}

	@Test
	void theSeedIsTheWeaversOwn() {
		UUID a = UUID.randomUUID(), b = UUID.randomUUID();
		String recipe = "hemomancy:memory_weaving/memory_blood_shot".toLowerCase(Locale.ROOT);
		assertEquals(EngramBoardGenerator.seed(42L, recipe, a), EngramBoardGenerator.seed(42L, recipe, a));
		assertNotEquals(EngramBoardGenerator.seed(42L, recipe, a), EngramBoardGenerator.seed(42L, recipe, b));
		assertNotEquals(EngramBoardGenerator.seed(42L, recipe, a), EngramBoardGenerator.seed(43L, recipe, a));
	}
}
