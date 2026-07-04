package com.vincenthuto.hemomancy.client.render.scar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.QliphothBloomClientData;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.OculifloraReticularisItem;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class OculifloraRevealRenderer {
	public static final int MAX_RENDERED_MARKERS = 16;
	private static final double REVEAL_RADIUS = 48.0D;
	private static final int NECTAR_VERTICAL_RADIUS = 16;
	private static long lastNectarScanTick = Long.MIN_VALUE;
	private static BlockPos lastNectarScanCenter = BlockPos.ZERO;
	private static List<BlockPos> cachedNectarPositions = List.of();

	private OculifloraRevealRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		if (HemoClientConfig.RENDER_OCULIFLORA_REVEAL != null
				&& !HemoClientConfig.RENDER_OCULIFLORA_REVEAL.get()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		ClientLevel level = mc.level;
		if (player == null || level == null || !OculifloraReticularisItem.networkSightActive(player)) {
			return;
		}

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
		PoseStack.Pose pose = poseStack.last();
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		float time = level.getGameTime() + partialTick;

		renderEntityMarkers(pose, consumer, level, player, camera, partialTick, time);
		renderBloomMarkers(pose, consumer, player, camera, time);
		renderNectarMarkers(pose, consumer, level, player, camera, time);

		buffer.endBatch(RenderType.lines());
	}

	private static void renderEntityMarkers(PoseStack.Pose pose, VertexConsumer consumer, ClientLevel level,
			LocalPlayer player, Vec3 camera, float partialTick, float time) {
		int rendered = 0;
		double radiusSq = REVEAL_RADIUS * REVEAL_RADIUS;
		for (Entity entity : level.entitiesForRendering()) {
			if (entity == player || !(entity instanceof LivingEntity living)) {
				continue;
			}
			if (entity.distanceToSqr(player) > radiusSq || !isVisibleNetworkSignal(entity, living)) {
				continue;
			}
			Vec3 center = entity.getPosition(partialTick).add(0.0D, entity.getBbHeight() * 0.55D, 0.0D);
			drawMarker(pose, consumer, center.subtract(camera), 0.55D, time, 178, 48, 232, 190);
			if (++rendered >= MAX_RENDERED_MARKERS) {
				return;
			}
		}
	}

	private static boolean isVisibleNetworkSignal(Entity entity, LivingEntity living) {
		boolean hemoEntity = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace()
				.equals(Hemomancy.MOD_ID);
		boolean bloodActivePlayer = entity instanceof Player player && HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.isActive()).orElse(false);
		return hemoEntity || bloodActivePlayer || hemoEntity && living.hasEffect(MobEffects.INVISIBILITY);
	}

	private static void renderBloomMarkers(PoseStack.Pose pose, VertexConsumer consumer, LocalPlayer player,
			Vec3 camera, float time) {
		QliphothBloomClientData.getActiveBlooms().stream()
				.sorted(Comparator.comparingDouble(bloom -> bloom.getCenter().distSqr(player.blockPosition())))
				.limit(MAX_RENDERED_MARKERS)
				.forEach(bloom -> {
					Vec3 center = Vec3.atCenterOf(bloom.getCenter()).subtract(camera);
					drawColumn(pose, consumer, center, 4.0D, time, 114, 14, 165, 210);
				});
	}

	private static void renderNectarMarkers(PoseStack.Pose pose, VertexConsumer consumer, ClientLevel level,
			LocalPlayer player, Vec3 camera, float time) {
		List<BlockPos> nectarPositions = nectarPositions(level, player);
		for (BlockPos pos : nectarPositions) {
			Vec3 center = Vec3.atCenterOf(pos).subtract(camera);
			drawMarker(pose, consumer, center, 0.35D, time, 64, 212, 196, 175);
		}
	}

	private static List<BlockPos> nectarPositions(ClientLevel level, LocalPlayer player) {
		BlockPos center = player.blockPosition();
		long now = level.getGameTime();
		if (now - lastNectarScanTick < 40 && center.closerThan(lastNectarScanCenter, 6.0D)) {
			return cachedNectarPositions;
		}

		List<BlockPos> found = new ArrayList<>();
		BlockPos min = center.offset(-(int) REVEAL_RADIUS, -NECTAR_VERTICAL_RADIUS, -(int) REVEAL_RADIUS);
		BlockPos max = center.offset((int) REVEAL_RADIUS, NECTAR_VERTICAL_RADIUS, (int) REVEAL_RADIUS);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (pos.distSqr(center) > REVEAL_RADIUS * REVEAL_RADIUS) {
				continue;
			}
			FluidState fluidState = level.getFluidState(pos);
			if (fluidState.is(FluidInit.MORPHIC_NECTAR.get())
					|| fluidState.is(FluidInit.MORPHIC_NECTAR_FLOWING.get())) {
				found.add(pos.immutable());
			}
		}
		found.sort(Comparator.comparingDouble(pos -> pos.distSqr(center)));
		if (found.size() > MAX_RENDERED_MARKERS) {
			found = new ArrayList<>(found.subList(0, MAX_RENDERED_MARKERS));
		}
		lastNectarScanTick = now;
		lastNectarScanCenter = center.immutable();
		cachedNectarPositions = List.copyOf(found);
		return cachedNectarPositions;
	}

	private static void drawColumn(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center, double height, float time,
			int red, int green, int blue, int alpha) {
		double pulse = 0.35D + Math.sin(time * 0.12D) * 0.15D;
		Vec3 bottom = center.add(0.0D, -0.25D, 0.0D);
		Vec3 top = center.add(0.0D, height + pulse, 0.0D);
		line(pose, consumer, bottom, top, red, green, blue, alpha);
		line(pose, consumer, center.add(-0.35D, 0.3D, 0.0D), center.add(0.35D, 0.3D, 0.0D),
				red, green, blue, alpha);
		line(pose, consumer, center.add(0.0D, 0.3D, -0.35D), center.add(0.0D, 0.3D, 0.35D),
				red, green, blue, alpha);
	}

	private static void drawMarker(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center, double radius, float time,
			int red, int green, int blue, int alpha) {
		double pulse = radius + Math.sin(time * 0.18D) * 0.08D;
		Vec3 up = center.add(0.0D, pulse, 0.0D);
		Vec3 down = center.add(0.0D, -pulse, 0.0D);
		Vec3 east = center.add(pulse, 0.0D, 0.0D);
		Vec3 west = center.add(-pulse, 0.0D, 0.0D);
		Vec3 south = center.add(0.0D, 0.0D, pulse);
		Vec3 north = center.add(0.0D, 0.0D, -pulse);
		line(pose, consumer, up, east, red, green, blue, alpha);
		line(pose, consumer, up, west, red, green, blue, alpha);
		line(pose, consumer, up, south, red, green, blue, alpha);
		line(pose, consumer, up, north, red, green, blue, alpha);
		line(pose, consumer, down, east, red, green, blue, alpha);
		line(pose, consumer, down, west, red, green, blue, alpha);
		line(pose, consumer, down, south, red, green, blue, alpha);
		line(pose, consumer, down, north, red, green, blue, alpha);
	}

	private static void line(PoseStack.Pose pose, VertexConsumer consumer, Vec3 from, Vec3 to,
			int red, int green, int blue, int alpha) {
		Vec3 normal = to.subtract(from);
		if (normal.lengthSqr() < 1.0E-6D) {
			normal = new Vec3(0.0D, 1.0D, 0.0D);
		} else {
			normal = normal.normalize();
		}
		consumer.addVertex(pose.pose(), (float) from.x, (float) from.y, (float) from.z)
				.setColor(red, green, blue, alpha)
				.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
		consumer.addVertex(pose.pose(), (float) to.x, (float) to.y, (float) to.z)
				.setColor(red, green, blue, alpha)
				.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
	}
}
