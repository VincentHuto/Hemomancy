package com.vincenthuto.hemomancy.client.render.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A BakedModel wrapper for Scar Pattern items.
 * Layer 0 (tintIndex 0) quads render normally (the pattern background).
 * Layer 1 (tintIndex 1) quads are scaled down toward the center so the
 * background is visible underneath.
 */
public class ScarPatternBakedModel implements BakedModel {

	private final BakedModel original;
	private static final float OVERLAY_SCALE = 0.55F; // how much to shrink the overlay (0.55 = 55% of original size)
	private static final float Z_OFFSET = 0.005F; // small offset to prevent Z-fighting when held in hand

	public ScarPatternBakedModel(BakedModel original) {
		this.original = original;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, @Nonnull RandomSource rand) {
		List<BakedQuad> originalQuads = original.getQuads(state, side, rand);
		List<BakedQuad> result = new ArrayList<>();

		for (BakedQuad quad : originalQuads) {
			if (quad.getTintIndex() == 1) {
				// Shrink this quad toward center
				result.add(shrinkQuad(quad));
			} else {
				result.add(quad);
			}
		}
		return result;
	}

	/**
	 * Shrinks a quad's vertex positions toward the center of the item face.
	 * Vertex format for items: each vertex has 8 ints:
	 *   [0] posX (float bits), [1] posY (float bits), [2] posZ (float bits),
	 *   [3] color (ABGR packed), [4] texU (float bits), [5] texV (float bits),
	 *   [6] lightmap, [7] normal
	 */
	private BakedQuad shrinkQuad(BakedQuad quad) {
		int[] vertexData = quad.getVertices().clone();
		int vertexSize = vertexData.length / 4; // 4 vertices per quad

		// Center of a standard item face is (0.5, 0.5) in model space
		float centerX = 0.5F;
		float centerY = 0.5F;

		for (int v = 0; v < 4; v++) {
			int offset = v * vertexSize;

			float x = Float.intBitsToFloat(vertexData[offset]);
			float y = Float.intBitsToFloat(vertexData[offset + 1]);
			float z = Float.intBitsToFloat(vertexData[offset + 2]);

			// Scale position toward center
			x = centerX + (x - centerX) * OVERLAY_SCALE;
			y = centerY + (y - centerY) * OVERLAY_SCALE;
			// Push slightly forward to prevent Z-fighting
			z = z + Z_OFFSET;

			vertexData[offset] = Float.floatToRawIntBits(x);
			vertexData[offset + 1] = Float.floatToRawIntBits(y);
			vertexData[offset + 2] = Float.floatToRawIntBits(z);
		}

		return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
	}

	// --- Delegate everything else to the original model ---

	@Override
	public boolean useAmbientOcclusion() {
		return original.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return original.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return original.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleIcon() {
		return original.getParticleIcon();
	}

	@Nonnull
	@Override
	public ItemOverrides getOverrides() {
		return original.getOverrides();
	}

	@Nonnull
	@Override
	public ItemTransforms getTransforms() {
		return original.getTransforms();
	}

	@Override
	public BakedModel applyTransform(ItemDisplayContext displayContext, PoseStack poseStack, boolean applyLeftHandTransform) {
		original.applyTransform(displayContext, poseStack, applyLeftHandTransform);
		return this;
	}
}

