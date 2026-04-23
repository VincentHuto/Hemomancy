package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FluidInit {

	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID,
			Hemomancy.MOD_ID);

//	public static final ResourceLocation WATER_STILL_RL = ResourceLocation.withDefaultNamespace("block/water_still");
//	public static final ResourceLocation WATER_FLOWING_RL = ResourceLocation.withDefaultNamespace("block/water_flow");
//	public static final ResourceLocation WATER_OVERLAY_RL = ResourceLocation.withDefaultNamespace("block/water_overlay");
//
//	public static final DeferredHolder<FlowingFluid, FlowingFluid> blood = FLUIDS.register("blood",
//			() -> new ForgeFlowingFluid.Source(FluidInit.blood_prop));
//
//	public static final DeferredHolder<FlowingFluid, FlowingFluid> blood_flowing = FLUIDS.register("blood_flowing",
//			() -> new ForgeFlowingFluid.Flowing(FluidInit.blood_prop));

//	public static final ForgeFlowingFluid.Properties blood_prop = new ForgeFlowingFluid.Properties(() -> blood.get(),
//			() -> blood_flowing.get(), FluidAttributes.builder(WATER_STILL_RL, WATER_FLOWING_RL).density(15)
//					.luminosity(2).viscosity(5).overlay(WATER_OVERLAY_RL).color(0xbf7c0000)).slopeFindDistance(2)
//							.levelDecreasePerBlock(0);
////
//	public static final DeferredHolder<LiquidBlock, LiquidBlock> blood_block = BlockInit.BASEBLOCKS.register("blood_block",
//			() -> new LiquidBlock(() -> FluidInit.blood.get(),
//					BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100f).noDrops()));

}

