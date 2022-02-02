package com.vincenthuto.hemomancy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vincenthuto.hemomancy.capa.block.vein.EarthenVeinLocEvents;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.capa.player.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.capa.player.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.capa.player.volume.RenderBloodLaserEvent;
import com.vincenthuto.hemomancy.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.entity.brain.SensorInit;
import com.vincenthuto.hemomancy.event.KeyBindEvents;
import com.vincenthuto.hemomancy.event.MorphlingJarEvents;
import com.vincenthuto.hemomancy.event.RuneBinderEvents;
import com.vincenthuto.hemomancy.gui.guide.HemoLib;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ContainerInit;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.init.FeatureInit;
import com.vincenthuto.hemomancy.init.FluidInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.init.ManipulationInit;
import com.vincenthuto.hemomancy.init.ParticleInit;
import com.vincenthuto.hemomancy.init.PotionInit;
import com.vincenthuto.hemomancy.init.RecipeTypeInit;
import com.vincenthuto.hemomancy.init.SkillPointInit;
import com.vincenthuto.hemomancy.init.WorldInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.recipe.BloodCraftingRecipes;
import com.vincenthuto.hemomancy.recipe.ChiselRecipes;
import com.vincenthuto.hemomancy.recipe.EarthlyTransfuserDataRecipe;
import com.vincenthuto.hemomancy.recipe.JuiceinatorDataRecipe;
import com.vincenthuto.hemomancy.recipe.PolypRecipes;
import com.vincenthuto.hemomancy.recipe.RecallerRecipes;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
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

	public Hemomancy() {

		DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> proxy = new ClientProxy());
		proxy.registerHandlers();
		forcesLoaded = ModList.get().isLoaded("forcesofreality");
		instance = this;

		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;

		ChiselRecipes.CHISELRECIPES.register(modEventBus);
		RecallerRecipes.RECALLERRECIPES.register(modEventBus);
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
		RecipeTypeInit.RECIPES.register(modEventBus);
		BlockEntityInit.TILES.register(modEventBus);
		ContainerInit.CONTAINERS.register(modEventBus);
		EntityInit.ENTITY_TYPES.register(modEventBus);
		FeatureInit.STRUCTURES.register(modEventBus);
		SensorInit.DATA_SERIALIZERS.register(modEventBus);
		SensorInit.SENSORS.register(modEventBus);

		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::clientSetup);
		forgeBus.register(this);
		forgeBus.addListener(RuneBinderEvents::pickupEvent);
		forgeBus.addListener(RuneBinderEvents::onClientTick);
		forgeBus.addListener(MorphlingJarEvents::pickupEvent);
		forgeBus.addListener(MorphlingJarEvents::onClientTick);
		forgeBus.addListener(KeyBindEvents::onClientTick);
		forgeBus.register(BloodVolumeEvents.class);
		forgeBus.register(VascularSystemEvents.class);
		forgeBus.register(BloodTendencyEvents.class);
		forgeBus.register(KnownManipulationEvents.class);
		forgeBus.register(EarthenVeinLocEvents.class);
		forgeBus.addListener(EventPriority.NORMAL, WorldInit::addDimensionalSpacing);

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

		event.enqueueWork(() -> {
			FeatureInit.setupStructures();
			WorldInit.registerConfiguredStructures();
		});

//		Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MOD_ID, "juiceinator"),
//				new RecipeType<JuiceinatorDataRecipe>() {
//					@Override
//					public String toString() {
//						return "juiceinator";
//					}
//				});
//
//		Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MOD_ID, "earthly_transfuser"),
//				new RecipeType<EarthlyTransfuserDataRecipe>() {
//					@Override
//					public String toString() {
//						return "earthly_transfuser";
//					}
//				});
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