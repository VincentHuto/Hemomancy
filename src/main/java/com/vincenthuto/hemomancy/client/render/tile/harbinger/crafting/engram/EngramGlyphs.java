package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.SomaticLoomEffectRules;
import com.vincenthuto.hemomancy.client.render.world.ThreadKnotGeometry;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules;
import com.vincenthuto.hutoslib.client.particle.TendrilRenderer;
import com.vincenthuto.hutoslib.client.particle.data.TendrilEffectData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * What stands up out of the engram: over each fragment the character it was recalled as, one of the Engram glyphs,
 * hanging in the air in its ichor's colour with a soft halo, rooted to its pool by a tendril now and then. A false one
 * flickers between the loom's beats; one the bloodline knows hangs grey and crumbling; a fragment threaded home sinks
 * into its pool and dims. While the weaver listens to a fragment its glyph swells and a nerve thread runs from it up
 * to the echo. Motes of ichor rise off home lanes, spores off rot, and as the skin first spreads tendrils race from
 * the loom out to every tip.
 */
public final class EngramGlyphs {
	private static final ResourceLocation GLYPHS = Hemomancy.rloc("textures/misc/engram_glyphs.png");
	private static final ResourceLocation GLOW = Hemomancy.rloc("textures/misc/engram_glyphs_glow.png");
	private static final int CHARACTERS = 36;
	/** Ticks between ambient motes and between a fragment's rooting tendrils. */
	private static final int MOTE_TICKS = 3, ROOT_TICKS = 70;
	private static final Map<Long, Long> LAST_AMBIENT = new HashMap<>();
	private static final Map<Long, Long> UNFURLED = new HashMap<>();

	private EngramGlyphs() {
	}

	/** Which of the 36 Engram characters a fragment was recalled as. */
	public static int character(SomaticLoomBlockEntity te, int fragment, int node) {
		long mixed = te.getBlockPos().asLong() * 0x9E3779B97F4A7C15L + node * 0x632BE59BD9B4E019L + fragment * 0x85EBCA6BL;
		return (int) Math.floorMod(mixed ^ (mixed >>> 29), CHARACTERS);
	}

	/** Where a fragment's glyph hangs over its pool, in world space. */
	static Vec3 glyphAt(SomaticLoomBlockEntity te, EngramSigilMesh mesh, int node, float time, int fragment, float sink) {
		double radius = te.sigilRadius();
		double[] unit = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.unit(node);
		double bob = Math.sin(time * 0.05D + fragment * 1.7D) * 0.07D;
		return mesh.ground(unit[0] * radius, unit[1] * radius, Mth.lerp(sink, 1.05D, 0.3D) + bob);
	}

	/** `dissolve`: how far a broken weave's engram has come apart (its glyphs crumble first), or below 0 while live. */
	public static void render(SomaticLoomBlockEntity te, float partialTick, PoseStack stack, MultiBufferSource buffers,
			float dissolve) {
		boolean dissolving = dissolve >= 0F;
		EngramDissolve.Snapshot snapshot = dissolving ? EngramDissolve.snapshot(te.getBlockPos()) : null;
		EngramBoard board = dissolving ? snapshot == null ? null : snapshot.board() : te.getEngram();
		Level level = te.getLevel();
		if (board == null || level == null || !dissolving && !te.isWeaving()) return;
		EngramSigilMesh mesh = EngramSigilMesh.of(te);
		float time = level.getGameTime() + partialTick;
		float unfurl = dissolving ? snapshot.unfurl() : EngramSigilRenderer.unfurl(te, partialTick);
		float climax = dissolving ? 0F : EngramSigilRenderer.climax(te, partialTick);
		float beat = SomaticLoomEffectRules.heartbeat(time, 1F);
		var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 loom = Vec3.atLowerCornerOf(te.getBlockPos());
		Vec3 eye = camera.getPosition();
		Matrix4f matrix = stack.last().pose();
		// Glyphs rise as the skin reaches their tips, and drop away at the climax.
		float rise = Mth.clamp((unfurl - 0.55F) / 0.45F, 0F, 1F) * (1F - Mth.clamp(climax / 0.5F, 0F, 1F));
		// A broken weave's glyphs crumble first, sinking into their pools as they go.
		if (dissolving) rise *= SomaticLoomEffectRules.dissolveFade(dissolve / 0.4F);
		if (rise <= 0.01F) return;
		int active = dissolving ? -1 : te.engramActive();
		float crumble = dissolving ? Mth.clamp(dissolve / 0.4F, 0F, 1F) : 0F;
		java.util.List<Card> cards = new java.util.ArrayList<>();
		for (int fragment = 0; fragment < board.fragmentCount(); fragment++) {
			EngramBoard.Fragment f = board.fragment(fragment);
			boolean home = EngramWeaveRules.connected(board, fragment);
			int colour = EngramWeaveRules.colour(board, fragment);
			float[] tint = f.intrusion() && f.premarked() ? new float[]{0.5F, 0.48F, 0.5F} : EngramSigilRenderer.ichor(colour);
			float heard = EngramClientState.listening(te.getBlockPos(), fragment);
			float alpha = rise;
			float size = 0.36F;
			if (home) alpha *= 0.32F;
			if (fragment == active) size *= 1.18F;
			if (heard > 0) size *= 1.3F;
			if (f.intrusion() && f.premarked()) {
				alpha *= 0.45F;
			} else if (f.intrusion()) {
				// The lie's only tell besides its memory: it breathes against the loom, never with it.
				alpha *= 0.62F + 0.38F * (1F - beat);
			} else {
				alpha *= 0.62F + 0.38F * beat;
			}
			Vec3 at = glyphAt(te, mesh, f.node(), time, fragment, Math.max(home ? 1F : 0F, crumble));
			cards.add(new Card(at.subtract(loom), eye.subtract(at), character(te, fragment, f.node()), size, tint, alpha, heard > 0));
		}
		// One buffer at a time: fetching the next ends the last one's batch.
		VertexConsumer glow = buffers.getBuffer(RenderType.entityTranslucentEmissive(GLOW));
		for (Card c : cards) card(glow, matrix, c.at(), c.toEye(), c.character(), c.size() * 2.1F, c.tint(), c.alpha() * (c.heard() ? 1F : 0.6F));
		VertexConsumer glyph = buffers.getBuffer(RenderType.entityTranslucentEmissive(GLYPHS));
		for (Card c : cards)
			card(glyph, matrix, c.at(), c.toEye(), c.character(), c.size(), ThreadKnotGeometry.mix(c.tint(), ThreadKnotGeometry.HOT, 0.25F),
					Math.min(1F, c.alpha() * 1.15F));
		if (dissolving) return;
		tethers(te, board, mesh, buffers, matrix, time, loom, eye);
		ambient(te, board, mesh, level, time, unfurl, partialTick);
	}

	private record Card(Vec3 at, Vec3 toEye, int character, float size, float[] tint, float alpha, boolean heard) {}

	/** A camera-facing card of one character from a strip of 36, turning only about the vertical. */
	private static void card(VertexConsumer vc, Matrix4f matrix, Vec3 at, Vec3 toEye, int character, float half, float[] tint, float alpha) {
		double flat = Math.sqrt(toEye.x * toEye.x + toEye.z * toEye.z);
		float rx = flat < 1.0E-4 ? 1F : (float) (toEye.z / flat), rz = flat < 1.0E-4 ? 0F : (float) (-toEye.x / flat);
		float u0 = character / (float) CHARACTERS, u1 = (character + 1) / (float) CHARACTERS;
		float x = (float) at.x, y = (float) at.y, z = (float) at.z;
		int a = Mth.clamp(Math.round(alpha * 255), 0, 255);
		int r = Math.round(tint[0] * 255), g = Math.round(tint[1] * 255), b = Math.round(tint[2] * 255);
		cardVertex(vc, matrix, x - rx * half, y - half, z - rz * half, u0, 1, r, g, b, a);
		cardVertex(vc, matrix, x + rx * half, y - half, z + rz * half, u1, 1, r, g, b, a);
		cardVertex(vc, matrix, x + rx * half, y + half, z + rz * half, u1, 0, r, g, b, a);
		cardVertex(vc, matrix, x - rx * half, y + half, z - rz * half, u0, 0, r, g, b, a);
	}

	private static void cardVertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z, float u, float v, int r,
			int g, int b, int a) {
		vc.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
	}

	/** While a fragment is heard, a nerve thread runs from its glyph up to the echo. */
	private static void tethers(SomaticLoomBlockEntity te, EngramBoard board, EngramSigilMesh mesh, MultiBufferSource buffers,
			Matrix4f matrix, float time, Vec3 loom, Vec3 eye) {
		EngramClientState.Listen listen = EngramClientState.listen();
		if (listen == null || !listen.loom().equals(te.getBlockPos()) || listen.fragment() >= board.fragmentCount()) return;
		EngramBoard.Fragment f = board.fragment(listen.fragment());
		Vec3 from = glyphAt(te, mesh, f.node(), time, listen.fragment(), 0F).subtract(loom);
		Vec3 to = te.echoAnchor().add(0, 1.0D, 0).subtract(loom);
		float[] colour = ThreadKnotGeometry.vivid(new ParticleColor(
				Math.round(EngramSigilRenderer.ichor(EngramWeaveRules.colour(board, listen.fragment()))[0] * 255),
				Math.round(EngramSigilRenderer.ichor(EngramWeaveRules.colour(board, listen.fragment()))[1] * 255),
				Math.round(EngramSigilRenderer.ichor(EngramWeaveRules.colour(board, listen.fragment()))[2] * 255)));
		ThreadKnotGeometry.tether(buffers.getBuffer(RenderTypeInit.LOOM_EFFECT), matrix, eye.subtract(loom),
				new Vector3f((float) from.x, (float) from.y, (float) from.z), new Vector3f((float) to.x, (float) to.y, (float) to.z),
				colour, time, 0.37F, 0.85F);
	}

	/** Motes, spores and roots: a few a tick at most, once per loom per game tick. */
	private static void ambient(SomaticLoomBlockEntity te, EngramBoard board, EngramSigilMesh mesh, Level level, float time,
			float unfurl, float partialTick) {
		long key = te.getBlockPos().asLong();
		long tick = level.getGameTime();
		Long last = LAST_AMBIENT.get(key);
		if (last != null && last == tick) return;
		if (LAST_AMBIENT.size() > 64) LAST_AMBIENT.clear();
		LAST_AMBIENT.put(key, tick);
		RandomSource random = level.random;
		double radius = te.sigilRadius();
		Long unfurled = UNFURLED.get(key);
		if (te.getWeaveTicks() < 20 && !te.isInClimax() && (unfurled == null || tick - unfurled > 200)) unfurlTendrils(te, mesh, partialTick);
		if (te.isPaused() || tick % MOTE_TICKS != 0) return;
		for (int fragment = 0; fragment < board.fragmentCount(); fragment++) {
			EngramBoard.Fragment f = board.fragment(fragment);
			int colour = EngramWeaveRules.colour(board, fragment);
			ParticleColor ichor = particleColour(colour);
			int[] lane = board.lane(fragment);
			if (EngramWeaveRules.connected(board, fragment) && lane.length > 1 && random.nextInt(3) == 0) {
				// Ichor steams off a lane threaded home.
				int k = random.nextInt(lane.length - 1);
				double[] a = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.unit(lane[k]);
				double[] b = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.unit(lane[k + 1]);
				double t = random.nextDouble();
				Vec3 at = mesh.ground(Mth.lerp(t, a[0], b[0]) * radius, Mth.lerp(t, a[1], b[1]) * radius, 0.08D);
				level.addParticle(HemoParticleData.ember(ichor, 0.05F, 0.7F, 26), at.x, at.y, at.z, 0, 0.02D, 0);
			}
			if (!f.intrusion() && !EngramWeaveRules.connected(board, fragment) && random.nextInt(6) == 0) {
				double[] u = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.unit(f.node());
				Vec3 at = mesh.ground(u[0] * radius + (random.nextDouble() - 0.5D) * 0.5D, u[1] * radius + (random.nextDouble() - 0.5D) * 0.5D, 0.1D);
				level.addParticle(HemoParticleData.glow(ichor), at.x, at.y, at.z, 0, 0.012D, 0);
			}
			if ((tick + fragment * 17L) % ROOT_TICKS == 0 && unfurl > 0.9F && !f.premarked()) {
				// A tendril roots the glyph back into its pool.
				Vec3 top = glyphAt(te, mesh, f.node(), time, fragment, EngramWeaveRules.connected(board, fragment) ? 1F : 0F).add(0, -0.25D, 0);
				double[] u = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.unit(f.node());
				Vec3 root = mesh.ground(u[0] * radius + (random.nextDouble() - 0.5D) * 0.4D, u[1] * radius + (random.nextDouble() - 0.5D) * 0.4D, 0.03D);
				long seed = key * 31L + tick * 7L + fragment;
				TendrilRenderer.INSTANCE.add(new TendrilEffectData(new TendrilAnchor.Point(top), new TendrilAnchor.Point(root),
						rootConfig(EngramSigilRenderer.ichor(colour), seed), seed), partialTick);
			}
		}
		if (board.pressure() > 0 && random.nextInt(2) == 0) {
			// Spores lift off the rot creeping in from the rim.
			double angle = random.nextDouble() * Math.PI * 2;
			double r = radius * (1.15D - 0.25D * board.pressure() / (double) EngramWeaveRules.PRESSURE_LIMIT * random.nextDouble());
			Vec3 at = mesh.ground(Math.sin(angle) * r, -Math.cos(angle) * r, 0.1D);
			level.addParticle(HemoParticleData.sporiticSpore(new ParticleColor(120, 140, 70)), at.x, at.y, at.z, 0, 0.015D, 0);
		}
	}

	/** As the skin first spreads, tendrils race out from the loom to every tip. */
	private static void unfurlTendrils(SomaticLoomBlockEntity te, EngramSigilMesh mesh, float partialTick) {
		long key = te.getBlockPos().asLong();
		if (UNFURLED.size() > 64) UNFURLED.clear();
		UNFURLED.put(key, te.getLevel().getGameTime());
		double radius = te.sigilRadius();
		Vec3 heart = te.sigilCentre().add(0, 0.25D, 0);
		for (int tip = 0; tip < 8; tip++) {
			double[] u = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.unit(tip);
			Vec3 start = mesh.ground(u[0] * radius * 0.24D, u[1] * radius * 0.24D, 0.05D);
			Vec3 end = mesh.ground(u[0] * radius, u[1] * radius, 0.05D);
			long seed = key * 0x9E3779B97F4A7C15L ^ te.getLevel().getGameTime() * 0xBF58476D1CE4E5B9L ^ tip * 0x94D049BB133111EBL;
			TendrilRenderer.INSTANCE.add(new TendrilEffectData(new TendrilAnchor.Point(start.lerp(heart, 0.2D)),
					new TendrilAnchor.Point(end), unfurlConfig(seed), seed), partialTick);
		}
	}

	private static TendrilEffectConfig rootConfig(float[] ichor, long seed) {
		int rgb = (Math.round(ichor[0] * 200) << 16) | (Math.round(ichor[1] * 200) << 8) | Math.round(ichor[2] * 200);
		return TendrilEffectConfig.defaults()
				.withMode(TendrilEffectConfig.Mode.FREEFORM)
				.withColors(0x90000000 | rgb, 0x70000000 | 0x2A0610)
				.withBlendColors(true)
				.withLifecycle(12, 14, 14)
				.withShape(12, 1, 0.009F, 0.03F)
				.withBranching(1, 1, 0.1F, 0.4F)
				.withWrithe(0.08F, 0.2F, 0.16F, 0.12F)
				.withFixedSeed(true, seed);
	}

	private static TendrilEffectConfig unfurlConfig(long seed) {
		return TendrilEffectConfig.defaults()
				.withMode(TendrilEffectConfig.Mode.FREEFORM)
				.withColors(0xF0300610, 0xD0B80B18)
				.withBlendColors(false)
				.withLifecycle(26, 22, 18)
				.withShape(22, 1, 0.035F, 0.07F)
				.withBranching(1, 2, 0.14F, 0.4F)
				.withWrithe(0.05F, 0.1F, 0.06F, 0.0F)
				.withFixedSeed(true, seed);
	}

	static ParticleColor particleColour(int tendency) {
		float[] c = EngramSigilRenderer.ichor(tendency);
		return new ParticleColor(Math.round(c[0] * 255), Math.round(c[1] * 255), Math.round(c[2] * 255));
	}

	public static void clear() {
		LAST_AMBIENT.clear();
		UNFURLED.clear();
	}
}
