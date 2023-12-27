package com.vincenthuto.hemomancy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.common.capability.block.vein.EarthenVeinLocEvents;
import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.data.DataGeneration;
import com.vincenthuto.hemomancy.common.data.book.BloodStructurePageTemplate;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.init.BaseFeatureInit;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.LootModifierInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.init.VillagerInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.recipe.PolypRecipes;
import com.vincenthuto.hemomancy.config.HemoConfig;
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod(Hemomancy.MOD_ID)
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class Hemomancy {

	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "hemomancy";
	public static Hemomancy instance;
	public ISidedProxy proxy;
	public static boolean forcesLoaded = false;

	public static final DeferredRegister<CreativeModeTab> CREATIVETABS = DeferredRegister
			.create(Registries.CREATIVE_MODE_TAB, Hemomancy.MOD_ID);
	public static final RegistryObject<CreativeModeTab> hemomancytab = CREATIVETABS.register("hemomancytab",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group." + MOD_ID + ".hemomancytab"))
					.icon(() -> new ItemStack(ItemInit.sanguine_formation.get())).build());

	public Hemomancy() {
		forcesLoaded = ModList.get().isLoaded("forcesofreality");
		instance = this;
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
	    HemoConfig.register();
		VillagerInit.POINTS_OF_INTEREST.register(modEventBus);
		VillagerInit.PROFESSIONS.register(modEventBus);
		ManipulationInit.MANIPS.register(modEventBus);
		ParticleInit.PARTICLE_TYPES.register(modEventBus);
		EffectInit.EFFECTS.register(modEventBus);
		EffectInit.POTION_TYPES.register(modEventBus);
		BaseFeatureInit.FEATURE_REGISTER.register(modEventBus);
		ItemInit.BASEITEMS.register(modEventBus);
		ItemInit.HANDHELDITEMS.register(modEventBus);
		ItemInit.SPECIALITEMS.register(modEventBus);
		ItemInit.SPAWNEGGS.register(modEventBus);
		BiomeInit.BIOME_REGISTER.register(modEventBus);
		BlockInit.BASEBLOCKS.register(modEventBus);
		BlockInit.SLABBLOCKS.register(modEventBus);
		BlockInit.STAIRBLOCKS.register(modEventBus);
		BlockInit.COLUMNBLOCKS.register(modEventBus);
		BlockInit.CROSSBLOCKS.register(modEventBus);
		BlockInit.OBJBLOCKS.register(modEventBus);
		BlockInit.SPECIALBLOCKS.register(modEventBus);
		BlockInit.POTTEDBLOCKS.register(modEventBus);

		BlockInit.MODELEDBLOCKS.register(modEventBus);
		FluidInit.FLUIDS.register(modEventBus);
		CREATIVETABS.register(modEventBus);
		RecipeInit.SERIALIZERS.register(modEventBus);
		RecipeInit.RECIPE_TYPES.register(modEventBus);
		SoundInit.SOUNDS.register(modEventBus);
		BlockEntityInit.TILES.register(modEventBus);
		ContainerInit.CONTAINERS.register(modEventBus);
		AttributeInit.ATTRIBUTES.register(modEventBus);
		EntityInit.ENTITY_TYPES.register(modEventBus);
		StructureInit.STRUCTURES.register(modEventBus);
		VillagerInit.STRUCTURE_PROCESSORS.register(modEventBus);
		LootModifierInit.LOOT_MODIFIERS.register(modEventBus);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> {
			return () -> {

				this.proxy = new ClientProxy();
			};
		});
		DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> {
			return () -> {
				this.proxy = new ServerProxy();
			};
		});

		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::buildContents);
		modEventBus.addListener(DataGeneration::generate);

		// modEventBus.addGenericListener(Feature.class, EventPriority.LOWEST,
		// Hemomancy::registerFeature);
		forgeBus.register(this);
		forgeBus.register(BloodVolumeEvents.class);
		forgeBus.register(VascularSystemEvents.class);
		forgeBus.register(BloodTendencyEvents.class);
		forgeBus.register(KnownManipulationEvents.class);
		forgeBus.register(EarthenVeinLocEvents.class);

	}

	public void buildContents(BuildCreativeModeTabContentsEvent populator) {
		if (populator.getTabKey() == hemomancytab.getKey()) {
			// Items
			ItemInit.BASEITEMS.getEntries().forEach(i -> populator.accept(i.get()));
			ItemInit.HANDHELDITEMS.getEntries().forEach(i -> populator.accept(i.get()));
			ItemInit.SPECIALITEMS.getEntries().forEach(i -> populator.accept(i.get()));
			ItemInit.SPAWNEGGS.getEntries().forEach(i -> populator.accept(i.get()));

			var b = BlockInit.getAllBlockEntriesAsStream();
			b.forEach(item -> {
				if (item.get() != BlockInit.attached_gourd_stem.get() 
						&& item.get() != BlockInit.gourd_stem.get()
						&& item.get() != BlockInit.active_befouling_ash_trail.get()
						&& item.get() != BlockInit.active_smouldering_ash_trail.get()) {
					populator.accept(item.get());
				}
			});
			// Blocks
//			BlockInit.BASEBLOCKS.getEntries().forEach(i -> populator.accept(i.get()));
//			BlockInit.SLABBLOCKS.getEntries().forEach(i -> populator.accept(i.get()));
//			BlockInit.STAIRBLOCKS.getEntries().forEach(i -> populator.accept(i.get()));
//			BlockInit.COLUMNBLOCKS.getEntries().forEach(i -> populator.accept(i.get()));
//			BlockInit.CROSSBLOCKS.getEntries().forEach(i -> populator.accept(i.get()));
//			BlockInit.OBJBLOCKS.getEntries().forEach(i -> populator.accept(i.get()));
//			BlockInit.MODELEDBLOCKS.getEntries().forEach(i -> populator.accept(i.get()));
//			BlockInit.SPECIALBLOCKS.getEntries().forEach(i -> populator.accept(i.get()));
		}
	}

	private void clientSetup(final FMLClientSetupEvent event) {
//		MinecraftForge.EVENT_BUS.addListener(CapeEvent::renderLevelLast);
//		MinecraftForge.EVENT_BUS.addListener(CapeEvent::onClientTick);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {

		event.enqueueWork(() -> {
			BookPlaceboReloadListener.INSTANCE.registerSerializer(Hemomancy.rloc("blood_structure_page"),
					BloodStructurePageTemplate.SERIALIZER);
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.bleeding_heart.getId(),
					BlockInit.potted_bleeding_heart);
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.stinkhorn_fungus.getId(),
					BlockInit.potted_stinkhorn_fungus);
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.infected_fungus.getId(),
					BlockInit.potted_infected_fungus);
			((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockInit.puffball_fungus.getId(),
					BlockInit.potted_puffball_fungus);

		});

		HemoEntityPredicates.init();
		SkillPointInit.init();
		PolypRecipes.initRecipes();
		PacketHandler.registerChannels();

	}

	public static Pair<ResourceLocation, BlockItem> createItemBlock(Pair<Block, ResourceLocation> block) {
		return Pair.of(block.getSecond(), new BlockItem(block.getFirst(), new Item.Properties()));
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

		var b = BlockInit.getAllBlockEntriesAsStream().map(m -> new Pair<>(m.get(), m.getId()))
				.map(t -> createItemBlock(t));
		b.forEach(item -> {
			if (item.getSecond().getBlock() != BlockInit.attached_gourd_stem.get()
					|| item.getSecond().getBlock() != BlockInit.gourd_stem.get()
					|| item.getSecond().getBlock() != BlockInit.active_befouling_ash_trail.get()
					|| item.getSecond().getBlock() != BlockInit.active_smouldering_ash_trail.get()) {
				registerBlockItem(event, item);
			}
		});

	}

	private static void registerBlockItem(RegisterEvent event, Pair<ResourceLocation, BlockItem> item) {
		event.register(ForgeRegistries.Keys.ITEMS, helper -> helper.register(item.getFirst(), item.getSecond()));
	}

	public static ResourceLocation rloc(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

}