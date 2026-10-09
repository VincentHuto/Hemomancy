package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.item.MarionetteCrossbarModel;
import com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar.CrossbarMotionRules;
import com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar.CrossbarStringAnchors;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Control threads from a puppeteer to each body it drives. They share the Animus strand surface used by the
 * Hematic Command tether, so player, Vesper, wild Blood Drunk and Circus performer threads read as the same
 * living material.
 */
public final class PuppeteerThreadRenderer {
	private static final int SEGMENTS = 28;
	private static final double WILD_PUPPETEER_ANCHOR_SCALE = 0.25;
	private static final double WILD_DOLL_ANCHOR_SCALE = -0.5;
	private static final double VESPER_ANCHOR_SCALE = 0.72;
	private static final double SANGUINE_HOUND_ANCHOR_SCALE = -0.25;

	/** Puppets that share one controller's Crossbar; each keeps its own string on the drawn bar. */
	private record CrossbarGroup(UUID owner, UUID crossbar) {
	}

	private PuppeteerThreadRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) {
			return;
		}
		// One pass finds every thread end, rather than rescanning the level once per summon or doll.
		List<LivingEntity> summons = new ArrayList<>();
		List<EnthralledDollEntity> dolls = new ArrayList<>();
		Map<UUID, LivingEntity> controllers = new HashMap<>();
		for (Entity entity : level.entitiesForRendering()) {
			if (entity instanceof LivingEntity summon && entity instanceof BoundPuppeteerSummon bound) {
				if (bound.hemomancy$getOwnerUUID() != null) summons.add(summon);
			} else if (entity instanceof EnthralledDollEntity doll && doll.isSummonedByPuppeteer()) {
				dolls.add(doll);
			} else if ((entity instanceof VesperTheCrownedRefusalEntity || entity instanceof BloodDrunkPuppeteerEntity
					|| entity instanceof CircusPerformerEntity) && entity.isAlive()) {
				controllers.put(entity.getUUID(), (LivingEntity) entity);
			}
		}
		if (summons.isEmpty() && dolls.isEmpty()) {
			return;
		}

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		RenderType type = ManipulationMaterials.ANIMUS.renderType();
		VertexConsumer consumer = buffer.getBuffer(type);
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		Map<CrossbarGroup, List<Integer>> groups = new HashMap<>();
		for (LivingEntity summon : summons) {
			BoundPuppeteerSummon bound = (BoundPuppeteerSummon) summon;
			groups.computeIfAbsent(new CrossbarGroup(bound.hemomancy$getOwnerUUID(), bound.hemomancy$getCrossbarUUID()),
					group -> new ArrayList<>()).add(summon.getId());
		}
		for (LivingEntity summon : summons) {
			renderThreadToSummon(poseStack, consumer, summon, (BoundPuppeteerSummon) summon, partialTick, camera,
					level, controllers, groups);
		}
		for (EnthralledDollEntity doll : dolls) {
			renderThreadToWildDoll(poseStack, consumer, doll, partialTick, camera, controllers);
		}
		buffer.endBatch(type);
	}

	private static void renderThreadToWildDoll(PoseStack poseStack, VertexConsumer consumer,
											   EnthralledDollEntity doll, float partialTick,
											   Vec3 camera, Map<UUID, LivingEntity> controllers) {
		LivingEntity controller = findDollController(controllers, doll.getOwnerUUID());
		if (controller == null) {
			return;
		}
		Vec3 start = (controller instanceof CircusPerformerEntity performer
				? performerAnchor(performer, partialTick)
				: entityAnchor(controller, partialTick, WILD_PUPPETEER_ANCHOR_SCALE)).subtract(camera);
		Vec3 end = entityAnchor(doll, partialTick, WILD_DOLL_ANCHOR_SCALE).subtract(camera);
		renderThread(poseStack, consumer, start, end, doll.tickCount + partialTick, 1.0F, false);
	}

	/** Wild Blood Drunk puppeteers and Circus performers both raise Enthralled Dolls on strings. */
	private static LivingEntity findDollController(Map<UUID, LivingEntity> controllers, UUID ownerId) {
		LivingEntity controller = ownerId == null ? null : controllers.get(ownerId);
		return controller instanceof BloodDrunkPuppeteerEntity || controller instanceof CircusPerformerEntity
				? controller : null;
	}

	private static void renderThreadToSummon(PoseStack poseStack, VertexConsumer consumer,
											 LivingEntity summon, BoundPuppeteerSummon bound,
											 float partialTick, Vec3 camera, ClientLevel level,
											 Map<UUID, LivingEntity> controllers, Map<CrossbarGroup, List<Integer>> groups) {
		UUID ownerId = bound.hemomancy$getOwnerUUID();
		if (ownerId == null) {
			return;
		}
		Player player = level.getPlayerByUUID(ownerId);
		// Vesper's summons and Circus teaching bodies carry their non-player controller as owner.
		LivingEntity listed = controllers.get(ownerId);
		LivingEntity controller = player != null ? player
				: listed instanceof VesperTheCrownedRefusalEntity || listed instanceof CircusPerformerEntity ? listed : null;
		if (controller == null) {
			return;
		}
		int dismissalTicks = bound.hemomancy$getDismissalTicks();
		if (!PuppeteerSummonRules.shouldRenderDismissingSummon(summon.tickCount, dismissalTicks)) {
			return;
		}
		float fade = (float) PuppeteerSummonRules.dismissalAlpha(dismissalTicks, partialTick);
		if (fade <= 0.01F) {
			return;
		}

		// A third-person Crossbar drawn this frame hands each puppet its own string knot.
		UUID crossbarId = bound.hemomancy$getCrossbarUUID();
		int string = CrossbarMotionRules.stringIndex(groups.getOrDefault(new CrossbarGroup(ownerId, crossbarId), List.of()),
				summon.getId(), MarionetteCrossbarModel.STRINGS.length);
		Vec3 knot = controller instanceof CircusPerformerEntity ? null
				: CrossbarStringAnchors.knot(controller, crossbarId, string, partialTick);
		Vec3 start = (knot != null ? knot : controller instanceof Player owner
				? ownerAnchor(owner, crossbarId, partialTick)
				: controller instanceof CircusPerformerEntity performer
				? performerAnchor(performer, partialTick)
				: entityAnchor(controller, partialTick, VESPER_ANCHOR_SCALE)).subtract(camera);

		Vec3 end = summonAnchor(summon, partialTick).subtract(camera);
		boolean gnawed = controller instanceof Player owner
				&& bound.hemomancy$getCrossbarUUID() != null
				&& !bound.hemomancy$isTrialSummon()
				&& BoundSummonBehavior.hasEquippedMorphling(owner);
		renderThread(poseStack, consumer, start, end, summon.tickCount + partialTick, fade, gnawed);
	}

	private static void renderThread(PoseStack poseStack, VertexConsumer consumer,
									 Vec3 start, Vec3 end, float time, float fade, boolean gnawed) {
		Vec3 delta = end.subtract(start);
		Vec3 point = threadPoint(start, delta, 0.0F, time);
		double texture = 0.0;
		for (int i = 0; i < SEGMENTS; i++) {
			float t = (float) i / SEGMENTS;
			float nextT = (float) (i + 1) / SEGMENTS;
			Vec3 next = threadPoint(start, delta, nextT, time);
			double nextTexture = texture + point.distanceTo(next);
			// A Morphling on the owner chews gaps that crawl along the thread toward the body.
			boolean bitten = gnawed && (i + (int) (time / 12.0F)) % 9 == 4;
			if (!bitten) {
				float pulse = (float) (Math.sin(time * 0.18F + t * 9.0F) * 0.5F + 0.5F);
				int color = threadColor(pulse, gnawed && i % 4 != 0);
				float alpha = (float) ((0.67 + 0.22 * Math.sin(t * Math.PI)) * fade);
				VisceralMesh.segment(poseStack, consumer, point, next,
						PuppeteerThreadEndpointRules.threadRadius(t), PuppeteerThreadEndpointRules.threadRadius(nextT),
						color, alpha, texture, nextTexture);
			}
			point = next;
			texture = nextTexture;
		}
		if (!gnawed) {
			// A bead of blood rides out along the thread, as on the Hematic Command tether.
			float travel = (time * 0.035F) % 1.0F;
			VisceralMesh.drop(poseStack, consumer, threadPoint(start, delta, travel, time), 0.018, 0.03,
					0xFF9AAB, fade * 0.9F, 0);
		}
	}

	private static int threadColor(float pulse, boolean dimmed) {
		int red = (int) (150 + pulse * 85);
		int green = (int) (8 + pulse * 20);
		int blue = (int) (12 + pulse * 18);
		if (dimmed) {
			red = (int) (red * 0.55F);
			green = (int) (green * 0.4F);
			blue = (int) (blue * 0.4F);
		}
		return (red << 16) | (green << 8) | blue;
	}

	private static Vec3 ownerAnchor(Player owner, UUID crossbarId, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		double x = Mth.lerp(partialTick, owner.xOld, owner.getX());
		double y = Mth.lerp(partialTick, owner.yOld, owner.getY()) + owner.getEyeHeight();
		double z = Mth.lerp(partialTick, owner.zOld, owner.getZ());
		float yaw = Mth.lerp(partialTick, owner.yRotO, owner.getYRot()) * Mth.DEG_TO_RAD;
		boolean offhand = crossbarId != null && crossbarId.equals(
				MarionetteCrossbarItem.getCrossbarId(owner.getOffhandItem()));
		double side = offhand ? 1.0D : -1.0D;
		if (owner.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT) side *= -1.0D;
		boolean firstPerson = owner == mc.player && mc.options.getCameraType().isFirstPerson();
		return PuppeteerThreadEndpointRules.playerHandEndpoint(new Vec3(x, y, z),
				owner.getViewVector(partialTick), yaw, side, firstPerson);
	}

	private static Vec3 performerAnchor(CircusPerformerEntity performer, float partialTick) {
		// The hand the performer renderer actually drew; the yaw estimate only covers a never-rendered performer.
		Vec3 drawn = PerformerHandAnchors.anchor(performer, partialTick);
		if (drawn != null) {
			return drawn;
		}
		Vec3 eye = new Vec3(Mth.lerp(partialTick, performer.xOld, performer.getX()),
				Mth.lerp(partialTick, performer.yOld, performer.getY()) + performer.getEyeHeight(),
				Mth.lerp(partialTick, performer.zOld, performer.getZ()));
		float bodyYaw = Mth.rotLerp(partialTick, performer.yBodyRotO, performer.yBodyRot) * Mth.DEG_TO_RAD;
		return PuppeteerThreadEndpointRules.performerHandEndpoint(eye, bodyYaw);
	}

	private static Vec3 summonAnchor(LivingEntity summon, float partialTick) {
		double heightScale = summon instanceof SanguineHoundEntity ? SANGUINE_HOUND_ANCHOR_SCALE
				: PuppeteerThreadEndpointRules.SUMMON_HEIGHT_SCALE;
		return PuppeteerThreadEndpointRules.summonEndpoint(
				summon.xOld, summon.yOld, summon.zOld,
				summon.getX(), summon.getY(), summon.getZ(), summon.getBbHeight(), heightScale, partialTick);
	}

	private static Vec3 entityAnchor(LivingEntity entity, float partialTick, double heightScale) {
		return PuppeteerThreadEndpointRules.summonEndpoint(
				entity.xOld, entity.yOld, entity.zOld,
				entity.getX(), entity.getY(), entity.getZ(), entity.getBbHeight(), heightScale, partialTick);
	}

	private static Vec3 threadPoint(Vec3 start, Vec3 delta, float t, float time) {
		double sag = -Math.sin(t * Math.PI) * 0.22;
		double wobbleX = Math.sin(time * 0.16 + t * 13.0) * 0.035;
		double wobbleZ = Math.cos(time * 0.13 + t * 10.0) * 0.035;
		return start.add(delta.x * t + wobbleX, delta.y * t + sag, delta.z * t + wobbleZ);
	}
}
