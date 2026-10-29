package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.client.render.item.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.SaintSarcophagusRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.SarcophagusPalettes;
import com.vincenthuto.hemomancy.client.render.tile.SectionedBakedModel;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.RingDoorRenderer;
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

import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ModelRegistrar {

    private ModelRegistrar() {
    }

    public static BakedModel bloodAbsorptionModel, bloodProjectionModel;

    /** The baked hand model of a cell-hand item, or null before baking. */
    public static BakedModel cellHandModel(Item item) {
        if (item instanceof BloodAbsorptionItem) return bloodAbsorptionModel;
        if (item instanceof BloodProjectionItem) return bloodProjectionModel;
        return null;
    }

    @SubscribeEvent
    public static void modelRegisterEvent(ModelEvent.RegisterAdditional event) {
        event.register(com.vincenthuto.hemomancy.client.render.layer.player.HematicMicroscopeLayer.LEFT_MODEL);
        event.register(ModelResourceLocation.standalone(Hemomancy.rloc("block/phlebotomists_field_case_lid")));
        event.register(com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.PulsePylonRenderer.RING);
        event.register(com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.PulsePylonRenderer.RING_ANCHOR);
        event.register(com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.PulsePylonRenderer.RING_FROST);
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
        RingDoorRenderer.standaloneModels().forEach(event::register);
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

    /** Wraps each of a block's state models with its further sections, turned to the state's facing. */
    private static void bakeSections(ModelEvent.ModifyBakingResult evt, net.minecraft.world.level.block.Block block,
            net.minecraft.world.level.block.state.properties.DirectionProperty facing,
            List<SectionedBakedModel.Section> sections) {
        for (var state : block.getStateDefinition().getPossibleStates()) {
            ModelResourceLocation location = net.minecraft.client.renderer.block.BlockModelShaper.stateToModelLocation(state);
            BakedModel base = evt.getModels().get(location);
            if (base != null) evt.getModels().put(location, new SectionedBakedModel(base, sections, state.getValue(facing)));
        }
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult evt) {
        // The Saint Sarcophagus is a cell longer than a block model reaches: bake its head section in with each facing
        BakedModel sarcophagusHead = evt.getModels().get(SaintSarcophagusRenderer.part("head"));
        if (sarcophagusHead != null)
            bakeSections(evt, BlockInit.saint_sarcophagus.get(),
                    com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock.FACING,
                    List.of(new SectionedBakedModel.Section(sarcophagusHead, 0, 0,
                            com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SarcophagusGeometry.HEAD_CUT)));
        // ...then dress each Saint's tomb, whole, in its own palette where it has one
        SarcophagusPalettes.bake(evt);
        // The ring door's frame is five cells wide and tall: bake every frame section in with each facing
        List<SectionedBakedModel.Section> frame = RingDoorRenderer.frameSections(evt.getModels()::get);
        if (frame != null)
            bakeSections(evt, BlockInit.ring_door.get(),
                    com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorBlock.FACING, frame);
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
