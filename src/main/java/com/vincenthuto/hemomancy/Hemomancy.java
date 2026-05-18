package com.vincenthuto.hemomancy;

import com.vincenthuto.hemomancy.common.block.harbinger.EngramTextureCache;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityRegistrar;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberDiscoveryEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.data.book.BloodStructurePageTemplate;
import com.vincenthuto.hemomancy.common.block.inscription.DiscoveryInscriptionLoader;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryLoader;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.config.HemoConfig;
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Hemomancy.MOD_ID)
public class Hemomancy {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "hemomancy";
    public static final DeferredRegister<CreativeModeTab> CREATIVETABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, Hemomancy.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> hemomancytab = CREATIVETABS.register(
            "hemomancytab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group." + MOD_ID + ".hemomancytab"))
                    .icon(() -> new ItemStack(ItemInit.sanguine_formation.get()))
                    .build());
    public static Hemomancy instance;
    public static boolean forcesLoaded = false;

    /**
     * NeoForge 1.21: the mod-event bus is injected into the constructor automatically.
     */
    public Hemomancy(IEventBus modEventBus) {
        forcesLoaded = ModList.get().isLoaded("forcesofreality");
        instance = this;
        // NeoForge uses NeoForge.EVENT_BUS instead of NeoForge.EVENT_BUS
        IEventBus forgeBus = NeoForge.EVENT_BUS;
        HemoConfig.register();
        VillagerInit.POINTS_OF_INTEREST.register(modEventBus);
        VillagerInit.PROFESSIONS.register(modEventBus);
        ManipulationInit.MANIPS.register(modEventBus);
        StillArtInit.STILL_ARTS.register(modEventBus);
        DataComponentInit.COMPONENTS.register(modEventBus);
        ParticleInit.PARTICLE_TYPES.register(modEventBus);
        EffectInit.EFFECTS.register(modEventBus);
        EffectInit.POTION_TYPES.register(modEventBus);
        CarverInit.CARVERS.register(modEventBus);
        BaseFeatureInit.FEATURE_REGISTER.register(modEventBus);
        ItemInit.BANNERPATTERNS.register(modEventBus);
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
        FluidInit.FLUID_TYPES.register(modEventBus);
        FluidInit.FLUIDS.register(modEventBus);
        BlockInit.LIQUIDBLOCKS.register(modEventBus);
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
        HemoAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        HemoCapabilityRegistrar.register(modEventBus);

        // GeckoLib 4 on NeoForge initializes via mod loading; explicit bootstrap call removed.
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener((FMLCommonSetupEvent event) -> LiberDiscoveryEvents.commonSetup(event));
        modEventBus.addListener((FMLCommonSetupEvent event) -> UnstainedProgressEvents.commonSetup(event));
        modEventBus.addListener(this::buildContents);
        forgeBus.register(this);
        forgeBus.addListener(this::onAddReloadListeners);

        // RegisterPayloadsEvent fires on the mod bus â€“ register here, not in commonSetup.
        PacketHandler.registerChannels(modEventBus);

        @SuppressWarnings("unused")
        ModList modList = ModList.get();
        // TODO(MnA-compat): re-enable once Mana and Artifice publishes a NeoForge 1.21.1 build
        // and the compat/mna/** source exclusion is removed from build.gradle.
        // if (modList.isLoaded("mna")) {
        //     LOGGER.info("MNA WAS LOADED");
        //     ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, HemoMnAConfig.register(), "hemomancy-mna-server.toml");
        //     forgeBus.addListener(MnAPlugin::onRegisterGuidebooks);
        //     MnAPluginItemInit.MNAITEMS.register(modEventBus);
        //     MnAPluginBlockInit.MNABLOCKS.register(modEventBus);
        //     MnAPluginBlockEntityInit.MNATILES.register(modEventBus);
        //     MnAPluginManipulationInit.MNA_MANIPS.register(modEventBus);
        //     modEventBus.addListener(MnAPluginItemInit::buildMnaCompatItemContents);
        //     modEventBus.addListener(MnAPluginBlockInit::onRegisterItems);
        //     modEventBus.addListener(MnAPluginBlockInit::buildMnaCompatBlockContents);
        //     forgeBus.addListener(MnAPlugin::playerInteractAnvil);
        //     forgeBus.addListener(MnAPlugin::onRunicAnvil);
        //     modEventBus.addListener(MnAPluginSpellInit::registerSpellBits);
        //     modEventBus.addListener(MnAPluginRitualInit::registerRitualEffects);
        //     MnAPluginEntityInit.MNA_ENTITY_TYPES.register(modEventBus);
        //     modEventBus.addListener(MnAPluginEntityInit::onAttributeCreate);
        //     forgeBus.addListener(BloodTitheHandler::onCalculateManaCost);
        //     forgeBus.addListener(BloodTitheHandler::onSpellCast);
        //     modEventBus.register(HarbingerEventHandler.class);
        //     if (FMLEnvironment.dist == Dist.CLIENT) {
        //         modEventBus.addListener(MnAPluginClientEvents::onRegisterSpecialModels);
        //         modEventBus.addListener(MnAPluginClientEvents::registerItemColors);
        //         modEventBus.addListener(MnAPluginClientEvents::registerModelLayers);
        //         modEventBus.addListener(MnAPluginClientEvents::renderEntities);
        //         modEventBus.addListener(MnAPluginClientEvents::onClientSetupEvent);
        //         forgeBus.addListener(MnAPluginClientEvents::onClientTick);
        //         modEventBus.register(HarbingerEventHandler.HarbingerClientEventHandler.class);
        //         modEventBus.addListener(MnAPluginBlockInit::registerBlocks);
        //     }
        // }
        // TODO(Curios-compat): re-enable once Curios publishes a NeoForge 1.21.1 build
        // and the compat/curios/** source exclusion is removed from build.gradle.
        // if (modList.isLoaded("curios")) {
        //     LOGGER.info("CURIOS WAS LOADED");
        //     modEventBus.addListener(CuriosPlugin::initCuriosSlots);
        //     modEventBus.addListener(CuriosPlugin::clientCurioSetup);
        // }
    }

    // Combined a few methods into one more generic one
    public static ItemStack findItemInPlayerInv(Player player, Class<? extends Item> item) {
        if (item.isInstance(player.getMainHandItem().getItem()))
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

    /**
     * NeoForge 1.21: use ResourceLocation.fromNamespaceAndPath() â€“ the two-arg
     * constructor ResourceLocation.parse(ns, path) is deprecated in 1.21.
     */
    public static ResourceLocation rloc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public void buildContents(BuildCreativeModeTabContentsEvent populator) {
        if (populator.getTabKey() == hemomancytab.getKey()) {
            var i = ItemInit.getAllItemEntriesAsStream();
            i.forEach(item -> {
                if (item.get() != ItemInit.active_befouling_ash.get()
                        && item.get() != ItemInit.active_smouldering_ash.get()) {
                    populator.accept(item.get());
                }
            });
            var b = BlockInit.getAllBlockEntriesAsStream();
            b.forEach(block -> {
                if (block.get() != BlockInit.attached_gourd_stem.get() && block.get() != BlockInit.gourd_stem.get()
                        && block.get() != BlockInit.active_befouling_ash_trail.get()
                        && block.get() != BlockInit.active_smouldering_ash_trail.get()
                        && block.get() != BlockInit.sanguine_conduit.get()
                        && block.get() != BlockInit.filler_block.get()
                        && block.get() != BlockInit.engram_block.get()
                        && block.get() != BlockInit.qliphoth_bloom.get()) {
                    populator.accept(block.get());
                }
            });
        }
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        EngramTextureCache.loadAll();
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ItemInquiryLoader());
        event.addListener(new DiscoveryInscriptionLoader());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        com.vincenthuto.hemomancy.client.morphling.MorphlingMutationRegistry.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BookPlaceboReloadListener.INSTANCE.registerSerializer(Hemomancy.rloc("blood_structure_page"),
                    BloodStructurePageTemplate.SERIALIZER);
        });
        HemoEntityPredicates.init();
        SkillPointInit.init();
        ManipulationTreeInit.init();
    }
}

