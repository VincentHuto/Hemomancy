package com.vincenthuto.hemomancy.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.neoforged.neoforge.common.ModConfigSpec;

class DialogueStyleClientConfigTest {
	@Test
	void dialogueStyleDefaultsToTexturedForEveryNpc() {
		HemoClientConfig.registerClientConfig(new ModConfigSpec.Builder());
		assertEquals(Boolean.TRUE, HemoClientConfig.USE_TEXTURED_DIALOGUE_STYLE.getDefault());
	}
}
