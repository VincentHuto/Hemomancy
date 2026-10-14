package com.vincenthuto.hemomancy.client.render.world.rite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DaemonHazeQueueTest {
	@BeforeEach
	void reset() {
		DaemonHazeQueue.clear();
	}

	static DaemonHazeQueue.Draw draw(float seed) {
		return new DaemonHazeQueue.Draw(1, 2, 3, 4.0F, 0.0F, 0.3F, 1.0F, 0.0F, 0.0F, 0.96F, 2.2F, 10.0F, seed, 1, 1, 0,
				1, 0xB01420, 0, 0, 0, 0, 0, 0, 0);
	}

	@Test
	void eachFrameDrainsWhatTheEntityPassQueued() {
		DaemonHazeQueue.add(draw(1));
		DaemonHazeQueue.add(draw(2));

		assertEquals(2, DaemonHazeQueue.drain().size());
		assertTrue(DaemonHazeQueue.drain().isEmpty(), "drawn once");
	}

	@Test
	void staffHazeIsQueuedAndDrainedWithTheDaemons() {
		DaemonHazeQueue.addStaff(new DaemonHazeQueue.StaffDraw(1, 2, 3, 10.0F, 4.0F, 1.0F, 1.0F));

		assertEquals(1, DaemonHazeQueue.drainStaff().size());
		assertTrue(DaemonHazeQueue.drainStaff().isEmpty());
		DaemonHazeQueue.addStaff(new DaemonHazeQueue.StaffDraw(1, 2, 3, 10.0F, 4.0F, 1.0F, 1.0F));
		DaemonHazeQueue.clear();
		assertTrue(DaemonHazeQueue.drainStaff().isEmpty(), "clearing on logout drops the staff haze too");
	}

	@Test
	void anUnflushedQueueNeverGrowsWithoutLimit() {
		for (int i = 0; i < DaemonHazeQueue.LIMIT * 3; i++) DaemonHazeQueue.add(draw(i));

		assertTrue(DaemonHazeQueue.drain().size() <= DaemonHazeQueue.LIMIT);
	}

	@Test
	void theBoxFacesWindOutwardAndCarryTheirOwnCoordinates() {
		float[] v = DaemonHazeQueue.boxVertices(0.5, 2.0);
		assertEquals(24 * 6, v.length);
		for (int face = 0; face < 6; face++) {
			int a = face * 4 * 6;
			int b = a + 6;
			int c = a + 12;
			double ex = v[b] - v[a], ey = v[b + 1] - v[a + 1], ez = v[b + 2] - v[a + 2];
			double fx = v[c] - v[a], fy = v[c + 1] - v[a + 1], fz = v[c + 2] - v[a + 2];
			double nx = ey * fz - ez * fy, ny = ez * fx - ex * fz, nz = ex * fy - ey * fx;
			double cx = 0, cy = 0, cz = 0;
			for (int corner = 0; corner < 4; corner++) {
				cx += v[a + corner * 6];
				cy += v[a + corner * 6 + 1];
				cz += v[a + corner * 6 + 2];
			}
			assertTrue(nx * cx + ny * cy + nz * cz > 0.0, "face " + face + " is counter-clockwise from outside");
		}
		for (int vertex = 0; vertex < 24; vertex++) {
			int at = vertex * 6;
			assertEquals(v[at], v[at + 3], 1e-6F, "u carries local x");
			assertEquals(v[at + 2], v[at + 4], 1e-6F, "v carries local z");
			assertEquals(v[at + 1] > 0 ? 255.0F : 0.0F, v[at + 5], 1e-6F, "red carries the height");
			assertEquals(0.5F, Math.abs(v[at]), 1e-6F);
			assertEquals(2.0F, Math.abs(v[at + 1]), 1e-6F);
		}
	}
}
