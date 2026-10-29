package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

/**
 * The tuning of the mausoleum ambience, kept free of the game so it can be tested. Depth runs from 0 at a complex's
 * highest hallowed floor to 1 at its lowest. In Hemorath's (the Order's reliquary down to the pit where he was bled dry)
 * the heart grows louder, quicker and lower with depth, and drips come more often and land harder. In Seraphae's (the
 * last ward her people built down to the infirmary where she first could not stop) a faint choir swells a little, and
 * her containment anchors tick more often. Putriciel's halls alternate burned bands and rotten ones, and the bands are
 * not stored anywhere, so his ambience goes by the band blocks found near the listener: fire crackles where the burned
 * ones are the most, wet wood creaks where the rotten ones are, and every half minute or so a pyre flares somewhere far
 * off, nearer-sounding in the burned bands. Velorum's is near silence: ice creaks somewhere in the walls once in a long
 * while, and the listener's own footsteps come back muffled, quieter and lower, while they stand in his hallowed halls.
 */
public final class MausoleumAmbienceRules {
	/** Heartbeat volume at the entrance and in the chamber. */
	public static final float ENTRANCE_VOLUME = 0.05F;
	public static final float CHAMBER_VOLUME = 0.4F;
	/** Ticks between beats at the entrance and in the chamber. */
	public static final int ENTRANCE_INTERVAL = 70;
	public static final int CHAMBER_INTERVAL = 45;
	/** Pitch on top of the sound file's 0.7: the beat drops about an eighth deeper by the chamber. */
	public static final float ENTRANCE_PITCH = 1.0F;
	public static final float CHAMBER_PITCH = 0.88F;
	/** Mean ticks between drips at the entrance and in the chamber. */
	public static final int ENTRANCE_DRIP_TICKS = 220;
	public static final int CHAMBER_DRIP_TICKS = 50;
	public static final float ENTRANCE_DRIP_VOLUME = 0.18F;
	public static final float CHAMBER_DRIP_VOLUME = 0.45F;
	/** How much of the gap to its target the heard depth closes each tick: about five seconds to settle. */
	public static final float DEPTH_RATE = 0.02F;
	/** How far presence moves each tick: two seconds to fade fully in or out. */
	public static final float PRESENCE_STEP = 1.0F / 40.0F;
	/** Below this the ambience is treated as silent. */
	public static final float SILENT = 0.005F;

	/** Seraphae's choir: barely there at the entrance, still faint in the infirmary. */
	public static final float CHOIR_ENTRANCE_VOLUME = 0.06F;
	public static final float CHOIR_CHAMBER_VOLUME = 0.2F;
	/** Pitch on top of the sound file's 0.6: the choir settles a little lower by the infirmary. */
	public static final float CHOIR_ENTRANCE_PITCH = 1.0F;
	public static final float CHOIR_CHAMBER_PITCH = 0.92F;
	/** Ticks between looks for anchors around the listener, and how far each look reaches across and up or down. */
	public static final int ANCHOR_SCAN_INTERVAL = 40;
	public static final int ANCHOR_SCAN_RADIUS = 8;
	public static final int ANCHOR_SCAN_HEIGHT = 3;
	/** Mean ticks between anchor ticks at the entrance and in the infirmary, and how loud each one lands. */
	public static final int ENTRANCE_ANCHOR_TICKS = 70;
	public static final int CHAMBER_ANCHOR_TICKS = 30;
	public static final float ENTRANCE_ANCHOR_VOLUME = 0.25F;
	public static final float CHAMBER_ANCHOR_VOLUME = 0.4F;
	/** How far either side of its sound file's pitch one anchor tick may land. */
	public static final float ANCHOR_PITCH_SPREAD = 0.08F;

	/** Ticks between looks for band blocks around the listener, and how far each look reaches across and up or down. */
	public static final int BAND_SCAN_INTERVAL = 40;
	public static final int BAND_SCAN_RADIUS = 8;
	public static final int BAND_SCAN_HEIGHT = 3;
	/** Band blocks in one look at which their band is heard at its fullest; with fewer it thins out. */
	public static final int BAND_FULL_COUNT = 24;
	/** How much of its sound the band with fewer blocks nearby still makes: a few roots in a burned band creak rarely. */
	public static final float MINORITY_SHARE = 0.2F;
	/** Mean ticks between crackles in a wholly burned band, and between creaks in a wholly rotten one. */
	public static final int CRACKLE_TICKS = 14;
	public static final int CREAK_TICKS = 45;
	/** How loud a crackle and a creak land from a band block at the listener's feet. */
	public static final float CRACKLE_VOLUME = 0.35F;
	public static final float CREAK_VOLUME = 0.3F;
	/** The share of that volume left for a band block at the edge of a look. */
	public static final float FAR_VOLUME_SHARE = 0.35F;
	/** How far either side of its sound file's pitch one crackle or one creak may land. */
	public static final float CRACKLE_PITCH_SPREAD = 0.15F;
	public static final float CREAK_PITCH_SPREAD = 0.1F;
	/** Ticks between distant flares, give or take the jitter, and the wait after coming in before the first. */
	public static final int FLARE_INTERVAL = 600;
	public static final int FLARE_JITTER = 200;
	public static final int FLARE_FIRST_DELAY = 160;
	/** A flare's volume heard from a wholly rotten band and from a wholly burned one: always far off, never loud. */
	public static final float FLARE_ROTTEN_VOLUME = 0.1F;
	public static final float FLARE_BURNED_VOLUME = 0.25F;
	/** How far off (blocks, across) a flare is placed, so it pans from somewhere down the halls. */
	public static final float FLARE_NEAREST = 16.0F;
	public static final float FLARE_FARTHEST = 28.0F;
	/** How far either side of its sound file's pitch one flare may land. */
	public static final float FLARE_PITCH_SPREAD = 0.08F;

	/** Ticks between Velorum's ice creaks, give or take the jitter, and the wait after coming in before the first. */
	public static final int ICE_CREAK_INTERVAL = 900;
	public static final int ICE_CREAK_JITTER = 300;
	public static final int ICE_CREAK_FIRST_DELAY = 240;
	/** An ice creak's volume at the entrance and in the chamber: quiet throughout, a little plainer by his cell. */
	public static final float ICE_CREAK_ENTRANCE_VOLUME = 0.18F;
	public static final float ICE_CREAK_CHAMBER_VOLUME = 0.28F;
	/** How far off (blocks, across) the ice creaks, and how far either side of its sound file's pitch. */
	public static final float ICE_CREAK_NEAREST = 4.0F;
	public static final float ICE_CREAK_FARTHEST = 10.0F;
	public static final float ICE_CREAK_PITCH_SPREAD = 0.08F;
	/** The listener's own footsteps in Velorum's halls: this share of their volume, at this share of their pitch. */
	public static final float MUFFLED_STEP_VOLUME = 0.35F;
	public static final float MUFFLED_STEP_PITCH = 0.8F;
	/** How far (blocks) from the listener's feet a step sound may start and still be their own. */
	public static final double OWN_STEP_REACH = 1.0D;

	private MausoleumAmbienceRules() {
	}

	/**
	 * How deep a listener standing at {@code feetY} is, between the highest hallowed floor and the lowest. A complex
	 * whose floors are all level is all entrance.
	 */
	public static float depth(double feetY, int highestFloor, int lowestFloor) {
		if (highestFloor <= lowestFloor) return 0.0F;
		return (float) HemoEasing.clamp01((highestFloor - feetY) / (highestFloor - lowestFloor));
	}

	public static float heartVolume(float depth) {
		return HemoEasing.lerp(ENTRANCE_VOLUME, CHAMBER_VOLUME, HemoEasing.smoothstep(depth));
	}

	public static int beatInterval(float depth) {
		return Math.round(HemoEasing.lerp(ENTRANCE_INTERVAL, CHAMBER_INTERVAL, HemoEasing.clamp01(depth)));
	}

	public static float heartPitch(float depth) {
		return HemoEasing.lerp(ENTRANCE_PITCH, CHAMBER_PITCH, HemoEasing.clamp01(depth));
	}

	/** The chance of a drip on any one tick. */
	public static float dripChance(float depth) {
		return 1.0F / HemoEasing.lerp(ENTRANCE_DRIP_TICKS, CHAMBER_DRIP_TICKS, HemoEasing.clamp01(depth));
	}

	public static float dripVolume(float depth) {
		return HemoEasing.lerp(ENTRANCE_DRIP_VOLUME, CHAMBER_DRIP_VOLUME, HemoEasing.clamp01(depth));
	}

	/** Whether a Saint's mausoleum has an ambience of its own yet; the others stay quiet. */
	public static boolean voiced(EnumSaintType saint) {
		return saint == EnumSaintType.HEMORATH || saint == EnumSaintType.SERAPHAE || saint == EnumSaintType.PUTRICIEL
				|| saint == EnumSaintType.VELORUM;
	}

	public static float choirVolume(float depth) {
		return HemoEasing.lerp(CHOIR_ENTRANCE_VOLUME, CHOIR_CHAMBER_VOLUME, HemoEasing.smoothstep(depth));
	}

	public static float choirPitch(float depth) {
		return HemoEasing.lerp(CHOIR_ENTRANCE_PITCH, CHOIR_CHAMBER_PITCH, HemoEasing.clamp01(depth));
	}

	/** The chance on any one tick that one of the anchors near the listener ticks. */
	public static float anchorTickChance(float depth) {
		return 1.0F / HemoEasing.lerp(ENTRANCE_ANCHOR_TICKS, CHAMBER_ANCHOR_TICKS, HemoEasing.clamp01(depth));
	}

	public static float anchorTickVolume(float depth) {
		return HemoEasing.lerp(ENTRANCE_ANCHOR_VOLUME, CHAMBER_ANCHOR_VOLUME, HemoEasing.clamp01(depth));
	}

	/** One tick's pitch from a roll in [0, 1): spread evenly about 1 so no two ticks sound quite alike. */
	public static float anchorTickPitch(float roll) {
		return spread(roll, ANCHOR_PITCH_SPREAD);
	}

	/** How fully a band is heard from the number of its blocks in one look: nothing with none, whole at the full count. */
	public static float bandStrength(int blocks) {
		return HemoEasing.clamp01(blocks / (float) BAND_FULL_COUNT);
	}

	/** The burned band's share of the band blocks nearby; even when neither is near. */
	public static float burnedShare(int burned, int rotten) {
		int total = Math.max(0, burned) + Math.max(0, rotten);
		return total == 0 ? 0.5F : Math.max(0, burned) / (float) total;
	}

	/** How much of its sound a band makes from its share of the band blocks nearby: all of it where it is alone. */
	public static float bandMix(float share) {
		return HemoEasing.lerp(MINORITY_SHARE, 1.0F, HemoEasing.clamp01(share));
	}

	/** The chance on any one tick of a crackle from the burned blocks near the listener. */
	public static float crackleChance(int burned, int rotten) {
		return bandStrength(burned) * bandMix(burnedShare(burned, rotten)) / CRACKLE_TICKS;
	}

	/** The chance on any one tick of a wet creak from the rotten blocks near the listener. */
	public static float creakChance(int burned, int rotten) {
		return bandStrength(rotten) * bandMix(1.0F - burnedShare(burned, rotten)) / CREAK_TICKS;
	}

	/** The share of a band sound's volume heard from a block {@code distance} away: falling to the far share by the edge. */
	public static float nearness(double distance) {
		return HemoEasing.lerp(1.0F, FAR_VOLUME_SHARE, (float) HemoEasing.clamp01(distance / BAND_SCAN_RADIUS));
	}

	public static float crackleVolume(double distance) {
		return CRACKLE_VOLUME * nearness(distance);
	}

	public static float creakVolume(double distance) {
		return CREAK_VOLUME * nearness(distance);
	}

	public static float cracklePitch(float roll) {
		return spread(roll, CRACKLE_PITCH_SPREAD);
	}

	public static float creakPitch(float roll) {
		return spread(roll, CREAK_PITCH_SPREAD);
	}

	/** Ticks to the next flare from a roll in [0, 1): the interval, give or take the jitter. */
	public static int flareInterval(float roll) {
		return Math.round(FLARE_INTERVAL + (HemoEasing.clamp01(roll) * 2.0F - 1.0F) * FLARE_JITTER);
	}

	/** A flare's volume: faint from the rotten bands, a little plainer from the burned ones. */
	public static float flareVolume(int burned, int rotten) {
		return HemoEasing.lerp(FLARE_ROTTEN_VOLUME, FLARE_BURNED_VOLUME, burnedShare(burned, rotten));
	}

	/** How far off a flare is placed, from a roll in [0, 1). */
	public static float flareDistance(float roll) {
		return HemoEasing.lerp(FLARE_NEAREST, FLARE_FARTHEST, HemoEasing.clamp01(roll));
	}

	public static float flarePitch(float roll) {
		return spread(roll, FLARE_PITCH_SPREAD);
	}

	/** Ticks to the next ice creak from a roll in [0, 1): the interval, give or take the jitter. */
	public static int iceCreakInterval(float roll) {
		return Math.round(ICE_CREAK_INTERVAL + (HemoEasing.clamp01(roll) * 2.0F - 1.0F) * ICE_CREAK_JITTER);
	}

	public static float iceCreakVolume(float depth) {
		return HemoEasing.lerp(ICE_CREAK_ENTRANCE_VOLUME, ICE_CREAK_CHAMBER_VOLUME, HemoEasing.clamp01(depth));
	}

	/** How far off an ice creak is placed, from a roll in [0, 1). */
	public static float iceCreakDistance(float roll) {
		return HemoEasing.lerp(ICE_CREAK_NEAREST, ICE_CREAK_FARTHEST, HemoEasing.clamp01(roll));
	}

	public static float iceCreakPitch(float roll) {
		return spread(roll, ICE_CREAK_PITCH_SPREAD);
	}

	/**
	 * Whether a sound is the listener's own footstep: a block's step sound from the players' source, starting at their
	 * feet. Other players' steps and mobs' steps are left alone.
	 */
	public static boolean ownFootstep(String soundPath, boolean playersSource, double distanceSqrToFeet) {
		return playersSource && soundPath.endsWith(".step") && distanceSqrToFeet <= OWN_STEP_REACH * OWN_STEP_REACH;
	}

	/** Whether footsteps are muffled at {@code feet}: only inside the hallowed pieces of a complex of Velorum's. */
	public static boolean mufflesSteps(EnumSaintType saint, List<BoundingBox> hallowed, BlockPos feet) {
		if (saint != EnumSaintType.VELORUM) return false;
		for (BoundingBox box : hallowed) {
			if (box.isInside(feet)) return true;
		}
		return false;
	}

	/** A pitch from a roll in [0, 1), spread evenly about 1 by {@code spread} either side. */
	private static float spread(float roll, float spread) {
		return 1.0F + (HemoEasing.clamp01(roll) * 2.0F - 1.0F) * spread;
	}

	/** The heard depth one tick on: it closes a share of the gap, and snaps once the gap is too small to hear. */
	public static float easeDepth(float heard, float target) {
		float next = HemoEasing.lerp(heard, target, DEPTH_RATE);
		return Math.abs(target - next) < 0.001F ? target : next;
	}

	/** Presence one tick on: rising toward 1 inside the complex, falling toward 0 outside it. */
	public static float easePresence(float presence, boolean inside) {
		return inside ? Math.min(1.0F, presence + PRESENCE_STEP) : Math.max(0.0F, presence - PRESENCE_STEP);
	}
}
