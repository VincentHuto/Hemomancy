package com.vincenthuto.hemomancy.client.render.tile;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A baked model with every quad of one sprite moved onto another painted in the same layout: a palette swap that
 * keeps the model's geometry, turns and render types. The quads are taken once, so the model must not vary its quads
 * by state or model data (a plain or sectioned block model). Used for the Saint Sarcophagus's per-Saint palettes.
 */
public class RetexturedBakedModel extends BakedModelWrapper<BakedModel> {
	private final Map<Direction, List<BakedQuad>> sided = new EnumMap<>(Direction.class);
	private final List<BakedQuad> unsided;

	public RetexturedBakedModel(BakedModel model, TextureAtlasSprite from, TextureAtlasSprite to) {
		super(model);
		RandomSource random = RandomSource.create(42L);
		for (Direction side : Direction.values()) sided.put(side, retextured(model.getQuads(null, side, random), from, to));
		this.unsided = retextured(model.getQuads(null, null, random), from, to);
	}

	private static List<BakedQuad> retextured(List<BakedQuad> quads, TextureAtlasSprite from, TextureAtlasSprite to) {
		List<BakedQuad> out = new ArrayList<>(quads.size());
		for (BakedQuad quad : quads) out.add(quad.getSprite() == from ? retextured(quad, from, to) : quad);
		return List.copyOf(out);
	}

	/** The same quad with each vertex's UV at the same place in {@code to} as it was in {@code from}. */
	private static BakedQuad retextured(BakedQuad quad, TextureAtlasSprite from, TextureAtlasSprite to) {
		int[] vertices = quad.getVertices().clone();
		for (int vertex = 0; vertex < 4; vertex++) {
			int uv = vertex * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
			float u = Float.intBitsToFloat(vertices[uv]), v = Float.intBitsToFloat(vertices[uv + 1]);
			vertices[uv] = Float.floatToRawIntBits(to.getU(from.getUOffset(u)));
			vertices[uv + 1] = Float.floatToRawIntBits(to.getV(from.getVOffset(v)));
		}
		return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), to, quad.isShade(),
				quad.hasAmbientOcclusion());
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
		return side == null ? unsided : sided.get(side);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
			ModelData data, @Nullable RenderType renderType) {
		return getQuads(state, side, rand);
	}
}
