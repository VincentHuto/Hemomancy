package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffGrowth;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffGrowthRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Living Staff shows its age (core-loop Track 8): one band of bone round the shaft for each growth ring its
 * handled blood has laid down, read from the {@code hemomancy:staff_rings} stamp on the stack, so every viewer (and
 * every item frame) sees the same Staff. Wraps the staff's inventory model; whatever fitting or morphling visual the
 * staff's own overrides choose is kept, and the bands are added round that model's shaft.
 */
public final class LivingStaffRingModel extends BakedModelWrapper<BakedModel> {
	private static final ResourceLocation BAND_TEXTURE = ResourceLocation.withDefaultNamespace("block/bone_block_side");
	/** Band heights in model pixels, lowest ring first, spaced up the shaft below the hand. */
	private static final float[] BAND_Y = { 5.0F, 7.25F, 9.5F, 11.75F, 14.0F };
	private static final float BAND_HEIGHT = 0.75F;
	private static final float BAND_OVERHANG = 0.3F;
	private final RingOverrides overrides;

	public LivingStaffRingModel(BakedModel staff) {
		super(staff);
		this.overrides = new RingOverrides(staff.getOverrides());
	}

	@Override
	public ItemOverrides getOverrides() {
		return overrides;
	}

	/**
	 * How far the bands are raised on a staff planted in a Focus, in model pixels: the foot of a planted shaft stands in
	 * the Focus throat, so the lowest bands are lifted clear of it.
	 */
	public static final float PLANTED_BAND_LIFT = 3.0F;
	private static final Map<BakedModel, BakedModel[]> PLANTED = new java.util.WeakHashMap<>();

	/**
	 * {@code base} (a planted staff model, which the rite draws directly, so no item override runs) with {@code rings}
	 * bands, raised by {@link #PLANTED_BAND_LIFT}; {@code base} itself for no rings. Cached by model.
	 */
	public static BakedModel withRings(BakedModel base, int rings) {
		int clamped = LivingStaffGrowthRules.clampRing(rings);
		if (clamped <= 0 || base == null) return base;
		BakedModel[] byRing = PLANTED.computeIfAbsent(base, key -> new BakedModel[LivingStaffGrowthRules.MAX_RING + 1]);
		if (byRing[clamped] == null) byRing[clamped] = new Ringed(base, clamped, PLANTED_BAND_LIFT);
		return byRing[clamped];
	}

	private static final class RingOverrides extends ItemOverrides {
		private final ItemOverrides staffOverrides;
		private final Map<BakedModel, BakedModel[]> ringed = new IdentityHashMap<>();

		RingOverrides(ItemOverrides staffOverrides) {
			this.staffOverrides = staffOverrides;
		}

		@Override
		public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
				@Nullable LivingEntity entity, int seed) {
			BakedModel resolved = staffOverrides.resolve(model, stack, level, entity, seed);
			if (resolved == null) resolved = model;
			if (resolved instanceof LivingStaffRingModel wrapper) resolved = wrapper.originalModel;
			int rings = LivingStaffGrowth.stampedRing(stack);
			if (rings <= 0) return resolved;
			BakedModel[] byRing = ringed.computeIfAbsent(resolved, key -> new BakedModel[LivingStaffGrowthRules.MAX_RING + 1]);
			if (byRing[rings] == null) byRing[rings] = new Ringed(resolved, rings, 0.0F);
			return byRing[rings];
		}
	}

	/** One staff model with {@code rings} bands baked onto its shaft. */
	private static final class Ringed extends BakedModelWrapper<BakedModel> {
		private final List<BakedQuad> bands;

		Ringed(BakedModel staff, int rings, float lift) {
			super(staff);
			this.bands = bands(staff, rings, lift);
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
			return withBands(originalModel.getQuads(state, side, rand), side);
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
				ModelData extraData, @Nullable RenderType renderType) {
			return withBands(originalModel.getQuads(state, side, rand, extraData, renderType), side);
		}

		private List<BakedQuad> withBands(List<BakedQuad> base, @Nullable Direction side) {
			if (side != null) return base;
			List<BakedQuad> all = new ArrayList<>(base.size() + bands.size());
			all.addAll(base);
			all.addAll(bands);
			return all;
		}

		@Override
		public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
			originalModel.applyTransform(context, poseStack, leftHand);
			return this;
		}

		@Override
		public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
			return List.of(this);
		}
	}

	/** Bands round the tallest upright element of {@code staff} (its shaft), one per ring. */
	private static List<BakedQuad> bands(BakedModel staff, int rings, float lift) {
		float[] shaft = shaftBounds(staff);
		TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager()
				.getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(BAND_TEXTURE);
		FaceBakery bakery = new FaceBakery();
		List<BakedQuad> quads = new ArrayList<>();
		for (int ring = 0; ring < Math.min(rings, BAND_Y.length); ring++) {
			Vector3f from = new Vector3f(shaft[0] - BAND_OVERHANG, BAND_Y[ring] + lift, shaft[2] - BAND_OVERHANG);
			Vector3f to = new Vector3f(shaft[1] + BAND_OVERHANG, BAND_Y[ring] + lift + BAND_HEIGHT, shaft[3] + BAND_OVERHANG);
			for (Direction direction : Direction.values()) {
				float[] uv = direction.getAxis() == Direction.Axis.Y
						? new float[] { 4.0F, 4.0F, 4.0F + (to.x - from.x), 4.0F + (to.z - from.z) }
						: new float[] { 2.0F + ring * 2.0F, 6.0F, 2.0F + ring * 2.0F + (to.x - from.x), 6.0F + BAND_HEIGHT };
				BlockElementFace face = new BlockElementFace(null, -1, "#band", new BlockFaceUV(uv, 0));
				quads.add(bakery.bakeQuad(from, to, face, sprite, direction, BlockModelRotation.X0_Y0, null, true));
			}
		}
		return List.copyOf(quads);
	}

	/** {minX, maxX, minZ, maxZ} of the staff's shaft in model pixels, found from its tallest upright quads. */
	private static float[] shaftBounds(BakedModel staff) {
		float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
		RandomSource random = RandomSource.create(42L);
		List<BakedQuad> quads = new ArrayList<>(staff.getQuads(null, null, random));
		for (Direction direction : Direction.values()) quads.addAll(staff.getQuads(null, direction, random));
		for (BakedQuad quad : quads) {
			int[] data = quad.getVertices();
			int stride = data.length / 4;
			float lowY = Float.MAX_VALUE, highY = -Float.MAX_VALUE;
			for (int v = 0; v < 4; v++) {
				float y = Float.intBitsToFloat(data[v * stride + 1]);
				lowY = Math.min(lowY, y);
				highY = Math.max(highY, y);
			}
			// The shaft runs most of the model's height; nothing else on a staff is ten pixels tall.
			if ((highY - lowY) * 16.0F < 10.0F) continue;
			for (int v = 0; v < 4; v++) {
				float x = Float.intBitsToFloat(data[v * stride]) * 16.0F;
				float z = Float.intBitsToFloat(data[v * stride + 2]) * 16.0F;
				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x);
				minZ = Math.min(minZ, z);
				maxZ = Math.max(maxZ, z);
			}
		}
		if (minX > maxX || minZ > maxZ) return new float[] { 7.5F, 8.5F, 8.0F, 9.0F };
		return new float[] { minX, maxX, minZ, maxZ };
	}
}
