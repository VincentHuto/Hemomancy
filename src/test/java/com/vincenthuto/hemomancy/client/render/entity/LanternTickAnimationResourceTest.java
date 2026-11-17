package com.vincenthuto.hemomancy.client.render.entity;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class LanternTickAnimationResourceTest {
	private static final Path SRC = Path.of("src/main/java/com/vincenthuto/hemomancy");

	private LanternTickAnimationResourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String entity = read(SRC.resolve("common/entity/mob/arthropod/LanternTickEntity.java"));
		String model = read(SRC.resolve("client/model/entity/mob/arthropod/LanternTickModel.java"));
		String layer = read(SRC.resolve("client/render/layer/player/LanternTickHelmetLayer.java"));

		assertContains("entity syncs animation state", entity, "ANIMATION_STATE");
		assertContains("entity exposes lure state", entity, "ANIMATION_LURE");
		assertContains("entity exposes stalking state", entity, "ANIMATION_STALKING");
		assertContains("entity exposes leaping state", entity, "ANIMATION_LEAPING");
		assertContains("entity exposes latched state", entity, "ANIMATION_LATCHED");
		assertContains("entity exposes bite pulse", entity, "BITE_ANIMATION_TICKS");
		assertContains("model has abdomen glow part", model, "abdomenGlow");
		assertContains("model legs roll outward instead of folding in", model, "LEG_OUTWARD_Z_ROT");
		assertContains("model keeps top abdomen glow above shell", model,
				"addBox(-3.25F, -4.05F, -2.25F, 6.5F, 0.25F, 9.0F");
		assertNotContains("model does not use angler lure part", model, "root.addOrReplaceChild(\"lure\"");
		assertNotContains("model does not animate a lure stalk", model, "this.lure");
		assertNotContains("abdomen glow does not rotate into body", model, "abdomenGlow.xRot = Mth.sin");
		assertContains("model animates lure", model, "applyLureAnimation");
		assertContains("model animates stalking", model, "applyStalkingAnimation");
		assertContains("model animates leaping", model, "applyLeapingAnimation");
		assertContains("model animates latched", model, "applyLatchedAnimation");
		assertContains("model animates bite", model, "applyBitePulse");
		assertContains("helmet layer keeps tick on top of head", layer, "HEAD_Y_OFFSET = -1.25F");
		assertContains("helmet layer avoids face placement", layer, "HEAD_Z_OFFSET = -0.02F");
	}
}
