package com.vincenthuto.hemomancy.common.init;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.*;
import com.vincenthuto.hemomancy.common.block.crafting.BlockChiselStation;
import com.vincenthuto.hemomancy.common.block.crafting.JuiceinatorBlock;
import com.vincenthuto.hemomancy.common.block.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.block.crafting.VialCentrifugeBlock;
import com.vincenthuto.hemomancy.common.block.functional.*;
import com.vincenthuto.hemomancy.common.block.idol.BlockHumaneIdol;
import com.vincenthuto.hemomancy.common.block.idol.BlockSerpentineIdol;
import com.vincenthuto.hemomancy.common.block.plant.*;
import com.vincenthuto.hemomancy.common.item.tile.EarthenVeinBlockItem;
import com.vincenthuto.hemomancy.common.item.tile.FungalImplantationPylonBlockItem;
import com.vincenthuto.hemomancy.common.item.tile.MnemonicReliquaryBlockItem;
import com.vincenthuto.hemomancy.common.item.tile.MortalDisplayBlockItem;
import com.vincenthuto.hemomancy.common.item.tile.SuspendedBloodCrystalBlockItem;
import com.vincenthuto.hemomancy.common.item.tile.SuspendedCleansedBloodCrystalBlockItem;
import com.vincenthuto.hemomancy.common.item.tile.SuspendedVivianiteBlockItem;
import com.vincenthuto.hemomancy.common.item.tile.VialCentrifugeBlockItem;
import com.vincenthuto.hemomancy.common.item.tile.VisceralMirrorBlockItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
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

	public static final DeferredRegister<Block> POTTEDBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);

	// Ash
	public static final RegistryObject<Block> smouldering_ash_trail = SPECIALBLOCKS.register("smouldering_ash_trail",
			() -> new SmoulderingAshTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));

	public static final RegistryObject<Block> befouling_ash_trail = SPECIALBLOCKS.register("befouling_ash_trail",
			() -> new BefoulingAshTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));

	public static final RegistryObject<Block> active_smouldering_ash_trail = SPECIALBLOCKS.register(
			"active_smouldering_ash_trail",
			() -> new ActiveSmoulderingAshTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));
	public static final RegistryObject<Block> active_befouling_ash_trail = SPECIALBLOCKS.register(
			"active_befouling_ash_trail",
			() -> new ActiveBefoulingAshTrailBlock(BlockBehaviour.Properties.of().noCollission().instabreak()));

	public static final RegistryObject<Block> engram_block = SPECIALBLOCKS.register("engram_block", EngramBlock::new);

	public static final RegistryObject<Block> filler_block = SPECIALBLOCKS.register("filler_block",
			() -> new FillerBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noOcclusion().noLootTable()));

	// Blocks

	public static final RegistryObject<Block> sanguine_glass = BASEBLOCKS.register("sanguine_glass",
			() -> new GlassBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final RegistryObject<Block> sanguine_pane = SPECIALBLOCKS.register("sanguine_pane",
			() -> new IronBarsBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final RegistryObject<Block> vivianite_glass = BASEBLOCKS.register("vivianite_glass",
			() -> new GlassBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final RegistryObject<Block> vivianite_pane = SPECIALBLOCKS.register("vivianite_pane",
			() -> new IronBarsBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final RegistryObject<Block> cleansed_sanguine_glass = BASEBLOCKS.register("cleansed_sanguine_glass",
			() -> new GlassBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final RegistryObject<Block> cleansed_sanguine_pane = SPECIALBLOCKS.register("cleansed_sanguine_pane",
			() -> new IronBarsBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final RegistryObject<Block> venous_stone = BASEBLOCKS.register("venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final RegistryObject<Block> venous_stone_slab = SLABBLOCKS.register("venous_stone_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final RegistryObject<Block> venous_stone_stairs = BASEBLOCKS.register(("venous_stone_stairs"),
			() -> new StairBlock(() -> venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(venous_stone.get())));

	public static final RegistryObject<Block> gilded_venous_stone = BASEBLOCKS.register("gilded_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone = BASEBLOCKS.register("polished_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_slab = SLABBLOCKS.register(
			"polished_venous_stone_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_stairs = STAIRBLOCKS.register(
			("polished_venous_stone_stairs"), () -> new StairBlock(() -> venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(venous_stone.get())));
	public static final RegistryObject<Block> chiseled_polished_venous_stone = BASEBLOCKS.register(
			"chiseled_polished_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_bricks = STAIRBLOCKS.register(
			"polished_venous_stone_bricks",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_brick_slab = SLABBLOCKS.register(
			"polished_venous_stone_brick_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_brick_stairs = STAIRBLOCKS.register(
			("polished_venous_stone_brick_stairs"), () -> new StairBlock(() -> venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(venous_stone.get())));
	public static final RegistryObject<Block> cracked_polished_venous_stone_bricks = BASEBLOCKS.register(
			"cracked_polished_venous_stone_bricks",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final RegistryObject<Block> infested_venous_stone = BASEBLOCKS.register("infested_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)));

	public static final RegistryObject<Block> conscious_mass = BASEBLOCKS.register("conscious_mass",
			() -> new Block(BlockBehaviour.Properties.of().sound(SoundType.WART_BLOCK).strength(0.2F, 1.0F)));

	public static final RegistryObject<Block> hematic_iron_block = BASEBLOCKS.register("hematic_iron_block",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
					.sound(SoundType.METAL)));
	public static final RegistryObject<Block> hematic_iron_pillar = COLUMNBLOCKS.register("hematic_iron_pillar",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(5.0F, 6.0F).sound(SoundType.METAL)));

	public static final RegistryObject<Block> chiseled_hematic_iron_block = BASEBLOCKS
			.register("chiseled_hematic_iron_block", () -> new Block(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)));

	public static final RegistryObject<Block> hemolytic_plating_block = BASEBLOCKS.register("hemolytic_plating_block",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
					.sound(SoundType.METAL)));

	public static final RegistryObject<Block> crimson_flames = SPECIALBLOCKS.register("crimson_flames",
            CrimsonFlameBlock::new);

	public static final RegistryObject<Block> mnemonic_reliquary = SPECIALBLOCKS.register("mnemonic_reliquary",
			MnemonicReliquaryBlock::new);

	// Plants

	public static final RegistryObject<Block> blood_wood_log = COLUMNBLOCKS.register("blood_wood_log",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final RegistryObject<Block> blood_wood_planks = BASEBLOCKS.register("blood_wood_planks",
			() -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD)));

	public static final RegistryObject<Block> hyphae = CROSSBLOCKS.register("hyphae",
			() -> new HyphaeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission()
					.instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ).ignitedByLava()
					.pushReaction(PushReaction.DESTROY)));

	public static final RegistryObject<Block> bleeding_heart = CROSSBLOCKS.register("bleeding_heart",
			() -> new BleedingHeartBlock(MobEffects.ABSORPTION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final RegistryObject<Block> potted_bleeding_heart = POTTEDBLOCKS.register("potted_bleeding_heart",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.bleeding_heart,
					BlockBehaviour.Properties.of()));

	public static final RegistryObject<Block> infected_fungus = CROSSBLOCKS.register("infected_fungus",
			() -> new InfectedFungusBlock(MobEffects.CONFUSION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final RegistryObject<Block> potted_infected_fungus = POTTEDBLOCKS.register("potted_infected_fungus",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.infected_fungus,
					BlockBehaviour.Properties.of().noCollission()));

	public static final RegistryObject<Block> puffball_fungus = MODELEDBLOCKS.register("puffball_fungus",
			() -> new PuffballFungusBlock(MobEffects.SATURATION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final RegistryObject<Block> potted_puffball_fungus = POTTEDBLOCKS.register("potted_puffball_fungus",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.puffball_fungus,
					BlockBehaviour.Properties.of().noCollission()));

	public static final RegistryObject<Block> stinkhorn_fungus = CROSSBLOCKS.register("stinkhorn_fungus",
			() -> new InfectedFungusBlock(MobEffects.CONFUSION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final RegistryObject<Block> potted_stinkhorn_fungus = POTTEDBLOCKS.register("potted_stinkhorn_fungus",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.stinkhorn_fungus,
					BlockBehaviour.Properties.of().noCollission()));

	public static final RegistryObject<Block> lethean_poppy = CROSSBLOCKS.register("lethean_poppy",
			() -> new LetheanPoppyBlock(MobEffects.REGENERATION, 8,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).randomTicks()));

	public static final RegistryObject<Block> erythrocytic_dirt = BASEBLOCKS.register("erythrocytic_dirt",
			() -> new Block(
					BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));

	public static final RegistryObject<Block> erythrocytic_mycelium = BASEBLOCKS.register("erythrocytic_mycelium",
			() -> new ErythrocyticMyceliumBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
					.randomTicks().strength(0.6F).sound(SoundType.GRASS)));

	public static final RegistryObject<Block> infected_stem = BASEBLOCKS.register("infected_stem",
			() -> new Block(BlockBehaviour.Properties.of().strength(0.5f, 15f).sound(SoundType.GRASS)));

	public static final RegistryObject<Block> hyphae_block = BASEBLOCKS.register("hyphae_block",
			() -> new Block(BlockBehaviour.Properties.of().strength(0.5f, 15f).sound(SoundType.GRASS)));

	public static final RegistryObject<Block> infected_cap = BASEBLOCKS.register("infected_cap",
			() -> new InfectedCapBlock(BlockBehaviour.Properties.of().strength(0.5f, 15f).sound(SoundType.GRASS)));

	public static final RegistryObject<Block> fruiting_infected_cap = BASEBLOCKS.register("fruiting_infected_cap",
			() -> new InfectedCapBlock(BlockBehaviour.Properties.of().strength(0.5f, 15f).sound(SoundType.GRASS)));

	public static final RegistryObject<Block> infested_wood = BASEBLOCKS.register("infested_wood",
			() -> new InfestedWoodBlock(BlockBehaviour.Properties.of().strength(1.5F, 3.0F).sound(SoundType.WOOD)
					.randomTicks()));

	// Myco-heterotrophic Plants

	public static final RegistryObject<Block> ghost_pipe = CROSSBLOCKS.register("ghost_pipe",
			() -> new GhostPipeBlock(MobEffects.NIGHT_VISION, 10,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).randomTicks()));

	public static final RegistryObject<Block> sarcodes = CROSSBLOCKS.register("sarcodes",
			() -> new SarcodesBlock(MobEffects.REGENERATION, 8,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final RegistryObject<Block> rafflesia = CROSSBLOCKS.register("rafflesia",
			() -> new RafflesiaBlock(MobEffects.CONFUSION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).randomTicks()));

	public static final RegistryObject<Block> potted_ghost_pipe = POTTEDBLOCKS.register("potted_ghost_pipe",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.ghost_pipe,
					BlockBehaviour.Properties.of().noCollission()));

	public static final RegistryObject<Block> potted_sarcodes = POTTEDBLOCKS.register("potted_sarcodes",
			() -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockInit.sarcodes,
					BlockBehaviour.Properties.of().noCollission()));

	// Gourd
	public static final RegistryObject<Block> gourd = MODELEDBLOCKS.register("gourd",
			() -> new GourdBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
					.instrument(NoteBlockInstrument.DIDGERIDOO).strength(1.0F).sound(SoundType.WOOD)
					.pushReaction(PushReaction.DESTROY)));

	public static final RegistryObject<Block> attached_gourd_stem = MODELEDBLOCKS.register("attached_gourd_stem",
			() -> new AttachedStemBlock((StemGrownBlock) gourd.get(), () -> {
				return ItemInit.gourd_seeds.get();
			}, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.WOOD)
					.pushReaction(PushReaction.DESTROY)));

	public static final RegistryObject<Block> gourd_stem = MODELEDBLOCKS.register("gourd_stem",
			() -> new StemBlock((StemGrownBlock) gourd.get(), () -> {
				return ItemInit.gourd_seeds.get();
			}, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak()
					.sound(SoundType.HARD_CROP).pushReaction(PushReaction.DESTROY)));

	// Idols
	public static final RegistryObject<Block> humane_idol = MODELEDBLOCKS.register("humane_idol",
			() -> new BlockHumaneIdol(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.STONE)));
	public static final RegistryObject<Block> serpentine_idol = MODELEDBLOCKS.register("serpentine_idol",
			() -> new BlockSerpentineIdol(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	// Tiles
	public static final RegistryObject<Block> runic_chisel_station = MODELEDBLOCKS.register("runic_chisel_station",
			() -> new BlockChiselStation(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final RegistryObject<Block> morphling_incubator = MODELEDBLOCKS.register("morphling_incubator",
			() -> new MorphlingIncubatorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final RegistryObject<Block> semi_sentient_construct = MODELEDBLOCKS
			.register("semi_sentient_construct", () -> new SemiSentientConstructBlock(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final RegistryObject<Block> unstained_podium = MODELEDBLOCKS.register("unstained_podium",
			() -> new UnstainedPodiumBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final RegistryObject<Block> altar_of_cleansing = MODELEDBLOCKS.register("altar_of_cleansing",
			() -> new AltarOfCleansingBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(2.0F, 8.0F).sound(SoundType.STONE).lightLevel(s -> 7)));

	public static final RegistryObject<Block> cleansed_stone = BASEBLOCKS.register("cleansed_stone",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final RegistryObject<Block> lethe_lantern = BASEBLOCKS.register("lethe_lantern",
			() -> new Block(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.0F, 3.0F).sound(SoundType.LANTERN).lightLevel(s -> 12)));

	public static final RegistryObject<Block> scrying_podium = MODELEDBLOCKS.register("scrying_podium",
			() -> new ScryingPodiumBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final RegistryObject<Block> fungal_podium = MODELEDBLOCKS.register("fungal_podium",
			() -> new FungalPodiumBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final RegistryObject<Block> fungal_implantation_pylon = MODELEDBLOCKS
			.register("fungal_implantation_pylon", () -> new FungalImplantationPylonBlock(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final RegistryObject<Block> dendritic_distributor = MODELEDBLOCKS.register("dendritic_distributor",
			() -> new DendriticDistributorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final RegistryObject<Block> mortal_display = MODELEDBLOCKS.register("mortal_display",
			() -> new MortalDisplayBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.STONE)));

	public static final RegistryObject<Block> somatic_loom = MODELEDBLOCKS
			.register("somatic_loom", () -> new SomaticLoomBlock(BlockBehaviour.Properties.of()
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)));
	public static final RegistryObject<Block> earthen_vein = MODELEDBLOCKS.register("earthen_vein",
			() -> new EarthenVeinBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.STONE)));
	public static final RegistryObject<Block> iron_brazier = MODELEDBLOCKS.register("iron_brazier",
			() -> new BrazierBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.METAL)));
	public static final RegistryObject<Block> juiceinator = MODELEDBLOCKS.register("juiceinator",
			() -> new JuiceinatorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.5F, 6.0F)
					.sound(SoundType.METAL)));

	public static final RegistryObject<Block> vial_centrifuge = MODELEDBLOCKS.register("vial_centrifuge",
			() -> new VialCentrifugeBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.METAL)));

	public static final RegistryObject<Block> blood_crystal = MODELEDBLOCKS.register("blood_crystal",
			() -> new BloodCrystalBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.METAL)));

	public static final RegistryObject<Block> suspended_vivianite = MODELEDBLOCKS.register("suspended_vivianite",
			() -> new SuspendedVivianiteBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.AMETHYST)));

	public static final RegistryObject<Block> suspended_blood_crystal = MODELEDBLOCKS.register("suspended_blood_crystal",
			() -> new SuspendedBloodCrystalBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.AMETHYST)));

	public static final RegistryObject<Block> suspended_cleansed_blood_crystal = MODELEDBLOCKS.register("suspended_cleansed_blood_crystal",
			() -> new SuspendedCleansedBloodCrystalBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F).sound(SoundType.AMETHYST)));

	public static final RegistryObject<Block> bog_body = MODELEDBLOCKS.register("bog_body",
			() -> new BogBodyBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.5F, 6.0F).sound(SoundType.WART_BLOCK)));

	public static final RegistryObject<Block> visceral_mirror = MODELEDBLOCKS.register("visceral_mirror",
			() -> new VisceralMirrorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()
					.noOcclusion().strength(2.0F, 8.0F).sound(SoundType.GLASS)));

	public static List<Block> getAllBlockEntries() {
		List<Block> blocks = new ArrayList<>();
		BASEBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		SLABBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		STAIRBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		COLUMNBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		CROSSBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		OBJBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		MODELEDBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		SPECIALBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		POTTEDBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));

		return blocks;
	}

	public static Stream<RegistryObject<Block>> getAllBlockEntriesAsStream() {

		Stream<RegistryObject<Block>> combinedStream = Stream.of(BASEBLOCKS.getEntries(), SLABBLOCKS.getEntries(),
				STAIRBLOCKS.getEntries(), COLUMNBLOCKS.getEntries(), CROSSBLOCKS.getEntries(),
				MODELEDBLOCKS.getEntries(), SPECIALBLOCKS.getEntries(), OBJBLOCKS.getEntries())
				.flatMap(Collection::stream);

		return combinedStream;
	}

	public static Pair<ResourceLocation, BlockItem> createItemBlock(Pair<Block, ResourceLocation> block) {
		Block b = block.getFirst();
		if (b == BlockInit.fungal_implantation_pylon.get()) {
			return Pair.of(block.getSecond(),
					new FungalImplantationPylonBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.vial_centrifuge.get()) {
			return Pair.of(block.getSecond(),
					new VialCentrifugeBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.earthen_vein.get()) {
			return Pair.of(block.getSecond(),
					new EarthenVeinBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.mortal_display.get()) {
			return Pair.of(block.getSecond(),
					new MortalDisplayBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.suspended_vivianite.get()) {
			return Pair.of(block.getSecond(),
					new SuspendedVivianiteBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.suspended_blood_crystal.get()) {
			return Pair.of(block.getSecond(),
					new SuspendedBloodCrystalBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.suspended_cleansed_blood_crystal.get()) {
			return Pair.of(block.getSecond(),
					new SuspendedCleansedBloodCrystalBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.mnemonic_reliquary.get()) {
			return Pair.of(block.getSecond(),
					new MnemonicReliquaryBlockItem(b, new Item.Properties()));
		}
		if (b == BlockInit.visceral_mirror.get()) {
			return Pair.of(block.getSecond(),
					new VisceralMirrorBlockItem(b, new Item.Properties()));
		}
		return Pair.of(block.getSecond(), new BlockItem(b, new Item.Properties()));
	}

	@SubscribeEvent
	public static void onRegisterItems(final RegisterEvent event) {
		if (event.getRegistryKey() != ForgeRegistries.Keys.ITEMS) {
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
					|| item.getSecond().getBlock() != BlockInit.filler_block.get()) {
				registerBlockItem(event, item);
			}
		});
	}

	private static void registerBlockItem(RegisterEvent event, Pair<ResourceLocation, BlockItem> item) {
		event.register(ForgeRegistries.Keys.ITEMS, helper -> helper.register(item.getFirst(), item.getSecond()));
	}

//	@SubscribeEvent
//	public static void registerBlocks(FMLClientSetupEvent event) {
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.smouldering_ash_trail.get(), RenderType.cutoutMipped());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.befouling_ash_trail.get(), RenderType.cutoutMipped());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.active_smouldering_ash_trail.get(), RenderType.cutoutMipped());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.active_befouling_ash_trail.get(), RenderType.cutoutMipped());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.earthen_vein.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.bleeding_heart.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.potted_bleeding_heart.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.potted_infected_fungus.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.potted_stinkhorn_fungus.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.potted_puffball_fungus.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.iron_brazier.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.infected_fungus.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.stinkhorn_fungus.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.fungal_podium.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.hyphae.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.puffball_fungus.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.fungal_implantation_pylon.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.gourd_stem.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.attached_gourd_stem.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.engram_block.get(), RenderType.translucent());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.bog_body.get(), RenderType.translucent());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.suspended_vivianite.get(), RenderType.translucent());
//		ItemBlockRenderTypes.setRenderLayer(BlockInit.suspended_blood_crystal.get(), RenderType.translucent());
//	}

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
		});
	}

}
