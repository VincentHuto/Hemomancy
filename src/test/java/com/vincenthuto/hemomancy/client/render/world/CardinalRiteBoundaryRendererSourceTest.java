package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CardinalRiteBoundaryRendererSourceTest {
	private static final Path RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/"
					+ "CardinalRiteBoundaryRenderer.java");

	@Test
	void transparentStainsUseTheColorOnlyPassInsteadOfWritingDepth() throws IOException {
		String source = read();

		assertTrue(source.contains("if (pass == Pass.GLOW && !frame.legacy && hasAnchorSockets) {\n"
						+ "\t\t\tfor (RingFrame ring : frame.rings) drawBoundaryFloorStain(vc, frame, ring);"),
				"boundary stain is drawn only on the color-only glow pass");
		assertTrue(source.contains("RenderTypeInit.RITE_BOUNDARY_GLOW);\n"
						+ "\t\tfor (RiteFrame frame : frames) {\n"
						+ "\t\t\tdrawRite(poseStack, glowVC, Pass.GLOW"),
				"the glow pass draws into the color-only render type");
	}

	@Test
	void removedRecipeFromAnOldWorldFallsBackToPlainBoundaryGeometry() throws IOException {
		String source = read();

		assertTrue(source.contains("CardinalRiteCeremonyDefinition ceremony = recipe == null"),
				"renderer must tolerate an active rite whose recipe was removed by a datapack update");
		assertTrue(source.contains("hasAnchorSockets ? layout.startClearance(segment) : 0.0D"),
				"missing ceremony should use zero-clearance boundary arcs");
		assertTrue(source.contains("if (pass == Pass.GLOW && !frame.legacy && hasAnchorSockets)"),
				"ceremony-authored floor stains must be skipped when recipe data is unavailable");
	}

	@Test
	void plantedStaffShowsTheRiteOwnersFittingNotTheViewers() throws IOException {
		String source = read();

		assertTrue(source.contains("staffVisual(mc, rite.getOwner())"), "staff visual comes from the rite owner");
		assertFalse(source.contains("staffVisualFor(mc.player)"), "the viewer's fitting must not be used");
		assertFalse(source.contains("new ItemStack(ItemInit.living_staff.get());\n\t\tint visual"),
				"the staff stack is not rebuilt every frame");
	}

	@Test
	void sealSoundsUseTheRiteSoundSetInsteadOfVanillaStandIns() throws IOException {
		String source = read();

		assertTrue(source.contains("SoundInit.RITE_SEGMENT_SEAL"), "segment seal uses the rite sound");
		assertFalse(source.contains("WARDEN_HEARTBEAT"), "no vanilla warden heartbeat");
		assertFalse(source.contains("RESPAWN_ANCHOR_CHARGE"), "no vanilla respawn anchor charge");
	}

	@Test
	void endingsPlayFromTheRiteGhostInsteadOfPoppingOut() throws IOException {
		String source = read();

		assertTrue(source.contains("CardinalRiteEventClientState.ghosts()"), "ghost endings are rendered");
		assertTrue(source.contains("CardinalRiteEndingRules.rupture("), "collapse tears the rings apart");
		assertTrue(source.contains("CardinalRiteEndingRules.staffMeltTicks("), "the staff melts on endings");
	}

	@Test
	void ringsBeatOnTheSharedRiteHeartbeat() throws IOException {
		String source = read();

		assertTrue(source.contains("CardinalRiteMath.heartbeat(time)"), "rings use the floor's lub-dub");
		assertFalse(source.contains("IchorianSigilOrganicGeometry.heartbeat"),
				"no separate sine heartbeat for the boundary");
	}

	private static String read() throws IOException {
		return Files.readString(RENDERER).replace("\r\n", "\n");
	}
}
