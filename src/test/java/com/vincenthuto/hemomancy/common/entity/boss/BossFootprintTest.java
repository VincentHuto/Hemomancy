package com.vincenthuto.hemomancy.common.entity.boss;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Height;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The kit's ground shapes: hits and misses for every record, the height bands and {@code groundOnly}, the motion of
 * the time-varying shapes, and NBT round trips. The frame aims south (+z) from the origin, so the right hand is west
 * (-x).
 */
final class BossFootprintTest {
	private static final double FLOOR = 64.0D;
	private static final Height H = Height.STANDING;

	static FootprintFrame frame() {
		AABB floor = new AABB(-7.5D, FLOOR, -7.5D, 7.5D, FLOOR + 16.0D, 7.5D);
		AABB chamber = new AABB(-18.0D, FLOOR - 1.0D, -13.0D, 19.0D, FLOOR + 16.0D, 14.0D);
		return new FootprintFrame(new Vec3(0.0D, FLOOR, 0.0D), new Vec3(0.0D, FLOOR, 10.0D), new Vec3(0.0D, FLOOR, 6.0D),
				FLOOR, List.of(), LongSets.EMPTY_SET, List.of(), floor, chamber);
	}

	static Vec3 at(double x, double z) {
		return new Vec3(x, FLOOR, z);
	}

	@Test
	void heightBandsAndGroundOnly() {
		BossFootprint.Disc disc = new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 3.0D, Height.JUMPABLE);
		assertTrue(disc.hits(frame(), at(1, 1), true, 0));
		assertTrue(disc.hits(frame(), new Vec3(1, FLOOR + 0.9D, 1), false, 0), "the band's top is inclusive");
		assertFalse(disc.hits(frame(), new Vec3(1, FLOOR + 1.0D, 1), false, 0), "a jump clears the jumpable band");
		BossFootprint.Disc grounded = new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 3.0D,
				new Height(0.0D, 2.5D, true));
		assertTrue(grounded.hits(frame(), at(1, 1), true, 0));
		assertFalse(grounded.hits(frame(), at(1, 1), false, 0), "groundOnly misses a body in the air");
		assertEquals(0.9D, Height.JUMPABLE.max());
	}

	@Test
	void laneGapsAndFans() {
		BossFootprint.Lane lanes = new BossFootprint.Lane(14.0D, 0.5D, 1, 2.0D, 0.0D, 0.0D, 0, H);
		assertTrue(lanes.hits(frame(), at(0, 5), 0), "centre lane");
		assertTrue(lanes.hits(frame(), at(-2, 5), 0), "right lane (west)");
		assertTrue(lanes.hits(frame(), at(2, 5), 0), "left lane (east)");
		assertFalse(lanes.hits(frame(), at(1, 5), 0), "the gap between lanes is safe");
		assertFalse(lanes.hits(frame(), at(0, -1), 0), "nothing behind the feet");
		assertFalse(lanes.hits(frame(), at(0, 14.5), 0), "nothing past the length");
		BossFootprint.Lane fan = new BossFootprint.Lane(10.0D, 0.5D, 1, 0.0D, 30.0D, 0.0D, 0, H);
		Vec3 along30 = BossFootprint.Shapes.at(frame(), frame().origin(), 30.0D, 6.0D);
		assertTrue(fan.hits(frame(), along30, 0), "a fanned lane runs at its heading");
		assertFalse(fan.hits(frame(), BossFootprint.Shapes.at(frame(), frame().origin(), 15.0D, 6.0D), 0),
				"between fanned lanes is safe");
	}

	@Test
	void laneFrontsRunOutAndSideLanesWait() {
		BossFootprint.Lane lanes = new BossFootprint.Lane(12.0D, 0.5D, 1, 2.0D, 0.0D, 1.0D, 4, H);
		assertEquals(5.0D, lanes.front(5, 0));
		assertEquals(1.0D, lanes.front(5, 1), "a side lane starts sideDelay ticks later");
		assertEquals(-1.0D, lanes.front(-1, 0), "no front before the commit");
		assertEquals(12.0D, lanes.front(40, 0), "the front stops at the length");
		assertTrue(lanes.hits(frame(), at(0, 4), 5));
		assertFalse(lanes.hits(frame(), at(0, 6), 5), "ahead of the front is not yet struck");
		assertFalse(lanes.hits(frame(), at(-2, 3), 5), "the side lane's front is behind");
	}

	@Test
	void spokesLeaveTheirWedgesSafe() {
		BossFootprint.Spokes spokes = new BossFootprint.Spokes(4, 0.6D, 1.0D, 9.0D, 0.0D, 0.0D, H);
		assertTrue(spokes.hits(frame(), at(0, 5), 0), "the spoke along the aim");
		assertTrue(spokes.hits(frame(), at(5, 0), 0), "the spoke a quarter round");
		assertFalse(spokes.hits(frame(), at(3, 3), 0), "the wedge between is safe");
		assertFalse(spokes.hits(frame(), at(0, 0.5), 0), "inside the inner radius");
		BossFootprint.Spokes growing = new BossFootprint.Spokes(4, 0.6D, 1.0D, 9.0D, 0.0D, 2.0D, H);
		assertEquals(5.0D, growing.reach(2));
		assertFalse(growing.hits(frame(), at(0, 7), 2), "past the growing reach");
	}

	@Test
	void conesAndSweeps() {
		BossFootprint.Cone cone = new BossFootprint.Cone(1.0D, 6.0D, 90.0D, 0.0D, H);
		assertTrue(cone.hits(frame(), at(0, 4), 0));
		assertTrue(cone.hits(frame(), at(2, 3), 0));
		assertFalse(cone.hits(frame(), at(4, 1), 0), "outside the cone's angle");
		BossFootprint.Sweep sweep = new BossFootprint.Sweep(1.0D, 7.0D, 120.0D, 20.0D, true, 10, H);
		assertEquals(-60.0D, sweep.angle(0), 1.0E-9);
		assertEquals(0.0D, sweep.angle(5), 1.0E-9);
		assertEquals(60.0D, sweep.angle(10), 1.0E-9);
		assertTrue(sweep.hits(frame(), at(0, 4), 5), "the band is along the aim at half way");
		assertFalse(sweep.hits(frame(), at(0, 4), 0), "and to one side at the start");
		BossFootprint.Sweep back = new BossFootprint.Sweep(1.0D, 7.0D, 120.0D, 20.0D, false, 10, H);
		assertEquals(60.0D, back.angle(0), 1.0E-9);
	}

	@Test
	void ringWavesAndTheJumpBand() {
		BossFootprint.Ring ring = new BossFootprint.Ring(1.0D, 11.0D, 1.0D, 2, 10, 10, Height.JUMPABLE);
		assertEquals(6.0D, ring.radius(5, 0), 1.0E-9);
		assertEquals(-1.0D, ring.radius(5, 1), "the second wave has not started");
		assertEquals(6.0D, ring.radius(15, 1), 1.0E-9);
		assertTrue(ring.hits(frame(), at(0, 6), true, 5));
		assertFalse(ring.hits(frame(), at(0, 4), true, 5), "inside the wave");
		assertFalse(ring.hits(frame(), new Vec3(0, FLOOR + 1.2D, 6), false, 5), "jumped");
		assertEquals(-1.0D, ring.radius(30, 1), "finished");
	}

	@Test
	void annulusArcsTurn() {
		BossFootprint.Annulus full = new BossFootprint.Annulus(3.0D, 6.0D, 0, 0.0D, 0.0D, 20, H);
		assertTrue(full.hits(frame(), at(-4, 0), 0));
		assertFalse(full.hits(frame(), at(1, 0), 0));
		BossFootprint.Annulus arcs = new BossFootprint.Annulus(3.0D, 6.0D, 2, 60.0D, 0.25D, 20, H);
		assertEquals(0.0D, arcs.arcAngle(0, 0), 1.0E-9);
		assertEquals(90.0D, arcs.arcAngle(20, 0), 1.0E-9);
		assertTrue(arcs.hits(frame(), at(0, 4), 0), "the first arc on the aim");
		assertFalse(arcs.hits(frame(), at(0, 4), 20), "a quarter turn later it has moved");
		assertTrue(arcs.hits(frame(), BossFootprint.Shapes.at(frame(), frame().origin(), 90.0D, 4.0D), 20));
	}

	@Test
	void discsCrossesAndAnchors() {
		BossFootprint.Disc target = new BossFootprint.Disc(BossFootprint.Anchor.TARGET, 2.0D, H);
		assertTrue(target.hits(frame(), at(0, 7), 0));
		assertFalse(target.hits(frame(), at(0, 3), 0));
		BossFootprint.Cross cross = new BossFootprint.Cross(BossFootprint.Anchor.ORIGIN, 5.0D, 0.6D, H);
		assertTrue(cross.hits(frame(), at(0, 4), 0));
		assertTrue(cross.hits(frame(), at(4, 0), 0));
		assertFalse(cross.hits(frame(), at(3, 3), 0));
		BossFootprint.Discs discs = new BossFootprint.Discs(1.5D, 5, H);
		FootprintFrame points = frame().withPoints(List.of(at(4, 4), at(-4, 4)));
		assertTrue(discs.hits(points, at(4, 4), 0));
		assertFalse(discs.hits(points, at(-4, 4), 2), "the second disc lands landingGap later");
		assertTrue(discs.hits(points, at(-4, 4), 5));
	}

	@Test
	void pathWalkers() {
		FootprintFrame path = frame().withPoints(List.of(at(0, 0), at(0, 10), at(10, 10)));
		BossFootprint.Path whole = new BossFootprint.Path(0.75D, 0.0D, 0.0D, 0, 0.0D, H);
		assertTrue(whole.hits(path, at(5, 10), 0), "the whole path, round its corner");
		assertFalse(whole.hits(path, at(5, 5), 0));
		BossFootprint.Path walking = new BossFootprint.Path(0.75D, 1.0D, 3.0D, 2, 6.0D, H);
		assertEquals(10.0D, walking.walker(10, 0), 1.0E-9);
		assertEquals(4.0D, walking.walker(10, 1), 1.0E-9);
		assertTrue(walking.hits(path, at(0, 9), 10), "the head walker");
		assertTrue(walking.hits(path, at(0, 3), 10), "the second walker");
		assertFalse(walking.hits(path, at(0, 6), 10), "between the walkers");
	}

	@Test
	void regionsExcludeTheirCells() {
		BossFootprint.Region floor = new BossFootprint.Region(BossFootprint.Area.BOSS_FLOOR, 0.0D, H);
		assertTrue(floor.hits(frame(), at(3.5, 3.5), 0));
		assertFalse(floor.hits(frame(), at(9, 0), 0), "off the boss floor");
		LongOpenHashSet cells = new LongOpenHashSet();
		cells.add(BossFootprint.cell(3, 3));
		assertFalse(floor.hits(frame().withCells(cells), at(3.5, 3.5), 0), "a safe cell");
		BossFootprint.Region around = new BossFootprint.Region(BossFootprint.Area.AROUND_ORIGIN, 4.0D, H);
		assertTrue(around.hits(frame(), at(2, 2), 0));
		assertFalse(around.hits(frame(), at(4, 4), 0));
	}

	@Test
	void tethersAreReReadAsTheirEndsMove() {
		BossFootprint.Tether chain = new BossFootprint.Tether(BossFootprint.End.BODY, BossFootprint.End.TARGET, 0, 0.5D,
				12.0D, H);
		assertTrue(chain.hits(frame(), at(0, 3), 0), "along the line to the target");
		FootprintFrame moved = frame().withTarget(at(6, 0));
		assertFalse(chain.hits(moved, at(0, 3), 0), "the line follows the target");
		assertTrue(chain.hits(moved, at(3, 0), 0));
		assertTrue(chain.broken(frame().withTarget(at(0, 13))), "range breaks it");
		BossFootprint.Tether end = new BossFootprint.Tether(BossFootprint.End.ANCHOR, BossFootprint.End.TARGET, 0, 0.0D,
				0.0D, H);
		FootprintFrame anchored = frame().withAnchors(List.of(at(-5, -5)));
		assertTrue(end.hits(anchored, at(0, 6.5), 0), "halfWidth 0 strikes only the target end");
		assertFalse(end.hits(anchored, at(-2, -2), 0));
	}

	@Test
	void unionPartsWaitTheirDelays() {
		BossFootprint.Union union = new BossFootprint.Union(List.of(
				new BossFootprint.Union.Part(new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 2.0D, H), 0),
				new BossFootprint.Union.Part(new BossFootprint.Disc(BossFootprint.Anchor.TARGET, 1.5D, Height.JUMPABLE), 8)));
		assertEquals(1, union.live(4).size());
		assertTrue(union.hits(frame(), at(1, 0), true, 0));
		assertFalse(union.hits(frame(), at(0, 6), true, 4), "the second part waits");
		assertTrue(union.hits(frame(), at(0, 6), true, 8));
		assertFalse(union.hits(frame(), new Vec3(0, FLOOR + 1.5D, 6), false, 8), "each part keeps its own band");
		assertFalse(BossFootprint.None.INSTANCE.hits(frame(), at(0, 0), true, 0));
		assertTrue(new BossFootprint.Occluded(new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 2.0D, H))
				.hits(frame(), at(1, 0), true, 0), "occlusion is the level's test; the plane is the inner shape's");
	}

	@Test
	void everyShapeSurvivesNbt() {
		List<BossFootprint> shapes = List.of(
				new BossFootprint.Lane(14.0D, 0.5D, 1, 2.0D, 0.0D, 1.0D, 3, H),
				new BossFootprint.Spokes(5, 0.6D, 1.0D, 9.0D, 12.0D, 1.5D, H),
				new BossFootprint.Cone(1.0D, 6.0D, 90.0D, 10.0D, H),
				new BossFootprint.Sweep(1.0D, 7.0D, 120.0D, 20.0D, true, 10, H),
				new BossFootprint.Ring(1.0D, 11.0D, 1.0D, 2, 10, 10, Height.JUMPABLE),
				new BossFootprint.Disc(BossFootprint.Anchor.AIM, 3.5D, new Height(0.0D, 3.0D, true)),
				new BossFootprint.Annulus(3.0D, 6.0D, 3, 40.0D, 0.5D, 30, H),
				new BossFootprint.Cross(BossFootprint.Anchor.TARGET, 5.0D, 0.6D, H),
				new BossFootprint.Discs(1.5D, 4, H),
				new BossFootprint.Path(0.75D, 1.0D, 3.0D, 2, 6.0D, H),
				new BossFootprint.Region(BossFootprint.Area.CHAMBER, 0.0D, H),
				new BossFootprint.Tether(BossFootprint.End.BODY, BossFootprint.End.ANCHOR, 2, 0.5D, 12.0D, H),
				new BossFootprint.Union(List.of(new BossFootprint.Union.Part(
						new BossFootprint.Occluded(new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 2.0D, H)), 4))),
				BossFootprint.None.INSTANCE);
		for (BossFootprint shape : shapes) {
			assertEquals(shape, BossFootprints.load(BossFootprints.save(shape)), shape.toString());
		}
		FootprintFrame frame = frame().withPoints(List.of(at(1, 2), at(3, 4))).withAnchors(List.of(at(-1, -1)));
		LongOpenHashSet cells = new LongOpenHashSet();
		cells.add(BossFootprint.cell(2, -3));
		frame = frame.withCells(cells);
		assertEquals(frame, FootprintFrame.load(frame.save()));
	}
}
