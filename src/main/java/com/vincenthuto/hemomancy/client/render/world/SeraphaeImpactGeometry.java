package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Look;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Spur;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeTelegraphPass.Scene;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeTissueGeometry.Ground;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A body Saint Seraphae has just struck (visual pass 2): read on the client from the hurt flash every client already
 * has (the body's hurt time and the damage event's source), so no packet is added and every viewer sees the same hit.
 * Her blows heal as they hurt. A nerve's strike (the lashes, the Convulsion, the lance, the wrench, the grasp) bursts in
 * light with gold crackling out of it and nerve-fire spiralling up the body; growth's strike (the Knot, Healed Ground)
 * drives bone out of the body along the blow and draws it back; light's strike (the Eye, the Fracture, the Extraction,
 * the flood, the thread) is the Riposte burst in white. The floor at the feet swells and goes raw under every one.
 * Timeline: flash 0 to 3.5, burst 0 to 6, bone out 0 to 1.5 and back from 3 to 8, fray to 10.
 */
final class SeraphaeImpactGeometry {
	private static final double REACH = 24.0D;

	/** What a strike of hers is made of. */
	enum Strike { FIRE, GROWTH, LIGHT }

	private SeraphaeImpactGeometry() {
	}

	/** The strike a move's hits land as; light when no move runs (a burst of growth, the presence). */
	static Strike strike(@Nullable SeraphaeMove move) {
		if (move == null) return Strike.LIGHT;
		return switch (move) {
			case NERVE_LASH, COUNTER_LASH, CONVULSION, CONVULSION_WIDE, AXON_LANCE, AXON_MIRROR, CHAIN_WRENCH, WING_GRASP -> Strike.FIRE;
			case REGROWTH_KNOT, HEALED_GROUND -> Strike.GROWTH;
			default -> Strike.LIGHT;
		};
	}

	static void collect(Scene s, SeraphaeEntity boss) {
		Strike strike = strike(SeraphaeMove.byId(boss.view().moveId()));
		Vec3 her = boss.getPosition(s.partial());
		Minecraft mc = Minecraft.getInstance();
		List<LivingEntity> struck = s.level().getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(REACH),
				body -> body != boss && body.hurtTime > 0 && body.isAlive());
		for (LivingEntity body : struck) {
			DamageSource source = body.getLastDamageSource();
			if (source == null || source.getEntity() != boss) continue;
			float age = body.hurtDuration - body.hurtTime + s.partial();
			if (age < 0.0F || age >= SeraphaeOvergrowthRules.IMPACT) continue;
			Vec3 feet = body.getPosition(s.partial());
			Vec3 at = feet.add(0.0D, body.getBbHeight() * 0.55D, 0.0D);
			Vec3 away = new Vec3(at.x - her.x, 0.0D, at.z - her.z);
			away = away.lengthSqr() < 1e-4 ? new Vec3(1, 0, 0) : away.normalize();
			int seed = body.getId() * 7 + body.hurtDuration;
			// in first person the struck viewer sees the floor and the nerve-fire, never a burst inside their own eye
			boolean self = body == mc.player && mc.options.getCameraType().isFirstPerson();
			if (body.onGround()) floor(s, feet, age, seed);
			switch (strike) {
				case FIRE -> nerve(s, at, feet, away, body.getBbHeight(), age, seed, self);
				case GROWTH -> growth(s, at, away, age, seed, self);
				case LIGHT -> {
					if (s.solids()) SeraphaeBoneGeometry.shards(s.p(), s.bone(), s.local(at), age, 1.0D, 4, seed + 30);
					else if (!self) SeraphaeLightGeometry.burst(s.p(), s.lux(), s.gold(), s.local(at), away, Vec3.ZERO, s.right(), s.up(), age,
							0.4D, seed, false);
				}
			}
		}
	}

	/** The floor at the struck feet swells raw and lights, then subsides. */
	private static void floor(Scene s, Vec3 feet, float age, int seed) {
		float grow = HemoEasing.easeOutCubic(HemoEasing.clamp01(age / 5.0F));
		Look look = new Look(1.0F, 0.6F + 0.4F * grow, 1.0F - HemoEasing.ramp(age, 1.0F, 8.0F) * 0.7F, 0.7F, 1.0F,
				1.0F - HemoEasing.ramp(age, 5.0F, SeraphaeOvergrowthRules.IMPACT));
		s.bloom(new Ground(feet.x, feet.z, 1.0D, 0.0D, feet.y, s.camera()), 0.0D, 0.3D + 0.55D * grow, -180.0D, 180.0D, look, 0.9F,
				seed + 2);
	}

	/** A nerve's strike: light bursts with gold crackling out of it, nerve-fire spirals up the body, shards fly. */
	private static void nerve(Scene s, Vec3 at, Vec3 feet, Vec3 away, double height, float age, int seed, boolean self) {
		if (s.solids()) {
			SeraphaeBoneGeometry.shards(s.p(), s.bone(), s.local(at), age, 0.9D, 4, seed + 30);
			return;
		}
		if (!self) SeraphaeLightGeometry.burst(s.p(), s.lux(), s.gold(), s.local(at), away, Vec3.ZERO, s.right(), s.up(), age, 0.32D, seed, true);
		float fade = 1.0F - HemoEasing.ramp(age, 2.0F, SeraphaeOvergrowthRules.IMPACT);
		double climb = HemoEasing.easeOutCubic(HemoEasing.clamp01(age / 4.0F));
		List<Vec3> helix = new ArrayList<>(17);
		for (int i = 0; i <= 16; i++) {
			double t = i / 16.0D * climb;
			double angle = t * Math.PI * 4.0D + seed;
			double r = 0.38D * (1.0D - 0.3D * t);
			helix.add(s.local(feet.add(Math.cos(angle) * r, 0.1D + t * height, Math.sin(angle) * r)));
		}
		SeraphaeLightGeometry.nerveLine(s.p(), s.gold(), helix, Vec3.ZERO, 0.07D, seed + 9, fade);
	}

	/** Growth's strike: bone drives out of the body along the blow and is drawn back in; light bursts round it. */
	private static void growth(Scene s, Vec3 at, Vec3 away, float age, int seed, boolean self) {
		if (!s.solids()) {
			if (!self) SeraphaeLightGeometry.burst(s.p(), s.lux(), s.gold(), s.local(at), away, Vec3.ZERO, s.right(), s.up(), age, 0.3D, seed,
					false);
			return;
		}
		float bite = SeraphaeOvergrowthRules.bite(age);
		if (bite > 0.0F && !self) {
			Vec3 side = away.cross(new Vec3(0, 1, 0));
			for (int i = 0; i < 4; i++) {
				double spread = (SeraphaeOvergrowthRules.unit(seed, i) - 0.5D) * 1.4D;
				Vec3 dir = away.add(side.scale(spread)).add(0.0D, (SeraphaeOvergrowthRules.unit(seed, 10 + i) - 0.3D) * 0.8D, 0.0D).normalize();
				Vec3 root = s.local(at).subtract(dir.scale(0.22D)).add(0.0D, (i - 1.5D) * 0.12D, 0.0D);
				s.p().pushPose();
				s.p().translate(root.x, root.y, root.z);
				s.p().mulPose(new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f((float) dir.x, (float) dir.y, (float) dir.z)));
				Spur spur = new Spur(0.0D, 0.0D, 0.6D + 0.2D * SeraphaeOvergrowthRules.unit(seed, 20 + i), 0.06D, 0.0D, 0.0D, 5, i * 1.1D,
						0.1D);
				SeraphaeBoneGeometry.spur(s.p(), s.bone(), Vec3.ZERO, spur, bite, 1.0D, 0.6F + 0.4F * bite,
						SeraphaeBoneGeometry.withLight(SeraphaeBoneGeometry.SPUR, 0.8F), seed + i * 5);
				s.p().popPose();
			}
		}
		SeraphaeBoneGeometry.shards(s.p(), s.bone(), s.local(at).subtract(0.0D, 0.4D, 0.0D), age, 0.9D, 5, seed + 30);
	}
}
