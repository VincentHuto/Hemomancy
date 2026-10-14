package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.floor.AnchorSores;
import com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteCeremonyLayout;
import com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteEndingRules;
import com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteHelixEffects;
import com.vincenthuto.hemomancy.client.render.world.rite.RiteQuads;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFittingHelper;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMath;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAnchorVisualRules;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilLandmarkGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilOrganicGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRenderPalette;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientHooks;
import org.joml.Matrix4f;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The living boundary of a Harbinger Cardinal Rite: concentric blood vessels
 * joined by anchor sockets, inward capillaries and the black stain they leave
 * on the floor. Rings, sockets and floor beat on the shared rite heartbeat and
 * lean toward the consecrated floor's palette, react to the rite's beats
 * (anchors filling, breaking and knitting, seals, waves, the staff impact),
 * and play the rite's ending out from its ghost instead of vanishing.
 *
 * <p>Three passes: a colour-only glow pass (halos and stains), a depth-writing
 * core pass (the vessels themselves), and an additive bloom pass for the
 * heartbeat swell and bright beats.
 */
public class CardinalRiteBoundaryRenderer {
	/** How long a collapsed or broken rite's boundary takes to tear away. */
	public static final float VIOLENT_ENDING_TICKS = CardinalRiteEndingRules.VIOLENT_TICKS;
	/** How long a completed rite's boundary takes to draw into the Focus. */
	public static final float COMPLETED_ENDING_TICKS = CardinalRiteEndingRules.COMPLETED_TICKS;

	// ── Ring geometry ──
	private static final int SEGMENTS = CardinalRiteCeremonyLayout.RING_SEGMENTS;
	private static final float CORE_WIDTH = 0.075F;
	private static final float GLOW_WIDTH = 0.20F;
	/** The dark groove either side of the vessel, so it reads as sunk into the floor rather than laid on it. */
	private static final float GROOVE_WIDTH = 0.035F;
	/** How narrow the vessel pinches as it dives into an anchor's sore. */
	private static final float SORE_DIVE_PINCH = 0.45F;
	/** How far along the ring, in blocks, the vessel fades out before its end inside a sore's mouth. */
	private static final float SORE_DIVE_BLOCKS = 0.32F;
	private static final float BLOOM_WIDTH = 0.15F;
	/** Bloom sits just above the vessel so the additive pass never z-fights the depth-writing core. */
	private static final float BLOOM_LIFT = 0.004F;
	private static final int SOCKET_SEGMENTS = 24;
	private static final float SOCKET_RADIUS = CardinalRiteCeremonyLayout.SOCKET_RADIUS;
	private static final float SOCKET_CORE_WIDTH = CardinalRiteCeremonyLayout.SOCKET_CORE_WIDTH;
	private static final float SOCKET_GLOW_WIDTH = 0.14F;
	private static final float SOCKET_UNDULATION_SCALE = 0.15F;
	private static final double BOUNDARY_SOCKET_WAVE_FADE_ANGLE = 0.20D;
	private static final int SOCKET_STAIN_SEGMENTS = 16;
	private static final int SIGIL_VESSEL_SEGMENTS = 7;
	private static final float EFFECT_Y = 0.024F;

	// ── Undulation ──
	private static final double UNDULATE_FREQ = 6.0D;
	private static final float UNDULATE_AMP = 0.12F;
	private static final double UNDULATE_SPEED = 0.045D;
	private static final double UNDULATE_FREQ2 = 13.0D;
	private static final float UNDULATE_AMP2 = 0.04F;
	private static final double UNDULATE_SPEED2 = 0.09D;
	private static final float RING_SURFACE_WAVE = 0.012F;
	private static final float SOCKET_SURFACE_WAVE = 0.006F;

	// ── Veins ──
	private static final int VEIN_COUNT = 18;
	private static final int VEIN_SEGS = 8;
	private static final float VEIN_LENGTH = 0.62F;
	private static final float VEIN_ROOT_WIDTH = 0.016F;
	/** Capillaries keep this far (in radians round the ring) from an anchor's sore. */
	private static final double VEIN_SORE_CLEARANCE = 0.35D;

	// ── Palette accents (Riposte standard: one hot core, one dark clot) ──
	private static final float HOT_R = 1.0F;
	private static final float HOT_G = 0.82F;
	private static final float HOT_B = 0.77F;
	private static final float BRUISE_R = 0.10F;
	private static final float BRUISE_G = 0.015F;
	private static final float BRUISE_B = 0.085F;
	/** How far ring vessels lean toward the floor style's vein colour. */
	private static final float FLOOR_VEIN_LEAN = 0.28F;
	/** How far ring halos lean toward the floor style's glow colour. */
	private static final float FLOOR_GLOW_LEAN = 0.18F;

	// ── Caches ──
	private static final int LAYOUT_REFRESH_TICKS = 100;
	private static final float[] SOCKET_COS = new float[SOCKET_SEGMENTS + 1];
	private static final float[] SOCKET_SIN = new float[SOCKET_SEGMENTS + 1];
	private static final float[] STAIN_COS = new float[SOCKET_STAIN_SEGMENTS + 1];
	private static final float[] STAIN_SIN = new float[SOCKET_STAIN_SEGMENTS + 1];
	private static final Map<ResourceLocation, CachedLayout> LAYOUTS = new HashMap<>();
	private static WeakReference<Level> layoutLevel = new WeakReference<>(null);
	private static final Map<UUID, Integer> OWNER_STAFF_VISUALS = new HashMap<>();
	private static final Map<String, ModelResourceLocation> STAFF_MODELS = new HashMap<>();
	private static ItemStack plantedStaffStack;

	static {
		for (int index = 0; index <= SOCKET_SEGMENTS; index++) {
			double angle = Math.PI * 2.0D * index / SOCKET_SEGMENTS;
			SOCKET_COS[index] = (float) Math.cos(angle);
			SOCKET_SIN[index] = (float) Math.sin(angle);
		}
		for (int index = 0; index <= SOCKET_STAIN_SEGMENTS; index++) {
			double angle = Math.PI * 2.0D * index / SOCKET_STAIN_SEGMENTS;
			STAIN_COS[index] = (float) Math.cos(angle);
			STAIN_SIN[index] = (float) Math.sin(angle);
		}
	}

	private enum Pass {
		GLOW,
		CORE,
		BLOOM
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		List<ActiveRiteClientData.RiteEntry> rites = ActiveRiteClientData.getActiveRites();
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		float currentTime = mc.level.getGameTime() + partialTick;
		Camera camera = mc.gameRenderer.getMainCamera();
		Vec3 cam = camera.getPosition();

		List<RiteFrame> frames = prepareFrames(mc, poseStack, rites, currentTime, partialTick, cam);

		// Enclose the live ritual space in the established black/red Fane
		// material. From inside the circle this shades the world beyond the
		// outermost completed ring while leaving the ritual interior clear.
		for (RiteFrame frame : frames) {
			drawExteriorField(poseStack, buffer, frame, currentTime, cam);
		}

		CardinalRiteFogRenderer.render(poseStack, buffer, rites, currentTime, camera);
		CardinalRiteHelixEffects.tick(mc, rites, currentTime, cam);
		if (frames.isEmpty()) return;
		playBoundaryCompletionSounds(mc, frames);

		// Separate passes so only one non-fixed render type is active at a time.
		// NeoForge 1.21.1's sequential BufferSource flushes the current type when a
		// second different type is requested, leaving the first VertexConsumer dead.
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
		for (RiteFrame frame : frames) {
			drawRite(poseStack, glowVC, Pass.GLOW, frame, currentTime, partialTick, cam);
		}
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		for (RiteFrame frame : frames) {
			drawRite(poseStack, coreVC, Pass.CORE, frame, currentTime, partialTick, cam);
		}
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);

		VertexConsumer bloomVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_BLOOM);
		for (RiteFrame frame : frames) {
			drawRite(poseStack, bloomVC, Pass.BLOOM, frame, currentTime, partialTick, cam);
		}
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_BLOOM);

		for (RiteFrame frame : frames) {
			if (frame.entry.hasPlantedStaff()) drawPlantedStaff(poseStack, buffer, frame, cam, partialTick);
		}
		buffer.endBatch();

		// The staff's binding strands are HutosLib tendril tubes, glow under core as HutosLib draws them. They come
		// after the staff: their render types test depth but never write it, so the staff must already be in the
		// depth buffer for the strands to pass in front of it and behind it.
		VertexConsumer tendrilGlowVC = buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW);
		for (RiteFrame frame : frames) {
			drawStaffTendrils(poseStack, tendrilGlowVC, frame, currentTime, partialTick, cam, true);
		}
		buffer.endBatch(HLRenderTypeInit.TENDRIL_GLOW);
		VertexConsumer tendrilCoreVC = buffer.getBuffer(HLRenderTypeInit.TENDRIL_CORE);
		for (RiteFrame frame : frames) {
			drawStaffTendrils(poseStack, tendrilCoreVC, frame, currentTime, partialTick, cam, false);
		}
		buffer.endBatch(HLRenderTypeInit.TENDRIL_CORE);
	}

	/** Forgets cached ceremony layouts and staff visuals, e.g. after a datapack reload. */
	public static void clearCaches() {
		LAYOUTS.clear();
		OWNER_STAFF_VISUALS.clear();
		layoutLevel = new WeakReference<>(null);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Frame preparation: everything shared by the three passes, once per rite
	// ════════════════════════════════════════════════════════════════════════

	private static List<RiteFrame> prepareFrames(Minecraft mc, PoseStack stack,
			List<ActiveRiteClientData.RiteEntry> rites, float time, float partialTick, Vec3 cam) {
		List<RiteFrame> frames = new ArrayList<>(rites.size() + 1);
		int renderDistance = mc.options.renderDistance().get();
		for (ActiveRiteClientData.RiteEntry rite : rites) {
			if (rite.isUnstained()) continue;
			CardinalRiteEventClientState.Ghost ghost = CardinalRiteEventClientState.ghostAt(rite.getCenter());
			if (ghost != null && supersedes(ghost, rite, partialTick)) continue;
			RiteFrame frame = buildFrame(mc, stack, rite, null, 0.0F, time, partialTick, cam, renderDistance);
			if (frame != null) frames.add(frame);
		}
		for (CardinalRiteEventClientState.Ghost ghost : CardinalRiteEventClientState.ghosts()) {
			if (ghost.entry().isUnstained()) continue;
			float age = ghost.age(partialTick);
			if (CardinalRiteEndingRules.finished(ghost.kind(), age)) continue;
			RiteFrame frame = buildFrame(mc, stack, ghost.entry(), ghost.kind(), age, time, partialTick, cam,
					renderDistance);
			if (frame != null) frames.add(frame);
		}
		return frames;
	}

	/**
	 * A rite's ending takes over from its live entry while the server may still
	 * be sending the last snapshots of the same rite.
	 */
	private static boolean supersedes(CardinalRiteEventClientState.Ghost ghost,
			ActiveRiteClientData.RiteEntry live, float partialTick) {
		if (ghost.entry() == live) return true;
		if (!Objects.equals(ghost.entry().getRecipeId(), live.getRecipeId())) return false;
		return live.isPhase(CardinalRitePhase.COMPLETE) || live.isPhase(CardinalRitePhase.COLLAPSED)
				|| ghost.age(partialTick) < CardinalRiteEndingRules.duration(ghost.kind());
	}

	private static RiteFrame buildFrame(Minecraft mc, PoseStack stack, ActiveRiteClientData.RiteEntry rite,
			CardinalRiteEventKind ending, float endingAge, float time, float partialTick, Vec3 cam,
			int renderDistance) {
		BlockPos center = rite.getCenter();
		boolean legacy = rite.isLegacy();
		int riteTier = (rite.getRiteSize() - 1) / 2;
		int ringCount = legacy ? Math.max(1, riteTier) : rite.getTotalRings();
		double cx = center.getX() + 0.5D;
		double cy = CardinalRiteBoundaryGeometry.boundaryPlaneY(center.getY());
		double cz = center.getZ() + 0.5D;
		float outerRadius = legacy ? (float) (rite.getRiteSize() / 2.0D + 1.0D + ringCount * 2.0D)
				: CardinalRiteBoundaryGeometry.interactiveRingRadius(ringCount) + 1.0F;
		double reach = Math.max(1, renderDistance) * 16.0D + Math.max(outerRadius, rite.getFootprintRadius()) + 4.0D;
		double dx = cx - cam.x;
		double dz = cz - cam.z;
		if (dx * dx + dz * dz > reach * reach) return null;

		CardinalRiteCeremonyLayout layout = legacy ? null : layoutFor(mc.level, rite.getRecipeId(), (long) time);
		RiteFrame frame = new RiteFrame(rite, ending, endingAge, legacy, ringCount, layout);
		frame.heartbeat = CardinalRiteMath.heartbeat(time);
		frame.fade = CardinalRiteEndingRules.fade(ending, endingAge);
		frame.stainOpacity = (legacy ? 1.0F
				: CardinalRiteBoundaryGeometry.stainOpacity(rite.stainFadeProgress(partialTick))) * frame.fade;
		frame.tint = floorTint(mc.level, center);
		collectBeats(frame, partialTick);

		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		frame.baseMatrix = new Matrix4f(stack.last().pose());
		float baseRadius = (float) (rite.getRiteSize() / 2.0D + 1.0D);
		frame.rings = new RingFrame[Math.max(0, ringCount)];
		for (int ring = 0; ring < ringCount; ring++) {
			float radius = legacy ? baseRadius + ring * 2.0F
					: layout != null ? layout.ringRadius(ring)
					: CardinalRiteBoundaryGeometry.interactiveRingRadius(ring);
			RingFrame ringFrame = new RingFrame(ring, radius);
			ringFrame.arcs = visibleArcs(rite, layout, legacy, ring, partialTick);
			ringFrame.endpoints = layout == null ? List.of() : layout.socketEndpoints(ring);
			CardinalRiteCompletionRingTheatrics.RingPose pose = CardinalRiteCompletionRingTheatrics.pose(
					rite.ritePhase(), rite.phaseRenderTicks(partialTick), rite.ringSpinTicks(partialTick),
					ring, ringCount, radius);
			float scale = pose.radialScale();
			float lift = pose.verticalOffset();
			float opacity = pose.opacity();
			if (ending == CardinalRiteEventKind.COMPLETED) {
				float contraction = CardinalRiteEndingRules.completionContraction(endingAge);
				scale *= 1.0F - contraction * 0.94F;
				lift += contraction * 0.35F;
				opacity *= frame.fade;
			} else if (ending == CardinalRiteEventKind.CANCELLED) {
				opacity *= frame.fade;
			} else if (CardinalRiteEndingRules.violent(ending)) {
				// Whatever has not drained by the end of the tear is gone with the ghost.
				opacity *= 1.0F - CardinalRiteMath.smoothstep((endingAge - 30.0F) / 10.0F);
			}
			ringFrame.radialScale = Math.max(0.001F, scale);
			ringFrame.opacity = opacity;
			ringFrame.socketVisibility = CardinalRiteCompletionRingTheatrics.socketVisibility(
					rite.ritePhase(), rite.phaseRenderTicks(partialTick)) * (ending == null ? 1.0F : frame.fade);
			ringFrame.directionSign = ring % 2 == 0 ? 1.0F : -1.0F;
			ringFrame.ringFade = Math.max(0.1F, 1.0F - ring * 0.15F);
			ringFrame.colors = RingColors.of(ring, ringCount, frame.tint);
			stack.pushPose();
			stack.translate(0.0F, lift, 0.0F);
			stack.mulPose(Axis.YP.rotation(pose.rotationRadians()));
			stack.scale(ringFrame.radialScale, 1.0F, ringFrame.radialScale);
			ringFrame.matrix = new Matrix4f(stack.last().pose());
			stack.popPose();
			frame.rings[ring] = ringFrame;
		}
		stack.popPose();
		return frame;
	}

	private static List<CardinalRiteBoundaryProgress.Segment> visibleArcs(ActiveRiteClientData.RiteEntry rite,
			CardinalRiteCeremonyLayout layout, boolean legacy, int ring, float partialTick) {
		if (legacy) return List.of(new CardinalRiteBoundaryProgress.Segment(ring, 0.0D, Math.PI * 2.0D));
		List<CardinalRiteBoundaryProgress.Segment> arcs = new ArrayList<>(4);
		boolean hasAnchorSockets = layout != null;
		for (CardinalRiteBoundaryProgress.Segment segment : rite.getBoundarySegments()) {
			if (segment.ring() != ring) continue;
			// A rite whose recipe was removed by a datapack update keeps zero-clearance arcs.
			double startClearance = hasAnchorSockets ? layout.startClearance(segment) : 0.0D;
			double endClearance = hasAnchorSockets ? layout.endClearance(segment) : 0.0D;
			arcs.addAll(CardinalRiteBoundaryGeometry.animatedSocketArcs(
					segment, rite.boundaryGrowth(segment, partialTick), startClearance, endClearance));
		}
		return arcs;
	}

	private static CardinalRiteCeremonyLayout layoutFor(Level level, ResourceLocation recipeId, long gameTime) {
		if (level == null || recipeId == null) return null;
		if (layoutLevel.get() != level) {
			LAYOUTS.clear();
			layoutLevel = new WeakReference<>(level);
		}
		CachedLayout cached = LAYOUTS.get(recipeId);
		if (cached != null && gameTime < cached.refreshAt() && gameTime >= cached.refreshAt() - LAYOUT_REFRESH_TICKS) {
			return cached.layout();
		}
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, recipeId);
		// A rite from an old world whose recipe was removed falls back to plain boundary geometry.
		CardinalRiteCeremonyDefinition ceremony = recipe == null ? null : recipe.getCeremony();
		CardinalRiteCeremonyLayout layout = cached != null && cached.ceremony() == ceremony
				? cached.layout() : CardinalRiteCeremonyLayout.of(ceremony);
		LAYOUTS.put(recipeId, new CachedLayout(ceremony, layout, gameTime + LAYOUT_REFRESH_TICKS));
		return layout;
	}

	private static FloorTint floorTint(Level level, BlockPos center) {
		if (level == null || !(level.getBlockEntity(center) instanceof CardinalFocusBlockEntity focus)) return null;
		ResourceLocation floorId = focus.getFormedFloor() != null ? focus.getFormedFloor() : focus.getFadingFloor();
		if (floorId == null) return null;
		return CardinalRiteFloorRegistry.get(floorId)
				.map(floor -> FloorTint.of(CardinalRiteFloorVisualRules.palette(
						CardinalRiteFloorVisualRules.styleIndex(floor.style()))))
				.orElse(null);
	}

	/** Reads the rite's recent beats into ring-wide flashes and per-anchor effects. */
	private static void collectBeats(RiteFrame frame, float partialTick) {
		float flash = 0.0F;
		float dim = 0.0F;
		for (CardinalRiteEventClientState.RiteEvent event
				: CardinalRiteEventClientState.recent(frame.entry.getCenter())) {
			float age = event.age(partialTick);
			switch (event.kind()) {
				case WAVE_CLEARED -> flash += CardinalRiteMath.pulse(age, 2.0F, 26.0F);
				case CONSECRATED -> flash += 1.2F * CardinalRiteMath.pulse(age, 3.0F, 34.0F);
				case SEALED -> flash += 0.8F * CardinalRiteMath.pulse(age, 2.0F, 24.0F);
				case SIGIL_AWAKENED -> flash += 0.4F * CardinalRiteMath.pulse(age, 2.0F, 16.0F);
				case WAVE_START -> dim += 0.35F * CardinalRiteMath.pulse(age, 1.0F, 12.0F);
				case STAFF_IMPACT -> {
					if (age < IMPACT_TICKS) frame.impactAge = age;
					flash += 0.5F * CardinalRiteMath.pulse(age, 1.0F, 14.0F);
				}
				case ANCHOR_FILLED, ANCHOR_BROKEN, ANCHOR_REPAIRED -> {
					if (age < anchorBeatLength(event.kind())) {
						frame.beats.add(new AnchorBeat(event.kind(), event.index(), age));
					}
				}
				default -> {
				}
			}
		}
		if (CardinalRiteEndingRules.violent(frame.ending) && frame.layout != null) {
			for (int index = 0; index < frame.layout.anchors().size(); index++) {
				float age = frame.endingAge - CardinalRiteEndingRules.socketBurstDelay(index);
				if (age >= 0.0F && age < anchorBeatLength(CardinalRiteEventKind.ANCHOR_BROKEN)) {
					frame.beats.add(new AnchorBeat(CardinalRiteEventKind.ANCHOR_BROKEN, index, age));
				}
			}
		}
		frame.flash = Math.min(1.5F, flash);
		frame.dim = 1.0F - Math.min(0.6F, dim);
	}

	private static final float IMPACT_TICKS = 14.0F;
	private static final float FILL_BEAT_TICKS = 26.0F;
	private static final float BREAK_BEAT_TICKS = 30.0F;
	private static final float REPAIR_BEAT_TICKS = 18.0F;

	private static float anchorBeatLength(CardinalRiteEventKind kind) {
		return switch (kind) {
			case ANCHOR_FILLED -> FILL_BEAT_TICKS;
			case ANCHOR_BROKEN -> BREAK_BEAT_TICKS;
			default -> REPAIR_BEAT_TICKS;
		};
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Per-pass drawing
	// ════════════════════════════════════════════════════════════════════════

	private static void drawRite(PoseStack stack, VertexConsumer vc, Pass pass, RiteFrame frame,
			float time, float partialTick, Vec3 cam) {
		boolean hasAnchorSockets = frame.layout != null;
		// Transparent stains feather out, so they stay on the colour-only glow pass and never write depth.
		if (pass == Pass.GLOW && !frame.legacy && hasAnchorSockets) {
			for (RingFrame ring : frame.rings) drawBoundaryFloorStain(vc, frame, ring);
		}
		for (RingFrame ring : frame.rings) {
			drawRing(vc, pass, frame, ring, time);
			drawVeins(vc, pass, frame, ring, time);
			if (hasAnchorSockets) {
				// Anchors open as sores in the consecrated floor (floor.AnchorSores); the boundary only adds their beats.
				drawAnchorBeats(vc, pass, frame, ring);
			}
		}
		if (!frame.legacy && hasAnchorSockets && frame.ending == null) {
			drawSegmentSeals(vc, pass, frame, partialTick);
		}
		drawFinaleFlash(vc, pass, frame, partialTick);
		drawImpactShock(vc, pass, frame);
		if (pass == Pass.BLOOM) return;
		boolean glow = pass == Pass.GLOW;
		drawSigilSegments(stack, vc, frame, time, cam, glow);
		drawSanguineBlobs(stack, vc, frame, time, cam, glow);
	}

	// ── Main ring ──

	private static void drawRing(VertexConsumer vc, Pass pass, RiteFrame frame, RingFrame ring, float time) {
		if (ring.arcs.isEmpty() || ring.opacity <= 0.001F || ring.radialScale <= 0.001F) return;
		Matrix4f mat = ring.matrix;
		RingColors colors = ring.colors;
		float inverseScale = 1.0F / ring.radialScale;
		float beat = frame.heartbeat;
		float baseAlpha = ring.ringFade * ring.opacity * frame.dim;
		float coreAlpha = (0.78F + beat * 0.18F) * baseAlpha;
		float glowAlpha = (0.07F + beat * 0.07F + frame.flash * 0.22F) * baseAlpha;
		float grooveAlpha = 0.42F * baseAlpha;
		float bloomAlpha = (0.10F + beat * 0.36F + frame.flash * 0.80F) * baseAlpha;
		float coreWidthBase = CORE_WIDTH * inverseScale * (1.0F + beat * 0.16F);
		float glowWidthBase = GLOW_WIDTH * inverseScale;
		float bloomWidthBase = BLOOM_WIDTH * inverseScale * (1.0F + frame.flash * 0.6F);
		boolean violent = CardinalRiteEndingRules.violent(frame.ending);
		double tearStart = CardinalRiteEndingRules.hash(frame.entry.getCenter().hashCode(), ring.index)
				* Math.PI * 2.0D;
		double chunkSweep = Math.PI * 2.0D / CardinalRiteEndingRules.RUPTURE_CHUNKS_PER_RING;

		for (CardinalRiteBoundaryProgress.Segment arc : ring.arcs) {
			int pieces = CardinalRiteBoundaryGeometry.pieceCount(arc.sweepAngle(), SEGMENTS);
			double pieceSweep = arc.sweepAngle() / Math.max(1, pieces);
			for (int piece = 0; piece < pieces; piece++) {
				double a1 = arc.startAngle() + pieceSweep * piece;
				double a2 = a1 + pieceSweep;
				double midpoint = (a1 + a2) * 0.5D;
				float integrity = frame.legacy ? 1.0F : arcIntegrity(ring.arcs, midpoint, time);
				if (integrity <= 0.01F) continue;

				float waveScale1 = frame.legacy ? 1.0F : CardinalRiteBoundaryGeometry.endpointWaveScale(
						a1, ring.endpoints, BOUNDARY_SOCKET_WAVE_FADE_ANGLE);
				float waveScale2 = frame.legacy ? 1.0F : CardinalRiteBoundaryGeometry.endpointWaveScale(
						a2, ring.endpoints, BOUNDARY_SOCKET_WAVE_FADE_ANGLE);
				float r1 = ring.radius + undulation(a1, time, ring.directionSign) * waveScale1;
				float r2 = ring.radius + undulation(a2, time, ring.directionSign) * waveScale2;
				float y1 = surfaceWave(a1, time, ring.directionSign, 0.0D, RING_SURFACE_WAVE * waveScale1);
				float y2 = surfaceWave(a2, time, ring.directionSign, 0.0D, RING_SURFACE_WAVE * waveScale2);

				float pieceAlpha = integrity;
				float bruise = 0.0F;
				float widthScale = 1.0F;
				if (violent) {
					// The tear runs around the ring; each torn chunk pinches apart, sags into the floor,
					// drifts outward and darkens to a bruise before it drains away.
					double fromTear = CardinalRiteMath.normalizeAngle(midpoint - tearStart);
					int chunk = (int) (fromTear / chunkSweep);
					float jitter = CardinalRiteEndingRules.hash(chunk, ring.index * 31 + 7);
					float order = chunk / (float) CardinalRiteEndingRules.RUPTURE_CHUNKS_PER_RING;
					float tear = CardinalRiteEndingRules.rupture(frame.endingAge, ring.index, order, jitter);
					float tremor = (1.0F - tear) * CardinalRiteMath.clamp01(frame.endingAge / 6.0F);
					r1 += (float) Math.sin(time * 2.3F + a1 * 11.0D) * 0.035F * tremor;
					r2 += (float) Math.sin(time * 2.3F + a2 * 11.0D) * 0.035F * tremor;
					if (tear > 0.0F) {
						double chunkCenter = tearStart + (chunk + 0.5D) * chunkSweep;
						float pinch = 1.0F - 0.55F * CardinalRiteMath.easeOutCubic(tear);
						a1 = chunkCenter + CardinalRiteMath.normalizeAngle(a1 - chunkCenter + Math.PI) * pinch
								- Math.PI * pinch;
						a2 = chunkCenter + CardinalRiteMath.normalizeAngle(a2 - chunkCenter + Math.PI) * pinch
								- Math.PI * pinch;
						float drift = CardinalRiteMath.easeOutCubic(tear) * (0.22F + jitter * 0.45F)
								* (jitter > 0.25F ? 1.0F : -0.6F);
						r1 += drift;
						r2 += drift;
						float sag = CardinalRiteMath.easeInCubic(tear) * 0.20F;
						y1 -= sag;
						y2 -= sag;
						bruise = CardinalRiteMath.smoothstep(tear * 1.4F);
						widthScale = 1.0F - 0.5F * tear;
						pieceAlpha *= 1.0F - CardinalRiteMath.smoothstep((tear - 0.35F) / 0.65F);
						if (pieceAlpha <= 0.005F) continue;
					}
				}

				float cos1 = (float) Math.cos(a1);
				float sin1 = (float) Math.sin(a1);
				float cos2 = (float) Math.cos(a2);
				float sin2 = (float) Math.sin(a2);
				float coreWidth = CardinalRiteBoundaryGeometry.integrityWidth(coreWidthBase, integrity) * widthScale;
				// The vessel pinches as it nears a sore, so it dives into the pit instead of butting against it.
				float halfCore1 = coreWidth * 0.5F * (1.0F - SORE_DIVE_PINCH * (1.0F - waveScale1));
				float halfCore2 = coreWidth * 0.5F * (1.0F - SORE_DIVE_PINCH * (1.0F - waveScale2));
				// ...and fades out over its last stretch, so its end is lost in the pit rather than cut off.
				float dive1 = frame.legacy ? 1.0F : CardinalRiteBoundaryGeometry.endpointWaveScale(
						a1, ring.endpoints, SORE_DIVE_BLOCKS / Math.max(0.5F, ring.radius));
				float dive2 = frame.legacy ? 1.0F : CardinalRiteBoundaryGeometry.endpointWaveScale(
						a2, ring.endpoints, SORE_DIVE_BLOCKS / Math.max(0.5F, ring.radius));
				float dry = CardinalRiteBoundaryGeometry.integrityBrightness(integrity) * (1.0F - bruise);
				float arterial = CardinalRiteBoundaryGeometry.arterialHighlight(midpoint, time, ring.index)
						* integrity * (1.0F - bruise);
				switch (pass) {
					case GLOW -> {
						float glowWidth = CardinalRiteBoundaryGeometry.integrityWidth(glowWidthBase, integrity)
								* widthScale;
						float alpha = glowAlpha * pieceAlpha;
						float red = mix(colors.glowR * dry, BRUISE_R, bruise);
						float green = mix(colors.glowG * dry, BRUISE_G, bruise);
						float blue = mix(colors.glowB * dry, BRUISE_B, bruise);
						// A soft inflamed bleed into the tissue, then a dark groove hugging the vessel's walls.
						RiteQuads.fadedBandPiece(vc, mat, dive1, dive2,
								cos1, sin1, y1, r1 - halfCore1 - glowWidth, r1 - halfCore1,
								cos2, sin2, y2, r2 - halfCore2 - glowWidth, r2 - halfCore2,
								red, green, blue, 0.0F, red, green, blue, alpha);
						RiteQuads.fadedBandPiece(vc, mat, dive1, dive2,
								cos1, sin1, y1, r1 + halfCore1, r1 + halfCore1 + glowWidth,
								cos2, sin2, y2, r2 + halfCore2, r2 + halfCore2 + glowWidth,
								red, green, blue, alpha, red, green, blue, 0.0F);
						float groove = grooveAlpha * pieceAlpha;
						float groove1 = GROOVE_WIDTH * waveScale1 * widthScale;
						float groove2 = GROOVE_WIDTH * waveScale2 * widthScale;
						RiteQuads.fadedBandPiece(vc, mat, dive1, dive2,
								cos1, sin1, y1, r1 - halfCore1 - groove1, r1 - halfCore1,
								cos2, sin2, y2, r2 - halfCore2 - groove2, r2 - halfCore2,
								BRUISE_R, BRUISE_G, BRUISE_B, 0.0F, BRUISE_R, BRUISE_G, BRUISE_B, groove);
						RiteQuads.fadedBandPiece(vc, mat, dive1, dive2,
								cos1, sin1, y1, r1 + halfCore1, r1 + halfCore1 + groove1,
								cos2, sin2, y2, r2 + halfCore2, r2 + halfCore2 + groove2,
								BRUISE_R, BRUISE_G, BRUISE_B, groove, BRUISE_R, BRUISE_G, BRUISE_B, 0.0F);
					}
					case CORE -> {
						// A vessel in cross-section: bright walls around a darker lumen, with the arterial
						// pulse brightening the blood as it passes.
						float alpha = coreAlpha * pieceAlpha;
						// Walls stay close to the lumen so the vessel reads as blood under skin, not a lit tube.
						float wallR = mix(Math.min(1.0F, colors.rimR * dry * 0.82F + arterial * 0.20F), BRUISE_R, bruise);
						float wallG = mix(colors.rimG * dry * 0.82F + arterial * 0.03F, BRUISE_G, bruise);
						float wallB = mix(colors.rimB * dry * 0.82F + arterial * 0.02F, BRUISE_B, bruise);
						float lumenR = mix(Math.min(1.0F, colors.coreR * dry * 0.78F + arterial * 0.34F),
								BRUISE_R * 0.6F, bruise);
						float lumenG = mix(colors.coreG * dry * 0.7F + arterial * 0.018F, BRUISE_G * 0.6F, bruise);
						float lumenB = mix(colors.coreB * dry * 0.8F + arterial * 0.012F, BRUISE_B * 0.6F, bruise);
						RiteQuads.fadedBandPiece(vc, mat, dive1, dive2,
								cos1, sin1, y1, r1 - halfCore1, r1,
								cos2, sin2, y2, r2 - halfCore2, r2,
								wallR, wallG, wallB, alpha, lumenR, lumenG, lumenB, alpha);
						RiteQuads.fadedBandPiece(vc, mat, dive1, dive2,
								cos1, sin1, y1, r1, r1 + halfCore1,
								cos2, sin2, y2, r2, r2 + halfCore2,
								lumenR, lumenG, lumenB, alpha, wallR, wallG, wallB, alpha);
					}
					case BLOOM -> {
						float alpha = (bloomAlpha + arterial * 0.22F) * pieceAlpha * (1.0F - bruise);
						if (alpha <= 0.004F) continue;
						float hot = CardinalRiteMath.clamp01(frame.flash * 0.45F + arterial * 0.25F);
						float red = mix(colors.rimR, HOT_R, hot);
						float green = mix(colors.rimG, HOT_G * 0.4F, hot);
						float blue = mix(colors.rimB, HOT_B * 0.4F, hot);
						// Hug the vessel walls and leave the lumen dark, so the swell reads as a rim light.
						float bloomWidth = bloomWidthBase * widthScale;
						float wall1 = halfCore1 * 0.4F;
						float wall2 = halfCore2 * 0.4F;
						RiteQuads.fadedBandPiece(vc, mat, dive1, dive2,
								cos1, sin1, y1 + BLOOM_LIFT, r1 - halfCore1 - bloomWidth, r1 - wall1,
								cos2, sin2, y2 + BLOOM_LIFT, r2 - halfCore2 - bloomWidth, r2 - wall2,
								red, green, blue, 0.0F, red, green, blue, alpha);
						RiteQuads.fadedBandPiece(vc, mat, dive1, dive2,
								cos1, sin1, y1 + BLOOM_LIFT, r1 + wall1, r1 + halfCore1 + bloomWidth,
								cos2, sin2, y2 + BLOOM_LIFT, r2 + wall2, r2 + halfCore2 + bloomWidth,
								red, green, blue, alpha, red, green, blue, 0.0F);
					}
				}
			}
		}
	}

	// ── Floor stain ──

	private static void drawBoundaryFloorStain(VertexConsumer vc, RiteFrame frame, RingFrame ring) {
		float[] mesh = frame.layout.stainMesh(ring.index);
		if (mesh.length == 0 || frame.stainOpacity <= 0.001F) return;
		float alpha = CardinalRiteBoundaryGeometry.boundaryStainAlpha(ring.index) * frame.stainOpacity;
		float y = CardinalRiteBoundaryGeometry.boundaryStainSurfaceOffset();
		Matrix4f mat = frame.baseMatrix;
		int stride = CardinalRiteCeremonyLayout.STAIN_VERTEX_STRIDE;
		for (int vertex = 0; vertex + stride <= mesh.length; vertex += stride) {
			boolean outer = mesh[vertex + 3] > 0.5F;
			vc.addVertex(mat, mesh[vertex], y, mesh[vertex + 1])
					.setColor(outer ? 0.008F : 0.006F, 0.0F, outer ? 0.012F : 0.010F, alpha * mesh[vertex + 2]);
		}
	}

	// ── Veins ──

	private static void drawVeins(VertexConsumer vc, Pass pass, RiteFrame frame, RingFrame ring, float time) {
		if (pass == Pass.BLOOM || ring.arcs.isEmpty() || ring.opacity <= 0.001F) return;
		float veinFade = CardinalRiteEndingRules.violent(frame.ending)
				? 1.0F - CardinalRiteMath.smoothstep(frame.endingAge / 16.0F) : 1.0F;
		float coreAlpha = (0.70F + frame.heartbeat * 0.18F) * ring.ringFade * ring.opacity * frame.dim * veinFade;
		if (coreAlpha <= 0.004F) return;
		float directionSign = ring.directionSign;
		int ringSeed = frame.entry.getCenter().hashCode() * 31 + ring.index;
		for (int v = 0; v < VEIN_COUNT; v++) {
			// Capillaries root at fixed, uneven points and only sway, so nothing crawls round the ring like teeth.
			float jitter = CardinalRiteEndingRules.hash(ringSeed, v);
			double baseAngle = (Math.PI * 2.0D / VEIN_COUNT) * (v + jitter * 0.7D);
			if (!frame.legacy && CardinalRiteBoundaryGeometry.endpointWaveScale(
					baseAngle, ring.endpoints, VEIN_SORE_CLEARANCE) < 0.999F) continue;
			if (!frame.legacy && !CardinalRiteBoundaryGeometry.hasVisibleBeamAt(ring.arcs, baseAngle)) continue;
			float rootIntegrity = frame.legacy ? 1.0F : arcIntegrity(ring.arcs, baseAngle, time);
			if (rootIntegrity <= 0.01F) continue;
			float surfaceY = CardinalRiteBoundaryGeometry.veinSurfaceOffset(
					surfaceWave(baseAngle, time, directionSign, 0.0D, RING_SURFACE_WAVE));
			float rootRadius = ring.radius + undulation(baseAngle, time, directionSign);
			float side = (v & 1) == 0 ? -1.0F : 1.0F;
			float halfCore = CORE_WIDTH * 0.5F;
			float length = VEIN_LENGTH * (0.55F + 0.45F * CardinalRiteEndingRules.hash(ringSeed, v + 101));
			double veinPulse = (Math.sin(time * 0.1D + v * 1.7D) + 1.0D) * 0.5D;
			float alpha = (float) (0.35D + 0.35D * veinPulse) * rootIntegrity * coreAlpha;
			float phase = jitter * 40.0F;
			drawCapillary(vc, pass, ring, rootRadius + side * halfCore * 0.6F, baseAngle, side, length,
					VEIN_ROOT_WIDTH, alpha, phase, time, surfaceY, veinFade);
			// Most capillaries fork once partway along, the branch peeling off to one side.
			if (CardinalRiteEndingRules.hash(ringSeed, v + 211) > 0.3F) {
				float forkAt = 0.35F + 0.2F * CardinalRiteEndingRules.hash(ringSeed, v + 307);
				float forkSign = CardinalRiteEndingRules.hash(ringSeed, v + 401) > 0.5F ? 1.0F : -1.0F;
				double[] fork = capillaryPoint(rootRadius + side * halfCore * 0.6F, baseAngle, side, length,
						forkAt, phase, time);
				drawCapillary(vc, pass, ring, (float) fork[0], fork[1] + forkSign * 0.04D / Math.max(1.0D, fork[0]),
						side, length * 0.5F, VEIN_ROOT_WIDTH * (1.0F - forkAt * 0.7F) * 0.8F,
						alpha * 0.8F, phase + forkSign * 9.0F, time, surfaceY, veinFade);
			}
		}
	}

	/** A point along a capillary: its radius and angle {@code t} of the way out from its root. */
	private static double[] capillaryPoint(float rootRadius, double baseAngle, float side, float length, float t,
			float phase, float time) {
		double radius = rootRadius + side * t * length;
		// Lateral wander in blocks, turned into an angle at this radius; a slow sway keeps it alive.
		double wander = (Math.sin(phase + t * 5.3D) * 0.09D + Math.sin(phase * 1.7D + t * 11.0D) * 0.025D) * t
				+ Math.sin(time * 0.03D + phase) * 0.02D * t;
		return new double[] {radius, baseAngle + wander / Math.max(0.5D, radius)};
	}

	/** A thin capillary threading off the vessel into the floor, fading out rather than ending in a point. */
	private static void drawCapillary(VertexConsumer vc, Pass pass, RingFrame ring, float rootRadius,
			double baseAngle, float side, float length, float rootWidth, float alpha, float phase, float time,
			float surfaceY, float veinFade) {
		RingColors colors = ring.colors;
		Matrix4f mat = ring.matrix;
		double[] previous = capillaryPoint(rootRadius, baseAngle, side, length, 0.0F, phase, time);
		for (int s = 0; s < VEIN_SEGS; s++) {
			float t0 = (float) s / VEIN_SEGS;
			float t1 = (float) (s + 1) / VEIN_SEGS;
			double[] next = capillaryPoint(rootRadius, baseAngle, side, length, t1, phase, time);
			float rad0 = (float) previous[0];
			float rad1 = (float) next[0];
			float cos0 = (float) Math.cos(previous[1]);
			float sin0 = (float) Math.sin(previous[1]);
			float cos1 = (float) Math.cos(next[1]);
			float sin1 = (float) Math.sin(next[1]);
			previous = next;
			float w0 = rootWidth * (1.0F - t0 * 0.7F);
			float w1 = rootWidth * (1.0F - t1 * 0.7F);
			float a0 = alpha * (1.0F - CardinalRiteMath.smoothstep(t0));
			float a1 = alpha * (1.0F - CardinalRiteMath.smoothstep(t1));
			// Width runs along the ring's tangent at each end.
			float px0 = -sin0;
			float pz0 = cos0;
			float px1 = -sin1;
			float pz1 = cos1;
			if (pass == Pass.CORE) {
				float cR = colors.coreR * 0.8F;
				float cG = colors.coreG * 0.8F;
				float cB = colors.coreB * 0.8F;
				RiteQuads.emitQuad(vc, mat,
						cos0 * rad0 - px0 * w0, surfaceY, sin0 * rad0 - pz0 * w0, cR, cG, cB, a0,
						cos0 * rad0 + px0 * w0, surfaceY, sin0 * rad0 + pz0 * w0, cR, cG, cB, a0,
						cos1 * rad1 + px1 * w1, surfaceY, sin1 * rad1 + pz1 * w1, cR, cG, cB, a1,
						cos1 * rad1 - px1 * w1, surfaceY, sin1 * rad1 - pz1 * w1, cR, cG, cB, a1);
			} else {
				// A faint flush either side, as if the blood shows through the tissue around it.
				float gR = colors.glowR;
				float gG = colors.glowG;
				float gB = colors.glowB;
				float gw0 = w0 * 3.0F;
				float gw1 = w1 * 3.0F;
				float ga0 = a0 * 0.22F * veinFade;
				float ga1 = a1 * 0.22F * veinFade;
				RiteQuads.emitQuad(vc, mat,
						cos0 * rad0 - px0 * gw0, surfaceY, sin0 * rad0 - pz0 * gw0, gR, gG, gB, 0.0F,
						cos0 * rad0 - px0 * w0, surfaceY, sin0 * rad0 - pz0 * w0, gR, gG, gB, ga0,
						cos1 * rad1 - px1 * w1, surfaceY, sin1 * rad1 - pz1 * w1, gR, gG, gB, ga1,
						cos1 * rad1 - px1 * gw1, surfaceY, sin1 * rad1 - pz1 * gw1, gR, gG, gB, 0.0F);
				RiteQuads.emitQuad(vc, mat,
						cos0 * rad0 + px0 * w0, surfaceY, sin0 * rad0 + pz0 * w0, gR, gG, gB, ga0,
						cos0 * rad0 + px0 * gw0, surfaceY, sin0 * rad0 + pz0 * gw0, gR, gG, gB, 0.0F,
						cos1 * rad1 + px1 * gw1, surfaceY, sin1 * rad1 + pz1 * gw1, gR, gG, gB, 0.0F,
						cos1 * rad1 + px1 * w1, surfaceY, sin1 * rad1 + pz1 * w1, gR, gG, gB, ga1);
			}
		}
	}

	// ── Anchor sockets ──

	// ── Anchor beats: filled pulse and bolus, rupture, knitting ──

	private static void drawAnchorBeats(VertexConsumer vc, Pass pass, RiteFrame frame, RingFrame ring) {
		if (frame.beats.isEmpty()) return;
		List<CardinalRiteCeremonyDefinition.Anchor> anchors = frame.layout.anchors();
		for (AnchorBeat beat : frame.beats) {
			if (beat.index() < 0 || beat.index() >= anchors.size()) continue;
			CardinalRiteCeremonyDefinition.Anchor anchor = anchors.get(beat.index());
			if (Math.max(0, anchor.ring()) != ring.index) continue;
			float x = anchor.x();
			float z = anchor.z();
			switch (beat.kind()) {
				case ANCHOR_FILLED -> drawFilledPulse(vc, pass, frame, ring, beat.index(), x, z, beat.age());
				case ANCHOR_BROKEN -> drawRupture(vc, pass, ring, beat.index(), x, z, beat.age());
				case ANCHOR_REPAIRED -> drawKnit(vc, pass, ring, x, z, beat.age());
				default -> {
				}
			}
		}
	}

	/** A bright bead runs once round the socket, a flash ring opens, then a bolus races along both arcs. */
	private static void drawFilledPulse(VertexConsumer vc, Pass pass, RiteFrame frame, RingFrame ring,
			int anchorIndex, float x, float z, float age) {
		Matrix4f mat = ring.matrix;
		RingColors colors = ring.colors;
		float y = EFFECT_Y + (pass == Pass.BLOOM ? BLOOM_LIFT : 0.0F);
		if (age < 12.0F) {
			float lap = CardinalRiteMath.easeOutCubic(age / 10.0F);
			float alpha = 1.0F - CardinalRiteMath.smoothstep((age - 7.0F) / 5.0F);
			for (int trail = 0; trail < 6; trail++) {
				double angle = -Math.PI * 0.5D + (lap - trail * 0.035F) * Math.PI * 2.0D;
				float bx = x + (float) Math.cos(angle) * SOCKET_RADIUS;
				float bz = z + (float) Math.sin(angle) * SOCKET_RADIUS;
				float trailAlpha = alpha * (1.0F - trail / 6.0F);
				switch (pass) {
					case GLOW -> RiteQuads.disc(vc, mat, bx, bz, y, 0.16F, colors.glowR, colors.glowG, colors.glowB,
							trailAlpha * 0.4F);
					case CORE -> RiteQuads.disc(vc, mat, bx, bz, y + 0.002F, 0.05F - trail * 0.006F,
							HOT_R, HOT_G * 0.55F, HOT_B * 0.5F, trailAlpha * 0.95F);
					case BLOOM -> RiteQuads.disc(vc, mat, bx, bz, y, 0.12F, HOT_R, HOT_G * 0.4F, HOT_B * 0.4F,
							trailAlpha * 0.8F);
				}
			}
			if (pass != Pass.CORE) {
				float ringAlpha = CardinalRiteMath.pulse(age, 1.0F, 12.0F);
				float radius = SOCKET_RADIUS + CardinalRiteMath.easeOutCubic(age / 10.0F) * 0.5F;
				RiteQuads.flashRing(vc, mat, x, z, y, radius, 0.16F, colors.rimR, colors.rimG, colors.rimB,
						ringAlpha * (pass == Pass.BLOOM ? 0.9F : 0.45F));
			}
		}
		if (age >= 4.0F) {
			float progress = CardinalRiteMath.smoothstep((age - 4.0F) / (FILL_BEAT_TICKS - 4.0F));
			float bolusAlpha = (float) Math.sin(progress * Math.PI);
			double anchorAngle = frame.layout.anchorAngle(anchorIndex);
			double forward = anchorAngle + frame.layout.forwardSweep(anchorIndex) * progress;
			double backward = anchorAngle - frame.layout.backwardSweep(anchorIndex) * progress;
			drawBolus(vc, pass, ring, forward, bolusAlpha, y);
			drawBolus(vc, pass, ring, backward, bolusAlpha, y);
		}
	}

	private static void drawBolus(VertexConsumer vc, Pass pass, RingFrame ring, double angle, float alpha, float y) {
		if (alpha <= 0.004F || !CardinalRiteBoundaryGeometry.hasVisibleBeamAt(ring.arcs, angle)) return;
		float x = (float) Math.cos(angle) * ring.radius;
		float z = (float) Math.sin(angle) * ring.radius;
		switch (pass) {
			case GLOW -> RiteQuads.disc(vc, ring.matrix, x, z, y, 0.17F, 1.0F, 0.075F, 0.035F, alpha * 0.30F);
			case CORE -> RiteQuads.disc(vc, ring.matrix, x, z, y + 0.002F, 0.055F, 1.0F, 0.075F, 0.035F,
					alpha * 0.92F);
			case BLOOM -> RiteQuads.disc(vc, ring.matrix, x, z, y, 0.14F, HOT_R, HOT_G * 0.35F, HOT_B * 0.3F,
					alpha * 0.7F);
		}
	}

	/** The socket bursts: fragments of its wall fling outward and bruise, over a dark spreading stain. */
	private static void drawRupture(VertexConsumer vc, Pass pass, RingFrame ring, int anchorIndex,
			float x, float z, float age) {
		Matrix4f mat = ring.matrix;
		RingColors colors = ring.colors;
		float t = CardinalRiteMath.clamp01(age / BREAK_BEAT_TICKS);
		if (pass == Pass.GLOW) {
			float stain = CardinalRiteMath.pulse(age, 3.0F, BREAK_BEAT_TICKS);
			RiteQuads.disc(vc, mat, x, z, EFFECT_Y - 0.006F, 0.45F + t * 0.45F,
					BRUISE_R * 0.5F, BRUISE_G * 0.5F, BRUISE_B * 0.5F, stain * 0.55F);
		}
		if (pass == Pass.BLOOM) {
			float flash = CardinalRiteMath.pulse(age, 0.5F, 6.0F);
			RiteQuads.disc(vc, mat, x, z, EFFECT_Y + BLOOM_LIFT, 0.55F, HOT_R, HOT_G * 0.3F, HOT_B * 0.3F,
					flash * 0.9F);
		}
		int fragments = 8;
		double fragmentSweep = Math.PI * 2.0D / fragments * 0.7D;
		float fling = CardinalRiteMath.easeOutCubic(age / 16.0F);
		float bruise = CardinalRiteMath.smoothstep(t * 1.6F);
		float alpha = 1.0F - CardinalRiteMath.smoothstep((t - 0.25F) / 0.75F);
		if (alpha <= 0.004F) return;
		for (int fragment = 0; fragment < fragments; fragment++) {
			float jitter = CardinalRiteEndingRules.hash(anchorIndex * 17 + fragment, 91);
			double center = Math.PI * 2.0D * fragment / fragments + jitter * 0.5D;
			double spin = (jitter - 0.5D) * 1.4D * fling;
			double a1 = center - fragmentSweep * 0.5D + spin;
			double a2 = center + fragmentSweep * 0.5D + spin;
			float radius = SOCKET_RADIUS + fling * (0.45F + jitter * 0.7F);
			float lift = EFFECT_Y + (float) Math.sin(Math.min(1.0F, age / 16.0F) * Math.PI) * 0.14F * (0.5F + jitter);
			float width = SOCKET_CORE_WIDTH * (1.4F - t);
			float cos1 = (float) Math.cos(a1);
			float sin1 = (float) Math.sin(a1);
			float cos2 = (float) Math.cos(a2);
			float sin2 = (float) Math.sin(a2);
			float red = mix(colors.glowR, BRUISE_R, bruise);
			float green = mix(colors.glowG, BRUISE_G, bruise);
			float blue = mix(colors.glowB, BRUISE_B, bruise);
			switch (pass) {
				case GLOW -> RiteQuads.bandPiece(vc, mat, x, z,
						cos1, sin1, lift, radius - width * 2.5F, radius + width * 2.5F,
						cos2, sin2, lift, radius - width * 2.5F, radius + width * 2.5F,
						red, green, blue, alpha * 0.25F, red, green, blue, 0.0F);
				case CORE -> RiteQuads.bandPiece(vc, mat, x, z,
						cos1, sin1, lift, radius - width, radius + width,
						cos2, sin2, lift, radius - width, radius + width,
						mix(colors.rimR, BRUISE_R, bruise), mix(colors.rimG, BRUISE_G, bruise),
						mix(colors.rimB, BRUISE_B, bruise), alpha,
						red * 0.7F, green * 0.7F, blue * 0.7F, alpha);
				case BLOOM -> {
					float hot = alpha * (1.0F - bruise) * 0.6F;
					if (hot > 0.004F) {
						RiteQuads.bandPiece(vc, mat, x, z,
								cos1, sin1, lift + BLOOM_LIFT, radius - width * 2.0F, radius + width * 2.0F,
								cos2, sin2, lift + BLOOM_LIFT, radius - width * 2.0F, radius + width * 2.0F,
								colors.rimR, colors.rimG, colors.rimB, hot, colors.rimR, colors.rimG, colors.rimB, 0.0F);
					}
				}
			}
		}
	}

	/** A bright ring contracts onto the socket as it knits shut. */
	private static void drawKnit(VertexConsumer vc, Pass pass, RingFrame ring, float x, float z, float age) {
		float alpha = CardinalRiteMath.pulse(age, 2.0F, REPAIR_BEAT_TICKS);
		if (alpha <= 0.004F) return;
		float radius = CardinalRiteMath.lerp(CardinalRiteMath.easeInCubic(age / REPAIR_BEAT_TICKS),
				SOCKET_RADIUS * 2.6F, SOCKET_RADIUS);
		RingColors colors = ring.colors;
		switch (pass) {
			case GLOW -> RiteQuads.flashRing(vc, ring.matrix, x, z, EFFECT_Y, radius, 0.22F,
					colors.glowR, colors.glowG, colors.glowB, alpha * 0.5F);
			case CORE -> RiteQuads.flashRing(vc, ring.matrix, x, z, EFFECT_Y + 0.002F, radius, 0.035F,
					HOT_R, HOT_G * 0.7F, HOT_B * 0.65F, alpha);
			case BLOOM -> RiteQuads.flashRing(vc, ring.matrix, x, z, EFFECT_Y + BLOOM_LIFT, radius, 0.18F,
					HOT_R, HOT_G * 0.45F, HOT_B * 0.4F, alpha * 0.9F);
		}
	}

	// ── Segment seals (growth completed) ──

	private static void drawSegmentSeals(VertexConsumer vc, Pass pass, RiteFrame frame, float partialTick) {
		for (CardinalRiteBoundaryProgress.Segment segment : frame.entry.getBoundarySegments()) {
			float effectAge = frame.entry.boundaryEffectAge(segment, partialTick);
			if (effectAge < 0.0F || effectAge > 17.0F) continue;
			if (segment.ring() < 0 || segment.ring() >= frame.rings.length) continue;
			RingFrame ring = frame.rings[segment.ring()];
			List<CardinalRiteBoundaryProgress.Segment> clipped = CardinalRiteBoundaryGeometry.animatedSocketArcs(
					segment, 1.0F, frame.layout.startClearance(segment), frame.layout.endClearance(segment));
			if (clipped.isEmpty()) continue;
			CardinalRiteBoundaryProgress.Segment arc = clipped.getFirst();
			double midpoint = arc.startAngle() + arc.sweepAngle() * 0.5D;
			double halfSweep = arc.sweepAngle() * 0.5D;
			Matrix4f mat = ring.matrix;
			float sealAlpha = CardinalRiteBoundaryGeometry.sealPulseAlpha(effectAge);
			if (sealAlpha > 0.0F) {
				float sealTravel = CardinalRiteBoundaryGeometry.sealTravel(effectAge);
				float midpointX = (float) Math.cos(midpoint) * ring.radius;
				float midpointZ = (float) Math.sin(midpoint) * ring.radius;
				float sealRadius = 0.10F + sealTravel * 0.16F;
				switch (pass) {
					case GLOW -> RiteQuads.flashRing(vc, mat, midpointX, midpointZ, 0.020F,
							sealRadius, 0.085F, 0.94F, 0.035F, 0.025F, sealAlpha * 0.44F);
					case CORE -> RiteQuads.flashRing(vc, mat, midpointX, midpointZ, 0.022F,
							sealRadius, 0.028F, 0.72F, 0.012F, 0.014F, sealAlpha * 0.92F);
					case BLOOM -> RiteQuads.flashRing(vc, mat, midpointX, midpointZ, 0.022F + BLOOM_LIFT,
							sealRadius, 0.14F, HOT_R, HOT_G * 0.4F, HOT_B * 0.4F, sealAlpha * 0.7F);
				}
				drawBoundaryEffectPair(vc, pass, mat, ring.radius, midpoint - halfSweep * sealTravel,
						midpoint + halfSweep * sealTravel, 0.065F, sealAlpha * 0.68F, 0.78F, 0.018F, 0.02F);
			}
			float bolusAlpha = CardinalRiteBoundaryGeometry.bolusAlpha(effectAge);
			if (bolusAlpha > 0.0F) {
				float bolusProgress = CardinalRiteBoundaryGeometry.bolusProgress(effectAge);
				drawBoundaryEffectPair(vc, pass, mat, ring.radius, midpoint - halfSweep * bolusProgress,
						midpoint + halfSweep * bolusProgress, 0.047F, bolusAlpha, 1.0F, 0.075F, 0.035F);
			}
		}
	}

	private static void drawBoundaryEffectPair(VertexConsumer vc, Pass pass, Matrix4f mat, float ringRadius,
			double firstAngle, double secondAngle, float coreRadius, float alpha,
			float red, float green, float blue) {
		drawBoundaryEffect(vc, pass, mat, ringRadius, firstAngle, coreRadius, alpha, red, green, blue);
		drawBoundaryEffect(vc, pass, mat, ringRadius, secondAngle, coreRadius, alpha, red, green, blue);
	}

	private static void drawBoundaryEffect(VertexConsumer vc, Pass pass, Matrix4f mat, float ringRadius,
			double angle, float coreRadius, float alpha, float red, float green, float blue) {
		float x = (float) Math.cos(angle) * ringRadius;
		float z = (float) Math.sin(angle) * ringRadius;
		switch (pass) {
			case GLOW -> RiteQuads.disc(vc, mat, x, z, 0.024F, coreRadius * 3.1F, red, green, blue, alpha * 0.28F);
			case CORE -> RiteQuads.disc(vc, mat, x, z, 0.026F, coreRadius, red, green, blue, alpha * 0.92F);
			case BLOOM -> RiteQuads.disc(vc, mat, x, z, 0.026F + BLOOM_LIFT, coreRadius * 2.4F,
					red, green * 0.6F, blue * 0.6F, alpha * 0.55F);
		}
	}

	// ── Finale flash and the staff's impact shock ──

	private static void drawFinaleFlash(VertexConsumer vc, Pass pass, RiteFrame frame, float partialTick) {
		if (pass == Pass.CORE || !frame.entry.isPhase(CardinalRitePhase.CULMINATION)) return;
		float alpha = CardinalRiteCompletionRingTheatrics.flashAlpha(frame.entry.phaseRenderTicks(partialTick))
				* (frame.ending == null ? 1.0F : frame.fade);
		if (alpha <= 0.001F) return;
		Matrix4f mat = frame.baseMatrix;
		if (pass == Pass.GLOW) {
			RiteQuads.disc(vc, mat, 0.0F, 0.0F, 1.35F, 1.25F + alpha * 1.6F, 1.0F, 0.18F, 0.14F, alpha * 0.72F);
			RiteQuads.flashRing(vc, mat, 0.0F, 0.0F, 1.38F, 0.65F + alpha * 1.8F, 0.28F, 1.0F, 0.55F, 0.48F, alpha);
		} else {
			RiteQuads.disc(vc, mat, 0.0F, 0.0F, 1.36F, 0.9F + alpha * 1.2F, HOT_R, HOT_G * 0.5F, HOT_B * 0.45F,
					alpha * 0.8F);
		}
	}

	private static void drawImpactShock(VertexConsumer vc, Pass pass, RiteFrame frame) {
		if (pass == Pass.CORE || frame.impactAge < 0.0F || frame.rings.length == 0) return;
		float t = frame.impactAge / IMPACT_TICKS;
		float outer = frame.rings[frame.rings.length - 1].radius + 0.6F;
		float radius = 0.45F + CardinalRiteMath.easeOutCubic(t) * outer;
		float alpha = (1.0F - t) * (1.0F - t);
		RingColors colors = frame.rings[0].colors;
		if (pass == Pass.GLOW) {
			RiteQuads.flashRing(vc, frame.baseMatrix, 0.0F, 0.0F, EFFECT_Y, radius, 0.6F,
					colors.glowR, colors.glowG, colors.glowB, alpha * 0.35F);
		} else {
			RiteQuads.flashRing(vc, frame.baseMatrix, 0.0F, 0.0F, EFFECT_Y + BLOOM_LIFT, radius, 0.32F,
					HOT_R, HOT_G * 0.45F, HOT_B * 0.4F, alpha * 0.85F);
		}
	}

	// ── Sigils, landmarks and staff tendrils (world space) ──

	private static void drawSigilSegments(PoseStack stack, VertexConsumer consumer, RiteFrame frame,
			float currentTime, Vec3 cam, boolean glow) {
		ActiveRiteClientData.RiteEntry rite = frame.entry;
		if (rite.getSigilSegments().isEmpty() || frame.fade <= 0.001F) return;
		float beat = frame.heartbeat;
		float halfWidth = glow ? 0.18F : 0.055F;
		float alpha = (glow ? 0.20F + beat * 0.20F : 0.72F + beat * 0.24F) * frame.fade;
		Matrix4f matrix = stack.last().pose();
		IchorianSigilRenderPalette.Color vesselColor = IchorianSigilRenderPalette.vessel(glow);
		List<IchorianSigilOrganicGeometry.Sample> samples = new ArrayList<>(SIGIL_VESSEL_SEGMENTS + 1);
		for (ActiveRiteClientData.SigilSegment segment : rite.getSigilSegments()) {
			long seed = Double.doubleToLongBits(segment.startX())
					^ Long.rotateLeft(Double.doubleToLongBits(segment.startZ()), 17)
					^ Long.rotateLeft(Double.doubleToLongBits(segment.endX()), 31)
					^ segment.color();
			samples.clear();
			for (int step = 0; step <= SIGIL_VESSEL_SEGMENTS; step++) {
				samples.add(IchorianSigilOrganicGeometry.sample(
						segment.startX(), segment.startY(), segment.startZ(),
						segment.endX(), segment.endY(), segment.endZ(),
						currentTime, seed, step, SIGIL_VESSEL_SEGMENTS, halfWidth));
			}
			for (IchorianSigilOrganicGeometry.RibbonSegment vessel
					: IchorianSigilOrganicGeometry.ribbonSegments(samples)) {
				drawOrganicSigilSection(consumer, matrix, vessel, cam,
						vesselColor.red(), vesselColor.green(), vesselColor.blue(), alpha, glow);
			}
		}
	}

	private static void drawOrganicSigilSection(VertexConsumer consumer, Matrix4f matrix,
			IchorianSigilOrganicGeometry.RibbonSegment segment,
			Vec3 cam, float red, float green, float blue, float alpha, boolean glow) {
		IchorianSigilOrganicGeometry.RibbonJoint start = segment.start();
		IchorianSigilOrganicGeometry.RibbonJoint end = segment.end();
		float startRed = Math.min(1.0F, red * start.redIntensity());
		float endRed = Math.min(1.0F, red * end.redIntensity());
		float startY = (float) (start.centerY() - cam.y);
		float endY = (float) (end.centerY() - cam.y);
		RiteQuads.emitQuad(consumer, matrix,
				(float) (start.leftX() - cam.x), startY,
				(float) (start.leftZ() - cam.z), startRed, green, blue, glow ? 0.0F : alpha,
				(float) (start.rightX() - cam.x), startY,
				(float) (start.rightZ() - cam.z), startRed, green, blue, alpha,
				(float) (end.rightX() - cam.x), endY,
				(float) (end.rightZ() - cam.z), endRed, green, blue, alpha,
				(float) (end.leftX() - cam.x), endY,
				(float) (end.leftZ() - cam.z), endRed, green, blue, glow ? 0.0F : alpha);
	}

	private static void drawSanguineBlobs(PoseStack stack, VertexConsumer consumer, RiteFrame frame,
			float currentTime, Vec3 cam, boolean glow) {
		if (frame.fade <= 0.001F) return;
		float partialTick = currentTime - (float) Math.floor(currentTime);
		// On a consecrated floor the anchors are sores in it; an orb there would sit on the pit and clip the floor.
		boolean sored = AnchorSores.opened(frame.entry.getCenter(), (long) Math.floor(currentTime));
		for (ActiveRiteClientData.SanguineBlob blob : frame.entry.getSanguineBlobs()) {
			boolean boundaryAnchor = blob.kind() == ActiveRiteClientData.NodeKind.BOUNDARY_ANCHOR;
			if (boundaryAnchor && sored) continue;
			float radius = CardinalRiteBoundaryGeometry.landmarkRenderRadius(
					blob.renderRadius(partialTick), boundaryAnchor, glow) * frame.fade;
			if (radius <= 0.001F) continue;
			stack.pushPose();
			stack.translate(blob.x() - cam.x, blob.y() - cam.y, blob.z() - cam.z);
			var recipe = boundaryAnchor
					? IchorianSigilLandmarkGeometry.boundaryAnchor(blob.seed())
					: IchorianSigilLandmarkGeometry.forRole(blob.role(), blob.seed());
			IchorianSigilLandmarkRenderer.render(
					consumer, stack, recipe, radius, blob.color(), currentTime,
					blob.seed(), blob.integrity(), glow, true);
			stack.popPose();
		}
	}

	private static void drawStaffTendrils(PoseStack poseStack, VertexConsumer consumer, RiteFrame frame,
			float currentTime, float partialTick, Vec3 camera, boolean glowPass) {
		ActiveRiteClientData.RiteEntry rite = frame.entry;
		if (!rite.hasPlantedStaff()) return;
		float visibilityProgress = CardinalRiteStaffTendrilGeometry.visibilityProgress(
				rite.staffPlantingRenderTicks(partialTick), staffMeltTicks(frame, partialTick));
		CardinalRiteStaffTendrilRenderer.render(poseStack, consumer, rite.getCenter(),
				currentTime, camera, visibilityProgress, glowPass);
	}

	private static float staffMeltTicks(RiteFrame frame, float partialTick) {
		return CardinalRiteEndingRules.staffMeltTicks(frame.ending, frame.endingAge,
				frame.entry.cancellationRenderTicks(partialTick), CardinalRiteCancellationRules.TOTAL_TICKS);
	}

	// ── Planted staff ──

	private static void drawPlantedStaff(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
			RiteFrame frame, Vec3 camera, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		ActiveRiteClientData.RiteEntry rite = frame.entry;
		CardinalRitePlantedStaffGeometry.AbsorptionPose plantedPose =
				CardinalRitePlantedStaffGeometry.absorptionPose(rite.getCenter(), staffMeltTicks(frame, partialTick));
		if (plantedPose.scale() <= 0.001F || plantedPose.opacity() <= 0.001F) return;
		poseStack.pushPose();
		poseStack.translate(
				plantedPose.position().x - camera.x,
				plantedPose.position().y - camera.y,
				plantedPose.position().z - camera.z);
		if (plantedPose.pitchDegrees() != 0.0F) {
			poseStack.mulPose(Axis.XP.rotationDegrees(plantedPose.pitchDegrees()));
		}
		poseStack.scale(plantedPose.scale(), plantedPose.scale(), plantedPose.scale());
		ItemStack staff = plantedStaffStack();
		// The rite owner's fitting, not the viewer's.
		BakedModel model = mc.getModelManager().getModel(staffModel(staffVisual(mc, rite.getOwner())));
		model = ClientHooks.handleCameraTransforms(poseStack, model, ItemDisplayContext.FIXED, false);
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		CardinalRitePlantedStaffGeometry.MeltStyle melt = plantedPose.melt();
		RenderType staffRenderType;
		if (melt.active()) {
			staffRenderType = HemoRenderTypes.cardinalStaffBloodMelt(
					(float) (mc.level.getGameTime() + partialTick),
					melt.seed(), melt.wiggleAmplitude(),
					(float) (melt.poolCenter().x - camera.x),
					(float) (melt.poolCenter().y - camera.y),
					(float) (melt.poolCenter().z - camera.z),
					melt.shaderProgress(),
					(float) (melt.poolCenter().y - camera.y),
					melt.height());
		} else {
			staffRenderType = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
		}
		VertexConsumer fadingConsumer = new CardinalRiteFadingVertexConsumer(
				buffer.getBuffer(staffRenderType), plantedPose.opacity());
		for (BakedModel renderPass : model.getRenderPasses(staff, true)) {
			mc.getItemRenderer().renderModelLists(renderPass, staff,
					LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
					poseStack, fadingConsumer);
		}
		buffer.endBatch(staffRenderType);
		poseStack.popPose();
	}

	private static ItemStack plantedStaffStack() {
		if (plantedStaffStack == null) plantedStaffStack = new ItemStack(ItemInit.living_staff.get());
		return plantedStaffStack;
	}

	private static int staffVisual(Minecraft mc, UUID owner) {
		if (owner == null || mc.level == null) return 0;
		Player player = mc.level.getPlayerByUUID(owner);
		if (player == null) return OWNER_STAFF_VISUALS.getOrDefault(owner, 0);
		int visual = LivingStaffFittingHelper.staffVisualFor(player);
		OWNER_STAFF_VISUALS.put(owner, visual);
		return visual;
	}

	private static ModelResourceLocation staffModel(int visual) {
		return STAFF_MODELS.computeIfAbsent(CardinalRitePlantedStaffModels.modelName(visual),
				name -> ModelResourceLocation.standalone(Hemomancy.rloc("item/" + name)));
	}

	// ── Exterior dome ──

	private static void drawExteriorField(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
			RiteFrame frame, float currentTime, Vec3 cam) {
		ActiveRiteClientData.RiteEntry rite = frame.entry;
		if (!rite.hasBoundaryDome()) return;
		if (!CardinalRiteBoundaryGeometry.shouldRenderExterior(rite.getTotalRings())) return;
		float fogPerimeterRadius = CardinalRiteFogGeometry.perimeterRadius(
				rite.getFootprintRadius(), rite.getRiteSize(), rite.getCompletedRings(),
				rite.getTotalRings(), frame.legacy);
		if (fogPerimeterRadius <= 0.0F) return;
		float domeRadius = CardinalRiteFogGeometry.enclosingDomeRadius(
				fogPerimeterRadius, CardinalRiteFogRenderer.fogVerticalOffset());
		float sink = CardinalRiteEndingRules.domeSink(frame.ending, frame.endingAge) * domeRadius;
		if (sink >= domeRadius) return;
		float seed = FaneBoundaryRenderer.revealedFaneStyleSeed(rite.getCenter(), domeRadius);
		BlockPos surface = CardinalRiteAnchorVisualRules.riteSurface(rite.getCenter());
		poseStack.pushPose();
		// An ending sinks the shell into the floor rather than popping it out of existence.
		if (sink > 0.0F) poseStack.translate(0.0F, -sink, 0.0F);
		FaneBoundaryRenderer.drawRevealedFaneStyleDome(
				poseStack, buffer, cam, surface, domeRadius, currentTime, seed);
		poseStack.popPose();
	}

	// ── Sounds ──

	private static void playBoundaryCompletionSounds(Minecraft mc, List<RiteFrame> frames) {
		if (mc.level == null) return;
		for (RiteFrame frame : frames) {
			if (frame.legacy || frame.ending != null) continue;
			ActiveRiteClientData.RiteEntry rite = frame.entry;
			for (CardinalRiteBoundaryProgress.Segment segment : rite.getBoundarySegments()) {
				if (!rite.consumeBoundaryCompletion(segment)) continue;
				float ringRadius = segment.ring() >= 0 && segment.ring() < frame.rings.length
						? frame.rings[segment.ring()].radius
						: CardinalRiteBoundaryGeometry.interactiveRingRadius(segment.ring());
				double midpoint = segment.startAngle() + segment.sweepAngle() * 0.5D;
				double x = rite.getCenter().getX() + 0.5D + Math.cos(midpoint) * ringRadius;
				double y = CardinalRiteBoundaryGeometry.boundaryPlaneY(rite.getCenter().getY());
				double z = rite.getCenter().getZ() + 0.5D + Math.sin(midpoint) * ringRadius;
				float pitch = 0.92F + CardinalRiteEndingRules.hash(segment.startAnchorIndex(), segment.ring()) * 0.16F;
				mc.level.playLocalSound(x, y, z, SoundInit.RITE_SEGMENT_SEAL.get(),
						SoundSource.BLOCKS, 0.6F, pitch, false);
			}
		}
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Shared helpers
	// ════════════════════════════════════════════════════════════════════════

	private static float arcIntegrity(List<CardinalRiteBoundaryProgress.Segment> arcs,
			double angle, float currentTime) {
		double normalizedAngle = CardinalRiteMath.normalizeAngle(angle);
		for (CardinalRiteBoundaryProgress.Segment arc : arcs) {
			double fromStart = CardinalRiteMath.normalizeAngle(
					normalizedAngle - CardinalRiteMath.normalizeAngle(arc.startAngle()));
			if (fromStart > arc.sweepAngle()) continue;
			float integrity = CardinalRiteMath.clamp01(arc.integrity());
			if (integrity <= 0.0F || integrity >= 1.0F) return integrity;
			float flicker = (float) ((Math.sin(
					currentTime * 1.65F + arc.startAngle() * 3.0D) + 1.0D) * 0.5D);
			return 1.0F - (1.0F - integrity) * (0.65F + 0.35F * flicker);
		}
		return 0.0F;
	}

	/**
	 * Radial breathing of a ring or socket at an angle: a slow crawling wave, a
	 * faster counter-wave and a global throb.
	 *
	 * @param directionSign +1 for forward crawl, -1 for reverse crawl
	 */
	private static float undulation(double angleRad, float time, float directionSign) {
		double w1 = Math.sin(angleRad * UNDULATE_FREQ + time * UNDULATE_SPEED * directionSign) * UNDULATE_AMP;
		double w2 = Math.sin(angleRad * UNDULATE_FREQ2 - time * UNDULATE_SPEED2 * directionSign) * UNDULATE_AMP2;
		double throb = Math.sin(time * 0.04D) * 0.035D;
		return (float) (w1 + w2 + throb);
	}

	/** Height of a vessel above the floor at an angle: a small travelling wave kept clear of the surface. */
	private static float surfaceWave(double angle, float time, float directionSign, double phaseOffset,
			float amplitude) {
		return CardinalRiteBoundaryGeometry.surfaceSafeOffset(
				(float) Math.sin(angle * 5.0D + time * 0.06D * directionSign + phaseOffset) * amplitude);
	}

	private static float mix(float from, float to, float amount) {
		return from + (to - from) * amount;
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Frame data
	// ════════════════════════════════════════════════════════════════════════

	private static final class RiteFrame {
		private final ActiveRiteClientData.RiteEntry entry;
		/** How the rite ended, or {@code null} while it is live. */
		private final CardinalRiteEventKind ending;
		private final float endingAge;
		private final boolean legacy;
		private final int ringCount;
		/** Ceremony layout; {@code null} for legacy rites and rites whose recipe is gone. */
		private final CardinalRiteCeremonyLayout layout;
		private final List<AnchorBeat> beats = new ArrayList<>(2);
		private Matrix4f baseMatrix;
		private RingFrame[] rings;
		private FloorTint tint;
		private float heartbeat;
		private float fade = 1.0F;
		private float stainOpacity = 1.0F;
		private float flash;
		private float dim = 1.0F;
		private float impactAge = -1.0F;

		private RiteFrame(ActiveRiteClientData.RiteEntry entry, CardinalRiteEventKind ending, float endingAge,
				boolean legacy, int ringCount, CardinalRiteCeremonyLayout layout) {
			this.entry = entry;
			this.ending = ending;
			this.endingAge = endingAge;
			this.legacy = legacy;
			this.ringCount = ringCount;
			this.layout = layout;
		}
	}

	private static final class RingFrame {
		private final int index;
		private final float radius;
		private List<CardinalRiteBoundaryProgress.Segment> arcs = List.of();
		private List<Double> endpoints = List.of();
		private Matrix4f matrix;
		private float radialScale = 1.0F;
		private float opacity = 1.0F;
		private float socketVisibility = 1.0F;
		private float directionSign = 1.0F;
		private float ringFade = 1.0F;
		private RingColors colors;

		private RingFrame(int index, float radius) {
			this.index = index;
			this.radius = radius;
		}
	}

	private record AnchorBeat(CardinalRiteEventKind kind, int index, float age) {
	}

	private record CachedLayout(CardinalRiteCeremonyDefinition ceremony, CardinalRiteCeremonyLayout layout,
			long refreshAt) {
	}

	/** The consecrated floor's vein and glow colours, which ring vessels lean toward. */
	private record FloorTint(float veinR, float veinG, float veinB, float glowR, float glowG, float glowB) {
		private static FloorTint of(CardinalRiteFloorVisualRules.Palette palette) {
			return new FloorTint(
					((palette.vein() >> 16) & 0xFF) / 255.0F, ((palette.vein() >> 8) & 0xFF) / 255.0F,
					(palette.vein() & 0xFF) / 255.0F,
					((palette.glow() >> 16) & 0xFF) / 255.0F, ((palette.glow() >> 8) & 0xFF) / 255.0F,
					(palette.glow() & 0xFF) / 255.0F);
		}
	}

	/**
	 * The one ring palette: inner rings dark venous red, outer rings brighter,
	 * leaned toward the floor style so rings, sockets and membrane read as one
	 * organism. Rim is the bright vessel wall, core the blood inside.
	 */
	private record RingColors(float coreR, float coreG, float coreB,
			float glowR, float glowG, float glowB,
			float rimR, float rimG, float rimB) {
		private static RingColors of(int ring, int ringCount, FloorTint tint) {
			float lighten = ringCount > 1 ? (float) ring / (ringCount - 1) * 0.6F : 0.0F;
			float coreR = 0.34F + lighten * 0.10F;
			float coreG = 0.008F + lighten * 0.018F;
			float coreB = 0.012F + lighten * 0.014F;
			float glowR = 0.72F + lighten * 0.12F;
			float glowG = 0.025F + lighten * 0.04F;
			float glowB = 0.03F + lighten * 0.03F;
			if (tint != null) {
				coreR = mix(coreR, tint.veinR() * 0.55F, FLOOR_VEIN_LEAN);
				coreG = mix(coreG, tint.veinG() * 0.55F, FLOOR_VEIN_LEAN);
				coreB = mix(coreB, tint.veinB() * 0.55F, FLOOR_VEIN_LEAN);
				glowR = mix(glowR, tint.glowR(), FLOOR_GLOW_LEAN);
				// Keep the halo in the red family whatever the floor's highlight.
				glowG = Math.min(mix(glowG, tint.glowG(), FLOOR_GLOW_LEAN), glowR * 0.35F);
				glowB = Math.min(mix(glowB, tint.glowB(), FLOOR_GLOW_LEAN), glowR * 0.32F);
			}
			return new RingColors(coreR, coreG, coreB, glowR, glowG, glowB,
					mix(coreR, glowR, 0.55F), mix(coreG, glowG, 0.45F), mix(coreB, glowB, 0.45F));
		}
	}
}
