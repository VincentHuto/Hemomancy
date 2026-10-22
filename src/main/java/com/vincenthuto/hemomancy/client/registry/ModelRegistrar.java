package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.item.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.SaintSarcophagusRenderer;
import com.vincenthuto.hemomancy.client.render.world.*;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.LowtideRuinObjModels;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ModelRegistrar {

    private ModelRegistrar() {
    }

    public static BakedModel bloodAbsorptionModel, bloodProjectionModel;

    @SubscribeEvent
    public static void modelRegisterEvent(ModelEvent.RegisterAdditional event) {
        event.register(com.vincenthuto.hemomancy.client.render.layer.player.HematicMicroscopeLayer.LEFT_MODEL);
        event.register(ModelResourceLocation.standalone(Hemomancy.rloc("block/phlebotomists_field_case_lid")));
        for (String part : new String[]{"door_solid", "door_glazed", "glass"})
            event.register(ModelResourceLocation.standalone(Hemomancy.rloc("block/phlebotomists_cabinet_" + part)));
        event.register(ModelResourceLocation.standalone(Hemomancy.rloc("block/clairaudiograph_stylus")));
        event.register(ModelResourceLocation.standalone(Hemomancy.rloc("block/clairaudiograph_feed")));
        event.register(ModelResourceLocation.standalone(Hemomancy.rloc("block/ghastly_alembic_condenser")));
        event.register(ModelResourceLocation.standalone(Hemomancy.rloc("block/ghastly_alembic_athanor")));
        for (String part : ResonantForgeRenderer.PART_MODELS)
            event.register(ModelResourceLocation.standalone(Hemomancy.rloc("block/resonant_forge_" + part)));
        for (String part : VialCentrifugeRenderer.PART_MODELS)
            event.register(ModelResourceLocation.standalone(Hemomancy.rloc("block/vial_centrifuge_" + part)));
        for (String part : HematicArmatureRenderer.PART_MODELS)
            event.register(ModelResourceLocation.standalone(Hemomancy.rloc("block/hematic_armature_" + part)));
        for (String part : MnemonicReliquaryRenderer.PART_MODELS)
            event.register(MnemonicReliquaryRenderer.part(part));
        for (String part : SaintSarcophagusRenderer.PART_MODELS)
            event.register(SaintSarcophagusRenderer.part(part));
        for (String part : DendriticDistributorRenderer.PART_MODELS)
            event.register(DendriticDistributorRenderer.part(part));
        event.register(MycelialCrucibleRenderer.CISTERN_BLOOD);
        event.register(MycelialLanternRenderer.GAUGE_BLOOD);
        for (String part : com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.SomaticLoomRenderer.PART_MODELS)
            event.register(com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.SomaticLoomRenderer.part(part));
        event.register(ModelResourceLocation.standalone(Hemomancy.rloc("item/blood_absorption_texture")));
        event.register(ModelResourceLocation.standalone(Hemomancy.rloc("item/blood_projection_texture")));
        CardinalRitePlantedStaffModels.uniqueModelNames().forEach(model ->
                event.register(ModelResourceLocation.standalone(Hemomancy.rloc("item/" + model))));
        LowtideRuinObjModels.register(event);

    }

    @SubscribeEvent
    public static void onModelBake(BakingCompleted evt) {
        bloodAbsorptionModel = evt.getModels()
                .get(ModelResourceLocation.standalone(Hemomancy.rloc("item/blood_absorption_texture")));
        bloodProjectionModel = evt.getModels()
                .get(ModelResourceLocation.standalone(Hemomancy.rloc("item/blood_projection_texture")));
        LowtideRuinObjModels.cache(evt);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult evt) {
        // Wrap all Scar Pattern item models so the overlay layer is shrunk down
        for (DeferredHolder<Item, ? extends Item> entry : ItemInit.BASEITEMS.getEntries()) {
            if (entry.get() instanceof ItemScarPattern) {
                ModelResourceLocation modelLoc = ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(entry.get()));
                BakedModel existing = evt.getModels().get(modelLoc);
                if (existing != null) {
                    evt.getModels().put(modelLoc, new ScarPatternBakedModel(existing));
                }
            }
        }
    }
}
