package com.vincenthuto.hemomancy;

import com.vincenthuto.hemomancy.common.block.inscription.DiscoveryInscriptionLoader;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityRegistrar;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.HemomancyFieldNotes;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberDiscoveryEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.data.book.BloodStructurePageTemplate;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumAffinities;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryLoader;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodInjectionData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorLoader;
import com.vincenthuto.hemomancy.common.succession.SuccessionProfessions;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilLoader;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ClairaudiographCatalogue;
import com.vincenthuto.hemomancy.config.HemoConfig;
import com.vincenthuto.hutoslib.common.book.FieldNotes;
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;
import com.vincenthuto.hutoslib.common.effectsource.EffectSourceInference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Hemomancy.MOD_ID)
public class Hemomancy {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "hemomancy";

    /**
     * NeoForge 1.21: the mod-event bus is injected into the constructor automatically.
     * Only mod-lifecycle listeners are added here; every other handler lives in an
     * {@code @EventBusSubscriber} class.
     */
    public Hemomancy(IEventBus modEventBus) {
        EffectSourceInference.registerPackageOwner("com.vincenthuto.hemomancy.", MOD_ID);
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
        CreativeTabInit.CREATIVETABS.register(modEventBus);
        RecipeInit.SERIALIZERS.register(modEventBus);
        RecipeInit.RECIPE_TYPES.register(modEventBus);
        SoundInit.SOUNDS.register(modEventBus);
        BlockEntityInit.TILES.register(modEventBus);
        ContainerInit.CONTAINERS.register(modEventBus);
        AttributeInit.ATTRIBUTES.register(modEventBus);
        EntityInit.ENTITY_TYPES.register(modEventBus);
        StructureInit.STRUCTURES.register(modEventBus);
        StructureInit.STRUCTURE_PIECES.register(modEventBus);
        VillagerInit.STRUCTURE_PROCESSORS.register(modEventBus);
        LootModifierInit.LOOT_MODIFIERS.register(modEventBus);
        HemoAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        HemoCapabilityRegistrar.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);

        // RegisterPayloadsEvent fires on the mod bus, so register here, not in commonSetup.
        PacketHandler.registerChannels(modEventBus);
    }

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
     * NeoForge 1.21: use ResourceLocation.fromNamespaceAndPath(); the two-arg
     * constructor ResourceLocation.parse(ns, path) is deprecated in 1.21.
     */
    public static ResourceLocation rloc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ScriptoriumAffinities());
        event.addListener(new BloodInjectionData());
        event.addListener(new BloodProfileData());
        event.addListener(new ClairaudiographCatalogue());
        event.addListener(new ItemInquiryLoader());
        event.addListener(new DiscoveryInscriptionLoader());
        event.addListener(new IchorianSigilLoader());
        event.addListener(new CardinalRiteFloorLoader());
        event.addListener(new SuccessionProfessions());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FieldNotes.register(Hemomancy.rloc("libers"), new HemomancyFieldNotes());
            BookPlaceboReloadListener.INSTANCE.registerSerializer(Hemomancy.rloc("blood_structure_page"),
                    BloodStructurePageTemplate.SERIALIZER);
        });
        HemoEntityPredicates.init();
        SkillPointInit.init();
        ManipulationTreeInit.init();
        // These two used to be separate listeners added right after this one; calling them last keeps that order.
        LiberDiscoveryEvents.commonSetup(event);
        UnstainedProgressEvents.commonSetup(event);
    }
}
