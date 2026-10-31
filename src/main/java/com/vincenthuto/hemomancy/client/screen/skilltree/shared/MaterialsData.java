package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.UnlockPredicate;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static catalogue of material / process entries shown in the
 * "Materials &amp; Processes" tabs.
 * <p>
 * Blood-faction entries appear in the {@link HarbingerProgressScreen};
 * Unstained/White Humor entries appear in the {@link UnstainedProgressScreen}.
 */
public final class MaterialsData {

    private static List<MaterialEntry> bloodEntries;

    // ────────────────────────────────────────────────────────────
    //  Blood faction — HarbingerProgressScreen
    // ────────────────────────────────────────────────────────────
    private static List<MaterialEntry> unstainedEntries;

    private MaterialsData() {
    }

    public static List<MaterialEntry> getBloodEntries() {
        if (bloodEntries == null) {
            bloodEntries = buildBloodEntries();
        }
        return bloodEntries;
    }

    // ────────────────────────────────────────────────────────────
    //  Unstained / White Humor faction — UnstainedProgressScreen
    // ────────────────────────────────────────────────────────────

    private static List<MaterialEntry> buildBloodEntries() {
        List<MaterialEntry> list = new ArrayList<>();
        list.add(new MaterialEntry("hematic_microscope", "item.hemomancy.hematic_microscope",
                "material.hemomancy.hematic_microscope.desc",
                "material.hemomancy.category.clinical_tools", () -> new ItemStack(ItemInit.hematic_microscope.get())));
        list.add(new MaterialEntry("phlebotomists_cabinet", "block.hemomancy.phlebotomists_cabinet",
                "material.hemomancy.phlebotomists_cabinet.desc",
                "material.hemomancy.category.clinical_tools", () -> new ItemStack(BlockInit.phlebotomists_cabinet.get())));
        list.add(new MaterialEntry("phlebotomists_field_case", "block.hemomancy.phlebotomists_field_case",
                "material.hemomancy.phlebotomists_field_case.desc",
                "material.hemomancy.category.clinical_tools", () -> new ItemStack(BlockInit.phlebotomists_field_case.get())));
        list.add(new MaterialEntry("clairaudiograph", "block.hemomancy.clairaudiograph",
                "material.hemomancy.clairaudiograph.desc",
                "material.hemomancy.category.clinical_tools", () -> new ItemStack(BlockInit.clairaudiograph.get())));
        list.add(new MaterialEntry("wax_cylinder", "item.hemomancy.wax_cylinder",
                "material.hemomancy.wax_cylinder.desc",
                "material.hemomancy.category.clinical_tools", () -> new ItemStack(ItemInit.wax_cylinder.get())));
        list.add(new MaterialEntry("ambergris_cylinder", "item.hemomancy.ambergris_cylinder",
                "material.hemomancy.ambergris_cylinder.desc",
                "material.hemomancy.category.clinical_tools", () -> new ItemStack(ItemInit.ambergris_cylinder.get())));


        // ── Functional Blocks ──
        list.add(new MaterialEntry("dendritic_distributor", "block.hemomancy.dendritic_distributor",
                "material.hemomancy.dendritic_distributor.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.dendritic_distributor.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("somatic_loom", "block.hemomancy.somatic_loom",
                "material.hemomancy.somatic_loom.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.somatic_loom.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("mnemonic_reliquary", "block.hemomancy.mnemonic_reliquary",
                "material.hemomancy.mnemonic_reliquary.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.mnemonic_reliquary.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("ghastly_alembic", "block.hemomancy.ghastly_alembic",
                "material.hemomancy.ghastly_alembic.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.ghastly_alembic.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("vial_centrifuge", "block.hemomancy.vial_centrifuge",
                "material.hemomancy.vial_centrifuge.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.vial_centrifuge.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("earthen_vein", "block.hemomancy.earthen_vein",
                "material.hemomancy.earthen_vein.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.earthen_vein.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("iron_brazier", "block.hemomancy.iron_brazier",
                "material.hemomancy.iron_brazier.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.iron_brazier.get()),
                true, UnlockPredicate.minDegree(1)));

        list.add(new MaterialEntry("morphling_incubator", "block.hemomancy.morphling_incubator",
                "material.hemomancy.morphling_incubator.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.morphling_incubator.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("scar_station", "material.hemomancy.scar_station",
                "material.hemomancy.scar_station.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.scar_station.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("scrying_podium", "block.hemomancy.scrying_podium",
                "material.hemomancy.scrying_podium.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.scrying_podium.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("scarlet_vanity", "block.hemomancy.scarlet_vanity",
                "material.hemomancy.scarlet_vanity.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.scarlet_vanity.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("fungal_podium", "block.hemomancy.fungal_podium",
                "material.hemomancy.fungal_podium.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.fungal_podium.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("fungal_implantation_pylon", "block.hemomancy.fungal_implantation_pylon",
                "material.hemomancy.fungal_implantation_pylon.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.fungal_implantation_pylon.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("mortal_display", "block.hemomancy.mortal_display",
                "material.hemomancy.mortal_display.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.mortal_display.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("visceral_mirror", "block.hemomancy.visceral_mirror",
                "material.hemomancy.visceral_mirror.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.visceral_mirror.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("blood_crystal_block", "material.hemomancy.blood_crystal_block",
                "material.hemomancy.blood_crystal_block.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.blood_crystal.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("suspended_vivianite", "block.hemomancy.suspended_vivianite",
                "material.hemomancy.suspended_vivianite.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.suspended_vivianite.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("blood_basin", "block.hemomancy.blood_basin",
                "material.hemomancy.blood_basin.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.blood_basin.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("blood_pylon", "block.hemomancy.blood_pylon",
                "material.hemomancy.blood_pylon.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.blood_pylon.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("consecrated_bloodwell", "block.hemomancy.consecrated_bloodwell",
                "material.hemomancy.consecrated_bloodwell.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.consecrated_bloodwell.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("covenant_throne", "block.hemomancy.covenant_throne",
                "material.hemomancy.covenant_throne.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.covenant_throne.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("sanguine_vigil", "block.hemomancy.sanguine_vigil",
                "material.hemomancy.sanguine_vigil.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.sanguine_vigil.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("sanguine_monolith", "block.hemomancy.sanguine_monolith",
                "material.hemomancy.sanguine_monolith.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.sanguine_monolith.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("harbinger_escritoire", "block.hemomancy.harbinger_escritoire",
                "material.hemomancy.harbinger_escritoire.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.harbinger_escritoire.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_cradle", "block.hemomancy.morphling_cradle",
                "material.hemomancy.morphling_cradle.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.morphling_cradle.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("mycelial_crucible", "block.hemomancy.mycelial_crucible",
                "material.hemomancy.mycelial_crucible.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.mycelial_crucible.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("blood_trial_altar", "block.hemomancy.blood_trial_altar",
                "material.hemomancy.blood_trial_altar.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.blood_trial_altar.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("bog_body", "block.hemomancy.bog_body",
                "material.hemomancy.bog_body.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.bog_body.get()),
                true, UnlockPredicate.minDegree(2)));

        // ── Building Blocks ──
        list.add(new MaterialEntry("venous_stone", "block.hemomancy.venous_stone",
                "material.hemomancy.venous_stone.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.venous_stone.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("hematic_iron_block", "block.hemomancy.hematic_iron_block",
                "material.hemomancy.hematic_iron_block.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.hematic_iron_block.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("sanguine_glass", "block.hemomancy.sanguine_glass",
                "material.hemomancy.sanguine_glass.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.sanguine_glass.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("vivianite_glass", "block.hemomancy.vivianite_glass",
                "material.hemomancy.vivianite_glass.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.vivianite_glass.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("conscious_mass", "block.hemomancy.conscious_mass",
                "material.hemomancy.conscious_mass.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.conscious_mass.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("erythrocytic_dirt", "block.hemomancy.erythrocytic_dirt",
                "material.hemomancy.erythrocytic_dirt.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.erythrocytic_dirt.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("hematic_iron_ore", "block.hemomancy.hematic_iron_ore",
                "material.hemomancy.hematic_iron_ore.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.hematic_iron_ore.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("hematic_iron_pillar", "block.hemomancy.hematic_iron_pillar",
                "material.hemomancy.hematic_iron_pillar.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.hematic_iron_pillar.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("chiseled_hematic_iron_block", "material.hemomancy.chiseled_hematic_iron_block",
                "material.hemomancy.chiseled_hematic_iron_block.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.chiseled_hematic_iron_block.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("gilded_venous_stone", "block.hemomancy.gilded_venous_stone",
                "material.hemomancy.gilded_venous_stone.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.gilded_venous_stone.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("polished_venous_stone", "block.hemomancy.polished_venous_stone",
                "material.hemomancy.polished_venous_stone.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.polished_venous_stone.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("polished_venous_stone_bricks", "block.hemomancy.polished_venous_stone_bricks",
                "material.hemomancy.polished_venous_stone_bricks.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.polished_venous_stone_bricks.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("infested_venous_stone", "block.hemomancy.infested_venous_stone",
                "material.hemomancy.infested_venous_stone.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.infested_venous_stone.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("blood_wood_log", "block.hemomancy.blood_wood_log",
                "material.hemomancy.blood_wood_log.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.blood_wood_log.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("blood_wood_planks", "block.hemomancy.blood_wood_planks",
                "material.hemomancy.blood_wood_planks.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.blood_wood_planks.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("erythrocytic_mycelium", "block.hemomancy.erythrocytic_mycelium",
                "material.hemomancy.erythrocytic_mycelium.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.erythrocytic_mycelium.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("infected_stem", "block.hemomancy.infected_stem",
                "material.hemomancy.infected_stem.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.infected_stem.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("hyphae_block", "block.hemomancy.hyphae_block",
                "material.hemomancy.hyphae_block.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.hyphae_block.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("infected_cap", "block.hemomancy.infected_cap",
                "material.hemomancy.infected_cap.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.infected_cap.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("fruiting_infected_cap", "block.hemomancy.fruiting_infected_cap",
                "material.hemomancy.fruiting_infected_cap.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.fruiting_infected_cap.get()),
                true, UnlockPredicate.minDegree(5)));

        // ── Raw Materials ──
        list.add(new MaterialEntry("sanguine_formation", "item.hemomancy.sanguine_formation",
                "material.hemomancy.sanguine_formation.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.sanguine_formation.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("sanguine_condenser", "block.hemomancy.sanguine_condenser",
                "material.hemomancy.sanguine_condenser.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(BlockInit.sanguine_condenser.get()),
                true, UnlockPredicate.minDegree(1)));

        list.add(new MaterialEntry("hematic_iron_scrap", "item.hemomancy.hematic_iron_scrap",
                "material.hemomancy.hematic_iron_scrap.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.hematic_iron_scrap.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("blood_crystal_shard", "item.hemomancy.blood_crystal_shard",
                "material.hemomancy.blood_crystal_shard.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.blood_crystal_shard.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("blood_rock", "item.hemomancy.blood_rock",
                "material.hemomancy.blood_rock.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.blood_rock.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("foul_paste", "item.hemomancy.foul_paste",
                "material.hemomancy.foul_paste.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.foul_paste.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("sanguine_conduit", "item.hemomancy.sanguine_conduit",
                "material.hemomancy.sanguine_conduit.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.sanguine_conduit.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("puppeteering_thread", "item.hemomancy.puppeteering_thread",
                "material.hemomancy.puppeteering_thread.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.puppeteering_thread.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("bleeding_bulb", "item.hemomancy.bleeding_bulb",
                "material.hemomancy.bleeding_bulb.desc",
                "material.hemomancy.category.plants", () -> new ItemStack(ItemInit.bleeding_bulb.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("blood_chum", "item.hemomancy.blood_chum",
                "material.hemomancy.blood_chum.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.blood_chum.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("vivianite_cluster", "item.hemomancy.vivianite_cluster",
                "material.hemomancy.vivianite_cluster.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.vivianite_cluster.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("spore_sac", "item.hemomancy.spore_sac",
                "material.hemomancy.spore_sac.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.spore_sac.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("chitinous_husk", "item.hemomancy.chitinous_husk",
                "material.hemomancy.chitinous_husk.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.chitinous_husk.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("serpent_scale", "item.hemomancy.serpent_scale",
                "material.hemomancy.serpent_scale.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.serpent_scale.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("toxicognath", "item.hemomancy.toxicognath",
                "material.hemomancy.toxicognath.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.toxicognath.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("fargone_proboscis", "item.hemomancy.fargone_proboscis",
                "material.hemomancy.fargone_proboscis.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.fargone_proboscis.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("telson", "item.hemomancy.telson",
                "material.hemomancy.telson.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.telson.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("queens_physogastrism", "item.hemomancy.queens_physogastrism",
                "material.hemomancy.queens_physogastrism.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.queens_physogastrism.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("cuttlefish_chromatophores", "item.hemomancy.cuttlefish_chromatophores",
                "material.hemomancy.cuttlefish_chromatophores.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.cuttlefish_chromatophores.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("sclerotic_oleum", "item.hemomancy.sclerotic_oleum",
                "material.hemomancy.sclerotic_oleum.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.sclerotic_oleum.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("aculeate_vitriol", "item.hemomancy.aculeate_vitriol",
                "material.hemomancy.aculeate_vitriol.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.aculeate_vitriol.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("chromatic_sublimate", "item.hemomancy.chromatic_sublimate",
                "material.hemomancy.chromatic_sublimate.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.chromatic_sublimate.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("venous_pinion", "item.hemomancy.venous_pinion",
                "material.hemomancy.venous_pinion.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.venous_pinion.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("hematic_iron_powder", "item.hemomancy.hematic_iron_powder",
                "material.hemomancy.hematic_iron_powder.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.hematic_iron_powder.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("sanguine_quintessence", "item.hemomancy.sanguine_quintessence",
                "material.hemomancy.sanguine_quintessence.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.sanguine_quintessence.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_stained_stone", "material.hemomancy.blood_stained_stone",
                "material.hemomancy.blood_stained_stone.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.blood_stained_stone.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("fungal_spine", "item.hemomancy.fungal_spine",
                "material.hemomancy.fungal_spine.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.fungal_spine.get()),
                true, UnlockPredicate.minDegree(7)));

        list.add(new MaterialEntry("curved_horn", "item.hemomancy.curved_horn",
                "material.hemomancy.curved_horn.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.curved_horn.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("fervent_husk", "item.hemomancy.fervent_husk",
                "material.hemomancy.fervent_husk.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.fervent_husk.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("vivianite_scalpel", "item.hemomancy.vivianite_scalpel",
                "material.hemomancy.vivianite_scalpel.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.vivianite_scalpel.get()),
                true, UnlockPredicate.minDegree(4)));

        // ── Enzymes ──
        list.add(new MaterialEntry("vivacious_enzyme", "item.hemomancy.vivacious_enzyme",
                "material.hemomancy.vivacious_enzyme.desc",
                "material.hemomancy.category.enzymes", () -> new ItemStack(ItemInit.vivacious_enzyme.get()),
                true, UnlockPredicate.minDegree(1)));

        list.add(new MaterialEntry("ruinous_enzyme", "item.hemomancy.ruinous_enzyme",
                "material.hemomancy.ruinous_enzyme.desc",
                "material.hemomancy.category.enzymes", () -> new ItemStack(ItemInit.ruinous_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("neurotic_enzyme", "item.hemomancy.neurotic_enzyme",
                "material.hemomancy.neurotic_enzyme.desc",
                "material.hemomancy.category.enzymes", () -> new ItemStack(ItemInit.neurotic_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("ferric_enzyme", "item.hemomancy.ferric_enzyme",
                "material.hemomancy.ferric_enzyme.desc",
                "material.hemomancy.category.enzymes", () -> new ItemStack(ItemInit.ferric_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("fervent_enzyme", "item.hemomancy.fervent_enzyme",
                "material.hemomancy.fervent_enzyme.desc",
                "material.hemomancy.category.enzymes", () -> new ItemStack(ItemInit.fervent_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("frigid_enzyme", "item.hemomancy.frigid_enzyme",
                "material.hemomancy.frigid_enzyme.desc",
                "material.hemomancy.category.enzymes", () -> new ItemStack(ItemInit.frigid_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("incandescent_enzyme", "item.hemomancy.incandescent_enzyme",
                "material.hemomancy.incandescent_enzyme.desc",
                "material.hemomancy.category.enzymes", () -> new ItemStack(ItemInit.incandescent_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("umbral_enzyme", "item.hemomancy.umbral_enzyme",
                "material.hemomancy.umbral_enzyme.desc",
                "material.hemomancy.category.enzymes", () -> new ItemStack(ItemInit.umbral_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("recycled_enzyme", "item.hemomancy.recycled_enzyme",
                "material.hemomancy.recycled_enzyme.desc",
                "material.hemomancy.category.enzymes", () -> new ItemStack(ItemInit.recycled_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

        // ── Equipment ──
        list.add(new MaterialEntry("hematic_iron_helm", "material.hemomancy.hematic_iron_helm",
                "material.hemomancy.hematic_iron_helm.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.hematic_iron_helm.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("blood_lust_helm", "material.hemomancy.blood_lust_helm",
                "material.hemomancy.blood_lust_helm.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.blood_lust_helm.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("barbed_blade", "item.hemomancy.barbed_blade",
                "material.hemomancy.barbed_blade.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.barbed_blade.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("chitinite_mace", "item.hemomancy.chitinite_mace",
                "material.hemomancy.chitinite_mace.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.chitinite_mace.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("sanguis_lancea", "item.hemomancy.sanguis_lancea",
                "material.hemomancy.sanguis_lancea.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.sanguis_lancea.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("living_syringe", "item.hemomancy.living_syringe",
                "material.hemomancy.living_syringe.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.living_syringe.get()),
                true, UnlockPredicate.minDegree(1)));

        list.add(new MaterialEntry("hematic_iron_sword", "item.hemomancy.hematic_iron_sword",
                "material.hemomancy.hematic_iron_sword.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.hematic_iron_sword.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("hematic_iron_knapper", "item.hemomancy.hematic_iron_knapper",
                "material.hemomancy.hematic_iron_knapper.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.hematic_iron_knapper.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("living_blade", "item.hemomancy.living_blade", "material.hemomancy.living_blade.desc", "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.living_blade.get()), true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_axe", "item.hemomancy.living_axe", "material.hemomancy.living_axe.desc", "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.living_axe.get()), true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_spear", "item.hemomancy.living_spear", "material.hemomancy.living_spear.desc", "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.living_spear.get()), true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_staff", "item.hemomancy.living_staff",
                "material.hemomancy.living_staff.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.living_staff.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_baghnakh", "item.hemomancy.living_baghnakh", "material.hemomancy.living_baghnakh.desc", "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.living_baghnakh.get()), true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_crossbow", "item.hemomancy.living_crossbow", "material.hemomancy.living_crossbow.desc", "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.living_crossbow.get()), true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_torch", "item.hemomancy.living_torch", "material.hemomancy.living_torch.desc", "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.living_torch.get()), true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("living_flail", "item.hemomancy.living_flail", "material.hemomancy.living_flail.desc", "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.living_flail.get()), true, UnlockPredicate.minDegree(4)));

        // ── Containers ──
        list.add(new MaterialEntry("bloody_flask", "item.hemomancy.bloody_flask",
                "material.hemomancy.bloody_flask.desc",
                "material.hemomancy.category.containers", () -> new ItemStack(ItemInit.bloody_flask.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("bloody_vial", "item.hemomancy.bloody_vial",
                "material.hemomancy.bloody_vial.desc",
                "material.hemomancy.category.containers", () -> new ItemStack(ItemInit.bloody_vial.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("blood_gourd_white", "material.hemomancy.blood_gourd_white",
                "material.hemomancy.blood_gourd_white.desc",
                "material.hemomancy.category.containers", () -> new ItemStack(ItemInit.blood_gourd_white.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("bloody_jug", "item.hemomancy.bloody_jug",
                "material.hemomancy.bloody_jug.desc",
                "material.hemomancy.category.containers", () -> new ItemStack(ItemInit.bloody_jug.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("morphling_jar", "item.hemomancy.morphling_jar",
                "material.hemomancy.morphling_jar.desc",
                "material.hemomancy.category.containers", () -> new ItemStack(ItemInit.morphling_jar.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("vial_rack", "item.hemomancy.vial_rack",
                "material.hemomancy.vial_rack.desc",
                "material.hemomancy.category.containers", () -> new ItemStack(ItemInit.vial_rack.get()),
                true, UnlockPredicate.minDegree(1)));

        // ── Plants ──
        list.add(new MaterialEntry("bleeding_heart", "block.hemomancy.bleeding_heart",
                "material.hemomancy.bleeding_heart.desc",
                "material.hemomancy.category.plants", () -> new ItemStack(BlockInit.bleeding_heart.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("infected_fungus", "block.hemomancy.infected_fungus",
                "material.hemomancy.infected_fungus.desc",
                "material.hemomancy.category.plants", () -> new ItemStack(BlockInit.infected_fungus.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("stinkhorn_fungus", "block.hemomancy.stinkhorn_fungus",
                "material.hemomancy.stinkhorn_fungus.desc",
                "material.hemomancy.category.plants", () -> new ItemStack(BlockInit.stinkhorn_fungus.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("rafflesia", "block.hemomancy.rafflesia",
                "material.hemomancy.rafflesia.desc",
                "material.hemomancy.category.plants", () -> new ItemStack(BlockInit.rafflesia.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("sarcodes", "block.hemomancy.sarcodes",
                "material.hemomancy.sarcodes.desc",
                "material.hemomancy.category.plants", () -> new ItemStack(BlockInit.sarcodes.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("devils_tooth", "block.hemomancy.devils_tooth",
                "material.hemomancy.devils_tooth.desc",
                "material.hemomancy.category.plants", () -> new ItemStack(BlockInit.devils_tooth.get()),
                true, UnlockPredicate.always()));

        // ── Morphlings ──
        list.add(new MaterialEntry("morphling_polyp", "item.hemomancy.morphling_polyp",
                "material.hemomancy.morphling_polyp.desc",
                "material.hemomancy.category.morphlings", () -> new ItemStack(ItemInit.morphling_polyp.get()),
                true, UnlockPredicate.minDegree(3)));

        for (MorphlingStrain strain : MorphlingStrains.ALL) {
            list.add(new MaterialEntry(strain.itemPath(), "material.hemomancy." + strain.itemPath(),
                    "material.hemomancy." + strain.itemPath() + ".desc",
                    "material.hemomancy.category.morphlings", () -> new ItemStack(strain.item().get()),
                    true, UnlockPredicate.minDegree(3)));
        }

        // ── Scars & Patterns ──
        list.add(new MaterialEntry("scar_blank", "item.hemomancy.scar_blank",
                "material.hemomancy.scar_blank.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_blank.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("scar_transcendence", "item.hemomancy.scar_transcendence",
                "material.hemomancy.scar_transcendence.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_transcendence.get()),
                true, UnlockPredicate.minDegree(6)));

        list.add(new MaterialEntry("scar_sol", "item.hemomancy.scar_sol",
                "material.hemomancy.scar_sol.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_sol.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("scar_heart", "item.hemomancy.scar_heart",
                "material.hemomancy.scar_heart.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_heart.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("scar_descendence", "item.hemomancy.scar_descendence",
                "material.hemomancy.scar_descendence.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_descendence.get()),
                true, UnlockPredicate.minDegree(6)));

        list.add(new MaterialEntry("scar_moon", "item.hemomancy.scar_moon",
                "material.hemomancy.scar_moon.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_moon.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("scar_eye", "item.hemomancy.scar_eye",
                "material.hemomancy.scar_eye.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_eye.get()),
                true, UnlockPredicate.minDegree(6)));

        list.add(new MaterialEntry("scar_feral", "item.hemomancy.scar_feral",
                "material.hemomancy.scar_feral.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_feral.get()),
                true, UnlockPredicate.minDegree(4)));

        // ── Tier 1 Scars ──

        list.add(new MaterialEntry("scar_thorn", "item.hemomancy.scar_thorn",
                "material.hemomancy.scar_thorn.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_thorn.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("scar_shade", "item.hemomancy.scar_shade",
                "material.hemomancy.scar_shade.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_shade.get()),
                true, UnlockPredicate.minDegree(4)));

        // ── Tier 2 Scars ──

        list.add(new MaterialEntry("scar_pyre", "item.hemomancy.scar_pyre",
                "material.hemomancy.scar_pyre.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_pyre.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("scar_marrow", "item.hemomancy.scar_marrow",
                "material.hemomancy.scar_marrow.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_marrow.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("scar_blight", "item.hemomancy.scar_blight",
                "material.hemomancy.scar_blight.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_blight.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("scar_rime", "item.hemomancy.scar_rime",
                "material.hemomancy.scar_rime.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_rime.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("scar_flux", "item.hemomancy.scar_flux",
                "material.hemomancy.scar_flux.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_flux.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("scar_halo", "item.hemomancy.scar_halo",
                "material.hemomancy.scar_halo.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_halo.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("scar_anvil", "item.hemomancy.scar_anvil",
                "material.hemomancy.scar_anvil.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_anvil.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("scar_veil", "item.hemomancy.scar_veil",
                "material.hemomancy.scar_veil.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_veil.get()),
                true, UnlockPredicate.minDegree(5)));

        // ── Tier 3 Scars ──

        list.add(new MaterialEntry("scar_phoenix", "item.hemomancy.scar_phoenix",
                "material.hemomancy.scar_phoenix.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_phoenix.get()),
                true, UnlockPredicate.minDegree(6)));

        list.add(new MaterialEntry("scar_wither", "item.hemomancy.scar_wither",
                "material.hemomancy.scar_wither.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_wither.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("scar_glacier", "item.hemomancy.scar_glacier",
                "material.hemomancy.scar_glacier.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_glacier.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("scar_chimera", "item.hemomancy.scar_chimera",
                "material.hemomancy.scar_chimera.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_chimera.get()),
                true, UnlockPredicate.minDegree(6)));

        list.add(new MaterialEntry("scar_corona", "item.hemomancy.scar_corona",
                "material.hemomancy.scar_corona.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_corona.get()),
                true, UnlockPredicate.minDegree(6)));

        list.add(new MaterialEntry("scar_crucible", "item.hemomancy.scar_crucible",
                "material.hemomancy.scar_crucible.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_crucible.get()),
                true, UnlockPredicate.minDegree(6)));

        list.add(new MaterialEntry("scar_oblivion", "item.hemomancy.scar_oblivion",
                "material.hemomancy.scar_oblivion.desc",
                "material.hemomancy.category.scars_patterns", () -> new ItemStack(ItemInit.scar_oblivion.get()),
                true, UnlockPredicate.minDegree(6)));

        list.add(new MaterialEntry("living_axe", "item.hemomancy.living_axe",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.living_axe.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_blade", "item.hemomancy.living_blade",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.living_blade.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_spear", "item.hemomancy.living_spear",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.living_spear.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_baghnakh", "item.hemomancy.living_baghnakh",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.living_baghnakh.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_crossbow", "item.hemomancy.living_crossbow",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.living_crossbow.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_flail", "item.hemomancy.living_flail",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.living_flail.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_torch", "item.hemomancy.living_torch",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.living_torch.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("mnemonic_ambergris", "item.hemomancy.mnemonic_ambergris",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.mnemonic_ambergris.get()),
                true, UnlockPredicate.minDegree(1)));

        list.add(new MaterialEntry("adipocere", "item.hemomancy.adipocere",
                "material.hemomancy.adipocere.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.adipocere.get()),
                true, UnlockPredicate.minDegree(1)));

        list.add(new MaterialEntry("calcified_blood_spine", "material.hemomancy.calcified_blood_spine",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.calcified_blood_spine.get()),
                true, UnlockPredicate.minDegree(1)));

        list.add(new MaterialEntry("chalybeate_sclerite", "item.hemomancy.chalybeate_sclerite",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.chalybeate_sclerite.get()),
                true, UnlockPredicate.minDegree(1)));

        // Generated registry gap-fill catalogue entries

        list.add(new MaterialEntry("active_befouling_ash", "item.hemomancy.active_befouling_ash",
                "material.hemomancy.shared.a_catalogued_reagent_used_in_blood",
                "material.hemomancy.category.alchemy_enzymes", () -> new ItemStack(ItemInit.active_befouling_ash.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("active_smouldering_ash", "item.hemomancy.active_smouldering_ash",
                "material.hemomancy.shared.a_catalogued_reagent_used_in_blood",
                "material.hemomancy.category.alchemy_enzymes", () -> new ItemStack(ItemInit.active_smouldering_ash.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("crimson_lacquer", "item.hemomancy.crimson_lacquer",
                "material.hemomancy.shared.a_catalogued_reagent_used_in_blood",
                "material.hemomancy.category.alchemy_enzymes", () -> new ItemStack(ItemInit.crimson_lacquer.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("engram_block", "block.hemomancy.engram_block",
                "material.hemomancy.shared.a_catalogued_reagent_used_in_blood",
                "material.hemomancy.category.alchemy_enzymes", () -> new ItemStack(ItemInit.engram_stamp.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("enzyme_primer", "item.hemomancy.enzyme_primer",
                "material.hemomancy.shared.a_catalogued_reagent_used_in_blood",
                "material.hemomancy.category.alchemy_enzymes", () -> new ItemStack(ItemInit.enzyme_primer.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("ferric_binder", "item.hemomancy.ferric_binder",
                "material.hemomancy.shared.a_catalogued_reagent_used_in_blood",
                "material.hemomancy.category.alchemy_enzymes", () -> new ItemStack(ItemInit.ferric_binder.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("qliphoth_bloom", "block.hemomancy.qliphoth_bloom",
                "material.hemomancy.qliphoth_bloom.desc",
                "material.hemomancy.category.qliphoth_reagents", () -> new ItemStack(ItemInit.qliphoth_pome.get()),
                false, UnlockPredicate.minDegree(7)));

        list.add(new MaterialEntry("humoral_barometer", "block.hemomancy.humoral_barometer",
                "material.hemomancy.shared.a_structural_or_decorative_bloodcraft_block",
                "material.hemomancy.category.architecture", () -> new ItemStack(BlockInit.humoral_barometer.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("ossuary_clock", "block.hemomancy.ossuary_clock",
                "material.hemomancy.shared.a_structural_or_decorative_bloodcraft_block",
                "material.hemomancy.category.architecture", () -> new ItemStack(BlockInit.ossuary_clock.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("sanguine_pane", "block.hemomancy.sanguine_pane",
                "material.hemomancy.shared.a_structural_or_decorative_bloodcraft_block",
                "material.hemomancy.category.architecture", () -> new ItemStack(BlockInit.sanguine_pane.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("venous_stone_slab", "block.hemomancy.venous_stone_slab",
                "material.hemomancy.shared.a_structural_or_decorative_bloodcraft_block",
                "material.hemomancy.category.architecture", () -> new ItemStack(BlockInit.venous_stone_slab.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("venous_stone_wall", "block.hemomancy.venous_stone_wall",
                "material.hemomancy.shared.a_structural_or_decorative_bloodcraft_block",
                "material.hemomancy.category.architecture", () -> new ItemStack(BlockInit.venous_stone_wall.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("vivianite_pane", "block.hemomancy.vivianite_pane",
                "material.hemomancy.shared.a_structural_or_decorative_bloodcraft_block",
                "material.hemomancy.category.architecture", () -> new ItemStack(BlockInit.vivianite_pane.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("liber_sanguinum", "item.hemomancy.liber_sanguinum",
                "material.hemomancy.liber_sanguinum.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.liber_sanguinum.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("blood_gourd_black", "material.hemomancy.blood_gourd_black",
                "material.hemomancy.shared.a_bloodcraft_vessel_or_gourd_family",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.blood_gourd_black.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("blood_gourd_red", "material.hemomancy.blood_gourd_red",
                "material.hemomancy.shared.a_bloodcraft_vessel_or_gourd_family",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.blood_gourd_red.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("dried_gourd", "item.hemomancy.dried_gourd",
                "material.hemomancy.shared.a_bloodcraft_vessel_or_gourd_family",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.dried_gourd.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("gourd", "block.hemomancy.gourd",
                "material.hemomancy.shared.a_bloodcraft_vessel_or_gourd_family",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(BlockInit.gourd.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("gourd_seeds", "item.hemomancy.gourd_seeds",
                "material.hemomancy.shared.a_bloodcraft_vessel_or_gourd_family",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.gourd_seeds.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("gourd_slice", "item.hemomancy.gourd_slice",
                "material.hemomancy.shared.a_bloodcraft_vessel_or_gourd_family",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.gourd_slice.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("gourd_stew", "item.hemomancy.gourd_stew",
                "material.hemomancy.shared.a_bloodcraft_vessel_or_gourd_family",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.gourd_stew.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("gourdvine_tap", "block.hemomancy.gourdvine_tap",
                "material.hemomancy.gourdvine_tap.desc",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(BlockInit.gourdvine_tap.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("hemorath_rib", "material.hemomancy.hemorath_rib",
                "material.hemomancy.shared.a_bloodcraft_vessel_or_gourd_family",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.hemorath_rib.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("sanguine_salve", "item.hemomancy.sanguine_salve",
                "material.hemomancy.sanguine_salve.desc",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.sanguine_salve.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("scrying_dish", "item.hemomancy.scrying_dish",
                "material.hemomancy.shared.a_bloodcraft_vessel_or_gourd_family",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.scrying_dish.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("vascular_poultice", "item.hemomancy.vascular_poultice",
                "material.hemomancy.vascular_poultice.desc",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.vascular_poultice.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("vitality_chalice", "item.hemomancy.vitality_chalice",
                "material.hemomancy.shared.a_bloodcraft_vessel_or_gourd_family",
                "material.hemomancy.category.gourds_vessels", () -> new ItemStack(ItemInit.vitality_chalice.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("hematic_armature", "block.hemomancy.hematic_armature",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.hematic_armature.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("hematic_iron_chain", "block.hemomancy.hematic_iron_chain",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.hematic_iron_chain.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("hematic_stake", "block.hemomancy.hematic_stake",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.hematic_stake.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("humane_idol", "block.hemomancy.humane_idol",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.humane_idol.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("mason_effigy", "material.hemomancy.mason_effigy",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.mason_effigy.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("mnemonic_candle", "block.hemomancy.mnemonic_candle",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.mnemonic_candle.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("mycelial_lantern", "block.hemomancy.mycelial_lantern",
                "material.hemomancy.mycelial_lantern.desc",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.mycelial_lantern.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("puppeteers_spindle", "material.hemomancy.puppeteers_spindle",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.puppeteers_spindle.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("sanguine_omen", "block.hemomancy.sanguine_omen",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.sanguine_omen.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("serpentine_idol", "block.hemomancy.serpentine_idol",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.serpentine_idol.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("specimen_jar", "block.hemomancy.specimen_jar",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.specimen_jar.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("witness_organ", "item.hemomancy.witness_organ",
                "material.hemomancy.shared.a_ritual_fixture_or_idol_used",
                "material.hemomancy.category.idols_fixtures", () -> new ItemStack(BlockInit.witness_organ.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("annettas_sanguis_lancea", "item.hemomancy.annettas_sanguis_lancea",
                "material.hemomancy.shared.a_living_harbinger_implement_grown_for",
                "material.hemomancy.category.living_implements", () -> new ItemStack(ItemInit.annettas_sanguis_lancea.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("sporitic_thurible", "item.hemomancy.sporitic_thurible",
                "material.hemomancy.shared.a_living_harbinger_implement_grown_for",
                "material.hemomancy.category.living_implements", () -> new ItemStack(ItemInit.sporitic_thurible.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("barbed_boots", "item.hemomancy.barbed_boots",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.barbed_boots.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("barbed_chestplate", "item.hemomancy.barbed_chestplate",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.barbed_chestplate.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("barbed_helm", "item.hemomancy.barbed_helm",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.barbed_helm.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("barbed_leggings", "item.hemomancy.barbed_leggings",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.barbed_leggings.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("barbed_shield", "item.hemomancy.barbed_shield",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.barbed_shield.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("chitinite_boots", "item.hemomancy.chitinite_boots",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.chitinite_boots.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("chitinite_chestplate", "item.hemomancy.chitinite_chestplate",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.chitinite_chestplate.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("chitinite_helm", "item.hemomancy.chitinite_helm",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.chitinite_helm.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("chitinite_leggings", "item.hemomancy.chitinite_leggings",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.chitinite_leggings.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("chitinite_shield", "item.hemomancy.chitinite_shield",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.chitinite_shield.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("prismatic_boots", "item.hemomancy.prismatic_boots",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.prismatic_boots.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("prismatic_chestplate", "item.hemomancy.prismatic_chestplate",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.prismatic_chestplate.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("prismatic_helm", "item.hemomancy.prismatic_helm",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.prismatic_helm.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("prismatic_leggings", "item.hemomancy.prismatic_leggings",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.prismatic_leggings.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("silent_archon_boots", "material.hemomancy.silent_archon_boots",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.silent_archon_boots.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("silent_archon_chestplate", "material.hemomancy.silent_archon_chestplate",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.silent_archon_chestplate.get()),
                true, UnlockPredicate.minDegree(7)));

        list.add(new MaterialEntry("silent_archon_helm", "material.hemomancy.silent_archon_helm",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.silent_archon_helm.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("silent_archon_leggings", "material.hemomancy.silent_archon_leggings",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.armor_vestments", () -> new ItemStack(ItemInit.silent_archon_leggings.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_lust_boots", "item.hemomancy.blood_lust_boots",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.blood_lust_boots.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_lust_chest", "item.hemomancy.blood_lust_chest",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.blood_lust_chest.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_lust_helm_grinning", "material.hemomancy.blood_lust_helm_grinning",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.blood_lust_helm_grinning.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_lust_helm_lodestone", "material.hemomancy.blood_lust_helm_lodestone",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.blood_lust_helm_lodestone.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_lust_helm_tengu", "item.hemomancy.blood_lust_helm_tengu",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.blood_lust_helm_tengu.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_lust_helm_velorum", "material.hemomancy.blood_lust_helm_velorum",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.blood_lust_helm_velorum.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_lust_legs", "item.hemomancy.blood_lust_legs",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.blood_lust_legs.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("edacious_blood_lust_boots", "material.hemomancy.edacious_blood_lust_boots",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.edacious_blood_lust_boots.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("edacious_blood_lust_chest", "material.hemomancy.edacious_blood_lust_chest",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.edacious_blood_lust_chest.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("edacious_blood_lust_helm", "material.hemomancy.edacious_blood_lust_helm",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.edacious_blood_lust_helm.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("edacious_blood_lust_legs", "material.hemomancy.edacious_blood_lust_legs",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.edacious_blood_lust_legs.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("grinning_mask", "item.hemomancy.grinning_mask",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.grinning_mask.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("lodestone_faceplate", "item.hemomancy.lodestone_faceplate",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.lodestone_faceplate.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("phantasmal_blood_lust_boots", "material.hemomancy.phantasmal_blood_lust_boots",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.phantasmal_blood_lust_boots.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("phantasmal_blood_lust_chest", "material.hemomancy.phantasmal_blood_lust_chest",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.phantasmal_blood_lust_chest.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("phantasmal_blood_lust_helm", "material.hemomancy.phantasmal_blood_lust_helm",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.phantasmal_blood_lust_helm.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("phantasmal_blood_lust_legs", "material.hemomancy.phantasmal_blood_lust_legs",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.phantasmal_blood_lust_legs.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("sheolic_blood_lust_boots", "material.hemomancy.sheolic_blood_lust_boots",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.sheolic_blood_lust_boots.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("sheolic_blood_lust_chest", "material.hemomancy.sheolic_blood_lust_chest",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.sheolic_blood_lust_chest.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("sheolic_blood_lust_helm", "material.hemomancy.sheolic_blood_lust_helm",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.sheolic_blood_lust_helm.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("sheolic_blood_lust_legs", "material.hemomancy.sheolic_blood_lust_legs",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.sheolic_blood_lust_legs.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("tengu_mask", "item.hemomancy.tengu_mask",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.tengu_mask.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("velorum_mask", "item.hemomancy.velorum_mask",
                "material.hemomancy.shared.a_wearable_harbinger_implement_tied_to",
                "material.hemomancy.category.masks_vestments", () -> new ItemStack(ItemInit.velorum_mask.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_wood_leaves", "block.hemomancy.blood_wood_leaves",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.blood_wood_leaves.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("calcified_erythrocoral", "block.hemomancy.calcified_erythrocoral",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.calcified_erythrocoral.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("calcified_hyphae", "block.hemomancy.calcified_hyphae",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.calcified_hyphae.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("erythrocoral_block", "block.hemomancy.erythrocoral_block",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.erythrocoral_block.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("erythrocoral_fan", "block.hemomancy.erythrocoral_fan",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.erythrocoral_fan.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("erythrocoral_tendril", "block.hemomancy.erythrocoral_tendril",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.erythrocoral_tendril.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("hematic_iron_bars", "block.hemomancy.hematic_iron_bars",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.hematic_iron_bars.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("hematic_iron_door", "block.hemomancy.hematic_iron_door",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.hematic_iron_door.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("hematic_iron_trapdoor", "block.hemomancy.hematic_iron_trapdoor",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.hematic_iron_trapdoor.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("hemorrhagic_crust", "block.hemomancy.hemorrhagic_crust",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.hemorrhagic_crust.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("hyphae", "block.hemomancy.hyphae",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.hyphae.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("infested_wood", "block.hemomancy.infested_wood",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.infested_wood.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("mycelium_erythrocytic_dirt", "block.hemomancy.mycelium_erythrocytic_dirt",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.mycelium_erythrocytic_dirt.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("sporite_crystal", "block.hemomancy.sporite_crystal",
                "material.hemomancy.shared.a_block_or_growth_from_blood",
                "material.hemomancy.category.myco_realm_blocks", () -> new ItemStack(BlockInit.sporite_crystal.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("constrictor_cord", "item.hemomancy.constrictor_cord",
                "material.hemomancy.shared.an_organic_component_used_in_advanced",
                "material.hemomancy.category.biomaterials", () -> new ItemStack(ItemInit.constrictor_cord.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("dried_leech", "item.hemomancy.dried_leech",
                "material.hemomancy.shared.an_organic_component_used_in_advanced",
                "material.hemomancy.category.biomaterials", () -> new ItemStack(ItemInit.dried_leech.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("erythrocoral_fragment", "item.hemomancy.erythrocoral_fragment",
                "material.hemomancy.shared.an_organic_component_used_in_advanced",
                "material.hemomancy.category.biomaterials", () -> new ItemStack(ItemInit.erythrocoral_fragment.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("scale_grip", "item.hemomancy.scale_grip",
                "material.hemomancy.shared.an_organic_component_used_in_advanced",
                "material.hemomancy.category.biomaterials", () -> new ItemStack(ItemInit.scale_grip.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("swollen_leech", "item.hemomancy.swollen_leech",
                "material.hemomancy.shared.an_organic_component_used_in_advanced",
                "material.hemomancy.category.biomaterials", () -> new ItemStack(ItemInit.swollen_leech.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("tendon_line", "item.hemomancy.tendon_line",
                "material.hemomancy.shared.an_organic_component_used_in_advanced",
                "material.hemomancy.category.biomaterials", () -> new ItemStack(ItemInit.tendon_line.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("hallowed_residuum_hemorath", "material.hemomancy.hallowed_residuum_hemorath",
                "material.hemomancy.shared.a_hallowed_saint_reagent_extracted_for",
                "material.hemomancy.category.hallowed_reagents", () -> new ItemStack(ItemInit.hallowed_residuum_hemorath.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("hallowed_residuum_putriciel", "material.hemomancy.hallowed_residuum_putriciel",
                "material.hemomancy.shared.a_hallowed_saint_reagent_extracted_for",
                "material.hemomancy.category.hallowed_reagents", () -> new ItemStack(ItemInit.hallowed_residuum_putriciel.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("hallowed_residuum_seraphae", "material.hemomancy.hallowed_residuum_seraphae",
                "material.hemomancy.shared.a_hallowed_saint_reagent_extracted_for",
                "material.hemomancy.category.hallowed_reagents", () -> new ItemStack(ItemInit.hallowed_residuum_seraphae.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("hallowed_residuum_velorum", "material.hemomancy.hallowed_residuum_velorum",
                "material.hemomancy.shared.a_hallowed_saint_reagent_extracted_for",
                "material.hemomancy.category.hallowed_reagents", () -> new ItemStack(ItemInit.hallowed_residuum_velorum.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("memory_of_vesper", "item.hemomancy.memory_of_vesper",
                "material.hemomancy.memory_of_vesper.desc",
                "material.hemomancy.category.qliphoth_reagents", () -> new ItemStack(ItemInit.memory_of_vesper.get()),
                true, UnlockPredicate.minDegree(7).and(player -> HemoCapabilityAccess.getInitiatoryDegree(player).hasWitnessedFungalRevelation())));

        list.add(new MaterialEntry("monolith_fragment", "item.hemomancy.monolith_fragment",
                "material.hemomancy.shared.a_late_stage_qliphoth_reagent_used",
                "material.hemomancy.category.qliphoth_reagents", () -> new ItemStack(ItemInit.monolith_fragment.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("monolith_imbued_cloth", "item.hemomancy.monolith_imbued_cloth",
                "material.hemomancy.shared.a_late_stage_qliphoth_reagent_used",
                "material.hemomancy.category.qliphoth_reagents", () -> new ItemStack(ItemInit.monolith_imbued_cloth.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("monolithic_cornerstone", "item.hemomancy.monolithic_cornerstone",
                "material.hemomancy.shared.a_late_stage_qliphoth_reagent_used",
                "material.hemomancy.category.qliphoth_reagents", () -> new ItemStack(ItemInit.monolithic_cornerstone.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("qliphoth_pome", "item.hemomancy.qliphoth_pome",
                "material.hemomancy.qliphoth_pome.desc",
                "material.hemomancy.category.qliphoth_reagents", () -> new ItemStack(ItemInit.qliphoth_pome.get()),
                true, UnlockPredicate.minDegree(7)));

        list.add(new MaterialEntry("qliphoth_seed", "item.hemomancy.qliphoth_seed",
                "material.hemomancy.qliphoth_seed.desc",
                "material.hemomancy.category.qliphoth_reagents", () -> new ItemStack(ItemInit.qliphoth_seed.get()),
                true, UnlockPredicate.minDegree(7)));

        list.add(new MaterialEntry("vicars_consecration_kit", "item.hemomancy.vicars_consecration_kit",
                "material.hemomancy.vicars_consecration_kit.desc",
                "material.hemomancy.category.qliphoth_reagents", () -> new ItemStack(ItemInit.vicars_consecration_kit.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("rhizovitta_communis", "item.hemomancy.rhizovitta_communis",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.rhizovitta_communis.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("antiphonomyces_resonans", "material.hemomancy.antiphonomyces_resonans",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.antiphonomyces_resonans.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("ferric_spores", "item.hemomancy.ferric_spores",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.ferric_spores.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("fervent_spores", "item.hemomancy.fervent_spores",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.fervent_spores.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("frigid_spores", "item.hemomancy.frigid_spores",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.frigid_spores.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("incandescent_spores", "item.hemomancy.incandescent_spores",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.incandescent_spores.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("oculiflora_reticularis", "item.hemomancy.oculiflora_reticularis",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.oculiflora_reticularis.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("neurotic_spores", "item.hemomancy.neurotic_spores",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.neurotic_spores.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("noctifly_agaric", "item.hemomancy.noctifly_agaric",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.noctifly_agaric.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("ruinous_spores", "item.hemomancy.ruinous_spores",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.ruinous_spores.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("putrivora_resolvens", "item.hemomancy.putrivora_resolvens",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.putrivora_resolvens.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("saprovitta_vestigium", "material.hemomancy.saprovitta_vestigium",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.saprovitta_vestigium.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("talaromyces_minus", "item.hemomancy.talaromyces_minus",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.talaromyces_minus.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("cryostroma_perdurans", "item.hemomancy.cryostroma_perdurans",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.cryostroma_perdurans.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("umbral_spores", "item.hemomancy.umbral_spores",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.umbral_spores.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("vivacious_spores", "item.hemomancy.vivacious_spores",
                "material.hemomancy.shared.a_fungal_culture_or_spore_sample",
                "material.hemomancy.category.spores_cultures", () -> new ItemStack(ItemInit.vivacious_spores.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("echo_of_heart", "item.hemomancy.echo_of_heart",
                "material.hemomancy.shared.a_visceral_echo_reflected_through_the",
                "material.hemomancy.category.organ_echoes", () -> new ItemStack(ItemInit.echo_of_heart.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("echo_of_kidneys", "item.hemomancy.echo_of_kidneys",
                "material.hemomancy.shared.a_visceral_echo_reflected_through_the",
                "material.hemomancy.category.organ_echoes", () -> new ItemStack(ItemInit.echo_of_kidneys.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("echo_of_liver", "item.hemomancy.echo_of_liver",
                "material.hemomancy.shared.a_visceral_echo_reflected_through_the",
                "material.hemomancy.category.organ_echoes", () -> new ItemStack(ItemInit.echo_of_liver.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("echo_of_lungs", "item.hemomancy.echo_of_lungs",
                "material.hemomancy.shared.a_visceral_echo_reflected_through_the",
                "material.hemomancy.category.organ_echoes", () -> new ItemStack(ItemInit.echo_of_lungs.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("echo_of_spleen", "item.hemomancy.echo_of_spleen",
                "material.hemomancy.shared.a_visceral_echo_reflected_through_the",
                "material.hemomancy.category.organ_echoes", () -> new ItemStack(ItemInit.echo_of_spleen.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("hematic_iron_chestplate", "item.hemomancy.hematic_iron_chestplate",
                "",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.hematic_iron_chestplate.get()),
                true, UnlockPredicate.minDegree(1)));

        return Collections.unmodifiableList(list);
    }

    public static List<MaterialEntry> getUnstainedEntries() {
        if (unstainedEntries == null) {
            unstainedEntries = buildUnstainedEntries();
        }
        return unstainedEntries;
    }

    private static List<MaterialEntry> buildUnstainedEntries() {
        List<MaterialEntry> list = new ArrayList<>();

        // ── Functional Blocks ──
        list.add(new MaterialEntry("altar_of_cleansing", "block.hemomancy.altar_of_cleansing",
                "material.hemomancy.altar_of_cleansing.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.altar_of_cleansing.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("unstained_podium", "block.hemomancy.unstained_podium",
                "material.hemomancy.unstained_podium.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.unstained_podium.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("suspended_cleansed_blood_crystal", "material.hemomancy.suspended_cleansed_blood_crystal",
                "material.hemomancy.suspended_cleansed_blood_crystal.desc",
                "material.hemomancy.category.functional_blocks", () -> new ItemStack(BlockInit.suspended_cleansed_blood_crystal.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        // ── Building Blocks ──
        list.add(new MaterialEntry("cleansed_stone", "block.hemomancy.cleansed_stone",
                "material.hemomancy.cleansed_stone.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.cleansed_stone.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("pallid_lantern", "block.hemomancy.pallid_lantern",
                "material.hemomancy.pallid_lantern.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.pallid_lantern.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("cleansed_sanguine_glass", "block.hemomancy.cleansed_sanguine_glass",
                "material.hemomancy.cleansed_sanguine_glass.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.cleansed_sanguine_glass.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("pale_silver_block", "block.hemomancy.pale_silver_block",
                "material.hemomancy.pale_silver_block.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.pale_silver_block.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        list.add(new MaterialEntry("pale_silver_bells", "block.hemomancy.pale_silver_bells",
                "material.hemomancy.pale_silver_bells.desc",
                "material.hemomancy.category.building_blocks", () -> new ItemStack(BlockInit.pale_silver_bells.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        // ── Plants ──
        list.add(new MaterialEntry("lethean_poppy", "block.hemomancy.lethean_poppy",
                "material.hemomancy.lethean_poppy.desc",
                "material.hemomancy.category.plants", () -> new ItemStack(BlockInit.lethean_poppy.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("ghost_pipe", "block.hemomancy.ghost_pipe",
                "material.hemomancy.ghost_pipe.desc",
                "material.hemomancy.category.plants", () -> new ItemStack(BlockInit.ghost_pipe.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("puffball_fungus", "block.hemomancy.puffball_fungus",
                "material.hemomancy.puffball_fungus.desc",
                "material.hemomancy.category.plants", () -> new ItemStack(BlockInit.puffball_fungus.get()),
                true, UnlockPredicate.always()));

        // ── Materials ──
        list.add(new MaterialEntry("tears_of_silthmere", "item.hemomancy.tears_of_silthmere",
                "material.hemomancy.tears_of_silthmere.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.tears_of_silthmere.get()),
                true, UnlockPredicate.minPurity(50.0f)));

        list.add(new MaterialEntry("lethean_poppy_wreath", "item.hemomancy.lethean_poppy_wreath",
                "material.hemomancy.lethean_poppy_wreath.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.lethean_poppy_wreath.get()),
                true, UnlockPredicate.minPurity(10.0f)));

        list.add(new MaterialEntry("silver_chalice", "item.hemomancy.silver_chalice",
                "material.hemomancy.silver_chalice.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.silver_chalice.get()),
                true, UnlockPredicate.minClarity(10.0f)));

        list.add(new MaterialEntry("pale_silver_ingot", "item.hemomancy.pale_silver_ingot",
                "material.hemomancy.pale_silver_ingot.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.pale_silver_ingot.get()),
                true, UnlockPredicate.minPurity(15.0f)));

        list.add(new MaterialEntry("pale_distillate", "item.hemomancy.pale_distillate",
                "material.hemomancy.pale_distillate.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.pale_distillate.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        list.add(new MaterialEntry("cleansed_blood_crystal_shard", "item.hemomancy.cleansed_blood_crystal_shard",
                "material.hemomancy.cleansed_blood_crystal_shard.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.cleansed_blood_crystal_shard.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        list.add(new MaterialEntry("pallid_icon", "material.hemomancy.pallid_icon",
                "material.hemomancy.pallid_icon.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.pallid_icon.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("consecrated_copper_ingot", "item.hemomancy.consecrated_copper_ingot",
                "material.hemomancy.consecrated_copper_ingot.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.consecrated_copper_ingot.get()),
                true, UnlockPredicate.minPurity(10.0f)));

        list.add(new MaterialEntry("hemolytic_vial", "item.hemomancy.hemolytic_vial",
                "material.hemomancy.hemolytic_vial.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.hemolytic_vial.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("pale_humor_flask", "item.hemomancy.pale_humor_flask",
                "material.hemomancy.pale_humor_flask.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.pale_humor_flask.get()),
                true, UnlockPredicate.minPurity(15.0f)));

        list.add(new MaterialEntry("draught_of_still_waters", "item.hemomancy.draught_of_still_waters",
                "material.hemomancy.draught_of_still_waters.desc",
                "material.hemomancy.category.materials", () -> new ItemStack(ItemInit.draught_of_still_waters.get()),
                true, UnlockPredicate.minPurity(50.0f)));

        // ── Anti-Blood Materials ──
        list.add(new MaterialEntry("hemolytic_solution", "item.hemomancy.hemolytic_solution",
                "material.hemomancy.hemolytic_solution.desc",
                "material.hemomancy.category.anti_blood", () -> new ItemStack(ItemInit.hemolytic_solution.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("hemolytic_plating", "item.hemomancy.hemolytic_plating",
                "material.hemomancy.hemolytic_plating.desc",
                "material.hemomancy.category.anti_blood", () -> new ItemStack(ItemInit.hemolytic_plating.get()),
                true, UnlockPredicate.minPurity(15.0f)));

        list.add(new MaterialEntry("neutralizing_gasket", "item.hemomancy.neutralizing_gasket",
                "material.hemomancy.neutralizing_gasket.desc",
                "material.hemomancy.category.anti_blood", () -> new ItemStack(ItemInit.neutralizing_gasket.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        list.add(new MaterialEntry("hemolytic_plating_block", "block.hemomancy.hemolytic_plating_block",
                "material.hemomancy.hemolytic_plating_block.desc",
                "material.hemomancy.category.anti_blood", () -> new ItemStack(BlockInit.hemolytic_plating_block.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        list.add(new MaterialEntry("cleansing_hemolymph", "item.hemomancy.cleansing_hemolymph",
                "material.hemomancy.cleansing_hemolymph.desc",
                "material.hemomancy.category.anti_blood", () -> new ItemStack(ItemInit.cleansing_hemolymph.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("lethean_dew", "item.hemomancy.lethean_dew",
                "material.hemomancy.lethean_dew.desc",
                "material.hemomancy.category.anti_blood", () -> new ItemStack(ItemInit.lethean_dew.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        list.add(new MaterialEntry("lethean_brew", "item.hemomancy.lethean_brew",
                "material.hemomancy.lethean_brew.desc",
                "material.hemomancy.category.anti_blood", () -> new ItemStack(ItemInit.lethean_brew.get()),
                true, UnlockPredicate.minPurity(50.0f)));

        // ── Equipment ──
        list.add(new MaterialEntry("unstained_helm", "material.hemomancy.unstained_helm",
                "material.hemomancy.unstained_helm.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.unstained_helm.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("unstained_warhammer", "item.hemomancy.unstained_warhammer",
                "material.hemomancy.unstained_warhammer.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.unstained_warhammer.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("unstained_shield", "item.hemomancy.unstained_shield",
                "material.hemomancy.unstained_shield.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.unstained_shield.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("self_reflection_mirror", "material.hemomancy.self_reflection_mirror",
                "material.hemomancy.self_reflection_mirror.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.self_reflection_mirror.get()),
                true, UnlockPredicate.always()));

        list.add(new MaterialEntry("absolution_dagger", "item.hemomancy.absolution_dagger",
                "material.hemomancy.absolution_dagger.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.absolution_dagger.get()),
                true, UnlockPredicate.minPurity(50.0f)));

        list.add(new MaterialEntry("liber_immaculatus", "item.hemomancy.liber_immaculatus",
                "material.hemomancy.liber_immaculatus.desc",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.liber_immaculatus.get()),
                true, UnlockPredicate.always()));

        // Generated registry gap-fill catalogue entries

        list.add(new MaterialEntry("pale_silver_bars", "block.hemomancy.pale_silver_bars",
                "material.hemomancy.shared.an_unstained_fixture_used_in_cleansing",
                "material.hemomancy.category.cleansing_fixtures", () -> new ItemStack(BlockInit.pale_silver_bars.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("pallid_retort", "block.hemomancy.pallid_retort",
                "material.hemomancy.shared.an_unstained_fixture_used_in_cleansing",
                "material.hemomancy.category.cleansing_fixtures", () -> new ItemStack(BlockInit.pallid_retort.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("pallid_silver_chain", "block.hemomancy.pallid_silver_chain",
                "material.hemomancy.shared.an_unstained_fixture_used_in_cleansing",
                "material.hemomancy.category.cleansing_fixtures", () -> new ItemStack(BlockInit.pallid_silver_chain.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("saint_sarcophagus", "block.hemomancy.saint_sarcophagus",
                "material.hemomancy.shared.an_unstained_fixture_used_in_cleansing",
                "material.hemomancy.category.cleansing_fixtures", () -> new ItemStack(BlockInit.saint_sarcophagus.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("cleansed_sanguine_pane", "block.hemomancy.cleansed_sanguine_pane",
                "material.hemomancy.cleansed_sanguine_pane.desc",
                "material.hemomancy.category.pale_architecture", () -> new ItemStack(BlockInit.cleansed_sanguine_pane.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        list.add(new MaterialEntry("annettas_absolution_dagger", "item.hemomancy.annettas_absolution_dagger",
                "material.hemomancy.shared.an_unstained_tool_weapon_or_vestment",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.annettas_absolution_dagger.get()),
                true, UnlockPredicate.minPurity(50.0f)));

        list.add(new MaterialEntry("pale_silver_bell", "item.hemomancy.pale_silver_bell",
                "material.hemomancy.shared.an_unstained_tool_weapon_or_vestment",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.pale_silver_bell.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("silthmere_glaive", "item.hemomancy.silthmere_glaive",
                "material.hemomancy.shared.an_unstained_tool_weapon_or_vestment",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.silthmere_glaive.get()),
                true, UnlockPredicate.minPurity(50.0f)));

        list.add(new MaterialEntry("unstained_boots", "item.hemomancy.unstained_boots",
                "material.hemomancy.shared.an_unstained_tool_weapon_or_vestment",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.unstained_boots.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("unstained_chestplate", "item.hemomancy.unstained_chestplate",
                "material.hemomancy.shared.an_unstained_tool_weapon_or_vestment",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.unstained_chestplate.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        list.add(new MaterialEntry("unstained_leggings", "item.hemomancy.unstained_leggings",
                "material.hemomancy.shared.an_unstained_tool_weapon_or_vestment",
                "material.hemomancy.category.equipment", () -> new ItemStack(ItemInit.unstained_leggings.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        return Collections.unmodifiableList(list);
    }
}
