package com.vincenthuto.hemomancy.render.item;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
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

public class CellHandBakedModel implements BakedModel {
	private final BakedModel original;

	public CellHandBakedModel(BakedModel original) {
		this.original = original;
	}

	@Override
	public BakedModel applyTransform(ItemDisplayContext ItemDisplayContext, PoseStack poseStack, boolean applyLeftHandTransform) {
		if ((ItemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| ItemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND)) {
			return this;
		}
		return original.applyTransform(ItemDisplayContext, poseStack, applyLeftHandTransform);
	}

	@Nonnull
	@Override
	public ItemOverrides getOverrides() {
		return original.getOverrides();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleIcon() {
		return original.getParticleIcon();
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, @Nonnull RandomSource rand) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ItemTransforms getTransforms() {
		return ItemTransforms.NO_TRANSFORMS;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		return original.usesBlockLight();
	}
}
