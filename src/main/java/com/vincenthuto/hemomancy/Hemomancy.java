package com.vincenthuto.hemomancy;

import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.data.book.BloodStructurePageTemplate;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.util.EngramTextureCache;
import com.vincenthuto.hemomancy.compat.curios.CuriosPlugin;
import com.vincenthuto.hemomancy.compat.mna.MnAPlugin;
import com.vincenthuto.hemomancy.compat.mna.MnAPluginClientEvents;
import com.vincenthuto.hemomancy.compat.mna.block.MnAPluginBlockInit;
import com.vincenthuto.hemomancy.compat.mna.entity.MnAPluginEntityInit;
import com.vincenthuto.hemomancy.compat.mna.faction.HarbingerEventHandler;
import com.vincenthuto.hemomancy.compat.mna.item.MnAPluginItemInit;
import com.vincenthuto.hemomancy.compat.mna.ritual.MnAPluginRitualInit;
import com.vincenthuto.hemomancy.compat.mna.spell.MnAPluginManipulationInit;
import com.vincenthuto.hemomancy.compat.mna.spell.MnAPluginSpellInit;
import com.vincenthuto.hemomancy.compat.mna.spell.BloodTitheHandler;
import com.vincenthuto.hemomancy.compat.mna.tile.MnAPluginBlockEntityInit;
import com.vincenthuto.hemomancy.config.HemoConfig;
import com.vincenthuto.hemomancy.config.HemoMnAConfig;
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
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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
    public static final RegistryObject<CreativeModeTab> hemomancytab = CREATIVETABS.register("hemomancytab",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group." + MOD_ID + ".hemomancytab"))
                    .icon(() -> new ItemStack(ItemInit.sanguine_formation.get())).build());
    public static Hemomancy instance;
    public static boolean forcesLoaded = false;
    public ISidedProxy proxy;

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
        GeckoLib.initialize();
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildContents);
        forgeBus.register(this);

        ModList modList = ModList.get();
        if (modList.isLoaded("mna")) {
            LOGGER.info("MNA WAS LOADED");
            // Register MnA-specific config
            ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, HemoMnAConfig.register(), "hemomancy-mna-server.toml");
            forgeBus.addListener(MnAPlugin::onRegisterGuidebooks);
            MnAPluginItemInit.MNAITEMS.register(modEventBus);
            MnAPluginBlockInit.MNABLOCKS.register(modEventBus);
            MnAPluginBlockEntityInit.MNATILES.register(modEventBus);
            MnAPluginManipulationInit.MNA_MANIPS.register(modEventBus);
            modEventBus.addListener(MnAPluginItemInit::buildMnaCompatItemContents);
            modEventBus.addListener(MnAPluginBlockInit::onRegisterItems);
            modEventBus.addListener(MnAPluginBlockInit::buildMnaCompatBlockContents);
            forgeBus.addListener(MnAPlugin::playerInteractAnvil);
            forgeBus.addListener(MnAPlugin::onRunicAnvil);
            modEventBus.addListener(MnAPluginSpellInit::registerSpellBits);
            modEventBus.addListener(MnAPluginRitualInit::registerRitualEffects);
            MnAPluginEntityInit.MNA_ENTITY_TYPES.register(modEventBus);
            modEventBus.addListener(MnAPluginEntityInit::onAttributeCreate);
            // Blood Tithe & Spell↔Manipulation combo event handlers
            forgeBus.addListener(BloodTitheHandler::onCalculateManaCost);
            forgeBus.addListener(BloodTitheHandler::onSpellCast);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> {
                return () -> {
                    modEventBus.addListener(MnAPluginClientEvents::onRegisterSpecialModels);
                    modEventBus.addListener(MnAPluginClientEvents::registerItemColors);
                    modEventBus.addListener(MnAPluginClientEvents::registerModelLayers);
                    modEventBus.addListener(MnAPluginClientEvents::renderEntities);
                    modEventBus.addListener(MnAPluginClientEvents::onClientSetupEvent);

                    forgeBus.addListener(MnAPluginClientEvents::onClientTick);
                };
            });
        }
        if (modList.isLoaded("curios")) {
            LOGGER.info("CURIOS WAS LOADED");
            modEventBus.addListener(CuriosPlugin::initCuriosSlots);
            modEventBus.addListener(CuriosPlugin::clientCurioSetup);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> {
                return () -> {

                };
            });
        }

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


    public static ResourceLocation rloc(String path) {
        return new ResourceLocation(MOD_ID, path);
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

    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {
            BookPlaceboReloadListener.INSTANCE.registerSerializer(Hemomancy.rloc("blood_structure_page"),
                    BloodStructurePageTemplate.SERIALIZER);

            // Blood-Faction Plant Brewing Recipes
            // Only blood-aligned plants brew into hemomancy potions.
            // Unstained plants (puffball, lethean poppy, ghost pipe)
            // deliberately do NOT brew blood-positive effects.
            registerPlantBrewingRecipe(BlockInit.bleeding_heart.get().asItem(),
                    EffectInit.potion_of_sanguine_siphon.get());
            registerPlantBrewingRecipe(BlockInit.infected_fungus.get().asItem(),
                    EffectInit.potion_of_mycorrhizal_mending.get());
            registerPlantBrewingRecipe(BlockInit.stinkhorn_fungus.get().asItem(),
                    EffectInit.potion_of_blood_binding.get());
            registerPlantBrewingRecipe(BlockInit.rafflesia.get().asItem(),
                    EffectInit.potion_of_hemolysis.get());
            registerPlantBrewingRecipe(BlockInit.sarcodes.get().asItem(),
                    EffectInit.potion_of_blood_rush.get());

            // Mob Drop Brewing Recipes
            // Hostile mob drops brew into existing hemomancy potions as alternative catalysts.
            registerDropBrewingRecipe(ItemInit.desiccated_membrane.get(),
                    EffectInit.potion_of_blood_loss.get());
            registerDropBrewingRecipe(ItemInit.molten_clot.get(),
                    EffectInit.potion_of_blood_rush.get());
            registerDropBrewingRecipe(ItemInit.void_ichor.get(),
                    EffectInit.potion_of_echoic_perception.get());
            registerDropBrewingRecipe(ItemInit.frozen_cruor.get(),
                    EffectInit.potion_of_chitinous_bulwark.get());
            registerDropBrewingRecipe(ItemInit.abyssal_ichor.get(),
                    EffectInit.potion_of_luminous_dissipation.get());
            registerDropBrewingRecipe(ItemInit.nerve_bundle.get(),
                    EffectInit.potion_of_serpentine_guile.get());

        });

        HemoEntityPredicates.init();
        SkillPointInit.init();
        ManipulationTreeInit.init();
        initUnstainedStageIcons();
        PacketHandler.registerChannels();

    }

    /** Assigns icon items to each purity and clarity stage for rendering on the progress tree. */
    private void initUnstainedStageIcons() {
        EnumPurityStage.CORRUPTED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.blood_stained_stone.get()));
        EnumPurityStage.TAINTED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.lethean_poppy_wreath.get()));
        EnumPurityStage.CLEANSING.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.lethean_extract.get()));
        EnumPurityStage.ABSOLVED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.tears_of_lethe.get()));
        EnumPurityStage.PURIFIED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.lethe_icon.get()));

        EnumClarityStage.AWAKENED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.cleansed_blood_crystal_shard.get()));
        EnumClarityStage.DISCERNING.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.silver_chalice.get()));
        EnumClarityStage.VIGILANT.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.pale_silver_ingot.get()));
        EnumClarityStage.RESOLUTE.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.tome_of_the_unstained.get()));
        EnumClarityStage.ENLIGHTENED.setIconItem(() -> new net.minecraft.world.item.ItemStack(ItemInit.lethe_icon.get()));
    }

    /** Registers a brewing recipe: Awkward Potion + plant item → mod potion. */
    private static void registerPlantBrewingRecipe(Item plantItem,
            net.minecraft.world.item.alchemy.Potion resultPotion) {
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
                Ingredient.of(plantItem),
                PotionUtils.setPotion(new ItemStack(Items.POTION), resultPotion));
    }

    /** Registers a brewing recipe: Awkward Potion + mob drop → mod potion. */
    private static void registerDropBrewingRecipe(Item dropItem,
            net.minecraft.world.item.alchemy.Potion resultPotion) {
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
                Ingredient.of(dropItem),
                PotionUtils.setPotion(new ItemStack(Items.POTION), resultPotion));
    }

}