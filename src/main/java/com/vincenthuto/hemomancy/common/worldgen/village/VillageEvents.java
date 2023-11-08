package com.vincenthuto.hemomancy.common.worldgen.village;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.VillagerInit;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.FORGE)

public class VillageEvents {

	// Trades

	@SubscribeEvent
	public static void registerTrades(VillagerTradesEvent event) {
		if (event.getType() != VillagerInit.HEMOPOTHECARY.get())
			return;

		Int2ObjectMap<List<VillagerTrades.ItemListing>> trademap = event.getTrades();

		trademap.get(1).addAll(Arrays.asList(sellItem(ItemInit.befouling_ash.get(), 1, 1, 10, 4)));
		trademap.get(1).addAll(Arrays.asList(sellItem(ItemInit.morphling_fungal.get(), 1, 1, 10, 4)));
		trademap.get(1).addAll(Arrays.asList(sellItem(ItemInit.recycled_enzyme.get(), 1, 1, 10, 4)));
		trademap.get(1).addAll(Arrays.asList(sellItem(ItemInit.bloody_flask.get(), 1, 1, 10, 4)));

		trademap.get(2).addAll(Arrays.asList(sellItem(ItemInit.fervent_husk.get(), 2, 1, 10, 4)));
		trademap.get(3).addAll(Arrays.asList(sellItem(ItemInit.barbed_helm.get(), 3, 1, 10, 4)));
		trademap.get(4).addAll(Arrays.asList(sellItem(ItemInit.foul_paste.get(), 4, 1, 10, 4)));
		trademap.get(5).addAll(Arrays.asList(sellItem(ItemInit.heart_pattern.get(), 5, 1, 10, 4)));

	}

	private static VillagerTrades.ItemListing sellItem(ItemLike thing, int price, int maxTrades, int xp,
			float priceMultiplier) {
		return sellItem(new ItemStack(thing), price, maxTrades, xp, priceMultiplier);
	}

	private static VillagerTrades.ItemListing sellItem(ItemStack thing, int price, int maxTrades, int xp,
			float priceMultiplier) {
		return new BasicItemListing(new ItemStack(Items.EMERALD, price), thing, maxTrades, xp, priceMultiplier);
	}

	private VillagerTrades.ItemListing buyItem(ItemStack thing, int reward, int maxTrades, int xp,
			float priceMultiplier) {
		return new BasicItemListing(thing, new ItemStack(Items.EMERALD, reward), maxTrades, xp, priceMultiplier);
	}

	private static class SellRandomFromTag implements VillagerTrades.ItemListing {
		private final TagKey<Item> tagSource;
		private final int quantity;
		private final int price;
		private final int maxUses;
		private final int xp;
		private final float priceMultiplier;

		private SellRandomFromTag(TagKey<Item> tagSource, int quantity, int price, int maxUses, int xp,
				float priceMultiplier) {
			this.tagSource = tagSource;
			this.quantity = quantity;
			this.price = price;
			this.maxUses = maxUses;
			this.xp = xp;
			this.priceMultiplier = priceMultiplier;
		}

		@org.jetbrains.annotations.Nullable
		@Override
		public MerchantOffer getOffer(Entity p_219693_, RandomSource rand) {
			return Optional.ofNullable(ForgeRegistries.ITEMS.tags()).map(tags -> tags.getTag(tagSource))
					.flatMap(tag -> tag.getRandomElement(rand))
					.map(itemHolder -> new MerchantOffer(new ItemStack(Items.EMERALD, price),
							new ItemStack(itemHolder, quantity), this.maxUses, this.xp, this.priceMultiplier))
					.orElse(null);
		}
	}

	// Structures
	@SubscribeEvent
	public static void addBuildingToVillages(final ServerAboutToStartEvent event) {
		Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess()
				.registry(Registries.TEMPLATE_POOL).orElseThrow();
		Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess()
				.registry(Registries.PROCESSOR_LIST).orElseThrow();
		addBuildingToPool(templatePoolRegistry, processorListRegistry,
				new ResourceLocation("minecraft:village/plains/houses"), "hemomancy:plains_hemopothecary", 10);
		addBuildingToPool(templatePoolRegistry, processorListRegistry,
				new ResourceLocation("minecraft:village/snowy/houses"), "hemomancy:snowy_hemopothecary", 10);
		addBuildingToPool(templatePoolRegistry, processorListRegistry,
				new ResourceLocation("minecraft:village/savanna/houses"), "hemomancy:savanna_hemopothecary", 10);
		addBuildingToPool(templatePoolRegistry, processorListRegistry,
				new ResourceLocation("minecraft:village/taiga/houses"), "hemomancy:taiga_hemopothecary", 10);
		addBuildingToPool(templatePoolRegistry, processorListRegistry,
				new ResourceLocation("minecraft:village/desert/houses"), "hemomancy:desert_hemopothecary", 10);
	}

	private static final ResourceKey<StructureProcessorList> TAILOR_SHOP_PROCESSOR_LIST_KEY = ResourceKey
			.create(Registries.PROCESSOR_LIST, Hemomancy.rloc("hemopothecary_processors"));

	private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
			Registry<StructureProcessorList> processorListRegistry, ResourceLocation poolRL, String nbtPieceRL,
			int weight) {
		Holder<StructureProcessorList> emptyProcessorList = processorListRegistry
				.getHolderOrThrow(TAILOR_SHOP_PROCESSOR_LIST_KEY);

		StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
		if (pool == null)
			return;

		SinglePoolElement piece = SinglePoolElement.single(nbtPieceRL, emptyProcessorList)
				.apply(StructureTemplatePool.Projection.RIGID);

		for (int i = 0; i < weight; i++) {
			pool.templates.add(piece);
		}

// for completeness
		List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
		listOfPieceEntries.add(new Pair<>(piece, weight));
		pool.rawTemplates = listOfPieceEntries;
	}



}
