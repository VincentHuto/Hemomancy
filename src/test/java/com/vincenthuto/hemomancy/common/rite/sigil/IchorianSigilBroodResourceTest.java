package com.vincenthuto.hemomancy.common.rite.sigil;

import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IchorianSigilBroodResourceTest {
	private static final Path ROOT =
			Path.of("src/main/resources/data/hemomancy/ichorian_sigil");
	private static final Map<String, Integer> NODE_COUNTS = Map.of(
			"reservoir", 4, "suture", 4, "bastion", 6,
			"shunt", 5, "mnemonic", 8, "seal", 6,
			"cage", 7, "hematic_lattice", 12, "lens", 9);
	private static final Map<String, IchorianSigilAnatomy.Style> STYLES = Map.of(
			"reservoir", IchorianSigilAnatomy.Style.PENDULOUS_AMPULLA,
			"suture", IchorianSigilAnatomy.Style.NEEDLE_THREAD,
			"bastion", IchorianSigilAnatomy.Style.CONTRACTILE_SHIELD,
			"shunt", IchorianSigilAnatomy.Style.ARTERIAL_FORK,
			"mnemonic", IchorianSigilAnatomy.Style.RECALL_RIBBON,
			"seal", IchorianSigilAnatomy.Style.FIVE_LIPPED_SHUTTER,
			"cage", IchorianSigilAnatomy.Style.WALKING_RIB_TOWER,
			"hematic_lattice", IchorianSigilAnatomy.Style.VASCULAR_ARBOR,
			"lens", IchorianSigilAnatomy.Style.OPTIC_STALK_VEIL);

	@Test
	void everyBuiltInSigilHasACompleteDistinctBroodRig() throws IOException {
		Set<IchorianSigilAnatomy.Style> seen = new HashSet<>();
		for (String path : NODE_COUNTS.keySet()) {
			IchorianSigilDefinition definition = load(path);
			assertEquals(NODE_COUNTS.get(path), definition.nodes().size(), path);
			assertEquals(NODE_COUNTS.get(path) * 50, definition.bloodCostMl(), path);
			IchorianSigilAnatomy anatomy = definition.awakenedForm().orElseThrow(
					() -> new AssertionError(path + " lacks a valid awakened form"));
			assertEquals(STYLES.get(path), anatomy.animation().style(), path);
			assertEquals(definition.nodes().size(), anatomy.landmarks().size(), path);
			assertTrue(seen.add(anatomy.animation().style()), path + " repeats a caste style");
			for (IchorianSigilDefinition.Connection edge : definition.connections()) {
				assertTrue(edge.from() >= 0 && edge.from() < definition.nodes().size(), path);
				assertTrue(edge.to() >= 0 && edge.to() < definition.nodes().size(), path);
			}
		}
		assertEquals(9, seen.size());
	}

	@Test
	void everyBuiltInSigilNodeHasItsOwnInteractableGroundCell() throws IOException {
		for (String path : NODE_COUNTS.keySet()) {
			IchorianSigilDefinition definition = load(path);
			Set<String> cells = new HashSet<>();
			for (IchorianSigilDefinition.Node node : definition.nodes()) {
				String cell = Math.round(node.x()) + "," + Math.round(node.z());
				assertTrue(cells.add(cell), path + " overlaps an interactable node at " + cell);
			}
		}
	}

	private static IchorianSigilDefinition load(String path) throws IOException {
		try (var reader = Files.newBufferedReader(ROOT.resolve(path + ".json"))) {
			return IchorianSigilLoader.parseDefinition(
					ResourceLocation.fromNamespaceAndPath("hemomancy", path),
					JsonParser.parseReader(reader).getAsJsonObject());
		}
	}
}
