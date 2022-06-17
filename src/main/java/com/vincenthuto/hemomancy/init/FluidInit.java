package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidInit {

	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS,
			Hemomancy.MOD_ID);

//	public static final ResourceLocation WATER_STILL_RL = new ResourceLocation("block/water_still");
//	public static final ResourceLocation WATER_FLOWING_RL = new ResourceLocation("block/water_flow");
//	public static final ResourceLocation WATER_OVERLAY_RL = new ResourceLocation("block/water_overlay");
//
//	public static final RegistryObject<FlowingFluid> blood = FLUIDS.register("blood",
//			() -> new ForgeFlowingFluid.Source(FluidInit.blood_prop));
//
//	public static final RegistryObject<FlowingFluid> blood_flowing = FLUIDS.register("blood_flowing",
//			() -> new ForgeFlowingFluid.Flowing(FluidInit.blood_prop));

//	public static final ForgeFlowingFluid.Properties blood_prop = new ForgeFlowingFluid.Properties(() -> blood.get(),
//			() -> blood_flowing.get(), FluidAttributes.builder(WATER_STILL_RL, WATER_FLOWING_RL).density(15)
//					.luminosity(2).viscosity(5).overlay(WATER_OVERLAY_RL).color(0xbf7c0000)).slopeFindDistance(2)
//							.levelDecreasePerBlock(0);
////
//	public static final RegistryObject<LiquidBlock> blood_block = BlockInit.BASEBLOCKS.register("blood_block",
//			() -> new LiquidBlock(() -> FluidInit.blood.get(),
//					BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100f).noDrops()));

}
