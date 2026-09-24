package com.vincenthuto.hemomancy.common.worldgen.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class VagrantMindFiberWebTest {
	private static List<VagrantMindGeometry.Lobe> lobes() {
		return VagrantMindGeometry.lobes(42L);
	}

	@Test
	void webSpansTheCavity() {
		List<VagrantMindFiberWeb.Strand> strands = VagrantMindFiberWeb.strands(42L, lobes());
		assertTrue(strands.size() >= 15, "a cavity this large needs a dense web, got " + strands.size());
	}

	@Test
	void webIsDeterministic() {
		assertEquals(VagrantMindFiberWeb.strands(42L, lobes()), VagrantMindFiberWeb.strands(42L, lobes()));
	}

	@Test
	void everyStrandHasLength() {
		for (VagrantMindFiberWeb.Strand strand : VagrantMindFiberWeb.strands(42L, lobes())) {
			double dx = strand.bx() - strand.ax();
			double dy = strand.by() - strand.ay();
			double dz = strand.bz() - strand.az();
			assertTrue(Math.sqrt(dx * dx + dy * dy + dz * dz) > 4.0,
					"degenerate strands would place single floating fiber blocks");
		}
	}

	@Test
	void branchPointsBecomeNodes() {
		List<VagrantMindFiberWeb.Strand> strands = VagrantMindFiberWeb.strands(42L, lobes());
		assertTrue(VagrantMindFiberWeb.nodes(strands).size() >= 3,
				"junctions must be lit by synaptic nodes");
	}
}
