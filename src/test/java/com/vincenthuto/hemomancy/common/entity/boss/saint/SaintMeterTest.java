package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Saint meter: drift, floor, ceiling, thresholds both ways, per-player states, scaling and the send rules. */
final class SaintMeterTest {
	private static final UUID A = new UUID(0, 1);
	private static final UUID B = new UUID(0, 2);

	/** A Hemorath-like meter: 100 per challenger scaled by the kit rule, leaking 1 a tick, thresholds at a half and full. */
	private static final class Leak implements SaintMeter.Rule {
		final List<String> crossed = new ArrayList<>();
		float floor;
		float playerDrift;

		@Override
		public float max(int challengers) {
			return 100.0F * SaintKit.scale(challengers);
		}

		@Override
		public float drift(SaintTrialEntity saint, float value, float max) {
			return -1.0F;
		}

		@Override
		public float drift(SaintTrialEntity saint, Player player, SaintMeter.PlayerState state) {
			return playerDrift;
		}

		@Override
		public float floor(SaintTrialEntity saint) {
			return floor;
		}

		@Override
		public float[] thresholds() {
			return new float[]{0.5F, 1.0F};
		}

		@Override
		public void crossed(SaintTrialEntity saint, Player player, int threshold, boolean rising) {
			crossed.add(threshold + (rising ? "+" : "-"));
		}
	}

	private static Map<UUID, Player> roster(UUID... ids) {
		Map<UUID, Player> roster = new LinkedHashMap<>();
		for (UUID id : ids) roster.put(id, null);
		return roster;
	}

	@Test
	void driftFloorAndCeiling() {
		Leak rule = new Leak();
		SaintMeter meter = new SaintMeter(SaintMeter.Owner.SAINT, rule);
		meter.bind(null);
		assertEquals(60.0F, meter.add(60.0F, A));
		meter.tick(roster());
		assertEquals(59.0F, meter.value());
		assertEquals(41.0F, meter.add(80.0F, (UUID) null), "the ceiling clamps the add");
		assertEquals(100.0F, meter.value());
		rule.floor = 99.5F;
		meter.tick(roster());
		assertEquals(99.5F, meter.value(), "the floor stops the leak");
		rule.floor = 0.0F;
		meter.set(0.5F);
		meter.tick(roster());
		assertEquals(0.0F, meter.value(), "never under zero");
	}

	@Test
	void thresholdsCrossBothWays() {
		Leak rule = new Leak();
		SaintMeter meter = new SaintMeter(SaintMeter.Owner.SAINT, rule);
		meter.bind(null);
		meter.add(50.0F, A);
		meter.add(50.0F, A);
		meter.tick(roster());
		meter.set(10.0F);
		assertEquals(List.of("0+", "1+", "1-", "0-"), rule.crossed);
	}

	@Test
	void theLastContributorIsTheLastToFill() {
		SaintMeter meter = new SaintMeter(SaintMeter.Owner.SAINT, new Leak());
		meter.bind(null);
		assertNull(meter.lastContributor());
		meter.add(5.0F, A);
		meter.add(5.0F, B);
		meter.add(-3.0F, A);
		assertEquals(B, meter.lastContributor(), "draining does not take the credit");
	}

	@Test
	void theMaximumScalesWithChallengers() {
		SaintMeter meter = new SaintMeter(SaintMeter.Owner.SAINT, new Leak());
		meter.bind(null);
		assertEquals(100.0F, meter.max());
		meter.challengers(3);
		assertEquals(200.0F, meter.max(), "1 + 0.5 (n - 1)");
		SaintMeter stillness = new SaintMeter(SaintMeter.Owner.PLAYER, new Leak());
		stillness.challengers(3);
		assertEquals(100.0F, stillness.playerMax(), "per-player meters never scale");
		assertEquals(1.0F, SaintKit.scale(1));
		assertEquals(2.5F, SaintKit.scale(4));
	}

	@Test
	void perPlayerStatesDriftCountDownAndLeaveWithTheRoster() {
		Leak rule = new Leak();
		rule.playerDrift = 2.0F;
		SaintMeter meter = new SaintMeter(SaintMeter.Owner.PLAYER, rule);
		meter.bind(null);
		meter.stateTicks(A, 3);
		meter.flags(A, 0b101);
		meter.secondary(A, 4.0F, 8.0F);
		meter.tick(roster(A, B));
		assertEquals(2.0F, meter.player(A).value());
		assertEquals(2.0F, meter.player(B).value());
		assertEquals(2, meter.player(A).stateTicks(), "the countdown runs");
		assertEquals(0b101, meter.player(A).flags());
		assertEquals(0.5F, meter.player(A).secondary() / meter.player(A).secondaryMax());
		meter.add(A, 49.0F);
		assertEquals(List.of("0+"), rule.crossed, "a per-player meter's thresholds cross per player");
		meter.tick(roster(B));
		assertNull(meter.existing(A), "a challenger off the roster loses its state");
		assertEquals(4.0F, meter.player(B).value());
		meter.add(B, 46.0F);
		assertEquals(128, meter.coarse(meter.player(B)), "the coarse copy is 0..255 of the maximum");
	}

	@Test
	void sendRules() {
		assertTrue(SaintMeter.dataDue(Float.NaN, 0.0F, 100.0F, 0), "a first send");
		assertFalse(SaintMeter.dataDue(50.0F, 50.05F, 100.0F, 1));
		assertTrue(SaintMeter.dataDue(50.0F, 50.125F, 100.0F, 1), "0.1% of the maximum");
		assertTrue(SaintMeter.dataDue(50.0F, 50.01F, 100.0F, 10), "or every 10 ticks");
		assertFalse(SaintMeter.dataDue(50.0F, 50.0F, 100.0F, 30), "nothing changed");
		assertTrue(SaintMeter.packetDue(Float.NaN, 0, Float.NaN, 0, 100, 1, 0, 0, false, false, 0), "joining");
		assertFalse(SaintMeter.packetDue(10, 20, 0, 0, 100, 1, 0, 0, false, false, 1), "never more than every 2 ticks");
		assertTrue(SaintMeter.packetDue(10, 10.25F, 0, 0, 100, 1, 0, 0, false, false, 2), "0.25% of the maximum");
		assertFalse(SaintMeter.packetDue(10, 10.2F, 0, 0, 100, 1, 0, 0, false, false, 2));
		assertTrue(SaintMeter.packetDue(10, 10, 0, 0, 100, 1, 0, 1, false, false, 2), "a flag changed");
		assertTrue(SaintMeter.packetDue(10, 10, 0, 0, 100, 1, 0, 0, false, true, 2), "a countdown started");
		assertTrue(SaintMeter.packetDue(10, 10, 0, 0, 100, 1, 0, 0, false, false, 20), "the keep-alive");
		SaintMeter.PlayerState state = new SaintMeter(SaintMeter.Owner.PLAYER, new Leak()).player(A);
		assertTrue(state.packetDue(100.0F));
		state.markSent();
		assertFalse(state.packetDue(100.0F));
		state.forgetSent();
		assertTrue(state.packetDue(100.0F), "a challenger rejoining gets a fresh send");
	}

	@Test
	void saveAndLoad() {
		SaintMeter meter = new SaintMeter(SaintMeter.Owner.SAINT, new Leak());
		meter.bind(null);
		meter.add(42.0F, A);
		meter.add(B, 7.0F);
		meter.flags(B, 3);
		CompoundTag tag = new CompoundTag();
		meter.save(tag);
		SaintMeter loaded = new SaintMeter(SaintMeter.Owner.SAINT, new Leak());
		loaded.load(tag);
		assertEquals(42.0F, loaded.value());
		assertEquals(A, loaded.lastContributor());
		assertEquals(7.0F, loaded.player(B).value());
		assertEquals(3, loaded.player(B).flags());
	}
}
