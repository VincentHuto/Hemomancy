package com.vincenthuto.hemomancy;

import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.data.book.BloodStructurePageTemplate;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityRegistrar;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.block.EngramTextureCache;
// ── Optional-dep compat imports (excluded from compilation when dep absent) ───
// MnA compat: re-enable when NeoForge 1.21.1 MnA build is published and the
// compat/mna/** source exclusion is removed from build.gradle.
// import com.vincenthuto.hemomancy.compat.curios.CuriosPlugin;
// import com.vincenthuto.hemomancy.compat.mna.MnAPlugin;
// import com.vincenthuto.hemomancy.compat.mna.MnAPluginClientEvents;
// import com.vincenthuto.hemomancy.compat.mna.block.MnAPluginBlockInit;
// import com.vincenthuto.hemomancy.compat.mna.entity.MnAPluginEntityInit;
// import com.vincenthuto.hemomancy.compat.mna.faction.HarbingerEventHandler;
// import com.vincenthuto.hemomancy.compat.mna.item.MnAPluginItemInit;
// import com.vincenthuto.hemomancy.compat.mna.ritual.MnAPluginRitualInit;
// import com.vincenthuto.hemomancy.compat.mna.spell.BloodTitheHandler;
// import com.vincenthuto.hemomancy.compat.mna.spell.MnAPluginManipulationInit;
// import com.vincenthuto.hemomancy.compat.mna.spell.MnAPluginSpellInit;
// import com.vincenthuto.hemomancy.compat.mna.tile.MnAPluginBlockEntityInit;
import com.vincenthuto.hemomancy.config.HemoConfig;
// import com.vincenthuto.hemomancy.config.HemoMnAConfig; // re-enable with MnA compat
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
// ── NeoForge API imports (replaces net.minecraftforge.*) ─────────────────────
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(Hemomancy.MOD_ID)
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
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
        DataComponentInit.COMPONENTS.register(modEventBus);
        ParticleInit.PARTICLE_TYPES.register(modEventBus);
        EffectInit.EFFECTS.register(modEventBus);
        EffectInit.POTION_TYPES.register(modEventBus);
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
        HemoAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        HemoCapabilityRegistrar.register(modEventBus);

        GeckoLib.initialize();
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildContents);
        forgeBus.register(this);

        // RegisterPayloadsEvent fires on the mod bus – register here, not in commonSetup.
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
     * NeoForge 1.21: use ResourceLocation.fromNamespaceAndPath() – the two-arg
     * constructor ResourceLocation.parse(ns, path) is deprecated in 1.21.
     */
    public static ResourceLocation rloc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public void buildContents(BuildCreativeModeTabContentsEvent populator) {
        if (populator.getTabKey() == hemomancytab.getKey()) {
            ItemInit.BASEITEMS.getEntries().forEach(i -> populator.accept(i.get()));
            ItemInit.HANDHELDITEMS.getEntries().forEach(i -> populator.accept(i.get()));
            ItemInit.SPECIALITEMS.getEntries().forEach(i -> populator.accept(i.get()));
            ItemInit.SPAWNEGGS.getEntries().forEach(i -> populator.accept(i.get()));

            var b = BlockInit.getAllBlockEntriesAsStream();
            b.forEach(item -> {
                if (item.get() != BlockInit.attached_gourd_stem.get() && item.get() != BlockInit.gourd_stem.get()
                        && item.get() != BlockInit.active_befouling_ash_trail.get()
                        && item.get() != BlockInit.active_smouldering_ash_trail.get()
                        && item.get() != BlockInit.engram_block.get()) {
                    populator.accept(item.get());
                }
            });
        }
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        EngramTextureCache.loadAll();
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
        initUnstainedStageIcons();
    }

    private void initUnstainedStageIcons() {
        EnumPurityStage.CORRUPTED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.blood_stained_stone.get()));
        EnumPurityStage.TAINTED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.lethean_poppy_wreath.get()));
        EnumPurityStage.CLEANSING.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.pale_distillate.get()));
        EnumPurityStage.ABSOLVED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.tears_of_silthmere.get()));
        EnumPurityStage.PURIFIED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.pallid_icon.get()));
        EnumClarityStage.AWAKENED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.cleansed_blood_crystal_shard.get()));
        EnumClarityStage.DISCERNING.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.silver_chalice.get()));
        EnumClarityStage.VIGILANT.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.pale_silver_ingot.get()));
        EnumClarityStage.RESOLUTE.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.tome_of_the_unstained.get()));
        EnumClarityStage.ENLIGHTENED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.pallid_icon.get()));
    }
}