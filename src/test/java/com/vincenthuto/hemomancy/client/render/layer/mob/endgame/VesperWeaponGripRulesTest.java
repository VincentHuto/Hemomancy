package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

final class VesperWeaponGripRulesTest {
	@Test
	void pairedLivingClawsUseMirroredHandedYaw() throws Exception {
		Method yaw = rules().getMethod("yawDegrees", EnumBloodTemper.class, boolean.class);
		assertEquals(180.0F, (float) yaw.invoke(null, EnumBloodTemper.TENEBRIS, false), 0.001F);
		assertEquals(0.0F, (float) yaw.invoke(null, EnumBloodTemper.TENEBRIS, true), 0.001F);
	}

	@Test
	void singleWeaponFormsKeepTheirExistingHandedYaw() throws Exception {
		Method yaw = rules().getMethod("yawDegrees", EnumBloodTemper.class, boolean.class);
		assertEquals(180.0F, (float) yaw.invoke(null, EnumBloodTemper.ANIMUS, false), 0.001F);
		assertEquals(0.0F, (float) yaw.invoke(null, EnumBloodTemper.ANIMUS, true), 0.001F);
	}

	private static Class<?> rules() {
		try {
			return Class.forName("com.vincenthuto.hemomancy.client.render.layer.mob.endgame.VesperWeaponGripRules");
		} catch (ClassNotFoundException missing) {
			return fail("Vesper weapon grip rules are missing");
		}
	}
}
