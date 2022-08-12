package com.vincenthuto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.block.ActiveBefoulingAshTrailBlock;
import com.vincenthuto.hemomancy.block.ActiveSmoulderingAshTrailBlock;
import com.vincenthuto.hemomancy.block.BefoulingAshTrailBlock;
import com.vincenthuto.hemomancy.block.BleedingHeartBlock;
import com.vincenthuto.hemomancy.block.BloodCrystalBlock;
import com.vincenthuto.hemomancy.block.BrazierBlock;
import com.vincenthuto.hemomancy.block.CrimsonFlameBlock;
import com.vincenthuto.hemomancy.block.DendriticDistributorBlock;
import com.vincenthuto.hemomancy.block.EarthenVeinBlock;
import com.vincenthuto.hemomancy.block.InfectedFungusBlock;
import com.vincenthuto.hemomancy.block.JuiceinatorBlock;
import com.vincenthuto.hemomancy.block.MorphlingIncubatorBlock;
import com.vincenthuto.hemomancy.block.MortalDisplayBlock;
import com.vincenthuto.hemomancy.block.ScryingPodiumBlock;
import com.vincenthuto.hemomancy.block.SemiSentientConstructBlock;
import com.vincenthuto.hemomancy.block.SmoulderingAshTrailBlock;
import com.vincenthuto.hemomancy.block.UnstainedPodiumBlock;
import com.vincenthuto.hemomancy.block.VisceralRecallerBlockS;
import com.vincenthuto.hemomancy.block.idol.BlockHumaneIdol;
import com.vincenthuto.hemomancy.block.idol.BlockSerpentineIdol;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.loading.FMLEnvironment;
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

	public static List<Block> getAllBlockEntries() {
		List<Block> blocks = new ArrayList<Block>();
		BASEBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		SLABBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		STAIRBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		COLUMNBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		CROSSBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		OBJBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		MODELEDBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		SPECIALBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		return blocks;
	}

	// Ash
	public static final RegistryObject<Block> smouldering_ash_trail = SPECIALBLOCKS.register("smouldering_ash_trail",
			() -> new SmoulderingAshTrailBlock(
					BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak()));
	public static final RegistryObject<Block> befouling_ash_trail = SPECIALBLOCKS.register("befouling_ash_trail",
			() -> new BefoulingAshTrailBlock(
					BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak()));

	public static final RegistryObject<Block> active_smouldering_ash_trail = SPECIALBLOCKS
			.register("active_smouldering_ash_trail", () -> new ActiveSmoulderingAshTrailBlock(
					BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak()));
	public static final RegistryObject<Block> active_befouling_ash_trail = SPECIALBLOCKS
			.register("active_befouling_ash_trail", () -> new ActiveBefoulingAshTrailBlock(
					BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak()));

	// Blocks
	public static final RegistryObject<Block> bleeding_heart = CROSSBLOCKS.register("bleeding_heart",
			() -> new BleedingHeartBlock(MobEffects.ABSORPTION, 12,
					BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
	public static final RegistryObject<Block> sanguine_glass = BASEBLOCKS.register("sanguine_glass",
			() -> new GlassBlock(BlockBehaviour.Properties.of(Material.GLASS).strength(0.1f, 1f).sound(SoundType.GLASS)
					.noOcclusion()));
	public static final RegistryObject<Block> sanguine_pane = SPECIALBLOCKS.register("sanguine_pane",
			() -> new IronBarsBlock(BlockBehaviour.Properties.of(Material.GLASS).strength(0.1f, 1f)
					.sound(SoundType.GLASS).noOcclusion()));
	public static final RegistryObject<Block> venous_stone = BASEBLOCKS.register("venous_stone",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> venous_stone_slab = SLABBLOCKS.register("venous_stone_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final RegistryObject<Block> venous_stone_stairs = BASEBLOCKS.register(("venous_stone_stairs"),
			() -> new StairBlock(() -> venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(venous_stone.get())));
	public static final RegistryObject<Block> gilded_venous_stone = BASEBLOCKS.register("gilded_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone = BASEBLOCKS.register("polished_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_slab = SLABBLOCKS
			.register("polished_venous_stone_slab", () -> new SlabBlock(BlockBehaviour.Properties
					.of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_stairs = STAIRBLOCKS.register(
			("polished_venous_stone_stairs"), () -> new StairBlock(() -> venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(venous_stone.get())));
	public static final RegistryObject<Block> chiseled_polished_venous_stone = BASEBLOCKS
			.register("chiseled_polished_venous_stone", () -> new Block(BlockBehaviour.Properties
					.of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_bricks = STAIRBLOCKS
			.register("polished_venous_stone_bricks", () -> new Block(BlockBehaviour.Properties
					.of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_brick_slab = SLABBLOCKS
			.register("polished_venous_stone_brick_slab", () -> new SlabBlock(BlockBehaviour.Properties
					.of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(1.5f, 6.0F)));
	public static final RegistryObject<Block> polished_venous_stone_brick_stairs = STAIRBLOCKS.register(
			("polished_venous_stone_brick_stairs"), () -> new StairBlock(() -> venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(venous_stone.get())));
	public static final RegistryObject<Block> cracked_polished_venous_stone_bricks = BASEBLOCKS
			.register("cracked_polished_venous_stone_bricks", () -> new Block(BlockBehaviour.Properties
					.of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> infested_venous_stone = BASEBLOCKS.register("infested_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
	public static final RegistryObject<Block> conscious_mass = BASEBLOCKS.register("conscious_mass",
			() -> new Block(BlockBehaviour.Properties.of(Material.DECORATION, MaterialColor.COLOR_PINK)
					.sound(SoundType.WART_BLOCK).strength(0.2F, 1.0F)));
	public static final RegistryObject<Block> hematic_iron_block = BASEBLOCKS.register("hematic_iron_block",
			() -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
					.requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> hematic_iron_pillar = COLUMNBLOCKS.register("hematic_iron_pillar",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
					.requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> chiseled_hematic_iron_block = BASEBLOCKS.register(
			"chiseled_hematic_iron_block",
			() -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
					.requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)));

	public static final RegistryObject<Block> blood_wood_log = COLUMNBLOCKS.register("blood_wood_log",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
					.strength(2.0F, 3.0F).sound(SoundType.WOOD)));

	public static final RegistryObject<Block> blood_wood_planks = BASEBLOCKS.register("blood_wood_planks",
			() -> new Block(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F, 3.0F)
					.sound(SoundType.WOOD)));

	public static final RegistryObject<Block> infected_fungus = CROSSBLOCKS.register("infected_fungus",
			() -> new InfectedFungusBlock(MobEffects.CONFUSION, 12,
					BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
	public static final RegistryObject<Block> infected_stem = BASEBLOCKS.register("infected_stem",
			() -> new Block(BlockBehaviour.Properties.of(Material.PLANT).strength(0.5f, 15f).sound(SoundType.GRASS)));
	public static final RegistryObject<Block> infected_cap = BASEBLOCKS.register("infected_cap",
			() -> new Block(BlockBehaviour.Properties.of(Material.PLANT).strength(0.5f, 15f).sound(SoundType.GRASS)));
	public static final RegistryObject<Block> crimson_flames = MODELEDBLOCKS.register("crimson_flames",
			() -> new CrimsonFlameBlock(BlockBehaviour.Properties.copy(Blocks.FIRE), 1.5f));

	// Idols
	public static final RegistryObject<Block> humane_idol = MODELEDBLOCKS.register("humane_idol",
			() -> new BlockHumaneIdol(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> serpentine_idol = MODELEDBLOCKS.register("serpentine_idol",
			() -> new BlockSerpentineIdol(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)));

	// Tiles
	public static final RegistryObject<Block> morphling_incubator = MODELEDBLOCKS.register("morphling_incubator",
			() -> new MorphlingIncubatorBlock(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> semi_sentient_construct = MODELEDBLOCKS
			.register("semi_sentient_construct", () -> new SemiSentientConstructBlock(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)));
	public static final RegistryObject<Block> unstained_podium = MODELEDBLOCKS.register("unstained_podium",
			() -> new UnstainedPodiumBlock(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> scrying_podium = MODELEDBLOCKS.register("scrying_podium",
			() -> new ScryingPodiumBlock(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> dendritic_distributor = MODELEDBLOCKS.register("dendritic_distributor",
			() -> new DendriticDistributorBlock(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)));
	public static final RegistryObject<Block> mortal_display = MODELEDBLOCKS.register("mortal_display",
			() -> new MortalDisplayBlock(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)));
	public static final RegistryObject<Block> visceral_artificial_recaller = MODELEDBLOCKS
			.register("visceral_artificial_recaller", () -> new VisceralRecallerBlockS(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)));
	public static final RegistryObject<Block> earthen_vein = MODELEDBLOCKS.register("earthen_vein",
			() -> new EarthenVeinBlock(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> iron_brazier = MODELEDBLOCKS.register("iron_brazier",
			() -> new BrazierBlock(
					BlockBehaviour.Properties.of(Material.METAL).strength(50f, 1500f).sound(SoundType.METAL)));

	public static final RegistryObject<Block> juiceinator = MODELEDBLOCKS.register("juiceinator",
			() -> new JuiceinatorBlock(
					BlockBehaviour.Properties.of(Material.METAL).strength(50f, 1500f).sound(SoundType.METAL)));

	public static final RegistryObject<Block> blood_crystal = MODELEDBLOCKS.register("blood_crystal",
			() -> new BloodCrystalBlock(BlockBehaviour.Properties.of(Material.METAL).noOcclusion().strength(50f, 1500f)
					.sound(SoundType.METAL)));

	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ItemBlockRenderTypes.setRenderLayer(BlockInit.blood_crystal.get(), RenderType.translucent());

			ItemBlockRenderTypes.setRenderLayer(BlockInit.sanguine_glass.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.sanguine_pane.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.smouldering_ash_trail.get(), RenderType.cutoutMipped());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.befouling_ash_trail.get(), RenderType.cutoutMipped());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.active_smouldering_ash_trail.get(),
					RenderType.cutoutMipped());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.active_befouling_ash_trail.get(), RenderType.cutoutMipped());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.scrying_podium.get(), RenderType.cutoutMipped());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.semi_sentient_construct.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.morphling_incubator.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.crimson_flames.get(), RenderType.cutoutMipped());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.bleeding_heart.get(), RenderType.cutoutMipped());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.infected_fungus.get(), RenderType.cutoutMipped());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.visceral_artificial_recaller.get(),
					RenderType.cutoutMipped());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.earthen_vein.get(), RenderType.cutoutMipped());
			ItemBlockRenderTypes.setRenderLayer(BlockInit.iron_brazier.get(), RenderType.cutoutMipped());

		}
	}

}
