package com.vincenthuto.hemomancy.common.entity.boss;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Height;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A drawn edge is a hit edge: points placed just inside each shape's outline, from the same motion methods the
 * client telegraph draws with, hit; points just outside it miss. Sampled over the active span.
 */
final class BossFootprintAgreementTest {
	private static final double IN = 0.02D;
	private static final double OUT = 0.08D;
	private static final Height H = Height.STANDING;

	private static FootprintFrame frame() {
		return BossFootprintTest.frame();
	}

	private static Vec3 along(double degrees, double distance) {
		return BossFootprint.Shapes.at(frame(), frame().origin(), degrees, distance);
	}

	/** A point {@code side} to the right of the line from the origin at {@code degrees}, {@code distance} along it. */
	private static Vec3 beside(double degrees, double distance, double side) {
		Vec3 on = along(degrees, distance);
		Vec3 right = along(degrees + 90.0D, 1.0D).subtract(frame().origin());
		return on.add(right.scale(side));
	}

	@Test
	void laneEdgesAndFronts() {
		BossFootprint.Lane lane = new BossFootprint.Lane(12.0D, 0.5D, 1, 3.0D, 0.0D, 1.0D, 2, H);
		for (int age = 1; age <= 14; age++) {
			for (int n = 0; n < lane.lanes(); n++) {
				int index = lane.index(n);
				double front = lane.front(age, index);
				if (front <= 0.1D) continue;
				double offset = lane.offset(index);
				assertTrue(lane.hits(frame(), beside(0.0D, front - IN, offset + lane.halfWidth() - IN), age));
				assertTrue(lane.hits(frame(), beside(0.0D, front - IN, offset - lane.halfWidth() + IN), age));
				assertFalse(lane.hits(frame(), beside(0.0D, front - IN, offset + lane.halfWidth() + OUT), age));
				if (front < lane.length()) assertFalse(lane.hits(frame(), beside(0.0D, front + OUT, offset), age));
			}
		}
	}

	@Test
	void spokeReachAndWidth() {
		BossFootprint.Spokes spokes = new BossFootprint.Spokes(5, 0.6D, 1.0D, 9.0D, 10.0D, 1.0D, H);
		for (int age = 1; age <= 10; age++) {
			double reach = spokes.reach(age);
			for (int spoke = 0; spoke < spokes.count(); spoke++) {
				double heading = spokes.angle(spoke);
				assertTrue(spokes.hits(frame(), beside(heading, reach - IN, spokes.halfWidth() - IN), age));
				assertFalse(spokes.hits(frame(), beside(heading, reach - IN, spokes.halfWidth() + OUT), age));
				if (reach < spokes.length()) assertFalse(spokes.hits(frame(), along(heading, reach + OUT), age));
			}
		}
	}

	@Test
	void sweepBandEdges() {
		BossFootprint.Sweep sweep = new BossFootprint.Sweep(1.0D, 7.0D, 120.0D, 20.0D, true, 12, H);
		for (int age = 0; age <= 12; age++) {
			double centre = sweep.angle(age);
			double edge = sweep.bandDegrees() / 2.0D;
			for (double side : new double[]{-1.0D, 1.0D}) {
				double inside = centre + side * (edge - 1.0D);
				double outside = centre + side * (edge + 1.0D);
				if (Math.abs(inside) <= sweep.degrees() / 2.0D) assertTrue(sweep.hits(frame(), along(inside, 4.0D), age));
				assertFalse(sweep.hits(frame(), along(outside, 4.0D), age));
			}
			assertTrue(sweep.hits(frame(), along(centre, sweep.radius() - IN), age));
			assertFalse(sweep.hits(frame(), along(centre, sweep.radius() + OUT), age));
		}
	}

	@Test
	void ringWaveEdges() {
		BossFootprint.Ring ring = new BossFootprint.Ring(1.0D, 11.0D, 1.0D, 2, 6, 12, Height.JUMPABLE);
		for (int age = 1; age <= 17; age++) {
			for (int wave = 0; wave < ring.waves(); wave++) {
				double radius = ring.radius(age, wave);
				if (radius < 1.6D) continue;
				assertTrue(ring.hits(frame(), along(33.0D, radius + ring.band() / 2.0D - IN), age));
				assertTrue(ring.hits(frame(), along(33.0D, radius - ring.band() / 2.0D + IN), age));
				double other = ring.radius(age, 1 - wave);
				double outside = radius + ring.band() / 2.0D + OUT;
				if (other < 0.0D || Math.abs(outside - other) > ring.band()) {
					assertFalse(ring.hits(frame(), along(33.0D, outside), age));
				}
			}
		}
	}

	@Test
	void annulusArcEdges() {
		BossFootprint.Annulus arcs = new BossFootprint.Annulus(3.0D, 6.0D, 3, 40.0D, 0.5D, 30, H);
		for (int age = 0; age <= 30; age += 3) {
			for (int arc = 0; arc < arcs.arcs(); arc++) {
				double centre = arcs.arcAngle(age, arc);
				assertTrue(arcs.hits(frame(), along(centre + arcs.arcDegrees() / 2.0D - 1.0D, 4.5D), age));
				assertFalse(arcs.hits(frame(), along(centre + arcs.arcDegrees() / 2.0D + 1.0D, 4.5D), age));
				assertTrue(arcs.hits(frame(), along(centre, arcs.outer() - IN), age));
				assertFalse(arcs.hits(frame(), along(centre, arcs.outer() + OUT), age));
				assertFalse(arcs.hits(frame(), along(centre, arcs.inner() - OUT), age));
			}
		}
	}

	@Test
	void pathWalkerEnds() {
		FootprintFrame path = frame().withPoints(List.of(BossFootprintTest.at(0, 0), BossFootprintTest.at(0, 20)));
		BossFootprint.Path walking = new BossFootprint.Path(0.75D, 1.0D, 3.0D, 1, 0.0D, H);
		for (int age = 4; age <= 18; age++) {
			double head = walking.walker(age, 0);
			Vec3 front = BossFootprint.Shapes.pointAlong(path.points(), head - IN);
			Vec3 tail = BossFootprint.Shapes.pointAlong(path.points(), head - walking.frontLength() + IN);
			assertTrue(walking.hits(path, front, age));
			assertTrue(walking.hits(path, tail, age));
			assertFalse(walking.hits(path, BossFootprint.Shapes.pointAlong(path.points(), head + OUT), age));
			assertFalse(walking.hits(path, BossFootprint.Shapes.pointAlong(path.points(),
					head - walking.frontLength() - OUT), age));
			assertFalse(walking.hits(path, front.add(walking.halfWidth() + OUT, 0.0D, 0.0D), age));
		}
	}

	@Test
	void discAndConeRims() {
		BossFootprint.Disc disc = new BossFootprint.Disc(BossFootprint.Anchor.TARGET, 2.5D, H);
		Vec3 target = frame().target();
		for (int i = 0; i < 12; i++) {
			double angle = Math.PI * 2.0D * i / 12.0D;
			Vec3 dir = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
			assertTrue(disc.hits(frame(), target.add(dir.scale(disc.radius() - IN)), 0));
			assertFalse(disc.hits(frame(), target.add(dir.scale(disc.radius() + OUT)), 0));
		}
		BossFootprint.Cone cone = new BossFootprint.Cone(1.0D, 6.0D, 70.0D, 20.0D, H);
		double edge = cone.offsetDegrees() + cone.degrees() / 2.0D;
		assertTrue(cone.hits(frame(), along(edge - 1.0D, 4.0D), 0));
		assertFalse(cone.hits(frame(), along(edge + 1.0D, 4.0D), 0));
		assertTrue(cone.hits(frame(), along(cone.offsetDegrees(), cone.radius() - IN), 0));
		assertFalse(cone.hits(frame(), along(cone.offsetDegrees(), cone.radius() + OUT), 0));
	}
}
