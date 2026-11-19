package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.SomaticLoomEffectRules;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramDissolve;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Cortical loom's weave raises a dome of its weaver's own Chamber of Will sky. Within the dome's radius the world
 * beyond is replaced by that sky, while everything inside it (the loom, the floor, the weaver, the threads, the echo)
 * stays real, seen from inside or out.
 * <p>
 * The sky is a sealed {@link ChamberDomePortal}: whatever lies beyond the dome stays hidden while entities and
 * translucent blocks inside it draw normally over the sky. A faint membrane and a ground ring mark the boundary.
 */
public final class SomaticLoomDomeRenderer {
	private static final int MAX_DOMES = 2;
	private static final double RANGE = 64.0D;
	private static final Map<BlockPos, Tracked> TRACKED = new HashMap<>();

	private record Tracked(SomaticLoomBlockEntity loom, long seen) {}

	private SomaticLoomDomeRenderer() {
	}

	/** Called by the loom's renderer each frame it draws: a Cortical weave in view (or one dissolving) may raise a dome. */
	public static void track(SomaticLoomBlockEntity loom, boolean dissolving) {
		if (loom.getLevel() == null || loom.upgradeTier() < 2 || !loom.isWeaving() && !dissolving) return;
		TRACKED.put(loom.getBlockPos().immutable(), new Tracked(loom, loom.getLevel().getGameTime()));
	}

	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS || TRACKED.isEmpty()) return;
		Minecraft minecraft = Minecraft.getInstance();
		var level = minecraft.level;
		if (level == null) {
			TRACKED.clear();
			return;
		}
		long now = level.getGameTime();
		Vec3 cam = event.getCamera().getPosition();
		TRACKED.values().removeIf(t -> t.loom().isRemoved() || t.loom().getLevel() != level || now - t.seen() > 5
				|| !t.loom().isWeaving() && EngramDissolve.progress(t.loom().getBlockPos(), level, 0F) < 0F
				|| t.loom().upgradeTier() < 2);
		List<Tracked> near = new ArrayList<>();
		for (Tracked t : TRACKED.values())
			if (Vec3.atCenterOf(t.loom().getBlockPos()).distanceToSqr(cam) < RANGE * RANGE) near.add(t);
		if (near.isEmpty()) return;
		near.sort(Comparator.comparingDouble(t -> Vec3.atCenterOf(t.loom().getBlockPos()).distanceToSqr(cam)));
		float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		for (int i = 0; i < Math.min(MAX_DOMES, near.size()); i++) renderDome(event, near.get(i).loom(), cam, partial);
	}

	private static void renderDome(RenderLevelStageEvent event, SomaticLoomBlockEntity loom, Vec3 cam, float partial) {
		float climax = loom.isInClimax()
				? Mth.clamp((loom.getClimaxTicks() + partial) / SomaticWeaveRules.CLIMAX_TICKS, 0, 1) : 0;
		// A broken weave's dome sinks away from where it stood, under the sky it was raised with.
		float dissolve = loom.isWeaving() ? -1F : EngramDissolve.progress(loom.getBlockPos(), loom.getLevel(), partial);
		EngramDissolve.Snapshot snapshot = dissolve >= 0F ? EngramDissolve.snapshot(loom.getBlockPos()) : null;
		float radius = snapshot != null ? SomaticLoomEffectRules.dissolvingDomeRadius(snapshot.domeRadius(), dissolve)
				: SomaticLoomEffectRules.domeRadius(loom.upgradeTier(), loom.isWeaving(),
						loom.getWeaveTicks() + (loom.isPaused() ? 0 : partial), loom.isInClimax(), climax);
		if (radius < 0.25F) return;
		ResourceLocation themeId = snapshot != null && snapshot.theme() != null ? snapshot.theme()
				: loom.getWeaverTheme() == null ? ChamberSkyThemeRegistry.DEFAULT.id() : loom.getWeaverTheme();
		ChamberSkyTheme theme = ChamberSkyThemeRegistry.byId(themeId);
		Vec3 centre = Vec3.atBottomCenterOf(loom.getBlockPos()).add(0, 0.5D, 0);
		if (!ChamberDomePortal.render(event, centre, radius, theme, partial)) return;

		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity();
		RenderSystem.applyModelViewMatrix();
		try {
			PoseStack world = new PoseStack();
			world.mulPose(event.getModelViewMatrix());
			float time = AnimationTime.gameTime(loom.getLevel(), partial) * theme.motionMultiplier();
			// Where the dome meets the ground (the loom's floor), under the seam's tissue.
			ChamberDomePortal.drawMembrane(world, centre, cam, radius, theme, time, loom.isPaused() ? 0.45F : 1F, -0.49F);
			SomaticLoomSeamRenderer.render(world, loom, centre, cam, radius, theme, partial);
		} finally {
			modelViewStack.popMatrix();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.depthMask(true);
			RenderSystem.enableCull();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableBlend();
		}
	}
}
