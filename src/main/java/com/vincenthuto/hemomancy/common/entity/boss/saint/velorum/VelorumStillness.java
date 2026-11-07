package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintHealing;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLines;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMeter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRoot;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The stillness meter on the fight (design section 4): the per-player {@link SaintMeter} rule, and the fight state
 * beside each challenger's value that the meter's own state cannot hold (last position, presence, the preserved hold,
 * the thaw and the cap, the kneel, and where they stood still longest for the Splinter Crown). Fight state on the
 * entity, never player data; a reload restarts the side state and keeps the saved values.
 */
public final class VelorumStillness implements SaintMeter.Rule {
	public static final ResourceLocation PRESERVED_ROOT = Hemomancy.rloc("velorum_preserved");
	/** How often the Stilling slowness is refreshed, and how long it lasts. */
	private static final int SLOW_REFRESH = 5;
	private static final int SLOW_TICKS = 10;
	/** The Splinter Crown reads where a challenger stood still in the last 5 seconds. */
	public static final int STILL_HISTORY = 100;

	/** One challenger's side state. */
	static final class Track {
		@Nullable
		Vec3 last;
		long presenceAt = Long.MIN_VALUE;
		int preservedLeft;
		float preservedHealth;
		float preservedDamage;
		int blows;
		int warmed;
		long thawUntil;
		long capUntil;
		int kneelLeft;
		final ArrayDeque<Long> stillCells = new ArrayDeque<>();
		final Map<Long, Integer> stillCounts = new HashMap<>();
		@Nullable
		Vec3 stillest;
		int stillestCount;
		/** The last ten ticks' travel, newest first, for Arrested Pulse's pace. */
		final double[] travel = new double[VelorumPulseRules.PACE_WINDOW];
		int travelled;
		int gloamLeft;
		/** Ticks since Arrested Pulse marked them, counted by the fight's own tick; {@link #NO_MARK} for none. */
		int markAge = NO_MARK;
		float markWarm;
		int markIdle;
	}

	private static final int NO_MARK = -1;

	private final Map<UUID, Track> tracks = new HashMap<>();

	@Override
	public float max(int challengers) {
		return VelorumStillnessRules.MAX;
	}

	@Override
	public float[] thresholds() {
		return new float[]{VelorumStillnessRules.COLD / VelorumStillnessRules.MAX,
				VelorumStillnessRules.STILLING / VelorumStillnessRules.MAX};
	}

	@Override
	public void crossed(@Nullable SaintTrialEntity saint, @Nullable Player player, int threshold, boolean rising) {
		if (rising && threshold == 0 && player != null) SaintLines.teach(player, "message.hemomancy.velorum.hint.cold");
	}

	/** One challenger's drift this tick (the kit calls it from the meter's tick). */
	@Override
	public float drift(@Nullable SaintTrialEntity saint, @Nullable Player player, SaintMeter.PlayerState state) {
		if (!(saint instanceof VelorumEntity velorum) || player == null) return 0.0F;
		Track track = track(player);
		Vec3 now = player.position();
		double travel = track.last == null ? 0.0D : Math.hypot(now.x - track.last.x, now.z - track.last.z);
		track.last = now;
		System.arraycopy(track.travel, 0, track.travel, 1, track.travel.length - 1);
		track.travel[0] = travel;
		track.travelled = Math.min(track.travel.length, track.travelled + 1);
		long time = player.level().getGameTime();
		if (track.preservedLeft > 0 || velorum.ending()) return 0.0F;
		boolean still = VelorumStillnessRules.still(inputs(velorum, player, track, travel, time));
		if (still) recordStill(track, player.blockPosition());
		float drift = VelorumStillnessRules.drift(inputs(velorum, player, track, travel, time));
		float ceiling = VelorumStillnessRules.ceiling(time < track.capUntil);
		return Math.min(drift, ceiling - state.value());
	}

	private VelorumStillnessRules.Inputs inputs(VelorumEntity velorum, Player player, Track track, double travel, long time) {
		MobEffectInstance slow = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
		int slowness = slow == null ? 0 : slow.getAmplifier() + 1;
		int rime = SchoolStates.levels(player, SchoolState.RIME);
		VelorumCongregation congregation = velorum.congregation();
		boolean watched = VelorumDeedRules.beholding(velorum.deeds().mask()) && congregation.watches(player);
		return new VelorumStillnessRules.Inputs(travel, player.isCrouching(), player.isSprinting(), player.isUsingItem(),
				slowness, rime, track.presenceAt >= time - 1, congregation.shelters(player), watched,
				velorum.openings().isOpen(), velorum.bowing(), velorum.seamDrawn(), velorum.hourHolding(),
				time < track.thawUntil, velorum.dark().inDark(player));
	}

	/** The presence touched this player this tick (the multipliers apply on the next meter tick). */
	public void presence(Player player) {
		track(player).presenceAt = player.level().getGameTime();
	}

	/** Adds stillness from a move, held under the post-preservation cap. */
	public void add(VelorumEntity velorum, Player player, float amount) {
		Track track = track(player);
		if (track.preservedLeft > 0 || velorum.ending()) return;
		SaintMeter meter = velorum.meter();
		float value = meter.player(player).value();
		float ceiling = VelorumStillnessRules.ceiling(player.level().getGameTime() < track.capUntil);
		meter.add(player, Math.min(amount, Math.max(0.0F, ceiling - value)));
	}

	public float value(VelorumEntity velorum, Player player) {
		SaintMeter.PlayerState state = velorum.meter().existing(player.getUUID());
		return state == null ? 0.0F : state.value();
	}

	public boolean preserved(Player player) {
		Track track = tracks.get(player.getUUID());
		return track != null && track.preservedLeft > 0;
	}

	public boolean kneeling(Player player) {
		Track track = tracks.get(player.getUUID());
		return track != null && track.kneelLeft > 0;
	}

	/** Bid to Kneel's hold ended with this challenger at 60 or more: Slowness IV for 30 ticks. */
	public void kneel(Player player, int ticks) {
		track(player).kneelLeft = ticks;
		VelorumMoves.slow(player, ticks, VelorumMoves.SLOWNESS_IV);
	}

	/** Where the challenger stood still longest in the last 5 seconds, else where they stand. */
	public Vec3 stillest(Player player) {
		Track track = tracks.get(player.getUUID());
		return track == null || track.stillest == null ? player.position() : track.stillest;
	}

	/** How much of the last 100 ticks they stood on their stillest spot, 0 to 1: how deep its stain is. */
	public float stillDepth(Player player) {
		Track track = tracks.get(player.getUUID());
		return track == null || track.stillest == null ? 0.0F : track.stillestCount / (float) STILL_HISTORY;
	}

	// ── the gloam and Arrested Pulse (revision 2026-11-06) ───────────

	/** The dark's sight and sound for {@code ticks}, without its fill. */
	public void gloam(Player player, int ticks) {
		Track track = track(player);
		track.gloamLeft = Math.max(track.gloamLeft, ticks);
	}

	public boolean gloamed(Player player) {
		Track track = tracks.get(player.getUUID());
		return track != null && track.gloamLeft > 0;
	}

	/** The claw closes as round this challenger's heart: beats at 30, 60 and 90 ticks from now. */
	public void mark(VelorumEntity velorum, Player player) {
		Track track = track(player);
		track.markAge = 0;
		track.markWarm = 0.0F;
		track.markIdle = 0;
		VelorumEffects.pulseMark(velorum, player);
		if (player instanceof ServerPlayer server) {
			server.playNotifySound(SoundInit.RITE_AMBIENT_HEARTBEAT.get(), SoundSource.HOSTILE, 1.0F, 0.6F);
		}
		SaintLines.teach(player, "message.hemomancy.velorum.hint.pulse");
	}

	public boolean marked(Player player) {
		Track track = tracks.get(player.getUUID());
		return track != null && track.markAge != NO_MARK;
	}

	/** A fed tick of another challenger's held Projection on a marked one; true while it warms. */
	public boolean warmMark(VelorumEntity velorum, Player target) {
		Track track = tracks.get(target.getUUID());
		if (track == null || track.markAge == NO_MARK) return false;
		track.markWarm = VelorumCongregationRules.feed(track.markWarm, true, 0);
		track.markIdle = 0;
		if (VelorumPulseRules.ends(false, (int) track.markWarm, false)) endMark(velorum, target, track);
		return true;
	}

	private static void unmark(Track track) {
		track.markAge = NO_MARK;
		track.markWarm = 0.0F;
		track.markIdle = 0;
	}

	/** The mark ends early (Searing, a warming, a preservation's release): its veins and frost fade off the body. */
	private static void endMark(VelorumEntity velorum, Player player, Track track) {
		if (track.markAge == NO_MARK) return;
		unmark(track);
		VelorumEffects.pulseEnd(velorum, player);
	}

	/** One tick of a mark: Searing or a warming ends it, a beat's tell shows, the beat passes or bursts. */
	private void tickMark(VelorumEntity velorum, Player player, Track track, float value) {
		if (track.markAge == NO_MARK) return;
		if (velorum.ending() || VelorumPulseRules.ends(SchoolStates.has(player, SchoolState.SEARING), (int) track.markWarm,
				false)) {
			endMark(velorum, player, track);
			return;
		}
		if (track.markWarm > 0.0F) {
			track.markIdle++;
			track.markWarm = VelorumCongregationRules.feed(track.markWarm, false, track.markIdle);
		}
		int since = track.markAge++;
		double pace = VelorumPulseRules.averagePace(track.travel, track.travelled);
		switch (VelorumPulseRules.beat(since, pace, value, velorum.openings().isOpen())) {
			case TELL -> {
				// the heart slows: each beat's tell is lower (the client draws the tell from the mark's own clock)
				int beat = VelorumPulseRules.tellAt(since);
				if (player instanceof ServerPlayer server) {
					server.playNotifySound(SoundInit.RITE_AMBIENT_HEARTBEAT.get(), SoundSource.HOSTILE, 1.0F,
							0.55F - 0.05F * beat);
				}
			}
			case PASS -> {
				VelorumEffects.pulseBeat(velorum, player, VelorumPulseRules.beatAt(since));
				VelorumMoves.sound(velorum, player.position(), SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F, 0.8F);
			}
			case BURST -> burst(velorum, player);
			case NONE -> {
			}
		}
		if (VelorumPulseRules.spent(since)) unmark(track);
	}

	/** Cruor spines burst out of the marked: 5, +15, Slowness I; 5 and +10 to anyone else within 1.5. */
	private void burst(VelorumEntity velorum, Player player) {
		VelorumMoves.hurt(velorum, player, VelorumPulseRules.BURST_DAMAGE);
		add(velorum, player, VelorumPulseRules.BURST_STILLNESS);
		VelorumMoves.slow(player, VelorumPulseRules.BURST_SLOW, VelorumMoves.SLOWNESS_I);
		VelorumEffects.pulseBurst(velorum, player);
		VelorumMoves.sound(velorum, player.position(), SoundEvents.GLASS_BREAK, 1.0F, 0.4F);
		VelorumMoves.sound(velorum, player.position(), SoundEvents.PLAYER_HURT_FREEZE, 1.0F, 0.6F);
		if (velorum.arena() == null) return;
		for (Player other : velorum.arena().challengers()) {
			if (other == player || BossFootprint.Shapes.planar(other.position(), player.position()) > VelorumPulseRules.SPLASH) continue;
			if (VelorumMoves.hurt(velorum, other, VelorumPulseRules.SPLASH_DAMAGE)) add(velorum, other, VelorumPulseRules.SPLASH_STILLNESS);
		}
		for (VelorumDevoteeEntity ally : velorum.congregation().devotees(velorum)) {
			if (ally.hostile() || BossFootprint.Shapes.planar(ally.position(), player.position()) > VelorumPulseRules.SPLASH) continue;
			VelorumMoves.hurt(velorum, ally, VelorumPulseRules.SPLASH_DAMAGE);
		}
	}

	/** Another challenger struck this preserved player's ice. */
	public void blow(Player target) {
		Track track = tracks.get(target.getUUID());
		if (track != null && track.preservedLeft > 0) track.blows++;
	}

	/** A fed tick of another's held Projection on this preserved player; true while the warming continues. */
	public boolean warm(Player target) {
		Track track = tracks.get(target.getUUID());
		if (track == null || track.preservedLeft <= 0) return false;
		track.warmed++;
		return true;
	}

	/**
	 * Once a tick, after the kit's meter tick: the flags, the heal multiplier, the Stilling slowness, preservation and
	 * its release, the kneel, for every challenger on the roster.
	 */
	public void tick(VelorumEntity velorum, List<Player> challengers) {
		SaintMeter meter = velorum.meter();
		long time = velorum.level().getGameTime();
		tracks.keySet().retainAll(challengers.stream().map(Player::getUUID).toList());
		for (Player player : challengers) {
			Track track = track(player);
			SaintMeter.PlayerState state = meter.player(player);
			if (track.kneelLeft > 0) track.kneelLeft--;
			tickMark(velorum, player, track, state.value());
			if (track.gloamLeft > 0) track.gloamLeft--;
			if (track.preservedLeft > 0) {
				tickPreserved(velorum, player, track);
			} else if (!velorum.ending() && VelorumStillnessRules.preserves(state.value()) && time >= track.capUntil) {
				preserve(velorum, player, track);
			}
			float value = meter.player(player).value();
			float healing = VelorumStillnessRules.healing(value);
			if (healing < 1.0F && !velorum.ending()) SaintHealing.set(player, healing, 2);
			if (VelorumStillnessRules.stilling(value) && track.preservedLeft <= 0 && time % SLOW_REFRESH == 0) {
				VelorumMoves.slow(player, SLOW_TICKS, VelorumMoves.SLOWNESS_I);
			}
			int flags = 0;
			if (track.preservedLeft > 0) flags |= VelorumStillnessRules.FLAG_PRESERVED;
			if (velorum.openings().isOpen()) flags |= VelorumStillnessRules.FLAG_HALTED;
			if (velorum.congregation().shelters(player)) flags |= VelorumStillnessRules.FLAG_SHELTERED;
			if (VelorumDeedRules.beholding(velorum.deeds().mask()) && velorum.congregation().watches(player)) {
				flags |= VelorumStillnessRules.FLAG_WATCHED;
			}
			if (time < track.thawUntil) flags |= VelorumStillnessRules.FLAG_THAWED;
			if (velorum.bowing()) flags |= VelorumStillnessRules.FLAG_RECEDING;
			if (track.kneelLeft > 0) flags |= VelorumStillnessRules.FLAG_KNEELING;
			if (velorum.dark().inDark(player)) flags |= VelorumStillnessRules.FLAG_DARK;
			if (track.gloamLeft > 0) flags |= VelorumStillnessRules.FLAG_GLOAMED;
			if (track.markAge != NO_MARK) {
				flags |= VelorumStillnessRules.FLAG_MARKED;
				if (VelorumPulseRules.telling(track.markAge)) flags |= VelorumStillnessRules.FLAG_PULSE_TELL;
			}
			if (velorum.congregation().eclipseGripped(player)) flags |= VelorumStillnessRules.FLAG_GRIPPED;
			meter.flags(player, flags);
			meter.stateTicks(player, track.preservedLeft);
		}
	}

	private void preserve(VelorumEntity velorum, Player player, Track track) {
		track.preservedLeft = VelorumStillnessRules.PRESERVED_TICKS;
		track.preservedHealth = player.getHealth();
		track.preservedDamage = 0.0F;
		track.blows = 0;
		track.warmed = 0;
		SaintRoot.root(player, VelorumStillnessRules.PRESERVED_TICKS + 1, PRESERVED_ROOT, true);
		SaintMoves.hurt(velorum, player, VelorumStillnessRules.PRESERVED_DAMAGE, null);
		track.preservedHealth = player.getHealth();
		VelorumMoves.sound(velorum, player.position(), SoundEvents.PLAYER_HURT_FREEZE, 1.0F, 0.7F);
		VelorumMoves.sound(velorum, player.position(), SoundEvents.GLASS_PLACE, 1.0F, 0.5F);
		VelorumEffects.preserved(velorum, player);
		SaintLines.teach(player, "message.hemomancy.velorum.hint.preserved");
		velorum.firstPreservation();
	}

	private void tickPreserved(VelorumEntity velorum, Player player, Track track) {
		float health = player.getHealth();
		if (health < track.preservedHealth) track.preservedDamage += track.preservedHealth - health;
		track.preservedHealth = health;
		track.preservedLeft--;
		if (track.preservedLeft <= 0
				|| VelorumStillnessRules.iceBreaks(track.preservedDamage, track.blows, track.warmed)) {
			release(velorum, player, track);
		}
	}

	/** Out of the ice: the meter to 50, the fill suspended 60 ticks, and the 95 cap for 200. */
	public void release(VelorumEntity velorum, Player player) {
		Track track = tracks.get(player.getUUID());
		if (track != null && track.preservedLeft > 0) release(velorum, player, track);
	}

	private void release(VelorumEntity velorum, Player player, Track track) {
		long time = player.level().getGameTime();
		track.preservedLeft = 0;
		endMark(velorum, player, track);
		track.thawUntil = time + VelorumStillnessRules.THAW_TICKS;
		track.capUntil = time + VelorumStillnessRules.CAP_TICKS;
		SaintRoot.release(player, PRESERVED_ROOT);
		SaintMeter meter = velorum.meter();
		meter.add(player, VelorumStillnessRules.RELEASE_VALUE - meter.player(player).value());
		VelorumMoves.sound(velorum, player.position(), SoundEvents.GLASS_BREAK, 1.0F, 0.9F);
		VelorumEffects.freed(velorum, player);
	}

	/** Every challenger out of the ice and every lesson left as it is: the ending and the trial's end. */
	public void releaseAll(VelorumEntity velorum, List<Player> challengers) {
		for (Player player : challengers) release(velorum, player);
	}

	/** The ending drains every meter to 0 between ticks 40 and 80. */
	public void drainForEnding(VelorumEntity velorum, List<Player> challengers, int tick) {
		float left = VelorumEndingRules.meterLeft(tick);
		SaintMeter meter = velorum.meter();
		for (Player player : challengers) {
			SaintMeter.PlayerState state = meter.player(player);
			float target = Math.min(state.value(), VelorumStillnessRules.MAX * left);
			meter.add(player, target - state.value());
		}
	}

	public void clear() {
		tracks.clear();
	}

	public List<Player> preservedAmong(List<Player> challengers) {
		List<Player> preserved = new ArrayList<>();
		for (Player player : challengers) if (preserved(player)) preserved.add(player);
		return preserved;
	}

	private Track track(Player player) {
		return tracks.computeIfAbsent(player.getUUID(), ignored -> new Track());
	}

	private static void recordStill(Track track, BlockPos cell) {
		long key = BlockPos.asLong(cell.getX(), 0, cell.getZ());
		track.stillCells.addLast(key);
		track.stillCounts.merge(key, 1, Integer::sum);
		while (track.stillCells.size() > STILL_HISTORY) {
			Long old = track.stillCells.removeFirst();
			track.stillCounts.computeIfPresent(old, (k, n) -> n <= 1 ? null : n - 1);
		}
		long best = key;
		int count = -1;
		for (Map.Entry<Long, Integer> entry : track.stillCounts.entrySet()) {
			if (entry.getValue() > count) {
				count = entry.getValue();
				best = entry.getKey();
			}
		}
		BlockPos at = BlockPos.of(best);
		track.stillest = new Vec3(at.getX() + 0.5D, cell.getY(), at.getZ() + 0.5D);
		track.stillestCount = count;
	}
}
