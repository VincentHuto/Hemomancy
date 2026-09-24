package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NaeglerophaeonCombatRulesTest {
	@Test
	void gripStaysBelowTheFaceAndTurnsWithTheVictim() {
		var feet=new net.minecraft.world.phys.Vec3(10,80,10);
		for(float yaw:new float[]{0,90,180,270}) {
			var grip=NaeglerophaeonCombatRules.gripPosition(feet,1.8,yaw).subtract(feet);
			assertEquals(.48,grip.horizontalDistance(),1e-6);
			assertTrue(grip.y<1.2);
		}
	}

	@Test
	void eightDamageBreaksGrab() {
		assertFalse(NaeglerophaeonCombatRules.breaksGrab(7.9F));
		assertTrue(NaeglerophaeonCombatRules.breaksGrab(8.0F));
	}
}
