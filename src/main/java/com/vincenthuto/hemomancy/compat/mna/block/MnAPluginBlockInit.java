package com.vincenthuto.hemomancy.compat.mna.block;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.*;
import com.vincenthuto.hemomancy.common.block.idol.BlockHumaneIdol;
import com.vincenthuto.hemomancy.common.block.idol.BlockSerpentineIdol;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class MnAPluginBlockInit {
	public static final DeferredRegister<Block> MNABLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			Hemomancy.MOD_ID);


	public static final DeferredHolder<Block, Block> broken_mana_trapazahedron = MNABLOCKS.register("broken_mana_trapazahedron", BrokenManaTrapazahedronBlock::new);

	public static List<Block> getAllBlockEntries() {
		List<Block> blocks = new ArrayList<>();
		MNABLOCKS.getEntries().stream().map(e -> e.get()).forEach(blocks::add);

		return blocks;
	}

	public static Stream<DeferredHolder<Block, Block>> getAllBlockEntriesAsStream() {

		Stream<DeferredHolder<Block, Block>> combinedStream = Stream.of(MNABLOCKS.getEntries()).flatMap(Collection::stream);

		return combinedStream;
	}

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void registerBlocks(FMLClientSetupEvent event) {
		ItemBlockRenderTypes.setRenderLayer(MnAPluginBlockInit.broken_mana_trapazahedron.get(), RenderType.translucent());
	}

	public static void buildMnaCompatBlockContents(BuildCreativeModeTabContentsEvent populator) {
		if (populator.getTabKey() == Hemomancy.hemomancytab.getKey()) {
			// Items
			MNABLOCKS.getEntries().forEach(i -> populator.accept(i.get()));
			var b = getAllBlockEntriesAsStream();
			b.forEach(item -> {
					populator.accept(item.get());
			});
		}
	}

	public static void onRegisterItems(final RegisterEvent event) {
		if (event.getRegistryKey() != ForgeRegistries.Keys.ITEMS) {
			return;
		}
		var b = getAllBlockEntriesAsStream().map(m -> new Pair<>(m.get(), m.getId()))
				.map(t -> Pair.of(t.getSecond(), new BlockItem(t.getFirst(), new Item.Properties())));
		b.forEach(item -> {
				registerBlockItem(event, item);
		});

	}
	private static void registerBlockItem(RegisterEvent event, Pair<ResourceLocation, BlockItem> item) {
		event.register(ForgeRegistries.Keys.ITEMS, helper -> helper.register(item.getFirst(), item.getSecond()));
	}
}
