package com.vincenthuto.hemomancy.common.block.shared;

import com.vincenthuto.hemomancy.common.block.inscription.DiscoveryInscriptionVisuals;

public final class DiscoveryInscriptionVisualsTest {
	private DiscoveryInscriptionVisualsTest() {
	}

	public static void main(String[] args) {
		assertClose("floor x", 0.03125,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.UP, 0, 0).x());
		assertClose("floor y stays on reveal plane", DiscoveryInscriptionVisuals.FLOOR_FACE,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.UP, 0, 0).y());
		assertClose("floor z", 0.03125,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.UP, 0, 0).z());

		assertClose("south face mirrors texture x", 0.96875,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.SOUTH, 0, 0).x());
		assertClose("south face y mirrors texture top", 0.96875,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.SOUTH, 0, 0).y());
		assertClose("south face z", DiscoveryInscriptionVisuals.LOW_FACE,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.SOUTH, 0, 0).z());

		assertClose("north face mirrors texture x", 0.96875,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.NORTH, 0, 0).x());
		assertClose("north face z", DiscoveryInscriptionVisuals.HIGH_FACE,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.NORTH, 0, 0).z());

		assertClose("east face x", DiscoveryInscriptionVisuals.LOW_FACE,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.EAST, 0, 0).x());
		assertClose("east face z from pixel", 0.03125,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.EAST, 0, 0).z());

		assertClose("west face x", DiscoveryInscriptionVisuals.HIGH_FACE,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.WEST, 0, 0).x());
		assertClose("west face preserves texture z", 0.03125,
				DiscoveryInscriptionVisuals.pixelCenter(DiscoveryInscriptionVisuals.Face.WEST, 0, 0).z());

		assertEquals("south drift", DiscoveryInscriptionVisuals.Face.SOUTH,
				DiscoveryInscriptionVisuals.outwardFace(DiscoveryInscriptionVisuals.Face.SOUTH));
		assertEquals("north drift", DiscoveryInscriptionVisuals.Face.NORTH,
				DiscoveryInscriptionVisuals.outwardFace(DiscoveryInscriptionVisuals.Face.NORTH));
		assertEquals("east drift", DiscoveryInscriptionVisuals.Face.EAST,
				DiscoveryInscriptionVisuals.outwardFace(DiscoveryInscriptionVisuals.Face.EAST));
		assertEquals("west drift", DiscoveryInscriptionVisuals.Face.WEST,
				DiscoveryInscriptionVisuals.outwardFace(DiscoveryInscriptionVisuals.Face.WEST));

		DiscoveryInscriptionVisuals.PixelCenter floor = DiscoveryInscriptionVisuals.pixelCenter(
				DiscoveryInscriptionVisuals.Face.UP, 8, 8);
		DiscoveryInscriptionVisuals.PixelCenter floorParticle = DiscoveryInscriptionVisuals.particleCenter(
				DiscoveryInscriptionVisuals.Face.UP, 8, 8);
		assertClose("floor particle drifts up", floor.y() + DiscoveryInscriptionVisuals.PARTICLE_OFFSET,
				floorParticle.y());
		assertEquals("floor drift", DiscoveryInscriptionVisuals.Face.UP,
				DiscoveryInscriptionVisuals.outwardFace(DiscoveryInscriptionVisuals.Face.UP));

		DiscoveryInscriptionVisuals.PixelCenter north = DiscoveryInscriptionVisuals.pixelCenter(
				DiscoveryInscriptionVisuals.Face.NORTH, 8, 8);
		DiscoveryInscriptionVisuals.PixelCenter northParticle = DiscoveryInscriptionVisuals.particleCenter(
				DiscoveryInscriptionVisuals.Face.NORTH, 8, 8);
		assertClose("north particle drifts outward", north.z() - DiscoveryInscriptionVisuals.PARTICLE_OFFSET,
				northParticle.z());

		DiscoveryInscriptionVisuals.PixelCenter east = DiscoveryInscriptionVisuals.pixelCenter(
				DiscoveryInscriptionVisuals.Face.EAST, 8, 8);
		DiscoveryInscriptionVisuals.PixelCenter eastParticle = DiscoveryInscriptionVisuals.particleCenter(
				DiscoveryInscriptionVisuals.Face.EAST, 8, 8);
		assertClose("east particle drifts outward", east.x() + DiscoveryInscriptionVisuals.PARTICLE_OFFSET,
				eastParticle.x());
	}

	private static void assertClose(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
