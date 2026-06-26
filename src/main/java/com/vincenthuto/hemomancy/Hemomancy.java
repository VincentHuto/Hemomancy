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
import com.vincenthuto.hemomancy.common.worldgen.PocketDimensionManager;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
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
import net.neoforged.neoforge.event.tick.LevelTickEvent;
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
                    .icon(() -> new ItemStack(ItemInit.charm_of_vascularium.get()))
                    .build());
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> hemomancywiptab = CREATIVETABS.register(
            "hemomancywip",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group." + MOD_ID + ".hemomancywip"))
                    .withTabsBefore(hemomancytab.getKey())
                    .icon(() -> new ItemStack(ItemInit.sanguine_blob.get()))
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
        ScarInit.SCARS.register(modEventBus);
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

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener((FMLCommonSetupEvent event) -> LiberDiscoveryEvents.commonSetup(event));
        modEventBus.addListener((FMLCommonSetupEvent event) -> UnstainedProgressEvents.commonSetup(event));
        modEventBus.addListener(this::buildContents);
        forgeBus.register(this);
        forgeBus.addListener(this::onAddReloadListeners);
        forgeBus.addListener(this::onLevelTick);


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
    private void onLevelTick(final LevelTickEvent.Post event) {
        PocketDimensionManager.tick(event.getLevel());
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
                if (shouldShowItemInCreativeTab(item.get())) {
                    populator.accept(item.get());
                }
            });
            var b = BlockInit.getAllBlockEntriesAsStream();
            b.forEach(block -> {
                if (shouldShowBlockInCreativeTab(block.get())) {
                    populator.accept(block.get());
                }
            });
        } else if (populator.getTabKey() == hemomancywiptab.getKey()) {
            acceptWipCreativeTabContents(populator);
        }
    }

    private static void acceptWipCreativeTabContents(BuildCreativeModeTabContentsEvent populator) {
        populator.accept(ItemInit.hematic_suture_needle.get());
        populator.accept(ItemInit.unsigned_ancestral_ledger.get());
        populator.accept(ItemInit.sanguine_blob.get());
        populator.accept(ItemInit.fervent_husk.get());
        populator.accept(ItemInit.curor_lens.get());
        populator.accept(ItemInit.vascular_poultice.get());
        populator.accept(ItemInit.memory_thread.get());
        populator.accept(ItemInit.hearty_compass.get());
        populator.accept(ItemInit.void_eye_organ.get());
        populator.accept(ItemInit.zombie_husk_effigy.get());
        populator.accept(ItemInit.desert_husk_effigy.get());
        populator.accept(ItemInit.spider_husk_effigy.get());
        populator.accept(ItemInit.vitality_chalice.get());
        populator.accept(ItemInit.vein_spider.get());
        populator.accept(ItemInit.constrictor_cord.get());
        populator.accept(ItemInit.blood_thrall_effigy.get());
        populator.accept(ItemInit.blood_bolt.get());
        populator.accept(ItemInit.vascular_status_gauge.get());
        populator.accept(ItemInit.hallowed_residuum_hemorath.get());
        populator.accept(ItemInit.hallowed_residuum_seraphae.get());
        populator.accept(ItemInit.hallowed_residuum_putriciel.get());
        populator.accept(ItemInit.hallowed_residuum_velorum.get());
        populator.accept(ItemInit.saint_relic_hemorath.get());
        populator.accept(ItemInit.saint_relic_seraphae.get());
        populator.accept(ItemInit.saint_relic_putriciel.get());
        populator.accept(ItemInit.saint_relic_velorum.get());
        populator.accept(ItemInit.echo_of_spleen.get());
        populator.accept(ItemInit.echo_of_liver.get());
        populator.accept(ItemInit.echo_of_heart.get());
        populator.accept(ItemInit.echo_of_lungs.get());
        populator.accept(ItemInit.echo_of_kidneys.get());
        populator.accept(ItemInit.draught_of_still_mercy.get());
        populator.accept(ItemInit.lethean_brew.get());
        populator.accept(ItemInit.tome_of_the_unstained.get());
        populator.accept(BlockInit.humane_idol.get());
        populator.accept(BlockInit.serpentine_idol.get());
        populator.accept(BlockInit.morphling_cradle.get());
        populator.accept(BlockInit.witness_organ.get());
        populator.accept(BlockInit.saint_sarcophagus.get());
        populator.accept(BlockInit.gourdvine_tap.get());
        populator.accept(BlockInit.infested_wood.get());
        populator.accept(BlockInit.sanguine_vigil.get());
        populator.accept(BlockInit.sanguine_omen.get());
        populator.accept(BlockInit.visceral_mirror.get());
        populator.accept(BlockInit.blood_basin.get());
        populator.accept(BlockInit.blood_pylon.get());
        populator.accept(BlockInit.blood_trial_altar.get());
        populator.accept(BlockInit.offering_gate.get());
        populator.accept(BlockInit.crucible_of_nether_ichor.get());
        populator.accept(BlockInit.voidtouched_vessel.get());
    }

    private static boolean shouldShowItemInCreativeTab(Item item) {
        return item != ItemInit.active_befouling_ash.get()
                && item != ItemInit.active_smouldering_ash.get()
                && item != ItemInit.memory_conjure_living_staff.get()
                && !isWipItem(item);
    }

    private static boolean isWipItem(Item item) {
        return item == ItemInit.hematic_suture_needle.get()
                || item == ItemInit.unsigned_ancestral_ledger.get()
                || item == ItemInit.sanguine_blob.get()
                || item == ItemInit.fervent_husk.get()
                || item == ItemInit.curor_lens.get()
                || item == ItemInit.vascular_poultice.get()
                || item == ItemInit.memory_thread.get()
                || item == ItemInit.hearty_compass.get()
                || item == ItemInit.void_eye_organ.get()
                || item == ItemInit.zombie_husk_effigy.get()
                || item == ItemInit.desert_husk_effigy.get()
                || item == ItemInit.spider_husk_effigy.get()
                || item == ItemInit.vitality_chalice.get()
                || item == ItemInit.vein_spider.get()
                || item == ItemInit.constrictor_cord.get()
                || item == ItemInit.blood_thrall_effigy.get()
                || item == ItemInit.blood_bolt.get()
                || item == ItemInit.vascular_status_gauge.get()
                || item == ItemInit.hallowed_residuum_hemorath.get()
                || item == ItemInit.hallowed_residuum_seraphae.get()
                || item == ItemInit.hallowed_residuum_putriciel.get()
                || item == ItemInit.hallowed_residuum_velorum.get()
                || item == ItemInit.saint_relic_hemorath.get()
                || item == ItemInit.saint_relic_seraphae.get()
                || item == ItemInit.saint_relic_putriciel.get()
                || item == ItemInit.saint_relic_velorum.get()
                || item == ItemInit.echo_of_spleen.get()
                || item == ItemInit.echo_of_liver.get()
                || item == ItemInit.echo_of_heart.get()
                || item == ItemInit.echo_of_lungs.get()
                || item == ItemInit.echo_of_kidneys.get()
                || item == ItemInit.draught_of_still_mercy.get()
                || item == ItemInit.lethean_brew.get()
                || item == ItemInit.tome_of_the_unstained.get();
    }

    private static boolean shouldShowBlockInCreativeTab(Block block) {
        return block.asItem() != Items.AIR
                && block != BlockInit.attached_gourd_stem.get()
                && block != BlockInit.gourd_stem.get()
                && block != BlockInit.active_befouling_ash_trail.get()
                && block != BlockInit.active_smouldering_ash_trail.get()
                && block != BlockInit.sanguine_conduit.get()
                && block != BlockInit.lethean_poppy_wreath.get()
                && block != BlockInit.filler_block.get()
                && block != BlockInit.engram_block.get()
                && block != BlockInit.abocipher_emitter.get()
                && block != BlockInit.qliphoth_bloom.get()
                && !isWipBlock(block);
    }

    private static boolean isWipBlock(Block block) {
        return block == BlockInit.humane_idol.get()
                || block == BlockInit.serpentine_idol.get()
                || block == BlockInit.morphling_cradle.get()
                || block == BlockInit.witness_organ.get()
                || block == BlockInit.saint_sarcophagus.get()
                || block == BlockInit.gourdvine_tap.get()
                || block == BlockInit.infested_wood.get()
                || block == BlockInit.sanguine_vigil.get()
                || block == BlockInit.sanguine_omen.get()
                || block == BlockInit.visceral_mirror.get()
                || block == BlockInit.blood_basin.get()
                || block == BlockInit.blood_pylon.get()
                || block == BlockInit.blood_trial_altar.get()
                || block == BlockInit.offering_gate.get()
                || block == BlockInit.crucible_of_nether_ichor.get()
                || block == BlockInit.voidtouched_vessel.get();
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

