package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import java.util.List;

/**
 * The air around a deepening rite: the fog leans toward the floor's blood colour and closes in a little with each
 * step, and further under strain. Only the strongest rite around the camera counts, so neighbouring rites never
 * stack, and the fog is never pulled inside {@link RiteWorldMath#MIN_FAR_PLANE}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RiteAirLayer {
	private RiteAirLayer() {
	}

	record Choice(RiteWorldState.World world, float strength, float weight) {
	}

	static Choice strongest(List<RiteWorldState.World> worlds, Vec3 camera, float partialTick) {
		Choice best = null;
		for (RiteWorldState.World world : worlds) {
			double dx = world.center().getX() + 0.5D - camera.x;
			double dz = world.center().getZ() + 0.5D - camera.z;
			float weight = RiteWorldMath.viewerWeight(Math.sqrt(dx * dx + dz * dz), world.footprint());
			float strength = RiteWorldMath.fogMix(world.fogProfile()) * world.level(partialTick) * weight
					* RiteWorldMath.airEnding(world.ending(), world.endingAge());
			if (strength <= 0.0F) continue;
			if (best == null || strength > best.strength()) best = new Choice(world, strength, weight);
		}
		return best;
	}

	@SubscribeEvent
	public static void colour(ViewportEvent.ComputeFogColor event) {
		Choice choice = strongest(RiteWorldState.worlds(), event.getCamera().getPosition(),
				(float) event.getPartialTick());
		if (choice == null) return;
		int vein = choice.world().floor().palette().vein();
		float red = ((vein >> 16) & 255) / 255.0F * 0.35F;
		float green = ((vein >> 8) & 255) / 255.0F * 0.35F;
		float blue = (vein & 255) / 255.0F * 0.35F;
		float mix = Math.min(1.0F, choice.strength());
		event.setRed(event.getRed() + (red - event.getRed()) * mix);
		event.setGreen(event.getGreen() + (green - event.getGreen()) * mix);
		event.setBlue(event.getBlue() + (blue - event.getBlue()) * mix);
	}

	@SubscribeEvent
	public static void distance(ViewportEvent.RenderFog event) {
		if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;
		float partialTick = (float) event.getPartialTick();
		Choice choice = strongest(RiteWorldState.worlds(), event.getCamera().getPosition(), partialTick);
		if (choice == null) return;
		RiteWorldState.World world = choice.world();
		float level = world.level(partialTick) * RiteWorldMath.airEnding(world.ending(), world.endingAge());
		float scale = RiteWorldMath.farPlaneScale(level, world.fogProfile(), world.strain())
				* RiteWorldMath.collapseSurge(world.ending(), world.endingAge());
		float far = event.getFarPlaneDistance();
		float pulled = RiteWorldMath.applyFarPlane(far, scale, choice.weight());
		if (pulled >= far) return;
		event.setNearPlaneDistance(event.getNearPlaneDistance() * pulled / far);
		event.setFarPlaneDistance(pulled);
		event.setCanceled(true);
	}
}
