package com.vincenthuto.hemomancy.common.manipulation.saint;

import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Saint gifts' live state (D-1 spec 2.8): session only, keyed by player, cleared by
 * {@code TransientServerStateEvents} on logout and server stop and by {@link CanonGiftEvents} on death and dimension
 * change. Nothing here is saved; a gift's marks on its targets die with the gift that laid them.
 */
public final class CanonGiftState {
	/** A Crimson Tithe being held: the vessel and its chains. */
	public static final class TitheHold {
		public double vessel;
		public double leaked;
		public double healed;
		public int heldTicks;
		public int fullTicks;
		/** Where the vessel was opened; a hold carried into another dimension does not empty there. */
		public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
		/** Chained bodies by UUID, in latch order, each with the ticks it has been out of the caster's sight. */
		public final Map<UUID, Integer> chains = new LinkedHashMap<>();
	}

	/** The star of lanes a released Tithe leaves, flooded for a while. */
	public record TitheRelease(Vec3 origin, double start, int lanes, double length, long born) {
	}

	/** An open Unclosing Eye: the beam's direction, swept toward the look at the capped rate. */
	public static final class EyeBeam {
		public final long started;
		public final float charge;
		public double yaw;
		public double pitch;
		public int pulses;
		/** Integrity this beam has given Seraphae herself, capped per cast. */
		public int integrity;
		public final Set<UUID> touched = new HashSet<>();

		public EyeBeam(long started, float charge, double yaw, double pitch) {
			this.started = started;
			this.charge = charge;
			this.yaw = yaw;
			this.pitch = pitch;
		}
	}

	/** After the beam: the Lapse, then the slit. */
	public record EyeAfter(long lapseUntil, long slitUntil) {
	}

	/** Growth a beam laid on one body, and the beam's caster. */
	public static final class Growth {
		public int growth;
		public long lastBeam;
		public long lastDecay;
		public int allyPulses;
		public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
		public final UUID caster;

		public Growth(UUID caster, long now) {
			this.caster = caster;
			this.lastBeam = now;
			this.lastDecay = now;
		}
	}

	/** A sown Bloom of Rot: one per caster, fixed where it was sown. */
	public static final class BloomField {
		public final Vec3 centre;
		public final long sown;
		public final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
		public double kindling;
		/** The game tick of the Ignition, or -1 while the field is unlit. */
		public long ignitedAt = -1L;
		public boolean fullFire;
		/** Whether the buried will has creaked past 75 Kindling. */
		public boolean creaked;
		public long lastCasterGain;
		public final Set<UUID> touched = new HashSet<>();
		public final Set<UUID> burned = new HashSet<>();

		public BloomField(Vec3 centre, long sown, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
			this.centre = centre;
			this.sown = sown;
			this.dimension = dimension;
			this.lastCasterGain = sown;
		}

		public boolean lit() {
			return ignitedAt >= 0L;
		}
	}

	/** Colonisation a Bloom laid on one body, capped at 12 whatever number of fields laid it. */
	public static final class Colony {
		public int value;
		public UUID caster;
		public Vec3 lastPosition;
		public long lastField;
		public long lastDecay;
		public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;

		public Colony(UUID caster, Vec3 position, long now) {
			this.caster = caster;
			this.lastPosition = position;
			this.lastField = now;
			this.lastDecay = now;
		}
	}

	/**
	 * An Endless Hour planted by one caster. Once it ends the record stays while the caster sets in their cruor or
	 * bows, and goes when both have passed.
	 */
	public static final class Hour {
		public final Vec3 point;
		public final long planted;
		public final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
		/** Damage the caster has taken inside this Hour, for the Bow. */
		public double taken;
		public boolean ended;
		public long setUntil = -1L;
		public long bowUntil = -1L;

		public Hour(Vec3 point, long planted, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
			this.point = point;
			this.planted = planted;
			this.dimension = dimension;
		}
	}

	/** Stillness an Hour laid on one body, with the body's last ten positions for its pace. */
	public static final class Stillness {
		public double value;
		public UUID caster;
		public final Vec3[] trail = new Vec3[EndlessHourRules.TRAIL_TICKS + 1];
		public int samples;
		public long lastHour;
		public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;

		public Stillness(UUID caster) {
			this.caster = caster;
		}

		/** Records this tick's position and returns the horizontal pace over the trail. */
		public double pace(Vec3 position) {
			System.arraycopy(trail, 0, trail, 1, trail.length - 1);
			trail[0] = position;
			samples = Math.min(trail.length, samples + 1);
			if (samples < 2) return 0.0D;
			Vec3 oldest = trail[samples - 1];
			double dx = position.x - oldest.x, dz = position.z - oldest.z;
			return EndlessHourRules.pace(Math.sqrt(dx * dx + dz * dz), samples - 1);
		}
	}

	private static final Map<UUID, TitheHold> TITHES = new ConcurrentHashMap<>();
	private static final Map<UUID, Hour> HOURS = new ConcurrentHashMap<>();
	private static final Map<UUID, Stillness> STILLNESS = new ConcurrentHashMap<>();
	private static final Map<UUID, TitheRelease> RELEASES = new ConcurrentHashMap<>();
	private static final Map<UUID, EyeBeam> BEAMS = new ConcurrentHashMap<>();
	private static final Map<UUID, EyeAfter> AFTER = new ConcurrentHashMap<>();
	private static final Map<UUID, Growth> GROWTH = new ConcurrentHashMap<>();
	private static final Map<UUID, BloomField> FIELDS = new ConcurrentHashMap<>();
	private static final Map<UUID, Colony> COLONIES = new ConcurrentHashMap<>();

	private CanonGiftState() {
	}

	public static TitheHold tithe(UUID player) {
		return TITHES.get(player);
	}

	public static TitheHold openTithe(UUID player) {
		return TITHES.computeIfAbsent(player, ignored -> new TitheHold());
	}

	public static TitheHold closeTithe(UUID player) {
		return TITHES.remove(player);
	}

	public static void release(UUID player, TitheRelease release) {
		RELEASES.put(player, release);
	}

	public static Map<UUID, TitheRelease> releases() {
		return RELEASES;
	}

	public static EyeBeam beam(UUID player) {
		return BEAMS.get(player);
	}

	public static void openBeam(UUID player, EyeBeam beam) {
		BEAMS.put(player, beam);
	}

	public static EyeBeam closeBeam(UUID player) {
		return BEAMS.remove(player);
	}

	public static Map<UUID, EyeBeam> beams() {
		return BEAMS;
	}

	public static EyeAfter after(UUID player) {
		return AFTER.get(player);
	}

	public static void after(UUID player, EyeAfter after) {
		AFTER.put(player, after);
	}

	public static Map<UUID, EyeAfter> afters() {
		return AFTER;
	}

	public static Map<UUID, Growth> growth() {
		return GROWTH;
	}

	public static BloomField bloom(UUID player) {
		return FIELDS.get(player);
	}

	public static void sow(UUID player, BloomField field) {
		FIELDS.put(player, field);
	}

	public static BloomField closeBloom(UUID player) {
		return FIELDS.remove(player);
	}

	public static Map<UUID, BloomField> fields() {
		return FIELDS;
	}

	public static Map<UUID, Colony> colonies() {
		return COLONIES;
	}

	/** The caster's Hour record, live or ending. */
	public static Hour hourRecord(UUID player) {
		return HOURS.get(player);
	}

	/** The caster's live Hour, or null once it has ended. */
	public static Hour hour(UUID player) {
		Hour hour = HOURS.get(player);
		return hour == null || hour.ended ? null : hour;
	}

	public static void plant(UUID player, Hour hour) {
		HOURS.put(player, hour);
	}

	public static Map<UUID, Hour> hours() {
		return HOURS;
	}

	public static Map<UUID, Stillness> stillness() {
		return STILLNESS;
	}

	/** Called by {@code TransientServerStateEvents} when a server starts and stops. */
	public static void clearSessionState() {
		TITHES.clear();
		RELEASES.clear();
		BEAMS.clear();
		AFTER.clear();
		GROWTH.clear();
		FIELDS.clear();
		COLONIES.clear();
		HOURS.clear();
		STILLNESS.clear();
	}

	/** Ends one player's gifts: on logout, death and dimension change. Marks they laid on others die with them. */
	public static void end(UUID player) {
		TITHES.remove(player);
		RELEASES.remove(player);
		BEAMS.remove(player);
		AFTER.remove(player);
		GROWTH.remove(player);
		GROWTH.values().removeIf(growth -> growth.caster.equals(player));
		FIELDS.remove(player);
		COLONIES.remove(player);
		COLONIES.values().removeIf(colony -> colony.caster.equals(player));
		HOURS.remove(player);
		STILLNESS.remove(player);
		STILLNESS.values().removeIf(still -> still.caster.equals(player));
	}
}
