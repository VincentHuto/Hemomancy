package com.vincenthuto.hemomancy.client.event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UnstainedWeaponSwingAnimationSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final Path HANDLER = ROOT.resolve(
			"src/main/java/com/vincenthuto/hemomancy/client/event/UnstainedWeaponSwingAnimationHandler.java");

	private UnstainedWeaponSwingAnimationSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String handler = read(HANDLER);

		assertContains("handler still routes glaive swings",
				handler, "applyGlaiveSwing(poseStack, swing, side);");
		assertContains("handler still routes hammer swings",
				handler, "applyWarhammerSwing(poseStack, swing, side);");
		assertContains("handler still routes dagger swings",
				handler, "applyDaggerSwing(poseStack, swing, side);");

		assertContains("warhammer swing should hold the windup longer before dropping",
				handler, "float windup = 1.0F - smoothStep(Mth.clamp(swing / 0.68F, 0.0F, 1.0F));");
		assertContains("warhammer swing should delay the slam for a heavier hit",
				handler, "float slamPhase = smoothStep(Mth.clamp((swing - 0.58F) / 0.42F, 0.0F, 1.0F));");
		assertContains("warhammer swing should stay out of screen center while winding up",
				handler, "poseStack.translate(side * 0.05F * windup, 0.18F * windup - 0.34F * slamPhase,");
		assertContains("warhammer swing should finish as a strong downward strike",
				handler, "Axis.XP.rotationDegrees(58.0F * windup - 178.0F * slamPhase - 10.0F * body)");

		assertContains("glaive swing should travel right and up during the sweep",
				handler, "poseStack.translate(side * 0.14F * sweep, 0.10F * arc, -0.06F * sweep);");
		assertContains("glaive swing should rotate into the up-right sweep",
				handler, "Axis.YP.rotationDegrees(side * 46.0F * arc)");
		assertContains("glaive swing should have a wide rotating cut",
				handler, "Axis.ZP.rotationDegrees(side * 62.0F * sweep)");

		assertContains("dagger swing should keep its working upward thrust",
				handler, "Axis.XP.rotationDegrees(48.0F * thrust + 18.0F * lift)");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " missing: " + expected);
		}
	}
}
