package com.vincenthuto.hemomancy.common.init;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.BloodStainedStoneMarkerBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.BloodScabPointedDripstoneBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.EscharianOvergrowthBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.CrimsonFlameBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.*;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.*;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.*;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.*;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.*;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.*;
import com.vincenthuto.hemomancy.common.block.inscription.DictationTableBlock;
import com.vincenthuto.hemomancy.common.block.inscription.DiscoveryInscriptionBlock;
import com.vincenthuto.hemomancy.common.block.shared.FillerBlock;
import com.vincenthuto.hemomancy.common.block.shared.DormantGanglionBlock;
import com.vincenthuto.hemomancy.common.block.shared.HemoWallBlock;
import com.vincenthuto.hemomancy.common.block.shared.MnemonicCandleBlock;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedDoorBlock;
import com.vincenthuto.hemomancy.common.block.unstained.SuspendedCleansedBloodCrystalBlock;
import com.vincenthuto.hemomancy.common.block.unstained.ViridSalisTrailBlock;
import com.vincenthuto.hemomancy.common.block.unstained.crafting.PallidRetortBlock;
import com.vincenthuto.hemomancy.common.block.unstained.crafting.StillwaterCondenserBlock;
import com.vincenthuto.hemomancy.common.block.unstained.crafting.VerdigrisLatticeBlock;
import com.vincenthuto.hemomancy.common.block.unstained.decoration.LetheanPoppyWreathBlock;
import com.vincenthuto.hemomancy.common.block.unstained.functional.AltarOfCleansingBlock;
import com.vincenthuto.hemomancy.common.block.unstained.functional.PaleSilverBellsBlock;
import com.vincenthuto.hemomancy.common.block.unstained.plant.GhostPipeBlock;
import com.vincenthuto.hemomancy.common.block.unstained.plant.LetheanPoppyBlock;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.crafting.MycelialCrucibleBlockItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
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
	 * The placed form of the Sanguine Conduit. The corresponding item (registered
	 * in {@link ItemInit}) gates placement behind Initiatory Degree ≥ 5 and handles
	 * item consumption, so no separate BlockItem is auto-registered here.
	 */
	public static final DeferredHolder<Block, Block> sanguine_conduit = BASEBLOCKS.register("sanguine_conduit",
				() -> new SanguineConduitBlock(BlockBehaviour.Properties.of()
						.mapColor(MapColor.COLOR_RED)
						.requiresCorrectToolForDrops()
						.strength(3.0f, 6.0f)
						.sound(SoundType.BONE_BLOCK)
						.lightLevel(state -> 4)
						.noOcclusion()));

	public static final DeferredHolder<Block, Block> hematic_suture_node = MODELEDBLOCKS.register("hematic_suture_node",
			() -> new HematicSutureNodeBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.requiresCorrectToolForDrops()
					.strength(2.0f, 6.0f)
					.sound(SoundType.BONE_BLOCK)
					.lightLevel(state -> 3)
					.noOcclusion()));

	public static final DeferredHolder<Block, Block> blood_echo_inscription = MODELEDBLOCKS.register(
			"blood_echo_inscription",
			() -> new DiscoveryInscriptionBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F), true));

	public static final DeferredHolder<Block, Block> rite_fragment_inscription = MODELEDBLOCKS.register(
			"rite_fragment_inscription",
			() -> new DiscoveryInscriptionBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.SNOW)
					.requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> blood_scorched_scab = BASEBLOCKS.register("blood_scorched_scab",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
					.strength(3.5F, 9F).sound(SoundType.NETHERRACK).requiresCorrectToolForDrops()));
	public static final DeferredHolder<Block, Block> pointed_blood_scorched_scab = BASEBLOCKS.register(
			"pointed_blood_scorched_scab", () -> new BloodScabPointedDripstoneBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK).forceSolidOn().instrument(NoteBlockInstrument.BASEDRUM)
					.noOcclusion().sound(SoundType.POINTED_DRIPSTONE).strength(1.5F, 3F).dynamicShape()
					.offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)
					.isRedstoneConductor((state,level,pos) -> false)));
	public static final DeferredHolder<Block, com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock> escharian_scyphus = MODELEDBLOCKS.register(
			"escharian_scyphus", () -> new com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock(
					BlockBehaviour.Properties.of().noCollission().noOcclusion().instabreak().sound(SoundType.FUNGUS)));

	public static final DeferredHolder<Block, EscharianOvergrowthBlock> escharian_overgrowth = MODELEDBLOCKS.register(
			"escharian_overgrowth", () -> new EscharianOvergrowthBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK).forceSolidOn().noOcclusion().dynamicShape().noLootTable()
					.strength(3.5F, 9F).sound(SoundType.NETHERRACK)));
	public static final DeferredHolder<Block, EscharianOvergrowthBlock> escharian_overgrowth_rim = MODELEDBLOCKS.register(
			"escharian_overgrowth_rim", () -> new EscharianOvergrowthBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE).forceSolidOn().noOcclusion().dynamicShape().noLootTable()
					.strength(3.5F, 9F).sound(SoundType.NETHERRACK)));
	/** The Spawn Mother's brood tumor: rooted only by her mortar in an owned fight, so it has no item and no drop. */
	public static final DeferredHolder<Block, Block> brood_tumor = MODELEDBLOCKS.register("brood_tumor",
			() -> new BroodTumorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.3F)
					.noLootTable().noOcclusion().lightLevel(state -> 6).sound(SoundType.HONEY_BLOCK)
					.pushReaction(PushReaction.DESTROY)));
	public static final DeferredHolder<Block, LiquidBlock> PHLEGETHONTIC_ICHOR_BLOCK = LIQUIDBLOCKS.register(
			"phlegethontic_ichor_block", () -> new com.vincenthuto.hemomancy.common.block.harbinger.PhlegethonticIchorBlock(
					FluidInit.PHLEGETHONTIC_ICHOR.get(), BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
					.noCollission().strength(100F).noLootTable().liquid().lightLevel(state -> 7).pushReaction(PushReaction.DESTROY)));

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

	public static final DeferredHolder<Block, LiquidBlock> RENDERING_BILE_BLOCK = LIQUIDBLOCKS.register(
			"rendering_bile_block",
			() -> new LiquidBlock(
					FluidInit.RENDERING_BILE.get(),
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.TERRACOTTA_YELLOW)
							.replaceable()
							.noCollission()
							.strength(100f)
							.pushReaction(PushReaction.DESTROY)
							.noLootTable()
							.liquid()
							.lightLevel(state -> 2)
							.sound(SoundType.EMPTY)));

	public static final DeferredHolder<Block, LiquidBlock> WHITE_HUMOR_BLOCK = LIQUIDBLOCKS.register(
			"white_humor_block",
			() -> new LiquidBlock(
					FluidInit.WHITE_HUMOR.get(),
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.SNOW)
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
	public static final DeferredHolder<Block, Block> virid_salis_trail = SPECIALBLOCKS.register("virid_salis_trail",
			() -> new ViridSalisTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));

	public static final DeferredHolder<Block, Block> active_smouldering_ash_trail = SPECIALBLOCKS.register(
			"active_smouldering_ash_trail",
			() -> new ActiveSmoulderingAshTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));
	public static final DeferredHolder<Block, Block> active_befouling_ash_trail = SPECIALBLOCKS.register(
			"active_befouling_ash_trail",
			() -> new ActiveBefoulingAshTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));

	public static final DeferredHolder<Block, Block> placed_blood_stained_stone = SPECIALBLOCKS.register(
			"placed_blood_stained_stone",
			() -> new BloodStainedStoneMarkerBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.noCollission()
					.instabreak()
					.noOcclusion()
					.sound(SoundType.STONE)
					.pushReaction(PushReaction.DESTROY)));

	public static final DeferredHolder<Block, Block> engram_block = SPECIALBLOCKS.register("engram_block", EngramBlock::new);

	public static final DeferredHolder<Block, Block> filler_block = SPECIALBLOCKS.register("filler_block",
			() -> new FillerBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion().noLootTable()));

	public static final DeferredHolder<Block, Block> abocipher_emitter = SPECIALBLOCKS.register("abocipher_emitter",
			() -> new AbocipherEmitterBlock(BlockBehaviour.Properties.of()
					.replaceable()
					.noCollission()
					.noOcclusion()
					.noLootTable()
					.pushReaction(PushReaction.DESTROY)
					.sound(SoundType.EMPTY)));

	public static final DeferredHolder<Block, Block> qliphoth_bloom = SPECIALBLOCKS.register("qliphoth_bloom",
			() -> new QliphothBloomBlock(
					BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion().noLootTable()
							.lightLevel(state -> 7)));

	// Blocks
	public static final DeferredHolder<Block, Block> frozen_cruor = BASEBLOCKS.register("frozen_cruor",
			() -> new IceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PACKED_ICE)
					.mapColor(MapColor.COLOR_RED)));

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

	public static final DeferredHolder<Block, Block> circus_curtain = SPECIALBLOCKS.register("circus_curtain",
			() -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_WOOL).noOcclusion()));

	public static final DeferredHolder<Block, Block> venous_stone = BASEBLOCKS.register("venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> venous_stone_slab = SLABBLOCKS.register("venous_stone_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final DeferredHolder<Block, Block> venous_stone_stairs = BASEBLOCKS.register(("venous_stone_stairs"),
			() -> new StairBlock(Blocks.STONE.defaultBlockState(),
					BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final DeferredHolder<Block, Block> venous_stone_wall = BASEBLOCKS.register("venous_stone_wall",
			() -> new HemoWallBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> bog_iron = BASEBLOCKS.register("bog_iron",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN)
					.requiresCorrectToolForDrops().strength(0.7F, 3.0F).sound(SoundType.GRAVEL)));

	public static final DeferredHolder<Block, Block> neural_tissue = BASEBLOCKS.register("neural_tissue",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> axonal_slate = BASEBLOCKS.register("axonal_slate",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> myelin_sheath = BASEBLOCKS.register("myelin_sheath",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.0F, 4.0F)));

	public static final DeferredHolder<Block, Block> ganglion_matter = BASEBLOCKS.register("ganglion_matter",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.lightLevel(state -> 6)));

	public static final DeferredHolder<Block, Block> cortical_folds = BASEBLOCKS.register("cortical_folds",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(2.0F, 6.0F)));

	public static final DeferredHolder<Block, Block> nerve_fiber = BASEBLOCKS.register("nerve_fiber",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(0.4F, 1.0F).noOcclusion()));

	public static final DeferredHolder<Block, Block> nerve_bundle = BASEBLOCKS.register("nerve_bundle",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(0.8F, 2.0F).noOcclusion()));

	public static final DeferredHolder<Block, Block> synaptic_node = BASEBLOCKS.register("synaptic_node",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.2F, 4.0F).lightLevel(state -> 10)));

	public static final DeferredHolder<Block, Block> dormant_ganglion = BASEBLOCKS.register("dormant_ganglion",
			DormantGanglionBlock::new);

	public static final DeferredHolder<Block, Block> dura_membrane = BASEBLOCKS.register("dura_membrane",
			() -> new Block(BlockBehaviour.Properties.of().strength(0.6F, 1.0F).noOcclusion()));

	public static final DeferredHolder<Block, Block> dendrite_growth = CROSSBLOCKS.register("dendrite_growth",
			() -> new Block(BlockBehaviour.Properties.of().instabreak().noCollission().noOcclusion()));

	public static final DeferredHolder<Block, Block> gilded_venous_stone = BASEBLOCKS.register("gilded_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone = BASEBLOCKS.register("polished_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone_slab = SLABBLOCKS.register(
			"polished_venous_stone_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone_stairs = STAIRBLOCKS.register(
			("polished_venous_stone_stairs"), () -> new StairBlock(Blocks.STONE.defaultBlockState(),
					BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone_wall = BASEBLOCKS.register(
			"polished_venous_stone_wall",
			() -> new HemoWallBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
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
			("polished_venous_stone_brick_stairs"), () -> new StairBlock(Blocks.STONE.defaultBlockState(),
					BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final DeferredHolder<Block, Block> polished_venous_stone_brick_wall = BASEBLOCKS.register(
			"polished_venous_stone_brick_wall",
			() -> new HemoWallBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final DeferredHolder<Block, Block> cracked_polished_venous_stone_bricks = BASEBLOCKS.register(
			"cracked_polished_venous_stone_bricks",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> bone_bricks = BASEBLOCKS.register("bone_bricks",
			() -> new Block(boneBrickProperties()));
	public static final DeferredHolder<Block, Block> cracked_bone_bricks = BASEBLOCKS.register("cracked_bone_bricks",
			() -> new Block(boneBrickProperties()));
	public static final DeferredHolder<Block, Block> chiseled_bone_bricks = BASEBLOCKS.register("chiseled_bone_bricks",
			() -> new Block(boneBrickProperties()));
	public static final DeferredHolder<Block, Block> bone_brick_stairs = STAIRBLOCKS.register("bone_brick_stairs",
			() -> new StairBlock(Blocks.BONE_BLOCK.defaultBlockState(), boneBrickProperties()));
	public static final DeferredHolder<Block, Block> bone_brick_slab = SLABBLOCKS.register("bone_brick_slab",
			() -> new SlabBlock(boneBrickProperties()));
	public static final DeferredHolder<Block, Block> bone_brick_wall = BASEBLOCKS.register("bone_brick_wall",
			() -> new HemoWallBlock(boneBrickProperties()));

	private static BlockBehaviour.Properties boneBrickProperties() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.SAND).requiresCorrectToolForDrops()
				.strength(2.0F, 6.0F).sound(SoundType.BONE_BLOCK);
	}

	public static final DeferredHolder<Block, Block> infested_venous_stone = BASEBLOCKS.register("infested_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final DeferredHolder<Block, Block> conscious_mass = BASEBLOCKS.register("conscious_mass",
			() -> new Block(BlockBehaviour.Properties.of().sound(SoundType.WART_BLOCK).strength(0.2F, 1.0F)));

	public static final DeferredHolder<Block, Block> ossuary_clock = MODELEDBLOCKS.register("ossuary_clock",
			() -> new OssuaryClockBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BROWN)
					.requiresCorrectToolForDrops()
					.noOcclusion()
					.strength(1.5F, 6.0F)
					.sound(SoundType.BONE_BLOCK)));

	public static final DeferredHolder<Block, Block> harbinger_escritoire = MODELEDBLOCKS.register("harbinger_escritoire",
			() -> new HarbingerEscritoireBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BROWN)
					.requiresCorrectToolForDrops()
					.noOcclusion()
					.strength(2.5F, 6.0F)
					.sound(SoundType.WOOD)));

	public static final DeferredHolder<Block, Block> humoral_barometer = MODELEDBLOCKS.register("humoral_barometer",
			() -> new HumoralBarometerBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F)
					.sound(SoundType.METAL)));

	// Fungal dimension ores
	public static final DeferredHolder<Block, Block> hematic_iron_ore = BASEBLOCKS.register("hematic_iron_ore",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.0F, 3.0F)
					.sound(SoundType.NETHER_ORE)));

	// New fungal dimension blocks
	public static final DeferredHolder<Block, Block> calcified_hyphae = BASEBLOCKS.register("calcified_hyphae",
			() -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.BONE_BLOCK)));

	public static final DeferredHolder<Block, Block> hematic_algal_crust = SPECIALBLOCKS.register("hematic_algal_crust",
			() -> new HematicAlgalCrustBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(.2F).noCollission().randomTicks().sound(SoundType.WET_GRASS)));
	public static final DeferredHolder<Block, Block> tidepool_anemone = SPECIALBLOCKS.register("tidepool_anemone",
			() -> new PelagicColonyBlock(PelagicColonyBlock.Kind.ANEMONE, BlockBehaviour.Properties.of().strength(.25F).noCollission().noOcclusion().sound(SoundType.WET_GRASS)));
	public static final DeferredHolder<Block, Block> bone_worm_colony = SPECIALBLOCKS.register("bone_worm_colony",
			() -> new BoneWormColonyBlock(BlockBehaviour.Properties.of().strength(.2F).noCollission().noOcclusion().sound(SoundType.WET_GRASS)));
	public static final DeferredHolder<Block, Block> giant_tube_worm_colony = SPECIALBLOCKS.register("giant_tube_worm_colony",
			() -> new GiantTubeWormColonyBlock(BlockBehaviour.Properties.of().strength(.4F).noCollission().noOcclusion().sound(SoundType.BONE_BLOCK)));

	public static final DeferredHolder<Block, Block> erythrocoral_block = BASEBLOCKS.register("erythrocoral_block",
			() -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.strength(1.2F, 3.0F)
					.sound(SoundType.WART_BLOCK)
					.lightLevel(state -> 2)));

	public static final DeferredHolder<Block, Block> calcified_erythrocoral = BASEBLOCKS.register("calcified_erythrocoral",
			() -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.requiresCorrectToolForDrops()
					.strength(1.8F, 4.0F)
					.sound(SoundType.BONE_BLOCK)));

	public static final DeferredHolder<Block, Block> erythrocoral_fan = SPECIALBLOCKS.register("erythrocoral_fan",
			() -> new ErythrocoralGrowthBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.noCollission()
					.instabreak()
					.sound(SoundType.WART_BLOCK)
					.pushReaction(PushReaction.DESTROY),
					Block.box(1.0D, 0.0D, 1.0D, 15.0D, 10.0D, 15.0D)));

	public static final DeferredHolder<Block, Block> erythrocoral_tendril = SPECIALBLOCKS.register("erythrocoral_tendril",
			() -> new ErythrocoralGrowthBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_MAGENTA)
					.noCollission()
					.instabreak()
					.sound(SoundType.WART_BLOCK)
					.pushReaction(PushReaction.DESTROY)
					.lightLevel(state -> 3),
					Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D)));

	public static final DeferredHolder<Block, Block> hemorrhagic_crust = BASEBLOCKS.register("hemorrhagic_crust",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 4.0F).sound(SoundType.NETHERRACK)));

	public static final DeferredHolder<Block, Block> sporite_crystal = BASEBLOCKS.register("sporite_crystal",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 3.0F).sound(SoundType.AMETHYST)
					.lightLevel((state) -> 10).noOcclusion()));

	public static final DeferredHolder<Block, Block> chitinite_block = BASEBLOCKS.register("chitinite_block",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 3.0F).sound(SoundType.STONE)
					.lightLevel((state) -> 10).noOcclusion()));

	public static final DeferredHolder<Block, Block> sclerite_block = BASEBLOCKS.register("sclerite_block",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 3.0F).sound(SoundType.COPPER)
					.lightLevel((state) -> 10).noOcclusion()));

	public static final DeferredHolder<Block, Block> organic_prism = BASEBLOCKS.register("organic_prism",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 3.0F).sound(SoundType.AMETHYST)
					.lightLevel((state) -> 10).noOcclusion()));

	public static final DeferredHolder<Block, Block> mnemonic_ambergris_block = BASEBLOCKS.register("mnemonic_ambergris_block",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 3.0F).sound(SoundType.WART_BLOCK)
					.lightLevel((state) -> 10).noOcclusion()));





	public static final DeferredHolder<Block, Block> puppeteers_wool = BASEBLOCKS.register("puppeteers_wool",
			() -> new Block(BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.WOOL)));

	public static final DeferredHolder<Block, Block> hematic_iron_block = BASEBLOCKS.register("hematic_iron_block",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
					.sound(SoundType.METAL)));
	public static final DeferredHolder<Block, Block> cardinal_focus = BASEBLOCKS.register("cardinal_focus",
			() -> new CardinalFocusBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 12.0F)
					.sound(SoundType.METAL).lightLevel(state -> 5)));
	public static final DeferredHolder<Block, Block> hematic_iron_pillar = COLUMNBLOCKS.register("hematic_iron_pillar",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(5.0F, 6.0F).sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> chiseled_hematic_iron_block = BASEBLOCKS
			.register("chiseled_hematic_iron_block", () -> new Block(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> hematic_iron_plating = BASEBLOCKS.register("hematic_iron_plating",
			() -> new Block(hematicIronPlatingProperties()));
	public static final DeferredHolder<Block, Block> hematic_iron_plating_stairs = STAIRBLOCKS.register(
			"hematic_iron_plating_stairs",
			() -> new StairBlock(Blocks.IRON_BLOCK.defaultBlockState(), hematicIronPlatingProperties()));
	public static final DeferredHolder<Block, Block> hematic_iron_plating_slab = SLABBLOCKS.register(
			"hematic_iron_plating_slab", () -> new SlabBlock(hematicIronPlatingProperties()));
	public static final DeferredHolder<Block, Block> hematic_iron_plating_wall = BASEBLOCKS.register(
			"hematic_iron_plating_wall", () -> new HemoWallBlock(hematicIronPlatingProperties()));

	private static BlockBehaviour.Properties hematicIronPlatingProperties() {
		return BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
				.sound(SoundType.METAL);
	}

	/** Seraphae's grown stone: quartz bricks and calcite where her healing reached. Textures and models from
	 * tools/model_export/seraphae_growth.py. */
	public static final DeferredHolder<Block, Block> healed_quartz_bricks = BASEBLOCKS.register("healed_quartz_bricks",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ)
					.instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
					.sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> healed_calcite = BASEBLOCKS.register("healed_calcite",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE)
					.instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.75F)
					.sound(SoundType.CALCITE)));
	/** Healed quartz whose growth glows from inside; the growth layer of its model is drawn full-bright. */
	public static final DeferredHolder<Block, Block> radiant_healed_quartz = BASEBLOCKS.register("radiant_healed_quartz",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ)
					.instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
					.sound(SoundType.STONE).lightLevel(state -> 12)));

	/** Putriciel's burned and rotten materials: ash floors, charred wood and soft wet mud brick. Textures and
	 * models from tools/model_export/putriciel_materials.py. */
	public static final DeferredHolder<Block, Block> packed_ash = BASEBLOCKS.register("packed_ash",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
					.instrument(NoteBlockInstrument.BASEDRUM).strength(0.6F).sound(SoundType.SOUL_SOIL)));
	public static final DeferredHolder<Block, Block> charred_planks = BASEBLOCKS.register("charred_planks",
			() -> new Block(charredWoodProperties()));
	public static final DeferredHolder<Block, Block> charred_planks_slab = SLABBLOCKS.register("charred_planks_slab",
			() -> new SlabBlock(charredWoodProperties()));
	public static final DeferredHolder<Block, Block> charred_planks_stairs = STAIRBLOCKS.register(
			"charred_planks_stairs",
			() -> new StairBlock(Blocks.DARK_OAK_PLANKS.defaultBlockState(), charredWoodProperties()));
	public static final DeferredHolder<Block, Block> charred_log = COLUMNBLOCKS.register("charred_log",
			() -> new RotatedPillarBlock(charredWoodProperties()));
	/** A bookshelf burned half to ash. Drops a little charcoal, itself with Silk Touch; no enchanting power. */
	public static final DeferredHolder<Block, Block> charred_bookshelf = BASEBLOCKS.register("charred_bookshelf",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
					.instrument(NoteBlockInstrument.BASS).strength(1.5F).sound(SoundType.WOOD)));
	/** Mud brick gone soft and wet: sinks underfoot (14 px collision, as vanilla mud) and slows like soul sand. */
	public static final DeferredHolder<Block, Block> sodden_mud_bricks = BASEBLOCKS.register("sodden_mud_bricks",
			() -> new MudBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN)
					.strength(1.0F, 3.0F).speedFactor(0.4F).sound(SoundType.MUD).isValidSpawn(Blocks::always)
					.isRedstoneConductor((state, level, pos) -> true).isViewBlocking((state, level, pos) -> true)
					.isSuffocating((state, level, pos) -> true)));

	private static BlockBehaviour.Properties charredWoodProperties() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F).sound(SoundType.WOOD);
	}

	/** Velorum's monastery materials: rimed deepslate trim, the rime lantern and dark lacquered wood. Textures and
	 * models from tools/model_export/velorum_materials.py. */
	public static final DeferredHolder<Block, Block> rimed_deepslate_bricks = BASEBLOCKS.register(
			"rimed_deepslate_bricks", () -> new Block(rimedDeepslateProperties()));
	public static final DeferredHolder<Block, Block> rimed_deepslate_brick_wall = BASEBLOCKS.register(
			"rimed_deepslate_brick_wall", () -> new HemoWallBlock(rimedDeepslateProperties().forceSolidOn()));
	public static final DeferredHolder<Block, Block> rimed_deepslate_brick_slab = SLABBLOCKS.register(
			"rimed_deepslate_brick_slab", () -> new SlabBlock(rimedDeepslateProperties()));
	public static final DeferredHolder<Block, Block> rimed_deepslate_brick_stairs = STAIRBLOCKS.register(
			"rimed_deepslate_brick_stairs",
			() -> new StairBlock(Blocks.DEEPSLATE_BRICKS.defaultBlockState(), rimedDeepslateProperties()));
	/** A pale blue-white light seen through a clear-ice shell; it never melts. */
	public static final DeferredHolder<Block, Block> rime_lantern = BASEBLOCKS.register("rime_lantern",
			() -> new TransparentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.ICE)
					.instrument(NoteBlockInstrument.HAT).strength(0.5F).sound(SoundType.GLASS).lightLevel(state -> 11)
					.noOcclusion().isValidSpawn(Blocks::never).isRedstoneConductor((state, level, pos) -> false)
					.isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
	public static final DeferredHolder<Block, Block> dark_lacquer_planks = BASEBLOCKS.register("dark_lacquer_planks",
			() -> new Block(darkLacquerProperties()));
	public static final DeferredHolder<Block, Block> dark_lacquer_planks_slab = SLABBLOCKS.register(
			"dark_lacquer_planks_slab", () -> new SlabBlock(darkLacquerProperties()));
	public static final DeferredHolder<Block, Block> dark_lacquer_planks_stairs = STAIRBLOCKS.register(
			"dark_lacquer_planks_stairs",
			() -> new StairBlock(Blocks.DARK_OAK_PLANKS.defaultBlockState(), darkLacquerProperties()));

	private static BlockBehaviour.Properties rimedDeepslateProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICKS);
	}

	private static BlockBehaviour.Properties darkLacquerProperties() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F).sound(SoundType.WOOD);
	}

	public static final DeferredHolder<Block, Block> hematic_iron_bars = BASEBLOCKS.register("hematic_iron_bars",
			() -> new HematicIronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)));

	public static final DeferredHolder<Block, Block> hematic_iron_door = BASEBLOCKS.register("hematic_iron_door",
			() -> new WaterloggedDoorBlock(BlockSetType.IRON, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR)));

	public static final DeferredHolder<Block, Block> hematic_iron_trapdoor = BASEBLOCKS.register("hematic_iron_trapdoor",
			() -> new TrapDoorBlock(BlockSetType.IRON, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_TRAPDOOR)));

	public static final DeferredHolder<Block, Block> hemolytic_plating_block = BASEBLOCKS.register("hemolytic_plating_block",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
					.sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> crimson_flames = SPECIALBLOCKS.register("crimson_flames",
			() -> new CrimsonFlameBlock());


	// Plants

	public static final DeferredHolder<Block, Block> blood_wood_log = COLUMNBLOCKS.register("blood_wood_log",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> blood_wood_planks = BASEBLOCKS.register("blood_wood_planks",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final DeferredHolder<Block, Block> blood_wood_leaves = BASEBLOCKS.register("blood_wood_leaves",
			() -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LEAVES)
					.mapColor(MapColor.COLOR_RED)));

	public static final DeferredHolder<Block, Block> hyphae = CROSSBLOCKS.register("hyphae",
			() -> new HyphaeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission()
					.instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ).ignitedByLava()
					.pushReaction(PushReaction.DESTROY)));

	public static final DeferredHolder<Block, Block> bleeding_heart = CROSSBLOCKS.register("bleeding_heart",
			() -> new BleedingHeartBlock(MobEffects.ABSORPTION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> potted_bleeding_heart = POTTEDBLOCKS.register("potted_bleeding_heart",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.bleeding_heart,
					BlockBehaviour.Properties.of()));

	public static final DeferredHolder<Block, Block> infected_fungus = CROSSBLOCKS.register("infected_fungus",
			() -> new InfectedFungusBlock(MobEffects.CONFUSION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> potted_infected_fungus = POTTEDBLOCKS.register("potted_infected_fungus",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.infected_fungus,
					BlockBehaviour.Properties.of().noCollission()));

	public static final DeferredHolder<Block, Block> puffball_fungus = MODELEDBLOCKS.register("puffball_fungus",
			() -> new PuffballFungusBlock(MobEffects.SATURATION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	/** A Gardens cap grown into a bridge by an Apotheos; withers on its own, so it has no item and no drop. */
	public static final DeferredHolder<Block, Block> cap_bridge = MODELEDBLOCKS.register("cap_bridge",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.plant.CapBridgeBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED).strength(0.5f).noLootTable().noOcclusion().sound(SoundType.FUNGUS)
					.pushReaction(PushReaction.DESTROY)));
	/** Hanging lamellae: open for an Apotheos, they bite and snap shut on anyone else. */
	public static final DeferredHolder<Block, Block> fungal_gill = MODELEDBLOCKS.register("fungal_gill",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalGillBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE).strength(0.4f).noOcclusion().sound(SoundType.WART_BLOCK)
					.pushReaction(PushReaction.DESTROY).isViewBlocking((state, level, pos) -> false)
					.isSuffocating((state, level, pos) -> false)));
	/**
	 * An Apotheos's heart-node: planted in the Gardens, it grows their private sanctum and holds their spawn. Forced
	 * solid, so a fluid can't flow into its small shape and wash it away (which would drop it and wither the sanctum).
	 */
	public static final DeferredHolder<Block, Block> heart_node = MODELEDBLOCKS.register("heart_node",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.functional.HeartNodeBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED).strength(2.0f, 3600000f).noOcclusion().forceSolidOn().sound(SoundType.WART_BLOCK)
					.lightLevel(state -> 5).pushReaction(PushReaction.BLOCK)));
	/**
	 * A heart-node's Mycelial Synapse: a Blood Structure that extends its owner's envelope. Raised only from its
	 * formation; its hidden item is the recipe's face and cannot place it. Owner-instant, drops its inputs. Forced
	 * solid like the heart-node, so no fluid washes it off its face.
	 */
	public static final DeferredHolder<Block, Block> mycelial_synapse = MODELEDBLOCKS.register("mycelial_synapse",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.functional.MycelialSynapseBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED).instabreak().noOcclusion().forceSolidOn().sound(SoundType.WART_BLOCK)
					.lightLevel(state -> state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.functional.MycelialSynapseBlock.WITHERED) ? 0 : 4)
					.pushReaction(PushReaction.BLOCK)));
	/** A dead Apotheos's gear, grown over at the death point; only the owner opens it. No item. */
	public static final DeferredHolder<Block, Block> fruiting_body_cache = MODELEDBLOCKS.register("fruiting_body_cache",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.functional.FruitingBodyCacheBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE).strength(-1.0f, 3600000f).noOcclusion().noLootTable()
					.sound(SoundType.WART_BLOCK).lightLevel(state -> 3).pushReaction(PushReaction.BLOCK)));
	/**
	 * A Saint mausoleum's ward anchor, buried in the threshold hall's wall: it holds the ward's region for the client
	 * and the threshold the eighth degree alone may cross. Invisible, no hitbox, unbreakable; no item.
	 */
	public static final DeferredHolder<Block, Block> ward_anchor = MODELEDBLOCKS.register("ward_anchor",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.WardAnchorBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.NONE).strength(-1.0f, 3600000f).noLootTable().noOcclusion().noCollission()
					.sound(SoundType.DEEPSLATE_BRICKS).pushReaction(PushReaction.BLOCK)));
	/**
	 * The ring door before a Saint's chamber, the controller of a five-by-five multiblock: a Silentium opens it, an
	 * Apotheos shatters it. Unbreakable; no item.
	 */
	public static final DeferredHolder<Block, Block> ring_door = MODELEDBLOCKS.register("ring_door",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(-1.0f, 3600000f)
					.noLootTable().noOcclusion().sound(SoundType.DEEPSLATE_BRICKS).lightLevel(state -> 3).pushReaction(PushReaction.BLOCK)));
	/** A Chapel's focus in a Saint mausoleum: its trial lights a node on the ring door. Unbreakable. */
	public static final DeferredHolder<Block, Block> chapel_focus = MODELEDBLOCKS.register("chapel_focus",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelFocusBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(-1.0f, 3600000f)
					.noLootTable().noOcclusion().sound(SoundType.DEEPSLATE_BRICKS).lightLevel(state -> 6)
					.pushReaction(PushReaction.BLOCK)));
	// Mausoleum set pieces (Track 6). Trial pieces are unbreakable like the focus; the dressing breaks and drops
	// nothing. Each has an item for building and testing; the Witness Devotee stays structure-only.
	/** One of Velorum's frozen congregation, two tall, in one of four poses. */
	public static final DeferredHolder<Block, Block> frozen_devotee = MODELEDBLOCKS.register("frozen_devotee",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.FrozenDevoteeBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.ICE).strength(2.0f, 6.0f).noLootTable().noOcclusion().sound(SoundType.GLASS)
					.pushReaction(PushReaction.BLOCK)));
	/** A Beholding witness: its eye lights when the memory touches it. */
	public static final DeferredHolder<Block, Block> witness_stone = MODELEDBLOCKS.register("witness_stone",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.WitnessStoneBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY).strength(-1.0f, 3600000f).noLootTable().noOcclusion()
					.sound(SoundType.DEEPSLATE_BRICKS).pushReaction(PushReaction.BLOCK)
					.lightLevel(state -> state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.WitnessStoneBlock.LIT) ? 7 : 0)));
	/** Velorum's Beholding witness: one of the Procession who turns its head and opens its eyes when lit. */
	public static final DeferredHolder<Block, Block> witness_devotee = MODELEDBLOCKS.register("witness_devotee",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.WitnessDevoteeBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.ICE).strength(-1.0f, 3600000f).noLootTable().noOcclusion()
					.sound(SoundType.GLASS).pushReaction(PushReaction.BLOCK)
					.lightLevel(state -> state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.WitnessDevoteeBlock.LIT) ? 7 : 0)));
	/** A Litany's standing stone, two tall, the Litany carved in Abocipher on its face. */
	public static final DeferredHolder<Block, Block> litany_stone = MODELEDBLOCKS.register("litany_stone",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.LitanyStoneBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY).strength(-1.0f, 3600000f).noLootTable().noOcclusion()
					.sound(SoundType.DEEPSLATE_BRICKS).pushReaction(PushReaction.BLOCK)));
	/** A Litany's offering stand, holding one of the candidates. */
	public static final DeferredHolder<Block, Block> offering_stand = MODELEDBLOCKS.register("offering_stand",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.OfferingStandBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE).strength(-1.0f, 3600000f).noLootTable().noOcclusion()
					.sound(SoundType.BONE_BLOCK).pushReaction(PushReaction.BLOCK)));
	/** A Repose pylon, three tall; its head charges before each pulse. */
	public static final DeferredHolder<Block, Block> pulse_pylon = MODELEDBLOCKS.register("pulse_pylon",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY).strength(-1.0f, 3600000f).noLootTable().noOcclusion()
					.sound(SoundType.DEEPSLATE_BRICKS).pushReaction(PushReaction.BLOCK)
					.lightLevel(state -> 2 + state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock.CHARGE) * 3)));
	/** A Downfall seal in the floor, which a wave of the fall rises from. */
	public static final DeferredHolder<Block, Block> spawn_mark = MODELEDBLOCKS.register("spawn_mark",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SpawnMarkBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY).strength(-1.0f, 3600000f).noLootTable().noOcclusion().noCollission()
					.sound(SoundType.DEEPSLATE_BRICKS).pushReaction(PushReaction.BLOCK)
					.lightLevel(state -> state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SpawnMarkBlock.DUE) ? 4 : 0)));
	/** A containment anchor in its dormant, built form: Seraphae's chain pylon as architecture. Two tall. */
	public static final DeferredHolder<Block, Block> containment_anchor_block = MODELEDBLOCKS.register("containment_anchor_block",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.ContainmentAnchorBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ).strength(3.0f, 9.0f).noLootTable().noOcclusion().sound(SoundType.STONE)
					.pushReaction(PushReaction.BLOCK)));
	/** Bone spurs grown out of a block face where Seraphae's healing reached; placed like an amethyst cluster. */
	public static final DeferredHolder<Block, Block> bone_outgrowth = MODELEDBLOCKS.register("bone_outgrowth",
			() -> new BoneOutgrowthBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
					.requiresCorrectToolForDrops().strength(1.0F).noOcclusion().sound(SoundType.BONE_BLOCK)
					.pushReaction(PushReaction.DESTROY)));
	/** Bracket fungus on a wall in Putriciel's overgrown bands; its gills glow faint green, brighter in bloom. */
	public static final DeferredHolder<Block, Block> spore_shelf = MODELEDBLOCKS.register("spore_shelf",
			() -> new SporeShelfBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW)
					.strength(0.5F).noOcclusion().noCollission().sound(SoundType.FUNGUS)
					.pushReaction(PushReaction.DESTROY).lightLevel(SporeShelfBlock::lightLevel)));
	/** Icicles hanging from Velorum's vaults: a short cluster, or the base and tip of a two-cell icicle. */
	public static final DeferredHolder<Block, Block> hanging_icicle = MODELEDBLOCKS.register("hanging_icicle",
			() -> new HangingIcicleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(0.5F)
					.noOcclusion().noCollission().sound(SoundType.GLASS).pushReaction(PushReaction.DESTROY)));
	/** Runoff frozen down a wall face in Velorum's monastery, in vertical runs down the pilasters. */
	public static final DeferredHolder<Block, Block> frozen_drip = MODELEDBLOCKS.register("frozen_drip",
			() -> new FrozenDripBlock(BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(0.5F)
					.noOcclusion().noCollission().sound(SoundType.GLASS).pushReaction(PushReaction.DESTROY)));
	/** Putriciel's bone pyre. */
	public static final DeferredHolder<Block, Block> rot_pyre = MODELEDBLOCKS.register("rot_pyre",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.RotPyreBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE).strength(1.0f, 3.0f).noLootTable().noOcclusion()
					.sound(SoundType.BONE_BLOCK).pushReaction(PushReaction.BLOCK)
					.lightLevel(state -> state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.decoration.RotPyreBlock.LIT) ? 10 : 0)));
	/** A loculus's sealed front: a bone-framed lacquer plate. */
	public static final DeferredHolder<Block, Block> sealed_niche = MODELEDBLOCKS.register("sealed_niche",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.NicheFrontBlock(
					com.vincenthuto.hemomancy.common.block.harbinger.decoration.NicheFrontBlock.SEALED,
					BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(2.0f, 6.0f).noLootTable()
							.noOcclusion().sound(SoundType.BONE_BLOCK).pushReaction(PushReaction.BLOCK)));
	/** A loculus's opened front: the frame and a shelf lip over the recess behind. */
	public static final DeferredHolder<Block, Block> opened_niche = MODELEDBLOCKS.register("opened_niche",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.NicheFrontBlock(
					com.vincenthuto.hemomancy.common.block.harbinger.decoration.NicheFrontBlock.OPENED,
					BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(2.0f, 6.0f).noLootTable()
							.noOcclusion().sound(SoundType.BONE_BLOCK).pushReaction(PushReaction.BLOCK)));
	/** The mausoleums' wall light: a hematic-iron sconce burning bone oil. */
	public static final DeferredHolder<Block, Block> reliquary_sconce = MODELEDBLOCKS.register("reliquary_sconce",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.ReliquarySconceBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED).strength(1.5f, 6.0f).noLootTable().noOcclusion().noCollission()
					.sound(SoundType.LANTERN).pushReaction(PushReaction.DESTROY)
					.lightLevel(state -> state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.decoration.ReliquarySconceBlock.FLAME).light())));
	/** Corpse wax on a wall: grown by the decoration pass in old dark rooms, or crafted from four adipocere for decor. */
	public static final DeferredHolder<Block, Block> adipocere_crust = MODELEDBLOCKS.register("adipocere_crust",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.plant.AdipocereCrustBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE).strength(0.3f).noOcclusion().noCollission()
					.sound(SoundType.HONEY_BLOCK).pushReaction(PushReaction.DESTROY)));
	// The Brined Court's Seat set pieces (Track 7): structure-only, no items, no drops. Light is biological.
	/** Gold lichen-crusted rock at the Lighthouse Garden: it glows at night. */
	public static final DeferredHolder<Block, Block> sunburst_crust = MODELEDBLOCKS.register("sunburst_crust",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.5f, 6.0f)
					.noLootTable().sound(SoundType.STONE).lightLevel(state -> 10)));
	/** The Lighthouse Garden's beacon: a living Sunburst bloom in a bone cage. */
	public static final DeferredHolder<Block, Block> sunburst_lamp = MODELEDBLOCKS.register("sunburst_lamp",
			() -> new CourtFixtureBlock(new double[][] {{2, 0, 2, 14, 16, 14}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_ORANGE).strength(2.0f, 6.0f).noLootTable().noOcclusion()
					.sound(SoundType.BONE_BLOCK).lightLevel(state -> 15)));
	/** Frond growth on the Garden's terraces and round its anchored arbor members. */
	public static final DeferredHolder<Block, Block> sunburst_frond = MODELEDBLOCKS.register("sunburst_frond",
			() -> new CourtFixtureBlock(new double[][] {{2, 0, 2, 14, 14, 14}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_ORANGE).strength(0.2f).noLootTable().noOcclusion().noCollission()
					.sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY).lightLevel(state -> 7)));
	/** A follower of Marimo gone all the way into his moss, on the undercroft's lowest walls. */
	public static final DeferredHolder<Block, Block> petrified_follower = MODELEDBLOCKS.register("petrified_follower",
			() -> new PetrifiedFollowerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN)
					.strength(3.0f, 9.0f).noLootTable().noOcclusion().sound(SoundType.MOSS)
					.pushReaction(PushReaction.BLOCK)));
	/** The polyp ring and flame-coloured tentacle crown that makes a vent chimney one of Hydra's heads. */
	public static final DeferredHolder<Block, Block> hydra_crown = MODELEDBLOCKS.register("hydra_crown",
			() -> new CourtFixtureBlock(new double[][] {{2, 0, 2, 14, 4, 14}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_ORANGE).strength(2.0f, 6.0f).noLootTable().noOcclusion()
					.sound(SoundType.SLIME_BLOCK).pushReaction(PushReaction.BLOCK).lightLevel(state -> 10)));
	/** The carved basin at a chimney's foot where the Kindling feed their offerings in. */
	public static final DeferredHolder<Block, Block> vent_offering_basin = MODELEDBLOCKS.register("vent_offering_basin",
			() -> new CourtFacingFixtureBlock(new double[][] {{1, 0, 1, 15, 9, 15}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK).strength(3.0f, 9.0f).noLootTable().noOcclusion()
					.sound(SoundType.GILDED_BLACKSTONE).pushReaction(PushReaction.BLOCK).lightLevel(state -> 6)));
	/** Hydra's body at the Vent Sanctum: the mass that members who did not keep a head became. */
	public static final DeferredHolder<Block, Block> kindled_tissue = MODELEDBLOCKS.register("kindled_tissue",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.0f, 6.0f)
					.noLootTable().sound(SoundType.SLIME_BLOCK).lightLevel(state -> 4)));
	// The Brined Court's presences (Track 10): Marimo is his moss. It never breaks outside Creative and never moves.
	/** Marimo's moss, filling the undercroft's lowest chamber and threading up through its walls. */
	public static final DeferredHolder<Block, Block> marimo_moss = MODELEDBLOCKS.register("marimo_moss",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.MarimoMossBlock(BlockBehaviour.Properties
					.of().mapColor(MapColor.COLOR_GREEN).strength(-1.0f, 3600000.0f).noLootTable().sound(SoundType.MOSS)
					.pushReaction(PushReaction.BLOCK).isValidSpawn((state, level, pos, type) -> false).lightLevel(state -> 4)));
	/** Marimo's Heart: the densest knot of his moss, which notices the living who come down to it. */
	public static final DeferredHolder<Block, Block> marimo_heart = MODELEDBLOCKS.register("marimo_heart",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.MarimoHeartBlock(BlockBehaviour.Properties
					.of().mapColor(MapColor.COLOR_YELLOW).strength(-1.0f, 3600000.0f).noLootTable().sound(SoundType.MOSS)
					.pushReaction(PushReaction.BLOCK).isValidSpawn((state, level, pos, type) -> false).lightLevel(state -> 9)));
	/** A bloom the Bloom Warden sows over the tier floor; fed by light from above, gone after the fight (Track 10). */
	public static final DeferredHolder<Block, Block> warden_bloom = MODELEDBLOCKS.register("warden_bloom",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.WardenBloomBlock(BlockBehaviour.Properties
					.of().mapColor(MapColor.COLOR_YELLOW).instabreak().noLootTable().noOcclusion().noCollission()
					.sound(SoundType.LILY_PAD).pushReaction(PushReaction.DESTROY).lightLevel(state -> 7)));
	// The Brined Court's outpost and sign set pieces (Track 8): placed by structures and features, items for building
	// and testing since the 2026-10-30 review, no drops.
	/** The Holding Stone's gripping growth, cemented to the rock at the Tide Refuge's entrance. */
	public static final DeferredHolder<Block, Block> holdfast_root_mass = MODELEDBLOCKS.register("holdfast_root_mass",
			() -> new CourtFacingFixtureBlock(new double[][] {{1, 0, 5, 15, 14, 16}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BROWN).strength(3.0f, 9.0f).noLootTable().noOcclusion()
					.sound(SoundType.ROOTS).pushReaction(PushReaction.BLOCK)));
	/** The Shared Wake's grown purse cups on the nursery bank, red egg cases inside. */
	public static final DeferredHolder<Block, Block> nest_cup = MODELEDBLOCKS.register("nest_cup",
			() -> new CourtFixtureBlock(new double[][] {{2, 0, 2, 14, 9, 14}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLUE).strength(0.8f).noLootTable().noOcclusion()
					.sound(SoundType.WART_BLOCK).pushReaction(PushReaction.DESTROY)));
	/** The lamp-lit pod that hangs from the Woven Current's lines. */
	public static final DeferredHolder<Block, Block> rope_pod = MODELEDBLOCKS.register("rope_pod",
			() -> new CourtFixtureBlock(new double[][] {{4, 1, 4, 12, 16, 12}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_PURPLE).strength(0.5f).noLootTable().noOcclusion().noCollission()
					.sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY).lightLevel(state -> 11)));
	/** The Woven Current's living line across a gully: climbable, like a chain on any axis. */
	public static final DeferredHolder<Block, Block> current_strand = MODELEDBLOCKS.register("current_strand",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.CurrentStrandBlock(
					BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.5f).noLootTable()
							.noOcclusion().noCollission().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
							.lightLevel(state -> 3)));
	/** Violet-blue grown tissue with a glowing vein net: the Line Camp's pod walls. */
	public static final DeferredHolder<Block, Block> current_tissue = MODELEDBLOCKS.register("current_tissue",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.0f, 4.0f)
					.noLootTable().sound(SoundType.WART_BLOCK).lightLevel(state -> 6)));
	/** The Ink Cap veil that screens the Veiled Fold's doorways. */
	public static final DeferredHolder<Block, Block> fold_veil = MODELEDBLOCKS.register("fold_veil",
			() -> new CourtFacingFixtureBlock(new double[][] {{0, 0, 5, 16, 16, 9}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK).strength(0.3f).noLootTable().noOcclusion().noCollission()
					.sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)));
	/** The Still Bell's grown diving-bell rind: hard frost blue-grey sclerotium, no metal. */
	public static final DeferredHolder<Block, Block> sclerotium_shell = MODELEDBLOCKS.register("sclerotium_shell",
			() -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(4.0f, 12.0f)
					.noLootTable().sound(SoundType.NETHER_WART)));
	/** A Still Bell elder sealed in its folded mantle, hung in the bell's upper ring. */
	public static final DeferredHolder<Block, Block> bell_sleeper = MODELEDBLOCKS.register("bell_sleeper",
			() -> new CourtFixtureBlock(new double[][] {{3, 0, 3, 13, 16, 13}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_LIGHT_BLUE).strength(3.0f, 9.0f).noLootTable().noOcclusion()
					.noCollission().sound(SoundType.WART_BLOCK).pushReaction(PushReaction.BLOCK).lightLevel(state -> 3)));
	/** Last Marrow's red feeding plumes, grown out of the whale fall's bones. */
	public static final DeferredHolder<Block, Block> marrow_plume = MODELEDBLOCKS.register("marrow_plume",
			() -> new CourtFixtureBlock(new double[][] {{3, 0, 3, 13, 15, 13}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED).strength(0.3f).noLootTable().noOcclusion().noCollission()
					.sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY).lightLevel(state -> 5)));
	/** Marimo's floating sign: a gold bloom on the water over reef and coast. */
	public static final DeferredHolder<Block, Block> sunburst_bloom = MODELEDBLOCKS.register("sunburst_bloom",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.SunburstBloomBlock(
					BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instabreak().noLootTable()
							.noOcclusion().sound(SoundType.LILY_PAD).pushReaction(PushReaction.DESTROY)
							.lightLevel(com.vincenthuto.hemomancy.common.block.harbinger.decoration.SunburstBloomBlock::light)));
	/** A house's boundary post: one block, the house as a property. */
	public static final DeferredHolder<Block, Block> house_marker = MODELEDBLOCKS.register("house_marker",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.HouseMarkerBlock(
					BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0f, 9.0f).noLootTable()
							.noOcclusion().sound(SoundType.GILDED_BLACKSTONE).pushReaction(PushReaction.BLOCK)
							.lightLevel(state -> 3)));
	/** The relay post at a depth boundary, red current threads drifting off it. */
	public static final DeferredHolder<Block, Block> relay_marker = MODELEDBLOCKS.register("relay_marker",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.RelayMarkerBlock(
					BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(2.0f, 6.0f)
							.noLootTable().noOcclusion().sound(SoundType.BONE_BLOCK).pushReaction(PushReaction.BLOCK)
							.lightLevel(state -> 4)));
	/** A grown float on the open water with a trailing line: a messenger's buoy. */
	public static final DeferredHolder<Block, Block> messenger_buoy = MODELEDBLOCKS.register("messenger_buoy",
			() -> new CourtFixtureBlock(new double[][] {{4, 6, 4, 12, 16, 12}}, BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE).strength(0.8f).noLootTable().noOcclusion()
					.sound(SoundType.WART_BLOCK).pushReaction(PushReaction.DESTROY)));
	/** Hemorath's floor gutter: a deepslate-tile floor with a blood channel cut in it, dry to full. */
	public static final DeferredHolder<Block, Block> blood_gutter = MODELEDBLOCKS.register("blood_gutter",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.BloodGutterBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(3.5f, 6.0f)
					.sound(SoundType.DEEPSLATE_TILES)));
	/** Hemorath's standing hall basin: venous stone banded in hematic iron, a spout on its facing side. */
	public static final DeferredHolder<Block, Block> venous_basin = MODELEDBLOCKS.register("venous_basin",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.VenousBasinBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().strength(2.0f, 6.0f).noOcclusion()
					.sound(SoundType.STONE)));
	/** A cyst in the Gardens' Archive holding one Saint's Recollection. No item; it withers when drawn. */
	public static final DeferredHolder<Block, Block> recollection_cyst = MODELEDBLOCKS.register("recollection_cyst",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.plant.RecollectionCystBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE).strength(0.6f).noLootTable().noOcclusion().noCollission()
					.sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 7).pushReaction(PushReaction.DESTROY)));
	public static final DeferredHolder<Block, Block> potted_puffball_fungus = POTTEDBLOCKS.register("potted_puffball_fungus",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.puffball_fungus,
					BlockBehaviour.Properties.of().noCollission()));

	public static final DeferredHolder<Block, Block> stinkhorn_fungus = CROSSBLOCKS.register("stinkhorn_fungus",
			() -> new InfectedFungusBlock(MobEffects.CONFUSION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> potted_stinkhorn_fungus = POTTEDBLOCKS.register("potted_stinkhorn_fungus",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.stinkhorn_fungus,
					BlockBehaviour.Properties.of().noCollission()));

	public static final DeferredHolder<Block, Block> devils_tooth = CROSSBLOCKS.register("devils_tooth",
			() -> new DevilsToothBlock(MobEffects.CONFUSION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> lethean_poppy = CROSSBLOCKS.register("lethean_poppy",
			() -> new LetheanPoppyBlock(MobEffects.REGENERATION, 8,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).randomTicks()));

	public static final DeferredHolder<Block, Block> lethean_poppy_wreath = MODELEDBLOCKS.register("lethean_poppy_wreath",
			() -> new LetheanPoppyWreathBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.SNOW)
					.noOcclusion()
					.strength(0.2F)
					.sound(SoundType.AZALEA_LEAVES)));

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
			() -> new GhostPipeBlock(MobEffects.NIGHT_VISION, 10,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).randomTicks()));

	public static final DeferredHolder<Block, Block> sarcodes = CROSSBLOCKS.register("sarcodes",
			() -> new SarcodesBlock(MobEffects.REGENERATION, 8,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> rafflesia = CROSSBLOCKS.register("rafflesia",
			() -> new RafflesiaBlock(MobEffects.CONFUSION, 12,
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
			() -> new AttachedStemBlock(GOURD_STEM_BLOCK_KEY, GOURD_BLOCK_KEY, GOURD_SEED_ITEM_KEY, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.WOOD)
					.pushReaction(PushReaction.DESTROY)));

	public static final DeferredHolder<Block, Block> gourd_stem = MODELEDBLOCKS.register("gourd_stem",
			() -> new StemBlock(GOURD_BLOCK_KEY, ATTACHED_GOURD_STEM_BLOCK_KEY, GOURD_SEED_ITEM_KEY, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak()
					.sound(SoundType.HARD_CROP).pushReaction(PushReaction.DESTROY)));

	// Tiles
    public static final DeferredHolder<Block, Block> vacant_effigy = MODELEDBLOCKS.register("vacant_effigy",
            () -> new com.vincenthuto.hemomancy.common.block.harbinger.rite.VacantEffigyBlock(BlockBehaviour.Properties.of().strength(1.5f).noOcclusion()));
	public static final DeferredHolder<Block, Block> vial_centrifuge = MODELEDBLOCKS.register("vial_centrifuge",
			() -> new VialCentrifugeBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.METAL).noOcclusion()));
	public static final DeferredHolder<Block, Block> enzymatic_scriptorium = MODELEDBLOCKS.register("enzymatic_scriptorium",
			() -> new EnzymaticScriptoriumBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 8.0F).sound(SoundType.METAL).noOcclusion()));
	public static final DeferredHolder<Block, Block> resonant_forge = MODELEDBLOCKS.register("resonant_forge",
			() -> new com.vincenthuto.hemomancy.common.block.harbinger.crafting.ResonantForgeBlock(
					BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
							.strength(4.0F, 10.0F).sound(SoundType.METAL).noOcclusion()));

	public static final DeferredHolder<Block, Block> ghastly_alembic = MODELEDBLOCKS.register("ghastly_alembic",
			() -> new GhastlyAlembicBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.METAL).noOcclusion().isViewBlocking((state, level, pos) -> false)));

	public static final DeferredHolder<Block, Block> somatic_loom = MODELEDBLOCKS
			.register("somatic_loom", () -> new SomaticLoomBlock(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> antecedent_vessel = SPECIALBLOCKS.register("antecedent_vessel", com.vincenthuto.hemomancy.common.block.harbinger.functional.VigilArchiveBlock::new);
    public static final DeferredHolder<Block, Block> clairaudiograph = SPECIALBLOCKS.register("clairaudiograph", com.vincenthuto.hemomancy.common.block.harbinger.functional.ClairaudiographBlock::new);
	public static final DeferredHolder<Block, Block> phlebotomists_field_case = SPECIALBLOCKS.register("phlebotomists_field_case", PhlebotomistsFieldCaseBlock::new);

	public static final DeferredHolder<Block, Block> phlebotomists_cabinet = SPECIALBLOCKS.register("phlebotomists_cabinet", PhlebotomistsCabinetBlock::new);

	public static final DeferredHolder<Block, Block> mnemonic_reliquary = SPECIALBLOCKS.register("mnemonic_reliquary",
			MnemonicReliquaryBlock::new);
	public static final DeferredHolder<Block, Block> dictation_table = SPECIALBLOCKS.register("dictation_table",
			DictationTableBlock::new);
	public static final DeferredHolder<Block, Block> mnemonic_candle = MODELEDBLOCKS.register("mnemonic_candle",
			() -> new MnemonicCandleBlock(BlockBehaviour.Properties.of()
					.strength(0.2F, 1.0F)
					.sound(SoundType.CANDLE)
					.lightLevel(state -> state.getValue(MnemonicCandleBlock.LIT) ? 12 : 0)
					.noOcclusion()));

	public static final DeferredHolder<Block, Block> scar_station = MODELEDBLOCKS.register("scar_station",
			() -> new ScarStationBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE).noOcclusion()));
	public static final DeferredHolder<Block, Block> mason_effigy = MODELEDBLOCKS.register("mason_effigy",
			() -> new MasonsEffigyBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 8.0F).sound(SoundType.STONE).noOcclusion()));
	public static final DeferredHolder<Block, Block> iron_brazier = MODELEDBLOCKS.register("iron_brazier",
			() -> new BrazierBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> morphling_incubator = MODELEDBLOCKS.register("morphling_incubator",
			() -> new MorphlingIncubatorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> morphling_cradle = MODELEDBLOCKS.register("morphling_cradle",
			() -> new MorphlingCradleBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> specimen_jar = MODELEDBLOCKS.register("specimen_jar",
			() -> new SpecimenJarBlock(BlockBehaviour.Properties.of().strength(0.3F, 1.0F)
					.sound(SoundType.GLASS).noOcclusion()));

	public static final DeferredHolder<Block, Block> consecrated_bloodwell = MODELEDBLOCKS.register("consecrated_bloodwell",
			() -> new ConsecratedBloodwellBlock(
					BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
							.strength(3.0F, 8.0F).sound(SoundType.METAL).noOcclusion().lightLevel(s -> 3)));

	public static final DeferredHolder<Block, Block> sanguine_monolith = MODELEDBLOCKS.register("sanguine_monolith",
			() -> new SanguineMonolithBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 8.0F).sound(SoundType.STONE).lightLevel(s -> 5).noOcclusion()));

	public static final DeferredHolder<Block, Block> unstained_podium = MODELEDBLOCKS.register("unstained_podium",
			() -> new UnstainedPodiumBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> altar_of_cleansing = MODELEDBLOCKS.register("altar_of_cleansing",
			() -> new AltarOfCleansingBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 8.0F).sound(SoundType.STONE).lightLevel(s -> 7).noOcclusion()));
	public static final DeferredHolder<Block, Block> pallid_retort = MODELEDBLOCKS.register("pallid_retort",
			() -> new PallidRetortBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.METAL)));
	public static final DeferredHolder<Block, Block> stillwater_condenser = MODELEDBLOCKS.register("stillwater_condenser",
			() -> new StillwaterCondenserBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 6.0F).sound(SoundType.COPPER).noOcclusion()));
	public static final DeferredHolder<Block, Block> verdigris_lattice = MODELEDBLOCKS.register("verdigris_lattice",
			() -> new VerdigrisLatticeBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.5F, 8.0F).sound(SoundType.COPPER).noOcclusion().lightLevel(state -> 3)));

	// Idols / late utility fixtures
	public static final DeferredHolder<Block, Block> humane_idol = MODELEDBLOCKS.register("humane_idol",
			() -> new BlockHumaneIdol(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> serpentine_idol = MODELEDBLOCKS.register("serpentine_idol",
			() -> new BlockSerpentineIdol(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> mycelial_crucible = MODELEDBLOCKS.register("mycelial_crucible",
			() -> new MycelialCrucibleBlock(
					BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
							.strength(2.5F, 8.0F).sound(SoundType.FUNGUS).noOcclusion()));

	public static final DeferredHolder<Block, Block> mycelial_lantern = MODELEDBLOCKS.register("mycelial_lantern",
			() -> new MycelialLanternBlock(
					BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
							.strength(2.5F, 8.0F).sound(SoundType.LANTERN).lightLevel(s -> 8).noOcclusion()));

	public static final DeferredHolder<Block, Block> witness_organ = MODELEDBLOCKS.register("witness_organ",
			() -> new WitnessOrganBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F)
					.sound(SoundType.WOOD)
					.noOcclusion()));

	public static final DeferredHolder<Block, Block> semi_sentient_construct = MODELEDBLOCKS
			.register("semi_sentient_construct", () -> new SemiSentientConstructBlock(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> cleansed_stone = BASEBLOCKS.register("cleansed_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> pale_silver_block = BASEBLOCKS.register("pale_silver_block",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 6.0F).sound(SoundType.METAL)));

	public static final DeferredHolder<Block, Block> pale_silver_bars = BASEBLOCKS.register("pale_silver_bars",
			() -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)));

	public static final DeferredHolder<Block, Block> pale_silver_bells = BASEBLOCKS.register("pale_silver_bells",
			() -> new PaleSilverBellsBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 5.0F).sound(SoundType.METAL).lightLevel(s -> 10).noOcclusion()));

	public static final DeferredHolder<Block, Block> pallid_lantern = BASEBLOCKS.register("pallid_lantern",
			() -> new LanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)));
	public static final DeferredHolder<Block, Block> hematic_lantern = BASEBLOCKS.register("hematic_lantern",
			() -> new LanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)));

	public static final DeferredHolder<Block, Block> pallid_silver_chain = BASEBLOCKS.register("pallid_silver_chain",
			() -> new ChainBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHAIN)));

	public static final DeferredHolder<Block, Block> hematic_iron_chain = BASEBLOCKS.register("hematic_iron_chain",
			() -> new ChainBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHAIN)));

	public static final DeferredHolder<Block, Block> scrying_podium = MODELEDBLOCKS.register("scrying_podium",
			() -> new ScryingPodiumBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> scarlet_vanity = MODELEDBLOCKS.register("scarlet_vanity",
			() -> new ScarletVanityBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 6.0F).sound(SoundType.WOOD).noOcclusion()));

	public static final DeferredHolder<Block, Block> fungal_podium = MODELEDBLOCKS.register("fungal_podium",
			() -> new FungalPodiumBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));
	public static final DeferredHolder<Block, Block> saint_sarcophagus = MODELEDBLOCKS.register("saint_sarcophagus",
			() -> new SaintSarcophagusBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 8.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> sanguine_omen = BASEBLOCKS.register("sanguine_omen",
			() -> new SanguineOmenBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
					.requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundType.STONE).lightLevel(s -> 3)
					.noOcclusion()));

	public static final DeferredHolder<Block, Block> fungal_implantation_pylon = MODELEDBLOCKS
			.register("fungal_implantation_pylon", () -> new FungalImplantationPylonBlock(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> dendritic_distributor = MODELEDBLOCKS.register("dendritic_distributor",
			() -> new DendriticDistributorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> hematic_stake = MODELEDBLOCKS.register("hematic_stake",
			() -> new HematicStakeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
					.noCollission().instabreak().sound(SoundType.METAL).noOcclusion()
					.lightLevel(s -> 2)));

	public static final DeferredHolder<Block, Block> gourdvine_tap = MODELEDBLOCKS.register("gourdvine_tap",
			() -> new GourdvineTapBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.PLANT)
					.noCollission()
					.instabreak()
					.noOcclusion()
					.sound(SoundType.GRASS)));

	public static final DeferredHolder<Block, Block> sanguine_condenser = MODELEDBLOCKS.register("sanguine_condenser",
			() -> new SanguineCondenserBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
					.requiresCorrectToolForDrops().strength(2.0F).sound(SoundType.METAL).noOcclusion()));

	public static final DeferredHolder<Block, Block> covenant_throne = MODELEDBLOCKS.register("covenant_throne",
			() -> new CovenantThroneBlock(
					BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
							.strength(3.5F, 9.0F).sound(SoundType.BONE_BLOCK).lightLevel(s -> 4).noOcclusion()));

	public static final DeferredHolder<Block, Block> warp_chair = MODELEDBLOCKS.register("warp_chair",
			() -> new WarpChairBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
					.strength(2.5F, 6.0F).sound(SoundType.WOOD).lightLevel(s -> 3).noOcclusion()
					.pushReaction(PushReaction.BLOCK)));

	public static final DeferredHolder<Block, Block> warp_chair_filler = SPECIALBLOCKS.register("warp_chair_filler",
			() -> new WarpChairFillerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
					.strength(2.5F, 6.0F).sound(SoundType.WOOD).noOcclusion()
					.pushReaction(PushReaction.BLOCK)));

	public static final DeferredHolder<Block, Block> hematic_armature = MODELEDBLOCKS.register("hematic_armature",
			() -> new HematicArmatureBlock(
					BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
							.requiresCorrectToolForDrops()
							.strength(3.5F, 9.0F).sound(SoundType.BONE_BLOCK).lightLevel(s -> 4).noOcclusion()));

	public static final DeferredHolder<Block, Block> sanguine_vigil = MODELEDBLOCKS.register("sanguine_vigil",
			() -> new SanguineVigilBlock(
					BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
							.strength(3.0F, 8.0F).sound(SoundType.BONE_BLOCK).lightLevel(s -> 6).noOcclusion()));

	public static final DeferredHolder<Block, Block> mortal_display = MODELEDBLOCKS.register("mortal_display",
			() -> new MortalDisplayBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> puppeteers_spindle = MODELEDBLOCKS
			.register("puppeteers_spindle", () -> new PuppeteersSpindleBlock(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.WOOD).noOcclusion()));
	public static final DeferredHolder<Block, Block> earthen_vein = MODELEDBLOCKS.register("earthen_vein",
			() -> new EarthenVeinBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.STONE)));

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

	// Puzzle / boss-room blocks Hemorath encounter
	public static final DeferredHolder<Block, Block> non_euclidean_hallway = MODELEDBLOCKS.register(
			"non_euclidean_hallway",
			() -> new NonEuclideanHallwayBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.noCollission()
					.noOcclusion()
					.strength(0.8F, 8.0F)
					.sound(SoundType.GLASS)
					.lightLevel(state -> 4)));

	public static final DeferredHolder<Block, Block> blood_basin = MODELEDBLOCKS.register("blood_basin",
			() -> new BloodBasinBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 6.0F).sound(SoundType.STONE).noOcclusion()));

	public static final DeferredHolder<Block, Block> offering_gate = SPECIALBLOCKS.register("offering_gate",
			() -> new OfferingGateBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 8.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> blood_pylon = MODELEDBLOCKS.register("blood_pylon",
			() -> new BloodPylonBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 6.0F).sound(SoundType.STONE)));

	public static final DeferredHolder<Block, Block> blood_trial_altar = MODELEDBLOCKS.register("blood_trial_altar",
			() -> new BloodTrialAltarBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(3.0F, 8.0F).sound(SoundType.STONE)));

	// ── Vanilla-gate rite components ──
	public static final DeferredHolder<Block, Block> crucible_of_nether_ichor = SPECIALBLOCKS.register(
			"crucible_of_nether_ichor",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 6.0F).sound(SoundType.STONE)
					.mapColor(MapColor.NETHER)));

	public static final DeferredHolder<Block, Block> voidtouched_vessel = SPECIALBLOCKS.register(
			"voidtouched_vessel",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 6.0F).sound(SoundType.STONE)
					.mapColor(MapColor.COLOR_PURPLE)));

    public static final DeferredHolder<Block, Block> circus_stage_record = MODELEDBLOCKS.register("circus_stage_record", () -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.CircusEvidenceBlock(BlockBehaviour.Properties.of().strength(2).sound(SoundType.WOOD), "stage"));
    public static final DeferredHolder<Block, Block> circus_loft_record = MODELEDBLOCKS.register("circus_loft_record", () -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.CircusEvidenceBlock(BlockBehaviour.Properties.of().strength(2).sound(SoundType.WOOD), "loft"));
    public static final DeferredHolder<Block, Block> circus_quarters_record = MODELEDBLOCKS.register("circus_quarters_record", () -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.CircusEvidenceBlock(BlockBehaviour.Properties.of().strength(2).sound(SoundType.WOOD), "quarters"));
    public static final DeferredHolder<Block, Block> circus_carousel_record = MODELEDBLOCKS.register("circus_carousel_record", () -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.CircusEvidenceBlock(BlockBehaviour.Properties.of().strength(2).sound(SoundType.WOOD), "carousel"));
    public static final DeferredHolder<Block, Block> laughing_mask = MODELEDBLOCKS.register("laughing_mask", () -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.TroupePropBlock(BlockBehaviour.Properties.of().strength(1).sound(SoundType.WOOD).noOcclusion(), false));
    public static final DeferredHolder<Block, Block> mourning_mask = MODELEDBLOCKS.register("mourning_mask", () -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.TroupePropBlock(BlockBehaviour.Properties.of().strength(1).sound(SoundType.WOOD).noOcclusion(), false));
    public static final DeferredHolder<Block, Block> blank_mask = MODELEDBLOCKS.register("blank_mask", () -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.TroupePropBlock(BlockBehaviour.Properties.of().strength(1).sound(SoundType.WOOD).noOcclusion(), false));
    public static final DeferredHolder<Block, Block> troupe_rigging = MODELEDBLOCKS.register("troupe_rigging", () -> new com.vincenthuto.hemomancy.common.block.harbinger.decoration.TroupePropBlock(BlockBehaviour.Properties.of().strength(1).sound(SoundType.WOOD).noOcclusion(), true));
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

	public enum ItemRoute { AUTO, CUSTOM, MANUAL, NONE }
	public enum CreativeRoute { MAIN, WIP, HIDDEN }
	public record BlockItemDecision(ItemRoute itemRoute, ResourceLocation itemId, CreativeRoute creativeRoute) {}

	// Every block in the eight ordinary registers defaults to a same-ID BlockItem in the main tab.
	// These are the exceptions; potted and liquid registers have no item or tab entry.
	private static final Map<String, Function<Block, BlockItem>> CUSTOM_BLOCK_ITEMS = Map.ofEntries(
			Map.entry("mortal_display", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("suspended_blood_crystal", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("suspended_cleansed_blood_crystal", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("suspended_vivianite", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("visceral_mirror", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("fungal_implantation_pylon", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("phlebotomists_field_case", b -> new PhlebotomistsFieldCaseBlockItem(b, new Item.Properties())),
			Map.entry("earthen_vein", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("vial_centrifuge", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("hematic_armature", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("mycelial_crucible", b -> new MycelialCrucibleBlockItem(b, new Item.Properties())),
			Map.entry("specimen_jar", b -> new SpecimenJarBlockItem(b, new Item.Properties())),
			Map.entry("sanguine_monolith", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("hematic_stake", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("puppeteers_spindle", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("mason_effigy", b -> new BlockItem(b, new Item.Properties())),
			Map.entry("covenant_throne", b -> new BlockItem(b, new Item.Properties())),
			// One item for every Saint's tomb; the stack names its Saint, and the tab lists one per palette.
			Map.entry("saint_sarcophagus", b -> new com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SaintSarcophagusBlockItem(b, new Item.Properties())),
			// Sits on the water like a lily pad; the house marker carries its house on the stack.
			Map.entry("sunburst_bloom", b -> new net.minecraft.world.item.PlaceOnWaterBlockItem(b, new Item.Properties())),
			Map.entry("house_marker", b -> new com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.HouseMarkerBlockItem(b, new Item.Properties())));
	private static final Map<String, String> MANUAL_BLOCK_ITEMS = Map.of(
			"gourd_stem", "gourd_seeds",
			"active_befouling_ash_trail", "active_befouling_ash",
			"active_smouldering_ash_trail", "active_smouldering_ash",
			"placed_blood_stained_stone", "blood_stained_stone",
			"lethean_poppy_wreath", "lethean_poppy_wreath",
			"sanguine_conduit", "sanguine_conduit");
	private static final Set<String> ITEMLESS_BLOCKS = Set.of("attached_gourd_stem", "filler_block",
			"warp_chair_filler", "abocipher_emitter", "qliphoth_bloom",
			"escharian_overgrowth", "escharian_overgrowth_rim", "brood_tumor", "dormant_ganglion", "cap_bridge", "fruiting_body_cache", "ward_anchor", "ring_door", "recollection_cyst",
			"witness_devotee", "warden_bloom");
	private static final Set<String> HIDDEN_BLOCK_ITEMS = Set.of("active_befouling_ash_trail",
			"active_smouldering_ash_trail", "engram_block", "mycelial_synapse");

	public static BlockItemDecision itemDecision(ResourceLocation blockId) {
		String path = blockId.getPath();
		if (POTTEDBLOCKS.getEntries().stream().anyMatch(entry -> entry.getId().equals(blockId))
				|| LIQUIDBLOCKS.getEntries().stream().anyMatch(entry -> entry.getId().equals(blockId))
				|| ITEMLESS_BLOCKS.contains(path)) {
			return new BlockItemDecision(ItemRoute.NONE, null, CreativeRoute.HIDDEN);
		}
		CreativeRoute tab = CreativeTabInit.isWipBlock(blockId) ? CreativeRoute.WIP
				: HIDDEN_BLOCK_ITEMS.contains(path) ? CreativeRoute.HIDDEN : CreativeRoute.MAIN;
		String manualId = MANUAL_BLOCK_ITEMS.get(path);
		if (manualId != null) {
			return new BlockItemDecision(ItemRoute.MANUAL, Hemomancy.rloc(manualId), tab);
		}
		return new BlockItemDecision(CUSTOM_BLOCK_ITEMS.containsKey(path) ? ItemRoute.CUSTOM : ItemRoute.AUTO,
				blockId, tab);
	}

	private static Pair<ResourceLocation, BlockItem> createItemBlock(Pair<? extends Block, ResourceLocation> block) {
		Function<Block, BlockItem> factory = CUSTOM_BLOCK_ITEMS.get(block.getSecond().getPath());
		BlockItem item = factory != null ? factory.apply(block.getFirst())
				: new BlockItem(block.getFirst(), new Item.Properties());
		return Pair.of(block.getSecond(), item);
	}

	@SubscribeEvent
	public static void onRegisterItems(final RegisterEvent event) {
		if (event.getRegistryKey() != Registries.ITEM) {
			return;
		}

		getAllBlockEntriesAsStream()
				.map(m -> Pair.of(m.get(), m.getId()))
				.filter(block -> !shouldSkipAutoBlockItem(block.getSecond()))
				.map(BlockInit::createItemBlock)
				.forEach(item -> registerBlockItem(event, item));
	}

	private static boolean shouldSkipAutoBlockItem(ResourceLocation blockId) {
		ItemRoute route = itemDecision(blockId).itemRoute();
		return route == ItemRoute.MANUAL || route == ItemRoute.NONE;
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
