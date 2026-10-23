package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.SomaticLoomEffectRules;
import com.vincenthuto.hemomancy.client.render.world.VisceralMesh;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Where a Cortical dome meets the ground, the world is torn open into the weaver's Chamber: a ragged lip of raw
 * tissue with skin peeled back from it, veins creeping across the floor and feeding beads of blood into the seam,
 * sinews pinning the sky to the ground, and blood weeping from the wound. The tear unzips around the circle from the
 * loom's front as the dome rises; at the climax the sinews snap, the veins withdraw and the seam knits shut.
 * Hemomancy's Animus manipulation material and blood cells only; tissue colours take the weaver's theme tints.
 */
final class SomaticLoomSeamRenderer {
	private static final int SAMPLES = 96, VEINS = 26, SINEWS = 12;
	/** Raw tissue, vessel, blood bead and pale sinew, before the theme's tint. */
	private static final int FLESH = 0xF0767C, VEIN = 0xD8444F, BEAD = 0xFFC2C8, SINEW = 0xFFE2DA, RAW = 0xFF9AA0;
	private static final Map<BlockPos, Ground> GROUND = new HashMap<>();
	private static final Map<BlockPos, Float> TENSION = new HashMap<>();
	private static long particleTick = Long.MIN_VALUE;

	/** Floor heights sampled around the seam, for one radius; refreshed when the dome has grown or every 2 s. */
	private record Ground(float radius, long sampled, double[] heights) {}

	private SomaticLoomSeamRenderer() {
	}

	static void render(PoseStack world, SomaticLoomBlockEntity loom, Vec3 centre, Vec3 cam, float radius,
			ChamberSkyTheme theme, float partial) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;
		float climax = loom.isInClimax()
				? Mth.clamp((loom.getClimaxTicks() + partial) / SomaticWeaveRules.CLIMAX_TICKS, 0, 1) : 0;
		float ticks = loom.getWeaveTicks() + (loom.isPaused() ? 0 : partial);
		float open = SomaticLoomEffectRules.seamOpen(ticks, loom.isInClimax(), climax);
		float growth = SomaticLoomEffectRules.veinGrowth(ticks, loom.isInClimax(), climax);
		float tension = SomaticLoomEffectRules.sinewTension(ticks, loom.isInClimax(), climax);
		if (open <= 0.001F) return;
		float time = AnimationTime.gameTime(level, partial);
		double[] heights = ground(level, loom.getBlockPos(), centre, radius, level.getGameTime());
		double front = frontAngle(loom);
		int flesh = tint(FLESH, theme.bloodTint()), vein = tint(VEIN, theme.veinTint()),
				bead = tint(BEAD, theme.bloodTint()), sinew = tint(SINEW, theme.capillaryTint()), raw = tint(RAW, theme.bloodTint());
		float beat = 0.5F + 0.5F * Mth.sin(time * 0.16F);
		float dim = loom.isPaused() ? 0.5F : 1F;

		var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		var material = tissue();
		VertexConsumer v = buffers.getBuffer(material);
		world.pushPose();
		world.translate(centre.x - cam.x, centre.y - cam.y, centre.z - cam.z);

		// The ragged lip: one continuous tube of swollen tissue along each torn run of the seam.
		double grown = Math.min(1, open * 3);
		boolean[] live = new boolean[SAMPLES];
		Vec3[] lipAt = new Vec3[SAMPLES];
		double[] swellAt = new double[SAMPLES];
		int[] colourAt = new int[SAMPLES];
		for (int i = 0; i < SAMPLES; i++) {
			live[i] = torn(i, front, open) && !Double.isNaN(heights[i]);
			if (!live[i]) continue;
			// Thickness and colour wander smoothly around the ring; each stretch beats a little out of step.
			float local = 0.5F + 0.5F * Mth.sin(time * 0.16F + i * 0.35F);
			double base = (0.14 + 0.26 * smoothNoise(i, 1)) * (1 + 0.22 * local) * grown;
			double knot = knotAt(i, time);
			swellAt[i] = base * (1 + 0.85 * knot);
			lipAt[i] = seam(i, radius, heights[i], centre).add(0, base * (0.4 + 0.5 * smoothNoise(i, 17)), 0);
			colourAt[i] = blend(VisceralMesh.shade(flesh, 0.75 + 0.45 * smoothNoise(i, 7)), raw, Math.min(1, knot * 1.2));
		}
		drawLip(world, v, live, lipAt, swellAt, colourAt, front, 0.97F * dim);
		for (int i = 0; i < SAMPLES; i++) {
			if (!live[i]) continue;
			Vec3 a = seam(i, radius, heights[i], centre), lift = lipAt[i].subtract(a);
			double swell = swellAt[i];
			float local = 0.5F + 0.5F * Mth.sin(time * 0.16F + i * 0.35F);
			// Skin peeled back from the wound, curling up and away on both sides.
			if (i % 2 == 1) {
				double length = (0.35 + 0.7 * noise(i * 3)) * grown;
				Vec3 out = new Vec3(a.x, 0, a.z).normalize().scale(i % 4 == 1 ? 1 : -1);
				Vec3 tip = a.add(out.scale(length * 0.75)).add(0, length * (0.55 + 0.25 * local), 0);
				VisceralMesh.strand(world, v, a.add(lift), tip, 0.06 + 0.05 * noise(i * 5), 0.18, time * 0.04 + i,
						VisceralMesh.shade(flesh, 1.05), 0.92F * dim);
			}
			// Blood pooling in the wound.
			if (i % 5 == 2) {
				world.pushPose();
				world.translate(a.x, a.y + 0.012, a.z);
				double pool = (0.25 + 0.35 * noise(i * 31)) * grown;
				VisceralMesh.latheBand(world, v, 0, 0, 0, pool, 14, blend(vein, bead, 0.35), 0.85F * dim, 0, pool);
				world.popPose();
			}
		}

		// Veins creep across the floor, both into the dome and out of it, beads of blood running back into the seam.
		if (growth > 0.001F) {
			for (int k = 0; k < VEINS; k++) {
				int at = (int) ((k + 0.5) * SAMPLES / VEINS) % SAMPLES;
				if (!torn(at, front, open) || Double.isNaN(heights[at])) continue;
				if (!live[at]) continue;
				double floorY = seam(at, radius, heights[at], centre).y;
				Vec3 out = new Vec3(lipAt[at].x, 0, lipAt[at].z).normalize().scale(k % 2 == 0 ? 1 : -1);
				double length = (1.0 + 2.2 * noise(k * 11)) * growth + swellAt[at];
				// Veins throb in time with the dome, swelling as each beat passes down them; beads of blood run
				// back toward the seam inside them.
				float pulse = 1 + 0.25F * Math.max(0, Mth.sin(time * 0.16F - k));
				double flow = 1 - (time * 0.018 + noise(k * 5)) % 1.0;
				Vec3[] path = vein(lipAt[at], floorY, out, length, k, time);
				double[] radii = SeamProfile.veinRadii(path.length, swellAt[at], 0.11 * pulse, flow, 0.06);
				drawVein(world, v, path, radii, vein, bead, flow, dim);
				// A branch and a twig leave the vein further out, each rooted inside it.
				int fork = path.length / 3, twigAt = path.length * 3 / 5;
				Vec3[] branch = vein(path[fork], floorY, out.yRot((float) ((k % 3 - 1) * 0.7 + 0.35)), length * 0.55, k + 31, time);
				drawVein(world, v, branch, SeamProfile.veinRadii(branch.length, radii[fork], 0.06 * pulse, -1, 0), vein, bead, -1, dim);
				Vec3[] twig = vein(path[twigAt], floorY, out.yRot((float) (-(k % 3 - 1) * 0.6 - 0.4)), length * 0.35, k + 57, time);
				drawVein(world, v, twig, SeamProfile.veinRadii(twig.length, radii[twigAt], 0.04 * pulse, -1, 0), vein, bead, -1, dim);
			}
		}

		// Sinews pin the Chamber's sky to the ground, a short way up the dome's wall.
		if (tension > 0) {
			for (int k = 0; k < SINEWS; k++) {
				int at = (int) ((k + 0.25) * SAMPLES / SINEWS) % SAMPLES;
				if (!torn(at, front, open) || Double.isNaN(heights[at])) continue;
				if (!live[at]) continue;
				Vec3 root = lipAt[at];
				double y = root.y + 1.4 + 0.8 * noise(k * 13);
				double ring = Math.sqrt(Math.max(0, radius * radius - y * y)) * 0.995;
				double angle = angleOf(at) + (k % 2 == 0 ? 0.05 : -0.05);
				Vec3 anchor = new Vec3(Math.cos(angle) * ring, y, Math.sin(angle) * ring);
				drawSinew(world, v, root, root.lerp(anchor, tension), swellAt[at], tension >= 1, sinew, raw, time * 0.03 + k, dim);
			}
		}
		world.popPose();
		buffers.endBatch(material);

		bleed(level, loom.getBlockPos(), centre, radius, heights, front, open, tension, bead);
	}

	private static net.minecraft.client.renderer.RenderType tissue;

	/**
	 * The Animus manipulation surface, but writing depth: the lip, veins, knots and sinews intersect as one body of
	 * tissue, so a vein entering the lip disappears into it rather than flickering over or through it.
	 */
	private static net.minecraft.client.renderer.RenderType tissue() {
		if (tissue == null) {
			tissue = net.minecraft.client.renderer.RenderType.create("somatic_loom_seam_tissue",
					com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR,
					com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, 16384, false, true,
					net.minecraft.client.renderer.RenderType.CompositeState.builder()
							.setShaderState(new net.minecraft.client.renderer.RenderStateShard.ShaderStateShard(
									net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader))
							.setTextureState(new net.minecraft.client.renderer.RenderStateShard.TextureStateShard(
									com.vincenthuto.hemomancy.Hemomancy.rloc("textures/effect/manipulation/animus.png"), false, false))
							.setTransparencyState(net.minecraft.client.renderer.RenderType.TRANSLUCENT_TRANSPARENCY)
							.setDepthTestState(net.minecraft.client.renderer.RenderType.LEQUAL_DEPTH_TEST)
							.setWriteMaskState(net.minecraft.client.renderer.RenderType.COLOR_DEPTH_WRITE)
							.setCullState(net.minecraft.client.renderer.RenderType.NO_CULL)
							.createCompositeState(false));
		}
		return tissue;
	}

	/** The seam weeps blood each tick; as it tears it sprays at the tear fronts, and snapping sinews burst. */
	private static void bleed(ClientLevel level, BlockPos pos, Vec3 centre, float radius, double[] heights, double front,
			float open, float tension, int bead) {
		long tick = level.getGameTime();
		Float previous = TENSION.put(pos, tension);
		if (tick == particleTick) return;
		particleTick = tick;
		var blood = HemoParticleData.bloodCell(new ParticleColor((bead >> 16) & 255, (bead >> 8) & 255, bead & 255));
		var random = level.random;
		for (int n = 0; n < 3; n++) {
			int i = random.nextInt(SAMPLES);
			if (!torn(i, front, open) || Double.isNaN(heights[i])) continue;
			Vec3 at = seam(i, radius, heights[i], centre).add(centre);
			level.addParticle(blood, at.x, at.y + 0.2, at.z, (random.nextDouble() - 0.5) * 0.02, 0.03, (random.nextDouble() - 0.5) * 0.02);
		}
		if (open > 0 && open < 1) {
			// The two tear fronts run around the circle from the loom's front.
			for (int side = -1; side <= 1; side += 2) {
				double angle = front + side * open * Math.PI;
				int i = Math.floorMod((int) Math.round(angle / (Math.PI * 2) * SAMPLES), SAMPLES);
				if (Double.isNaN(heights[i])) continue;
				Vec3 at = seam(i, radius, heights[i], centre).add(centre);
				for (int n = 0; n < 4; n++)
					level.addParticle(blood, at.x, at.y + 0.15, at.z, (random.nextDouble() - 0.5) * 0.12, 0.08 + random.nextDouble() * 0.08,
							(random.nextDouble() - 0.5) * 0.12);
			}
		}
		if (previous != null && previous > 0.5F && tension <= 0) {
			for (int k = 0; k < SINEWS; k++) {
				int i = (int) ((k + 0.25) * SAMPLES / SINEWS) % SAMPLES;
				if (Double.isNaN(heights[i])) continue;
				Vec3 at = seam(i, radius, heights[i], centre).add(centre).add(0, 0.8, 0);
				for (int n = 0; n < 6; n++)
					level.addParticle(blood, at.x, at.y, at.z, (random.nextDouble() - 0.5) * 0.2, random.nextDouble() * 0.15,
							(random.nextDouble() - 0.5) * 0.2);
			}
		}
	}

	/** One continuous vein along its path, blushing to the bead's colour where a bead of blood swells it. */
	private static void drawVein(PoseStack world, VertexConsumer v, Vec3[] path, double[] radii, int colour, int bead,
			double beadAt, float dim) {
		int[] colours = new int[path.length];
		for (int i = 0; i < path.length; i++) {
			double weight = beadAt < 0 ? 0 : SeamProfile.knot((i / (double) (path.length - 1) - beadAt) * (path.length - 1), 1.6);
			colours[i] = blend(colour, bead, weight);
		}
		SeamTube.draw(world, v, java.util.Arrays.asList(path), radii, colours, 0.95F * dim, false);
	}

	/**
	 * A sinew: one tube leaving the lip's centre at most of its thickness, thinning to a cord, and swelling into a
	 * knot where it holds the sky once it is taut.
	 */
	private static void drawSinew(PoseStack world, VertexConsumer v, Vec3 root, Vec3 end, double lipRadius, boolean anchored,
			int colour, int raw, double phase, float dim) {
		int points = 17;
		java.util.List<Vec3> path = new java.util.ArrayList<>();
		double[] radii = new double[points];
		int[] colours = new int[points];
		for (int i = 0; i < points; i++) {
			double t = i / (double) (points - 1);
			path.add(com.vincenthuto.hemomancy.client.render.world.VisceralGeometry.strandPoint(root, end, t, 0.03, phase));
			double flare = Math.max(0, 1 - t / 0.2);
			flare = flare * flare * (3 - 2 * flare);
			double knot = anchored ? SeamProfile.knot((1 - t) * (points - 1), 1.6) : 0;
			radii[i] = 0.04 + (lipRadius * SeamProfile.ROOT_FLARE - 0.04) * flare + 0.07 * knot;
			colours[i] = blend(colour, raw, Math.max(flare, knot));
		}
		SeamTube.draw(world, v, path, radii, colours, 0.92F * dim, false);
	}

	/** How strongly a pulsing knot swells the lip at sample `i`: knots sit every few samples, beating out of step. */
	private static double knotAt(int i, float time) {
		double best = 0;
		for (int k = -4; k <= 4; k++) {
			int centre = Math.floorMod(i + k, SAMPLES);
			if (centre % 5 != 0 || noise(centre * 23) < 0.35) continue;
			double pulse = 0.75 + 0.25 * Math.sin(time * 0.16 + centre * 0.7);
			best = Math.max(best, SeamProfile.knot(k, 1.5) * (0.6 + 0.6 * noise(centre * 29)) * pulse);
		}
		return best;
	}

	private static int blend(int a, int b, double t) {
		t = Mth.clamp(t, 0, 1);
		int r = (int) Mth.lerp(t, (a >> 16) & 255, (b >> 16) & 255), g = (int) Mth.lerp(t, (a >> 8) & 255, (b >> 8) & 255),
				bl = (int) Mth.lerp(t, a & 255, b & 255);
		return r << 16 | g << 8 | bl;
	}

	/**
	 * The lip, as one tube per contiguous torn run: the runs start where the tear has not yet reached (opposite the
	 * loom's front), and a fully open seam closes into a single ring.
	 */
	private static void drawLip(PoseStack world, VertexConsumer v, boolean[] live, Vec3[] lipAt, double[] swellAt,
			int[] colourAt, double front, float alpha) {
		boolean whole = true;
		for (boolean b : live) whole &= b;
		int start = Math.floorMod((int) Math.round((front + Math.PI) / (Math.PI * 2) * SAMPLES), SAMPLES);
		java.util.List<Vec3> run = new java.util.ArrayList<>();
		java.util.List<Integer> index = new java.util.ArrayList<>();
		for (int k = 0; k <= SAMPLES; k++) {
			int i = (start + k) % SAMPLES;
			boolean on = k < SAMPLES && live[i];
			if (on) {
				run.add(lipAt[i]);
				index.add(i);
			}
			if ((!on || k == SAMPLES) && run.size() >= 2) {
				double[] radii = new double[run.size()];
				int[] colours = new int[run.size()];
				for (int r = 0; r < radii.length; r++) {
					radii[r] = swellAt[index.get(r)];
					colours[r] = colourAt[index.get(r)];
				}
				SeamTube.draw(world, v, run, radii, colours, alpha, whole && run.size() == SAMPLES);
			}
			if (!on) {
				run = new java.util.ArrayList<>();
				index = new java.util.ArrayList<>();
			}
		}
	}

	/**
	 * Value noise around the seam: random knots every few samples, eased between, wrapping with the ring so a closed
	 * seam has no step where it meets itself.
	 */
	private static double smoothNoise(int sample, int salt) {
		int spacing = 4, knots = SAMPLES / spacing;
		int k = Math.floorMod(sample, SAMPLES) / spacing;
		double f = (Math.floorMod(sample, SAMPLES) % spacing) / (double) spacing;
		f = f * f * (3 - 2 * f);
		return Mth.lerp(f, noise(k + salt * 1009), noise((k + 1) % knots + salt * 1009));
	}

	/**
	 * A vein's course: out of `root` (inside the lip or a parent vein) and down onto the floor at `floorY` over its
	 * first stretch, then along `direction`, wandering side to side, lying just on the ground.
	 */
	private static Vec3[] vein(Vec3 root, double floorY, Vec3 direction, double length, int seed, float time) {
		Vec3 side = new Vec3(-direction.z, 0, direction.x);
		Vec3[] path = new Vec3[17];
		for (int i = 0; i < path.length; i++) {
			double t = i / (double) (path.length - 1);
			double wander = Math.sin(t * Math.PI * 2.2 + seed * 1.7) * 0.22 * t + Math.sin(time * 0.05 + seed) * 0.02 * t;
			double settle = Math.min(1, t / 0.3);
			settle = settle * settle * (3 - 2 * settle);
			double y = Mth.lerp(settle, root.y, floorY + 0.04);
			Vec3 flat = root.add(direction.scale(length * t)).add(side.scale(wander));
			path[i] = new Vec3(flat.x, y, flat.z);
		}
		return path;
	}

	/** Whether sample `i` lies inside the torn arc, which opens both ways from the loom's front. */
	private static boolean torn(int i, double front, float open) {
		double delta = Mth.wrapDegrees((angleOf(i) - front) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
		return Math.abs(delta) <= open * Math.PI + 1.0E-4D;
	}

	private static double angleOf(int i) {
		return i * Math.PI * 2 / SAMPLES;
	}

	/** Seam point `i` relative to the dome's centre, on the floor at that azimuth. */
	private static Vec3 seam(int i, float radius, double groundY, Vec3 centre) {
		double y = groundY - centre.y;
		double ring = Math.sqrt(Math.max(0, radius * radius - y * y));
		return new Vec3(Math.cos(angleOf(i)) * ring, y, Math.sin(angleOf(i)) * ring);
	}

	private static double[] ground(ClientLevel level, BlockPos pos, Vec3 centre, float radius, long now) {
		Ground cached = GROUND.get(pos);
		if (cached != null && Math.abs(cached.radius() - radius) < 0.2F && now - cached.sampled() < 40) return cached.heights();
		double[] heights = new double[SAMPLES];
		double floor = pos.getY();
		for (int i = 0; i < SAMPLES; i++) {
			double ring = Math.sqrt(Math.max(0, radius * radius - Math.pow(floor - centre.y, 2)));
			double x = centre.x + Math.cos(angleOf(i)) * ring, z = centre.z + Math.sin(angleOf(i)) * ring;
			var hit = level.clip(new ClipContext(new Vec3(x, floor + 3, z), new Vec3(x, floor - 4, z),
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
			heights[i] = hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().y : Double.NaN;
		}
		if (GROUND.size() > 16) GROUND.clear();
		GROUND.put(pos.immutable(), new Ground(radius, now, heights));
		return heights;
	}

	/** The loom's front (toward its weaver), as an angle around the dome. */
	private static double frontAngle(SomaticLoomBlockEntity loom) {
		Direction facing = loom.getBlockState().hasProperty(SomaticLoomBlock.FACING)
				? loom.getBlockState().getValue(SomaticLoomBlock.FACING) : Direction.NORTH;
		return Math.atan2(facing.getStepZ(), facing.getStepX());
	}

	/** Cheap repeatable 0..1 noise for ragged widths and lengths. */
	private static double noise(int seed) {
		double s = Math.sin(seed * 12.9898) * 43758.5453;
		return s - Math.floor(s);
	}

	/** A tissue colour taking part of the theme's tint (tints are multipliers, white by default). */
	private static int tint(int base, int tint) {
		int r = (base >> 16) & 255, g = (base >> 8) & 255, b = base & 255;
		int tr = (tint >> 16) & 255, tg = (tint >> 8) & 255, tb = tint & 255;
		r = (int) Mth.lerp(0.55F, r, r * tr / 255f);
		g = (int) Mth.lerp(0.55F, g, g * tg / 255f);
		b = (int) Mth.lerp(0.55F, b, b * tb / 255f);
		return r << 16 | g << 8 | b;
	}
}
