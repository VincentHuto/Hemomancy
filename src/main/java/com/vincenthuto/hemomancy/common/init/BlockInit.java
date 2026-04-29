package com.vincenthuto.hemomancy.common.init;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.*;
import com.vincenthuto.hemomancy.common.block.puzzle.*;
import com.vincenthuto.hemomancy.common.block.crafting.ScarStationBlock;
import com.vincenthuto.hemomancy.common.block.crafting.GhastlyAlembicBlock;
import com.vincenthuto.hemomancy.common.block.crafting.PallidRetortBlock;
import com.vincenthuto.hemomancy.common.block.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.block.crafting.VialCentrifugeBlock;
import com.vincenthuto.hemomancy.common.block.functional.*;
import com.vincenthuto.hemomancy.common.block.idol.BlockHumaneIdol;
import com.vincenthuto.hemomancy.common.block.idol.BlockSerpentineIdol;
import com.vincenthuto.hemomancy.common.block.plant.*;
import com.vincenthuto.hemomancy.common.item.tile.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class BlockInit {
	public static final DeferredRegister<Block> BASEBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> SLABBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> STAIRBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> COLUMNBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> CROSSBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> OBJBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> MODELEDBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> SPECIALBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);

	public static final DeferredRegister<Block> POTTEDBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);

	/** Liquid/fluid blocks. Not included in creative-tab population or auto block-item registration. */
	public static final DeferredRegister<Block> LIQUIDBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);

	/**
	 * Blocks whose item form is managed entirely by {@link ItemInit} (or has no
	 * item form at all). Excluded from the auto block-item registration stream in
	 * {@link #getAllBlockEntriesAsStream()}.
	 */
	public static final DeferredRegister<Block> NOITEMBLOCKS = DeferredRegister.create(Registries.BLOCK,
			Hemomancy.MOD_ID);

	/**
	 * The placed form of the Sanguine Conduit. The corresponding item (registered
	 * in {@link ItemInit}) gates placement behind Initiatory Degree ≥ 5 and handles
	 * item consumption, so no separate BlockItem is auto-registered here.
	 */
	public static final DeferredHolder<Block, Block> sanguine_conduit = NOITEMBLOCKS.register("sanguine_conduit",
			SanguineConduitBlock::new);

	public static final DeferredHolder<Block, LiquidBlock> MORPHIC_NECTAR_BLOCK = LIQUIDBLOCKS.register(
			"morphic_nectar_block",
			() -> new LiquidBlock(
					FluidInit.MORPHIC_NECTAR.get(),
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.COLOR_GREEN)
							.replaceable()
							.noCollission()
							.strength(100f)
							.pushReaction(PushReaction.DESTROY)
							.noLootTable()
							.liquid()
							.sound(SoundType.EMPTY)));

	private static final ResourceKey<Block> GOURD_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, Hemomancy.rloc("gourd"));
	private static final ResourceKey<Block> GOURD_STEM_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, Hemomancy.rloc("gourd_stem"));
	private static final ResourceKey<Block> ATTACHED_GOURD_STEM_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, Hemomancy.rloc("attached_gourd_stem"));
	private static final ResourceKey<Item> GOURD_SEED_ITEM_KEY = ResourceKey.create(Registries.ITEM, Hemomancy.rloc("gourd_seeds"));

	// Ash
	public static final DeferredHolder<Block, Block> smouldering_ash_trail = SPECIALBLOCKS.register("smouldering_ash_trail",
			() -> new SmoulderingAshTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));

	public static final DeferredHolder<Block, Block> befouling_ash_trail = SPECIALBLOCKS.register("befouling_ash_trail",
			() -> new BefoulingAshTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));

	public static final DeferredHolder<Block, Block> active_smouldering_ash_trail = SPECIALBLOCKS.register(
			"active_smouldering_ash_trail",
			() -> new ActiveSmoulderingAshTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));
	public static final DeferredHolder<Block, Block> active_befouling_ash_trail = SPECIALBLOCKS.register(
			"active_befouling_ash_trail",
			() -> new ActiveBefoulingAshTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));

	public static final DeferredHolder<Block, Block> engram_block = SPECIALBLOCKS.register("engram_block", EngramBlock::new);

	public static final DeferredHolder<Block, Block> filler_block = SPECIALBLOCKS.register("filler_block",
			() -> new FillerBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion().noLootTable()));

	public static final DeferredHolder<Block, Block> qliphoth_bloom = SPECIALBLOCKS.register("qliphoth_bloom",
			() -> new com.vincenthuto.hemomancy.common.block.functional.QliphothBloomBlock(
					BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion().noLootTable()
							.lightLevel(state -> 7)));

	// Blocks

	public static final DeferredHolder<Block, Block> sanguine_glass = BASEBLOCKS.register("sanguine_glass",
			() -> new TransparentBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final DeferredHolder<Block, Block> sanguine_pane = SPECIALBLOCKS.register("sanguine_pane",
			() -> new IronBarsBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final DeferredHolder<Block, Block> vivianite_glass = BASEBLOCKS.register("vivianite_glass",
			() -> new TransparentBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final DeferredHolder<Block, Block> vivianite_pane = SPECIALBLOCKS.register("vivianite_pane",
			() -> new IronBarsBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final DeferredHolder<Block, Block> cleansed_sanguine_glass = BASEBLOCKS.register("cleansed_sanguine_glass",
			() -> new TransparentBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final DeferredHolder<Block, Block> cleansed_sanguine_pane = SPECIALBLOCKS.register("cleansed_sanguine_pane",
			() -> new IronBarsBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final DeferredHolder<Block, Block> venous_stone = BASEBLOCKS.register("venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> venous_stone_slab = SLABBLOCKS.register("venous_stone_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final DeferredHolder<Block, Block> venous_stone_stairs = BASEBLOCKS.register(("venous_stone_stairs"),
			() -> new StairBlock(venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.ofFullCopy(venous_stone.get())));

	public static final DeferredHolder<Block, Block> gilded_venous_stone = BASEBLOCKS.register("gilded_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone = BASEBLOCKS.register("polished_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone_slab = SLABBLOCKS.register(
			"polished_venous_stone_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone_stairs = STAIRBLOCKS.register(
			("polished_venous_stone_stairs"), () -> new StairBlock(venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.ofFullCopy(venous_stone.get())));
	public static final DeferredHolder<Block, Block> chiseled_polished_venous_stone = BASEBLOCKS.register(
			"chiseled_polished_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone_bricks = STAIRBLOCKS.register(
			"polished_venous_stone_bricks",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone_brick_slab = SLABBLOCKS.register(
			"polished_venous_stone_brick_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone_brick_stairs = STAIRBLOCKS.register(
			("polished_venous_stone_brick_stairs"), () -> new StairBlock(venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.ofFullCopy(venous_stone.get())));
	public static final DeferredHolder<Block, Block> cracked_polished_venous_stone_bricks = BASEBLOCKS.register(
			"cracked_polished_venous_stone_bricks",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> infested_venous_stone = BASEBLOCKS.register("infested_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> conscious_mass = BASEBLOCKS.register("conscious_mass",
			() -> new Block(BlockBehaviour.Properties.of().sound(SoundType.WART_BLOCK).strength(0.2F, 1.0F)));

	// Fungal dimension ores
	public static final DeferredHolder<Block, Block> hematic_iron_ore = BASEBLOCKS.register("hematic_iron_ore",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.0F, 3.0F)
					.sound(SoundType.NETHER_ORE)));

	// New fungal dimension blocks
	public static final DeferredHolder<Block, Block> calcified_hyphae = BASEBLOCKS.register("calcified_hyphae",
			() -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.BONE_BLOCK)));

	public static final DeferredHolder<Block, Block> hemorrhagic_crust = BASEBLOCKS.register("hemorrhagic_crust",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 4.0F).sound(SoundType.NETHERRACK)));

	public static final DeferredHolder<Block, Block> sporite_crystal = BASEBLOCKS.register("sporite_crystal",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 3.0F).sound(SoundType.AMETHYST)
					.lightLevel((state) -> 10).noOcclusion()));

	public static final DeferredHolder<Block, Block> hematic_iron_block = BASEBLOCKS.register("hematic_iron_block",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
					.sound(SoundType.METAL)));
	public static final DeferredHolder<Block, Block> hematic_iron_pillar = COLUMNBLOCKS.register("hematic_iron_pillar",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(5.0F, 6.0F).sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> chiseled_hematic_iron_block = BASEBLOCKS
			.register("chiseled_hematic_iron_block", () -> new Block(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> hemolytic_plating_block = BASEBLOCKS.register("hemolytic_plating_block",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
					.sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> crimson_flames = SPECIALBLOCKS.register("crimson_flames",
			() -> new CrimsonFlameBlock());

	public static final DeferredHolder<Block, Block> mnemonic_reliquary = SPECIALBLOCKS.register("mnemonic_reliquary",
			MnemonicReliquaryBlock::new);
	public static final DeferredHolder<Block, Block> dictation_table = SPECIALBLOCKS.register("dictation_table",
			DictationTableBlock::new);

	// Plants

	public static final DeferredHolder<Block, Block> blood_wood_log = COLUMNBLOCKS.register("blood_wood_log",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> blood_wood_planks = BASEBLOCKS.register("blood_wood_planks",
			() -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD)));

	public static final DeferredHolder<Block, Block> hyphae = CROSSBLOCKS.register("hyphae",
			() -> new HyphaeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission()
					.instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ).ignitedByLava()
					.pushReaction(PushReaction.DESTROY)));

	public static final DeferredHolder<Block, Block> bleeding_heart = CROSSBLOCKS.register("bleeding_heart",
			() -> new BleedingHeartBlock(MobEffects.ABSORPTION.value(), 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> potted_bleeding_heart = POTTEDBLOCKS.register("potted_bleeding_heart",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.bleeding_heart,
					BlockBehaviour.Properties.of()));

	public static final DeferredHolder<Block, Block> infected_fungus = CROSSBLOCKS.register("infected_fungus",
			() -> new InfectedFungusBlock(MobEffects.CONFUSION.value(), 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> potted_infected_fungus = POTTEDBLOCKS.register("potted_infected_fungus",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.infected_fungus,
					BlockBehaviour.Properties.of().noCollission()));

	public static final DeferredHolder<Block, Block> puffball_fungus = MODELEDBLOCKS.register("puffball_fungus",
			() -> new PuffballFungusBlock(MobEffects.SATURATION.value(), 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> potted_puffball_fungus = POTTEDBLOCKS.register("potted_puffball_fungus",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.puffball_fungus,
					BlockBehaviour.Properties.of().noCollission()));

	public static final DeferredHolder<Block, Block> stinkhorn_fungus = CROSSBLOCKS.register("stinkhorn_fungus",
			() -> new InfectedFungusBlock(MobEffects.CONFUSION.value(), 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> potted_stinkhorn_fungus = POTTEDBLOCKS.register("potted_stinkhorn_fungus",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.stinkhorn_fungus,
					BlockBehaviour.Properties.of().noCollission()));

	public static final DeferredHolder<Block, Block> devils_tooth = CROSSBLOCKS.register("devils_tooth",
			() -> new DevilsToothBlock(MobEffects.CONFUSION.value(), 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> lethean_poppy = CROSSBLOCKS.register("lethean_poppy",
			() -> new LetheanPoppyBlock(MobEffects.REGENERATION.value(), 8,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).randomTicks()));

	public static final DeferredHolder<Block, Block> erythrocytic_dirt = BASEBLOCKS.register("erythrocytic_dirt",
			() -> new Block(
					BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));

	public static final DeferredHolder<Block, Block> erythrocytic_mycelium = BASEBLOCKS.register("erythrocytic_mycelium",
			() -> new ErythrocyticMyceliumBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
					.randomTicks().strength(0.6F).sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> mycelium_erythrocytic_dirt = BASEBLOCKS.register("mycelium_erythrocytic_dirt",
			() -> new ErythrocyticMyceliumBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
					.randomTicks().strength(0.6F).sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> infected_stem = BASEBLOCKS.register("infected_stem",
			() -> new Block(BlockBehaviour.Properties.of().strength(0.5f, 15f).sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> hyphae_block = BASEBLOCKS.register("hyphae_block",
			() -> new Block(BlockBehaviour.Properties.of().strength(0.5f, 15f).sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> infected_cap = BASEBLOCKS.register("infected_cap",
			() -> new InfectedCapBlock(BlockBehaviour.Properties.of().strength(0.5f, 15f).sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> fruiting_infected_cap = BASEBLOCKS.register("fruiting_infected_cap",
			() -> new InfectedCapBlock(BlockBehaviour.Properties.of().strength(0.5f, 15f).sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> infested_wood = BASEBLOCKS.register("infested_wood",
			() -> new InfestedWoodBlock(BlockBehaviour.Properties.of().strength(1.5F, 3.0F).sound(SoundType.WOOD)
					.randomTicks()));

	// Myco-heterotrophic Plants

	public static final DeferredHolder<Block, Block> ghost_pipe = CROSSBLOCKS.register("ghost_pipe",
			() -> new GhostPipeBlock(MobEffects.NIGHT_VISION.value(), 10,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).randomTicks()));

	public static final DeferredHolder<Block, Block> sarcodes = CROSSBLOCKS.register("sarcodes",
			() -> new SarcodesBlock(MobEffects.REGENERATION.value(), 8,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> rafflesia = CROSSBLOCKS.register("rafflesia",
			() -> new RafflesiaBlock(MobEffects.CONFUSION.value(), 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).randomTicks()));

	public static final DeferredHolder<Block, Block> potted_ghost_pipe = POTTEDBLOCKS.register("potted_ghost_pipe",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.ghost_pipe,
					BlockBehaviour.Properties.of().noCollission()));

	public static final DeferredHolder<Block, Block> potted_sarcodes = POTTEDBLOCKS.register("potted_sarcodes",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.sarcodes,
					BlockBehaviour.Properties.of().noCollission()));

	public static final DeferredHolder<Block, Block> potted_lethean_poppy = POTTEDBLOCKS.register("potted_lethean_poppy",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.lethean_poppy,
					BlockBehaviour.Properties.of().noCollission()));

	// Gourd
	public static final DeferredHolder<Block, Block> gourd = MODELEDBLOCKS.register("gourd",
			() -> new GourdBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
					.instrument(NoteBlockInstrument.DIDGERIDOO).strength(1.0F).sound(SoundType.WOOD)
					.pushReaction(PushReaction.DESTROY)));

	public static final DeferredHolder<Block, Block> attached_gourd_stem = MODELEDBLOCKS.register("attached_gourd_stem",
			() -> new AttachedStemBlock(GOURD_BLOCK_KEY, GOURD_STEM_BLOCK_KEY, GOURD_SEED_ITEM_KEY, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.WOOD)
					.pushReaction(PushReaction.DESTROY)));

	public static final DeferredHolder<Block, Block> gourd_stem = MODELEDBLOCKS.register("gourd_stem",
			() -> new StemBlock(GOURD_BLOCK_KEY, ATTACHED_GOURD_STEM_BLOCK_KEY, GOURD_SEED_ITEM_KEY, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak()
					.sound(SoundType.HARD_CROP).pushReaction(PushReaction.DESTROY)));

	// Idols
	public static final DeferredHolder<Block, Block> humane_idol = MODELEDBLOCKS.register("humane_idol",
			() -> new BlockHumaneIdol(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> serpentine_idol = MODELEDBLOCKS.register("serpentine_idol",
			() -> new BlockSerpentineIdol(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	// Tiles
	public static final DeferredHolder<Block, Block> scar_station = MODELEDBLOCKS.register("scar_station",
			() -> new ScarStationBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE).noOcclusion()));

	public static final DeferredHolder<Block, Block> morphling_incubator = MODELEDBLOCKS.register("morphling_incubator",
			() -> new MorphlingIncubatorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> morphling_cradle = MODELEDBLOCKS.register("morphling_cradle",
			() -> new MorphlingCradleBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> semi_sentient_construct = MODELEDBLOCKS
			.register("semi_sentient_construct", () -> new SemiSentientConstructBlock(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> unstained_podium = MODELEDBLOCKS.register("unstained_podium",
			() -> new UnstainedPodiumBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> altar_of_cleansing = MODELEDBLOCKS.register("altar_of_cleansing",
			() -> new AltarOfCleansingBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 8.0F).sound(SoundType.STONE).lightLevel(s -> 7).noOcclusion()));

	public static final DeferredHolder<Block, Block> cleansed_stone = BASEBLOCKS.register("cleansed_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> pale_silver_block = BASEBLOCKS.register("pale_silver_block",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 6.0F).sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> pale_silver_bells = BASEBLOCKS.register("pale_silver_bells",
			() -> new PaleSilverBellsBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 5.0F).sound(SoundType.METAL).lightLevel(s -> 10)));

	public static final DeferredHolder<Block, Block> pallid_lantern = BASEBLOCKS.register("pallid_lantern",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.0F, 3.0F).sound(SoundType.LANTERN).lightLevel(s -> 12)));

	public static final DeferredHolder<Block, Block> scrying_podium = MODELEDBLOCKS.register("scrying_podium",
			() -> new ScryingPodiumBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> fungal_podium = MODELEDBLOCKS.register("fungal_podium",
			() -> new FungalPodiumBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> saint_sarcophagus = MODELEDBLOCKS.register("saint_sarcophagus",
			() -> new SaintSarcophagusBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 8.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> sanguine_monolith = MODELEDBLOCKS.register("sanguine_monolith",
			() -> new SanguineMonolithBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 8.0F).sound(SoundType.STONE).lightLevel(s -> 5).noOcclusion()));

	public static final DeferredHolder<Block, Block> fungal_implantation_pylon = MODELEDBLOCKS
			.register("fungal_implantation_pylon", () -> new FungalImplantationPylonBlock(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> dendritic_distributor = MODELEDBLOCKS.register("dendritic_distributor",
			() -> new DendriticDistributorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> mortal_display = MODELEDBLOCKS.register("mortal_display",
			() -> new MortalDisplayBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> somatic_loom = MODELEDBLOCKS
			.register("somatic_loom", () -> new SomaticLoomBlock(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> earthen_vein = MODELEDBLOCKS.register("earthen_vein",
			() -> new EarthenVeinBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> iron_brazier = MODELEDBLOCKS.register("iron_brazier",
			() -> new BrazierBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.METAL)));
	public static final DeferredHolder<Block, Block> ghastly_alembic = MODELEDBLOCKS.register("ghastly_alembic",
			() -> new GhastlyAlembicBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.METAL)));
	public static final DeferredHolder<Block, Block> pallid_retort = MODELEDBLOCKS.register("pallid_retort",
			() -> new PallidRetortBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> vial_centrifuge = MODELEDBLOCKS.register("vial_centrifuge",
			() -> new VialCentrifugeBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> blood_crystal = MODELEDBLOCKS.register("blood_crystal",
			() -> new BloodCrystalBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> suspended_vivianite = MODELEDBLOCKS.register("suspended_vivianite",
			() -> new SuspendedVivianiteBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.AMETHYST)));

	public static final DeferredHolder<Block, Block> suspended_blood_crystal = MODELEDBLOCKS.register("suspended_blood_crystal",
			() -> new SuspendedBloodCrystalBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.AMETHYST)));

	public static final DeferredHolder<Block, Block> suspended_cleansed_blood_crystal = MODELEDBLOCKS.register("suspended_cleansed_blood_crystal",
			() -> new SuspendedCleansedBloodCrystalBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.AMETHYST)));

	public static final DeferredHolder<Block, Block> bog_body = MODELEDBLOCKS.register("bog_body",
			() -> new BogBodyBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.5F, 6.0F).sound(SoundType.WART_BLOCK)));

	public static final DeferredHolder<Block, Block> visceral_mirror = MODELEDBLOCKS.register("visceral_mirror",
			() -> new VisceralMirrorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.noOcclusion().strength(2.0F, 8.0F).sound(SoundType.GLASS)));

	// Puzzle / boss-room blocks â€” Hemorath encounter
	public static final DeferredHolder<Block, Block> blood_basin = MODELEDBLOCKS.register("blood_basin",
			() -> new BloodBasinBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> offering_gate = SPECIALBLOCKS.register("offering_gate",
			() -> new OfferingGateBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 8.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> blood_pylon = MODELEDBLOCKS.register("blood_pylon",
			() -> new BloodPylonBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> blood_trial_altar = MODELEDBLOCKS.register("blood_trial_altar",
			() -> new BloodTrialAltarBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 8.0F).sound(SoundType.STONE)));

	public static List<Block> getAllBlockEntries() {
		List<Block> blocks = new ArrayList<>();
		BASEBLOCKS.getEntries().stream().map(e -> e.get()).forEach(b -> blocks.add(b));
		SLABBLOCKS.getEntries().stream().map(e -> e.get()).forEach(b -> blocks.add(b));
		STAIRBLOCKS.getEntries().stream().map(e -> e.get()).forEach(b -> blocks.add(b));
		COLUMNBLOCKS.getEntries().stream().map(e -> e.get()).forEach(b -> blocks.add(b));
		CROSSBLOCKS.getEntries().stream().map(e -> e.get()).forEach(b -> blocks.add(b));
		OBJBLOCKS.getEntries().stream().map(e -> e.get()).forEach(b -> blocks.add(b));
		MODELEDBLOCKS.getEntries().stream().map(e -> e.get()).forEach(b -> blocks.add(b));
		SPECIALBLOCKS.getEntries().stream().map(e -> e.get()).forEach(b -> blocks.add(b));
		POTTEDBLOCKS.getEntries().stream().map(e -> e.get()).forEach(b -> blocks.add(b));

		return blocks;
	}

	public static Stream<DeferredHolder<Block, ? extends Block>> getAllBlockEntriesAsStream() {

		Stream<DeferredHolder<Block, ? extends Block>> combinedStream = Stream
				.<Collection<? extends DeferredHolder<Block, ? extends Block>>>of(BASEBLOCKS.getEntries(), SLABBLOCKS.getEntries(),
				STAIRBLOCKS.getEntries(), COLUMNBLOCKS.getEntries(), CROSSBLOCKS.getEntries(),
				MODELEDBLOCKS.getEntries(), SPECIALBLOCKS.getEntries(), OBJBLOCKS.getEntries())
				.flatMap(Collection::stream);

		return combinedStream;
	}

	public static Pair<ResourceLocation, BlockItem> createItemBlock(Pair<Block, ResourceLocation> block) {
		var b = block.getFirst();
		if (b == BlockInit.mortal_display.get()) {
			return Pair.of(block.getSecond(), new MortalDisplayBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.suspended_blood_crystal.get()) {
			return Pair.of(block.getSecond(), new SuspendedBloodCrystalBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.suspended_cleansed_blood_crystal.get()) {
			return Pair.of(block.getSecond(), new SuspendedCleansedBloodCrystalBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.suspended_vivianite.get()) {
			return Pair.of(block.getSecond(), new SuspendedVivianiteBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.visceral_mirror.get()) {
			return Pair.of(block.getSecond(), new VisceralMirrorBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.fungal_implantation_pylon.get()) {
			return Pair.of(block.getSecond(), new FungalImplantationPylonBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.mnemonic_reliquary.get()) {
			return Pair.of(block.getSecond(), new MnemonicReliquaryBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.dictation_table.get()) {
			return Pair.of(block.getSecond(), new DictationTableBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.earthen_vein.get()) {
			return Pair.of(block.getSecond(), new EarthenVeinBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.vial_centrifuge.get()) {
			return Pair.of(block.getSecond(), new VialCentrifugeBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.ghastly_alembic.get()) {
			return Pair.of(block.getSecond(), new GhastlyAlembicBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.pallid_retort.get()) {
			return Pair.of(block.getSecond(), new PallidRetortBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.scar_station.get()) {
			return Pair.of(block.getSecond(), new ScarStationBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.morphling_incubator.get()) {
			return Pair.of(block.getSecond(), new MorphlingIncubatorBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.morphling_cradle.get()) {
			return Pair.of(block.getSecond(), new MorphlingCradleBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.altar_of_cleansing.get()) {
			return Pair.of(block.getSecond(), new AltarOfCleansingBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.saint_sarcophagus.get()) {
			return Pair.of(block.getSecond(), new SaintSarcophagusBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.sanguine_monolith.get()) {
			return Pair.of(block.getSecond(), new SanguineMonolithBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.somatic_loom.get()) {
			return Pair.of(block.getSecond(), new SomaticLoomBlockItem(b, new Item.Properties()));
		}
		return Pair.of(block.getSecond(), new BlockItem(b, new Item.Properties()));
	}

	@SubscribeEvent
	public static void onRegisterItems(final RegisterEvent event) {
		if (event.getRegistryKey() != Registries.ITEM) {
			return;
		}

		var b = getAllBlockEntriesAsStream().map(m -> new Pair<>(m.get(), m.getId()))
				.map(BlockInit::createItemBlock);
		b.forEach(item -> {
			if (item.getSecond().getBlock() != BlockInit.attached_gourd_stem.get()
					|| item.getSecond().getBlock() != BlockInit.gourd_stem.get()
					|| item.getSecond().getBlock() != BlockInit.active_befouling_ash_trail.get()
					|| item.getSecond().getBlock() != BlockInit.active_smouldering_ash_trail.get()
					|| item.getSecond().getBlock() != BlockInit.engram_block.get()
					|| item.getSecond().getBlock() != BlockInit.filler_block.get()
					|| item.getSecond().getBlock() != BlockInit.qliphoth_bloom.get()) {
				registerBlockItem(event, item);
			}
		});
	}

	private static void registerBlockItem(RegisterEvent event, Pair<ResourceLocation, BlockItem> item) {
		event.register(Registries.ITEM, helper -> helper.register(item.getFirst(), item.getSecond()));
	}


	@SubscribeEvent
	public static void registerBlocks(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.bleeding_heart.getId(),
					BlockInit.potted_bleeding_heart);
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.infected_fungus.getId(),
					BlockInit.potted_infected_fungus);
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.stinkhorn_fungus.getId(),
					BlockInit.potted_stinkhorn_fungus);
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.puffball_fungus.getId(),
					BlockInit.potted_puffball_fungus);
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.ghost_pipe.getId(),
					BlockInit.potted_ghost_pipe);
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.sarcodes.getId(),
					BlockInit.potted_sarcodes);
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.lethean_poppy.getId(),
					BlockInit.potted_lethean_poppy);
		});
	}

}
