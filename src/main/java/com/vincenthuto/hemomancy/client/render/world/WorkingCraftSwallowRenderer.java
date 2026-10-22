package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Draws a Working floor swallowing its craft: the floor's membrane climbs the
 * built structure as it is fed, pulses once when the rite seals, darkens to
 * clotted blood once the daemon dives inside it, and on completion the floor
 * yanks the structure under it, landing with a thud. A craft that ends any other
 * way drains back down and leaves the structure standing.
 */
public final class WorkingCraftSwallowRenderer {
	/** Working floor sinew red; the shader brightens the meniscus toward the pale core. */
	private static final int SHELL_TINT = CardinalRiteFloorVisualRules.palette(
			CardinalRiteFloorVisualRules.STYLE_WORKING).vein() & 0xFFFFFF;
	private static final Map<BlockPos, float[]> DISPLAYED_FILL = new HashMap<>();
	/** Per live craft in its culmination: the sync it was read from, its phase ticks then, and the client tick. */
	private static final Map<BlockPos, CulminationClock> CULMINATION = new HashMap<>();
	/** Completed crafts whose structure has already landed, keyed by centre and ghost start. */
	private static final Set<Long> THUDDED = new HashSet<>();
	private static final net.minecraft.util.RandomSource RANDOM = net.minecraft.util.RandomSource.create();
	private static ClientLevel world;

	/** One subject to draw this frame, live or ending. */
	record View(BlockPos center, List<BlockPos> positions, List<BlockState> states, int minX, int minZ, int maxX,
			int maxZ, int bottomY, int height, float fill, double sink, float alpha, float sealPulse, float possession) {
	}

	private record CulminationClock(ActiveRiteData.RiteEntry entry, int phaseTicks, long clientTick) {
		float ticks(long now, float partialTick) {
			return phaseTicks + (now - clientTick) + partialTick;
		}
	}

	private WorkingCraftSwallowRenderer() {
	}

	/** Eases each live craft's displayed fill toward its last synced fill. */
	public static void tick() {
		ClientLevel current = Minecraft.getInstance().level;
		if (current != world) {
			clear();
			world = current;
		}
		if (current == null) return;
		long now = current.getGameTime();
		Set<BlockPos> live = new HashSet<>();
		for (ActiveRiteData.RiteEntry entry : ActiveRiteClientData.getActiveRites()) {
			if (!entry.hasSubject()) continue;
			live.add(entry.getCenter());
			float[] fill = DISPLAYED_FILL.computeIfAbsent(entry.getCenter(), center -> new float[2]);
			fill[0] = fill[1];
			fill[1] = WorkingCraftSwallowRules.smoothFill(fill[1], entry.getSubjectFill());
			if (CULMINATION_PHASE.equals(entry.getPhase())) {
				CulminationClock clock = CULMINATION.get(entry.getCenter());
				if (clock == null || clock.entry() != entry) {
					CULMINATION.put(entry.getCenter(), new CulminationClock(entry, entry.getPhaseTicks(), now));
				}
				float possession = WorkingCraftSwallowRules.possession(CULMINATION.get(entry.getCenter()).ticks(now, 0.0F));
				if (possession > 0.0F) bleed(current, entry, possession);
			} else {
				CULMINATION.remove(entry.getCenter());
			}
		}
		DISPLAYED_FILL.keySet().retainAll(live);
		CULMINATION.keySet().retainAll(live);
		Set<Long> ghosts = new HashSet<>();
		for (CardinalRiteEventClientState.Ghost ghost : CardinalRiteEventClientState.ghosts()) {
			if (ghost.kind() != CardinalRiteEventKind.COMPLETED || !ghost.entry().hasSubject()) continue;
			long key = ghost.center().asLong() * 31L + ghost.startTick();
			ghosts.add(key);
			if (ghost.age(0.0F) >= WorkingCraftSwallowRules.THUD_TICK && THUDDED.add(key)) thud(current, ghost.entry());
		}
		THUDDED.retainAll(ghosts);
	}

	public static void clear() {
		DISPLAYED_FILL.clear();
		CULMINATION.clear();
		THUDDED.clear();
	}

	private static final String CULMINATION_PHASE = com.vincenthuto.hemomancy.common.rite.CardinalRitePhase.CULMINATION.name();
	private static final com.vincenthuto.hutoslib.client.particle.util.ParticleColor DARK_BLOOD =
			new com.vincenthuto.hutoslib.client.particle.util.ParticleColor(70, 2, 10);
	private static final com.vincenthuto.hutoslib.client.particle.util.ParticleColor BLOOD =
			new com.vincenthuto.hutoslib.client.particle.util.ParticleColor(150, 6, 16);

	/** The possessed structure sweats dark blood and smoulders. */
	private static void bleed(ClientLevel level, ActiveRiteData.RiteEntry entry, float possession) {
		List<BlockPos> positions = entry.getSubjectPositions();
		if (positions.isEmpty() || RANDOM.nextFloat() > 0.35F + 0.5F * possession) return;
		BlockPos pos = positions.get(RANDOM.nextInt(positions.size()));
		double x = pos.getX() + RANDOM.nextDouble();
		double z = pos.getZ() + RANDOM.nextDouble();
		level.addParticle(com.vincenthuto.hemomancy.common.particle.HemoParticleData.bloodCell(DARK_BLOOD),
				x, pos.getY() + RANDOM.nextDouble(), z, 0.0D, -0.03D, 0.0D);
		if (RANDOM.nextFloat() < possession * 0.5F) {
			level.addParticle(com.vincenthuto.hemomancy.common.particle.HemoParticleData.darkGlow(DARK_BLOOD),
					x, pos.getY() + 1.02D, z, 0.0D, 0.02D, 0.0D);
		}
	}

	/** The structure hits the bottom of the floor: a heavy thud, a splash of blood and a shudder underfoot. */
	private static void thud(ClientLevel level, ActiveRiteData.RiteEntry entry) {
		View view = view(entry, 1.0F, 0.0D, 1.0F, 0.0F, 1.0F);
		if (view == null) return;
		double cx = (view.minX() + view.maxX() + 1) / 2.0D;
		double cz = (view.minZ() + view.maxZ() + 1) / 2.0D;
		double y = view.bottomY() + 0.05D;
		level.playLocalSound(cx, y, cz, com.vincenthuto.hemomancy.common.init.SoundInit.RITE_SUBJECT_THUD.get(),
				net.minecraft.sounds.SoundSource.BLOCKS, 1.4F, 0.9F + RANDOM.nextFloat() * 0.1F, false);
		double reach = Math.max(view.maxX() - view.minX(), view.maxZ() - view.minZ()) / 2.0D + 0.8D;
		for (int i = 0; i < 28; i++) {
			double angle = i / 28.0D * Math.PI * 2.0D;
			level.addParticle(com.vincenthuto.hemomancy.common.particle.HemoParticleData.bloodCell(i % 3 == 0 ? DARK_BLOOD : BLOOD),
					cx + Math.cos(angle) * reach, y, cz + Math.sin(angle) * reach,
					Math.cos(angle) * 0.12D, 0.08D + RANDOM.nextDouble() * 0.1D, Math.sin(angle) * 0.12D);
		}
		level.addParticle(com.vincenthuto.hemomancy.common.particle.HemoParticleData.hitGlow(BLOOD), cx, y + 0.2D, cz,
				0.0D, 0.0D, 0.0D);
		var player = Minecraft.getInstance().player;
		if (player != null && player.distanceToSqr(cx, y, cz) < 16.0D * 16.0D) {
			com.vincenthuto.hemomancy.client.event.CardinalRiteImpactClientEvents.start(8, RANDOM.nextInt());
		}
	}

	/**
	 * Where a Blood Projection from {@code source} aimed at {@code hit} lands: on the climbing fill line when it
	 * strikes a feeding craft's structure, otherwise where it was aimed.
	 */
	public static Vec3 projectionLanding(Vec3 source, net.minecraft.world.phys.BlockHitResult hit) {
		for (ActiveRiteData.RiteEntry entry : ActiveRiteClientData.getActiveRites()) {
			if (!entry.hasSubject() || entry.getSubjectFill() >= 1.0F
					|| !entry.getSubjectPositions().contains(hit.getBlockPos())) continue;
			float[] fill = DISPLAYED_FILL.get(entry.getCenter());
			View view = view(entry, fill == null ? entry.getSubjectFill() : fill[1], 0.0D, 1.0F, 0.0F, 0.0F);
			if (view == null) break;
			return WorkingCraftSwallowRules.projectionLanding(source, hit.getLocation(),
					hit.getDirection().getAxis().isHorizontal(), view.minX(), view.minZ(), view.maxX(), view.maxZ(),
					view.bottomY(), view.height(), view.fill());
		}
		return hit.getLocation();
	}

	static List<View> views(float partialTick) {
		List<View> views = new ArrayList<>();
		if (Minecraft.getInstance().level == null) return views;
		for (ActiveRiteData.RiteEntry entry : ActiveRiteClientData.getActiveRites()) {
			if (!entry.hasSubject()) continue;
			float[] fill = DISPLAYED_FILL.get(entry.getCenter());
			float shown = fill == null ? entry.getSubjectFill() : fill[0] + (fill[1] - fill[0]) * partialTick;
			float pulse = WorkingCraftSwallowRules.sealPulse(CardinalRiteEventClientState.latestAge(
					entry.getCenter(), CardinalRiteEventKind.SEALED, partialTick));
			CulminationClock clock = CULMINATION.get(entry.getCenter());
			float possession = clock == null ? 0.0F : WorkingCraftSwallowRules.possession(
					clock.ticks(Minecraft.getInstance().level.getGameTime(), partialTick));
			View view = view(entry, shown, 0.0D, 1.0F, pulse, possession);
			if (view != null) views.add(view);
		}
		for (CardinalRiteEventClientState.Ghost ghost : CardinalRiteEventClientState.ghosts()) {
			ActiveRiteData.RiteEntry entry = ghost.entry();
			if (!entry.hasSubject()) continue;
			float age = ghost.age(partialTick);
			View view;
			if (ghost.kind() == CardinalRiteEventKind.COMPLETED) {
				if (age > WorkingCraftSwallowRules.THUD_TICK + 8) continue;
				// Still possessed while it is dragged under; the membrane closes over it after the thud.
				float fade = 1.0F - HemoEasing.clamp01((age - WorkingCraftSwallowRules.THUD_TICK) / 8.0F);
				view = view(entry, 1.0F, -1.0D, fade, 0.0F, 1.0F);
				if (view != null) view = withSink(view, WorkingCraftSwallowRules.sinkOffset(age, view.height()));
			} else {
				if (age > WorkingCraftSwallowRules.DRAIN_TICKS) continue;
				view = view(entry, WorkingCraftSwallowRules.drainFill(entry.getSubjectFill(), age), 0.0D, 1.0F, 0.0F, 0.0F);
			}
			if (view != null) views.add(view);
		}
		return views;
	}

	private static View view(ActiveRiteData.RiteEntry entry, float fill, double sink, float alpha, float pulse,
			float possession) {
		List<BlockPos> positions = entry.getSubjectPositions();
		int[] ids = entry.getSubjectStateIds();
		if (positions.isEmpty() || ids.length != positions.size()) return null;
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		List<BlockState> states = new ArrayList<>(ids.length);
		for (int i = 0; i < positions.size(); i++) {
			BlockPos pos = positions.get(i);
			minX = Math.min(minX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxX = Math.max(maxX, pos.getX());
			maxY = Math.max(maxY, pos.getY());
			maxZ = Math.max(maxZ, pos.getZ());
			states.add(Block.stateById(ids[i]));
		}
		return new View(entry.getCenter(), positions, states, minX, minZ, maxX, maxZ, minY, maxY - minY + 1,
				fill, Math.max(0.0D, sink), alpha, pulse, possession);
	}

	private static View withSink(View view, double sink) {
		return new View(view.center(), view.positions(), view.states(), view.minX(), view.minZ(), view.maxX(),
				view.maxZ(), view.bottomY(), view.height(), view.fill(), sink, view.alpha(), view.sealPulse(),
				view.possession());
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		List<View> views = views(partialTick);
		if (views.isEmpty()) return;
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		float time = mc.level.getGameTime() + partialTick;
		for (View view : views) {
			if (view.center().distToCenterSqr(camera) > 96.0D * 96.0D) continue;
			if (view.sink() > 0.0D) renderSinkingBlocks(mc, poseStack, buffers, camera, view);
			renderShell(poseStack, buffers, camera, time, view);
		}
	}

	/** The captured structure, sinking under the membrane; the floor hides whatever has gone below it. */
	private static void renderSinkingBlocks(Minecraft mc, PoseStack poseStack, MultiBufferSource.BufferSource buffers,
			Vec3 camera, View view) {
		for (int i = 0; i < view.positions().size(); i++) {
			BlockState state = view.states().get(i);
			if (state.isAir()) continue;
			BlockPos pos = view.positions().get(i);
			poseStack.pushPose();
			poseStack.translate(pos.getX() - camera.x, pos.getY() - view.sink() - camera.y, pos.getZ() - camera.z);
			mc.getBlockRenderer().renderSingleBlock(state, poseStack, buffers,
					LevelRenderer.getLightColor(mc.level, pos), OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
					RenderType.cutoutMipped());
			poseStack.popPose();
		}
		buffers.endBatch(RenderType.cutoutMipped());
	}

	private static void renderShell(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Vec3 camera,
			float time, View view) {
		if (view.fill() <= 0.0F || view.alpha() <= 0.0F) return;
		float heartbeat = CardinalRiteFloorVisualRules.heartbeat(time);
		float strength = Math.min(1.0F, 0.55F + 0.45F * view.fill() + 0.25F * heartbeat + 0.6F * view.sealPulse()
				+ 0.5F * view.possession());
		int alpha = Math.round(255.0F * view.alpha() * strength);
		if (alpha <= 0) return;
		double fillTop = WorkingCraftSwallowRules.fillLine(view.bottomY(), view.height(), view.fill()) - view.sink();
		double centerX = (view.minX() + view.maxX() + 1) / 2.0D;
		double centerZ = (view.minZ() + view.maxZ() + 1) / 2.0D;
		// Possessed, the membrane thickens and throbs harder over the whole structure.
		float throb = view.possession() * (0.6F + 0.4F * heartbeat);
		RenderType type = HemoRenderTypes.bloodStructureWarp(time,
				Math.min(1.0F, view.fill() + view.sealPulse() * 0.4F + throb * 0.5F),
				(float) (view.center().asLong() % 997L) / 997.0F, 0.018F + 0.03F * view.fill() + 0.03F * throb,
				(float) (centerX - camera.x), (float) (view.bottomY() + view.height() / 2.0D - view.sink() - camera.y),
				(float) (centerZ - camera.z), 0.0F, (float) (view.bottomY() - camera.y), view.height(),
				(float) (fillTop + 0.02D - camera.y));
		VertexConsumer consumer = buffers.getBuffer(type);
		poseStack.pushPose();
		poseStack.translate(0.0D, -view.sink(), 0.0D);
		BlockShellFaces.emit(consumer, poseStack.last(), new HashSet<>(view.positions()), camera,
				alpha << 24 | WorkingCraftSwallowRules.possessionTint(SHELL_TINT, view.possession()));
		poseStack.popPose();
		buffers.endBatch(type);
	}
}
