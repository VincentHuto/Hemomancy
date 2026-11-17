package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenVatBlock;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.KeptSpecimen;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVatBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Draws the creature standing in a Specimen Vat (core-loop Track 5), sized to its vessel's clear inside
 * ({@link MenagerieRules#vatClearWidth}, {@link MenagerieRules#vatClearHeight}): a lone vat's octagonal column, or the
 * same octagon grown to a joined vessel's size, centred on the vessel's floor. Only the vat running a vessel draws.
 * The glass is the block model's translucent layer, drawn after, so the creature shows through it. Display, animation
 * range and husk pose are shared with the jar.
 * <p>
 * A hitbox says little about a model (a pig's is 0.9 wide but its body runs 1.4 long), so the vat measures the model
 * itself once per display and vessel size: it draws the creature into a recorder and fits the circle inside the
 * vessel's octagon around what it drew, centred on the floor. A long, low creature (a pig, a cat, a centipede) would have to shrink to fit its length
 * across the glass, so when standing it on its tail lets it draw at least {@link #TILT_GAIN} times larger, it floats
 * nose-up instead, its length running up the column.
 */
public class SpecimenVatRenderer implements BlockEntityRenderer<SpecimenVatBlockEntity> {
	/** Room for limbs and tails that swing past the rest pose the model was measured in. */
	private static final float SWING_MARGIN = 0.92F;
	/** Idle-animation moments the model is measured at, {@link #MEASURE_STEP} ticks apart. */
	private static final int MEASURE_SAMPLES = 12;
	private static final int MEASURE_STEP = 3;
	/** How much larger a creature must draw nose-up before it is tipped; a cow gains nothing and stays standing. */
	private static final float TILT_GAIN = 1.2F;
	private static final float TILT_PITCH = 90.0F;
	/** One measured fit per display entity; weak, so a rebuilt display drops its old fit. */
	private static final Map<Entity, Fit> FITS = new WeakHashMap<>();

	/**
	 * How large to draw a creature, how far to tip it nose-up, and where to move it to stand centred on the floor, for
	 * a vessel {@code width} vats across and {@code layers} high.
	 */
	private record Fit(float scale, Vec3 centring, float pitch, int width, int layers) {
	}

	public SpecimenVatRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public int getViewDistance() {
		return SpecimenJarRenderer.VIEW_DISTANCE;
	}

	@Override
	public AABB getRenderBoundingBox(SpecimenVatBlockEntity vat) {
		int width = vat.width();
		return BroodThreadRenderer.bounds(vat, new AABB(vat.getBlockPos())
				.expandTowards(width - 1.0D, vat.layers() * 2.0D - 1.0D, width - 1.0D));
	}

	@Override
	public void render(SpecimenVatBlockEntity vat, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
		if (!vat.runsVessel()) return;
		KeptSpecimen kept = vat.kept();
		if (!kept.occupied()) return;
		BroodThreadRenderer.draw(vat, partialTicks, poseStack, buffer);
		Entity entity = SpecimenJarRenderer.display(vat, kept);
		if (entity == null) return;
		int width = vat.width(), layers = vat.layers();
		Fit fit = FITS.get(entity);
		if (fit == null || fit.width() != width || fit.layers() != layers) {
			fit = measure(entity, width, layers);
			FITS.put(entity, fit);
		}
		Direction facing = vat.getBlockState().getValue(SpecimenVatBlock.FACING);
		poseStack.pushPose();
		poseStack.translate(width * 0.5D, 0.0D, width * 0.5D);
		poseStack.mulPose(Vector3.YP.rotationDegrees(180.0F - facing.toYRot()).toMoj());
		poseStack.translate(-0.5D, 0.0D, -0.5D);
		SpecimenJarRenderer.drawKept(vat, kept, entity, fit.scale(), SpecimenVatBlock.FLOOR, fit.centring(), fit.pitch(), partialTicks, poseStack,
				buffer, combinedLight);
		poseStack.popPose();
	}

	/** Draws the creature once, in the half turn the vessel draws it in, and fits the vessel around its vertices. */
	private static Fit measure(Entity entity, int width, int layers) {
		Bounds bounds = new Bounds();
		int tickCount = entity.tickCount;
		try {
			// Several moments of its idle animation, so a blaze's circling rods are measured all the way round.
			for (int sample = 0; sample < MEASURE_SAMPLES; sample++) {
				entity.tickCount = tickCount + sample * MEASURE_STEP;
				PoseStack pose = new PoseStack();
				pose.mulPose(Vector3.YP.rotationDegrees(180.0F).toMoj());
				Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity)
						.render(entity, 0.0F, 0.0F, pose, bounds, LightTexture.FULL_BRIGHT);
			}
		} catch (RuntimeException unmeasurable) {
			return hitboxFit(entity, width, layers);
		} finally {
			entity.tickCount = tickCount;
		}
		if (bounds.vertices.isEmpty()) return hitboxFit(entity, width, layers);
		Fit standing = fit(bounds.vertices, 0.0F, width, layers);
		Fit tipped = fit(bounds.vertices, TILT_PITCH, width, layers);
		return tipped.scale() >= standing.scale() * TILT_GAIN ? tipped : standing;
	}

	/**
	 * Fits the vessel around the vertices turned {@code pitch} degrees about x, as the pose stack will turn them. The
	 * model faces -z here, so a quarter turn carries its nose (-z) up to +y and its back (+y) out to +z. Every vessel is
	 * an octagon, fitted as the circle inside it.
	 */
	private static Fit fit(List<float[]> vertices, float pitch, int width, int layers) {
		boolean tipped = pitch != 0.0F;
		double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
		for (float[] vertex : vertices) {
			double x = vertex[0], y = tipped ? -vertex[2] : vertex[1], z = tipped ? vertex[1] : vertex[2];
			minX = Math.min(minX, x); maxX = Math.max(maxX, x);
			minY = Math.min(minY, y); maxY = Math.max(maxY, y);
			minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
		}
		double centreX = (minX + maxX) * 0.5D, centreZ = (minZ + maxZ) * 0.5D;
		double furthest = 0.0D;
		for (float[] vertex : vertices) {
			double dx = vertex[0] - centreX, dz = (tipped ? vertex[1] : vertex[2]) - centreZ;
			furthest = Math.max(furthest, dx * dx + dz * dz);
		}
		double reach = Math.max(0.1D, Math.sqrt(furthest));
		double height = Math.max(0.1D, maxY - minY);
		double clearWidth = MenagerieRules.vatClearWidth(width), clearHeight = MenagerieRules.vatClearHeight(layers);
		float scale = (float) Math.min(1.0D, Math.min(clearWidth * 0.5D / reach, clearHeight / height) * SWING_MARGIN);
		return new Fit(scale, new Vec3(-centreX, -minY, -centreZ), pitch, width, layers);
	}

	/** For a creature whose renderer draws nothing into a buffer: the fit by hitbox, standing on its origin. */
	private static Fit hitboxFit(Entity entity, int width, int layers) {
		float bodyWidth = Math.max(0.2F, entity.getBbWidth());
		float bodyHeight = Math.max(0.2F, entity.getBbHeight());
		float scale = Math.min(1.0F, Math.min(MenagerieRules.vatClearWidth(width) / bodyWidth,
				MenagerieRules.vatClearHeight(layers) / bodyHeight));
		return new Fit(scale, Vec3.ZERO, 0.0F, width, layers);
	}

	/** A buffer source that draws nothing and keeps every model vertex. */
	private static final class Bounds implements MultiBufferSource {
		final List<float[]> vertices = new ArrayList<>();

		private final VertexConsumer recorder = new VertexConsumer() {
			@Override public VertexConsumer addVertex(float x, float y, float z) { vertices.add(new float[] {x, y, z}); return this; }
			@Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
			@Override public VertexConsumer setUv(float u, float v) { return this; }
			@Override public VertexConsumer setUv1(int u, int v) { return this; }
			@Override public VertexConsumer setUv2(int u, int v) { return this; }
			@Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
		};
		private final VertexConsumer ignored = new VertexConsumer() {
			@Override public VertexConsumer addVertex(float x, float y, float z) { return this; }
			@Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
			@Override public VertexConsumer setUv(float u, float v) { return this; }
			@Override public VertexConsumer setUv1(int u, int v) { return this; }
			@Override public VertexConsumer setUv2(int u, int v) { return this; }
			@Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
		};

		@Override
		public VertexConsumer getBuffer(RenderType type) {
			// A name tag floats above the creature; it is not part of the body the glass must hold. Match the type's
			// name only: every entity type's description names its "textures/".
			return type.toString().startsWith("RenderType[text") ? ignored : recorder;
		}
	}
}
