package com.huto.hemomancy;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huto.hemomancy.capa.manip.KnownManipulationEvents;
import com.huto.hemomancy.capa.tendency.BloodTendencyEvents;
import com.huto.hemomancy.capa.vascular.VascularSystemEvents;
import com.huto.hemomancy.capa.volume.BloodVolumeEvents;
import com.huto.hemomancy.capa.volume.RenderBloodLaserEvent;
import com.huto.hemomancy.event.CapeEvent;
import com.huto.hemomancy.event.KeyBindEvents;
import com.huto.hemomancy.event.MorphlingJarEvents;
import com.huto.hemomancy.event.RuneBinderEvents;
import com.huto.hemomancy.gui.guide.GuideBookLib;
import com.huto.hemomancy.gui.guide.tendency.TendencyBookLib;
import com.huto.hemomancy.init.BlockEntityInit;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.CapabilityInit;
import com.huto.hemomancy.init.ConfiguredStructureInit;
import com.huto.hemomancy.init.ContainerInit;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.init.SkillPointInit;
import com.huto.hemomancy.init.StructureInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.recipe.ArmBannerCraftRecipe;
import com.huto.hemomancy.recipe.CopyBloodGourdDataRecipe;
import com.huto.hemomancy.recipe.CopyMorphlingJarDataRecipe;
import com.huto.hemomancy.recipe.CopyRuneBinderDataRecipe;
import com.huto.hemomancy.recipe.FillBloodGourdDataRecipe;
import com.huto.hemomancy.recipe.ModBloodCraftingRecipes;
import com.huto.hemomancy.recipe.ModChiselRecipes;
import com.huto.hemomancy.recipe.ModRecallerRecipes;
import com.huto.hemomancy.recipe.PolypRecipes;
import com.huto.hemomancy.util.ModEntityPredicates;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.registries.IForgeRegistry;

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

		ModChiselRecipes.CHISELRECIPES.register(modEventBus);
		ModRecallerRecipes.RECALLERRECIPES.register(modEventBus);
		ManipulationInit.MANIPS.register(modEventBus);
		StructureInit.STRUCTURES.register(modEventBus);
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
		BlockEntityInit.TILES.register(modEventBus);
		ContainerInit.CONTAINERS.register(modEventBus);
		EntityInit.ENTITY_TYPES.register(modEventBus);
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::enqueueIMC);
		modEventBus.addListener(this::processIMC);
		modEventBus.addListener(this::clientSetup);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.addListener(RuneBinderEvents::pickupEvent);
		MinecraftForge.EVENT_BUS.addListener(RuneBinderEvents::onClientTick);
		MinecraftForge.EVENT_BUS.addListener(MorphlingJarEvents::pickupEvent);
		MinecraftForge.EVENT_BUS.addListener(MorphlingJarEvents::onClientTick);
		MinecraftForge.EVENT_BUS.addListener(KeyBindEvents::onClientTick);
		MinecraftForge.EVENT_BUS.register(BloodVolumeEvents.class);
		MinecraftForge.EVENT_BUS.register(VascularSystemEvents.class);
		MinecraftForge.EVENT_BUS.register(BloodTendencyEvents.class);
		MinecraftForge.EVENT_BUS.register(KnownManipulationEvents.class);
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.addListener(EventPriority.NORMAL, this::addDimensionalSpacing);
		forgeBus.addListener(EventPriority.HIGH, this::biomeModification);

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

	private void commonSetup(final FMLCommonSetupEvent event) {
		ModEntityPredicates.init();
		CapabilityInit.init();
		SkillPointInit.init();
		ModBloodCraftingRecipes.initPatterns();
		ModBloodCraftingRecipes.initRecipes();
		PolypRecipes.initRecipes();
		PacketHandler.registerChannels();
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(RenderBloodLaserEvent.class);
		MinecraftForge.EVENT_BUS.addListener(CapeEvent::renderLevelLast);
		MinecraftForge.EVENT_BUS.addListener(CapeEvent::onClientTick);
		GuideBookLib.registerPages();
		TendencyBookLib.registerPages();
		this.addLayers();

	}

	private void enqueueIMC(final InterModEnqueueEvent event) {

	}

	private void processIMC(final InterModProcessEvent event) {

	}

	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {
	}

	private static boolean addedAbsorbLayerDefault = false;
	private static boolean addedSwordLayerDefault = false;
	@SuppressWarnings("unused")
	private static boolean addedCapeLayerDefault = false;
	private static boolean addedAbsorbLayeSlim = false;
	private static boolean addedSwordLayerSlim = false;
	@SuppressWarnings("unused")
	private static boolean addedCapeLayerSlim = false;

	// Rune Layers
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@OnlyIn(Dist.CLIENT)
	private void addLayers() {
		/*
		 * Map<String, EntityRenderer<? extends Player>> skinMap =
		 * Minecraft.getInstance().getEntityRenderDispatcher() .getSkinMap();
		 * PlayerRenderer render; render = skinMap.get("default"); render.addLayer(new
		 * RenderRunesLayer(render)); if (!addedAbsorbLayerDefault) {
		 * render.addLayer(new RenderCellHandLayer(render)); addedAbsorbLayerDefault =
		 * true; } if (!addedSwordLayerDefault) { render.addLayer(new
		 * RenderLivingBladeHipLayer(render)); addedSwordLayerDefault = true; } // if
		 * (!addedCapeLayerDefault) { // render.addLayer(new RenderClothLayer(render));
		 * // addedSwordLayerDefault = true; // } render = skinMap.get("slim");
		 * render.addLayer(new RenderRunesLayer(render)); if (!addedSwordLayerSlim) {
		 * render.addLayer(new RenderLivingBladeHipLayer(render)); addedSwordLayerSlim =
		 * true; } if (!addedAbsorbLayeSlim) { render.addLayer(new
		 * RenderCellHandLayer(render)); addedAbsorbLayeSlim = true; } // if
		 * (!addedCapeLayerSlim) { // render.addLayer(new RenderClothLayer(render)); //
		 * addedAbsorbLayeSlim = true; // }
		 */
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

	@SubscribeEvent
	public static void onRecipeRegistry(final RegistryEvent.Register<RecipeSerializer<?>> event) {
		event.getRegistry().register(new CopyMorphlingJarDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(MOD_ID, "morphling_jar_upgrade")));
		event.getRegistry().register(new CopyRuneBinderDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(MOD_ID, "rune_bindForSetuper_upgrade")));
		event.getRegistry().register(new CopyBloodGourdDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(MOD_ID, "blood_gourd_upgrade")));
		event.getRegistry().register(new FillBloodGourdDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(MOD_ID, "blood_gourd_fill")));
		event.getRegistry().register(new SimpleRecipeSerializer<>(ArmBannerCraftRecipe::new)
				.setRegistryName(new ResourceLocation(MOD_ID, "arm_banner_craft")));

	}

	// Structure Jazz
	@SubscribeEvent
	public static void onRegisterStructures(final RegistryEvent.Register<StructureFeature<?>> event) {
		StructureInit.registerStructures(event);
		ConfiguredStructureInit.registerConfiguredStructures();
	}

	public void biomeModification(final BiomeLoadingEvent event) {
		// Add our structure to all biomes including other modded biomes
		// You can filter to certain biomes based on stuff like temperature, scale,
		// precipitation, mod id
		// event.getGeneration().getStructures().add(() ->
		// ConfiguredStructureInit.CONFIGURED_RUN_DOWN_HOUSE);
		event.getGeneration().getStructures().add(() -> ConfiguredStructureInit.CONFIGURED_BLOOD_TEMPLE);
	}

	/**
	 * Use this for dimension blacklists for your structure.
	 */
	public void addDimensionalSpacing(final WorldEvent.Load event) {
		if (event.getWorld() instanceof ServerLevel) {
			ServerLevel serverLevel = (ServerLevel) event.getWorld();
			// Prevent spawning our structure in Vanilla's superflat world
			if (serverLevel.getChunkSource().getGenerator() instanceof FlatLevelSource
					&& serverLevel.dimension().equals(Level.OVERWORLD)) {
				return;
			}
			Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(
					serverLevel.getChunkSource().generator.getSettings().structureConfig());
//			tempMap.put(StructureInit.RUN_DOWN_HOUSE.get(),
//					DimensionStructuresSettings.DEFAULTS.get(StructureInit.RUN_DOWN_HOUSE.get()));
			if (serverLevel.dimension().equals(Level.NETHER)) {
				tempMap.put(StructureInit.blood_temple.get(),
						StructureSettings.DEFAULTS.get(StructureInit.blood_temple.get()));
			}
			serverLevel.getChunkSource().generator.getSettings().structureConfig().putAll(tempMap);
		}
	}

}
