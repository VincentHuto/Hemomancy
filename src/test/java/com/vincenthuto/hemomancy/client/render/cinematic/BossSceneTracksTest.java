package com.vincenthuto.hemomancy.client.render.cinematic;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossSceneTracksTest {
	@Test
	void keysAreFlatOutsideAndSmoothBetween() {
		assertEquals(2.0F, BossSceneTracks.keys(-5, 0, 2, 10, 6), 1.0E-5F);
		assertEquals(6.0F, BossSceneTracks.keys(50, 0, 2, 10, 6), 1.0E-5F);
		assertEquals(4.0F, BossSceneTracks.keys(5, 0, 2, 10, 6), 1.0E-5F);
	}

	@Test
	void titleCardFadesTwelveTicks() {
		TitleCard card = new TitleCard("n", "e", 100, 40, 0xFFFFFF);
		assertEquals(0.0F, card.alpha(99), 1.0E-5F);
		assertEquals(0.0F, card.alpha(100), 1.0E-5F);
		assertEquals(1.0F, card.alpha(112), 1.0E-5F);
		assertEquals(1.0F, card.alpha(128), 1.0E-5F);
		assertEquals(0.0F, card.alpha(140), 1.0E-5F);
		assertTrue(card.alpha(106) > 0.0F && card.alpha(106) < 1.0F);
		assertEquals(0.0F, card.rule(100), 1.0E-5F);
		assertEquals(1.0F, card.rule(116), 1.0E-5F);
	}

	@Test
	void shotEasesBetweenKeys() {
		CinematicShot shot = CinematicShot.of(new CinematicShot.Key(0, 0, 10, 2, 1), new CinematicShot.Key(20, 90, 6, 4, 1));
		CinematicShot.Key start = shot.at(0), mid = shot.at(10), end = shot.at(30);
		assertEquals(10.0F, start.distance(), 1.0E-5F);
		assertEquals(45.0F, mid.yawDeg(), 1.0E-4F);
		assertEquals(8.0F, mid.distance(), 1.0E-4F);
		assertEquals(6.0F, end.distance(), 1.0E-5F);
		assertTrue(shot.at(5).yawDeg() < 22.5F, "eased, not linear");
	}

	@Test
	void shotYawZeroStandsInFrontOfTheBoss() {
		// a boss facing +Z (yRot 0): yaw 0 puts the camera ahead of it along +Z, 180 behind it
		Vec3 front = CinematicShot.position(new CinematicShot.Key(0, 0, 10, 2, 1), Vec3.ZERO, 0.0F);
		assertEquals(0.0D, front.x, 1.0E-4D);
		assertEquals(2.0D, front.y, 1.0E-4D);
		assertEquals(10.0D, front.z, 1.0E-4D);
		Vec3 behind = CinematicShot.position(new CinematicShot.Key(0, 180, 10, 2, 1), Vec3.ZERO, 0.0F);
		assertEquals(-10.0D, behind.z, 1.0E-4D);
	}

	@Test
	void viewerAnchorPutsYawZeroOnThePlayersSide() {
		Vec3 boss = new Vec3(10, 64, 10), viewer = new Vec3(10, 64, 0);
		float anchor = CinematicShot.yawToward(boss, viewer);
		Vec3 camera = CinematicShot.position(new CinematicShot.Key(0, 0, 5, 0, 0), boss, anchor);
		// Mth.sin is a lookup table, good to a few thousandths of a block
		assertEquals(10.0D, camera.x, 1.0E-2D);
		assertEquals(5.0D, camera.z, 1.0E-2D, "between the boss and the player");
		assertTrue(CinematicShot.toward(new CinematicShot.Key(0, 0, 5, 0, 0)).fromViewer());
		assertTrue(!CinematicShot.of(new CinematicShot.Key(0, 0, 5, 0, 0)).fromViewer());
	}

	@Test
	void lookAnglesPointAtTarget() {
		float[] ahead = CinematicShot.lookAngles(Vec3.ZERO, new Vec3(0, 0, 5));
		assertEquals(0.0F, ahead[0], 1.0E-4F);
		assertEquals(0.0F, ahead[1], 1.0E-4F);
		float[] west = CinematicShot.lookAngles(Vec3.ZERO, new Vec3(-5, 0, 0));
		assertEquals(90.0F, west[0], 1.0E-4F, "minecraft yaw 90 faces -X");
		float[] below = CinematicShot.lookAngles(Vec3.ZERO, new Vec3(0, -5, 5));
		assertEquals(45.0F, below[1], 1.0E-4F, "positive pitch looks down");
	}
}
