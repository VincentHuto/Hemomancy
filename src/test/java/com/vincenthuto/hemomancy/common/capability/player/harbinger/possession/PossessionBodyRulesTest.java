package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PossessionBodyRulesTest {
	@Test
	void eachFormTakesItsMobsRoom() {
		assertBody(SurrogateForm.BAT, 0.5f, 0.9f, 0.45f);
		assertBody(SurrogateForm.ENDERMAN, 0.6f, 2.9f, 2.55f);
		assertBody(SurrogateForm.BLAZE, 0.6f, 1.8f, 1.53f);
		assertBody(SurrogateForm.GOLEM, 1.4f, 2.7f, 2.295f);
		assertBody(SurrogateForm.HORSE, 1.3964844f, 1.6f, 1.52f);
		assertBody(SurrogateForm.DONKEY, 1.3964844f, 1.5f, 1.425f);
		assertBody(SurrogateForm.WOLF, 0.6f, 0.85f, 0.68f);
		assertBody(SurrogateForm.SPIDER, 1.4f, 0.9f, 0.65f);
		assertBody(SurrogateForm.ARMOR_STAND, 0.5f, 1.975f, 1.7775f);
	}

	@Test
	void everyEyeSitsInsideItsBody() {
		for (SurrogateForm form : SurrogateForm.values()) {
			PossessionBodyRules.Body body = PossessionBodyRules.body(form);
			assertTrue(body.eyeHeight() > 0f && body.eyeHeight() < body.height(), form.name());
		}
	}

	private static void assertBody(SurrogateForm form, float width, float height, float eye) {
		PossessionBodyRules.Body body = PossessionBodyRules.body(form);
		assertEquals(width, body.width(), 1e-6, form + " width");
		assertEquals(height, body.height(), 1e-6, form + " height");
		assertEquals(eye, body.eyeHeight(), 1e-6, form + " eye");
	}
}
