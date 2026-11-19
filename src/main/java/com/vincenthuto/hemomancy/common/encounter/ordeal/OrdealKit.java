package com.vincenthuto.hemomancy.common.encounter.ordeal;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * The shared pieces every ordeal's choreography and puppet moves are built from (2026-11-17): a footprint on the
 * floor drawn by the bold ordeal pass, a blow on the fighter, the ring's bounds and the turn of a body.
 */
public final class OrdealKit {
	private OrdealKit() {
	}

	/**
	 * One footprint on the ring's floor for {@code life} ticks, owned by {@code owner} (the professor or the puppet) and
	 * drawn by the client's {@code OrdealTelegraphPass} from its shape: a disc as a closing mark, a ring as a smoulder
	 * then a wave, a lane as a bar. {@code kind} is one of the owner's in {@link OrdealHazardKinds}. Hazards never
	 * pulse: the move lands itself.
	 */
	public static void hazard(ServerLevel chamber, Entity owner, int kind, BossFootprint shape, Vec3 origin, Vec3 aim, int life) {
		if (owner == null) return;
		Vec3 facing = aim.distanceToSqr(origin) < 1.0E-6D ? origin.add(0, 0, 1) : aim;
		BossGroundHazardEntity.spawn(chamber, owner, kind, shape, FootprintFrame.simple(origin, facing, facing, origin.y), life,
				Integer.MAX_VALUE);
	}

	/** A blow on the fighter from {@code by}: damage, then a shove along {@code away} and up. */
	public static void strike(ServerLevel chamber, ServerPlayer caster, Entity by, float damage, Vec3 away, double push,
			double lift) {
		caster.hurt(by instanceof LivingEntity living ? chamber.damageSources().mobAttack(living) : chamber.damageSources().magic(),
				damage);
		Vec3 out = flat(away);
		if (push > 0.0D || lift > 0.0D) {
			caster.setDeltaMovement(out.x * push, lift, out.z * push);
			caster.hurtMarked = true;
		}
		CircusEffects.impact(chamber, caster.position().add(0, 1, 0), 0.6D);
	}

	/** Keeps a body inside the ring: set back inside it if it has gone past {@code limit} from the centre. */
	public static void keepInRing(Entity body, Vec3 floor, double limit) {
		Vec3 out = new Vec3(body.getX() - floor.x, 0.0D, body.getZ() - floor.z);
		if (out.length() <= limit) return;
		Vec3 back = floor.add(out.normalize().scale(limit));
		body.teleportTo(back.x, Math.max(body.getY(), floor.y), back.z);
	}

	/** Turns a body to face along {@code aim} (body, head and yaw together). */
	public static void face(Entity body, Vec3 aim) {
		if (aim.lengthSqr() < 1.0E-6D) return;
		float yaw = (float) (Mth.atan2(aim.z, aim.x) * Mth.RAD_TO_DEG) - 90.0F;
		body.setYRot(yaw);
		body.setYHeadRot(yaw);
		body.setYBodyRot(yaw);
	}

	/** The yaw that looks from {@code from} to {@code to}. */
	public static float yaw(Vec3 from, Vec3 to) {
		return (float) (Math.toDegrees(Math.atan2(to.z - from.z, to.x - from.x)) - 90.0D);
	}

	public static Vec3 flat(Vec3 v) {
		Vec3 f = new Vec3(v.x, 0.0D, v.z);
		return f.lengthSqr() < 1.0E-6D ? Vec3.ZERO : f.normalize();
	}

	public static double horizontal(Vec3 a, Vec3 b) {
		return Math.hypot(a.x - b.x, a.z - b.z);
	}
}
