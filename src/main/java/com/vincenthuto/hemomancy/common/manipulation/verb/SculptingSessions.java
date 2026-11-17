package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastingAnimationManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Open Sculpting sessions, one per player: which Sculpting is working on which target since when. Session state keyed by
 * player UUID; cleared through {@code TransientServerStateEvents}. A session ends when the use stops, the target or
 * selection changes, the claim lapses, or the dispatcher stops seeing it. The cast presentation belongs to the channel:
 * it survives a change of target while the same Sculpting keeps working, and stops while a gate refuses the pulses.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SculptingSessions {
	private static final Map<UUID, Session> SESSIONS = new HashMap<>();
	/**
	 * The tick of each player's last paid pulse, kept across sessions, so switching targets or pressing the verb again
	 * never pulses sooner than the Sculpting's cadence.
	 */
	private static final Map<UUID, Long> LAST_PULSE = new HashMap<>();

	private SculptingSessions() {
	}

	/** A running session. {@code bankedTicks} is paid work toward the next credited use. */
	public static final class Session {
		final String manipulation;
		final Verb verb;
		final String targetKey;
		final VerbSculpting sculpting;
		final long started;
		long lastSeen;
		int pulses;
		int bankedTicks;
		/** The Sculpting's {@code begin} has run for this target. */
		boolean opened;
		/**
		 * The cast presentation is up. It belongs to the channel, not the target, so a session that replaces one on a
		 * new target while the player keeps channelling the same Sculpting takes it over instead of restarting it.
		 */
		boolean presenting;

		Session(String manipulation, Verb verb, String targetKey, VerbSculpting sculpting, long started) {
			this.manipulation = manipulation;
			this.verb = verb;
			this.targetKey = targetKey;
			this.sculpting = sculpting;
			this.started = started;
			this.lastSeen = started;
		}

		public String manipulation() {
			return manipulation;
		}

		public VerbSculpting sculpting() {
			return sculpting;
		}

		public Verb verb() {
			return verb;
		}

		public String targetKey() {
			return targetKey;
		}

		public long started() {
			return started;
		}

		public int pulses() {
			return pulses;
		}

		public boolean opened() {
			return opened;
		}

		boolean continues(String manipulation, Verb verb, String targetKey, long now) {
			return sameChannel(manipulation, verb, now) && this.targetKey.equals(targetKey);
		}

		boolean sameChannel(String manipulation, Verb verb, long now) {
			return this.manipulation.equals(manipulation) && this.verb == verb && now - lastSeen <= 1;
		}
	}

	/**
	 * Whether the player's Sculpting is working: a pulse has been paid, the last one was not refused, and the channel
	 * has not stopped. The cast presentation stays up exactly this long.
	 */
	public static boolean isActive(UUID player) {
		Session session = SESSIONS.get(player);
		return session != null && session.presenting;
	}

	public static Session current(Player player) {
		return SESSIONS.get(player.getUUID());
	}

	/**
	 * The session for this tick's claim: the running one when it continues, otherwise the old one ends and a new one
	 * starts. A new session opens (see {@link #opened}) only once a pulse has been paid, so a refused claim neither
	 * begins the Sculpting nor shows a cast.
	 */
	static Session track(ServerPlayer player, SculptingDispatcher.Selected selected, Verb verb, VerbTarget target) {
		long now = player.level().getGameTime();
		String name = selected.manipulation().getName();
		Session running = SESSIONS.get(player.getUUID());
		if (running != null && running.sculpting == selected.sculpting() && running.continues(name, verb, target.key(), now)) {
			running.lastSeen = now;
			return running;
		}
		boolean keepPresentation = false;
		int keepBanked = 0;
		if (running != null && running.sameChannel(name, verb, now)) {
			// Only the target (or which of the manipulation's Sculptings claims it) changed: end the Sculpting's work on
			// the old one, keep the cast presentation running and the paid work toward the next credited use (a haul or
			// a sweep changes target nearly every tick). A Sculpting that draws itself instead (a strand) stops it.
			SESSIONS.remove(player.getUUID());
			if (running.opened) running.sculpting.end(player, false);
			keepPresentation = running.presenting && selected.sculpting().presentsCast();
			if (running.presenting && !keepPresentation) CastingAnimationManager.channelStop(player, false);
			keepBanked = running.bankedTicks;
		} else if (running != null) {
			end(player, false);
		}
		Session session = new Session(name, verb, target.key(), selected.sculpting(), now);
		session.presenting = keepPresentation;
		session.bankedTicks = keepBanked;
		SESSIONS.put(player.getUUID(), session);
		return session;
	}

	/**
	 * The running session of a held channel ({@link VerbSculpting#holdsChannel}) when it continues this tick: begun,
	 * the same manipulation, Sculpting and verb, and fed last tick. It keeps its first target's key whatever the aim does now.
	 * Null when there is none, and the dispatcher claims afresh.
	 */
	static Session held(ServerPlayer player, SculptingDispatcher.Selected selected, Verb verb, long now) {
		Session running = SESSIONS.get(player.getUUID());
		if (running == null || !running.opened || running.sculpting != selected.sculpting()
				|| !running.sameChannel(selected.manipulation().getName(), verb, now)) {
			return null;
		}
		running.lastSeen = now;
		return running;
	}

	/** The tick of the player's last paid pulse, or {@link Long#MIN_VALUE} before any. */
	static long lastPulse(Player player) {
		return LAST_PULSE.getOrDefault(player.getUUID(), Long.MIN_VALUE);
	}

	static void recordPulse(Player player, long now) {
		LAST_PULSE.put(player.getUUID(), now);
	}

	/**
	 * Once a pulse is paid, before the pulse itself runs: the Sculpting's {@code begin} on a new target, and the cast's
	 * sustained presentation for onlookers when it is not already up.
	 */
	static void opened(ServerPlayer player, Session session, SculptingDispatcher.Selected selected, VerbTarget target) {
		if (!session.opened) {
			session.opened = true;
			selected.sculpting().begin(player, target);
		}
		if (!session.presenting && selected.sculpting().presentsCast()) {
			session.presenting = true;
			CastingAnimationManager.channelStart(player, selected.manipulation());
		}
	}

	/** A gate refused the pulse: nothing is being worked, so the cast presentation stops until a pulse is paid. */
	static void refused(ServerPlayer player, Session session) {
		if (!session.presenting) return;
		session.presenting = false;
		CastingAnimationManager.channelStop(player, false);
	}

	/** Ends the player's session, if any. {@code released} when the player let go of the verb. */
	public static void end(ServerPlayer player, boolean released) {
		Session session = SESSIONS.remove(player.getUUID());
		if (session == null) return;
		if (session.opened) session.sculpting.end(player, released);
		if (session.presenting) CastingAnimationManager.channelStop(player, released);
	}

	/** Ends a session the verb chain no longer feeds: the use stopped or another claim took the channel. */
	@SubscribeEvent
	public static void tick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) return;
		if (!player.isAlive() || !player.isUsingItem() || player.level().getGameTime() - session.lastSeen > 1) {
			end(player, false);
		}
	}

	public static void clearSessionState() {
		SESSIONS.clear();
		LAST_PULSE.clear();
	}

	public static void logout(Player player) {
		SESSIONS.remove(player.getUUID());
		LAST_PULSE.remove(player.getUUID());
	}
}
