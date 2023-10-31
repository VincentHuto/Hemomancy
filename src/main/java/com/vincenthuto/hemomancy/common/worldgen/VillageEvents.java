package com.vincenthuto.hemomancy.common.worldgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.registry.ItemInit;
import com.vincenthuto.hemomancy.common.registry.VillagerInit;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.FORGE)

public class VillageEvents {
	
	//Trades
	
	
	
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
	
	
	
	
	
	
	
	
	
	
	
	
	//Structures

	@SubscribeEvent
	public static void onTagsUpdated(ServerAboutToStartEvent ev) {

		// Register engineer's houses for each biome
		for (String biome : new String[] { "plains", "snowy", "savanna", "desert", "taiga" })
			for (String type : new String[] { "hemopothecary"})
				addToPool(new ResourceLocation("village/" + biome + "/houses"),
						Hemomancy.rloc("village/houses/" + biome + "_" + type), ev.getServer().registryAccess());
	}

	public static final ResourceLocation HEMAPOTHECARY = Hemomancy.rloc("hemapothecary");

	
	private static void addToPool(ResourceLocation poolId, ResourceLocation toAdd, RegistryAccess regAccess) {

		Registry<StructureTemplatePool> templatePoolRegistry = regAccess.registry(Registries.TEMPLATE_POOL)
				.orElseThrow();
		;
		StructureTemplatePool poolAccess = templatePoolRegistry.get(poolId);
		if (!(poolAccess.rawTemplates instanceof ArrayList))
			poolAccess.rawTemplates = new ArrayList<>(poolAccess.rawTemplates);

		SinglePoolElement addedElement = SinglePoolElement.single(toAdd.toString()).apply(Projection.RIGID);
		poolAccess.rawTemplates.add(Pair.of(addedElement,5));
		poolAccess.templates.add(addedElement);
	}

	private static class TradeListing implements ItemListing {
		private final TradeOutline outline;
		private final LazyItemStack lazyItem;
		private final PriceInterval priceInfo;
		private final int maxUses;
		private final int xp;
		private float priceMultiplier = 0.05f;

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull Function<Level, ItemStack> item,
				@Nonnull PriceInterval priceInfo, int maxUses, int xp) {
			this.outline = outline;
			this.lazyItem = new LazyItemStack(item);
			this.priceInfo = priceInfo;
			this.maxUses = maxUses;
			this.xp = xp;
		}

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull ItemStack itemStack,
				@Nonnull PriceInterval buyAmounts, int maxUses, int xp) {
			this(outline, l -> itemStack, buyAmounts, maxUses, xp);
		}

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull ItemLike item, @Nonnull PriceInterval buyAmounts,
				int maxUses, int xp) {
			this(outline, new ItemStack(item), buyAmounts, maxUses, xp);
		}

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull TagKey<Item> tag, @Nonnull PriceInterval buyAmounts,
				int maxUses, int xp) {
			this(outline, l -> l != null ? getPreferredTagStack(l.registryAccess(), tag) : ItemStack.EMPTY,
					buyAmounts, maxUses, xp);
		}
		private static final Map<TagKey<Item>, ItemStack> oreOutputPreference = new HashMap<>();


		public static ItemStack getPreferredTagStack(RegistryAccess tags, TagKey<Item> tag)
		{
			// TODO caching should not be global, tags can change!
			return oreOutputPreference.computeIfAbsent(
					tag, rl -> getPreferredElementbyMod(
							elementStream(tags, rl), tags.registryOrThrow(Registries.ITEM)
					).orElse(Items.AIR).getDefaultInstance()
			).copy();
		}
		public static <T> Optional<T> getPreferredElementbyMod(Stream<T> list, Registry<T> registry)
		{
			return getPreferredElementbyMod(list, registry::getKey);
		}

		public static <T> Optional<T> getPreferredElementbyMod(Stream<T> list, Function<T, ResourceLocation> getName)
		{
			return list.min(
				Comparator.<T>comparingInt(t -> {
					ResourceLocation name = getName.apply(t);
					String modId = name.getNamespace();
					int idx = modPreference.indexOf(modId);
					if(idx < 0)
						return modPreference.size();
					else
						return idx;
				}).thenComparing(getName)
			);
		}
		public static List<? extends String> modPreference;


		public static <T> Stream<T> elementStream(RegistryAccess tags, ResourceKey<Registry<T>> registry, ResourceLocation tag) {
			return holderStream(tags, registry, tag).map(Holder::value);
		}

		public static <T> Stream<T> elementStream(RegistryAccess tags, TagKey<T> key) {
			return holderStream(tags.registryOrThrow(key.registry()), key).map(Holder::value);
		}

		public static <T> Stream<T> elementStream(Registry<T> registry, TagKey<T> tag) {
			return holderStream(registry, tag).map(Holder::value);
		}
		public static <T> Stream<Holder<T>> holderStream(RegistryAccess tags, ResourceKey<Registry<T>> registry, ResourceLocation tag) {
			return holderStream(tags.registryOrThrow(registry), TagKey.create(registry, tag));
		}

		public static <T> Stream<Holder<T>> holderStream(Registry<T> registry, TagKey<T> tag) {
			return StreamSupport.stream(registry.getTagOrEmpty(tag).spliterator(), false);
		}

		public TradeListing setMultiplier(float priceMultiplier) {
			this.priceMultiplier = priceMultiplier;
			return this;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(@Nullable Entity trader, @Nonnull RandomSource rand) {
			ItemStack buying = this.lazyItem.apply(trader != null ? trader.level() : null);
			return this.outline.generateOffer(buying, priceInfo, rand, maxUses, xp, priceMultiplier);
		}
	}

	/**
	 * Lazy-loaded ItemStack to support tag-based trades
	 */
	private static class LazyItemStack implements Function<Level, ItemStack> {
		private final Function<Level, ItemStack> function;
		private ItemStack instance;

		private LazyItemStack(Function<Level, ItemStack> function) {
			this.function = function;
		}

		@Override
		public ItemStack apply(Level level) {
			if (instance == null)
				instance = function.apply(level);
			return instance;
		}
	}

	/**
	 * Functional interface to create constant implementations from
	 */
	@FunctionalInterface
	private interface TradeOutline {
		MerchantOffer generateOffer(ItemStack item, PriceInterval priceInfo, RandomSource random, int maxUses, int xp,
				float priceMultiplier);
	}

	private static final TradeOutline EMERALD_FOR_ITEM = (buying, priceInfo, random, maxUses, xp,
			priceMultiplier) -> new MerchantOffer(
					ItemHandlerHelper.copyStackWithSize(buying, priceInfo.getPrice(random)),
					new ItemStack(Items.EMERALD), maxUses, xp, priceMultiplier);
	private static final TradeOutline ONE_ITEM_FOR_EMERALDS = (selling, priceInfo, random, maxUses, xp,
			priceMultiplier) -> new MerchantOffer(new ItemStack(Items.EMERALD, priceInfo.getPrice(random)), selling,
					maxUses, xp, priceMultiplier);
	private static final TradeOutline ITEMS_FOR_ONE_EMERALD = (selling, priceInfo, random, maxUses, xp,
			priceMultiplier) -> new MerchantOffer(new ItemStack(Items.EMERALD),
					ItemHandlerHelper.copyStackWithSize(selling, priceInfo.getPrice(random)), maxUses, xp,
					priceMultiplier);

	private record PriceInterval(int min, int max) {
		int getPrice(RandomSource rand) {
			return min >= max ? min : min + rand.nextInt(max - min + 1);
		}
	}

}
