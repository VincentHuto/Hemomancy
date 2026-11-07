package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLines;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Seraphae's voice and lessons (design section 14). She speaks like a nurse: short, warm, present tense, never naming
 * her keepers, never cruel; the only question is where it hurts. Lessons show once per fight per player on the action
 * bar through {@link SaintLines}.
 */
public final class SeraphaeLines {
	private static final String VOICE = "message.hemomancy.seraphae.voice.";
	private static final String TEACH = "message.hemomancy.seraphae.teach.";

	public static final String RISE = VOICE + "rise";
	public static final String[] REACH = {VOICE + "reach_wound", VOICE + "reach_pull", VOICE + "reach_hurt"};
	public static final String HOLD = VOICE + "hold";
	public static final String HELD_BURST = VOICE + "held_burst";
	public static final String RECOIL = VOICE + "recoil";
	public static final String RESTRAINT = VOICE + "restraint";
	public static final String ESCAPING = VOICE + "escaping";
	public static final String FLOOD = VOICE + "flood";
	public static final String ENDING_FIRST = VOICE + "ending_open";
	public static final String ENDING_SECOND = VOICE + "ending_help";

	public static final String TEACH_RECOIL = TEACH + "recoil";
	public static final String TEACH_GROWTH = "message.hemomancy.seraphae.growth_warning";
	public static final String TEACH_CLOSES = TEACH + "closes";
	public static final String TEACH_SLIP = TEACH + "slip";
	public static final String TEACH_FRACTURE = TEACH + "fracture";
	public static final String TEACH_CONDENSING = TEACH + "condensing";
	public static final String TEACH_HOLD = TEACH + "hold";
	public static final String TEACH_LAPSE = TEACH + "lapse";
	public static final String EXCESS_GROWTH = "message.hemomancy.seraphae.excess_growth";
	public static final String REFUSED_DRAIN = TEACH + "refused_drain";
	public static final String NO_BLOOD = TEACH + "no_blood";

	private SeraphaeLines() {
	}

	public static void voice(SaintTrialEntity seraphae, String key) {
		SaintLines.voice(seraphae, key);
	}

	public static void teach(Player player, String key) {
		SaintLines.teach(player, key);
	}

	/** Every challenger learns at once (a slip, a Fracture, the Condensing). */
	public static void teachAll(SaintTrialEntity seraphae, String key) {
		if (seraphae.arena() == null) return;
		for (Player player : seraphae.arena().challengers()) SaintLines.teach(player, key);
	}

	/** One of her three reach lines, chosen by the move's serial so it varies without a random draw. */
	public static String reach(int serial) {
		return REACH[Math.floorMod(serial, REACH.length)];
	}
}
