package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

final class LivingStaffMorphWiringSourceTest {

	@Test
	void everySharedWeaponFormChangeBroadcastsTheMorph() throws IOException {
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/"
				+ "LivingStaffWeaponFormHelper.java");

		assertTrue(helper.contains("PacketHandler.sendTracking("),
				"shared weapon-form mutations must broadcast their before/after stacks");
		assertTrue(helper.contains("syncMorph(player, beforeMain, beforeOff)"),
				"selection-driven swaps must route through the shared morph broadcaster");
	}

	@Test
	void morphRendererAddsTheExistingBloodMeltPass() throws IOException {
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/harbinger/"
				+ "LivingStaffMorphRenderer.java");
		String flail = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/harbinger/"
				+ "LivingFlailRenderHelper.java");

		assertTrue(renderer.contains("HemoRenderTypes.cardinalStaffBloodMelt"),
				"morphing weapons must use the same red melt shader as the Cardinal Rite staff");
		assertFalse(renderer.contains("VertexMultiConsumer"),
				"a shared BufferSource cannot build two morph render types simultaneously");
		assertFalse(flail.contains("VertexMultiConsumer"),
				"the flail melt must also render its passes in order");
	}
}
