package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.block.BlockBefoulingAshTrail;
import com.huto.hemomancy.block.BlockChiselStation;
import com.huto.hemomancy.block.BlockDendriticDistributor;
import com.huto.hemomancy.block.BlockMorphlingIncubator;
import com.huto.hemomancy.block.BlockRuneModStation;
import com.huto.hemomancy.block.BlockSemiSentientConstruct;
import com.huto.hemomancy.block.BlockSmoulderingAshTrail;
import com.huto.hemomancy.block.BlockUnstainedPodium;
import com.huto.hemomancy.block.idol.BlockSerpentineIdol;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class BlockInit {

	public static final DeferredRegister<Block> BASEBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> COLUMNBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> CROSSBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> OBJBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> MODELEDBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> SPECIALBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);

	// Ash
	public static final RegistryObject<Block> smouldering_ash_trail = SPECIALBLOCKS.register("smouldering_ash_trail",
			() -> new BlockSmoulderingAshTrail(AbstractBlock.Properties.create(Material.MISCELLANEOUS)
					.doesNotBlockMovement().zeroHardnessAndResistance()));
	public static final RegistryObject<Block> befouling_ash_trail = SPECIALBLOCKS.register("befouling_ash_trail",
			() -> new BlockBefoulingAshTrail(AbstractBlock.Properties.create(Material.MISCELLANEOUS)
					.doesNotBlockMovement().zeroHardnessAndResistance()));
	// Blocks
	public static final RegistryObject<Block> sanguine_glass = BASEBLOCKS.register("sanguine_glass",
			() -> new GlassBlock(Block.Properties.create(Material.GLASS).hardnessAndResistance(0.1f, 1f)
					.sound(SoundType.GLASS).notSolid()));
	public static final RegistryObject<Block> sanguine_pane = SPECIALBLOCKS.register("sanguine_pane",
			() -> new PaneBlock(Block.Properties.create(Material.GLASS).hardnessAndResistance(0.1f, 1f)
					.sound(SoundType.GLASS).notSolid()));
	public static final RegistryObject<Block> venous_stone = BASEBLOCKS.register("venous_stone",
			() -> new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE).setRequiresTool()
					.hardnessAndResistance(1.5F, 6.0F)));
	public static final RegistryObject<Block> infested_venous_stone = BASEBLOCKS.register("infested_venous_stone",
			() -> new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE).setRequiresTool()
					.hardnessAndResistance(1.5F, 6.0F)));
	public static final RegistryObject<Block> conscious_mass = BASEBLOCKS.register("conscious_mass",
			() -> new Block(AbstractBlock.Properties.create(Material.MISCELLANEOUS, MaterialColor.PINK)
					.sound(SoundType.WART).hardnessAndResistance(0.2F, 1.0F)));
	public static final RegistryObject<Block> tainted_iron_block = BASEBLOCKS.register("tainted_iron_block",
			() -> new Block(AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON).setRequiresTool()
					.hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> chiseled_tainted_iron_block = BASEBLOCKS.register(
			"chiseled_tainted_iron_block",
			() -> new Block(AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON).setRequiresTool()
					.hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> infected_stem = BASEBLOCKS.register("infected_stem", () -> new Block(
			Block.Properties.create(Material.PLANTS).hardnessAndResistance(0.5f, 15f).sound(SoundType.PLANT)));
	public static final RegistryObject<Block> infected_cap = BASEBLOCKS.register("infected_cap", () -> new Block(
			Block.Properties.create(Material.PLANTS).hardnessAndResistance(0.5f, 15f).sound(SoundType.PLANT)));

	// Idols
	public static final RegistryObject<Block> serpentine_idol = MODELEDBLOCKS.register("serpentine_idol",
			() -> new BlockSerpentineIdol(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));

	// Tiles
	public static final RegistryObject<Block> runic_chisel_station = MODELEDBLOCKS.register("runic_chisel_station",
			() -> new BlockChiselStation(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> morphling_incubator = MODELEDBLOCKS.register("morphling_incubator",
			() -> new BlockMorphlingIncubator(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> rune_mod_station = MODELEDBLOCKS.register("rune_mod_station",
			() -> new BlockRuneModStation(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));
	public static final RegistryObject<Block> semi_sentient_construct = MODELEDBLOCKS
			.register("semi_sentient_construct", () -> new BlockSemiSentientConstruct(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));
	public static final RegistryObject<Block> unstained_podium = MODELEDBLOCKS.register("unstained_podium",
			() -> new BlockUnstainedPodium(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));
	public static final RegistryObject<Block> dendritic_distributor = MODELEDBLOCKS.register("dendritic_distributor",
			() -> new BlockDendriticDistributor(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));

	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			RenderTypeLookup.setRenderLayer(BlockInit.sanguine_glass.get(), RenderType.getTranslucent());
			RenderTypeLookup.setRenderLayer(BlockInit.sanguine_pane.get(), RenderType.getTranslucent());
			RenderTypeLookup.setRenderLayer(BlockInit.smouldering_ash_trail.get(), RenderType.getCutoutMipped());
			RenderTypeLookup.setRenderLayer(BlockInit.befouling_ash_trail.get(), RenderType.getCutoutMipped());
			RenderTypeLookup.setRenderLayer(BlockInit.rune_mod_station.get(), RenderType.getTranslucent());
			RenderTypeLookup.setRenderLayer(BlockInit.semi_sentient_construct.get(), RenderType.getTranslucent());
			RenderTypeLookup.setRenderLayer(BlockInit.morphling_incubator.get(), RenderType.getTranslucent());

		}
	}

}
