package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class SummonsPreviewSourceTest {

	private SummonsPreviewSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String view = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabView.java");
		String bodies = read("src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerSummonBodies.java");
		String controller = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabController.java");

		assertNotContains("summons preview should not use limited follow-mouse entity renderer",
				view, "renderEntityInInventoryFollowsMouse");
		assertNotContains("summons preview should not synthesize a cosine mouse position",
				view, "Mth.cos(state.previewRotationAngle)");
		assertContains("summons preview should render from explicit yaw",
				view, "float yaw = 180.0F + (float) Math.toDegrees(state.previewRotationAngle)");
		assertContains("summons preview should use direct entity inventory renderer",
				view, "InventoryScreen.renderEntityInInventory(gfx");
		assertContains("summons preview should restore entity rotations after preview rendering",
				view, "restorePreviewRotations(entity, rotations)");
		assertContains("summons preview should track preview scissor state",
				view, "boolean scissorEnabled = false");
		assertContains("summons preview should always release preview scissor before fallback drawing",
				view, "if (scissorEnabled) {");
		assertContains("summons preview should use height-aware render scale",
				view, "int scale = previewEntityScale(entity, bottom - top)");
		assertContains("summons preview should raise entity center to keep feet visible",
				view, "int centerY = top + Math.round((bottom - top) * 0.56F)");
		assertContains("summons preview should leave bottom room for tall mobs",
				view, "private static int previewEntityScale(LivingEntity entity, int availableH)");
		assertContains("preview bodies should come from the shared summon body table",
				view, "PuppeteerSummonBodies.create(definition.name(), mc.level)");
		assertContains("scarlet mummer should have a preview entity",
				bodies, "PuppeteerSummonDefinitions.SCARLET_MUMMER, EntityInit.scarlet_mummer");
		assertContains("sanguine hound should have a preview entity",
				bodies, "PuppeteerSummonDefinitions.SANGUINE_HOUND, EntityInit.sanguine_hound");
		assertContains("ringmaster pattern should preview its own entity type",
				bodies, "PuppeteerSummonDefinitions.RINGMASTER_PATTERN, EntityInit.ringmaster_pattern");
		assertContains("preview must retain the summon identity for shared entity types",
				view, "bound.hemomancy$setSummonName(definition.name())");
		assertContains("summons auto spin should be half the prior speed",
				controller, "PREVIEW_AUTO_ROTATION_SPEED = 0.175f");
		assertContains("summons drag spin should be half the prior sensitivity",
				controller, "PREVIEW_DRAG_ROTATION_SPEED = 0.02f");
		assertContains("summons auto spin should use the named speed",
				controller, "state.previewRotationAngle += partial * PREVIEW_AUTO_ROTATION_SPEED");
		assertContains("summons drag spin should use the named sensitivity",
				controller, "* PREVIEW_DRAG_ROTATION_SPEED");
	}
}
