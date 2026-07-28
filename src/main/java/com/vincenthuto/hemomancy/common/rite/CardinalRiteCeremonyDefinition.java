package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data-driven ceremony declaration attached to a Cardinal Rite recipe.
 */
public record CardinalRiteCeremonyDefinition(
		CardinalRiteCeremonyProfile profile,
		List<Anchor> anchors,
		List<SupportSocket> supportSockets,
		List<String> waves,
		List<String> guaranteedWaves,
		String signatureHandler,
		List<BlockPos> fragileOffsets) {

	private static final Set<String> ABBREVIATED_RITES = Set.of(
			"sanguine_attunement", "vascular_mending", "crimson_beacon", "hematic_fortification",
			"bloodline_recall", "pallid_vessel_rite", "crimson_vessel_rite", "ashen_vessel_rite",
			"horn_of_culmination_rite", "chamber_of_will", "ancestral_communion");

	public CardinalRiteCeremonyDefinition {
		profile = profile == null ? CardinalRiteCeremonyProfile.FULL : profile;
		anchors = List.copyOf(anchors == null ? List.of() : anchors);
		supportSockets = List.copyOf(supportSockets == null ? List.of() : supportSockets);
		waves = List.copyOf(waves == null ? List.of() : waves);
		guaranteedWaves = List.copyOf(guaranteedWaves == null ? List.of() : guaranteedWaves);
		signatureHandler = signatureHandler == null ? "" : signatureHandler;
		fragileOffsets = List.copyOf(fragileOffsets == null ? List.of() : fragileOffsets);
	}

	public int anchorBloodCostMl() {
		return anchors.size() * CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;
	}

	public boolean abbreviated() {
		return profile == CardinalRiteCeremonyProfile.ABBREVIATED;
	}

	/**
	 * Safe built-in conversion for older Harbinger recipe packs. Explicit
	 * ceremony JSON replaces this definition when present.
	 */
	public static CardinalRiteCeremonyDefinition convertedDefault(ResourceLocation recipeId, CardinalRiteType riteType,
			int degree) {
		String path = recipeId == null ? "unknown" : recipeId.getPath();
		int slash = path.lastIndexOf('/');
		if (slash >= 0) path = path.substring(slash + 1);
		CardinalRiteCeremonyCatalog.Spec authored = CardinalRiteCeremonyCatalog.spec(path);
		CardinalRiteCeremonyProfile profile = authored != null ? authored.profile() : ABBREVIATED_RITES.contains(path)
				? CardinalRiteCeremonyProfile.ABBREVIATED
				: CardinalRiteCeremonyProfile.FULL;
		int rings = Math.max(1, degree);
		int rotation = authored != null ? authored.rotation() : Math.floorMod(path.hashCode(), 4);
		List<Anchor> anchors = anchorsForLayout(rings, rotation,
				authored == null ? CardinalRiteCeremonyCatalog.Layout.CARDINAL : authored.layout());
		List<SupportSocket> sockets = List.of(
				new SupportSocket(rings + 3, 0, 0, "bastion"),
				new SupportSocket(-(rings + 3), 0, 0, "reservoir"),
				new SupportSocket(0, 0, rings + 3, "mnemonic"),
				new SupportSocket(0, 0, -(rings + 3), "hematic_lattice"));
		List<String> wavePool = List.of("bloodlicker_siphon", "fargone_dive", "rogue_will",
				"false_omens", "response_sigil");
		List<String> guaranteed = guaranteedSupportWave(riteType);
		String handler = signatureFor(path);
		List<BlockPos> fragile = List.of(new BlockPos(1, 0, 1), new BlockPos(-1, 0, -1),
				new BlockPos(1, 0, -1));
		return new CardinalRiteCeremonyDefinition(profile, anchors, sockets, wavePool, guaranteed, handler, fragile);
	}

	/**
	 * Generates the standard four-node-per-ring circulation for data-driven
	 * rites that select an authored layout family instead of listing every
	 * anchor coordinate by hand.
	 */
	public static List<Anchor> anchorsForLayout(int rings, int rotation,
			CardinalRiteCeremonyCatalog.Layout layout) {
		List<Anchor> anchors = new ArrayList<>();
		Set<BlockPos> occupied = new HashSet<>();
		for (int ring = 1; ring <= rings; ring++) {
			int radius = ring + 2;
			int diagonalX = Math.max(1, (int) Math.round(radius / Math.sqrt(2.0D)));
			int diagonalZ = diagonalX;
			while (layout == CardinalRiteCeremonyCatalog.Layout.DIAGONAL
					&& diagonalRingOverlaps(occupied, diagonalX, diagonalZ)) {
				diagonalZ++;
			}
			int[][] points = switch (layout) {
				case DIAGONAL -> new int[][] {{-diagonalX,-diagonalZ},{diagonalX,-diagonalZ},
						{diagonalX,diagonalZ},{-diagonalX,diagonalZ}};
				case CROOKED -> new int[][] {{0,-radius},{radius,1},{0,radius},{-radius,-1}};
				case SERPENTINE -> new int[][] {{-radius,-1},{0,-radius},{radius,1},{0,radius}};
				default -> new int[][] {{0,-radius},{radius,0},{0,radius},{-radius,0}};
			};
			for (int step = 0; step < 4; step++) {
				int index = (step + rotation) & 3;
				int[] rotated = rotateForRing(points[index][0], points[index][1], ring - 1);
				anchors.add(new Anchor(rotated[0], 1, rotated[1], ring - 1, step));
				occupied.add(new BlockPos(rotated[0], 0, rotated[1]));
			}
		}
		return anchors;
	}

	private static int[] rotateForRing(int x, int z, int ringIndex) {
		double angle = Math.max(0, ringIndex) * Math.PI / 4.0D;
		double cosine = Math.cos(angle);
		double sine = Math.sin(angle);
		return new int[] {
				(int) Math.round(x * cosine - z * sine),
				(int) Math.round(x * sine + z * cosine)
		};
	}

	private static boolean diagonalRingOverlaps(Set<BlockPos> occupied, int x, int z) {
		return occupied.contains(new BlockPos(-x, 0, -z))
				|| occupied.contains(new BlockPos(x, 0, -z))
				|| occupied.contains(new BlockPos(x, 0, z))
				|| occupied.contains(new BlockPos(-x, 0, z));
	}

	private static List<String> guaranteedSupportWave(CardinalRiteType type) {
		return switch (CardinalRiteCeremonyRules.formIndex(type)) {
			case 0 -> List.of("discover_reservoir");
			case 1 -> List.of("discover_bastion");
			case 2 -> List.of("discover_mnemonic");
			default -> List.of("discover_hematic_lattice");
		};
	}

	private static String signatureFor(String path) {
		return switch (path) {
			case "sanguine_initiation" -> "first_circulation";
			case "votary_rite" -> "tendency_response";
			case "initiate_rite" -> "blood_memory";
			case "sanguine_brotherhood" -> "simulacrum_wound";
			case "illuminatus_rite" -> "fungal_root";
			case "sanctified_rite" -> "covenant_currents";
			case "archon_rite" -> "trifold_judgment";
			case "apotheos_rite" -> "reverse_circulation";
			default -> path;
		};
	}

	public record Anchor(int x, int y, int z, int ring, int order) {
		public BlockPos offset() {
			return new BlockPos(x, y, z);
		}
	}

	public record SupportSocket(int x, int y, int z, String suggestedSigil) {
		public BlockPos offset() {
			return new BlockPos(x, y, z);
		}
	}
}
