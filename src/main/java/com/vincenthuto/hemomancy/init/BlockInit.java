package com.vincenthuto.hemomancy.init;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.vincenthuto.hemomancy.block.BlockActiveBefoulingAshTrail;
import com.vincenthuto.hemomancy.block.BlockActiveSmoulderingAshTrail;
import com.vincenthuto.hemomancy.block.BlockBefoulingAshTrail;
import com.vincenthuto.hemomancy.block.BlockBleedingHeart;
import com.vincenthuto.hemomancy.block.BlockBrazier;
import com.vincenthuto.hemomancy.block.BlockChiselStation;
import com.vincenthuto.hemomancy.block.BlockCrimsonFlame;
import com.vincenthuto.hemomancy.block.BlockDendriticDistributor;
import com.vincenthuto.hemomancy.block.BlockEarthenVein;
import com.vincenthuto.hemomancy.block.BlockEarthlyTransfuser;
import com.vincenthuto.hemomancy.block.BlockJuiceinator;
import com.vincenthuto.hemomancy.block.BlockMorphlingIncubator;
import com.vincenthuto.hemomancy.block.BlockMortalDisplay;
import com.vincenthuto.hemomancy.block.BlockRuneModStation;
import com.vincenthuto.hemomancy.block.BlockScryingPodium;
import com.vincenthuto.hemomancy.block.BlockSemiSentientConstruct;
import com.vincenthuto.hemomancy.block.BlockSmoulderingAshTrail;
import com.vincenthuto.hemomancy.block.BlockUnstainedPodium;
import com.vincenthuto.hemomancy.block.BlockVisceralRecaller;
import com.vincenthuto.hemomancy.block.idol.BlockHumaneIdol;
import com.vincenthuto.hemomancy.block.idol.BlockSerpentineIdol;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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

	public static final DeferredRegister<Item> BLOCKITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Hemomancy.MOD_ID);

	// Ash
	public static final RegistryObject<Block> smouldering_ash_trail = registerBlockItems("smouldering_ash_trail",
			() -> new BlockSmoulderingAshTrail(
					BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak()),
			new Item.Properties().tab(HemomancyItemGroup.instance), SPECIALBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> befouling_ash_trail = registerBlockItems("befouling_ash_trail",
			() -> new BlockBefoulingAshTrail(
					BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak()),
			new Item.Properties().tab(HemomancyItemGroup.instance), SPECIALBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> active_smouldering_ash_trail = registerBlockItems(
			"active_smouldering_ash_trail",
			() -> new BlockActiveSmoulderingAshTrail(
					BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak()),
			new Item.Properties().tab(HemomancyItemGroup.instance), SPECIALBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> active_befouling_ash_trail = registerBlockItems(
			"active_befouling_ash_trail",
			() -> new BlockActiveBefoulingAshTrail(
					BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak()),
			new Item.Properties().tab(HemomancyItemGroup.instance), SPECIALBLOCKS, BLOCKITEMS);

	// Blocks
	public static final RegistryObject<Block> bleeding_heart = registerBlockItems("bleeding_heart",
			() -> new BlockBleedingHeart(MobEffects.ABSORPTION, 12,
					BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS)),
			new Item.Properties().tab(HemomancyItemGroup.instance), CROSSBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> sanguine_glass = registerBlockItems(
			"sanguine_glass", () -> new GlassBlock(BlockBehaviour.Properties.of(Material.GLASS).strength(0.1f, 1f)
					.sound(SoundType.GLASS).noOcclusion()),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> sanguine_pane = registerBlockItems("sanguine_pane",
			() -> new IronBarsBlock(BlockBehaviour.Properties.of(Material.GLASS).strength(0.1f, 1f)
					.sound(SoundType.GLASS).noOcclusion()),
			new Item.Properties().tab(HemomancyItemGroup.instance), SPECIALBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> venous_stone = registerBlockItems("venous_stone",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> venous_stone_slab = registerBlockItems("venous_stone_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5f, 6.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), SLABBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> venous_stone_stairs = registerBlockItems(("venous_stone_stairs"),
			() -> new StairBlock(() -> venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(venous_stone.get())),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> gilded_venous_stone = registerBlockItems("gilded_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> polished_venous_stone = registerBlockItems("polished_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> polished_venous_stone_slab = registerBlockItems(
			"polished_venous_stone_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5f, 6.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), SLABBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> polished_venous_stone_stairs = registerBlockItems(
			("polished_venous_stone_stairs"),
			() -> new StairBlock(() -> venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(venous_stone.get())),
			new Item.Properties().tab(HemomancyItemGroup.instance), STAIRBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> chiseled_polished_venous_stone = registerBlockItems(
			"chiseled_polished_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> polished_venous_stone_bricks = registerBlockItems(
			"polished_venous_stone_bricks",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), STAIRBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> polished_venous_stone_brick_slab = registerBlockItems(
			"polished_venous_stone_brick_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5f, 6.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), SLABBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> polished_venous_stone_brick_stairs = registerBlockItems(
			("polished_venous_stone_brick_stairs"),
			() -> new StairBlock(() -> venous_stone.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(venous_stone.get())),
			new Item.Properties().tab(HemomancyItemGroup.instance), STAIRBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> cracked_polished_venous_stone_bricks = registerBlockItems(
			"cracked_polished_venous_stone_bricks",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> infested_venous_stone = registerBlockItems("infested_venous_stone",
			() -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
					.requiresCorrectToolForDrops().strength(1.5F, 6.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> conscious_mass = registerBlockItems("conscious_mass",
			() -> new Block(BlockBehaviour.Properties.of(Material.DECORATION, MaterialColor.COLOR_PINK)
					.sound(SoundType.WART_BLOCK).strength(0.2F, 1.0F)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> hematic_iron_block = registerBlockItems("hematic_iron_block",
			() -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
					.requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> hematic_iron_pillar = registerBlockItems("hematic_iron_pillar",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
					.requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)),
			new Item.Properties().tab(HemomancyItemGroup.instance), COLUMNBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> chiseled_hematic_iron_block = registerBlockItems(
			"chiseled_hematic_iron_block",
			() -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
					.requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> blood_wood_log = registerBlockItems("blood_wood_log",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
					.strength(2.0F, 3.0F).sound(SoundType.WOOD)),
			new Item.Properties().tab(HemomancyItemGroup.instance), COLUMNBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> blood_wood_planks = registerBlockItems("blood_wood_planks",
			() -> new Block(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F, 3.0F)
					.sound(SoundType.WOOD)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> infected_stem = registerBlockItems("infected_stem",
			() -> new Block(BlockBehaviour.Properties.of(Material.PLANT).strength(0.5f, 15f).sound(SoundType.GRASS)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);
	public static final RegistryObject<Block> infected_cap = registerBlockItems("infected_cap",
			() -> new Block(BlockBehaviour.Properties.of(Material.PLANT).strength(0.5f, 15f).sound(SoundType.GRASS)),
			new Item.Properties().tab(HemomancyItemGroup.instance), BASEBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> crimson_flames = registerBlockItems("crimson_flames",
			() -> new BlockCrimsonFlame(BlockBehaviour.Properties.copy(Blocks.FIRE), 1.5f),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	// Idols
	public static final RegistryObject<Block> humane_idol = registerBlockItems("humane_idol",
			() -> new BlockHumaneIdol(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> serpentine_idol = registerBlockItems("serpentine_idol",
			() -> new BlockSerpentineIdol(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	// Tiles
	public static final RegistryObject<Block> runic_chisel_station = registerBlockItems("runic_chisel_station",
			() -> new BlockChiselStation(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> morphling_incubator = registerBlockItems("morphling_incubator",
			() -> new BlockMorphlingIncubator(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> semi_sentient_construct = registerBlockItems("semi_sentient_construct",
			() -> new BlockSemiSentientConstruct(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> unstained_podium = registerBlockItems("unstained_podium",
			() -> new BlockUnstainedPodium(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> scrying_podium = registerBlockItems("scrying_podium",
			() -> new BlockScryingPodium(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> rune_mod_station = registerBlockItems("rune_mod_station",
			() -> new BlockRuneModStation(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> dendritic_distributor = registerBlockItems("dendritic_distributor",
			() -> new BlockDendriticDistributor(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> mortal_display = registerBlockItems("mortal_display",
			() -> new BlockMortalDisplay(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> visceral_artificial_recaller = registerBlockItems(
			"visceral_artificial_recaller",
			() -> new BlockVisceralRecaller(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> earthen_vein = registerBlockItems("earthen_vein",
			() -> new BlockEarthenVein(
					BlockBehaviour.Properties.of(Material.STONE).strength(50f, 1500f).sound(SoundType.STONE)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> iron_brazier = registerBlockItems("iron_brazier",
			() -> new BlockBrazier(
					BlockBehaviour.Properties.of(Material.METAL).strength(50f, 1500f).sound(SoundType.METAL)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> juiceinator = registerBlockItems("juiceinator",
			() -> new BlockJuiceinator(
					BlockBehaviour.Properties.of(Material.METAL).strength(50f, 1500f).sound(SoundType.METAL)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static final RegistryObject<Block> earthly_transfuser = registerBlockItems("earthly_transfuser",
			() -> new BlockEarthlyTransfuser(
					BlockBehaviour.Properties.of(Material.METAL).strength(50f, 1500f).sound(SoundType.METAL)),
			new Item.Properties().tab(HemomancyItemGroup.instance), MODELEDBLOCKS, BLOCKITEMS);

	public static RegistryObject<Block> registerBlockItems(String name, final Supplier<? extends Block> blockSup,
			Item.Properties itemProps, DeferredRegister<Block> blockReg, DeferredRegister<Item> itemReg) {
		RegistryObject<Block> regBlock = blockReg.register(name, blockSup);
		itemReg.register(name, () -> new BlockItem(regBlock.get(), itemProps));
		return regBlock;

	}

	@SubscribeEvent
	public static void registerBlocks(FMLClientSetupEvent event) {
		ItemBlockRenderTypes.setRenderLayer(BlockInit.sanguine_glass.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.sanguine_pane.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.smouldering_ash_trail.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.befouling_ash_trail.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.active_smouldering_ash_trail.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.active_befouling_ash_trail.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.rune_mod_station.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.scrying_podium.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.semi_sentient_construct.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.morphling_incubator.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.crimson_flames.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.bleeding_heart.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.visceral_artificial_recaller.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.earthen_vein.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.iron_brazier.get(), RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(BlockInit.earthly_transfuser.get(), RenderType.cutoutMipped());

	}

}
