package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One opening at a time, chains, extension caps, hit caps with challengers, admit results and the time since. */
final class SaintOpeningsTest {
	private static final SaintOpening.Spec PLANTED = new SaintOpening.Spec(1, "planted", 24, 1.5F, 3, 1,
			EnumSet.of(SaintOpening.Delivery.MELEE, SaintOpening.Delivery.SCHOOL_CAST));
	private static final SaintOpening.Spec BOW = new SaintOpening.Spec(2, "bow", 70, 2.0F, 0, 0,
			EnumSet.allOf(SaintOpening.Delivery.class));

	@Test
	void oneAtATime() {
		List<String> changes = new ArrayList<>();
		SaintOpenings openings = new SaintOpenings((from, to) -> changes.add((from == null ? "-" : from.key()) + ">"
				+ (to == null ? "-" : to.key())));
		assertEquals(1.0F, openings.damageTaken(), "1 when closed");
		assertTrue(openings.open(PLANTED, 0));
		assertEquals(24, openings.remaining(), "its own length");
		assertFalse(openings.open(BOW, 10), "false while another is open");
		assertTrue(openings.isOpen(PLANTED));
		assertEquals(1.5F, openings.damageTaken());
		for (int i = 0; i < 24; i++) openings.tick();
		assertFalse(openings.isOpen());
		assertEquals(List.of("->planted", "planted>-"), changes);
	}

	@Test
	void chainToClosesStraightIntoTheNext() {
		SaintOpenings openings = new SaintOpenings();
		openings.open(PLANTED, 10);
		openings.tick();
		openings.chainTo(BOW, 40);
		assertTrue(openings.isOpen(BOW));
		assertEquals(0, openings.age());
		assertEquals(40, openings.remaining());
		assertEquals(0, openings.hits());
	}

	@Test
	void extensionIsCappedPerWindow() {
		SaintOpenings openings = new SaintOpenings();
		assertEquals(0, openings.extend(10, 20), "nothing to extend when closed");
		openings.open(BOW, 30);
		assertEquals(10, openings.extend(10, 25));
		assertEquals(15, openings.extend(10, 25) + openings.extend(10, 25), "the cap holds over the window");
		assertEquals(55, openings.remaining());
		assertEquals(55, openings.length());
		openings.close();
		openings.open(BOW, 30);
		assertEquals(10, openings.extend(10, 25), "a new window has its own cap");
	}

	@Test
	void hitCapsGrowWithChallengers() {
		SaintOpenings openings = new SaintOpenings();
		assertEquals(SaintOpening.Admit.CLOSED, openings.admit(SaintOpening.Delivery.MELEE, 1));
		openings.open(PLANTED, 0);
		assertEquals(SaintOpening.Admit.NOT_COUNTED, openings.admit(SaintOpening.Delivery.PROJECTILE, 1));
		for (int i = 0; i < 3; i++) assertEquals(SaintOpening.Admit.COUNTED, openings.admit(SaintOpening.Delivery.MELEE, 1));
		assertEquals(SaintOpening.Admit.CAPPED, openings.admit(SaintOpening.Delivery.MELEE, 1));
		assertEquals(SaintOpening.Admit.COUNTED, openings.admit(SaintOpening.Delivery.SCHOOL_CAST, 3),
				"each challenger past the first adds to the cap");
		assertEquals(SaintOpening.Admit.COUNTED, openings.admit(SaintOpening.Delivery.MELEE, 3));
		assertEquals(SaintOpening.Admit.CAPPED, openings.admit(SaintOpening.Delivery.MELEE, 3));
		assertEquals(5, openings.hits());
		assertEquals(0, BOW.cap(4), "no cap");
	}

	@Test
	void ticksSinceClosed() {
		SaintOpenings openings = new SaintOpenings();
		openings.open(PLANTED, 2);
		assertEquals(0, openings.ticksSinceClosed(), "0 while open");
		openings.tick();
		openings.tick();
		assertFalse(openings.isOpen());
		openings.tick();
		openings.tick();
		openings.tick();
		assertEquals(3, openings.ticksSinceClosed());
	}

	@Test
	void saveAndLoad() {
		SaintOpenings openings = new SaintOpenings();
		openings.open(PLANTED, 20);
		openings.tick();
		openings.admit(SaintOpening.Delivery.MELEE, 1);
		CompoundTag tag = new CompoundTag();
		openings.save(tag);
		SaintOpenings loaded = new SaintOpenings();
		loaded.load(tag);
		assertEquals(PLANTED, loaded.current());
		assertEquals(1, loaded.age());
		assertEquals(19, loaded.remaining());
		assertEquals(1, loaded.hits());
		SaintOpenings closed = new SaintOpenings();
		CompoundTag empty = new CompoundTag();
		closed.save(empty);
		closed.load(empty);
		assertNull(closed.current());
	}
}
