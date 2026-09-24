package com.vincenthuto.hemomancy.common.worldgen.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class VagrantMindGeometryTest {
	@Test
	void synapseBlobsFormRepeatablePatchesOnTheInnerWall() {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(7L);
		int lit = 0;
		int adjacent = 0;
		for (int x = -90; x <= 90; x += 2) {
			for (int y = -70; y <= 70; y += 2) {
				for (int z = -90; z <= 90; z += 2) {
					if (!VagrantMindGeometry.isInnerWall(lobes, x, y, z, 8.0)
							|| !VagrantMindGeometry.isSynapseBlob(7L, x, y, z)) continue;
					lit++;
					if (VagrantMindGeometry.isSynapseBlob(7L, x + 1, y, z)
							|| VagrantMindGeometry.isSynapseBlob(7L, x, y + 1, z)
							|| VagrantMindGeometry.isSynapseBlob(7L, x, y, z + 1)) adjacent++;
				}
			}
		}
		assertTrue(lit > 20, "the cavity needs visible synapse patches");
		assertTrue(adjacent > lit / 2, "synapses should gather into blobs");
	}
	@Test
	void lobeCountStaysInRange() {
		for (long seed = 0; seed < 50; seed++) {
			int count = VagrantMindGeometry.lobes(seed).size();
			assertTrue(count >= 14 && count <= 20, "seed " + seed + " produced " + count + " lobes");
		}
	}

	@Test
	void geometryIsDeterministicForASeed() {
		assertEquals(VagrantMindGeometry.lobes(1234L), VagrantMindGeometry.lobes(1234L),
				"the same seed must rebuild the same brain, or chunks will disagree");
	}

	@Test
	void centreIsSolidAndDistantSpaceIsEmpty() {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(7L);
		assertTrue(VagrantMindGeometry.field(lobes, 0, 0, 0) >= 1.0, "the core must be inside the field");
		assertFalse(VagrantMindGeometry.field(lobes, 500, 500, 500) >= 1.0, "far space must be outside");
	}

	@Test
	void shellIsHollow() {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(7L);
		assertFalse(VagrantMindGeometry.isShell(lobes, 0, 0, 0, 8.0),
				"the centre is cavity, not shell — otherwise there is nothing to explore");
	}

	@Test
	void fissureSplitsTheHemispheres() {
		assertTrue(VagrantMindGeometry.isFissure(0.0, 40.0, 3.0), "x=0 is the sagittal plane");
		assertFalse(VagrantMindGeometry.isFissure(30.0, 40.0, 3.0), "far off-axis is not fissure");
	}

	@Test
	void sagittalSeamWandersInsteadOfCuttingOneStraightColumn() {
		int centreHits = 0;
		int samples = 0;
		for (double z = -80.0; z <= 80.0; z += 4.0) {
			samples++;
			centreHits += VagrantMindGeometry.isFissure(0.0, z, 3.0) ? 1 : 0;
		}
		assertTrue(centreHits < samples * 3 / 4,
				"a fixed x=0 fissure produces the unnatural ruler-straight line visible on the crown");
	}

	@Test
	void sagittalSeamIsIntactTissueRatherThanAnOpeningIntoTheHollowInterior() {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(7L);
		int seamSamples = 0;
		for (double y = 4.0; y <= 100.0; y += 2.0) {
			for (double z = -100.0; z <= 100.0; z += 2.0) {
				for (double x = -6.0; x <= 6.0; x += 1.0) {
					if (!VagrantMindGeometry.isFissure(x, z, 3.0)
							|| !VagrantMindGeometry.isShell(lobes, x, y, z, 8.0)) {
						continue;
					}
					seamSamples++;
					assertEquals(VagrantMindGeometry.SurfaceMaterial.TISSUE,
							VagrantMindGeometry.surfaceMaterial(lobes, x, y, z, 8.0, 3.0),
							"the sagittal seam must remain closed instead of exposing the cavity");
				}
			}
		}
		assertTrue(seamSamples > 0, "the fixture must exercise real sagittal shell voxels");
	}

	@Test
	void everySeedHasDefinedCerebellumAndBrainstemMasses() {
		for (long seed = 0; seed < 50; seed++) {
			List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(seed);
			assertTrue(VagrantMindGeometry.field(lobes, -24, -31, 52) >= 1.0,
					"left cerebellar hemisphere is missing for seed " + seed);
			assertTrue(VagrantMindGeometry.field(lobes, 24, -31, 52) >= 1.0,
					"right cerebellar hemisphere is missing for seed " + seed);
			assertTrue(VagrantMindGeometry.field(lobes, 0, -76, 42) >= 1.0,
					"inferior brainstem is missing for seed " + seed);
		}
	}

	@Test
	void brainstemExtendsLowerAndCerebellumHasDistinctSurfaceFolds() {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(7L);
		assertTrue(VagrantMindGeometry.field(lobes, 0, -112, 42) >= 1.0,
				"the elongated brainstem should remain present below the old lower tip");

		List<VagrantMindGeometry.Lobe> cerebellarSurface = List.of(
				new VagrantMindGeometry.Lobe(0.0, -34.0, 60.0, 6.0, 6.0, 6.0));
		assertEquals(VagrantMindGeometry.SurfaceMaterial.TISSUE,
				VagrantMindGeometry.surfaceMaterial(cerebellarSurface, 0.0, -34.0, 60.0, 8.0, 3.0),
				"the cerebellar midline should read as a median sulcus");
		assertEquals(VagrantMindGeometry.SurfaceMaterial.FOLDS,
				VagrantMindGeometry.surfaceMaterial(cerebellarSurface, 2.0, -34.0, 60.0, 8.0, 3.0),
				"the cerebellar hemisphere should have its own folial ridges");
	}

	@Test
	void shellNeverReachesThePlacementEnvelopeFaces() {
		for (long seed = 0; seed < 50; seed++) {
			List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(seed);
			for (int a = -128; a <= 128; a += 4) {
				for (int b = -128; b <= 128; b += 4) {
					assertFalse(VagrantMindGeometry.isShell(lobes, -128, a, b, 8.0),
							"shell is clipped by the negative X envelope face for seed " + seed);
					assertFalse(VagrantMindGeometry.isShell(lobes, 128, a, b, 8.0),
							"shell is clipped by the positive X envelope face for seed " + seed);
					assertFalse(VagrantMindGeometry.isShell(lobes, a, b, -128, 8.0),
							"shell is clipped by the negative Z envelope face for seed " + seed);
					assertFalse(VagrantMindGeometry.isShell(lobes, a, b, 128, 8.0),
							"shell is clipped by the positive Z envelope face for seed " + seed);
					assertFalse(VagrantMindGeometry.isShell(lobes, a, -128, b, 8.0),
							"shell is clipped by the lower envelope face for seed " + seed);
					assertFalse(VagrantMindGeometry.isShell(lobes, a, 128, b, 8.0),
							"shell is clipped by the upper envelope face for seed " + seed);
				}
			}
		}
	}

	@Test
	void terrainClearanceWrapsTheShellWithAnOrganicBuffer() {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(7L);
		int bufferedPoints = 0;
		for (double x = -120.0; x <= 120.0; x += 4.0) {
			for (double y = -120.0; y <= 120.0; y += 4.0) {
				for (double z = -120.0; z <= 120.0; z += 4.0) {
					boolean shell = VagrantMindGeometry.isShell(lobes, x, y, z, 8.0);
					boolean clears = VagrantMindGeometry.clearsTerrain(lobes, x, y, z);
					if (shell) {
						assertTrue(clears, "the terrain carver must clear every shell position before placement");
					}
					if (clears && VagrantMindGeometry.field(lobes, x, y, z) < 1.0) {
						bufferedPoints++;
					}
				}
			}
		}
		assertTrue(bufferedPoints > 200,
				"clearance must extend beyond the shell as a real terrain-shaping buffer");
	}

	@Test
	void terrainClearanceNeverBecomesABoxAtTheEnvelopeFaces() {
		for (long seed = 0; seed < 20; seed++) {
			List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(seed);
			for (int a = -128; a <= 128; a += 4) {
				for (int b = -128; b <= 128; b += 4) {
					assertFalse(VagrantMindGeometry.clearsTerrain(lobes, -128, a, b));
					assertFalse(VagrantMindGeometry.clearsTerrain(lobes, 128, a, b));
					assertFalse(VagrantMindGeometry.clearsTerrain(lobes, a, b, -128));
					assertFalse(VagrantMindGeometry.clearsTerrain(lobes, a, b, 128));
					assertFalse(VagrantMindGeometry.clearsTerrain(lobes, a, -128, b));
					assertFalse(VagrantMindGeometry.clearsTerrain(lobes, a, 128, b));
				}
			}
		}
	}

	@Test
	void shellHasSubstanceAcrossTheBrain() {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(7L);
		int shellCount = 0;
		for (double x = -120.0; x <= 120.0; x += 4.0) {
			for (double y = -80.0; y <= 80.0; y += 4.0) {
				for (double z = -120.0; z <= 120.0; z += 4.0) {
					if (VagrantMindGeometry.isShell(lobes, x, y, z, 8.0)) {
						shellCount++;
					}
				}
			}
		}
		// A coarse 4-block grid over a ~100-radius brain crosses many lobe boundaries; a real shell
		// should register on the order of hundreds of sample points, not a handful of stray hits.
		assertTrue(shellCount > 200,
				"expected a substantial shell surface across the sampled grid, but only found " + shellCount
						+ " shell points — isShell may be broken (e.g. always false)");
	}

	@Test
	void interiorCavityIsHollowNotShell() {
		List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(7L);
		int interiorCount = 0;
		for (double x = -120.0; x <= 120.0; x += 4.0) {
			for (double y = -80.0; y <= 80.0; y += 4.0) {
				for (double z = -120.0; z <= 120.0; z += 4.0) {
					if (VagrantMindGeometry.field(lobes, x, y, z) >= 1.0
							&& !VagrantMindGeometry.isShell(lobes, x, y, z, 8.0)) {
						interiorCount++;
					}
				}
			}
		}
		// Interior points are inside the field but not near a boundary in any axis direction —
		// genuine cavity volume a player could fly through. A hollow brain should have plenty of it.
		assertTrue(interiorCount > 200,
				"expected substantial hollow interior volume across the sampled grid, but only found "
						+ interiorCount + " interior points — the brain may be a solid lump, not hollow");
	}

	@Test
	void columnSpanNeverClipsRealGeometry() {
		for (long seed : new long[] { 7L, 42L, -31337L }) {
			List<VagrantMindGeometry.Lobe> lobes = VagrantMindGeometry.lobes(seed);
			int skipped = 0;
			int total = 0;
			for (double x = -120.0; x <= 120.0; x += 2.0) {
				for (double z = -120.0; z <= 120.0; z += 2.0) {
					double[] span = VagrantMindGeometry.columnSpan(lobes, x, z);
					for (double y = -80.0; y <= 80.0; y += 2.0) {
						total++;
						boolean inSpan = span != null && y >= span[0] && y <= span[1];
						if (!inSpan) {
							skipped++;
							assertFalse(VagrantMindGeometry.isShell(lobes, x, y, z, 8.0),
									"columnSpan excluded a voxel that isShell accepts, so the placement "
											+ "early-out would clip real shell geometry at " + x + "," + y
											+ "," + z + " for seed " + seed);
						}
					}
				}
			}
			// The bound exists to be worth taking: measured at roughly 30% of the envelope, which is
			// the price of staying conservative (it bounds where the field is non-zero, not where it
			// reaches 1.0). A regression below a fifth means the early-out has stopped paying.
			assertTrue(skipped > total / 5,
					"columnSpan should skip a meaningful share of the bounding box, but only skipped "
							+ skipped + " of " + total);
		}
	}
}
