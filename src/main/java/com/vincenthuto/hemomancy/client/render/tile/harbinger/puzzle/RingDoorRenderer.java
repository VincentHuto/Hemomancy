package com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.TendrilTubeBrush;
import com.vincenthuto.hemomancy.client.render.tile.SectionedBakedModel;
import com.vincenthuto.hemomancy.client.render.world.BloodStreams;
import com.vincenthuto.hemomancy.client.render.world.CellHandStreams;
import com.vincenthuto.hemomancy.client.render.world.RingDoorSpreadQueue;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorGeometry.Section;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorGeometry;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity.DoorMode;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Draws the ring door's moving parts over its baked frame: the slab and its three rings, each turned to its node's
 * state, the nodes lit full-bright, and the door opening to held Blood Projection. While it charges, the
 * {@code ring_door_opening} shader spreads across the slab and the rings' front from where the projection strikes: a
 * Silentium's blood, which turns to the monolith's black and red behind its front, or an Apotheos's hyphae, which crack
 * the slab. A Silentium's blood runs from their hand to the strike and fills each ring's channel round to the notch, a
 * pulse runs down the seam, and once full the slab sinks through the threshold; for Hemorath the blood runs on along the
 * floor into his chamber. An Apotheos's hyphae grow from their hand to the strike; full, the slab bursts into falling
 * fragments. Parts, sections and anchors come from tools/model_export/ring_door.py via {@link RingDoorGeometry};
 * timings from {@link RingDoorRules}.
 */
public class RingDoorRenderer implements BlockEntityRenderer<RingDoorBlockEntity> {
	private static final Section[][] RINGS = {RingDoorGeometry.OUTER, RingDoorGeometry.MIDDLE, RingDoorGeometry.INNER};
	private static final Section[][] LIT = {RingDoorGeometry.OUTER_LIT, RingDoorGeometry.MIDDLE_LIT,
			RingDoorGeometry.INNER_LIT};
	private static final float SLAB_HEIGHT = RingDoorGeometry.SLAB[4] - RingDoorGeometry.SLAB[1];
	private static final ParticleColor MOTE = new ParticleColor(236, 40, 52);
	private static final RenderType HYPHAE_CORE = RenderType.entityCutoutNoCull(
			Hemomancy.rloc("textures/entity/ring_door_hyphae.png"));
	private static final float HYPHAE_R = 1.0F, HYPHAE_G = 0.93F, HYPHAE_B = 0.66F;
	private static final int ARC_POINTS = 24;
	private static final int HYPHAE_STATIONS = 18;
	private static final int HYPHAE_THREADS = 7;
	/** The hyphae reach from the hand to the door over this long. */
	private static final float HYPHAE_GROW_TICKS = 10;
	private static final int FRAGMENTS = 14;
	/** A lit node gives off a mote about this often. */
	private static final int MOTE_TICKS = 70;
	/** How far behind the door Hemorath's blood runs along the floor: over the chamber's track and its landing. */
	private static final float RUNOFF_Z = 60;
	/** How much of the way the shown strike point moves toward the projection's each frame. */
	private static final double ORIGIN_GLIDE = .2;
	private final Map<Long, Long> lastMote = new HashMap<>();
	private final Map<Long, Vec3> shownOrigin = new HashMap<>();
	/** When each door's hyphae started reaching out, in game time. */
	private final Map<Long, Float> hyphaeSince = new HashMap<>();

	public RingDoorRenderer(BlockEntityRendererProvider.Context context) {}

	public static ModelResourceLocation part(String model) {
		return ModelResourceLocation.standalone(Hemomancy.rloc("block/" + model));
	}

	/** Every section the renderer or the baked frame needs beyond the blockstate's own model. */
	public static Stream<ModelResourceLocation> standaloneModels() {
		Stream<Section> all = Stream.of(RingDoorGeometry.FRAME, RingDoorGeometry.SLAB_SECTIONS, RingDoorGeometry.OUTER,
				RingDoorGeometry.MIDDLE, RingDoorGeometry.INNER, RingDoorGeometry.OUTER_LIT, RingDoorGeometry.MIDDLE_LIT,
				RingDoorGeometry.INNER_LIT).flatMap(Stream::of);
		return all.filter(section -> !section.model().equals("ring_door")).map(section -> part(section.model()));
	}

	/** The frame's sections past the blockstate's own, baked, or null before they are. */
	@Nullable
	public static List<SectionedBakedModel.Section> frameSections(Function<ModelResourceLocation, BakedModel> models) {
		List<SectionedBakedModel.Section> out = new ArrayList<>();
		for (Section section : RingDoorGeometry.FRAME) {
			if (section.model().equals("ring_door")) continue;
			BakedModel model = models.apply(part(section.model()));
			if (model == null) return null;
			out.add(new SectionedBakedModel.Section(model, section.x(), section.y(), section.z()));
		}
		return out;
	}

	@Override
	public void render(RingDoorBlockEntity door, float partialTicks, PoseStack pose, MultiBufferSource buffers,
			int packedLight, int overlay) {
		Level level = door.getLevel();
		if (level == null) return;
		Minecraft mc = Minecraft.getInstance();
		Direction facing = door.getBlockState().getValue(RingDoorBlock.FACING);
		BlockPos pos = door.getBlockPos();
		float partial = AnimationTime.partialTick();
		float time = AnimationTime.gameTime(level, partialTicks);
		// The door is lit as the nave in front of its middle is, not by its own buried cell.
		int light = LevelRenderer.getLightColor(level, pos.above(2).relative(facing));
		RingDoorBlockEntity.Phase phase = door.phase();
		boolean opening = phase == RingDoorBlockEntity.Phase.OPENING;
		boolean sinking = phase == RingDoorBlockEntity.Phase.SINKING;
		boolean open = phase == RingDoorBlockEntity.Phase.OPEN;
		float t = door.phaseTick() + (sinking || open ? partial : 0);
		float progress = sinking ? 1 : opening ? RingDoorRules.progress(door.charge(partial)) : 0;
		boolean forced = opening && door.path() == DoorMode.FORCED;
		float sink = sinking ? RingDoorRules.sink(t) * SLAB_HEIGHT : 0;
		if (!opening && !sinking) shownOrigin.remove(pos.asLong());
		Vec3 strike = opening || sinking ? strike(door, facing) : null;

		pose.pushPose();
		pose.translate(.5, 0, .5);
		pose.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
		pose.translate(-.5, 0, -.5);
		if (!open) {
			pose.pushPose();
			// An Apotheos's hyphae work the slab loose: it shudders harder as they spread.
			float shake = forced ? progress * progress * .35F : 0;
			pose.translate(shake * Mth.sin(time * 5.1F) / 16F, -sink / 16F, shake * Mth.cos(time * 4.3F) / 16F);
			drawSections(mc, RingDoorGeometry.SLAB_SECTIONS, pose, buffers, light, overlay);
			for (int ring = 0; ring < RINGS.length; ring++) {
				pose.pushPose();
				pose.translate(RingDoorGeometry.RING_X / 16F, RingDoorGeometry.RING_Y / 16F, 0);
				pose.mulPose(Axis.ZP.rotationDegrees(ringAngle(door, ring, level, partialTicks)));
				pose.translate(-RingDoorGeometry.RING_LOCAL / 16F, -RingDoorGeometry.RING_LOCAL / 16F, 0);
				drawSections(mc, RINGS[ring], pose, buffers, light, overlay);
				if (ring < door.nodes()) drawSections(mc, LIT[ring], pose, buffers, LightTexture.FULL_BRIGHT, overlay);
				pose.popPose();
			}
			if (strike != null) drawSpread(door, facing, strike, progress, forced, sink, time, pose, light);
			pose.popPose();
		} else if (door.openedBy() == DoorMode.FORCED && t < RingDoorRules.BURST_TICKS) {
			drawFragments(mc, pos, t, pose, buffers, light);
		}
		pose.popPose();

		if (forced) {
			drawHyphae(door, level, strike, time, partialTicks, pose, buffers, light);
		} else if (opening || sinking) {
			drawBlood(door, facing, progress, sinking ? RingDoorRules.sink(t) : 0, sink, pose, buffers);
		}
		if (!open) moteFromNodes(door, level, facing, partialTicks);
	}

	private static void drawSections(Minecraft mc, Section[] sections, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay) {
		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
		for (Section section : sections) {
			pose.pushPose();
			pose.translate(section.x() / 16F, section.y() / 16F, section.z() / 16F);
			mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(), consumer, null,
					mc.getModelManager().getModel(part(section.model())), 1F, 1F, 1F, light, overlay);
			pose.popPose();
		}
	}

	/** A ring's turn: scrambled until its node lights, then an eased turn that brings its notch onto the seam. */
	private static float ringAngle(RingDoorBlockEntity door, int ring, Level level, float partialTicks) {
		float rest = RingDoorRules.restAngle(ring, door.getBlockPos().asLong());
		if (ring >= door.nodes()) return rest;
		long start = door.turnStart(ring);
		if (start <= 0) return 0;
		float since = (float) (AnimationTime.gameTime(level, partialTicks) - start);
		return Mth.lerp(RingDoorRules.turn(since), rest, 0);
	}

	// --- the spread ---------------------------------------------------------------------------

	/**
	 * Where the spread grows from, in the world: glided toward the projection's latest strike, so it follows the aim
	 * without jumping; the slab's middle before any strike is known.
	 */
	private Vec3 strike(RingDoorBlockEntity door, Direction facing) {
		BlockPos pos = door.getBlockPos();
		Vec3 target = door.origin() != null ? door.origin()
				: Vec3.atLowerCornerOf(pos).add(at(facing, RingDoorGeometry.RING_X, RingDoorGeometry.RING_Y,
						RingDoorGeometry.SLAB[2]));
		Vec3 shown = shownOrigin.get(pos.asLong());
		shown = shown == null ? target : shown.lerp(target, ORIGIN_GLIDE);
		shownOrigin.put(pos.asLong(), shown);
		return shown;
	}

	/**
	 * The shader's two layers in the door's frame, queued to draw after the translucent blocks: one on the slab's face
	 * outside the well, one on the rings' front inside it. The pattern is laid out from the door's own pixel origin,
	 * lowered with the slab as it sinks.
	 */
	private static void drawSpread(RingDoorBlockEntity door, Direction facing, Vec3 strike, float progress,
			boolean forced, float sink, float time, PoseStack pose, int light) {
		BlockPos pos = door.getBlockPos();
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		Vec3 lowered = new Vec3(0, -sink / 16.0, 0);
		Vec3 base = Vec3.atLowerCornerOf(pos).add(lowered);
		Vec3 anchor = base.add(at(facing, 0, 0, 0));
		Vec3 right = at(facing, 16, 0, 0).subtract(at(facing, 0, 0, 0)).normalize();
		Vec3 origin = strike.add(lowered);
		double ox = origin.subtract(anchor).dot(right) * 16, oy = (origin.y - anchor.y) * 16;
		float[] slab = RingDoorGeometry.SLAB;
		double reach = 0;
		for (float x : new float[] {slab[0], slab[3]})
			for (float y : new float[] {slab[1], slab[4]}) reach = Math.max(reach, Math.hypot(x - ox, y - oy));
		float seed = Math.floorMod(pos.asLong() * 31L, 997L) / 97F;
		float well = RingDoorGeometry.WELL;
		RingDoorSpreadQueue.add(new RingDoorSpreadQueue.Spread(pose.last().copy(), time, progress, seed, forced ? 1 : 0,
				origin.subtract(camera), anchor.subtract(camera), right, RingDoorGeometry.RING_X, RingDoorGeometry.RING_Y,
				well, (float) reach, new float[] {slab[0], slab[1], slab[3], slab[4], slab[2] - .08F},
				new float[] {RingDoorGeometry.RING_X - well, RingDoorGeometry.RING_Y - well, RingDoorGeometry.RING_X + well,
						RingDoorGeometry.RING_Y + well, RingDoorGeometry.RING_FRONT_Z - .1F}, light));
	}

	// --- the Silentium's blood -----------------------------------------------------------------

	/**
	 * Each ring's channel filling from its node round both sides to the notch as the charge passes it, the pulse down
	 * the aligned notches and seam, and, while the slab sinks ({@code sunk} 0 to 1), Hemorath's blood running on
	 * through it to his chamber. The stream from the hand is Blood Projection's own pour.
	 */
	private static void drawBlood(RingDoorBlockEntity door, Direction facing, float progress, float sunk, float sink,
			PoseStack pose, MultiBufferSource buffers) {
		BlockPos pos = door.getBlockPos();
		Vec3 eye = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(Vec3.atLowerCornerOf(pos));
		int seed = (int) Math.floorMod(pos.asLong(), 1000L);
		float cz = RingDoorGeometry.CHANNEL_FRONT_Z - .25F;
		float width = RingDoorGeometry.CHANNEL_HALF * .9F / 16F;
		for (int ring = 0; ring < RINGS.length; ring++) {
			float fill = RingDoorRules.ringFill(ring, progress);
			if (fill <= 0) continue;
			float radius = RingDoorGeometry.CHANNEL_RADII[ring];
			for (int side = -1; side <= 1; side += 2) {
				List<Vec3> arc = new ArrayList<>(ARC_POINTS + 1);
				for (int i = 0; i <= ARC_POINTS; i++) {
					double angle = Math.toRadians(90 + side * fill * 180 * i / ARC_POINTS);
					arc.add(at(facing, RingDoorGeometry.RING_X + radius * Math.cos(angle),
							RingDoorGeometry.RING_Y + radius * Math.sin(angle) - sink, cz));
				}
				BloodStreams.stream(pose, buffers, arc, eye, width, width * .8, seed + ring * 7 + side, .95F);
			}
		}
		float pulse = RingDoorRules.pulse(progress);
		if (pulse > 0) {
			float top = RingDoorGeometry.SLAB[4], bottom = RingDoorGeometry.SLAB[1];
			float head = Mth.lerp(pulse, top, bottom);
			List<Vec3> seam = List.of(at(facing, RingDoorGeometry.RING_X, top - sink, .9F),
					at(facing, RingDoorGeometry.RING_X, head - sink, .9F));
			BloodStreams.stream(pose, buffers, seam, eye, .04, .04, seed + 59, .95F);
			if (door.saint() == EnumSaintType.HEMORATH && sunk > 0) {
				float run = Mth.clamp(sunk * 1.5F, 0, 1);
				List<Vec3> floor = new ArrayList<>();
				floor.add(at(facing, RingDoorGeometry.RING_X, bottom, .9F));
				for (int i = 0; i <= 12; i++) {
					float z = Mth.lerp(i / 12F * run, 1, RUNOFF_Z);
					floor.add(at(facing, RingDoorGeometry.RING_X + Mth.sin(z * .21F) * 2.5F, .7F, z));
				}
				BloodStreams.stream(pose, buffers, floor, eye, .07, .05, seed + 83, .95F);
			}
		}
	}

	/** Where a player's blood or hyphae leave them: a little ahead of and below their eyes, toward their main hand. */
	private static Vec3 handOf(Player player, float partialTicks) {
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 side = look.cross(new Vec3(0, 1, 0)).normalize();
		Vec3 eyes = player.getEyePosition(partialTicks);
		return eyes.add(look.scale(.45)).add(side.scale(.3)).subtract(0, .45, 0);
	}

	/** A north-frame door pixel as a point in the block's own space. */
	private static Vec3 at(Direction facing, double x, double y, double z) {
		double[] p = RingDoorGeometry.point(facing, x, y, z);
		return new Vec3(p[0], p[1], p[2]);
	}

	// --- the Apotheos's hyphae -----------------------------------------------------------------

	/**
	 * Whether {@code living}'s projection at {@code hit} is an Apotheos's hyphae rather than blood: the struck block is
	 * part of a ring door being forced, or the door is still shut and the local player aiming at it is an Apotheos.
	 * Blood Projection's pour and its cells stand down for it, and the door draws the hyphae.
	 */
	public static boolean hyphaeInPlaceOfBlood(LivingEntity living, BlockHitResult hit) {
		Level level = living.level();
		BlockPos pos = hit.getBlockPos();
		if (level.getBlockEntity(pos) instanceof FillerBlockEntity filler && filler.getMainBlockPos() != null)
			pos = filler.getMainBlockPos();
		if (!(level.getBlockEntity(pos) instanceof RingDoorBlockEntity door)) return false;
		RingDoorBlockEntity.Phase phase = door.phase();
		if (phase == RingDoorBlockEntity.Phase.OPENING) return door.path() == DoorMode.FORCED;
		return phase == RingDoorBlockEntity.Phase.IDLE && living == Minecraft.getInstance().player
				&& ApotheosBodyEvents.isApotheos((Player) living);
	}

	/**
	 * While an Apotheos feeds the door, their hyphae reach out of the hand to the strike: a loose bundle of fine threads
	 * that grow out over half a second, twist round one another on the way, and splay apart as they meet the door, where
	 * the spread takes over. Pale bone threads under a faint white-gold sheen.
	 */
	private void drawHyphae(RingDoorBlockEntity door, Level level, Vec3 struck, float time, float partialTicks,
			PoseStack pose, MultiBufferSource buffers, int light) {
		BlockPos pos = door.getBlockPos();
		Player projector = door.projector() == null ? null : level.getPlayerByUUID(door.projector());
		if (projector == null || !door.feeding()) {
			hyphaeSince.remove(pos.asLong());
			return;
		}
		float since = time - hyphaeSince.computeIfAbsent(pos.asLong(), key -> time);
		float reach = HemoEasing.clamp01(since / HYPHAE_GROW_TICKS);
		Vec3 palm = CellHandStreams.livePalm(projector);
		Vec3 hand = (palm != null ? palm : handOf(projector, partialTicks)).subtract(Vec3.atLowerCornerOf(pos));
		Vec3 strike = struck.subtract(Vec3.atLowerCornerOf(pos));
		Vec3 span = strike.subtract(hand);
		double length = span.length();
		if (length < .1) return;
		Vec3 along = span.scale(1 / length);
		Vec3 side = along.cross(new Vec3(0, 1, 0));
		side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
		Vec3 up = side.cross(along).normalize();
		long seed = pos.asLong();
		int stations = Math.max(2, Math.round(HYPHAE_STATIONS * reach));
		float[][] lines = new float[HYPHAE_THREADS][];
		float[][] radii = new float[HYPHAE_THREADS][];
		VertexConsumer cores = buffers.getBuffer(HYPHAE_CORE);
		for (int thread = 0; thread < HYPHAE_THREADS; thread++) {
			float phase = thread * Mth.TWO_PI / HYPHAE_THREADS + (seed & 7);
			float sway = .6F + .4F * Mth.sin(thread * 2.3F + (seed & 15));
			float[] line = new float[(stations + 1) * 3];
			for (int i = 0; i <= stations; i++) {
				float k = i / (float) HYPHAE_STATIONS;
				// Bundled tight along the way, loosening at the hand and splaying wide where they meet the door.
				float spread = .015F + .03F * Mth.sin(k * Mth.PI) + .22F * k * k * k * k * sway;
				float turn = phase + k * 7 + time * .05F;
				Vec3 at = hand.add(span.scale(k)).add(up.scale(Mth.sin(k * Mth.PI) * Math.min(.25, length * .05)))
						.add(side.scale(Mth.cos(turn) * spread)).add(up.scale(Mth.sin(turn) * spread))
						.add(side.scale(Mth.sin(time * .2F + k * 11 + thread) * .015F * Mth.sin(k * Mth.PI)));
				line[i * 3] = (float) at.x * 16;
				line[i * 3 + 1] = (float) at.y * 16;
				line[i * 3 + 2] = (float) at.z * 16;
			}
			lines[thread] = line;
			radii[thread] = TendrilTubeBrush.taper(stations + 1, .32F, .12F, 1.0F, seed + thread);
			TendrilTubeBrush.core(pose.last(), cores, line, radii[thread], time * .03F + thread, light,
					OverlayTexture.NO_OVERLAY, 1.0F);
		}
		// One buffer at a time: the sheen goes on once every thread's core is down.
		VertexConsumer sheen = buffers.getBuffer(HLRenderTypeInit.TENDRIL_GLOW);
		for (int thread = 0; thread < HYPHAE_THREADS; thread++) {
			float[] alpha = new float[stations + 1];
			float travel = (time * .06F + thread * .17F) % 1.0F;
			for (int i = 0; i <= stations; i++) {
				float pulse = Math.max(0, 1 - Math.abs(i / (float) HYPHAE_STATIONS - travel) * 6);
				alpha[i] = .05F + .2F * pulse;
			}
			TendrilTubeBrush.glow(pose.last(), sheen, lines[thread], radii[thread], 1.5F, HYPHAE_R, HYPHAE_G, HYPHAE_B,
					alpha);
		}
	}

	/** The burst: pieces of the slab's carved face thrown toward the nave, tumbling down to the floor. */
	private static void drawFragments(Minecraft mc, BlockPos pos, float t, PoseStack pose, MultiBufferSource buffers,
			int light) {
		TextureAtlasSprite sprite = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(Hemomancy.rloc("block/ring_door"));
		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
		float[] slab = RingDoorGeometry.SLAB, uv = RingDoorGeometry.FRAGMENT_UV;
		RandomSource random = RandomSource.create(pos.asLong() * 31);
		float fade = 1 - Mth.clamp((t - RingDoorRules.BURST_TICKS * .7F) / (RingDoorRules.BURST_TICKS * .3F), 0, 1);
		for (int i = 0; i < FRAGMENTS; i++) {
			float x = Mth.lerp(random.nextFloat(), slab[0] + 4, slab[3] - 4);
			float y = Mth.lerp(random.nextFloat(), slab[1] + 8, slab[4] - 4);
			float vx = (x - RingDoorGeometry.RING_X) * .03F + (random.nextFloat() - .5F) * .8F;
			float vy = .6F + random.nextFloat() * 1.2F, vz = -(1.2F + random.nextFloat() * 1.6F);
			float size = (4 + random.nextFloat() * 6) * fade;
			float spin = (random.nextFloat() - .5F) * 40;
			float u0 = Mth.lerp(random.nextFloat() * .5F, uv[0], uv[2]), v0 = Mth.lerp(random.nextFloat() * .5F, uv[1], uv[3]);
			float u1 = Mth.lerp(.5F, u0, uv[2]), v1 = Mth.lerp(.5F, v0, uv[3]);
			// Pixels per tick, with gravity, stopping at the floor.
			float px = x + vx * t, py = Math.max(y + vy * t - .09F * t * t, size / 2), pz = slab[2] + vz * t;
			pose.pushPose();
			pose.translate(px / 16F, py / 16F, pz / 16F);
			pose.mulPose(Axis.ZP.rotationDegrees(spin * t));
			pose.mulPose(Axis.XP.rotationDegrees(spin * .7F * t));
			card(consumer, pose, sprite, size / 32F, u0, v0, u1, v1, light);
			pose.popPose();
		}
	}

	private static void card(VertexConsumer consumer, PoseStack pose, TextureAtlasSprite sprite, float half, float u0,
			float v0, float u1, float v1, int light) {
		PoseStack.Pose last = pose.last();
		float[][] corners = {{half, half, u0, v0}, {half, -half, u0, v1}, {-half, -half, u1, v1}, {-half, half, u1, v0}};
		for (float[] c : corners)
			consumer.addVertex(last, c[0], c[1], 0).setColor(1F, 1F, 1F, 1F)
					.setUv(sprite.getU(c[2]), sprite.getV(c[3])).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
					.setNormal(last, 0, 0, -1);
	}

	// --- nodes --------------------------------------------------------------------------------

	/** Each lit node breathes out a mote of blood-light now and then. */
	private void moteFromNodes(RingDoorBlockEntity door, Level level, Direction facing, float partialTicks) {
		long now = level.getGameTime();
		Long last = lastMote.get(door.getBlockPos().asLong());
		if (last != null && now - last < MOTE_TICKS / Math.max(1, door.nodes())) return;
		lastMote.put(door.getBlockPos().asLong(), now);
		if (door.nodes() == 0) return;
		int ring = level.random.nextInt(door.nodes());
		double angle = Math.toRadians(ringAngle(door, ring, level, partialTicks) + 90);
		float r = RingDoorGeometry.CHANNEL_RADII[ring];
		Vec3 node = at(facing, RingDoorGeometry.RING_X + r * Math.cos(angle), RingDoorGeometry.RING_Y + r * Math.sin(angle),
				0).add(Vec3.atLowerCornerOf(door.getBlockPos()));
		Vec3 out = at(facing, 8, 0, -16).subtract(at(facing, 8, 0, 0)).scale(.02);
		level.addParticle(HemoParticleData.glow(MOTE), node.x, node.y, node.z, out.x, .006, out.z);
	}

	@Override
	public AABB getRenderBoundingBox(RingDoorBlockEntity door) {
		// The face reaches two cells either side and four up; while it opens a stream or hyphae reach back to the
		// projector, and Hemorath's blood runs on into his chamber.
		AABB face = new AABB(door.getBlockPos()).inflate(2, 0, 2).expandTowards(0, 4, 0);
		RingDoorBlockEntity.Phase phase = door.phase();
		return phase == RingDoorBlockEntity.Phase.OPENING || phase == RingDoorBlockEntity.Phase.SINKING
				? face.inflate(18) : face;
	}

	@Override
	public boolean shouldRenderOffScreen(RingDoorBlockEntity door) {
		return true;
	}
}
