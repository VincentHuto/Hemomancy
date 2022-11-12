package com.vincenthuto.hemomancy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.capa.block.vein.EarthenVeinLocEvents;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.capa.player.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.capa.player.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.capa.volume.RenderBloodLaserEvent;
import com.vincenthuto.hemomancy.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.event.KeyBindEvents;
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
import com.vincenthuto.hemomancy.init.SoundInit;
import com.vincenthuto.hemomancy.init.StructureInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.recipe.PolypRecipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod("hemomancy")
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)

public class Hemomancy {
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

	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "hemomancy";
	public static Hemomancy instance;
	public static IProxy proxy = new IProxy() {
	};

	public static boolean forcesLoaded = false;

	public static Pair<ResourceLocation, BlockItem> createItemBlock(Pair<Block, ResourceLocation> block) {
		return Pair.of(block.getSecond(),
				new BlockItem(block.getFirst(), new Item.Properties().tab(HemomancyItemGroup.instance)));
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
	public static void onRegisterItems(final RegisterEvent event) {
		if (event.getRegistryKey() != ForgeRegistries.Keys.ITEMS) {
			return;
		}

		BlockInit.getAllBlockEntriesAsStream().map(m -> new Pair<>(m.get(), m.getId())).map(t -> createItemBlock(t))
				.forEach(item -> registerBlockItem(event, item));
	}

	private static void registerBlockItem(RegisterEvent event, Pair<ResourceLocation, BlockItem> item) {
		event.register(ForgeRegistries.Keys.ITEMS, helper -> helper.register(item.getFirst(), item.getSecond()));
	}

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
		RecipeInit.RECIPE_TYPES.register(modEventBus);
		SoundInit.SOUNDS.register(modEventBus);
		BlockEntityInit.TILES.register(modEventBus);
		ContainerInit.CONTAINERS.register(modEventBus);
		EntityInit.ENTITY_TYPES.register(modEventBus);
		StructureInit.STRUCTURES.register(modEventBus);
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::clientSetup);
		// modEventBus.addGenericListener(Feature.class, EventPriority.LOWEST,
		// Hemomancy::registerFeature);
		forgeBus.register(this);
		forgeBus.addListener(KeyBindEvents::onClientTick);
		forgeBus.register(BloodVolumeEvents.class);
		forgeBus.register(VascularSystemEvents.class);
		forgeBus.register(BloodTendencyEvents.class);
		forgeBus.register(KnownManipulationEvents.class);
		forgeBus.register(EarthenVeinLocEvents.class);
		// forgeBus.addListener(EventPriority.NORMAL, WorldInit::addDimensionalSpacing);

		ModLoadingContext modLoadingContext = ModLoadingContext.get();

	}

	private void clientSetup(final FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(RenderBloodLaserEvent.class);
		HemoLib hemo = new HemoLib();
		hemo.registerTome();

	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		HemoEntityPredicates.init();
		SkillPointInit.init();
		PolypRecipes.initRecipes();
		PacketHandler.registerChannels();

	}

}