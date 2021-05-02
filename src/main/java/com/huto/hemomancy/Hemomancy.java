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
import com.huto.hemomancy.gui.tendency.TendencyBookLib;
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
import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.recipe.ArmBannerCraftRecipe;
import com.huto.hemomancy.recipe.CopyBloodGourdDataRecipe;
import com.huto.hemomancy.recipe.CopyMorphlingJarDataRecipe;
import com.huto.hemomancy.recipe.CopyRuneBinderDataRecipe;
import com.huto.hemomancy.recipe.FillBloodGourdDataRecipe;
import com.huto.hemomancy.recipe.ModBloodCraftingRecipes;
import com.huto.hemomancy.recipe.ModChiselRecipes;
import com.huto.hemomancy.recipe.PolypRecipes;
import com.huto.hemomancy.render.layer.RenderBloodAbsorptionLayer;
import com.huto.hemomancy.render.layer.RenderLivingBladeHipLayer;
import com.huto.hemomancy.render.layer.RenderRunesLayer;
import com.huto.hemomancy.util.ModEntityPredicates;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
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
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
		BlockInit.COLUMNBLOCKS.register(modEventBus);
		BlockInit.CROSSBLOCKS.register(modEventBus);
		BlockInit.OBJBLOCKS.register(modEventBus);
		BlockInit.SPECIALBLOCKS.register(modEventBus);
		BlockInit.MODELEDBLOCKS.register(modEventBus);
		TileEntityInit.TILES.register(modEventBus);
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
			final Item.Properties properties = new Item.Properties().group(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.COLUMNBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().group(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.CROSSBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().group(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.MODELEDBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().group(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.OBJBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			final Item.Properties properties = new Item.Properties().group(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
		});
		BlockInit.SPECIALBLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
			if (block != BlockInit.active_befouling_ash_trail.get()
					&& block != BlockInit.active_smouldering_ash_trail.get()) {
				final Item.Properties properties = new Item.Properties().group(HemomancyItemGroup.instance);
				final BlockItem blockItem = new BlockItem(block, properties);
				blockItem.setRegistryName(block.getRegistryName());
				registry.register(blockItem);
			}
		});
	}

// Creative Tab
	public static class HemomancyItemGroup extends ItemGroup {
		public static final HemomancyItemGroup instance = new HemomancyItemGroup(ItemGroup.GROUPS.length, "hemomancy");

		public HemomancyItemGroup(int index, String label) {
			super(index, label);
		}

		@Override
		public ItemStack createIcon() {
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
		PacketHandler.registerRuneBinderChannels();
		PacketHandler.registerMorphlingJarChannels();
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(RenderBloodLaserEvent.class);
		MinecraftForge.EVENT_BUS.addListener(CapeEvent::renderWorldLast);
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
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();
		PlayerRenderer render;
		render = skinMap.get("default");
		render.addLayer(new RenderRunesLayer(render));
		if (!addedAbsorbLayerDefault) {
			render.addLayer(new RenderBloodAbsorptionLayer(render));
			addedAbsorbLayerDefault = true;
		}
		if (!addedSwordLayerDefault) {
			render.addLayer(new RenderLivingBladeHipLayer(render));
			addedSwordLayerDefault = true;
		}
//		if (!addedCapeLayerDefault) {
//			render.addLayer(new RenderClothLayer(render));
//			addedSwordLayerDefault = true;
//		}
		render = skinMap.get("slim");
		render.addLayer(new RenderRunesLayer(render));
		if (!addedSwordLayerSlim) {
			render.addLayer(new RenderLivingBladeHipLayer(render));
			addedSwordLayerSlim = true;
		}
		if (!addedAbsorbLayeSlim) {
			render.addLayer(new RenderBloodAbsorptionLayer(render));
			addedAbsorbLayeSlim = true;
		}
//		if (!addedCapeLayerSlim) {
//			render.addLayer(new RenderClothLayer(render));
//			addedAbsorbLayeSlim = true;
//		}

	}

	// Combined a few methods into one more generic one
	public static ItemStack findItemInPlayerInv(PlayerEntity player, Class<? extends Item> item) {
		if (item.isInstance(player.getHeldItemOffhand().getItem()))
			return player.getHeldItemMainhand();
		if (item.isInstance(player.getHeldItemOffhand().getItem()))
			return player.getHeldItemOffhand();
		PlayerInventory inventory = player.inventory;
		for (int i = 0; i <= 35; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (item.isInstance(stack.getItem()))
				return stack;
		}
		return ItemStack.EMPTY;

	}

	@SubscribeEvent
	public static void onRecipeRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
		event.getRegistry().register(new CopyMorphlingJarDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(MOD_ID, "morphling_jar_upgrade")));
		event.getRegistry().register(new CopyRuneBinderDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(MOD_ID, "rune_binder_upgrade")));
		event.getRegistry().register(new CopyBloodGourdDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(MOD_ID, "blood_gourd_upgrade")));
		event.getRegistry().register(new FillBloodGourdDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(MOD_ID, "blood_gourd_fill")));
		event.getRegistry().register(new SpecialRecipeSerializer<>(ArmBannerCraftRecipe::new)
				.setRegistryName(new ResourceLocation(MOD_ID, "arm_banner_craft")));

	}

	// Structure Jazz
	@SubscribeEvent
	public static void onRegisterStructures(final RegistryEvent.Register<Structure<?>> event) {
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
		if (event.getWorld() instanceof ServerWorld) {
			ServerWorld serverWorld = (ServerWorld) event.getWorld();
			// Prevent spawning our structure in Vanilla's superflat world
			if (serverWorld.getChunkProvider().getChunkGenerator() instanceof FlatChunkGenerator
					&& serverWorld.getDimensionKey().equals(World.OVERWORLD)) {
				return;
			}
			Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(
					serverWorld.getChunkProvider().generator.func_235957_b_().func_236195_a_());
//			tempMap.put(StructureInit.RUN_DOWN_HOUSE.get(),
//					DimensionStructuresSettings.field_236191_b_.get(StructureInit.RUN_DOWN_HOUSE.get()));
			if (serverWorld.getDimensionKey().equals(World.THE_NETHER)) {
				tempMap.put(StructureInit.blood_temple.get(),
						DimensionStructuresSettings.field_236191_b_.get(StructureInit.blood_temple.get()));
			}
			serverWorld.getChunkProvider().generator.func_235957_b_().field_236193_d_ = tempMap;
		}
	}

}
