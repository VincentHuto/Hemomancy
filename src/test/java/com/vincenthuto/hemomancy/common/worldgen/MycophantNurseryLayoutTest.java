package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.common.worldgen.MycophantNurseryLayout.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantNurseryLayoutTest {
	private static boolean solid(Material material) {
		return material != Material.AIR && material != Material.GROWTH && material != Material.NECTAR;
	}

	@Test
	void thePoolHasAFloorAndABoneRim() {
		assertEquals(Material.NECTAR, MycophantNurseryLayout.at(0, 1, 0));
		assertEquals(Material.SOIL, MycophantNurseryLayout.at(0, 0, 0));
		assertEquals(Material.BONE, MycophantNurseryLayout.at(5, 1, 0));
		assertEquals(Material.AIR, MycophantNurseryLayout.at(0, 2, 0));
	}

	@Test
	void theOculusIsOpenAboveThePool() {
		for (int y = 2; y < MycophantNurseryLayout.CEILING_Y; y++) assertEquals(Material.AIR, MycophantNurseryLayout.at(1, y, 1));
		assertEquals(Material.BARRIER, MycophantNurseryLayout.at(1, MycophantNurseryLayout.CEILING_Y, 1));
	}

	@Test
	void theFightingFloorIsFlatAndHasHeadroomForHerRear() {
		for (int x = -MycophantNurseryLayout.PLAY_RADIUS; x <= MycophantNurseryLayout.PLAY_RADIUS; x++) {
			for (int z = -MycophantNurseryLayout.PLAY_RADIUS; z <= MycophantNurseryLayout.PLAY_RADIUS; z++) {
				if (x * x + z * z > MycophantNurseryLayout.PLAY_RADIUS * MycophantNurseryLayout.PLAY_RADIUS) continue;
				Material floor = MycophantNurseryLayout.at(x, 1, z);
				assertTrue(floor == Material.FLOOR || floor == Material.NECTAR || floor == Material.BONE, x + "," + z + " " + floor);
				// she stands at y 2 and rears about seven blocks; nothing solid hangs into that
				for (int y = 2; y < MycophantNurseryLayout.LOWEST_DRIP; y++)
					assertFalse(solid(MycophantNurseryLayout.at(x, y, z)), x + "," + y + "," + z);
			}
		}
	}

	@Test
	void theArrivalPointIsClear() {
		int z = (int) Math.floor(MycophantNurseryLayout.ARRIVAL_Z);
		assertEquals(Material.FLOOR, MycophantNurseryLayout.at(0, 1, z));
		assertEquals(Material.AIR, MycophantNurseryLayout.at(0, 2, z));
		assertEquals(Material.AIR, MycophantNurseryLayout.at(0, 3, z));
	}

	@Test
	void wallsAndVaultCloseTheHollowInsideTheBarrierShell() {
		for (int a = 0; a < 360; a += 5) {
			double angle = Math.toRadians(a);
			int x = (int) Math.round(Math.cos(angle) * 23.0D);
			int z = (int) Math.round(Math.sin(angle) * 23.0D);
			// past the wall it is solid from the floor up to the vault
			for (int y = 1; y <= MycophantNurseryLayout.vaultY(Math.hypot(x, z)); y++)
				assertTrue(solid(MycophantNurseryLayout.at(x, y, z)), x + "," + y + "," + z);
		}
		// over the open floor the vault is a shell
		assertTrue(solid(MycophantNurseryLayout.at(12, MycophantNurseryLayout.vaultY(12.0D), 0)));
		assertEquals(Material.BARRIER, MycophantNurseryLayout.at(24, 5, 0));
		assertEquals(Material.BARRIER, MycophantNurseryLayout.at(0, 5, -24));
	}

	@Test
	void ribsAndLightsAndGrowthAreActuallyThere() {
		int bone = 0, light = 0, growth = 0, puddles = 0;
		for (int x = -24; x < 24; x++) for (int z = -24; z < 24; z++) {
			for (int y = 1; y < MycophantNurseryLayout.CEILING_Y; y++) {
				Material material = MycophantNurseryLayout.at(x, y, z);
				if (material == Material.BONE) bone++;
				if (material == Material.LIGHT) light++;
				if (material == Material.GROWTH) growth++;
			}
			double r = Math.hypot(x, z);
			if (r > MycophantNurseryLayout.POOL_RADIUS + 2 && MycophantNurseryLayout.at(x, 1, z) == Material.NECTAR) puddles++;
		}
		assertTrue(bone > 150, "bone " + bone);
		assertTrue(light > 12, "light " + light);
		assertTrue(growth > 40, "growth " + growth);
		assertTrue(puddles > 15, "puddles " + puddles);
		assertNotEquals(MycophantNurseryLayout.wallRadius(0.0D), MycophantNurseryLayout.wallRadius(1.0D));
	}
}
