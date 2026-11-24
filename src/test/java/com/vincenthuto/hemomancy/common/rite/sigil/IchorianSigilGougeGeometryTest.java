package com.vincenthuto.hemomancy.common.rite.sigil;

import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilGougeGeometry.Cross;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilGougeGeometry.STRIP_HALF_WIDTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IchorianSigilGougeGeometryTest {
	private static final double EPSILON = 1.0e-5D;

	@Test
	void flatLinkSpansNodeToNodeOnTheSurface() {
		List<Cross> strip = IchorianSigilGougeGeometry.strip(0.0D, 64.1D, 0.0D, 4.0D, 64.1D, 0.0D);
		assertEquals(9, strip.size());
		assertEquals(0.0F, strip.get(0).along(), EPSILON);
		Cross last = strip.get(strip.size() - 1);
		assertEquals(1.0F, last.along(), EPSILON);
		assertEquals(4.0F, last.alongBlocks(), EPSILON);
		for (Cross cross : strip) {
			double width = Math.hypot(cross.rightX() - cross.leftX(), cross.rightZ() - cross.leftZ());
			assertEquals(STRIP_HALF_WIDTH * 2.0D, width, EPSILON);
			// The centre line is z = 0: left and right sit either side of it.
			assertTrue(Math.signum(cross.leftZ()) == -Math.signum(cross.rightZ()) && cross.leftZ() != 0.0D);
			assertEquals((cross.leftX() + cross.rightX()) * 0.5D, cross.alongBlocks(), EPSILON);
			assertEquals(64.012D, cross.y(), EPSILON);
		}
	}

	@Test
	void steppedLinkRisesFromOneNodeHeightToTheOther() {
		List<Cross> strip = IchorianSigilGougeGeometry.strip(0.0D, 64.1D, 0.0D, 3.0D, 65.1D, 1.0D);
		assertTrue(strip.size() >= 2);
		assertEquals(64.012D, strip.get(0).y(), EPSILON);
		assertEquals(65.012D, strip.get(strip.size() - 1).y(), EPSILON);
		double previous = Double.NEGATIVE_INFINITY;
		for (Cross cross : strip) {
			assertFalse(Double.isNaN(cross.leftX()) || Double.isNaN(cross.leftZ())
					|| Double.isNaN(cross.rightX()) || Double.isNaN(cross.rightZ())
					|| Double.isNaN(cross.y()) || Float.isNaN(cross.along()) || Float.isNaN(cross.alongBlocks()));
			assertTrue(cross.y() > previous, "strip keeps rising");
			previous = cross.y();
		}
	}

	@Test
	void leftStaysOnTheSameSideWhateverTheDirection() {
		assertEquals(winding(0.0D, 0.0D, 4.0D, 0.0D), winding(0.0D, 0.0D, 0.0D, -4.0D));
		assertEquals(winding(0.0D, 0.0D, 4.0D, 0.0D), winding(5.0D, 2.0D, -1.0D, 3.0D));
	}

	@Test
	void degenerateLinksEmitNothing() {
		assertTrue(IchorianSigilGougeGeometry.strip(1.0D, 64.1D, 1.0D, 1.0D, 64.1D, 1.0D).isEmpty());
		assertTrue(IchorianSigilGougeGeometry.strip(1.0D, 64.1D, 1.0D, 1.01D, 64.1D, 1.0D).isEmpty());
	}

	/** Sign of direction x (left - centre) in the XZ plane, the same for every section. */
	private static double winding(double startX, double startZ, double endX, double endZ) {
		List<Cross> strip = IchorianSigilGougeGeometry.strip(startX, 64.1D, startZ, endX, 64.1D, endZ);
		double dirX = endX - startX;
		double dirZ = endZ - startZ;
		double sign = 0.0D;
		for (Cross cross : strip) {
			double centreX = (cross.leftX() + cross.rightX()) * 0.5D;
			double centreZ = (cross.leftZ() + cross.rightZ()) * 0.5D;
			double cross2d = dirX * (cross.leftZ() - centreZ) - dirZ * (cross.leftX() - centreX);
			double here = Math.signum(cross2d);
			assertTrue(here != 0.0D);
			if (sign == 0.0D) {
				sign = here;
			}
			assertEquals(sign, here);
		}
		return sign;
	}
}
