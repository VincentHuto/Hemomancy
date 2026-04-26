package com.vincenthuto.hemomancy.common.init;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class FluidInit {

	public static final DeferredRegister<Fluid> FLUIDS =
			DeferredRegister.create(net.minecraft.core.registries.Registries.FLUID, Hemomancy.MOD_ID);

	public static final DeferredRegister<FluidType> FLUID_TYPES =
			DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, Hemomancy.MOD_ID);

	// ---- Morphic Nectar ----

	public static final DeferredHolder<FluidType, FluidType> MORPHIC_NECTAR_TYPE = FLUID_TYPES.register(
			"morphic_nectar",
			() -> new FluidType(FluidType.Properties.create()
					.density(1200)
					.viscosity(3000)
					.lightLevel(3)) {
				@Override
				public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
					consumer.accept(new IClientFluidTypeExtensions() {
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
					});
				}
			});

	public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MORPHIC_NECTAR =
			FLUIDS.register("morphic_nectar",
					() -> new BaseFlowingFluid.Source(FluidInit.MORPHIC_NECTAR_PROPS));

	public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> MORPHIC_NECTAR_FLOWING =
			FLUIDS.register("morphic_nectar_flowing",
					() -> new BaseFlowingFluid.Flowing(FluidInit.MORPHIC_NECTAR_PROPS));

	// Properties defined after the holders to avoid forward-reference issues.
	// Suppliers in DeferredHolder are lazy — both sides are fine at use-time.
	public static final BaseFlowingFluid.Properties MORPHIC_NECTAR_PROPS =
			new BaseFlowingFluid.Properties(
					MORPHIC_NECTAR_TYPE,
					MORPHIC_NECTAR,
					MORPHIC_NECTAR_FLOWING)
			.slopeFindDistance(3)
			.levelDecreasePerBlock(1)
			.block(BlockInit.MORPHIC_NECTAR_BLOCK);
}
