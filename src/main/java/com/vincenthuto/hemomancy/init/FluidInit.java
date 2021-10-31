package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidInit {

	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS,
			Hemomancy.MOD_ID);

	public static final ResourceLocation WATER_STILL_RL = new ResourceLocation("block/water_still");
	public static final ResourceLocation WATER_FLOWING_RL = new ResourceLocation("block/water_flow");
	public static final ResourceLocation WATER_OVERLAY_RL = new ResourceLocation("block/water_overlay");

	public static final RegistryObject<FlowingFluid> test_fluid = FLUIDS.register("test_fluid",
			() -> new ForgeFlowingFluid.Source(FluidInit.test_fluid_prop));

	public static final RegistryObject<FlowingFluid> test_fluid_flowing = FLUIDS.register("test_fluid_flowing",
			() -> new ForgeFlowingFluid.Flowing(FluidInit.test_fluid_prop));

	public static final ForgeFlowingFluid.Properties test_fluid_prop = new ForgeFlowingFluid.Properties(
			() -> test_fluid.get(), () -> test_fluid_flowing.get(),
			FluidAttributes.builder(WATER_STILL_RL, WATER_FLOWING_RL).density(15).luminosity(2).viscosity(5)
					.overlay(WATER_OVERLAY_RL).color(0xbffed0d0)).slopeFindDistance(2).levelDecreasePerBlock(0)
							.canMultiply().block(() -> FluidInit.test_fluid_block.get());

	public static final RegistryObject<LiquidBlock> test_fluid_block = BlockInit.BASEBLOCKS.register("test_fluid_block",
			() -> new LiquidBlock(() -> FluidInit.test_fluid.get(),
					BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100f).noDrops()));

}
