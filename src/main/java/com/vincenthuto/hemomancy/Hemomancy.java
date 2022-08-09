package com.vincenthuto.hemomancy;

import java.util.OptionalInt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vincenthuto.hemomancy.capa.block.vein.EarthenVeinLocEvents;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.capa.player.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.capa.player.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.capa.volume.RenderBloodLaserEvent;
import com.vincenthuto.hemomancy.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.entity.WorldGenEvents;
import com.vincenthuto.hemomancy.event.KeyBindEvents;
import com.vincenthuto.hemomancy.event.MorphlingJarEvents;
import com.vincenthuto.hemomancy.gui.guide.HemoLib;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ContainerInit;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.init.FluidInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.init.ManipulationInit;
import com.vincenthuto.hemomancy.init.ParticleInit;
import com.vincenthuto.hemomancy.init.PotionInit;
import com.vincenthuto.hemomancy.init.RecipeInit;
import com.vincenthuto.hemomancy.init.SkillPointInit;
import com.vincenthuto.hemomancy.init.StructureInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.recipe.BloodCraftingRecipes;
import com.vincenthuto.hemomancy.recipe.PolypRecipes;

import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.DarkOakFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

@Mod("hemomancy")
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)

public class Hemomancy {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "hemomancy";
	public static Hemomancy instance;
	public static IProxy proxy = new IProxy() {
	};
	public static boolean forcesLoaded = false;

//	public static TagKey<ConfiguredStructureFeature<?, ?>> blood_temple_located = TagKey.create(
//	Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY,
//	new ResourceLocation(Hemomancy.MOD_ID, "blood_temple_located"));
	@SuppressWarnings("deprecation")
	public Hemomancy() {
		DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> proxy = new ClientProxy());
		proxy.registerHandlers();
		forcesLoaded = ModList.get().isLoaded("forcesofreality");
		instance = this;
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		ManipulationInit.MANIPS.register(modEventBus);
		ParticleInit.PARTICLE_TYPES.register(modEventBus);
		PotionInit.EFFECTS.register(modEventBus);
		PotionInit.POTION_TYPES.register(modEventBus);
		ItemInit.BASEITEMS.register(modEventBus);
		ItemInit.HANDHELDITEMS.register(modEventBus);
		ItemInit.SPECIALITEMS.register(modEventBus);
		ItemInit.SPAWNEGGS.register(modEventBus);
		BlockInit.BASEBLOCKS.register(modEventBus);
		BlockInit.SLABBLOCKS.register(modEventBus);
		BlockInit.STAIRBLOCKS.register(modEventBus);
		BlockInit.COLUMNBLOCKS.register(modEventBus);
		BlockInit.CROSSBLOCKS.register(modEventBus);
		BlockInit.OBJBLOCKS.register(modEventBus);
		BlockInit.SPECIALBLOCKS.register(modEventBus);
		BlockInit.MODELEDBLOCKS.register(modEventBus);
		FluidInit.FLUIDS.register(modEventBus);
		RecipeInit.SERIALIZERS.register(modEventBus);
		BlockEntityInit.TILES.register(modEventBus);
		ContainerInit.CONTAINERS.register(modEventBus);
		EntityInit.ENTITY_TYPES.register(modEventBus);
		StructureInit.STRUCTURES.register(modEventBus);
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addGenericListener(Feature.class, EventPriority.LOWEST, Hemomancy::registerFeature);
		forgeBus.register(this);
		forgeBus.addListener(MorphlingJarEvents::pickupEvent);
		forgeBus.addListener(MorphlingJarEvents::onClientTick);
		forgeBus.addListener(KeyBindEvents::onClientTick);
		forgeBus.register(BloodVolumeEvents.class);
		forgeBus.register(VascularSystemEvents.class);
		forgeBus.register(BloodTendencyEvents.class);
		forgeBus.register(KnownManipulationEvents.class);
		forgeBus.register(EarthenVeinLocEvents.class);
		// forgeBus.addListener(EventPriority.NORMAL, WorldInit::addDimensionalSpacing);

	}

	private static void registerFeature(RegistryEvent.Register<Feature<?>> event) {
		WorldGenEvents.BLEEDING_HEART_FEATURE = WorldGenEvents.flower("bleeding_heart", 64, BlockInit.bleeding_heart);
		WorldGenEvents.BLEEDING_HEART_PLACEMENT = WorldGenEvents.flowerPlacement("aum",
				WorldGenEvents.BLEEDING_HEART_FEATURE, 32);
		// Ores
		WorldGenEvents.VENOUS_FEATURE = WorldGenEvents.netherOre("venous_ore", BlockInit.venous_stone,
				BlockInit.gilded_venous_stone, 27, 0.25F);

		WorldGenEvents.VENOUS_PLACEMENT = WorldGenEvents.orePlacement("venous_ore", WorldGenEvents.VENOUS_FEATURE, 56,
				HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(36)));

		// Trees
		   WorldGenEvents.WITCHWOOD_TREE_FEATURE = WorldGenEvents.tree("infected_fungus", BlockInit.infected_stem, new DarkOakTrunkPlacer(9, 3, 1), BlockInit.infected_cap, new DarkOakFoliagePlacer(ConstantInt.of(1), ConstantInt.of(1)), new ThreeLayersFeatureSize(1, 2, 1, 1, 2, OptionalInt.empty()));
	        WorldGenEvents.WITCHWOOD_TREE_PLACEMENT = WorldGenEvents.treePlacement("infected_fungus", WorldGenEvents.WITCHWOOD_TREE_FEATURE, BlockInit.infected_fungus);
	        WorldGenEvents.WITCHWOOD_TREE_VEGETATION = WorldGenEvents.treeVegetation("infected_fungus", WorldGenEvents.WITCHWOOD_TREE_FEATURE, PlacementUtils.countExtra(1, 0.1F, 0), 8, BlockInit.infected_fungus);
	}

// Automatically Registers BlockItems
	@SubscribeEvent
	public static void onRegisterItems(final RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		BlockInit.BASEBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().tab(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.SLABBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().tab(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.STAIRBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().tab(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.COLUMNBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().tab(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.CROSSBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().tab(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.MODELEDBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().tab(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.OBJBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().tab(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.SPECIALBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			if (block != BlockInit.active_befouling_ash_trail.get()
					&& block != BlockInit.active_smouldering_ash_trail.get()) {
				final Item.Properties properties = new Item.Properties().tab(HemomancyItemGroup.instance);
				final BlockItem blockItem = new BlockItem(block, properties);
				blockItem.setRegistryName(block.getRegistryName());
				registry.register(blockItem);
			}
		});
	}

// Creative Tab
	public static class HemomancyItemGroup extends CreativeModeTab {
		public static final HemomancyItemGroup instance = new HemomancyItemGroup(CreativeModeTab.TABS.length,
				"hemomancy");

		public HemomancyItemGroup(int index, String label) {
			super(index, label);
		}

		@Override
		public ItemStack makeIcon() {
			return new ItemStack(ItemInit.sanguine_formation.get());
		}

	}

	private void clientSetup(final FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(RenderBloodLaserEvent.class);
		HemoLib hemo = new HemoLib();
		hemo.registerTome();

	}

	private void commonSetup(final FMLCommonSetupEvent event) {

		HemoEntityPredicates.init();
		SkillPointInit.init();
		BloodCraftingRecipes.initPatterns();
		BloodCraftingRecipes.initRecipes();
		PolypRecipes.initRecipes();
		PacketHandler.registerChannels();
	}

	// Combined a few methods into one more generic one
	public static ItemStack findItemInPlayerInv(Player player, Class<? extends Item> item) {
		if (item.isInstance(player.getOffhandItem().getItem()))
			return player.getMainHandItem();
		if (item.isInstance(player.getOffhandItem().getItem()))
			return player.getOffhandItem();
		Inventory inventory = player.getInventory();
		for (int i = 0; i <= 35; i++) {
			ItemStack stack = inventory.getItem(i);
			if (item.isInstance(stack.getItem()))
				return stack;
		}
		return ItemStack.EMPTY;

	}

}