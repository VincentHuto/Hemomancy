package com.vincenthuto.hemomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DialogueStyleClientConfigTest {
	@Test
	void dialogueStyleDefaultsToTexturedForEveryNpc() {
		HemoClientConfig.registerClientConfig(new ModConfigSpec.Builder());
		assertEquals(Boolean.TRUE, HemoClientConfig.USE_TEXTURED_DIALOGUE_STYLE.getDefault());
	}
}
