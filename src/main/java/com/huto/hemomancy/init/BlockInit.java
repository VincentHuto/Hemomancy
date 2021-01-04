package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.block.BlockChiselStation;
import com.huto.hemomancy.block.BlockRuneModStation;

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
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Block> BASEBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);

	public static final RegistryObject<Block> sanguine_glass = BLOCKS.register("sanguine_glass", () -> new GlassBlock(
			Block.Properties.create(Material.GLASS).hardnessAndResistance(0.1f, 1f).sound(SoundType.GLASS).notSolid()));
	public static final RegistryObject<Block> sanguine_pane = BLOCKS.register("sanguine_pane", () -> new PaneBlock(
			Block.Properties.create(Material.GLASS).hardnessAndResistance(0.1f, 1f).sound(SoundType.GLASS).notSolid()));

	public static final RegistryObject<Block> venous_stone = BLOCKS.register("venous_stone",
			() -> new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE).setRequiresTool()
					.hardnessAndResistance(1.5F, 6.0F)));

	public static final RegistryObject<Block> tainted_iron_block = BLOCKS.register("tainted_iron_block",
			() -> new Block(AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON).setRequiresTool()
					.hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));

	public static final RegistryObject<Block> chiseled_tainted_iron_block = BLOCKS.register(
			"chiseled_tainted_iron_block",
			() -> new Block(AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON).setRequiresTool()
					.hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));

	public static final RegistryObject<Block> runic_chisel_station = BLOCKS.register("runic_chisel_station",
			() -> new BlockChiselStation(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));

	public static final RegistryObject<Block> rune_mod_station = BLOCKS.register("rune_mod_station",
			() -> new BlockRuneModStation(
					Block.Properties.create(Material.ROCK).hardnessAndResistance(50f, 1500f).sound(SoundType.STONE)));

	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			RenderTypeLookup.setRenderLayer(BlockInit.sanguine_glass.get(), RenderType.getTranslucent());
			RenderTypeLookup.setRenderLayer(BlockInit.sanguine_pane.get(), RenderType.getTranslucent());

		}
	}

}
