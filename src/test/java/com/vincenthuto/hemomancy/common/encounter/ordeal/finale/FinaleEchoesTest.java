package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor;

class FinaleEchoesTest {
	@Test
	void maskRoundTrips() {
		assertEquals(List.of(), FinaleEchoes.roles(FinaleEchoes.roleMask(List.of())));
		assertEquals(List.of("circus_strongman"), FinaleEchoes.roles(FinaleEchoes.roleMask(List.of(OrdealProfessor.STRONGMAN))));
		List<OrdealProfessor> all = OrdealProfessor.all();
		assertEquals(all.stream().map(OrdealProfessor::role).toList(), FinaleEchoes.roles(FinaleEchoes.roleMask(all)));
		// Out of roster order in, roster order out.
		List<OrdealProfessor> reversed = new java.util.ArrayList<>(all);
		java.util.Collections.reverse(reversed);
		assertEquals(all.stream().map(OrdealProfessor::role).toList(), FinaleEchoes.roles(FinaleEchoes.roleMask(reversed)));
	}
}
