package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.ThreadKnotGeometry;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomGeometry.*;

/**
 * Draws the Somatic Loom's state onto the loom itself (tools/model_export/somatic_loom.py, anchors in
 * {@link com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomGeometry}). Stored enzymes wind dyed thread onto the pirns and the reservoir fills the
 * upright veins. A selected weave colours its share of the warp; projected blood pulses up the veins into a
 * beating ganglion; each strand drawn home lights its threads and lays a band at the fell. The memory floats
 * over the fell, catalysts lie on the fresh cloth and the pattern hangs above the ganglion. The engram unfurled
 * around the loom while it weaves is drawn by EngramSigilRenderer, called from here.
 */
public class SomaticLoomRenderer implements BlockEntityRenderer<SomaticLoomBlockEntity> {
	public static final List<String> PART_MODELS = parts();

	/** The engram spreads across the ritual area, and the loom keeps drawing it while its block is off screen. */
	private static final double RENDER_BOUNDS = SomaticWeaveRules.RITUAL_RADIUS;

	/** Clotted blood in the veins, plus the visual standard's hot core and clot accents. */
	private static final float[] BLOOD = {0.55f, 0.03f, 0.06f};
	private static final float[] HOT = ThreadKnotGeometry.HOT;
	private static final float[] CLOT = {0.48f, 0.06f, 0.13f};
	/** Ticks for one pulse to climb from sill to ganglion, with no blood projected and once fully paid. */
	private static final float PULSE_SLOW = 44f, PULSE_FAST = 18f;

	private final SomaticLoomEcho echo;

	public SomaticLoomRenderer(BlockEntityRendererProvider.Context ctx) {
		this.echo = new SomaticLoomEcho(ctx);
	}

	/** Spun-thread cops then enzyme-vial liquids, each in EnumBloodTendency order. */
	private static List<String> parts() {
		List<String> parts = new ArrayList<>();
		for (String kind : new String[]{"cop_", "vial_"})
			for (EnumBloodTendency tendency : EnumBloodTendency.values())
				parts.add(kind + tendency.name().toLowerCase(Locale.ROOT));
		return List.copyOf(parts);
	}

	public static ModelResourceLocation part(String name) {
		return ModelResourceLocation.standalone(Hemomancy.rloc("block/somatic_loom_" + name));
	}

	@Override
	public boolean shouldRenderOffScreen(SomaticLoomBlockEntity te) {
		return true;
	}

	@Override
	public AABB getRenderBoundingBox(SomaticLoomBlockEntity te) {
		return new AABB(te.getBlockPos()).inflate(RENDER_BOUNDS);
	}

	@Override
	public void render(SomaticLoomBlockEntity te, float partialTicks, PoseStack stack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (te.getLevel() == null) return;
		float time = AnimationTime.gameTime(te.getLevel(), partialTicks);
		Direction facing = te.getBlockState().getValue(SomaticLoomBlock.FACING);

		// The engram lies on the ground in world axes, so it is drawn before the pose turns to the loom's facing.
		// A weave broken before its climax leaves everything it raised to dissolve rather than vanish.
		float dissolve = com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramDissolve.update(te, partialTicks, liveDomeRadius(te, partialTicks));
		boolean dissolving = dissolve >= 0F;
		com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramSigilRenderer.render(te, partialTicks, stack, buffer, dissolve);
		com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramGlyphs.render(te, partialTicks, stack, buffer, dissolve);
		playEngramBeats(te);

		// Textured parts first: fetching their buffers would end a flat-colour batch already in progress.
		stack.pushPose();
		toLoomFrame(stack, facing);
		renderCops(te, stack, buffer, combinedLight, combinedOverlay);
		renderVials(te, stack, buffer, combinedLight, combinedOverlay);
		renderItems(te, partialTicks, stack, buffer, combinedLight, combinedOverlay);
		echo.render(te, partialTicks, stack, buffer, dissolve);
		com.vincenthuto.hemomancy.client.render.world.chamberofwill.SomaticLoomDomeRenderer.track(te, dissolving);

		stack.pushPose();
		stack.scale(1 / 16f, 1 / 16f, 1 / 16f);
		Matrix4f px = stack.last().pose();
		VertexConsumer vc = buffer.getBuffer(RenderTypeInit.LOOM_EFFECT);
		float fade = dissolving ? SomaticLoomEffectRules.dissolveFade(dissolve) : 1f;
		float charge = dissolving ? fade : ritualCharge(te);
		drawVeins(vc, px, te, charge, time, dissolving);
		drawGanglion(vc, px, te, charge, time, dissolving);
		drawFeelers(vc, px, te, time, dissolving, fade);
		drawWarp(vc, px, te, time);
		drawWovenBands(vc, px, te, time, dissolving, fade);
		stack.popPose();
		stack.popPose();

	}

	/**
	 * The engram's beats this client has not yet played: each fragment heard plays its memory small and dim on the echo
	 * (a false one plays the other memory it carries), each lane threaded home plays its beat at full stature, and
	 * every beat shows where it happened. A client first seeing a weave already under way skips what came before.
	 */
	private void playEngramBeats(SomaticLoomBlockEntity te) {
		var board = te.getEngram();
		var manip = te.getWeaveManipulation();
		if (board == null || manip == null) return;
		for (var heard : com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramClientState.takeHeard(te.getBlockPos())) {
			if (heard.fragment() < 0 || heard.fragment() >= board.fragmentCount()) continue;
			var fragment = board.fragment(heard.fragment());
			var memory = fragment.intrusion()
					? com.vincenthuto.hemomancy.common.init.ManipulationInit.getByName(fragment.decoy()) : manip;
			echo.playBeat(te, memory == null ? manip : memory, 0.45f);
			com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramBeatEffects.heard(te, heard.fragment());
		}
		var beats = te.engramBeats();
		int seen = te.beatSeen();
		int latest = beats.isEmpty() ? 0 : beats.getLast().serial();
		if (seen < 0) {
			te.markBeatSeen(latest);
			return;
		}
		for (var beat : beats) {
			if (beat.serial() <= seen) continue;
			com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramBeatEffects.play(te, beat);
			if (beat.event() == com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveSession.Event.LAND)
				echo.playBeat(te, manip, 1f);
		}
		te.markBeatSeen(Math.max(seen, latest));
	}

	/** Turns the pose into the north-facing model's frame, matching the blockstate's y rotation. */
	private static void toLoomFrame(PoseStack stack, Direction facing) {
		stack.translate(0.5D, 0.0D, 0.5D);
		stack.mulPose(Axis.YP.rotationDegrees(facingDegrees(facing)));
		stack.translate(-0.5D, 0.0D, -0.5D);
	}

	private static float facingDegrees(Direction facing) {
		return switch (facing) {
			case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
		};
	}

	/** The dome a live Cortical weave stands under now, kept so a broken weave's dome can sink from where it was. */
	private static float liveDomeRadius(SomaticLoomBlockEntity te, float partialTicks) {
		return SomaticLoomEffectRules.domeRadius(te.upgradeTier(), te.isWeaving(),
				te.getWeaveTicks() + (te.isPaused() ? 0 : partialTicks), te.isInClimax(),
				com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramSigilRenderer.climax(te, partialTicks));
	}

	/** 0 before any blood is projected, rising to 1 once the weave is paid; full while the orbs are woven. */
	private static float ritualCharge(SomaticLoomBlockEntity te) {
		if (te.isWeaving()) return 1f;
		if (!te.isAwaitingBlood()) return 0f;
		return SomaticLoomEffectRules.fill(te.getRitualBloodCharged(), te.getRitualBloodRequired());
	}

	//  Pirns and vials: stored enzyme stands as liquid; a paid weave spins it up onto the pirns as thread

	private static void renderCops(SomaticLoomBlockEntity te, PoseStack stack, MultiBufferSource buffer,
			int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		EnumBloodTendency[] tendencies = EnumBloodTendency.values();
		for (int t = 0; t < tendencies.length; t++) {
			float fill = SomaticLoomEffectRules.spunFill(te.displayedSpun(tendencies[t]));
			if (fill <= 0.01f) continue;
			stack.pushPose();
			// The part is one pixel tall on the pirn floor; stretch it to the wound height.
			stack.translate(0, PIRN_FLOOR / 16f, 0);
			stack.scale(1, PIRN_HEIGHT * fill, 1);
			stack.translate(0, -PIRN_FLOOR / 16f, 0);
			mc.getBlockRenderer().getModelRenderer().renderModel(stack.last(),
					buffer.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), null,
					mc.getModelManager().getModel(part(PART_MODELS.get(t))), 1, 1, 1, light, overlay);
			stack.popPose();
		}
	}

	private static void renderVials(SomaticLoomBlockEntity te, PoseStack stack, MultiBufferSource buffer,
			int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		EnumBloodTendency[] tendencies = EnumBloodTendency.values();
		for (int t = 0; t < tendencies.length; t++) {
			float fill = SomaticLoomEffectRules.vialFill(te.displayedStock(tendencies[t]), SomaticLoomBlockEntity.ENZYME_CAPACITY);
			if (fill <= 0.01f) continue;
			stack.pushPose();
			// One pixel of liquid on the vial floor, stretched to the stock it holds.
			stack.translate(0, VIAL_FLOOR / 16f, 0);
			stack.scale(1, VIAL_DEPTH * fill, 1);
			stack.translate(0, -VIAL_FLOOR / 16f, 0);
			mc.getBlockRenderer().getModelRenderer().renderModel(stack.last(),
					buffer.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS)), null,
					mc.getModelManager().getModel(part(PART_MODELS.get(tendencies.length + t))), 1, 1, 1, light, overlay);
			stack.popPose();
		}
	}

	//  Items: the memory over the fell, catalysts on the fresh cloth, the pattern above the ganglion

	public void renderItems(SomaticLoomBlockEntity te, float partialTicks, PoseStack stack,
			MultiBufferSource buffer, int light, int overlay) {
		float time = AnimationTime.gameTime(te.getLevel(), partialTicks);
		Minecraft mc = Minecraft.getInstance();

		ItemStack memory = te.contents.get(0);
		if (!memory.isEmpty()) {
			stack.pushPose();
			// Low over the fell and a little forward, so it never hides the echo standing behind it.
			float y = MEMORY_Y - 1.5f + Mth.sin(time * 0.06f) * 0.3f, z = FELL_Z - 1.0f, spin = time;
			if (te.isInClimax()) {
				// The echo, grown whole, takes the vessel into its hands, then carries it down into the fell.
				float c = Mth.clamp((te.getClimaxTicks() + partialTicks) / SomaticWeaveRules.CLIMAX_TICKS, 0, 1);
				float rise = Mth.clamp(c / 0.4f, 0, 1), fall = Mth.clamp((c - 0.7f) / 0.3f, 0, 1);
				y = Mth.lerp(fall, Mth.lerp(rise, y, ECHO_Y + 17f), FELL_Y + 1f);
				z = Mth.lerp(fall, Mth.lerp(rise, z, ECHO_Z - 3f), FELL_Z);
				spin = time * (1 + 3 * rise);
			}
			stack.translate(0.5F, y / 16f, z / 16f);
			stack.mulPose(Vector3.YP.rotationDegrees(spin).toMoj());
			stack.scale(0.38f, 0.38f, 0.38f);
			mc.getItemRenderer().renderStatic(null, memory, ItemDisplayContext.FIXED, true, stack, buffer,
					null, light, overlay, 0);
			stack.popPose();
		}

		List<ItemStack> catalysts = new ArrayList<>();
		for (int i = 1; i < te.contents.size(); i++)
			if (!te.contents.get(i).isEmpty()) catalysts.add(te.contents.get(i));
		for (int i = 0; i < catalysts.size(); i++) {
			float x = SomaticLoomEffectRules.catalystX(i, catalysts.size(), CATALYST_X0, CATALYST_X1);
			stack.pushPose();
			stack.translate(x / 16f, CATALYST_Y / 16f + Mth.sin(time * 0.05f + i * 1.7f) * 0.012f, CATALYST_Z / 16f);
			stack.mulPose(Vector3.YP.rotationDegrees(time * 0.6f + i * 47f).toMoj());
			stack.scale(0.3f, 0.3f, 0.3f);
			mc.getItemRenderer().renderStatic(null, catalysts.get(i), ItemDisplayContext.FIXED, true, stack, buffer,
					null, light, overlay, 0);
			stack.popPose();
		}

		// While weaving, the echo itself shows what the loom is making.
		MemoryWeavingRecipe recipe = te.isWeaving() ? null : te.getPreviewRecipe();
		if (recipe != null) {
			stack.pushPose();
			stack.translate(GANGLION_X / 16f, PREVIEW_Y / 16f + Mth.sin(time * 0.1f) * 0.03f, GANGLION_Z / 16f);
			stack.mulPose(Vector3.YP.rotationDegrees(time * 0.5f).toMoj());
			stack.scale(0.35f, 0.35f, 0.35f);
			mc.getItemRenderer().renderStatic(null, recipe.getResultItem(te.getLevel().registryAccess()),
					ItemDisplayContext.FIXED, true, stack, buffer, null, light, overlay, 0);
			stack.popPose();
		}
	}

	//  Veins and ganglion: the reservoir, projected blood and the loom's heartbeat

	private static void drawVeins(VertexConsumer vc, Matrix4f px, SomaticLoomBlockEntity te, float charge, float time,
			boolean dissolving) {
		float reservoir = SomaticLoomEffectRules.fill(te.getBloodVolume(), te.getMaxBloodVolume());
		// A broken weave's charge drains out of the veins rather than snapping off.
		boolean ritual = te.isAwaitingBlood() || te.isWeaving() || dissolving;
		float beat = SomaticLoomEffectRules.heartbeat(time, charge);
		for (float[] v : UPRIGHT_VEINS) {
			float top = v[1] + (v[4] - v[1]) * (ritual ? Math.max(reservoir, charge) : reservoir);
			if (top > v[1])
				shell(vc, px, v[0], v[1], v[2], v[3], top, v[5], 0.03f, BLOOD, ritual ? 0.7f + 0.3f * beat : 0.85f);
		}
		if (!ritual) return;
		float[] bar = CROSSBAR_VEIN;
		if (charge > 0)
			shell(vc, px, bar[0], bar[1], bar[2], bar[3], bar[4], bar[5], 0.03f, BLOOD, 0.4f + 0.5f * charge);
		// A hot pulse climbs both uprights, then runs along the crossbar into the ganglion.
		float rise = UPRIGHT_VEINS[0][4] - UPRIGHT_VEINS[0][1];
		float run = GANGLION_X - bar[0];
		float front = SomaticLoomEffectRules.pulseFront(time, Mth.lerp(charge, PULSE_SLOW, PULSE_FAST)) * (rise + run);
		float alpha = 0.3f + 0.65f * charge;
		for (int side = 0; side < 2; side++) {
			float[] v = UPRIGHT_VEINS[side];
			if (front < rise) {
				float y = v[1] + front;
				shell(vc, px, v[0], y, v[2], v[3], Math.min(v[4], y + 1.6f), v[5], 0.06f, HOT, alpha);
			} else {
				float along = front - rise;
				float x0 = side == 0 ? bar[0] + along : bar[3] - along - 1.6f;
				shell(vc, px, x0, bar[1], bar[2], x0 + 1.6f, bar[4], bar[5], 0.06f, HOT, alpha);
			}
		}
	}

	private static void drawGanglion(VertexConsumer vc, Matrix4f px, SomaticLoomBlockEntity te, float charge, float time,
			boolean dissolving) {
		float alpha, swell;
		if (te.isAwaitingBlood() || te.isWeaving() || dissolving) {
			float beat = SomaticLoomEffectRules.heartbeat(time, charge);
			// Paid weaves keep a steady low glow so the ganglion reads as alive, not as a lit lantern; a broken one dims out.
			alpha = (te.isWeaving() ? 0.12f + 0.12f * charge : dissolving ? 0.24f * charge : 0.18f + 0.4f * charge)
					* (0.45f + 0.55f * beat);
			swell = 0.2f + 0.45f * beat;
		} else if (te.getPreviewRecipe() != null) {
			// A weave is chosen: the ganglion breathes slowly while it holds the pattern.
			alpha = 0.08f + 0.06f * (Mth.sin(time * 0.05f) + 1f);
			swell = 0.2f;
		} else {
			return;
		}
		// Upgraded looms grow a larger ganglion; the glow follows the stage's knot.
		int stage = Mth.clamp(te.upgradeTier(), 0, GANGLION_RADII.length - 1);
		float radius = GANGLION_RADII[stage], foot = GANGLION_FEET[stage], top = GANGLION_TOPS[stage];
		octagon(vc, px, GANGLION_X, GANGLION_Z, radius + swell, foot - swell * 0.5f, top + swell * 0.5f, BLOOD, alpha);
		octagon(vc, px, GANGLION_X, GANGLION_Z, radius + swell + 0.35f, foot - swell, top + swell,
				te.isWeaving() || dissolving ? HOT : BLOOD, alpha * 0.35f);
	}

	/**
	 * Ganglionic feelers: their tips glow faintly while the engram is unfurled, and quicken and flare while the weaver
	 * listens to a fragment, for the feelers are what let a Ganglionic loom hear one from anywhere on its engram.
	 */
	private static void drawFeelers(VertexConsumer vc, Matrix4f px, SomaticLoomBlockEntity te, float time, boolean dissolving,
			float fade) {
		if (te.upgradeTier() < 1 || !te.isWeaving() && !dissolving || te.getLevel() == null) return;
		boolean listening = com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramClientState
				.listening(te.getBlockPos(), -1) > 0;
		for (int k = 0; k < FEELER_TIPS.length; k++) {
			float[] tip = FEELER_TIPS[k];
			float beat = 0.5f + 0.5f * Mth.sin(time * (listening ? 0.6f : 0.08f) + k * 1.9f);
			float alpha = listening ? 0.35f + 0.45f * beat : 0.06f + 0.08f * beat;
			float r = listening ? 0.42f + 0.18f * beat : 0.3f;
			// Listening, they droop toward the engram.
			float droop = listening ? 1.4f + 0.5f * beat : 0f;
			octagon(vc, px, tip[0], tip[2], r, tip[1] - r - droop, tip[1] + r - droop, listening ? HOT : BLOOD, alpha * fade);
		}
	}

	//  Warp and cloth: the selected weave's tendencies, and the strands already woven home

	private static void drawWarp(VertexConsumer vc, Matrix4f px, SomaticLoomBlockEntity te, float time) {
		boolean weaving = te.isWeaving();
		MemoryWeavingRecipe recipe = weaving ? te.getWeaveRecipe() : te.getPreviewRecipe();
		if (recipe == null) return;
		EnumBloodTendency[] tendencies = EnumBloodTendency.values();
		int[] required = new int[tendencies.length];
		int[] orbs = new int[tendencies.length], done = new int[tendencies.length];
		for (int t = 0; t < tendencies.length; t++) required[t] = recipe.getEnzymeRequirement(tendencies[t]);
		var board = te.getEngram();
		if (weaving && board != null) {
			for (int i = 0; i < board.fragmentCount(); i++) {
				var fragment = board.fragment(i);
				if (fragment.intrusion()) continue;
				int t = fragment.trueColour();
				orbs[t]++;
				if (te.isInClimax() || com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.connected(board, i)) done[t]++;
			}
		}
		int[] threads = SomaticLoomEffectRules.threadTendencies(required, WARP_X.length);
		for (int k = 0; k < threads.length; k++) {
			int t = threads[k];
			if (t < 0) continue;
			int stored = te.getStoredEnzyme(tendencies[t]);
			// A paid weave has reserved its thread, so a weave in progress never shows as short.
			boolean shortStock = !weaving && stored < required[t] * com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.SPANS_PER_ENZYME;
			float completed = weaving && orbs[t] > 0 ? done[t] / (float) orbs[t] : 0f;
			float alpha = SomaticLoomEffectRules.threadGlow(completed, shortStock, time, k);
			float[] colour = ThreadKnotGeometry.vivid(tendencies[t].getColor());
			if (shortStock) colour = ThreadKnotGeometry.mix(colour, CLOT, 0.7f);
			float x = WARP_X[k], top = k % 2 == 0 ? UPPER_TOP : LOWER_TOP;
			// A core over the thread's top and sides, then a faint halo either side of it.
			shell(vc, px, x - THREAD_HALF, top - 0.25f, FELL_Z, x + THREAD_HALF, top, WARP_END_Z, 0.02f, colour, alpha);
			flat(vc, px, x - 0.45f, x + 0.45f, top + 0.04f, FELL_Z, WARP_END_Z, colour, alpha * 0.3f);
		}
	}

	private static void drawWovenBands(VertexConsumer vc, Matrix4f px, SomaticLoomBlockEntity te, float time, boolean dissolving,
			float fade) {
		var snapshot = dissolving ? com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramDissolve.snapshot(te.getBlockPos()) : null;
		var board = dissolving ? snapshot == null ? null : snapshot.board() : te.getEngram();
		if (!te.isWeaving() && !dissolving || board == null) return;
		List<EnumBloodTendency> woven = new ArrayList<>();
		int total = 0;
		for (int i = 0; i < board.fragmentCount(); i++) {
			if (board.fragment(i).intrusion()) continue;
			total++;
			if (te.isInClimax() || com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.connected(board, i))
				woven.add(EnumBloodTendency.values()[board.fragment(i).trueColour()]);
		}
		float step = SomaticLoomEffectRules.bandStep(total, FELL_Z - CLOTH_FRONT_Z - 1f);
		// The newest pick lies at the fell; earlier ones have been beaten toward the breast beam.
		for (int b = 0; b < woven.size(); b++) {
			float z1 = FELL_Z - 0.55f - (woven.size() - 1 - b) * step;
			float shimmer = 0.8f + 0.2f * Mth.sin(time * 0.15f + b * 1.3f);
			flat(vc, px, CLOTH_X0, CLOTH_X1, CLOTH_TOP + 0.1f, z1 - step * 0.75f, z1,
					ThreadKnotGeometry.vivid(woven.get(b).getColor()), 0.85f * shimmer * fade);
		}
	}

	//  Flat-colour primitives in model pixels

	/** Horizontal quad over x0..x1, z0..z1 at height y. */
	private static void flat(VertexConsumer vc, Matrix4f px, float x0, float x1, float y, float z0, float z1,
			float[] c, float a) {
		ThreadKnotGeometry.emitQuad(vc, px, x0, y, z0, c[0], c[1], c[2], a, x1, y, z0, c[0], c[1], c[2], a,
				x1, y, z1, c[0], c[1], c[2], a, x0, y, z1, c[0], c[1], c[2], a);
	}

	/** Top and side faces of a box grown `proud` pixels past a model element, so the colour sits over its texture. */
	private static void shell(VertexConsumer vc, Matrix4f px, float x0, float y0, float z0, float x1, float y1,
			float z1, float proud, float[] c, float a) {
		x0 -= proud; y0 -= proud; z0 -= proud; x1 += proud; y1 += proud; z1 += proud;
		float r = c[0], g = c[1], b = c[2];
		ThreadKnotGeometry.emitQuad(vc, px, x0, y1, z0, r, g, b, a, x1, y1, z0, r, g, b, a, x1, y1, z1, r, g, b, a, x0, y1, z1, r, g, b, a);
		ThreadKnotGeometry.emitQuad(vc, px, x0, y0, z0, r, g, b, a, x0, y1, z0, r, g, b, a, x1, y1, z0, r, g, b, a, x1, y0, z0, r, g, b, a);
		ThreadKnotGeometry.emitQuad(vc, px, x0, y0, z1, r, g, b, a, x1, y0, z1, r, g, b, a, x1, y1, z1, r, g, b, a, x0, y1, z1, r, g, b, a);
		ThreadKnotGeometry.emitQuad(vc, px, x0, y0, z0, r, g, b, a, x0, y0, z1, r, g, b, a, x0, y1, z1, r, g, b, a, x0, y1, z0, r, g, b, a);
		ThreadKnotGeometry.emitQuad(vc, px, x1, y0, z0, r, g, b, a, x1, y1, z0, r, g, b, a, x1, y1, z1, r, g, b, a, x1, y0, z1, r, g, b, a);
	}

	/** Upright octagonal glow with a lid, its flats `radius` from (cx, cz). */
	private static void octagon(VertexConsumer vc, Matrix4f px, float cx, float cz, float radius, float y0, float y1,
			float[] c, float a) {
		float r = c[0], g = c[1], b = c[2];
		float corner = radius / Mth.cos(Mth.PI / 8);
		for (int i = 0; i < 8; i++) {
			float a0 = Mth.PI / 8 + i * Mth.PI / 4, a1 = a0 + Mth.PI / 4;
			float xa = cx + Mth.cos(a0) * corner, za = cz + Mth.sin(a0) * corner;
			float xb = cx + Mth.cos(a1) * corner, zb = cz + Mth.sin(a1) * corner;
			ThreadKnotGeometry.emitQuad(vc, px, xa, y0, za, r, g, b, a, xb, y0, zb, r, g, b, a, xb, y1, zb, r, g, b, a, xa, y1, za, r, g, b, a);
			ThreadKnotGeometry.emitQuad(vc, px, cx, y1, cz, r, g, b, a, xa, y1, za, r, g, b, a, xb, y1, zb, r, g, b, a, cx, y1, cz, r, g, b, a);
		}
	}
}
