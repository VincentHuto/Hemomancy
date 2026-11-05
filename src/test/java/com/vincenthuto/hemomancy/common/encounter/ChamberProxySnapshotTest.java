package com.vincenthuto.hemomancy.common.encounter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ChamberProxySnapshotTest {
	@Test
	void aPackedBlockComesBackWhole() {
		for (int dx : new int[] { -10, -1, 0, 7, 10 }) {
			for (int dy : new int[] { -2, 0, 13, 29 }) {
				for (int dz : new int[] { -10, 0, 24 }) {
					for (int id : new int[] { 0, 1, 4097, 65535, 1 << 20 }) {
						long packed = ChamberProxySnapshot.pack(dx, dy, dz, id);
						assertEquals(dx, ChamberProxySnapshot.dx(packed));
						assertEquals(dy, ChamberProxySnapshot.dy(packed));
						assertEquals(dz, ChamberProxySnapshot.dz(packed));
						assertEquals(id, ChamberProxySnapshot.stateId(packed));
					}
				}
			}
		}
	}
}
