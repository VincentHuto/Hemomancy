package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

/** One memory's cast, replayed by the loom's echo at the release of each loop. Visual only. */
public interface EchoCast {
	/**
	 * Where the echo stands and how large it is. `figure` is its absolute scale: 0.42 for a base loom's small grey
	 * memory, 1.0 for a Cortical loom's life-size Harbinger. Live casts are sized for a full-size caster, so every
	 * radius and distance an echo cast uses is multiplied by it.
	 */
	record EchoContext(ClientLevel level, Vec3 hands, Vec3 feet, Vec3 forward, float figure, BloodManipulation manipulation) {
		/** The echo's chest, where self-casts and auras centre. */
		public Vec3 chest() {
			return feet.add(0, 1.3D * figure, 0);
		}

		/** A point `blocks` (at life size) ahead along the loom's front, at chest height. */
		public Vec3 ahead(double blocks) {
			return chest().add(forward.scale(blocks * figure));
		}

		/** An imagined body in front of the loom, at chest height: what aimed casts strike. */
		public Vec3 target() {
			return ahead(2.6D);
		}

		/** The target's feet, for casts that land on the ground there. */
		public Vec3 targetFeet() {
			return feet.add(forward.scale(2.6D * figure));
		}

		public double scaled(double lifeSize) {
			return lifeSize * figure;
		}

		/** Horizontal and perpendicular to the loom's front, to the echo's right. */
		public Vec3 side() {
			Vec3 flat = new Vec3(forward.x, 0, forward.z);
			if (flat.lengthSqr() < 1.0E-8D) return new Vec3(1, 0, 0);
			flat = flat.normalize();
			return new Vec3(-flat.z, 0, flat.x);
		}
	}

	void release(EchoContext context);
}
