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



//package com.vincenthuto.hemomancy.init;
//
//import com.vincenthuto.hemomancy.Hemomancy;
//
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.level.material.FlowingFluid;
//import net.minecraft.world.level.material.Fluid;
//import net.neoforged.neoforge.fluids.FluidType;
//import net.neoforged.neoforge.fluids.ForgeFlowingFluid;
//import net.neoforged.neoforge.registries.DeferredRegister;
//import net.neoforged.neoforge.registries.ForgeRegistries;
//import net.neoforged.neoforge.registries.DeferredHolder;
//
//public class FluidInit {
//
//	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS,
//			Hemomancy.MOD_ID);
//
//	public static final DeferredHolder<FluidType, FluidType> BLOOD_TYPE = RegistryObject.createOptional(
//			ResourceLocation.withDefaultNamespace("blood"), ForgeRegistries.Keys.FLUID_TYPES.location(), Hemomancy.MOD_ID);
//	public static final DeferredHolder<Fluid, Fluid> BLOOD = RegistryObject.create(ResourceLocation.withDefaultNamespace("blood"),
//			ForgeRegistries.FLUIDS);
//	public static final DeferredHolder<Fluid, Fluid> FLOWING_BLOOD = RegistryObject
//			.create(ResourceLocation.withDefaultNamespace("flowing_blood"), ForgeRegistries.FLUIDS);
//
//	public static final DeferredHolder<FlowingFluid, FlowingFluid> blood = FLUIDS.register("blood",
//			() -> new ForgeFlowingFluid.Source(FluidInit.blood_prop));
//
//	public static final DeferredHolder<FlowingFluid, FlowingFluid> blood_flowing = FLUIDS.register("blood_flowing",
//			() -> new ForgeFlowingFluid.Flowing(FluidInit.blood_prop));
//
//	public static final ForgeFlowingFluid.Properties blood_prop = new ForgeFlowingFluid.Properties(BLOOD_TYPE, BLOOD,
//			FLOWING_BLOOD).bucket(() -> Items.BUCKET);
//
//	ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(BLOOD_TYPE, BLOOD, FLOWING_BLOOD)
//			.bucket(() -> Items.BUCKET);
//
////
////	public static final DeferredHolder<LiquidBlock, LiquidBlock> blood_block = BlockInit.BASEBLOCKS.register("blood_block",
////			() -> new LiquidBlock(() -> FluidInit.blood.get(),
////					BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100f).noDrops()));
//
//}
