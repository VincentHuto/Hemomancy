package com.vincenthuto.hemomancy.client.render.item;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class HelmetItemRendererPoseSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/item");

	private HelmetItemRendererPoseSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		assertHeadpieceRendererPose("hemolymphopoda headpiece",
				read(SOURCE_ROOT.resolve("HemolymphopodaHeadpieceItemRenderer.java")));
		assertHeadpieceRendererPose("lantern tick helmet", read(SOURCE_ROOT.resolve("LanternTickHelmetItemRenderer.java")));
	}

	private static void assertHeadpieceRendererPose(String label, String source) {
		assertContains(label + " should reuse armor head item transform", source,
				"ArmorItemDisplayTransformHelper.apply(context, EquipmentSlot.HEAD, poseStack)");
		assertContains(label + " should yaw toward the player in GUI", source, "Axis.YP.rotationDegrees");
		assertContains(label + " should roll slightly in GUI", source, "Axis.ZP.rotationDegrees");
		assertNotContains(label + " should not use the old raw quaternion item pose", source,
				"new Quaternion(Vector3.YP");
	}
}
