package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class MaterialRenderSnapshotTest {

	@Test
	void familyChangesRebuildMatchingNodesAndPositionsTogether() {
		MaterialAtlasNode bone = node("bone", "remains");
		MaterialAtlasNode iron = node("iron", "metals");
		LinkedHashMap<MaterialAtlasNode, int[]> positions = new LinkedHashMap<>();
		positions.put(bone, new int[] {10, 20});
		positions.put(iron, new int[] {30, 40});

		MaterialRenderSnapshot snapshot = MaterialRenderSnapshot.filter(
				List.of(bone, iron), positions, family -> family.equals("metals"));

		assertEquals(List.of(iron), snapshot.nodes());
		assertEquals(List.of(iron), List.copyOf(snapshot.positions().keySet()));
		assertTrue(snapshot.positions().get(iron)[0] == 30);
	}

	private static MaterialAtlasNode node(String id, String family) {
		MaterialAtlasBucket bucket = new MaterialAtlasBucket(
				MaterialAtlasPath.HARBINGER, family, family, family, 0, 0, 0, 0, 0);
		MaterialAtlasEntry atlasEntry = new MaterialAtlasEntry(MaterialAtlasPath.HARBINGER,
				id, bucket, MaterialGate.always(), 0, List.of(), 0, 0);
		MaterialEntry entry = new MaterialEntry(id, id, "", family, () -> ItemStack.EMPTY);
		return new MaterialAtlasNode(entry, atlasEntry, MaterialVisibility.UNLOCKED);
	}
}
