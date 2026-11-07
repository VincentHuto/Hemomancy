package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A Saint's signature resource: a value, a maximum, rising thresholds, a drift (a leak is negative) and a floor. It
 * belongs to the Saint (Hemorath's absorbed blood, Seraphae's integrity, Putriciel's absolutions) or to each player
 * (Velorum's stillness). Either way every challenger has a {@link PlayerState} beside it, for a per-player secondary
 * value, a Saint-defined flag word and a state countdown. Per-player states live on the Saint entity, are dropped when
 * the arena's roster drops the player, and are never player data.
 * <p>
 * A Saint-owned maximum scales with the challenger count (the Rule decides how); a per-player maximum is read with one
 * challenger, because per-player meters never scale.
 */
public final class SaintMeter {
	/** The entity data resend threshold: a change of 0.1% of the maximum. */
	public static final float DATA_STEP = 0.001F;
	public static final int DATA_KEEP_ALIVE = 10;
	/** The {@code SaintMeterPacket} thresholds: 0.25% of the maximum, at most every 2 ticks, a keep-alive at 20. */
	public static final float PACKET_STEP = 0.0025F;
	public static final int PACKET_MIN_GAP = 2;
	public static final int PACKET_KEEP_ALIVE = 20;

	public enum Owner { SAINT, PLAYER }

	public interface Rule {
		float max(int challengers);

		default float start(@Nullable SaintTrialEntity saint) {
			return 0.0F;
		}

		/** Saint-owned: change per tick (a leak is negative), from the Saint's live state. */
		default float drift(@Nullable SaintTrialEntity saint, float value, float max) {
			return 0.0F;
		}

		/** Per-player: change per tick for one challenger. */
		default float drift(@Nullable SaintTrialEntity saint, @Nullable Player player, PlayerState state) {
			return 0.0F;
		}

		default float floor(@Nullable SaintTrialEntity saint) {
			return 0.0F;
		}

		/** Fractions of the maximum, rising. */
		default float[] thresholds() {
			return new float[0];
		}

		/** A threshold crossed; {@code player} is null for the Saint's own value. */
		default void crossed(@Nullable SaintTrialEntity saint, @Nullable Player player, int threshold, boolean rising) {
		}
	}

	/** A rule with a fixed maximum and nothing else, for Saints whose meter is not yet built. */
	public static Rule fixed(float max) {
		return challengers -> max;
	}

	public static final class PlayerState {
		private float value;
		private float secondary;
		private float secondaryMax = 1.0F;
		private int flags;
		private int stateTicks;
		private float sentValue = Float.NaN;
		private float sentSecondary = Float.NaN;
		private int sentFlags;
		private boolean sentRunning;
		private int sinceSent = Integer.MAX_VALUE / 2;

		public float value() {
			return value;
		}

		public float secondary() {
			return secondary;
		}

		public float secondaryMax() {
			return secondaryMax;
		}

		public int flags() {
			return flags;
		}

		public int stateTicks() {
			return stateTicks;
		}

		/** Whether a {@code SaintMeterPacket} is due for this state against a maximum of {@code max}. */
		public boolean packetDue(float max) {
			return SaintMeter.packetDue(sentValue, value, sentSecondary, secondary, max, secondaryMax, sentFlags, flags,
					sentRunning, stateTicks > 0, sinceSent);
		}

		/** Records that the current state was sent. */
		public void markSent() {
			sentValue = value;
			sentSecondary = secondary;
			sentFlags = flags;
			sentRunning = stateTicks > 0;
			sinceSent = 0;
		}

		/** Forgets the last send, so the next check sends (a challenger joining the roster). */
		public void forgetSent() {
			sentValue = Float.NaN;
			sinceSent = Integer.MAX_VALUE / 2;
		}

		void age() {
			if (sinceSent < Integer.MAX_VALUE / 2) sinceSent++;
			if (stateTicks > 0) stateTicks--;
		}

		CompoundTag save(UUID id) {
			CompoundTag tag = new CompoundTag();
			tag.putUUID("id", id);
			tag.putFloat("value", value);
			tag.putFloat("secondary", secondary);
			tag.putFloat("secondary_max", secondaryMax);
			tag.putInt("flags", flags);
			tag.putInt("state_ticks", stateTicks);
			return tag;
		}

		void load(CompoundTag tag) {
			value = tag.getFloat("value");
			secondary = tag.getFloat("secondary");
			secondaryMax = tag.contains("secondary_max") ? tag.getFloat("secondary_max") : 1.0F;
			flags = tag.getInt("flags");
			stateTicks = tag.getInt("state_ticks");
		}
	}

	private final Owner owner;
	private final Rule rule;
	private final Map<UUID, PlayerState> players = new LinkedHashMap<>();
	@Nullable
	private SaintTrialEntity saint;
	private float value;
	private boolean started;
	private int challengers = 1;
	@Nullable
	private UUID lastContributor;
	private float dataSent = Float.NaN;
	private int sinceData;

	public SaintMeter(Owner owner, Rule rule) {
		this.owner = owner;
		this.rule = rule;
	}

	/** Called once by the kit: the Saint thresholds and drift read. */
	void bind(@Nullable SaintTrialEntity saint) {
		this.saint = saint;
		if (!started) {
			started = true;
			value = bounded(rule.start(saint));
		}
	}

	public Owner owner() {
		return owner;
	}

	public Rule rule() {
		return rule;
	}

	public float value() {
		return value;
	}

	public float max() {
		return Math.max(1.0E-4F, rule.max(Math.max(1, challengers)));
	}

	/** A per-player value's maximum: the Saint's maximum for a Saint-owned meter, one challenger's for a per-player one. */
	public float playerMax() {
		return owner == Owner.PLAYER ? Math.max(1.0E-4F, rule.max(1)) : max();
	}

	public float fraction() {
		return Mth.clamp(value / max(), 0.0F, 1.0F);
	}

	public float floor() {
		return rule.floor(saint);
	}

	/** The challenger count the maximum is read with. */
	public void challengers(int count) {
		challengers = Math.max(1, count);
	}

	/** Adds to the Saint's value, clamped between the floor and the maximum; returns what moved. */
	public float add(float amount, @Nullable Player contributor) {
		return add(amount, contributor == null ? null : contributor.getUUID(), contributor);
	}

	public float add(float amount, @Nullable UUID contributor) {
		return add(amount, contributor, null);
	}

	private float add(float amount, @Nullable UUID contributor, @Nullable Player player) {
		float before = value;
		value = bounded(value + amount);
		float moved = value - before;
		if (moved > 0.0F && contributor != null) lastContributor = contributor;
		crossings(before, value, max(), null);
		return moved;
	}

	public void set(float next) {
		float before = value;
		value = bounded(next);
		crossings(before, value, max(), null);
	}

	@Nullable
	public UUID lastContributor() {
		return lastContributor;
	}

	/** The player's state, made on first read. */
	public PlayerState player(Player player) {
		return player(player.getUUID());
	}

	public PlayerState player(UUID id) {
		return players.computeIfAbsent(id, ignored -> new PlayerState());
	}

	@Nullable
	public PlayerState existing(UUID id) {
		return players.get(id);
	}

	public Map<UUID, PlayerState> players() {
		return Collections.unmodifiableMap(players);
	}

	/** Adds to one challenger's value, clamped to 0..playerMax; returns what moved. */
	public float add(Player player, float amount) {
		return add(player.getUUID(), player, amount);
	}

	public float add(UUID id, float amount) {
		return add(id, null, amount);
	}

	private float add(UUID id, @Nullable Player player, float amount) {
		PlayerState state = player(id);
		float before = state.value;
		float max = playerMax();
		state.value = Mth.clamp(state.value + amount, 0.0F, max);
		// thresholds are fractions of the meter itself: a Saint-owned meter's per-player values cross none
		if (owner == Owner.PLAYER) crossings(before, state.value, max, player);
		return state.value - before;
	}

	public void secondary(Player player, float secondary, float secondaryMax) {
		secondary(player.getUUID(), secondary, secondaryMax);
	}

	public void secondary(UUID id, float secondary, float secondaryMax) {
		PlayerState state = player(id);
		state.secondaryMax = Math.max(1.0E-4F, secondaryMax);
		state.secondary = Mth.clamp(secondary, 0.0F, state.secondaryMax);
	}

	public void flags(Player player, int flags) {
		flags(player.getUUID(), flags);
	}

	public void flags(UUID id, int flags) {
		player(id).flags = flags;
	}

	public void stateTicks(Player player, int ticks) {
		stateTicks(player.getUUID(), ticks);
	}

	public void stateTicks(UUID id, int ticks) {
		player(id).stateTicks = Math.max(0, ticks);
	}

	/** Drops the states of players no longer on the roster. */
	public void retain(Set<UUID> roster) {
		players.keySet().removeIf(id -> !roster.contains(id));
	}

	/** One tick of the kit: drift, floor and thresholds, against the arena's challengers. */
	public void tick(SaintTrialEntity saint) {
		SaintArena arena = saint.arena();
		Collection<Player> roster = arena == null ? java.util.List.of() : arena.challengers();
		if (arena != null) challengers(arena.challengerCount());
		Map<UUID, Player> byId = new LinkedHashMap<>();
		for (Player player : roster) byId.put(player.getUUID(), player);
		tick(byId);
	}

	/**
	 * One tick against a roster: the Saint value drifts and keeps its floor; each listed challenger gets a state, its
	 * drift and its countdown; states off the roster are dropped. {@code roster} may map to null players in tests.
	 */
	public void tick(Map<UUID, Player> roster) {
		float before = value;
		value = bounded(value + rule.drift(saint, value, max()));
		crossings(before, value, max(), null);
		retain(roster.keySet());
		for (Map.Entry<UUID, Player> entry : roster.entrySet()) {
			PlayerState state = player(entry.getKey());
			float drift = rule.drift(saint, entry.getValue(), state);
			if (drift != 0.0F) add(entry.getKey(), entry.getValue(), drift);
			state.age();
		}
		if (sinceData < Integer.MAX_VALUE) sinceData++;
	}

	/** Whether the entity data copy of the Saint value is due (0.1% of the maximum, or 10 ticks). */
	public boolean dataDue() {
		return dataDue(dataSent, value, max(), sinceData);
	}

	public void markDataSent() {
		dataSent = value;
		sinceData = 0;
	}

	/** The coarse per-player value carried in {@code DATA_CHALLENGERS}: 0..255 of the maximum. */
	public int coarse(PlayerState state) {
		return Mth.clamp(Math.round(state.value / playerMax() * 255.0F), 0, 255);
	}

	public static boolean dataDue(float sent, float value, float max, int ticksSince) {
		if (Float.isNaN(sent)) return true;
		if (Math.abs(value - sent) >= DATA_STEP * max) return true;
		return value != sent && ticksSince >= DATA_KEEP_ALIVE;
	}

	/**
	 * The {@code SaintMeterPacket} rule: never more often than every 2 ticks; then on a first send, a change of 0.25% of
	 * either maximum, any flag change, a state countdown starting or stopping, or 20 ticks since the last send.
	 */
	public static boolean packetDue(float sentValue, float value, float sentSecondary, float secondary, float max,
			float secondaryMax, int sentFlags, int flags, boolean sentRunning, boolean running, int ticksSince) {
		if (Float.isNaN(sentValue)) return true;
		if (ticksSince < PACKET_MIN_GAP) return false;
		if (ticksSince >= PACKET_KEEP_ALIVE) return true;
		if (Math.abs(value - sentValue) >= PACKET_STEP * max) return true;
		if (!Float.isNaN(sentSecondary) && Math.abs(secondary - sentSecondary) >= PACKET_STEP * secondaryMax) return true;
		return flags != sentFlags || running != sentRunning;
	}

	public void save(CompoundTag tag) {
		CompoundTag meter = new CompoundTag();
		meter.putFloat("value", value);
		meter.putBoolean("started", started);
		meter.putInt("challengers", challengers);
		if (lastContributor != null) meter.putUUID("last_contributor", lastContributor);
		ListTag list = new ListTag();
		players.forEach((id, state) -> list.add(state.save(id)));
		meter.put("players", list);
		tag.put("SaintMeter", meter);
	}

	public void load(CompoundTag tag) {
		CompoundTag meter = tag.getCompound("SaintMeter");
		value = meter.getFloat("value");
		started = meter.getBoolean("started");
		challengers = Math.max(1, meter.getInt("challengers"));
		lastContributor = meter.hasUUID("last_contributor") ? meter.getUUID("last_contributor") : null;
		players.clear();
		ListTag list = meter.getList("players", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			if (!entry.hasUUID("id")) continue;
			PlayerState state = new PlayerState();
			state.load(entry);
			players.put(entry.getUUID("id"), state);
		}
		dataSent = Float.NaN;
	}

	private float bounded(float next) {
		float max = max();
		return Mth.clamp(next, Math.min(rule.floor(saint), max), max);
	}

	private void crossings(float before, float after, float max, @Nullable Player player) {
		if (before == after) return;
		float[] thresholds = rule.thresholds();
		float from = before / max, to = after / max;
		for (int i = 0; i < thresholds.length; i++) {
			float at = thresholds[i];
			if (from < at && to >= at) rule.crossed(saint, player, i, true);
			else if (from >= at && to < at) rule.crossed(saint, player, i, false);
		}
	}
}
