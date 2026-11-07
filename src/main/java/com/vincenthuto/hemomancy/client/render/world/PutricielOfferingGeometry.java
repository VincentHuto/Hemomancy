package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The dispatcher's door to Putriciel's world geometry (hooked in {@code ManipulationVisualRenderer} before the generic
 * geometry, and skipped by {@code ManipulationMotes}). The work is split three ways (design section 12.4):
 * {@link PutricielRotGeometry} (the body's rot, rot moves and impacts), {@link PutricielFireGeometry} (the fire on
 * the body, the Offering, fire impacts, the ending) and {@link PutricielColonisationGeometry} (growth on players and the
 * presence's ground read). Telegraph shapes are the kit's pass; this draws what the moves and the body do.
 */
final class PutricielOfferingGeometry {
	/** How far from the camera a Putriciel is drawn. */
	static final double RANGE = 96.0D;

	/** One body being drawn: its replay state, its seed and age, and the camera's frame relative to the body. */
	record Body(PutricielAnimState motion, int seed, float age, Vec3 camera, Vec3 right, Vec3 up, Vec3 floor) {
		boolean fire() {
			return motion.stage().fire() || motion.endingTime() >= 0.0F;
		}
	}

	private PutricielOfferingGeometry() {
	}

	static boolean handles(Form form) {
		return form == Form.PUTRICIEL_OFFERING;
	}

	static void collect(ClientLevel level, PoseStack p, LuxUmbraBatch batch, Vec3 camera, Vec3 right, Vec3 up, float partial) {
		for (PutricielEntity boss : level.getEntitiesOfClass(PutricielEntity.class, new AABB(camera, camera).inflate(RANGE))) {
			if (!boss.isAlive() || boss.isInvisible()) continue;
			float rise = boss.riseTime(partial);
			if (rise >= 0 && rise < SaintRise.TICKS) continue;
			PutricielAnimState motion = PutricielAnimState.of(boss);
			motion.update(boss, partial);
			Vec3 at = boss.getPosition(partial);
			Vec3 floor = new Vec3(0.0D, Math.floor(at.y + 1.0E-3D) - at.y, 0.0D);
			Body body = new Body(motion, boss.getId(), AnimationTime.age(boss, partial), camera.subtract(at), right, up, floor);
			p.pushPose();
			p.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
			VertexConsumer rot = batch.vertices(LuxUmbraBatch.Material.PUTRICIEL_ROT);
			PutricielRotGeometry.body(p, rot, body);
			if (body.fire()) PutricielFireGeometry.body(p, batch.vertices(LuxUmbraBatch.Material.FLAME), rot, body);
			PutricielColonisationGeometry.presence(p, rot, body.fire() ? batch.vertices(LuxUmbraBatch.Material.FLAME)
					: FerricDuctilisGeometry.DISCARD, boss, body);
			p.popPose();
			PutricielColonisationGeometry.players(level, boss, p, rot, camera, right, up, partial);
		}
	}

	/** A burst on the {@code PUTRICIEL_OFFERING} channel: fire kinds in the Flammeus look, the rest in rot. */
	static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer rot, VertexConsumer fire, Vec3 camera,
			Vec3 right, Vec3 up, double time, float age, float alpha) {
		float fade = alpha * PutricielVisualRules.fade(age, packet.ticks());
		if (fade <= 0) return;
		int kind = packet.count();
		int seed = Math.floorMod(packet.from().hashCode(), 113);
		if (PutricielVisualRules.fireBurst(kind)) PutricielFireGeometry.burst(p, fire, rot, camera, right, up, kind, packet.radius(), age, fade, seed);
		else PutricielRotGeometry.burst(p, rot, camera, right, up, kind, packet.radius(), age, fade, seed);
	}
}
