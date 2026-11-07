package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Spike;
import com.vincenthuto.hemomancy.client.render.world.HemorathTelegraphPass.Scene;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMove;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * A body Saint Hemorath has just struck (visual pass 2): read on the client from the hurt flash every client already
 * has (the body's hurt time and the damage event's source), so no packet is added and every viewer sees the same hit.
 * Iron hits drive splinters into the body that bite and are torn back out with chips of iron and a spurt of blood out
 * of the far side; blood hits burst in the Riposte strike's rays with a pale flash and a dark clot shell, spray along
 * the blow and splash the floor at the feet. Timeline: flash 0 to 3, burst 0 to 6, linger and fray to 10.
 */
final class HemorathImpactGeometry {
	private static final double REACH = 24.0D;
	/** Ticks a hit shows for: the hurt flash's own length. */
	private static final float LIFE = 10.0F;
	private static final int TINT = HemorathBloodGeometry.TINT, CORE = HemorathBloodGeometry.CORE, CLOT = HemorathBloodGeometry.CLOT;

	private HemorathImpactGeometry() {
	}

	static void collect(Scene s, HemorathEntity boss) {
		HemorathMove move = HemorathMove.byId(boss.view().moveId());
		boolean iron = move != null && iron(move);
		Vec3 him = boss.getPosition(s.partial());
		List<LivingEntity> struck = s.level().getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(REACH),
				body -> body != boss && body.hurtTime > 0 && body.isAlive());
		for (LivingEntity body : struck) {
			DamageSource source = body.getLastDamageSource();
			if (source == null || source.getEntity() != boss) continue;
			float age = body.hurtDuration - body.hurtTime + s.partial();
			if (age < 0.0F || age >= LIFE) continue;
			Vec3 feet = body.getPosition(s.partial());
			Vec3 at = feet.add(0.0D, body.getBbHeight() * 0.55D, 0.0D);
			Vec3 away = new Vec3(at.x - him.x, 0.0D, at.z - him.z);
			away = away.lengthSqr() < 1e-4 ? new Vec3(1, 0, 0) : away.normalize();
			int seed = body.getId() * 7 + body.hurtDuration;
			if (iron) iron(s, s.local(at), s.local(feet), away, age, seed);
			else blood(s, s.local(at), s.local(feet), away, age, seed);
		}
	}

	/** The moves whose hits are iron. */
	static boolean iron(HemorathMove move) {
		return switch (move) {
			case IRON_RAKE, IRON_THICKET, CROWN_FALL, SPIKED_HALO, STAFF_FALL, STAFF_HEEL, CHAINED_DEBT, TITHE_RECKONING -> true;
			default -> false;
		};
	}

	/** Splinters bite in along the blow and are torn back out; chips fly; blood spurts out the far side. */
	private static void iron(Scene s, Vec3 at, Vec3 feet, Vec3 away, float age, int seed) {
		if (s.solids()) {
			float bite = HemoEasing.clamp01(age / 1.5F) * (1.0F - HemoEasing.ramp(age, 3.0F, 8.0F));
			for (int i = 0; i < 4; i++) {
				double spread = (HemorathFissureRules.unit(seed, i) - 0.5D) * 1.4D;
				Vec3 dir = away.add(away.cross(new Vec3(0, 1, 0)).scale(spread)).add(0.0D, (HemorathFissureRules.unit(seed, 10 + i) - 0.3D) * 0.8D, 0.0D)
						.normalize();
				Vec3 root = at.subtract(dir.scale(0.25D)).add(0.0D, (i - 1.5D) * 0.12D, 0.0D);
				s.p().pushPose();
				s.p().translate(root.x, root.y, root.z);
				s.p().mulPose(new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F),
						new Vector3f((float) dir.x, (float) dir.y, (float) dir.z)));
				Spike splinter = new Spike(0.0D, 0.0D, 0.55D + 0.2D * HemorathFissureRules.unit(seed, 20 + i), 0.05D, 0.0D, 0.0D, 5,
						i * 1.1D);
				HemorathIronGeometry.spike(s.p(), s.iron(), Vec3.ZERO, splinter, bite, 0.6F + 0.4F * bite, 0.0D, HemorathIronGeometry.IRON,
						seed + i * 5);
				s.p().popPose();
			}
			HemorathIronGeometry.chips(s.p(), s.iron(), at.subtract(0.0D, 0.4D, 0.0D), age, 0.9D, 5, seed + 30);
			return;
		}
		flash(s, at, age, seed, 0.32D);
		HemorathBloodGeometry.spatter(s.p(), s.blood(), at, away, Vec3.ZERO, s.right(), s.up(), age, seed + 40, 3.5D, 1.0F);
		splash(s, feet, age, seed);
	}

	/** The Riposte strike's rays out of the body along the blow, a flash and a clot shell, spray, a splash at the feet. */
	private static void blood(Scene s, Vec3 at, Vec3 feet, Vec3 away, float age, int seed) {
		if (s.solids()) return;
		flash(s, at, age, seed, 0.42D);
		float fade = 1.0F - HemoEasing.ramp(age, 2.0F, LIFE);
		double reach = 0.4D + age * 0.09D;
		Vec3 side = away.cross(new Vec3(0, 1, 0));
		for (int i = 0; i < 7; i++) {
			double a = i * 2.39996D + seed;
			Vec3 ray = away.add(side.scale(Math.cos(a) * 0.75D)).add(0.0D, Math.sin(a) * 0.6D, 0.0D).normalize();
			stream(s.p(), s.blood(), at.add(ray.scale(0.12D)), at.add(ray.scale(reach * (0.8D + 0.4D * HemorathFissureRules.unit(seed, i)))),
					Vec3.ZERO, 0.045D, 0.03D, s.time() * 0.05D + i, SLASH, seed + i, TINT, fade);
		}
		for (double offset : slashOffsets()) {
			Vec3 lift = new Vec3(0.0D, offset, 0.0D);
			double cross = 0.25D + Math.min(1.0D, age * 0.2D) * 0.3D;
			stream(s.p(), s.blood(), at.add(side.scale(-cross)).add(lift), at.add(side.scale(cross)).add(lift), Vec3.ZERO, 0.05D, 0.05D,
					s.time() * 0.1D, SLASH, seed + 9, CLOT, fade * 0.8F);
		}
		HemorathBloodGeometry.spatter(s.p(), s.blood(), at, away, Vec3.ZERO, s.right(), s.up(), age, seed + 40, 3.0D, 1.0F);
		splash(s, feet, age, seed);
	}

	private static void flash(Scene s, Vec3 at, float age, int seed, double size) {
		float flash = 1.0F - HemoEasing.ramp(age, 0.0F, 3.5F);
		double r = size * (1.0D - 0.6D * HemoEasing.clamp01(age / 3.5F));
		card(s.p(), s.blood(), at, s.right(), s.up(), r, r, FOCUS, seed, CORE, flash);
		float shell = (1.0F - HemoEasing.ramp(age, 1.0F, LIFE)) * 0.45F;
		double w = size * (1.2D + age * 0.12D);
		card(s.p(), s.blood(), at, s.right(), s.up(), w, w * 1.2D, WISP, seed + 1, CLOT, shell);
	}

	private static void splash(Scene s, Vec3 feet, float age, int seed) {
		float grow = HemoEasing.easeOutCubic(HemoEasing.clamp01(age / 6.0F));
		float fade = 1.0F - HemoEasing.ramp(age, 5.0F, LIFE);
		ground(s.p(), s.blood(), feet.add(0.0D, 0.03D, 0.0D), 0.2D + 0.45D * grow, POOL, seed + 2, TINT, 0.7F * fade);
	}
}
