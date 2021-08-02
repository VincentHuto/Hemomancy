package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.block.BlockActiveBefoulingAshTrail;
import com.huto.hemomancy.block.BlockActiveSmoulderingAshTrail;
import com.huto.hemomancy.block.BlockBefoulingAshTrail;
import com.huto.hemomancy.block.BlockBleedingHeart;
import com.huto.hemomancy.block.BlockChiselStation;
import com.huto.hemomancy.block.BlockCrimsonFlame;
import com.huto.hemomancy.block.BlockDendriticDistributor;
import com.huto.hemomancy.block.BlockMorphlingIncubator;
import com.huto.hemomancy.block.BlockMortalDisplay;
import com.huto.hemomancy.block.BlockRuneModStation;
import com.huto.hemomancy.block.BlockSemiSentientConstruct;
import com.huto.hemomancy.block.BlockSmoulderingAshTrail;
import com.huto.hemomancy.block.BlockUnstainedPodium;
import com.huto.hemomancy.block.BlockVisceralRecaller;
import com.huto.hemomancy.block.idol.BlockHumaneIdol;
import com.huto.hemomancy.block.idol.BlockSerpentineIdol;

import net.minecraft.block.PaneBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.potion.Effects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class BlockInit {
	public static final DeferredRegister<Block> BASEBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> SLABBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> STAIRBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
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
			() -> new BlockSmoulderingAshTrail(BlockBehaviour.Properties.create(Material.MISCELLANEOUS)
					.doesNotBlockMovement().zeroHardnessAndResistance()));
	public static final RegistryObject<Block> befouling_ash_trail = SPECIALBLOCKS.register("befouling_ash_trail",
			() -> new BlockBefoulingAshTrail(AbstractBlock.Properties.create(Material.MISCELLANEOUS)
					.doesNotBlockMovement().zeroHardnessAndResistance()));

	public static final RegistryObject<Block> active_smouldering_ash_trail = SPECIALBLOCKS
			.register("active_smouldering_ash_trail", () -> new BlockActiveSmoulderingAshTrail(AbstractBlock.Properties
					.create(Material.MISCELLANEOUS).doesNotBlockMovement().zeroHardnessAndResistance()));
	public static final RegistryObject<Block> active_befouling_ash_trail = SPECIALBLOCKS
			.register("active_befouling_ash_trail", () -> new BlockActiveBefoulingAshTrail(AbstractBlock.Properties
					.create(Material.MISCELLANEOUS).doesNotBlockMovement().zeroHardnessAndResistance()));

	// Blocks
	public static final RegistryObject<Block> bleeding_heart = CROSSBLOCKS.register("bleeding_heart",
			() -> new BlockBleedingHeart(Effects.ABSORPTION, 12, AbstractBlock.Properties.create(Material.PLANTS)
					.doesNotBlockMovement().zeroHardnessAndResistance().sound(SoundType.PLANT)));
	public static final RegistryObject<Block> sanguine_glass = BASEBLOCKS.register("sanguine_glass",
			() -> new GlassBlock(Block.Properties.create(Material.GLASS).hardnessAndResistance(0.1f, 1f)
					.sound(SoundType.GLASS).notSolid()));
	public static final RegistryObject<Block> sanguine_pane = SPECIALBLOCKS.register("sanguine_pane",
			() -> new PaneBlock(Block.Properties.create(Material.GLASS).hardnessAndResistance(0.1f, 1f)
					.sound(SoundType.GLASS).notSolid()));

	public static final RegistryObject<Block> venous_stone = BASEBLOCKS.register("venous_stone",
			() -> new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE).setRequiresTool()
					.hardnessAndResistance(1.5F, 6.0F)));
	public static final RegistryObject<Block> venous_stone_slab = SLABBLOCKS.register("venous_stone_slab",
			() -> new SlabBlock(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE).setRequiresTool()
					.hardnessAndResistance(1.5f, 6.0F)));
	public static final RegistryObject<Block> venous_stone_stairs = BASEBLOCKS.register(("venous_stone_stairs"),
			() -> new StairsBlock(venous_stone.get().getDefaultState(),
					AbstractBlock.Properties.from(venous_stone.get())));

	public static final RegistryObject<Block> gilded_venous_stone = BASEBLOCKS.register("gilded_venous_stone",
			() -> new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE).setRequiresTool()
					.hardnessAndResistance(1.5F, 6.0F)));

	public static final RegistryObject<Block> polished_venous_stone = BASEBLOCKS.register("polished_venous_stone",
			() -> new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE).setRequiresTool()
					.hardnessAndResistance(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_slab = SLABBLOCKS
			.register("polished_venous_stone_slab", () -> new SlabBlock(AbstractBlock.Properties
					.create(Material.ROCK, MaterialColor.STONE).setRequiresTool().hardnessAndResistance(1.5f, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_stairs = STAIRBLOCKS
			.register(("polished_venous_stone_stairs"), () -> new StairsBlock(venous_stone.get().getDefaultState(),
					AbstractBlock.Properties.from(venous_stone.get())));
	public static final RegistryObject<Block> chiseled_polished_venous_stone = BASEBLOCKS
			.register("chiseled_polished_venous_stone", () -> new Block(AbstractBlock.Properties
					.create(Material.ROCK, MaterialColor.STONE).setRequiresTool().hardnessAndResistance(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_bricks = STAIRBLOCKS
			.register("polished_venous_stone_bricks", () -> new Block(AbstractBlock.Properties
					.create(Material.ROCK, MaterialColor.STONE).setRequiresTool().hardnessAndResistance(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_brick_slab = SLABBLOCKS
			.register("polished_venous_stone_brick_slab", () -> new SlabBlock(AbstractBlock.Properties
					.create(Material.ROCK, MaterialColor.STONE).setRequiresTool().hardnessAndResistance(1.5f, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_brick_stairs = STAIRBLOCKS.register(
			("polished_venous_stone_brick_stairs"), () -> new StairsBlock(venous_stone.get().getDefaultState(),
					AbstractBlock.Properties.from(venous_stone.get())));
	public static final RegistryObject<Block> cracked_polished_venous_stone_bricks = BASEBLOCKS
			.register("cracked_polished_venous_stone_bricks", () -> new Block(AbstractBlock.Properties
					.create(Material.ROCK, MaterialColor.STONE).setRequiresTool().hardnessAndResistance(1.5F, 6.0F)));
	public static final RegistryObject<Block> infested_venous_stone = BASEBLOCKS.register("infested_venous_stone",
			() -> new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE).setRequiresTool()
					.hardnessAndResistance(1.5F, 6.0F)));
	public static final RegistryObject<Block> conscious_mass = BASEBLOCKS.register("conscious_mass",
			() -> new Block(AbstractBlock.Properties.create(Material.MISCELLANEOUS, MaterialColor.PINK)
					.sound(SoundType.WART).hardnessAndResistance(0.2F, 1.0F)));
	public static final RegistryObject<Block> hematic_iron_block = BASEBLOCKS.register("hematic_iron_block",
			() -> new Block(AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON).setRequiresTool()
					.hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> hematic_iron_pillar = COLUMNBLOCKS.register("hematic_iron_pillar",
			() -> new RotatedPillarBlock(Block.Properties.create(Material.IRON, MaterialColor.IRON).setRequiresTool()
					.hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> chiseled_hematic_iron_block = BASEBLOCKS.register(
			"chiseled_hematic_iron_block",
			() -> new Block(AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON).setRequiresTool()
					.hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));

	public static final RegistryObject<Block> blood_wood_log = COLUMNBLOCKS.register("blood_wood_log",
			() -> new RotatedPillarBlock(Block.Properties.create(Material.WOOD, MaterialColor.BROWN)
					.hardnessAndResistance(2.0F, 3.0F).sound(SoundType.WOOD)));
	
	public static final RegistryObject<Block> blood_wood_planks = BASEBLOCKS.register("blood_wood_planks",
			() -> new Block(AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
					.hardnessAndResistance(2.0F, 3.0F).sound(SoundType.WOOD)));

	
	

	public static final RegistryObject<Block> infected_stem = BASEBLOCKS.register("infected_stem", () -> new Block(
			Block.Properties.create(Material.PLANTS).hardnessAndResistance(0.5f, 15f).sound(SoundType.PLANT)));
	public static final RegistryObject<Block> infected_cap = BASEBLOCKS.register("infected_cap", () -> new Block(
			Block.Properties.create(Material.PLANTS).hardnessAndResistance(0.5f, 15f).sound(SoundType.PLANT)));
	public static final RegistryObject<Block> crimson_flames = MODELEDBLOCKS.register("crimson_flames",
			() -> new BlockCrimsonFlame(Block.Properties.from(Blocks.FIRE), 1.5f));

	// Idols
	public static final RegistryObject<Block> humane_idol = MODELEDBLOCKS.register("humane_idol",
			() -> new BlockHumaneIdol(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));
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
	public static final RegistryObject<Block> mortal_display = MODELEDBLOCKS.register("mortal_display",
			() -> new BlockMortalDisplay(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> visceral_artificial_recaller = MODELEDBLOCKS
			.register("visceral_artificial_recaller", () -> new BlockVisceralRecaller(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));

	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			RenderTypeLookup.setRenderLayer(BlockInit.sanguine_glass.get(), RenderType.getTranslucent());
			RenderTypeLookup.setRenderLayer(BlockInit.sanguine_pane.get(), RenderType.getTranslucent());
			RenderTypeLookup.setRenderLayer(BlockInit.smouldering_ash_trail.get(), RenderType.getCutoutMipped());
			RenderTypeLookup.setRenderLayer(BlockInit.befouling_ash_trail.get(), RenderType.getCutoutMipped());
			RenderTypeLookup.setRenderLayer(BlockInit.active_smouldering_ash_trail.get(), RenderType.getCutoutMipped());
			RenderTypeLookup.setRenderLayer(BlockInit.active_befouling_ash_trail.get(), RenderType.getCutoutMipped());
			RenderTypeLookup.setRenderLayer(BlockInit.rune_mod_station.get(), RenderType.getTranslucent());
			RenderTypeLookup.setRenderLayer(BlockInit.semi_sentient_construct.get(), RenderType.getTranslucent());
			RenderTypeLookup.setRenderLayer(BlockInit.morphling_incubator.get(), RenderType.getTranslucent());
			RenderTypeLookup.setRenderLayer(BlockInit.crimson_flames.get(), RenderType.getCutoutMipped());
			RenderTypeLookup.setRenderLayer(BlockInit.bleeding_heart.get(), RenderType.getCutoutMipped());
			RenderTypeLookup.setRenderLayer(BlockInit.visceral_artificial_recaller.get(), RenderType.getTranslucent());

		}
	}

}
