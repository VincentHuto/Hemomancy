package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

/**
 * The Crowned Refusal's three wounds. Each health threshold splits one open; the player drains it with Blood
 * Absorption while the mount fights to shake them off. Hits only keep it from scabbing (plus a small fallback drain so
 * no build can soft-lock), killing one of Vesper's puppets tears it, and a neglected wound scabs back toward, but
 * never below, half of the progress already made.
 */
public final class VesperWoundRules {
	public static final float DRAIN_REQUIRED = 100.0F;
	public static final int SCAB_DELAY = 80;
	public static final float SCAB_RATE = 0.4F;
	public static final float SCAB_FLOOR_FRACTION = 0.5F;
	public static final float PUPPET_TEAR = 25.0F;
	public static final float HIT_FALLBACK_RATIO = 0.10F;
	/** Old saves stored a 0..40 anchor damage meter. */
	private static final float LEGACY_ANCHOR_SCALE = DRAIN_REQUIRED / VesperCombatRules.ANCHOR_MAX_DAMAGE;

	public enum Site {
		CARAPACE, NECK, TAIL;

		public static Site fromIndex(int index) {
			Site[] sites = values();
			return sites[Mth.clamp(index, 0, sites.length - 1)];
		}
	}

	/** drained: progress toward rupture; idleTicks: since anyone last drained or struck it; peak: best progress. */
	public record Wound(Site site, float drained, int idleTicks, float peak) {
	}

	private VesperWoundRules() {
	}

	public static Wound open(Site site) {
		return new Wound(site, 0.0F, 0, 0.0F);
	}

	public static Wound drain(Wound wound, float amount) {
		return advance(wound, Math.max(0.0F, amount));
	}

	public static Wound hit(Wound wound, float damage) {
		return advance(wound, Math.max(0.0F, damage) * HIT_FALLBACK_RATIO);
	}

	public static Wound tear(Wound wound) {
		return advance(wound, PUPPET_TEAR);
	}

	public static Wound tick(Wound wound) {
		int idle = wound.idleTicks() + 1;
		float drained = wound.drained();
		if (idle > SCAB_DELAY) drained = Math.max(scabFloor(wound.peak()), drained - SCAB_RATE);
		return new Wound(wound.site(), Math.min(drained, wound.drained()), idle, wound.peak());
	}

	public static boolean broken(Wound wound) {
		return wound.drained() >= DRAIN_REQUIRED;
	}

	public static float scabFloor(float peak) {
		return peak * SCAB_FLOOR_FRACTION;
	}

	public static float progress(Wound wound) {
		return Mth.clamp(wound.drained() / DRAIN_REQUIRED, 0.0F, 1.0F);
	}

	private static Wound advance(Wound wound, float amount) {
		float drained = Math.min(DRAIN_REQUIRED, wound.drained() + amount);
		return new Wound(wound.site(), drained, 0, Math.max(wound.peak(), drained));
	}

	/**
	 * Where each wound sits on the mount, in blocks from the entity origin: forward along its facing and up. Matches
	 * the wound sockets in tools/model_export/vesper_mount.py (model units / 16, with the model's -z as forward).
	 */
	public record Socket(double forward, double up) {
	}

	public static Socket socket(Site site) {
		return switch (site) {
			case CARAPACE -> new Socket(-0.375D, 2.97D);
			case NECK -> new Socket(2.75D, 1.9D);
			case TAIL -> new Socket(-1.9D, 1.45D);
		};
	}

	/** The socket's world-space offset {x, y, z} for a mount facing the given yaw. */
	public static double[] worldOffset(Socket socket, float yawDegrees) {
		double yaw = Math.toRadians(yawDegrees);
		return new double[] { -Math.sin(yaw) * socket.forward(), socket.up(), Math.cos(yaw) * socket.forward() };
	}

	/** How the wounded mount answers the player draining it. */
	public enum Guard { TURN_GUARD, BITE, TAIL_SWEEP, SHAKE_OFF }

	/**
	 * drainerAngle: degrees from the mount's facing to the drainer (0 ahead, +-180 behind). A drainer standing right
	 * at the wound gets the wound turned away from them; otherwise the mount answers with what reaches them.
	 */
	public static Guard guard(Site site, double drainerAngle) {
		double angle = Math.abs(Mth.wrapDegrees(drainerAngle));
		boolean ahead = angle < 60.0D;
		boolean behind = angle > 120.0D;
		if (site == Site.NECK && ahead || site == Site.TAIL && behind) return Guard.TURN_GUARD;
		if (ahead) return Guard.BITE;
		if (behind) return Guard.TAIL_SWEEP;
		return Guard.SHAKE_OFF;
	}

	public static void save(@Nullable Wound wound, CompoundTag tag) {
		if (wound == null) return;
		CompoundTag data = new CompoundTag();
		data.putInt("Site", wound.site().ordinal());
		data.putFloat("Drained", wound.drained());
		data.putInt("Idle", wound.idleTicks());
		data.putFloat("Peak", wound.peak());
		tag.put("VesperWound", data);
	}

	@Nullable
	public static Wound load(CompoundTag tag) {
		if (tag.contains("VesperWound")) {
			CompoundTag data = tag.getCompound("VesperWound");
			float drained = Mth.clamp(data.getFloat("Drained"), 0.0F, DRAIN_REQUIRED);
			return new Wound(Site.fromIndex(data.getInt("Site")), drained, Math.max(0, data.getInt("Idle")),
					Math.max(drained, Mth.clamp(data.getFloat("Peak"), 0.0F, DRAIN_REQUIRED)));
		}
		if (tag.contains("ActiveThroneAnchor") && tag.getInt("ActiveThroneAnchor") >= 0) {
			float drained = Mth.clamp(tag.getFloat("ActiveAnchorDamage") * LEGACY_ANCHOR_SCALE, 0.0F, DRAIN_REQUIRED);
			return new Wound(Site.fromIndex(tag.getInt("ActiveThroneAnchor")), drained, 0, drained);
		}
		return null;
	}
}
