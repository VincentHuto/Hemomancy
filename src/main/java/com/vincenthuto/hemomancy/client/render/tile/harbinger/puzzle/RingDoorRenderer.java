package com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.tile.SectionedBakedModel;
import com.vincenthuto.hemomancy.client.render.world.BloodStreams;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorGeometry;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorGeometry.Section;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Draws the ring door's moving parts over its baked frame: the slab and its three rings, each turned to its node's
 * state, the nodes lit full-bright, and the door's two endings. A Silentium's projection runs a stream of blood from
 * their hand into each ring's node, fills the channel round both sides to the notch, pulses down the seam and sinks the
 * slab through the threshold; for Hemorath the blood runs on along the floor into his chamber. An Apotheos's hyphae
 * crack the slab from the hub out, and it bursts into falling fragments. Parts, sections and anchors come from
 * tools/model_export/ring_door.py via {@link RingDoorGeometry}; timings from {@link RingDoorRules}.
 */
public class RingDoorRenderer implements BlockEntityRenderer<RingDoorBlockEntity> {
	private static final Section[][] RINGS = {RingDoorGeometry.OUTER, RingDoorGeometry.MIDDLE, RingDoorGeometry.INNER};
	private static final Section[][] LIT = {RingDoorGeometry.OUTER_LIT, RingDoorGeometry.MIDDLE_LIT,
			RingDoorGeometry.INNER_LIT};
	private static final float SLAB_HEIGHT = RingDoorGeometry.SLAB[4] - RingDoorGeometry.SLAB[1];
	private static final ParticleColor MOTE = new ParticleColor(236, 40, 52);
	private static final int ARC_POINTS = 24;
	private static final int CRACK_BRANCHES = 7;
	private static final int FRAGMENTS = 14;
	/** A lit node gives off a mote about this often. */
	private static final int MOTE_TICKS = 70;
	/** How far behind the door Hemorath's blood runs along the floor: over the chamber's track and its landing. */
	private static final float RUNOFF_Z = 60;
	private static final float HYPHAE_R = 214 / 255F, HYPHAE_G = 196 / 255F, HYPHAE_B = 150 / 255F;
	private final Map<Long, List<Crack>> cracks = new HashMap<>();
	private final Map<Long, Long> lastMote = new HashMap<>();

	/** One growing segment of hyphae across the slab's face, in door pixels; it shows once the crack passes birth. */
	private record Crack(float x0, float y0, float x1, float y1, float birth, float width) {}

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
		float t = door.phaseTick() + (door.phase() == RingDoorBlockEntity.Phase.IDLE ? 0 : partial);
		// The door is lit as the nave in front of its middle is, not by its own buried cell.
		int light = LevelRenderer.getLightColor(level, pos.above(2).relative(facing));
		boolean projecting = door.phase() == RingDoorBlockEntity.Phase.PROJECTING;
		boolean shattering = door.phase() == RingDoorBlockEntity.Phase.SHATTERING;
		boolean open = door.phase() == RingDoorBlockEntity.Phase.OPEN;
		float sink = projecting ? RingDoorRules.sink(t) * SLAB_HEIGHT : 0;
		float crack = shattering ? RingDoorRules.crack(t) : 0;

		pose.pushPose();
		pose.translate(.5, 0, .5);
		pose.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
		pose.translate(-.5, 0, -.5);
		if (!open) {
			pose.pushPose();
			// The hyphae work the slab loose: it shudders harder as they spread.
			float shake = crack * crack * .35F;
			pose.translate(shake * Mth.sin(t * 5.1F) / 16F, -sink / 16F, shake * Mth.cos(t * 4.3F) / 16F);
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
			if (crack > 0) drawCracks(cracksFor(pos), crack, pose, buffers);
			pose.popPose();
		} else if (door.openedBy() == SaintSarcophagusBlockEntity.DoorMode.FORCED && t < RingDoorRules.BURST_TICKS) {
			drawFragments(mc, pos, t, pose, buffers, light);
		}
		pose.popPose();

		if (projecting) drawProjection(door, level, facing, t, sink, partialTicks, pose, buffers);
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

	// --- the Silentium's projection ----------------------------------------------------------

	private static void drawProjection(RingDoorBlockEntity door, Level level, Direction facing, float t, float sink,
			float partialTicks, PoseStack pose, MultiBufferSource buffers) {
		BlockPos pos = door.getBlockPos();
		Vec3 eye = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(Vec3.atLowerCornerOf(pos));
		int seed = (int) Math.floorMod(pos.asLong(), 1000L);
		int stage = RingDoorRules.stageAt((int) t);
		float cz = RingDoorGeometry.CHANNEL_FRONT_Z - .25F;
		float width = RingDoorGeometry.CHANNEL_HALF * .9F / 16F;
		// Each ring's channel fills from its node round both sides to the notch.
		for (int ring = 0; ring < RINGS.length; ring++) {
			float fill = RingDoorRules.ringFill(ring, t);
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
		// The stream from the projector's hand into the ring being filled, reaching out over the stage's first ticks.
		Player projector = door.projector() == null ? null : level.getPlayerByUUID(door.projector());
		if (projector != null && stage < RINGS.length) {
			float reach = Mth.clamp((t - stage * RingDoorRules.FILL_TICKS) / 8F, 0, 1);
			Vec3 hand = handOf(projector, partialTicks).subtract(Vec3.atLowerCornerOf(pos));
			float r = RingDoorGeometry.CHANNEL_RADII[stage];
			Vec3 node = at(facing, RingDoorGeometry.RING_X, RingDoorGeometry.RING_Y + r, .3F);
			List<Vec3> path = new ArrayList<>();
			Vec3 lift = new Vec3(0, .35 + hand.distanceTo(node) * .12, 0);
			int points = 16;
			for (int i = 0; i <= Math.round(points * reach); i++) {
				double k = i / (double) points;
				Vec3 mid = hand.lerp(node, .5).add(lift);
				path.add(hand.lerp(mid, k).lerp(mid.lerp(node, k), k));
			}
			if (path.size() > 1) BloodStreams.stream(pose, buffers, path, eye, .045, .03, seed + 41, .9F);
		}
		// The pulse down the aligned notches and seam, and Hemorath's blood running on through it to his chamber.
		float pulse = RingDoorRules.pulse(t);
		if (pulse > 0) {
			float top = RingDoorGeometry.SLAB[4], bottom = RingDoorGeometry.SLAB[1];
			float head = Mth.lerp(Math.min(pulse * 1.4F, 1), top, bottom);
			List<Vec3> seam = List.of(at(facing, RingDoorGeometry.RING_X, top - sink, .9F),
					at(facing, RingDoorGeometry.RING_X, head - sink, .9F));
			BloodStreams.stream(pose, buffers, seam, eye, .04, .04, seed + 59, .95F);
			if (door.saint() == EnumSaintType.HEMORATH && pulse * 1.4F > 1) {
				float run = Mth.clamp((pulse * 1.4F - 1) * 2.5F + RingDoorRules.sink(t), 0, 1);
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

	/** Where a player's blood leaves them: a little ahead of and below their eyes, toward their main hand. */
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

	// --- the Apotheos's shattering -----------------------------------------------------------

	private List<Crack> cracksFor(BlockPos pos) {
		return cracks.computeIfAbsent(pos.asLong(), seed -> growCracks(RandomSource.create(seed)));
	}

	/** Hyphae out of the hub: a few branches running for the slab's edges, each forking as it goes. */
	private static List<Crack> growCracks(RandomSource random) {
		List<Crack> out = new ArrayList<>();
		for (int b = 0; b < CRACK_BRANCHES; b++) {
			double angle = (b + random.nextDouble() * .6) * Math.PI * 2 / CRACK_BRANCHES;
			grow(out, random, RingDoorGeometry.RING_X, RingDoorGeometry.RING_Y, angle, 0, 1.1F, 0);
		}
		return out;
	}

	private static void grow(List<Crack> out, RandomSource random, double x, double y, double angle, float birth,
			float width, int depth) {
		float[] slab = RingDoorGeometry.SLAB;
		for (int step = 0; step < 14 - depth * 4; step++) {
			double length = 2.5 + random.nextDouble() * 3;
			angle += (random.nextDouble() - .5) * .9;
			double nx = Mth.clamp(x + Math.cos(angle) * length, slab[0] + 1, slab[3] - 1);
			double ny = Mth.clamp(y + Math.sin(angle) * length, slab[1] + 1, slab[4] - 1);
			float next = Math.min(birth + .06F + depth * .03F, 1);
			out.add(new Crack((float) x, (float) y, (float) nx, (float) ny, birth, width));
			if (depth < 2 && random.nextFloat() < .28F)
				grow(out, random, nx, ny, angle + (random.nextBoolean() ? 1 : -1) * (.6 + random.nextDouble() * .5),
						next, width * .65F, depth + 1);
			x = nx;
			y = ny;
			birth = next;
			width *= .93F;
			if (x <= slab[0] + 1 || x >= slab[3] - 1 || y <= slab[1] + 1 || y >= slab[4] - 1) return;
		}
	}

	/**
	 * The cracks grown so far, laid on whatever face lies under them: just over the rings inside the well, just over the
	 * slab outside it. A dark fissure, then the hyphae glowing pale along it.
	 */
	private static void drawCracks(List<Crack> cracks, float progress, PoseStack pose, MultiBufferSource buffers) {
		Matrix4f matrix = pose.last().pose();
		// One buffer at a time: asking the source for the glow's would end the fissures' mid-draw.
		VertexConsumer fissure = buffers.getBuffer(RenderType.debugQuads());
		for (Crack crack : cracks)
			if (crack.birth() < progress)
				crackLine(fissure, matrix, crack, progress, 0, 1, .08F, .02F, .03F, .9F);
		VertexConsumer glow = buffers.getBuffer(RenderType.lightning());
		for (Crack crack : cracks)
			if (crack.birth() < progress)
				crackLine(glow, matrix, crack, progress, .03F, .45F, HYPHAE_R, HYPHAE_G, HYPHAE_B, .45F + .5F * progress);
	}

	/** One crack grown as far as `progress` has reached, `lift` pixels nearer and `widthShare` of its width. */
	private static void crackLine(VertexConsumer consumer, Matrix4f matrix, Crack crack, float progress, float lift,
			float widthShare, float r, float g, float b, float a) {
		float grown = Mth.clamp((progress - crack.birth()) / .06F, 0, 1);
		float x1 = Mth.lerp(grown, crack.x0(), crack.x1()), y1 = Mth.lerp(grown, crack.y0(), crack.y1());
		double radius = Math.hypot((crack.x0() + x1) / 2 - RingDoorGeometry.RING_X,
				(crack.y0() + y1) / 2 - RingDoorGeometry.RING_Y);
		float z = radius < RingDoorGeometry.WELL ? RingDoorGeometry.RING_FRONT_Z - .15F : RingDoorGeometry.SLAB[2] - .12F;
		line(consumer, matrix, crack.x0(), crack.y0(), x1, y1, z - lift, crack.width() * widthShare, r, g, b, a);
	}

	/** A flat quad from (x0, y0) to (x1, y1) in the door's plane at depth z, `width` pixels wide. */
	private static void line(VertexConsumer consumer, Matrix4f matrix, float x0, float y0, float x1, float y1, float z,
			float width, float r, float g, float b, float a) {
		float dx = x1 - x0, dy = y1 - y0, length = Mth.sqrt(dx * dx + dy * dy);
		if (length < 1e-3F) return;
		float nx = -dy / length * width / 2, ny = dx / length * width / 2;
		float s = 1 / 16F;
		// Wound to face the nave (-z), the side the slab is cracked from.
		consumer.addVertex(matrix, (x1 + nx) * s, (y1 + ny) * s, z * s).setColor(r, g, b, a);
		consumer.addVertex(matrix, (x1 - nx) * s, (y1 - ny) * s, z * s).setColor(r, g, b, a);
		consumer.addVertex(matrix, (x0 - nx) * s, (y0 - ny) * s, z * s).setColor(r, g, b, a);
		consumer.addVertex(matrix, (x0 + nx) * s, (y0 + ny) * s, z * s).setColor(r, g, b, a);
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
		// The face reaches two cells either side and four up; a projection's stream reaches back to the projector, and
		// Hemorath's blood runs on into his chamber.
		AABB face = new AABB(door.getBlockPos()).inflate(2, 0, 2).expandTowards(0, 4, 0);
		return door.phase() == RingDoorBlockEntity.Phase.PROJECTING ? face.inflate(18) : face;
	}

	@Override
	public boolean shouldRenderOffScreen(RingDoorBlockEntity door) {
		return true;
	}
}
