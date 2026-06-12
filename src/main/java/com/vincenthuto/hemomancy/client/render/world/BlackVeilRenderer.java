package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class BlackVeilRenderer {
	private static final List<Entry> VEILS = new ArrayList<>();

	private BlackVeilRenderer() {
	}

	public static void addVeil(Vec3 center, float radius, int durationTicks, int seed) {
		Minecraft mc = Minecraft.getInstance();
		long now = mc.level == null ? 0 : mc.level.getGameTime();
		VEILS.add(new Entry(center, radius, now + durationTicks, seed));
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || VEILS.isEmpty()) return;

		long now = mc.level.getGameTime();
		Iterator<Entry> iterator = VEILS.iterator();
		while (iterator.hasNext()) {
			if (now > iterator.next().expiryTick()) iterator.remove();
		}
		if (VEILS.isEmpty()) return;

		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
		float time = now + partialTick;
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

		for (Entry veil : VEILS) {
			BlockPos center = BlockPos.containing(veil.center);
			float seed = FaneBoundaryRenderer.revealedFaneStyleSeed(center, veil.radius);
			FaneBoundaryRenderer.drawRevealedFaneStyleDome(poseStack, buffer, cam, center, veil.radius, time, seed);
		}
	}

	private record Entry(Vec3 center, float radius, long expiryTick, int seed) {
	}
}
