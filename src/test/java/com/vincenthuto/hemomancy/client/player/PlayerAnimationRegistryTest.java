package com.vincenthuto.hemomancy.client.player;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The third-person poses keep the order the mixin and {@code PlayerAnimationClientState} applied them in. */
class PlayerAnimationRegistryTest {
	@Test
	void thirdPersonPosesKeepTheirPriorityOrder() {
		assertEquals(List.of("cardinal_staff_planting", "offset_reset", "casting", "hematic_microscope",
						"blood_vial_injection", "living_torch_breath", "unstained_weapon_swing", "living_weapon_stroke",
						"warp_chair"),
				PlayerAnimationRegistry.THIRD_PERSON.stream().map(PlayerAnimationRegistry.Entry::name).toList());
	}

	@Test
	void onlyTheAnimationPosesClaimTheFrame() {
		assertEquals(List.of("hematic_microscope", "blood_vial_injection", "living_torch_breath",
						"unstained_weapon_swing", "living_weapon_stroke"),
				PlayerAnimationRegistry.THIRD_PERSON.stream().filter(PlayerAnimationRegistry.Entry::claims)
						.map(PlayerAnimationRegistry.Entry::name).toList());
	}
}
