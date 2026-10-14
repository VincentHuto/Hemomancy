package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * With the rite panel off, the rite's owner hears from it in the action bar instead: one line when a phase
 * begins, then each new problem in turn. A line keeps the action bar for {@link #QUIET_TICKS} before the next.
 */
public final class RiteGuidance {
	/** Long enough for the vanilla action bar to show a line and fade it. */
	static final int QUIET_TICKS = 70;
	private static final Map<BlockPos, Seen> SEEN = new HashMap<>();
	private static long ticks;

	private RiteGuidance() {
	}

	record Line(String key, List<String> args) {
	}

	/** What the owner has already been told about one rite. Problems are remembered by identity. */
	record Seen(String phase, String cue, List<String> announced, long quietUntil, int rings) {
		static final Seen NONE = new Seen("", "", List.of(), Long.MIN_VALUE, -1);

		/** Rings unseen (-1). */
		Seen(String phase, String cue, List<String> announced, long quietUntil) {
			this(phase, cue, announced, quietUntil, -1);
		}
	}

	record Decision(Seen seen, Line line) {
	}

	public static void tick(Minecraft minecraft) {
		ticks++;
		if (minecraft.player == null || minecraft.level == null
				|| HemoClientConfig.cardinalRiteHud() == HemoClientConfig.CardinalRiteHudMode.FULL) {
			SEEN.clear();
			return;
		}
		UUID player = minecraft.player.getUUID();
		List<BlockPos> live = new ArrayList<>();
		for (ActiveRiteClientData.RiteEntry entry : ActiveRiteClientData.getActiveRites()) {
			if (!listensTo(entry, player)) continue;
			live.add(entry.getCenter());
			Decision decision = decide(SEEN.getOrDefault(entry.getCenter(), Seen.NONE), entry.getPhase(),
					entry.getCue(), entry.isSuccession(), entry.getProblems(), ticks, entry.getOpenRings());
			SEEN.put(entry.getCenter(), decision.seen());
			if (decision.line() != null) {
				minecraft.player.displayClientMessage(Component.translatable(decision.line().key(),
						decision.line().args().toArray()).withStyle(ChatFormatting.DARK_RED), true);
			}
		}
		SEEN.keySet().retainAll(live);
	}

	public static void clear() {
		SEEN.clear();
	}

	static boolean listensTo(ActiveRiteClientData.RiteEntry entry, UUID player) {
		return player != null && player.equals(entry.getOwner()) && !entry.isUnstained() && !entry.isLegacy();
	}

	/** The guide line for a phase, or null when the phase speaks for itself. */
	static String phaseKey(String phase) {
		return switch (phase == null ? "" : phase) {
			case "CONSECRATION", "INSCRIPTION", "STATION_PROJECTION", "PUPPET_TRIAL", "STILL_INTERVAL",
					"OFFERING_PROCESSION", "CULMINATION" -> "hemomancy.rite.guide." + phase.toLowerCase(Locale.ROOT);
			default -> null;
		};
	}

	/** Each sleeping sigil is its own problem; any other problem is the same however its numbers move. */
	private static String identity(RiteProblem problem) {
		return CardinalRiteProblems.SIGIL_REQUIRED.equals(problem.key())
				? problem.key() + problem.args() : problem.key();
	}

	/** "The Nth ring wakes", named for the deepest ring awake; ordinals stop at the eighth. */
	static String ringKey(int openRings) {
		return "hemomancy.rite.guide.ring_woke." + Math.clamp(openRings, 2, 8);
	}

	static Decision decide(Seen seen, String phase, String cue, boolean succession, List<RiteProblem> problems,
			long now) {
		return decide(seen, phase, cue, succession, problems, now, -1);
	}

	/** As above, also announcing each ring that wakes after the rite was first seen; -1 open rings is unknown. */
	static Decision decide(Seen seen, String phase, String cue, boolean succession, List<RiteProblem> problems,
			long now, int openRings) {
		List<String> current = problems.stream().map(RiteGuidance::identity).toList();
		// Forget problems that were resolved, so they are announced again if they return.
		List<String> announced = seen.announced().stream().filter(current::contains).toList();
		Seen kept = new Seen(seen.phase(), seen.cue(), announced, seen.quietUntil(), seen.rings());
		// Rings already awake when the rite is first seen are learned, not announced.
		if (openRings >= 0 && (kept.rings() < 0 || openRings < kept.rings())) {
			kept = new Seen(kept.phase(), kept.cue(), announced, kept.quietUntil(), openRings);
		}
		if (now < seen.quietUntil()) return new Decision(kept, null);
		String safePhase = phase == null ? "" : phase;
		if (!safePhase.equals(kept.phase())) {
			String key = phaseKey(safePhase);
			kept = new Seen(safePhase, kept.cue(), announced, key == null ? kept.quietUntil() : now + QUIET_TICKS,
					kept.rings());
			if (key != null) return new Decision(kept, new Line(key, List.of()));
		}
		if (openRings > kept.rings() && kept.rings() >= 0) {
			return new Decision(new Seen(kept.phase(), kept.cue(), announced, now + QUIET_TICKS, openRings),
					new Line(ringKey(openRings), List.of()));
		}
		String safeCue = cue == null ? "" : cue;
		if (succession && !safeCue.isBlank() && !safeCue.equals(kept.cue())) {
			return new Decision(new Seen(kept.phase(), safeCue, announced, now + QUIET_TICKS, kept.rings()),
					new Line(safeCue, List.of()));
		}
		for (int i = 0; i < problems.size(); i++) {
			if (announced.contains(current.get(i))) continue;
			List<String> next = new ArrayList<>(announced);
			next.add(current.get(i));
			return new Decision(new Seen(kept.phase(), kept.cue(), List.copyOf(next), now + QUIET_TICKS, kept.rings()),
					new Line(problems.get(i).key(), problems.get(i).args()));
		}
		return new Decision(kept, null);
	}
}
