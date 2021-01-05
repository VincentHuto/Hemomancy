package com.huto.hemomancy;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeEvents;
import com.huto.hemomancy.capabilities.tendency.BloodTendencyEvents;
import com.huto.hemomancy.capabilities.vascularsystem.VascularSystemEvents;
import com.huto.hemomancy.event.KeyBindEvents;
import com.huto.hemomancy.event.RuneBinderEvents;
import com.huto.hemomancy.gui.guide.GuideBookLib;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.CapabilityInit;
import com.huto.hemomancy.init.ContainerInit;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.item.runes.ItemRuneBinder;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.recipes.CopyRuneBinderDataRecipe;
import com.huto.hemomancy.recipes.ModChiselRecipes;
import com.huto.hemomancy.render.layer.RunesRenderLayer;

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

	@SuppressWarnings("deprecation")
	public Hemomancy() {
		DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> proxy = new ClientProxy());
		proxy.registerHandlers();
		instance = this;
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
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
		MinecraftForge.EVENT_BUS.addListener(KeyBindEvents::onClientTick);
		MinecraftForge.EVENT_BUS.register(BloodVolumeEvents.class);
		MinecraftForge.EVENT_BUS.register(VascularSystemEvents.class);
		MinecraftForge.EVENT_BUS.register(BloodTendencyEvents.class);

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
			final Item.Properties properties = new Item.Properties().group(HemomancyItemGroup.instance);
			final BlockItem blockItem = new BlockItem(block, properties);
			blockItem.setRegistryName(block.getRegistryName());
			registry.register(blockItem);
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
		CapabilityInit.init();
		ModChiselRecipes.init();
		PacketHandler.registerChannels();
		PacketHandler.registerRuneBinderChannels();
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		GuideBookLib.registerPages();
		this.addLayers();

	}

	private void enqueueIMC(final InterModEnqueueEvent event) {
	}

	private void processIMC(final InterModProcessEvent event) {

	}

	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {
	}

	// Rune Layers
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@OnlyIn(Dist.CLIENT)
	private void addLayers() {
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();
		PlayerRenderer render;
		render = skinMap.get("default");
		render.addLayer(new RunesRenderLayer(render));
		render = skinMap.get("slim");
		render.addLayer(new RunesRenderLayer(render));
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

	@SubscribeEvent
	public static void onRecipeRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
		event.getRegistry().register(new CopyRuneBinderDataRecipe.Serializer()
				.setRegistryName(new ResourceLocation(MOD_ID, "rune_binder_upgrade")));
	}
}
