package com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.HematicMemoryExpression;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VascularDiagnosisRulesTest {

	@Test
	void recommendsMendingForClottedOrWidespreadVaricoseDamage() {
		assertEquals(VascularDiagnosisRules.Treatment.VASCULAR_MENDING,
				VascularDiagnosisRules.diagnose(system(100F, 10F, 100F, 100F, 100F), List.of()).treatment());
		assertEquals(VascularDiagnosisRules.Treatment.VASCULAR_MENDING,
				VascularDiagnosisRules.diagnose(system(40F, 40F, 40F, 100F, 100F), List.of()).treatment());
	}

	@Test
	void recommendsPoulticeSalveOrRestByDamageSpread() {
		assertEquals(VascularDiagnosisRules.Treatment.VASCULAR_POULTICE,
				VascularDiagnosisRules.diagnose(system(40F, 45F, 100F, 100F, 100F), List.of()).treatment());
		assertEquals(VascularDiagnosisRules.Treatment.SANGUINE_SALVE,
				VascularDiagnosisRules.diagnose(system(40F, 100F, 100F, 100F, 100F), List.of()).treatment());
		assertEquals(VascularDiagnosisRules.Treatment.REST_AND_FOOD,
				VascularDiagnosisRules.diagnose(system(80F, 100F, 100F, 100F, 100F), List.of()).treatment());
	}

	@Test
	void diagnosisKeepsRoutesGroupedWithTheirSection() {
		var noetic = new VascularDiagnosisRules.MemoryRoute(EnumVeinSections.HEAD,
				HematicMemoryExpression.NOETIC, "Crimson Sight", 2);
		var thelemic = new VascularDiagnosisRules.MemoryRoute(EnumVeinSections.ARMS,
				HematicMemoryExpression.THELEMIC, "Laboring Arms", 0);

		var diagnosis = VascularDiagnosisRules.diagnose(system(70F, 40F, 100F, 100F, 100F),
				List.of(noetic, thelemic));

		assertEquals(EnumVeinSections.ARMS, diagnosis.worstSection());
		assertEquals(40F, diagnosis.health());
		assertEquals(EnumBloodFlow.VARICOSE, diagnosis.flow());
		assertEquals(List.of(noetic), diagnosis.routes().get(EnumVeinSections.HEAD));
		assertEquals(List.of(thelemic), diagnosis.routes().get(EnumVeinSections.ARMS));
	}

	private static Map<EnumVeinSections, Float> system(float head, float arms, float heart, float body, float legs) {
		return Map.of(
				EnumVeinSections.HEAD, head,
				EnumVeinSections.ARMS, arms,
				EnumVeinSections.HEART, heart,
				EnumVeinSections.BODY, body,
				EnumVeinSections.LEGS, legs);
	}
}
