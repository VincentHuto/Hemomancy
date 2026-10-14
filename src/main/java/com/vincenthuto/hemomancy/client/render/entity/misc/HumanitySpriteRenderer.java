package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteOfferingMemory;
import com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteVisualRules;
import com.vincenthuto.hemomancy.client.render.world.rite.DaemonHaze;
import com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeQueue;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.entity.utility.DaemonFlight;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Draws the Cardinal Rite daemon as a figure of red smoke with glowing eyes: a box ray-marched by the
 * {@code cardinal_daemon_haze} shader, posed and streamed by how it moves, queued through {@link DaemonHazeQueue} so
 * it draws after the other rite visuals, and any offering it is still swallowing. The maths lives in
 * {@link DaemonHaze} and {@link DaemonFlight}; this class only reads the entity and its rite.
 */
public final class HumanitySpriteRenderer extends EntityRenderer<HumanitySpriteEntity> {
	private static final ResourceLocation BLANK = Hemomancy.rloc("textures/entity/blank.png");
	private static final int OFFERING_TINT = 0xB01420;

	public HumanitySpriteRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(HumanitySpriteEntity entity, Frustum frustum, double camX, double camY, double camZ) {
		if (!entity.shouldRender(camX, camY, camZ)) return false;
		// Lying along its path with its tail streaming out, the smoke reaches well past the body's box.
		double reach = 1.0D + entity.getSpriteScale() * 5.5D;
		return frustum.isVisible(entity.getBoundingBoxForCulling().inflate(reach));
	}

	@Override
	public void render(HumanitySpriteEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || entity.isInvisible()) return;
		float time = (float) (entity.level().getGameTime() + partialTick);
		Vec3 position = entity.getPosition(partialTick);
		double scale = entity.renderScale(partialTick);
		float emergence = entity.emergenceProgress(partialTick);
		// The head turns ahead of the body; sent within half a turn of it so the shader can twist between them.
		float bodyDegrees = entity.renderBodyYaw(partialTick);
		double yaw = Math.toRadians(bodyDegrees);
		double headYaw = yaw + Math.toRadians(Mth.wrapDegrees(entity.renderHeadYaw(partialTick) - bodyDegrees));

		ActiveRiteClientData.RiteEntry rite = riteFor(entity.getRiteOwner());
		double firmness = 1.0D;
		float cancellation = 0.0F;
		float flare = 0.0F;
		float swallow = 0.0F;
		int tint = OFFERING_TINT;
		List<CardinalRiteEventClientState.RiteEvent> absorbed = List.of();
		if (rite != null) {
			BlockPos center = rite.getCenter();
			cancellation = Mth.clamp(rite.cancellationRenderTicks(partialTick)
					/ CardinalRiteCancellationRules.DAEMON_ABSORPTION_TICKS, 0.0F, 1.0F);
			firmness = CardinalRiteVisualRules.firmness(
					CardinalRiteEventClientState.latestAge(center, CardinalRiteEventKind.SEALED, partialTick),
					isPastSealing(rite.getPhase()));
			RiteWorldState.World world = RiteWorldState.at(center);
			if (world != null) flare = world.level(partialTick);
			absorbed = CardinalRiteOfferingMemory.absorbed(center);
			CardinalRiteEventClientState.RiteEvent newest = newestArrival(absorbed, partialTick);
			if (newest != null) {
				swallow = DaemonHaze.swallow(sinceArrival(newest, partialTick));
				tint = CardinalRiteOfferingMemory.colour(newest.item());
			}
		}
		float strength = DaemonHaze.strength(firmness, cancellation);
		float eyes = DaemonHaze.eyesOpen(emergence);
		// Posed by how it moves: upright while it hovers, swinging over to lie along its path, head first, as it
		// picks up speed, its tail streaming out behind.
		DaemonFlight flight = entity.flight();
		double[] velocity = flight.velocity();
		float[] trail = DaemonHaze.trail(velocity[0], velocity[1], velocity[2]);
		double[] crown = flight.crown(partialTick);
		float[] tilt = DaemonHaze.tilt(crown[0], crown[1], crown[2], yaw);
		float stream = (float) flight.stream(partialTick);
		float flow = (float) flight.flow(partialTick);
		float seed = Math.floorMod(entity.getId(), 113);
		double[] box = DaemonHaze.box(scale, tilt[3], stream);
		double height = CardinalRiteVisualRules.DAEMON_HEIGHT * scale;
		// Hovering, it bobs gently on the air.
		double bob = Math.sin(time * 0.06F + seed) * 0.03D * height * (1.0D - stream);

		// Queued, and drawn after every rite visual and the clouds: the smoke writes no depth, so anything drawn
		// after it would paint over it.
		DaemonHazeQueue.add(new DaemonHazeQueue.Draw(position.x, position.y + bob, position.z, (float) height,
				(float) yaw, (float) headYaw, tilt[0], tilt[2], tilt[3], (float) box[0], (float) box[1],
				time, seed, emergence, strength, swallow, eyes, tint, flare, trail[0], trail[1], trail[2],
				cancellation, flow, stream));

		if (rite != null) {
			// While the daemon is abroad, black and red haze curls off the crook of its planted staff.
			BlockPos focus = rite.getCenter();
			double[] staff = DaemonHaze.staffHazeCentre(focus.getX(), focus.getY(), focus.getZ());
			DaemonHazeQueue.addStaff(new DaemonHazeQueue.StaffDraw(staff[0], staff[1], staff[2], time, seed,
					emergence, strength));
		}

		renderSwallowedOfferings(entity, absorbed, position, scale, partialTick, time, poseStack, buffer);
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	/** Each offering still being swallowed drifts into the haze's chest, shrinking and dimming away. */
	private void renderSwallowedOfferings(HumanitySpriteEntity entity,
			List<CardinalRiteEventClientState.RiteEvent> absorbed, Vec3 position, double scale, float partialTick,
			float time, PoseStack poseStack, MultiBufferSource buffer) {
		Minecraft mc = Minecraft.getInstance();
		for (int slot = 0; slot < absorbed.size(); slot++) {
			CardinalRiteEventClientState.RiteEvent event = absorbed.get(slot);
			float since = sinceArrival(event, partialTick);
			if (since < 0.0F || since >= DaemonHaze.SWALLOW_TICKS || event.item().isEmpty()) continue;
			Vec3 from = event.points().size() > 1 ? event.points().get(1).subtract(position) : Vec3.ZERO;
			double[] sink = DaemonHaze.sink(from.x, from.y, from.z, scale, since);
			if (sink[3] <= 0.01D) continue;
			poseStack.pushPose();
			poseStack.translate(sink[0], sink[1], sink[2]);
			poseStack.mulPose(Axis.YP.rotationDegrees(time * 9.0F + slot * 47.0F));
			poseStack.scale((float) sink[3], (float) sink[3], (float) sink[3]);
			mc.getItemRenderer().renderStatic(event.item(), ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId() + slot);
			poseStack.popPose();
		}
	}

	private static CardinalRiteEventClientState.RiteEvent newestArrival(
			List<CardinalRiteEventClientState.RiteEvent> absorbed, float partialTick) {
		CardinalRiteEventClientState.RiteEvent newest = null;
		float youngest = Float.MAX_VALUE;
		for (CardinalRiteEventClientState.RiteEvent event : absorbed) {
			float since = sinceArrival(event, partialTick);
			if (since >= 0.0F && since < youngest) {
				youngest = since;
				newest = event;
			}
		}
		return newest;
	}

	private static float sinceArrival(CardinalRiteEventClientState.RiteEvent event, float partialTick) {
		return event.age(partialTick) - CardinalRiteVisualRules.OFFERING_ARRIVE_TICKS;
	}

	private static boolean isPastSealing(String phase) {
		if (phase == null) return false;
		return switch (phase) {
			case "ORDEAL", "PUPPET_TRIAL", "STILL_INTERVAL", "OFFERING_PROCESSION", "CULMINATION", "COMPLETE" -> true;
			default -> false;
		};
	}

	private static ActiveRiteClientData.RiteEntry riteFor(UUID owner) {
		if (owner == null) return null;
		for (ActiveRiteClientData.RiteEntry rite : ActiveRiteClientData.getActiveRites()) {
			if (owner.equals(rite.getOwner())) return rite;
		}
		CardinalRiteEventClientState.Ghost latest = null;
		for (CardinalRiteEventClientState.Ghost ghost : CardinalRiteEventClientState.ghosts()) {
			if (owner.equals(ghost.entry().getOwner())) latest = ghost;
		}
		return latest == null ? null : latest.entry();
	}

	@Override
	public ResourceLocation getTextureLocation(HumanitySpriteEntity entity) {
		return BLANK;
	}
}
