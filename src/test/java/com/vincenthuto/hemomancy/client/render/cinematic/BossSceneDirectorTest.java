package com.vincenthuto.hemomancy.client.render.cinematic;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.CameraType;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossSceneDirectorTest {
	private static final ResourceLocation PLAIN = Hemomancy.rloc("test_plain");
	private static final ResourceLocation BOUND = Hemomancy.rloc("test_bound");
	private static final ResourceLocation SHOT = Hemomancy.rloc("test_shot");

	private BossSceneDirector director;

	@BeforeEach
	void setUp() {
		BossSceneRegistry.register(PLAIN, () -> new Scene(60, false, null));
		BossSceneRegistry.register(BOUND, () -> new Scene(100, true, null));
		BossSceneRegistry.register(SHOT, () -> new Scene(60, false,
				CinematicShot.of(new CinematicShot.Key(0, 0, 8, 2, 1))));
		director = new BossSceneDirector(id -> null);
	}

	@Test
	void startsAtOffset() {
		director.start(PLAIN, 1, 15);
		assertTrue(director.active());
		assertEquals(15.0F, director.time(0.0F), 1.0E-5F);
	}

	@Test
	void endsAfterLength() {
		director.start(PLAIN, 1, 0);
		for (int i = 0; i < 60; i++) director.tick();
		assertTrue(director.active(), "still on its last tick");
		director.tick();
		assertFalse(director.active());
	}

	@Test
	void replaceRestartsClock() {
		director.start(PLAIN, 1, 0);
		for (int i = 0; i < 30; i++) director.tick();
		director.start(BOUND, 2, 0);
		assertEquals(0.0F, director.time(0.0F), 1.0E-5F);
		assertTrue(director.inputLocked(0.0F));
	}

	@Test
	void unknownSceneIsIgnored() {
		director.start(Hemomancy.rloc("no_such_scene"), 1, 0);
		assertFalse(director.active());
	}

	@Test
	void holdingSneakTwentyTicksSkips() {
		director.start(PLAIN, 1, 0);
		for (int i = 0; i < 19; i++) {
			director.holdSkip(true);
			director.tick();
		}
		assertTrue(director.presenting());
		director.holdSkip(true);
		assertFalse(director.presenting(), "skipped after twenty held ticks");
		assertTrue(director.active(), "the scene's clock still runs for the server's sake");
	}

	@Test
	void releasingSneakResetsTheSkip() {
		director.start(PLAIN, 1, 0);
		for (int i = 0; i < 15; i++) director.holdSkip(true);
		director.holdSkip(false);
		for (int i = 0; i < 15; i++) director.holdSkip(true);
		assertTrue(director.presenting());
	}

	@Test
	void skipKeepsInputWhenSceneSaysSo() {
		director.start(BOUND, 1, 0);
		for (int i = 0; i < 20; i++) director.holdSkip(true);
		assertFalse(director.presenting());
		assertTrue(director.inputLocked(0.0F), "the server still binds the player");
		for (int i = 0; i < 60; i++) director.tick();
		assertFalse(director.inputLocked(0.0F), "freed when the scene's binding ends");
	}

	@Test
	void missingBossFallsBackToPlayerView() {
		director.start(SHOT, 1, 0);
		assertFalse(director.usesShot(), "no boss to frame, so the player keeps their own view");
	}

	@Test
	void restoresCameraTypeOnEnd() {
		director.start(SHOT, 1, 0);
		assertEquals(CameraType.THIRD_PERSON_BACK, director.holdCamera(CameraType.FIRST_PERSON));
		assertEquals(CameraType.THIRD_PERSON_BACK, director.holdCamera(CameraType.THIRD_PERSON_BACK), "holding twice keeps the first");
		for (int i = 0; i <= 60; i++) director.tick();
		assertEquals(CameraType.FIRST_PERSON, director.releaseCamera());
		assertNull(director.releaseCamera(), "released once");
	}

	@Test
	void aDimensionChangeEndsAnOrdinaryScene() {
		director.start(PLAIN, 1, 0);
		director.playerCloned();
		assertFalse(director.active());
	}

	@Test
	void clearRestoresEverything() {
		director.start(BOUND, 1, 0);
		director.holdCamera(CameraType.FIRST_PERSON);
		director.clear();
		assertFalse(director.active());
		assertFalse(director.inputLocked(0.0F));
		assertFalse(director.presenting());
		assertEquals(CameraType.FIRST_PERSON, director.releaseCamera(), "the held camera still comes back after a clear");
	}

	private record Scene(int length, boolean binds, CinematicShot shot) implements BossScene {
		@Override
		public boolean inputLocked(float t) {
			return binds && t < 80;
		}

		@Override
		public boolean keepsInputAfterSkip(float t) {
			return binds && t < 60;
		}
	}
}
