package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.CardinalRiteFogLightning;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

/**
 * The sky wound: a slow vortex of cloud gathering high over a rite once it is halfway through, darkening and veined
 * as the rite deepens, opening into a red eye at culmination. Storm rites throw lightning from it, and a collapse
 * brings it down in a last few strikes. It is drawn to {@link RiteWorldMath#WOUND_DRAW_DISTANCE} as a landmark.
 */
public final class RiteSkyWound {
	private static final Map<BlockPos, Long> LAST_STRIKE = new HashMap<>();
	private static final RandomSource RANDOM = RandomSource.create();

	private RiteSkyWound() {
	}

	record Shape(float presence, float radius, float grow, float open, float flash, float strain) {
	}

	static Shape shape(RiteWorldState.World world, float partialTick) {
		float level = world.level(partialTick);
		float ending = RiteWorldMath.woundEnding(world.ending(), world.endingAge());
		boolean closes = world.ending() == RiteWorldMath.Ending.COMPLETED
				|| world.ending() == RiteWorldMath.Ending.COLLAPSED;
		float presence = RiteWorldMath.woundPresence(level, world.total())
				* (closes ? Math.min(1.0F, Math.max(0.0F, ending) * 4.0F) : ending);
		if (presence <= 0.003F) return null;
		float radius = RiteWorldMath.woundRadius(world.footprint(), level) * (closes ? Math.max(0.0F, ending) : 1.0F);
		float grow = RiteWorldMath.ramp(level, RiteWorldMath.WOUND_FROM);
		float open = world.culminating() ? HemoEasing.smoothstep(0.9F, 1.0F, level) : 0.0F;
		float flash = RiteWorldMath.woundFlash(world.ending(), world.endingAge(), world.surge());
		float strain = world.strain() == CardinalRiteInstability.STABLE ? 0.0F
				: world.strain() == CardinalRiteInstability.STRAINED ? 0.5F : 1.0F;
		return new Shape(presence, radius, grow, open, flash, strain);
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		float time = AnimationTime.gameTime(mc.level, partialTick);
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		for (RiteWorldState.World world : RiteWorldState.worlds()) {
			Shape shape = shape(world, partialTick);
			if (shape == null) continue;
			BlockPos center = world.center();
			double x = center.getX() + 0.5D;
			double z = center.getZ() + 0.5D;
			double y = RiteWorldMath.woundAltitude(center.getY(), world.footprint(), mc.level.getMaxBuildHeight());
			double distance = Math.sqrt((x - camera.x) * (x - camera.x) + (z - camera.z) * (z - camera.z));
			if (distance > RiteWorldMath.WOUND_DRAW_DISTANCE) continue;
			float fade = (float) Math.min(1.0D, (RiteWorldMath.WOUND_DRAW_DISTANCE - distance) / 64.0D);
			var palette = world.floor().palette();
			RenderType type = HemoRenderTypes.cardinalRiteSkyWound(time, seed(center), shape.grow(), shape.open(),
					shape.flash(), shape.strain(), palette.base(), palette.vein(), palette.glow());
			VertexConsumer consumer = buffers.getBuffer(type);
			poseStack.pushPose();
			poseStack.translate(x - camera.x, y - camera.y, z - camera.z);
			disc(consumer, poseStack.last().pose(), shape.radius(), Math.round(255.0F * shape.presence() * fade));
			poseStack.popPose();
			buffers.endBatch(type);
		}
	}

	private static void disc(VertexConsumer consumer, Matrix4f matrix, float radius, int alpha) {
		consumer.addVertex(matrix, -radius, 0.0F, -radius).setUv(-1.0F, -1.0F).setColor(255, 255, 255, alpha);
		consumer.addVertex(matrix, -radius, 0.0F, radius).setUv(-1.0F, 1.0F).setColor(255, 255, 255, alpha);
		consumer.addVertex(matrix, radius, 0.0F, radius).setUv(1.0F, 1.0F).setColor(255, 255, 255, alpha);
		consumer.addVertex(matrix, radius, 0.0F, -radius).setUv(1.0F, -1.0F).setColor(255, 255, 255, alpha);
	}

	/** Ambient strikes from storm rites' wounds, and a collapse's last strikes. Called once per client tick. */
	public static void tick(Minecraft mc) {
		if (mc.level == null) {
			LAST_STRIKE.clear();
			return;
		}
		long now = mc.level.getGameTime();
		for (RiteWorldState.World world : RiteWorldState.worlds()) {
			Shape shape = shape(world, 1.0F);
			if (shape == null) continue;
			boolean strike = false;
			if (world.ending() == RiteWorldMath.Ending.COLLAPSED) {
				for (int at : RiteWorldMath.COLLAPSE_STRIKE_TICKS) {
					if (Math.round(world.endingAge()) == at) strike = true;
				}
			} else if (!world.ended() && world.lightning()) {
				Long last = LAST_STRIKE.get(world.center());
				if (last == null) {
					LAST_STRIKE.put(world.center(), now);
				} else if (now - last >= RiteWorldMath.woundStrikeInterval(world.level(1.0F))) {
					strike = true;
				}
			}
			if (!strike) continue;
			LAST_STRIKE.put(world.center(), now);
			BlockPos center = world.center();
			double y = RiteWorldMath.woundAltitude(center.getY(), world.footprint(), mc.level.getMaxBuildHeight());
			double fromAngle = RANDOM.nextDouble() * Math.PI * 2.0D;
			double from = RANDOM.nextDouble() * shape.radius() * 0.5D;
			double toAngle = RANDOM.nextDouble() * Math.PI * 2.0D;
			double to = RANDOM.nextDouble() * world.footprint() * 0.8D;
			Vec3 start = new Vec3(center.getX() + 0.5D + Math.cos(fromAngle) * from, y - 1.0D,
					center.getZ() + 0.5D + Math.sin(fromAngle) * from);
			Vec3 end = new Vec3(center.getX() + 0.5D + Math.cos(toAngle) * to, center.getY() + 1.1D,
					center.getZ() + 0.5D + Math.sin(toAngle) * to);
			CardinalRiteFogLightning.strike(start, end, RANDOM.nextLong(), 0.0F);
		}
		LAST_STRIKE.keySet().removeIf(center -> RiteWorldState.at(center) == null);
	}

	public static void clear() {
		LAST_STRIKE.clear();
	}

	private static float seed(BlockPos center) {
		return center.getX() * 0.173F + center.getY() * 0.051F + center.getZ() * 0.211F;
	}
}
