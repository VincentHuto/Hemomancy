package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

final class LivingStaffMorphWiringSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void everySharedWeaponFormChangeBroadcastsTheMorph() throws IOException {
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/"
				+ "LivingStaffWeaponFormHelper.java");

		assertTrue(helper.contains("PacketDistributor.sendToPlayersTrackingEntityAndSelf"),
				"shared weapon-form mutations must broadcast their before/after stacks");
		assertTrue(helper.contains("syncMorph(player, beforeMain, beforeOff)"),
				"selection-driven swaps must route through the shared morph broadcaster");
	}

	@Test
	void morphRendererAddsTheExistingBloodMeltPass() throws IOException {
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/"
				+ "LivingStaffMorphRenderer.java");
		String flail = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/"
				+ "LivingFlailRenderHelper.java");

		assertTrue(renderer.contains("HemoRenderTypes.cardinalStaffBloodMelt"),
				"morphing weapons must use the same red melt shader as the Cardinal Rite staff");
		assertFalse(renderer.contains("VertexMultiConsumer"),
				"a shared BufferSource cannot build two morph render types simultaneously");
		assertFalse(flail.contains("VertexMultiConsumer"),
				"the flail melt must also render its passes in order");
	}

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath)).replace("\r\n", "\n");
	}
}
