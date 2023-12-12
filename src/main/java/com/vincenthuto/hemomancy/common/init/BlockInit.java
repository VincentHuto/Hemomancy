package com.vincenthuto.hemomancy.common.init;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.ActiveBefoulingAshTrailBlock;
import com.vincenthuto.hemomancy.common.block.ActiveSmoulderingAshTrailBlock;
import com.vincenthuto.hemomancy.common.block.BefoulingAshTrailBlock;
import com.vincenthuto.hemomancy.common.block.BleedingHeartBlock;
import com.vincenthuto.hemomancy.common.block.BlockChiselStation;
import com.vincenthuto.hemomancy.common.block.BloodCrystalBlock;
import com.vincenthuto.hemomancy.common.block.BrazierBlock;
import com.vincenthuto.hemomancy.common.block.CrimsonFlameBlock;
import com.vincenthuto.hemomancy.common.block.DendriticDistributorBlock;
import com.vincenthuto.hemomancy.common.block.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.block.ErythrocyticMyceliumBlock;
import com.vincenthuto.hemomancy.common.block.FungalImplantationPylonBlock;
import com.vincenthuto.hemomancy.common.block.FungalPodiumBlock;
import com.vincenthuto.hemomancy.common.block.HyphaeBlock;
import com.vincenthuto.hemomancy.common.block.InfectedCapBlock;
import com.vincenthuto.hemomancy.common.block.InfectedFungusBlock;
import com.vincenthuto.hemomancy.common.block.JuiceinatorBlock;
import com.vincenthuto.hemomancy.common.block.MorphlingIncubatorBlock;
import com.vincenthuto.hemomancy.common.block.MortalDisplayBlock;
import com.vincenthuto.hemomancy.common.block.PuffballFungusBlock;
import com.vincenthuto.hemomancy.common.block.ScryingPodiumBlock;
import com.vincenthuto.hemomancy.common.block.SemiSentientConstructBlock;
import com.vincenthuto.hemomancy.common.block.SmoulderingAshTrailBlock;
import com.vincenthuto.hemomancy.common.block.UnstainedPodiumBlock;
import com.vincenthuto.hemomancy.common.block.VialCentrifugeBlock;
import com.vincenthuto.hemomancy.common.block.VisceralRecallerBlock;
import com.vincenthuto.hemomancy.common.block.idol.BlockHumaneIdol;
import com.vincenthuto.hemomancy.common.block.idol.BlockSerpentineIdol;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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

	// Blocks

	public static final RegistryObject<Block> sanguine_glass = BASEBLOCKS.register("sanguine_glass",
			() -> new GlassBlock(
					BlockBehaviour.Properties.of().strength(0.1f, 1f).sound(SoundType.GLASS).noOcclusion()));

	public static final RegistryObject<Block> sanguine_pane = SPECIALBLOCKS.register("sanguine_pane",
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

	public static final RegistryObject<Block> blood_wood_log = COLUMNBLOCKS.register("blood_wood_log",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final RegistryObject<Block> blood_wood_planks = BASEBLOCKS.register("blood_wood_planks",
			() -> new Block(BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD)));

	public static final RegistryObject<Block> hyphae = CROSSBLOCKS.register("hyphae",
			() -> new HyphaeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).replaceable()
					.noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ)
					.ignitedByLava().pushReaction(PushReaction.DESTROY)));

	public static final RegistryObject<Block> bleeding_heart = CROSSBLOCKS.register("bleeding_heart",
			() -> new BleedingHeartBlock(MobEffects.ABSORPTION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final RegistryObject<Block> potted_bleeding_heart = POTTEDBLOCKS.register("potted_bleeding_heart",
			() -> new FlowerPotBlock(null, BlockInit.bleeding_heart, BlockBehaviour.Properties.of()));

	public static final RegistryObject<Block> infected_fungus = CROSSBLOCKS.register("infected_fungus",
			() -> new InfectedFungusBlock(MobEffects.CONFUSION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	public static final RegistryObject<Block> puffball_fungus = MODELEDBLOCKS.register("puffball_fungus",
			() -> new PuffballFungusBlock(MobEffects.SATURATION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));
	
	
	public static final RegistryObject<Block> stinkhorn_fungus = CROSSBLOCKS.register("stinkhorn_fungus",
			() -> new InfectedFungusBlock(MobEffects.CONFUSION, 12,
					BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS)));

	

	public static final RegistryObject<Block> potted_infected_fungus = POTTEDBLOCKS.register("potted_infected_fungus",
			() -> new FlowerPotBlock(null, BlockInit.infected_fungus, BlockBehaviour.Properties.of().noCollission()));

	public static final RegistryObject<Block> potted_stinkhorn_fungus = POTTEDBLOCKS.register("potted_stinkhorn_fungus",
			() -> new FlowerPotBlock(null, BlockInit.infected_fungus, BlockBehaviour.Properties.of().noCollission()));

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
	
	
	
	
	public static final RegistryObject<Block> crimson_flames = SPECIALBLOCKS.register("crimson_flames",
			() -> new CrimsonFlameBlock(BlockBehaviour.Properties.copy(Blocks.FIRE), 1.5f));

	// Idols
	public static final RegistryObject<Block> humane_idol = MODELEDBLOCKS.register("humane_idol",
			() -> new BlockHumaneIdol(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));
	public static final RegistryObject<Block> serpentine_idol = MODELEDBLOCKS.register("serpentine_idol",
			() -> new BlockSerpentineIdol(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));

	// Tiles
	public static final RegistryObject<Block> runic_chisel_station = MODELEDBLOCKS.register("runic_chisel_station",
			() -> new BlockChiselStation(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> morphling_incubator = MODELEDBLOCKS.register("morphling_incubator",
			() -> new MorphlingIncubatorBlock(
					BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> semi_sentient_construct = MODELEDBLOCKS
			.register("semi_sentient_construct", () -> new SemiSentientConstructBlock(
					BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> unstained_podium = MODELEDBLOCKS.register("unstained_podium",
			() -> new UnstainedPodiumBlock(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> scrying_podium = MODELEDBLOCKS.register("scrying_podium",
			() -> new ScryingPodiumBlock(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> fungal_podium = MODELEDBLOCKS.register("fungal_podium",
			() -> new FungalPodiumBlock(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));
	
	public static final RegistryObject<Block> fungal_implantation_pylon = MODELEDBLOCKS.register("fungal_implantation_pylon",
			() -> new FungalImplantationPylonBlock(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));
	
	
	public static final RegistryObject<Block> dendritic_distributor = MODELEDBLOCKS.register("dendritic_distributor",
			() -> new DendriticDistributorBlock(
					BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> mortal_display = MODELEDBLOCKS.register("mortal_display",
			() -> new MortalDisplayBlock(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> visceral_artificial_recaller = MODELEDBLOCKS
			.register("visceral_artificial_recaller", () -> new VisceralRecallerBlock(
					BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));
	public static final RegistryObject<Block> earthen_vein = MODELEDBLOCKS.register("earthen_vein",
			() -> new EarthenVeinBlock(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.STONE)));
	public static final RegistryObject<Block> iron_brazier = MODELEDBLOCKS.register("iron_brazier",
			() -> new BrazierBlock(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.METAL)));
	public static final RegistryObject<Block> juiceinator = MODELEDBLOCKS.register("juiceinator",
			() -> new JuiceinatorBlock(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.METAL)));

	public static final RegistryObject<Block> vial_centrifuge = MODELEDBLOCKS.register("vial_centrifuge",
			() -> new VialCentrifugeBlock(BlockBehaviour.Properties.of().strength(50f, 1500f).sound(SoundType.METAL)));

	public static final RegistryObject<Block> blood_crystal = MODELEDBLOCKS.register("blood_crystal",
			() -> new BloodCrystalBlock(
					BlockBehaviour.Properties.of().noOcclusion().strength(50f, 1500f).sound(SoundType.METAL)));

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

		Stream<RegistryObject<Block>> combinedStream = Stream
				.of(BASEBLOCKS.getEntries(), SLABBLOCKS.getEntries(), STAIRBLOCKS.getEntries(),
						COLUMNBLOCKS.getEntries(), CROSSBLOCKS.getEntries(), MODELEDBLOCKS.getEntries(),
						SPECIALBLOCKS.getEntries(), OBJBLOCKS.getEntries(), POTTEDBLOCKS.getEntries())
				.flatMap(Collection::stream);

		return combinedStream;
	}

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void registerBlocks(FMLClientSetupEvent event) {
		ItemBlockRenderTypes.setRenderLayer(BlockInit.smouldering_ash_trail.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.befouling_ash_trail.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.active_smouldering_ash_trail.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.active_befouling_ash_trail.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.earthen_vein.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.bleeding_heart.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.potted_bleeding_heart.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.potted_infected_fungus.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.potted_stinkhorn_fungus.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.iron_brazier.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.infected_fungus.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.stinkhorn_fungus.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.fungal_podium.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.hyphae.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.puffball_fungus.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.fungal_implantation_pylon.get(), RenderType.cutout());

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
		});
	}

}
