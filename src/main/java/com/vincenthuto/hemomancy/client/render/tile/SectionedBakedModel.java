package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A multiblock's baked body for one facing when it is bigger than a block model can reach (-16..32 pixels): the
 * blockstate's own section plus further sections, each exported moved back by a pixel shift and moved here to its
 * place, then turned to the facing about the main block's centre as the blockstate turns the first. The whole body
 * stays chunk-baked. Used by the Saint Sarcophagus (its head cell) and the ring door (its frame).
 */
public class SectionedBakedModel extends BakedModelWrapper<BakedModel> {
	/** A section's model and the pixels it was exported back by, in the north-facing frame. */
	public record Section(BakedModel model, float x, float y, float z) {}

	private final List<BakedQuad> extra;

	public SectionedBakedModel(BakedModel base, List<Section> sections, Direction facing) {
		super(base);
		List<BakedQuad> quads = new ArrayList<>();
		for (Section section : sections) quads.addAll(turned(section, facing));
		this.extra = List.copyOf(quads);
	}

	/** Every quad of a section moved to its place and turned like the blockstate turns the base model. */
	private static List<BakedQuad> turned(Section section, Direction facing) {
		Matrix4f matrix = new Matrix4f().translate(.5F, 0, .5F)
				.rotate(Axis.YP.rotationDegrees(180 - facing.toYRot()))
				.translate(-.5F, 0, -.5F)
				.translate(section.x() / 16F, section.y() / 16F, section.z() / 16F);
		var transformer = QuadTransformers.applying(new Transformation(matrix));
		int turns = facing.get2DDataValue() - Direction.NORTH.get2DDataValue();
		RandomSource random = RandomSource.create(42L);
		List<BakedQuad> out = new ArrayList<>();
		for (Direction side : sidesAndNull()) {
			for (BakedQuad quad : section.model().getQuads(null, side, random)) {
				BakedQuad moved = transformer.process(quad);
				Direction direction = quad.getDirection();
				if (direction.getAxis().isHorizontal())
					for (int i = 0; i < Math.floorMod(turns, 4); i++) direction = direction.getClockWise();
				out.add(new BakedQuad(moved.getVertices(), moved.getTintIndex(), direction, moved.getSprite(),
						moved.isShade(), moved.hasAmbientOcclusion()));
			}
		}
		return out;
	}

	private static Direction[] sidesAndNull() {
		Direction[] all = new Direction[Direction.values().length + 1];
		System.arraycopy(Direction.values(), 0, all, 0, Direction.values().length);
		return all;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
		return withSections(super.getQuads(state, side, rand), side);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
			ModelData data, @Nullable RenderType renderType) {
		return withSections(super.getQuads(state, side, rand, data, renderType), side);
	}

	/** The further sections never cull against a neighbour: they lie cells away from the block that is asked. */
	private List<BakedQuad> withSections(List<BakedQuad> base, @Nullable Direction side) {
		if (side != null || extra.isEmpty()) return base;
		List<BakedQuad> all = new ArrayList<>(base.size() + extra.size());
		all.addAll(base);
		all.addAll(extra);
		return all;
	}
}
