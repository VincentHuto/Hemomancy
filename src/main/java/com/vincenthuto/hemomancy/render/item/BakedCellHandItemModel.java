package com.vincenthuto.hemomancy.render.item;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BakedCellHandItemModel implements BakedModel {
	private final BakedModel original;

	public BakedCellHandItemModel(BakedModel original) {
		this.original = original;
	}

	@Nonnull
	@Override
	public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack stack) {
		if ((cameraTransformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
				|| cameraTransformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND)) {
			return this;
		}
		return original.handlePerspective(cameraTransformType, stack);
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, @Nonnull Random rand) {
		return ImmutableList.of();
	}

	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		return original.usesBlockLight();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleIcon() {
		return original.getParticleIcon();
	}

	@Nonnull
	@Override
	public ItemTransforms getTransforms() {
		return ItemTransforms.NO_TRANSFORMS;
	}

	@Nonnull
	@Override
	public ItemOverrides getOverrides() {
		return original.getOverrides();
	}
}
