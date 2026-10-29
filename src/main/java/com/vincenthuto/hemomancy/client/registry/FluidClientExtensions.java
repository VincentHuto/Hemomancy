package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.FluidInit;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Textures and tints of the Hemomancy fluids, registered by {@link ClientExtensionsRegistrar}. */
final class FluidClientExtensions {

	private FluidClientExtensions() {
	}

	static void register(RegisterClientExtensionsEvent event) {
		event.registerFluidType(new IClientFluidTypeExtensions() {
			@Override public ResourceLocation getStillTexture() { return Hemomancy.rloc("block/phlegethontic_ichor_still"); }
			@Override public ResourceLocation getFlowingTexture() { return Hemomancy.rloc("block/phlegethontic_ichor_flow"); }
			@Override public ResourceLocation getOverlayTexture() { return Hemomancy.rloc("block/phlegethontic_ichor_overlay"); }
			@Override public ResourceLocation getRenderOverlayTexture(Minecraft minecraft) {
				return Hemomancy.rloc("textures/block/phlegethontic_ichor_overlay.png");
			}
			@Override public int getTintColor() { return 0xE0FFFFFF; }
		}, FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get());

		event.registerFluidType(new IClientFluidTypeExtensions() {
			private static final ResourceLocation STILL =
					Hemomancy.rloc("block/morphic_nectar_still");
			private static final ResourceLocation FLOW =
					Hemomancy.rloc("block/morphic_nectar_flow");
			private static final ResourceLocation OVERLAY =
					Hemomancy.rloc("block/morphic_nectar_overlay");
			private static final ResourceLocation CAMERA_OVERLAY =
					Hemomancy.rloc("textures/block/morphic_nectar_overlay.png");

			@Override
			public ResourceLocation getStillTexture() { return STILL; }

			@Override
			public ResourceLocation getFlowingTexture() { return FLOW; }

			@Override
			public ResourceLocation getOverlayTexture() { return OVERLAY; }

			@Override
			public ResourceLocation getRenderOverlayTexture(Minecraft mc) { return CAMERA_OVERLAY; }

			// Amber-green tint (ARGB): slightly translucent, yellow-green
			@Override
			public int getTintColor() { return 0xCFB4E832; }
		}, FluidInit.MORPHIC_NECTAR_TYPE.get());

		event.registerFluidType(new IClientFluidTypeExtensions() {
			private static final ResourceLocation STILL = Hemomancy.rloc("block/rendering_bile_still");
			private static final ResourceLocation FLOW = Hemomancy.rloc("block/rendering_bile_flow");
			private static final ResourceLocation OVERLAY = Hemomancy.rloc("block/rendering_bile_overlay");
			private static final ResourceLocation CAMERA_OVERLAY =
					Hemomancy.rloc("textures/block/rendering_bile_overlay.png");

			@Override public ResourceLocation getStillTexture() { return STILL; }
			@Override public ResourceLocation getFlowingTexture() { return FLOW; }
			@Override public ResourceLocation getOverlayTexture() { return OVERLAY; }
			@Override public ResourceLocation getRenderOverlayTexture(Minecraft mc) { return CAMERA_OVERLAY; }
			// The textures carry the ochre colour; a light translucency only.
			@Override public int getTintColor() { return 0xDDFFFFFF; }
		}, FluidInit.RENDERING_BILE_TYPE.get());

		event.registerFluidType(new IClientFluidTypeExtensions() {
			private static final ResourceLocation STILL =
					Hemomancy.rloc("block/white_humor_still");
			private static final ResourceLocation FLOW =
					Hemomancy.rloc("block/white_humor_flow");
			private static final ResourceLocation OVERLAY =
					Hemomancy.rloc("block/white_humor_overlay");
			private static final ResourceLocation CAMERA_OVERLAY =
					Hemomancy.rloc("textures/block/white_humor_overlay.png");

			@Override
			public ResourceLocation getStillTexture() { return STILL; }

			@Override
			public ResourceLocation getFlowingTexture() { return FLOW; }

			@Override
			public ResourceLocation getOverlayTexture() { return OVERLAY; }

			@Override
			public ResourceLocation getRenderOverlayTexture(Minecraft mc) { return CAMERA_OVERLAY; }

			@Override
			public int getTintColor() { return 0xDDEAF7FF; }
		}, FluidInit.WHITE_HUMOR_TYPE.get());
	}
}
