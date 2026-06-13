package com.vincenthuto.hemomancy.client.render.item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HelmetItemRendererPoseSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/item");

	private HelmetItemRendererPoseSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertHeadpieceRendererPose("hemolymphopoda headpiece",
				read(SOURCE_ROOT.resolve("HemolymphopodaHeadpieceItemRenderer.java")));
		assertHeadpieceRendererPose("lantern tick helmet", read(SOURCE_ROOT.resolve("LanternTickHelmetItemRenderer.java")));
	}

	private static void assertHeadpieceRendererPose(String label, String source) {
		assertContains(label + " should reuse armor head item transform", source,
				"ArmorItemDisplayTransformHelper.apply(context, EquipmentSlot.HEAD, poseStack)");
		assertContains(label + " should yaw toward the player in GUI", source, "Axis.YP.rotationDegrees");
		assertContains(label + " should roll slightly in GUI", source, "Axis.ZP.rotationDegrees");
		assertDoesNotContain(label + " should not use the old raw quaternion item pose", source,
				"new Quaternion(Vector3.YP");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String source, String unexpected) {
		if (source.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}
