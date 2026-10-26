package com.vincenthuto.hemomancy.common.entity.boss.presentation;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossIntroWindowTest {
	@Test
	void inactiveUntilBegun() {
		BossIntroWindow window = new BossIntroWindow(10);
		assertFalse(window.active());
		assertEquals(-1, window.elapsed());
		assertFalse(window.tick(), "nothing to end");
	}

	@Test
	void runsExactlyLength() {
		BossIntroWindow window = new BossIntroWindow(10);
		window.begin();
		for (int t = 0; t < 9; t++) {
			assertTrue(window.active(), "tick " + t);
			assertEquals(t, window.elapsed());
			assertFalse(window.tick());
		}
		assertTrue(window.active());
		assertTrue(window.tick(), "reports the tick it ends on");
		assertFalse(window.active());
	}

	@Test
	void survivesSaveLoad() {
		BossIntroWindow window = new BossIntroWindow(60);
		window.begin();
		for (int t = 0; t < 25; t++) window.tick();
		CompoundTag tag = new CompoundTag();
		window.save(tag, "Intro");
		BossIntroWindow loaded = new BossIntroWindow(60);
		loaded.load(tag, "Intro");
		assertTrue(loaded.active());
		assertEquals(25, loaded.elapsed());
	}

	@Test
	void endedWindowStaysEnded() {
		BossIntroWindow window = new BossIntroWindow(3);
		window.begin();
		for (int t = 0; t < 3; t++) window.tick();
		for (int t = 0; t < 10; t++) assertFalse(window.tick());
		assertFalse(window.active());
		CompoundTag tag = new CompoundTag();
		window.save(tag, "Intro");
		BossIntroWindow loaded = new BossIntroWindow(3);
		loaded.load(tag, "Intro");
		assertFalse(loaded.active(), "an ended window loads ended");
	}
}
