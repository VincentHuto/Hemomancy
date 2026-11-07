package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielColonisationRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.FOCUS;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.POOL;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.RIPPLE;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.SLASH;

/**
 * Colonisation where everyone can see it, from the kit's {@code DATA_CHALLENGERS} flags (bits 0 to 5 carry each
 * challenger's growth in halves): a soft mould stain under the feet growing with it, shelf cards on the shoulders and
 * back from 10, small caps from 15, and soot motes while the fire burns it off. Also the presence's ground read: a faint
 * mould ring at the rot radius breathing on each gain, a thin heat ring at 4 in the fire. No strands, no contour lines.
 */
final class PutricielColonisationGeometry {
	static final double PLAYER_RANGE = 48.0D;
	static final float STAIN_ALPHA = .25F;

	private PutricielColonisationGeometry() {
	}

	/** The presence's ring round the body (drawn in the body's frame). */
	static void presence(PoseStack p, VertexConsumer rot, VertexConsumer fire, PutricielEntity boss,
			PutricielOfferingGeometry.Body b) {
		double radius = boss.syncedPresenceRadius();
		if (radius <= 0.0D) return;
		Vec3 at = b.floor().add(0, .03, 0);
		if (b.motion().stage().fire()) {
			LuxUmbraGeometry.ground(p, fire, at, radius, RIPPLE, b.seed(), PutricielFireGeometry.TINT, .2F);
		} else {
			float breath = (float) Math.pow(Math.max(0.0D, Math.sin(b.age() * Math.PI / 20.0D)), 6);
			LuxUmbraGeometry.ground(p, rot, at, radius, RIPPLE, b.seed(), PutricielRotGeometry.TINT, .10F + .08F * breath);
		}
	}

	/** Growth on every player near a Putriciel, read from its synced challenger flags. */
	static void players(ClientLevel level, PutricielEntity boss, PoseStack p, VertexConsumer rot, Vec3 camera, Vec3 right,
			Vec3 up, float partial) {
		for (Player player : level.players()) {
			if (player.distanceToSqr(boss) > PLAYER_RANGE * PLAYER_RANGE || player.isSpectator()) continue;
			int flags = boss.syncedChallengerFlags(player.getId());
			float value = PutricielColonisationRules.value(flags);
			if (value <= 0.0F) continue;
			Vec3 feet = player.getPosition(partial);
			p.pushPose();
			p.translate(feet.x - camera.x, feet.y - camera.y, feet.z - camera.z);
			int seed = player.getId() * 7;
			LuxUmbraGeometry.ground(p, rot, new Vec3(0, .02, 0), .4 + .04 * value, POOL, seed, PutricielRotGeometry.TINT, STAIN_ALPHA);
			float yaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
			Vec3 side = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
			Vec3 back = new Vec3(Math.sin(yaw), 0, -Math.cos(yaw));
			if (value >= PutricielColonisationRules.SHELVED) {
				for (int i = 0; i < 3; i++) {
					Vec3 at = new Vec3(0, 1.35 + i * .12, 0).add(side.scale((i - 1) * .22)).add(back.scale(.18));
					LuxUmbraGeometry.card(p, rot, at, right, up, .09, .035, SLASH, seed + i, PutricielRotGeometry.CREAM, .45F);
				}
			}
			if (value >= PutricielColonisationRules.FRUITING) {
				for (int i = 0; i < 2; i++) {
					Vec3 at = new Vec3(0, 1.5, 0).add(side.scale(i == 0 ? -.28 : .28));
					LuxUmbraGeometry.card(p, rot, at, right, up, .05, .05, FOCUS, seed + 5 + i, PutricielRotGeometry.CREAM, .35F);
				}
			}
			if (PutricielColonisationRules.burning(flags)) {
				float age = AnimationTime.age(player, partial);
				for (int i = 0; i < 3; i++) {
					double s = (age * .05 + i / 3.0) % 1;
					LuxUmbraGeometry.card(p, rot, new Vec3(Math.sin(i * 2.1) * .25, .8 + s * 1.2, Math.cos(i * 2.1) * .25), right, up,
							.03, .03, FOCUS, seed + 9 + i, PutricielRotGeometry.CLOT, (float) Math.sin(s * Math.PI) * .4F);
				}
			}
			p.popPose();
		}
	}
}
