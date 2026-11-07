package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.world.PutricielBloomRules.Cap;
import com.vincenthuto.hemomancy.client.render.world.PutricielGroundGeometry.Front;
import com.vincenthuto.hemomancy.client.render.world.PutricielGroundGeometry.Ground;
import com.vincenthuto.hemomancy.client.render.world.PutricielGroundGeometry.Paint;
import com.vincenthuto.hemomancy.client.render.world.PutricielTelegraphPass.Scene;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDamageTypes;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * A body Saint Putriciel has just struck (visual pass 2): read on the client from the hurt flash every client already
 * has (the body's hurt time and the damage event's source, which must be Putriciel), so no packet is added and every
 * viewer sees the same hit. A rot hit colonises the wound: bracket caps burst out of the body along the blow and
 * wither, a puff of spores breaks off it, ooze is flung along the blow and clods drop at the feet. A cleansing hit
 * wraps the body in the purifier's fire: tongues climb it from the wound, sparks fly along the blow, soot lifts and
 * the floor scorches at the feet. Timeline: burst 0 to 3, hold to 6, wither and fade to {@link #LIFE}.
 */
final class PutricielImpactGeometry {
	private static final double REACH = 24.0D;
	/** Ticks a hit shows for: the hurt flash's own length. */
	static final float LIFE = 10.0F;

	private PutricielImpactGeometry() {
	}

	static void collect(Scene s, PutricielEntity boss) {
		Vec3 them = boss.getPosition(s.partial());
		List<LivingEntity> struck = s.level().getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(REACH),
				body -> body != boss && body.hurtTime > 0 && body.isAlive());
		for (LivingEntity body : struck) {
			DamageSource source = body.getLastDamageSource();
			if (source == null || source.getEntity() != boss) continue;
			float age = body.hurtDuration - body.hurtTime + s.partial();
			if (age < 0.0F || age >= LIFE) continue;
			Vec3 feet = body.getPosition(s.partial());
			Vec3 at = feet.add(0.0D, body.getBbHeight() * 0.6D, 0.0D);
			Vec3 away = new Vec3(at.x - them.x, 0.0D, at.z - them.z);
			away = away.lengthSqr() < 1.0E-4D ? new Vec3(1, 0, 0) : away.normalize();
			int seed = body.getId() * 7 + body.hurtDuration;
			double skin = Math.max(0.25D, body.getBbWidth() * 0.55D);
			if (source.is(SaintDamageTypes.PUTRICIEL_CLEANSING)) fire(s, at, feet, away, skin, age, seed);
			else rot(s, s.local(at), s.local(feet), away, skin, age, seed);
		}
	}

	/** Caps burst out of the wound and wither; spores puff off it; ooze is flung along the blow; clods drop. */
	private static void rot(Scene s, Vec3 at, Vec3 feet, Vec3 away, double skin, float age, int seed) {
		if (s.solid()) {
			for (int i = 0; i < 3; i++) {
				double yaw = Math.atan2(away.z, away.x) + (PutricielBloomRules.unit(seed, i) - 0.5D) * 1.6D;
				Vec3 root = at.add(Math.cos(yaw) * skin * 0.8D, (i - 1) * 0.18D, Math.sin(yaw) * skin * 0.8D);
				Cap cap = new Cap(0.0D, 0.0D, 0.0D, 0.16D + 0.06D * PutricielBloomRules.unit(seed, 10 + i), yaw, 0.35D, 2.2D, 0.04D, 0, 0.0F);
				float own = age * 1.4F - i * 0.5F;
				PutricielGrowthGeometry.cap(s.p(), s.growth(), root, cap, PutricielBloomRules.size(own, 6.0F),
						PutricielBloomRules.withered(own, 6.0F), 0.0D, PutricielGrowthGeometry.FRESH, PutricielBloomRules.formation(own, 6.0F),
						seed + i * 11);
			}
			PutricielGrowthGeometry.clods(s.p(), s.growth(), feet, age, 1.0D, 3, PutricielGrowthGeometry.LOAM, seed + 30);
			return;
		}
		if (!s.spore()) return;
		float fade = 1.0F - age / LIFE;
		Vec3 wound = at.add(away.scale(skin));
		PutricielSporeGeometry.burst(s.p(), s.spores(), wound, s.right(), s.up(), age * 1.6F, 0.7D, 5, 0.3F, 1.0F, seed + 40);
		float fling = HemoEasing.clamp01(age / 4.0F);
		for (int i = 0; i < 4; i++) {
			double turn = (PutricielBloomRules.unit(seed, 50 + i) - 0.5D) * 1.1D;
			Vec3 dir = new Vec3(away.x * Math.cos(turn) - away.z * Math.sin(turn), 0.0D, away.z * Math.cos(turn) + away.x * Math.sin(turn));
			double speed = 0.9D + 0.6D * PutricielBloomRules.unit(seed, 60 + i);
			Vec3 head = wound.add(dir.scale(speed * fling)).add(0.0D, 0.3D * fling - 0.9D * fling * fling, 0.0D);
			Vec3 tail = wound.add(dir.scale(speed * Math.max(0.0D, fling - 0.3D)));
			LuxUmbraGeometry.ribbonPath(s.p(), s.spores(), List.of(tail, tail.lerp(head, 0.5D), head), Vec3.ZERO, 0.03D, 0.07D,
					PutricielSporeGeometry.OOZE, seed + 70 + i, PutricielSporeGeometry.state(0.0F, 0.4F, 0.2F), fade);
		}
		PutricielSporeGeometry.mote(s.p(), s.spores(), wound, s.right(), s.up(), 0.18D * fade, 1.0F, fade, seed + 80);
	}

	/** Tongues climb the body from the wound; sparks fly along the blow; soot lifts; the floor scorches at the feet. */
	private static void fire(Scene s, Vec3 worldAt, Vec3 worldFeet, Vec3 away, double skin, float age, int seed) {
		if (s.ground()) {
			Ground g = new Ground(worldFeet.x, worldFeet.z, 1.0D, 0.0D, Math.floor(worldFeet.y + 1.0E-3D), s.camera());
			Vec3 centre = g.world(0.0D, 0.0D);
			PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.0D, 0.75D, -180.0D, 180.0D,
					Front.from(centre, 0.75D * HemoEasing.clamp01(age / 3.0F), 0.25D),
					new Paint(PutricielGroundGeometry.SCORCH, 0.9F * (1.0F - age / LIFE), 0.3F, 1.0F - HemoEasing.ramp(age, 6.0F, LIFE)), seed);
			return;
		}
		if (!s.spore()) return;
		Vec3 at = s.local(worldAt);
		float climb = PutricielBloomRules.flare(age) * (1.0F - HemoEasing.ramp(age, 5.0F, LIFE));
		for (int i = 0; i < 5; i++) {
			double a = Math.atan2(away.z, away.x) + Math.PI + (i - 2) * 0.55D;
			Vec3 root = at.add(Math.cos(a) * skin * 0.7D, -0.35D + (i % 2) * 0.2D, Math.sin(a) * skin * 0.7D);
			PutricielFlameGeometry.tongue(s.p(), s.flame(), root, new Vec3(Math.cos(a) * 0.15D, 0.0D, Math.sin(a) * 0.15D),
					(0.55D + 0.25D * PutricielBloomRules.unit(seed, i)) * climb, 0.09D, s.time(), seed + i, 0.95F);
		}
		PutricielFlameGeometry.flash(s.p(), s.flame(), at, s.right(), s.up(), age, 0.4D, seed + 10, 0.9F);
		PutricielFlameGeometry.sparks(s.p(), s.flame(), at, away, s.right(), s.up(), age, 1.6D, 6, seed + 20, 1.0F);
		PutricielFlameGeometry.smoke(s.p(), s.flame(), at.add(0.0D, 0.6D, 0.0D), s.right(), s.up(), 0.35D, s.time(), seed + 30,
				0.5F * (1.0F - age / LIFE));
	}
}
