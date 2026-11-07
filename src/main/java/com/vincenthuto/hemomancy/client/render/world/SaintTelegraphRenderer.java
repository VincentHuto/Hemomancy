package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.registry.SaintClientStyles;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveView;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphFamily;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The one Saint footprint pass, under {@code WorldEffectDispatcher} straight after the manipulation visuals. Its input
 * is only synced data: each kit Saint's move block, decoded by {@link SaintMoveView} into {@link SaintMoveInputs} and a
 * {@link FootprintFrame}, then the move's own footprint, timeline and {@link TelegraphSpec}; and each
 * {@link BossGroundHazardEntity}'s synced shape and frame. A move draws from its reveal tick (pulled earlier so it
 * always shows {@code SaintMoves.revealFloor} ticks before its commit) to the commit, and through the active span when
 * the spec says so, with a 4-tick flash at the commit. Nothing draws during a Saint's rise or its ending. Each Saint's
 * {@link SaintTelegraphStyle} paints first; the default painter takes what a style declines.
 */
public final class SaintTelegraphRenderer {
	/** Saints and hazards further than this from the camera are not drawn. */
	private static final double RANGE = 64.0D;
	public static final float COMMIT_FLASH_TICKS = 4.0F;
	private static final LuxUmbraBatch BATCH = new LuxUmbraBatch();

	private SaintTelegraphRenderer() {
	}

	public static void render(RenderLevelStageEvent event) {
		boolean solids = event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES;
		if (!solids && event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) return;
		Vec3 camera = event.getCamera().getPosition();
		List<SaintTrialEntity> saints = new ArrayList<>();
		List<BossGroundHazardEntity> hazards = new ArrayList<>();
		for (Entity entity : level.entitiesForRendering()) {
			if (entity.isRemoved() || entity.distanceToSqr(camera) > RANGE * RANGE) continue;
			if (entity instanceof SaintTrialEntity saint && saint.kit() != null && saint.syncedMoveId() != 0) saints.add(saint);
			else if (entity instanceof BossGroundHazardEntity hazard) hazards.add(hazard);
		}
		if (saints.isEmpty() && hazards.isEmpty()) return;
		float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
		double time = AnimationTime.gameTime(level, (double) partial);
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		PoseStack poses = new PoseStack();
		if (!solids) poses.mulPose(event.getModelViewMatrix());
		LuxUmbraRenderTypes.begin(time);
		ThermalRenderTypes.begin(time);
		AnimusMortemRenderTypes.begin(time);
		FerricDuctilisRenderTypes.begin(time);
		BATCH.clear();
		TelegraphCanvas canvas = solids
				? new TelegraphCanvas(poses, camera, null, null, buffers.getBuffer(FerricDuctilisRenderTypes.IRON), time)
				: new TelegraphCanvas(poses, camera, BATCH, buffers.getBuffer(HemoRenderTypes.SAINT_TELEGRAPH_TINT), null, time);
		for (BossGroundHazardEntity hazard : hazards) paintHazard(hazard, canvas, partial);
		for (SaintTrialEntity saint : saints) {
			TelegraphDraw draw = draw(saint, partial);
			if (draw == null) continue;
			EnumSaintType type = saint.saintType();
			SaintTelegraphStyle style = SaintClientStyles.telegraph(type);
			if (!style.paint(draw, canvas)) {
				SaintTelegraphPainter.paint(draw, canvas, family(draw.spec().fill(), SaintClientStyles.fill(type)),
						family(draw.spec().edge(), SaintClientStyles.edge(type)));
			}
		}
		if (solids) {
			buffers.endBatch(FerricDuctilisRenderTypes.IRON);
			return;
		}
		buffers.endBatch(HemoRenderTypes.SAINT_TELEGRAPH_TINT);
		BATCH.drawMaterials(material -> buffers.getBuffer(ThermalRenderTypes.material(material)));
		for (LuxUmbraBatch.Material material : LuxUmbraBatch.Material.values()) {
			buffers.endBatch(ThermalRenderTypes.material(material));
		}
		LuxUmbraRenderTypes.finish(buffers);
	}

	/** The move as the pass draws it this frame, or null when nothing shows (no move, rising, ending, unrevealed). */
	@Nullable
	static TelegraphDraw draw(SaintTrialEntity saint, float partial) {
		if (risingOrEnding(saint, partial)) return null;
		SaintMoveView view = saint.view();
		SaintMove move = view.move();
		if (move == null) return null;
		SaintMoveInputs inputs = view.inputs();
		BossMoveTimeline timeline = SaintControllerRules.effective(move.kind(), move.timeline(inputs));
		TelegraphSpec spec = move.telegraph(inputs);
		if (spec == null) spec = TelegraphSpec.DEFAULT;
		float moveTime = view.moveTime(partial);
		if (moveTime < 0.0F) return null;
		BossMoveTimeline.Stage stage = timeline.stageAt((int) Math.floor(moveTime));
		int reveal = SaintControllerRules.revealTick(spec, timeline, move.kind());
		if (!SaintTelegraphTiming.visible(moveTime, reveal, timeline, spec.showWhileActive())) return null;
		FootprintFrame frame = view.frame();
		if (frame == null) {
			frame = FootprintFrame.simple(saint.syncedMoveOrigin(), saint.syncedMoveAim(), saint.syncedMoveAim(), saint.getY());
		}
		BossFootprint footprint = move.footprint(inputs);
		if (footprint == null) return null;
		float age = moveTime - timeline.commitTick();
		return new TelegraphDraw(saint, move, footprint, frame, timeline, stage, moveTime, age, spec,
				SaintTelegraphTiming.reveal(moveTime, reveal, timeline.commitTick()),
				SaintTelegraphTiming.commitFlash(moveTime, timeline.commitTick(), COMMIT_FLASH_TICKS), partial);
	}

	private static void paintHazard(BossGroundHazardEntity hazard, TelegraphCanvas canvas, float partial) {
		BossFootprint shape = hazard.shape();
		if (shape instanceof BossFootprint.None) return;
		Entity owner = hazard.owner();
		SaintTrialEntity saint = owner instanceof SaintTrialEntity trial ? trial : null;
		float age = AnimationTime.age(hazard, partial);
		HazardDraw draw = new HazardDraw(hazard, shape, hazard.frame(), age, hazard.life(),
				SaintTelegraphTiming.hazardFade(age, hazard.life(), hazard.fade(), hazard.fading(), hazard.fadeStart()), partial);
		if (draw.fade() <= 0.0F) return;
		if (saint != null) {
			EnumSaintType type = saint.saintType();
			if (SaintClientStyles.telegraph(type).paintHazard(draw, canvas)) return;
			SaintTelegraphPainter.paintHazard(draw, canvas, SaintClientStyles.fill(type), SaintClientStyles.edge(type));
			return;
		}
		SaintTelegraphPainter.paintHazard(draw, canvas, TelegraphFamily.ANIMUS, TelegraphFamily.ANIMUS);
	}

	private static boolean risingOrEnding(SaintTrialEntity saint, float partial) {
		float rise = saint.riseTime(partial);
		if (rise >= 0.0F && rise < SaintRise.TICKS) return true;
		return saint.syncedEndingTick() >= 0;
	}

	private static TelegraphFamily family(@Nullable TelegraphFamily chosen, TelegraphFamily saints) {
		return chosen == null ? saints : chosen;
	}
}
