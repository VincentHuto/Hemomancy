package com.huto.hemomancy;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huto.hemomancy.capa.manip.KnownManipulationEvents;
import com.huto.hemomancy.capa.tendency.BloodTendencyEvents;
import com.huto.hemomancy.capa.vascular.VascularSystemEvents;
import com.huto.hemomancy.capa.volume.BloodVolumeEvents;
import com.huto.hemomancy.capa.volume.RenderBloodLaserEvent;
import com.huto.hemomancy.event.KeyBindEvents;
import com.huto.hemomancy.event.MorphlingJarEvents;
import com.huto.hemomancy.event.RuneBinderEvents;
import com.huto.hemomancy.gui.guide.GuideBookLib;
import com.huto.hemomancy.gui.tendency.TendencyBookLib;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.CapabilityInit;
import com.huto.hemomancy.init.ContainerInit;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.init.ParticleInit;
import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.init.SkillPointInit;
import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.item.morphlings.ItemMorphlingJar;
import com.huto.hemomancy.item.rune.ItemRuneBinder;
import com.huto.hemomancy.item.tool.living.ItemLivingBlade;
import com.huto.hemomancy.item.tool.living.ItemLivingStaff;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.recipes.CopyBloodGourdDataRecipe;
import com.huto.hemomancy.recipes.CopyMorphlingJarDataRecipe;
import com.huto.hemomancy.recipes.CopyRuneBinderDataRecipe;
import com.huto.hemomancy.recipes.FillBloodGourdDataRecipe;
import com.huto.hemomancy.recipes.ModBloodCraftingRecipes;
import com.huto.hemomancy.recipes.ModChiselRecipes;
import com.huto.hemomancy.recipes.PolypRecipes;
import com.huto.hemomancy.render.layer.HandParticleLayer;
import com.huto.hemomancy.render.layer.LivingBladeRenderLayer;
import com.huto.hemomancy.render.layer.RunesRenderLayer;
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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
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

	@SuppressWarnings("deprecation")
	public Hemomancy() {
		DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> proxy = new ClientProxy());
		proxy.registerHandlers();
		forcesLoaded = ModList.get().isLoaded("forcesofreality");
		instance = this;
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
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
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
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
		ModChiselRecipes.init();
		PolypRecipes.initRecipes();
		ManipulationInit.init();
		PacketHandler.registerChannels();
		PacketHandler.registerRuneBinderChannels();
		PacketHandler.registerMorphlingJarChannels();
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(RenderBloodLaserEvent.class);
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

	private static boolean addedSpellLayerDefault = false;
	private static boolean addedSwordLayerDefault = false;
	private static boolean addedSpellLayeSlim = false;
	private static boolean addedSwordLayerSlim = false;

	// Rune Layers
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@OnlyIn(Dist.CLIENT)
	private void addLayers() {
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();
		PlayerRenderer render;
		render = skinMap.get("default");
		render.addLayer(new RunesRenderLayer(render));
		if (!addedSpellLayerDefault) {
			render.addLayer(new HandParticleLayer(render));
			addedSpellLayerDefault = true;
		}
		if (!addedSwordLayerDefault) {
			render.addLayer(new LivingBladeRenderLayer(render));
			addedSwordLayerDefault = true;
		}
		render = skinMap.get("slim");
		render.addLayer(new RunesRenderLayer(render));
		if (!addedSwordLayerSlim) {
			render.addLayer(new LivingBladeRenderLayer(render));
			addedSwordLayerSlim = true;
		}
		if (!addedSpellLayeSlim) {
			render.addLayer(new HandParticleLayer(render));
			addedSpellLayeSlim = true;
		}

	}

	public static ItemStack findLivingBlade(PlayerEntity player) {
		if (player.getHeldItemMainhand().getItem() instanceof ItemLivingBlade)
			return player.getHeldItemMainhand();
		if (player.getHeldItemOffhand().getItem() instanceof ItemLivingBlade)
			return player.getHeldItemOffhand();
		PlayerInventory inventory = player.inventory;
		for (int i = 0; i <= 35; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.getItem() instanceof ItemLivingBlade)
				return stack;
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack findRuneBinder(PlayerEntity player) {
		if (player.getHeldItemMainhand().getItem() instanceof ItemRuneBinder)
			return player.getHeldItemMainhand();
		if (player.getHeldItemOffhand().getItem() instanceof ItemRuneBinder)
			return player.getHeldItemOffhand();
		PlayerInventory inventory = player.inventory;
		for (int i = 0; i <= 35; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.getItem() instanceof ItemRuneBinder)
				return stack;
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack findMorphlingJar(PlayerEntity player) {
		if (player.getHeldItemMainhand().getItem() instanceof ItemMorphlingJar)
			return player.getHeldItemMainhand();
		if (player.getHeldItemOffhand().getItem() instanceof ItemMorphlingJar)
			return player.getHeldItemOffhand();
		PlayerInventory inventory = player.inventory;
		for (int i = 0; i <= 35; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.getItem() instanceof ItemMorphlingJar)
				return stack;
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack findLivingStaff(PlayerEntity player) {
		if (player.getHeldItemMainhand().getItem() instanceof ItemLivingStaff)
			return player.getHeldItemMainhand();
		if (player.getHeldItemOffhand().getItem() instanceof ItemLivingStaff)
			return player.getHeldItemOffhand();
		PlayerInventory inventory = player.inventory;
		for (int i = 0; i <= 35; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.getItem() instanceof ItemLivingStaff)
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
	}
}
