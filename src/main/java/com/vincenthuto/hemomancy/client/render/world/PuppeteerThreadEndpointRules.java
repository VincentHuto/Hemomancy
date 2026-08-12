package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class PuppeteerThreadEndpointRules {
	private static final double SUMMON_HEIGHT_SCALE = 0.45D;

	private PuppeteerThreadEndpointRules() {
	}

	public static Vec3 summonEndpoint(double oldX, double oldY, double oldZ,
			double x, double y, double z, double boundingBoxHeight, float partialTick) {
		return new Vec3(
				Mth.lerp(partialTick, oldX, x),
				Mth.lerp(partialTick, oldY, y) + boundingBoxHeight * SUMMON_HEIGHT_SCALE,
				Mth.lerp(partialTick, oldZ, z));
	}
}
