package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.UnlockPredicate;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
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

        // ── Functional Blocks ──
                                                                                                                                                                                list.add(new MaterialEntry("dendritic_distributor", "Dendritic Distributor",
                "Synaptic Loadout station for saving and applying remembered manipulation patterns.",
                "Functional Blocks", () -> new ItemStack(BlockInit.dendritic_distributor.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("somatic_loom", "Somatic Loom",
                "Weaves blood memories into usable manipulation forms.",
                "Functional Blocks", () -> new ItemStack(BlockInit.somatic_loom.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("mnemonic_reliquary", "Mnemonic Reliquary",
                "Loadout block for equipping and managing blood manipulations.",
                "Functional Blocks", () -> new ItemStack(BlockInit.mnemonic_reliquary.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("ghastly_alembic", "Ghastly Alembic",
                "Processes organic materials into useful blood-craft reagents.",
                "Functional Blocks", () -> new ItemStack(BlockInit.ghastly_alembic.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("vial_centrifuge", "Vial Centrifuge",
                "Separates blood samples into component enzymes.",
                "Functional Blocks", () -> new ItemStack(BlockInit.vial_centrifuge.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("earthen_vein", "Earthen Vein",
                "A living conduit block that transports blood volume.",
                "Functional Blocks", () -> new ItemStack(BlockInit.earthen_vein.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("iron_brazier", "Iron Brazier",
                "Ritual fire source for blood crafting and rites.",
                "Functional Blocks", () -> new ItemStack(BlockInit.iron_brazier.get()),
                true, UnlockPredicate.minDegree(1)));

                                                                                                                                                                                list.add(new MaterialEntry("morphling_incubator", "Morphling Incubator",
                "Incubates morphling polyps into specialised morphlings.",
                "Functional Blocks", () -> new ItemStack(BlockInit.morphling_incubator.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_station", "Cerebral Scar Station",
                "Carves Scar Patterns into blank scars.",
                "Functional Blocks", () -> new ItemStack(BlockInit.scar_station.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("scrying_podium", "Scrying Podium",
                "Reveals hidden information about blood tendencies.",
                "Functional Blocks", () -> new ItemStack(BlockInit.scrying_podium.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("scarlet_vanity", "Scarlet Vanity",
                "Equips bonded harbinger tools through the scars inventory.",
                "Functional Blocks", () -> new ItemStack(BlockInit.scarlet_vanity.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("fungal_podium", "Fungal Podium",
                "Processes fungal spores for scar imprinting.",
                "Functional Blocks", () -> new ItemStack(BlockInit.fungal_podium.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("fungal_implantation_pylon", "Fungal Implantation Pylon",
                "Implants fungal growths for symbiotic effects.",
                "Functional Blocks", () -> new ItemStack(BlockInit.fungal_implantation_pylon.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("mortal_display", "Mortal Display",
                "Displays and manages visceral organ echoes.",
                "Functional Blocks", () -> new ItemStack(BlockInit.mortal_display.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("visceral_mirror", "Visceral Mirror",
                "Reflects the player's internal organ state.",
                "Functional Blocks", () -> new ItemStack(BlockInit.visceral_mirror.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("blood_crystal_block", "Blood Crystal",
                "A crystallised concentration of sanguine energy.",
                "Functional Blocks", () -> new ItemStack(BlockInit.blood_crystal.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("suspended_vivianite", "Suspended Vivianite",
                "A vivianite crystal frame grown from bog-body blood mineral deposits.",
                "Functional Blocks", () -> new ItemStack(BlockInit.suspended_vivianite.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("blood_basin", "Blood Basin",
                "Ritual basin used to pool sanguine energy for blood crafting.",
                "Functional Blocks", () -> new ItemStack(BlockInit.blood_basin.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("blood_pylon", "Blood Pylon",
                "Radiates blood energy outward; links to other blood structures.",
                "Functional Blocks", () -> new ItemStack(BlockInit.blood_pylon.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("consecrated_bloodwell", "Consecrated Bloodwell",
                "A sanctified blood reservoir at the heart of the Founding Fane.",
                "Functional Blocks", () -> new ItemStack(BlockInit.consecrated_bloodwell.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("covenant_throne", "Covenant Throne",
                "The seat of authority within a consecrated Fane.",
                "Functional Blocks", () -> new ItemStack(BlockInit.covenant_throne.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("sanguine_vigil", "Sanguine Vigil",
                "A watchful sentinel structure tied to the Founding Fane boundary.",
                "Functional Blocks", () -> new ItemStack(BlockInit.sanguine_vigil.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("sanguine_monolith", "Sanguine Monolith",
                "Tall obelisk that anchors blood energy in the surrounding area.",
                "Functional Blocks", () -> new ItemStack(BlockInit.sanguine_monolith.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("dictation_table", "Dictation Table",
                "Used to transcribe blood knowledge into engram stamps.",
                "Functional Blocks", () -> new ItemStack(BlockInit.dictation_table.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("morphling_cradle", "Morphling Cradle",
                "Nurtures developing morphling polyps before incubation.",
                "Functional Blocks", () -> new ItemStack(BlockInit.morphling_cradle.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("mycelial_crucible", "Mycelial Crucible",
                "Processes fungal and sanguine matter together for advanced reagents.",
                "Functional Blocks", () -> new ItemStack(BlockInit.mycelial_crucible.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("blood_trial_altar", "Blood Trial Altar",
                "Altar at which the trials of the Saints are conducted.",
                "Functional Blocks", () -> new ItemStack(BlockInit.blood_trial_altar.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("bog_body", "Bog Body",
                "A preserved corpse saturated with sanguine minerals. Yields vivianite.",
                "Functional Blocks", () -> new ItemStack(BlockInit.bog_body.get()),
                true, UnlockPredicate.minDegree(2)));

        // ── Building Blocks ──
                                                                                                                                                                                list.add(new MaterialEntry("venous_stone", "Venous Stone",
                "Dark stone veined with sanguine minerals. Core building material.",
                "Building Blocks", () -> new ItemStack(BlockInit.venous_stone.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("hematic_iron_block", "Hematic Iron Block",
                "Block of refined hematic iron. Sturdy and fire-resistant.",
                "Building Blocks", () -> new ItemStack(BlockInit.hematic_iron_block.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("sanguine_glass", "Sanguine Glass",
                "Translucent glass infused with blood. Decorative building material.",
                "Building Blocks", () -> new ItemStack(BlockInit.sanguine_glass.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("vivianite_glass", "Vivianite Glass",
                "Translucent glass made from blood-aligned vivianite mineral.",
                "Building Blocks", () -> new ItemStack(BlockInit.vivianite_glass.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("conscious_mass", "Conscious Mass",
                "Pulsing organic block formed from concentrated blood energy.",
                "Building Blocks", () -> new ItemStack(BlockInit.conscious_mass.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("erythrocytic_dirt", "Erythrocytic Dirt",
                "Blood-rich soil that supports hemomantic flora.",
                "Building Blocks", () -> new ItemStack(BlockInit.erythrocytic_dirt.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("hematic_iron_ore", "Hematic Iron Ore",
                "Ore vein containing raw hematic iron deposits.",
                "Building Blocks", () -> new ItemStack(BlockInit.hematic_iron_ore.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("hematic_iron_pillar", "Hematic Iron Pillar",
                "Decorative column of refined hematic iron.",
                "Building Blocks", () -> new ItemStack(BlockInit.hematic_iron_pillar.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("chiseled_hematic_iron_block", "Chiseled Hematic Iron",
                "Hematic iron block carved with sanguine motifs.",
                "Building Blocks", () -> new ItemStack(BlockInit.chiseled_hematic_iron_block.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("gilded_venous_stone", "Gilded Venous Stone",
                "Venous stone accented with blood-gilt veining.",
                "Building Blocks", () -> new ItemStack(BlockInit.gilded_venous_stone.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("polished_venous_stone", "Polished Venous Stone",
                "Smooth-ground venous stone. Refined building material.",
                "Building Blocks", () -> new ItemStack(BlockInit.polished_venous_stone.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("polished_venous_stone_bricks", "Polished Venous Stone Bricks",
                "Polished venous stone cut into precise brick shapes.",
                "Building Blocks", () -> new ItemStack(BlockInit.polished_venous_stone_bricks.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("infested_venous_stone", "Infested Venous Stone",
                "Venous stone colonised by fungal hyphae.",
                "Building Blocks", () -> new ItemStack(BlockInit.infested_venous_stone.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("blood_wood_log", "Blood Wood Log",
                "Dense crimson-barked log from the fungal tree of the Sporecrown biome.",
                "Building Blocks", () -> new ItemStack(BlockInit.blood_wood_log.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("blood_wood_planks", "Blood Wood Planks",
                "Planks milled from blood wood logs. Richly red grain.",
                "Building Blocks", () -> new ItemStack(BlockInit.blood_wood_planks.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("erythrocytic_mycelium", "Erythrocytic Mycelium",
                "Blood-rich mycelium spread. The hyphae web pulses faintly.",
                "Building Blocks", () -> new ItemStack(BlockInit.erythrocytic_mycelium.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("infected_stem", "Infected Stem",
                "Woody stem of the infected fungal tree, riddled with spore channels.",
                "Building Blocks", () -> new ItemStack(BlockInit.infected_stem.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("hyphae_block", "Hyphae Block",
                "Compressed hyphae. Springy and slightly warm to the touch.",
                "Building Blocks", () -> new ItemStack(BlockInit.hyphae_block.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("infected_cap", "Infected Cap",
                "The cap layer of the infected fungal tree. Sporulates gently.",
                "Building Blocks", () -> new ItemStack(BlockInit.infected_cap.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("fruiting_infected_cap", "Fruiting Infected Cap",
                "An infected cap in active fruiting phase. Denser with spores.",
                "Building Blocks", () -> new ItemStack(BlockInit.fruiting_infected_cap.get()),
                true, UnlockPredicate.minDegree(5)));

        // ── Raw Materials ──
                                                                                                                                                                                list.add(new MaterialEntry("sanguine_formation", "Sanguine Formation",
                "Raw blood crystal; basic crafting ingredient.",
                "Materials", () -> new ItemStack(ItemInit.sanguine_formation.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("hematic_iron_scrap", "Hematic Iron Scrap",
                "Scrap metal infused with blood iron. Smelt into ingots.",
                "Materials", () -> new ItemStack(ItemInit.hematic_iron_scrap.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("blood_crystal_shard", "Blood Crystal Shard",
                "A shard of crystallised blood energy.",
                "Materials", () -> new ItemStack(ItemInit.blood_crystal_shard.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("blood_rock", "Blood Rock",
                "Dense stone saturated with sanguine energy.",
                "Materials", () -> new ItemStack(ItemInit.blood_rock.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("foul_paste", "Foul Paste",
                "A vile mixture used in blood crafting recipes.",
                "Materials", () -> new ItemStack(ItemInit.foul_paste.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("sanguine_conduit", "Sanguine Conduit",
                "Channels blood energy between devices and structures.",
                "Materials", () -> new ItemStack(ItemInit.sanguine_conduit.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("puppeteering_thread", "Puppeteering Thread",
                "Fine threads used in manipulation memory weaving.",
                "Materials", () -> new ItemStack(ItemInit.puppeteering_thread.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("bleeding_bulb", "Bleeding Bulb",
                "Primary bleeding-heart flora reagent for hemomantic recipes.",
                "Plants", () -> new ItemStack(ItemInit.bleeding_bulb.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("blood_chum", "Blood Chum",
                "Bloody bait mass that briefly chums nearby fishing waters without improving treasure quality.",
                "Materials", () -> new ItemStack(ItemInit.blood_chum.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("vivianite_cluster", "Vivianite Cluster",
                "A mineral cluster with faint sanguine resonance.",
                "Materials", () -> new ItemStack(ItemInit.vivianite_cluster.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("spore_sac", "Spore Sac",
                "Collected fungal spores for various applications.",
                "Materials", () -> new ItemStack(ItemInit.spore_sac.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("chitinous_husk", "Chitinous Husk",
                "Hardened exoskeletal material from blood creatures.",
                "Materials", () -> new ItemStack(ItemInit.chitinous_husk.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("serpent_scale", "Serpent Scale",
                "Scale from a blood serpent. Used in advanced crafting.",
                "Materials", () -> new ItemStack(ItemInit.serpent_scale.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("toxicognath", "Toxicognath",
                "Venom-bearing mouthpart taken from a Venom-Rib Centipede.",
                "Materials", () -> new ItemStack(ItemInit.toxicognath.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("fargone_proboscis", "Fargone Proboscis",
                "A blood-drinking proboscis cut from a Fargone.",
                "Materials", () -> new ItemStack(ItemInit.fargone_proboscis.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("telson", "Telson",
                "A desiccant scorpion stinger and venom bulb prepared for corrosive armor baths.",
                "Materials", () -> new ItemStack(ItemInit.telson.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("queens_physogastrism", "Queen's Physogastrism",
                "Distended royal brood tissue used in hardening armor oleums.",
                "Materials", () -> new ItemStack(ItemInit.queens_physogastrism.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("cuttlefish_chromatophores", "Cuttlefish Chromatophores",
                "Pigment organs from a Prism Cuttle, still flashing with false color.",
                "Materials", () -> new ItemStack(ItemInit.cuttlefish_chromatophores.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("sclerotic_oleum", "Sclerotic Oleum",
                "Armor-hardening quench oil for Chitinite sidegrades.",
                "Materials", () -> new ItemStack(ItemInit.sclerotic_oleum.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("aculeate_vitriol", "Aculeate Vitriol",
                "Biting corrosive infusion for Barbed sidegrades.",
                "Materials", () -> new ItemStack(ItemInit.aculeate_vitriol.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("chromatic_sublimate", "Chromatic Sublimate",
                "Prismatic control-sheen coating for Prismatic sidegrades.",
                "Materials", () -> new ItemStack(ItemInit.chromatic_sublimate.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("venous_pinion", "Venous Pinion",
                "Rare balancing feather from a Venous Strider, used for emergency-fall sabatons.",
                "Materials", () -> new ItemStack(ItemInit.venous_pinion.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("hematic_iron_powder", "Hematic Iron Powder",
                "Ground hematic iron. Intermediate step before smelting into ingots.",
                "Materials", () -> new ItemStack(ItemInit.hematic_iron_powder.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("sanguine_quintessence", "Sanguine Quintessence",
                "The purest distillate of blood energy. Required for high-tier workings.",
                "Materials", () -> new ItemStack(ItemInit.sanguine_quintessence.get()),
                true, UnlockPredicate.minDegree(5)));

//        list.add(new MaterialEntry("hematic_field_ink", "Hematic Field Ink",
//                "Blood-infused ink used for writing patterns and engrams.",
//                "Materials", () -> new ItemStack(ItemInit.hematic_field_ink.get()),
//                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("blood_stained_stone", "Blood-Stained Stone",
                "Common stone saturated with sanguine residue.",
                "Materials", () -> new ItemStack(ItemInit.blood_stained_stone.get()),
                true, UnlockPredicate.always()));

//        list.add(new MaterialEntry("desiccated_membrane", "Desiccated Membrane",
//                "Dried organic membrane harvested from blood constructs.",
//                "Materials", () -> new ItemStack(ItemInit.desiccated_membrane.get()),
//                true, UnlockPredicate.minDegree(3)));
//
//        list.add(new MaterialEntry("molten_scab", "Molten Scab",
//                "Hardened blood residue with a high heat signature.",
//                "Materials", () -> new ItemStack(ItemInit.molten_scab.get()),
//                true, UnlockPredicate.minDegree(3)));
//
//        list.add(new MaterialEntry("frozen_clot", "Frozen Clot",
//                "A coagulated mass of blood locked in a cold stasis.",
//                "Materials", () -> new ItemStack(ItemInit.frozen_clot.get()),
//                true, UnlockPredicate.minDegree(3)));
//
//        list.add(new MaterialEntry("abyssal_ichor", "Abyssal Ichor",
//                "Dark ichor drawn from deeper blood constructs. Tenebris-aligned.",
//                "Materials", () -> new ItemStack(ItemInit.abyssal_ichor.get()),
//                true, UnlockPredicate.minDegree(5)));
//
//        list.add(new MaterialEntry("void_ichor", "Void Ichor",
//                "Ichor from the periphery of blood space. Reacts violently with light.",
//                "Materials", () -> new ItemStack(ItemInit.void_ichor.get()),
//                true, UnlockPredicate.minDegree(6)));
//
//        list.add(new MaterialEntry("nerve_bundle", "Nerve Bundle",
//                "Extracted nerve cluster. Carries residual Ductilis tendency.",
//                "Materials", () -> new ItemStack(ItemInit.nerve_bundle.get()),
//                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("fungal_spine", "Fungal Spine",
                "Hardened spine from a fungal blood creature. Used in crafting.",
                "Materials", () -> new ItemStack(ItemInit.fungal_spine.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("curved_horn", "Curved Horn",
                "A curved horn from a blood-aligned creature. Sturdy and resonant.",
                "Materials", () -> new ItemStack(ItemInit.curved_horn.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("fervent_husk", "Fervent Husk",
                "The shed exoskeleton of a Flammeus-aligned creature.",
                "Materials", () -> new ItemStack(ItemInit.fervent_husk.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("vivianite_scalpel", "Vivianite Scalpel",
                "A precision blade of vivianite crystal. Required to begin organ extraction at the Visceral Mirror. Also doubles vivianite yield when harvesting Bog Bodies.",
                "Materials", () -> new ItemStack(ItemInit.vivianite_scalpel.get()),
                true, UnlockPredicate.minDegree(4)));

        // ── Enzymes ──
                                                                                                                                                                                list.add(new MaterialEntry("vivacious_enzyme", "Vivacious Enzyme",
                "Animus-aligned enzyme. Boosts vitality effects.",
                "Enzymes", () -> new ItemStack(ItemInit.vivacious_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("ruinous_enzyme", "Ruinous Enzyme",
                "Mortem-aligned enzyme. Enhances destructive abilities.",
                "Enzymes", () -> new ItemStack(ItemInit.ruinous_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("neurotic_enzyme", "Neurotic Enzyme",
                "Ductilis-aligned enzyme. Sharpens mental manipulation.",
                "Enzymes", () -> new ItemStack(ItemInit.neurotic_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("ferric_enzyme", "Ferric Enzyme",
                "Ferric-aligned enzyme. Strengthens metallic transmutation.",
                "Enzymes", () -> new ItemStack(ItemInit.ferric_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("fervent_enzyme", "Fervent Enzyme",
                "Flammeus-aligned enzyme. Amplifies heat and combustion effects.",
                "Enzymes", () -> new ItemStack(ItemInit.fervent_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("frigid_enzyme", "Frigid Enzyme",
                "Congeatio-aligned enzyme. Channels cold and crystallisation.",
                "Enzymes", () -> new ItemStack(ItemInit.frigid_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("incandescent_enzyme", "Incandescent Enzyme",
                "Lux-aligned enzyme. Focuses radiant and illuminating properties.",
                "Enzymes", () -> new ItemStack(ItemInit.incandescent_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("umbral_enzyme", "Umbral Enzyme",
                "Tenebris-aligned enzyme. Enhances shadow and concealment workings.",
                "Enzymes", () -> new ItemStack(ItemInit.umbral_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("recycled_enzyme", "Recycled Enzyme",
                "Reclaimed enzyme matter. Weak but versatile across tendency types.",
                "Enzymes", () -> new ItemStack(ItemInit.recycled_enzyme.get()),
                true, UnlockPredicate.minDegree(2)));

        // ── Equipment ──
                                                                                                                list.add(new MaterialEntry("hematic_iron_helm", "Hematic Iron Armor",
                "Fire-resistant armor forged from blood-infused iron.",
                "Equipment", () -> new ItemStack(ItemInit.hematic_iron_helm.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("blood_lust_helm", "Blood Lust Armor",
                "Savage armor that empowers bloodthirsty combat.",
                "Equipment", () -> new ItemStack(ItemInit.blood_lust_helm.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("barbed_blade", "Barbed Blade",
                "Cruel living weapon lined with blood-drawing barbs.",
                "Equipment", () -> new ItemStack(ItemInit.barbed_blade.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("chitinite_mace", "Chitinite Mace",
                "Heavy mace crafted from hardened chitinite.",
                "Equipment", () -> new ItemStack(ItemInit.chitinite_mace.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("sanguis_lancea", "Sanguis Lancea",
                "A living lance that drains blood on hit.",
                "Equipment", () -> new ItemStack(ItemInit.sanguis_lancea.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("living_syringe", "Living Syringe",
                "Extracts and injects blood with surgical precision.",
                "Equipment", () -> new ItemStack(ItemInit.living_syringe.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("hematic_iron_sword", "Hematic Iron Sword",
                "A sword forged from hematic iron. Bleeds the target on a clean strike.",
                "Equipment", () -> new ItemStack(ItemInit.hematic_iron_sword.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("hematic_iron_knapper", "Hematic Iron Knapper",
                "A knapping tool for shaping hematic iron into finer components.",
                "Equipment", () -> new ItemStack(ItemInit.hematic_iron_knapper.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("living_blade", "Living Blade", "A blade of animate sanguine tissue. Grows sharper as blood is drawn.", "Equipment", () -> new ItemStack(ItemInit.living_blade.get()), true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_axe", "Living Axe", "An axe formed from living blood-iron. Severs and drinks in one motion.", "Equipment", () -> new ItemStack(ItemInit.living_axe.get()), true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_spear", "Living Spear", "A spear that remembers every wound it has dealt.", "Equipment", () -> new ItemStack(ItemInit.living_spear.get()), true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("living_staff", "Living Staff",
                "A staff of animate sanguine matter. Channels manipulation cost.",
                "Equipment", () -> new ItemStack(ItemInit.living_staff.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_baghnakh", "Living Baghnakh", "Clawed gauntlet of living tissue. Tears and drains on each strike.", "Equipment", () -> new ItemStack(ItemInit.living_baghnakh.get()), true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_crossbow", "Living Crossbow", "A crossbow grown from blood-iron sinew. Fires bolts of coagulated blood.", "Equipment", () -> new ItemStack(ItemInit.living_crossbow.get()), true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_torch", "Living Torch", "A living brand that keeps fervent blood burning at its crown.", "Equipment", () -> new ItemStack(ItemInit.living_torch.get()), true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("living_flail", "Living Flail", "A cold chained head of living tissue that drags heat from whatever it strikes.", "Equipment", () -> new ItemStack(ItemInit.living_flail.get()), true, UnlockPredicate.minDegree(4)));

        // ── Containers ──
                                                                                                                                                                                list.add(new MaterialEntry("bloody_flask", "Bloody Flask",
                "Portable blood container. Holds 250 mL.",
                "Containers", () -> new ItemStack(ItemInit.bloody_flask.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("bloody_vial", "Bloody Vial",
                "Small vial for blood samples and centrifuging.",
                "Containers", () -> new ItemStack(ItemInit.bloody_vial.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("blood_gourd_white", "Blood Gourd",
                "Organic gourd cultivated to store blood naturally.",
                "Containers", () -> new ItemStack(ItemInit.blood_gourd_white.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("bloody_jug", "Bloody Jug",
                "Larger blood storage vessel. Holds 1000 mL.",
                "Containers", () -> new ItemStack(ItemInit.bloody_jug.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                                                                                list.add(new MaterialEntry("morphling_jar", "Morphling Jar",
                "Captures and stores a living morphling.",
                "Containers", () -> new ItemStack(ItemInit.morphling_jar.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                                                list.add(new MaterialEntry("vial_rack", "Vial Rack",
                "Holds multiple bloody vials in organised storage.",
                "Containers", () -> new ItemStack(ItemInit.vial_rack.get()),
                true, UnlockPredicate.minDegree(2)));

        // ── Plants ──
                                                                                                                                                                                list.add(new MaterialEntry("bleeding_heart", "Bleeding Heart",
                "Crimson flower that bleeds on harvesting. Brews into blood-positive potions.",
                "Plants", () -> new ItemStack(BlockInit.bleeding_heart.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("infected_fungus", "Infected Fungus",
                "A fungus colonised by the Fungal Entity's influence. Key brewing reagent.",
                "Plants", () -> new ItemStack(BlockInit.infected_fungus.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("stinkhorn_fungus", "Stinkhorn Fungus",
                "Pungent fruiting body. Its spores carry a subtle sanguine resonance.",
                "Plants", () -> new ItemStack(BlockInit.stinkhorn_fungus.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("rafflesia", "Rafflesia",
                "A rare bloom found on particularly pure calcified hypae",
                "Plants", () -> new ItemStack(BlockInit.rafflesia.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("sarcodes", "Sarcodes",
                "A crimson snow plant that grows where blood has soaked the earth.",
                "Plants", () -> new ItemStack(BlockInit.sarcodes.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("devils_tooth", "Devil's Tooth",
                "Tooth-shaped fungus dripping with a reddish fluid. Caustic on contact.",
                "Plants", () -> new ItemStack(BlockInit.devils_tooth.get()),
                true, UnlockPredicate.always()));

        // ── Morphlings ──
                                                                                                                                                                                list.add(new MaterialEntry("morphling_polyp", "Morphling Polyp",
                "The base larval form of a morphling. Place in the Morphling Incubator to develop.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_polyp.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                        list.add(new MaterialEntry("morphling_deadmans_purse", "Morphling: Deadman's Purse",
                "A fungal strain that preserves the blood-siphon role of the old leech cluster.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_deadmans_purse.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                        list.add(new MaterialEntry("morphling_gravecap", "Morphling: Gravecap",
                "A fungal strain shaped around decay, spore healing, and corpse-bloom pressure.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_gravecap.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                        list.add(new MaterialEntry("morphling_witchs_ear", "Morphling: Witch's Ear",
                "A fungal strain that keeps the old nocturnal sensing and glide profile.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_witchs_ear.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                        list.add(new MaterialEntry("morphling_foxfire", "Morphling: Foxfire",
                "A luminous fungal strain that flashes and blinds like the former cuttlefish form.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_foxfire.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                        list.add(new MaterialEntry("morphling_bootlace", "Morphling: Bootlace",
                "A threadlike fungal strain retaining the former web, climb, and tether profile.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_bootlace.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                        list.add(new MaterialEntry("morphling_irontooth", "Morphling: Irontooth",
                "A dense fungal strain that preserves the old burrowing and tremor-sense profile.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_irontooth.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                        list.add(new MaterialEntry("morphling_emberfang", "Morphling: Emberfang",
                "A heated fungal strain that keeps the venom-strike profile of the old serpent form.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_emberfang.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                                                        list.add(new MaterialEntry("morphling_winter_shroud", "Morphling: Winter Shroud",
                "A cold fungal strain that preserves the old segmented resilience profile.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_winter_shroud.get()),
                true, UnlockPredicate.minDegree(3)));

        // ── Scars & Patterns ──
                                                                                                                                                                                list.add(new MaterialEntry("scar_blank", "Blank Scar",
                "An uncarved scar tablet. Base material for all scar inscriptions. Requires Adept rank.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_blank.get()),
                true, UnlockPredicate.minDegree(4)));

//        list.add(new MaterialEntry("scar_binder", "Scar Binder",
//                "Leather-bound case that stores Scar Patterns and carved scars.",
//                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_binder.get()),
//                true, UnlockPredicate.minDegree(4)));
//
//        list.add(new MaterialEntry("scar_binder_upgraded", "Scar Binder (Upgraded)",
//                "Expanded scar binder with additional storage capacity.",
//                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_binder_upgraded.get()),
//                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_transcendence", "Scar of Transcendence",
                "Lux-aligned scar (Tier III). The light becomes a weight; stillness becomes its price.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_transcendence.get()),
                true, UnlockPredicate.minDegree(6)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_sol", "Scar of Sol",
                "Flammeus-aligned scar (Tier II). The inner fire burns brighter — and burns away the armour.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_sol.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_heart", "Scar of the Heart",
                "Animus-aligned scar (Tier I). More life in the veins, less blood to spare.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_heart.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_descendence", "Scar of Descendence",
                "Congeatio-aligned scar (Tier III). The fastest practitioner is the one who never swings.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_descendence.get()),
                true, UnlockPredicate.minDegree(6)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_moon", "Scar of the Moon",
                "Tenebris-aligned scar (Tier II). Shadow-swift, but the strike grows lighter.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_moon.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_eye", "Scar of the Eye",
                "Tenebris-aligned scar (Tier III). The eyes never close. They never stop drinking.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_eye.get()),
                true, UnlockPredicate.minDegree(6)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_feral", "Scar of the Feral",
                "Ductilis-aligned scar (Tier I). Awakens primal nervous pathways — but the skin thins.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_feral.get()),
                true, UnlockPredicate.minDegree(4)));

        // ── Tier 1 Scars ──

                                                                                                                                                                                list.add(new MaterialEntry("scar_thorn", "Scar of the Thorn",
                "Ferric-aligned scar (Tier I). Iron hardens the blood; iron slows the feet.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_thorn.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_shade", "Scar of the Shade",
                "Tenebris-aligned scar (Tier I). The shadow quickens; the fist quiets.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_shade.get()),
                true, UnlockPredicate.minDegree(4)));

        // ── Tier 2 Scars ──

                                                                                                                                                                                list.add(new MaterialEntry("scar_pyre", "Scar of the Pyre",
                "Flammeus-aligned scar (Tier I). Conviction sharpens the edge — and strips away the guard.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_pyre.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_marrow", "Scar of Marrow",
                "Animus-aligned scar (Tier II). Deep reserves of life, at the cost of blood and speed.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_marrow.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_blight", "Scar of Blight",
                "Mortem-aligned scar (Tier I). Channels rot outward — but the toxin does not always obey.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_blight.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_rime", "Scar of Rime",
                "Congeatio-aligned scar (Tier I). Frost in the legs, frost in the hands.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_rime.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_flux", "Scar of Flux",
                "Ductilis-aligned scar (Tier II). The strike accelerates; the skin thins further.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_flux.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_halo", "Scar of the Halo",
                "Lux-aligned scar (Tier I). Radiance hardens the body; it weighs upon the step.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_halo.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_anvil", "Scar of the Anvil",
                "Ferric-aligned scar (Tier II). The blood's iron thickens the shell — and the pace.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_anvil.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_veil", "Scar of the Veil",
                "Lux-aligned scar (Tier II). Light-forged toughness, purchased with sluggish feet.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_veil.get()),
                true, UnlockPredicate.minDegree(5)));

        // ── Tier 3 Scars ──

                                                                                                                                                                                list.add(new MaterialEntry("scar_phoenix", "Scar of the Phoenix",
                "Animus-aligned scar (Tier III). Immolation and rebirth — the blood pays the fare.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_phoenix.get()),
                true, UnlockPredicate.minDegree(6)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_wither", "Scar of Withering",
                "Mortem-aligned scar (Tier II). Entropy flows outward at the cost of the vessel's own vitality.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_wither.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_glacier", "Scar of the Glacier",
                "Congeatio-aligned scar (Tier II). Commanding the ice is slow work.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_glacier.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_chimera", "Scar of the Chimera",
                "Ductilis-aligned scar (Tier III). Protean speed and lethal chains — but flesh and HP bleed away.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_chimera.get()),
                true, UnlockPredicate.minDegree(6)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_corona", "Scar of the Corona",
                "Flammeus-aligned scar (Tier III). The blood runs so hot the armour blisters off.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_corona.get()),
                true, UnlockPredicate.minDegree(6)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_crucible", "Scar of the Crucible",
                "Ferric-aligned scar (Tier III). The ultimate forge-trial leaves the body immovable.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_crucible.get()),
                true, UnlockPredicate.minDegree(6)));

                                                                                                                                                                                list.add(new MaterialEntry("scar_oblivion", "Scar of Oblivion",
                "Mortem-aligned scar (Tier III). The void consumes both enemies and the practitioner's own blood.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_oblivion.get()),
                true, UnlockPredicate.minDegree(6)));

        list.add(new MaterialEntry("living_axe", "Living Axe",
                "",
                "Materials", () -> new ItemStack(ItemInit.living_axe.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_blade", "Living Blade",
                "",
                "Materials", () -> new ItemStack(ItemInit.living_blade.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_spear", "Living Spear",
                "",
                "Materials", () -> new ItemStack(ItemInit.living_spear.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_baghnakh", "Living Baghnakh",
                "",
                "Materials", () -> new ItemStack(ItemInit.living_baghnakh.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_crossbow", "Living Crossbow",
                "",
                "Materials", () -> new ItemStack(ItemInit.living_crossbow.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_flail", "Living Flail",
                "",
                "Materials", () -> new ItemStack(ItemInit.living_flail.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_torch", "Living Torch",
                "",
                "Materials", () -> new ItemStack(ItemInit.living_torch.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                                        list.add(new MaterialEntry("mnemonic_ambergris", "Mnemonic Ambergris",
                "",
                "Materials", () -> new ItemStack(ItemInit.mnemonic_ambergris.get()),
                true, UnlockPredicate.minDegree(1)));

                                                                                                                                list.add(new MaterialEntry("calcified_blood_spine", "Calcified Blood Spine",
                "",
                "Materials", () -> new ItemStack(ItemInit.calcified_blood_spine.get()),
                true, UnlockPredicate.minDegree(1)));

                                                                                                                                list.add(new MaterialEntry("chalybeate_sclerite", "Chalybeate Sclerite",
                "",
                "Materials", () -> new ItemStack(ItemInit.chalybeate_sclerite.get()),
                true, UnlockPredicate.minDegree(1)));

        // Generated registry gap-fill catalogue entries

                                                                                                                        list.add(new MaterialEntry("active_befouling_ash", "Active Befouling Ash",
                "A catalogued reagent used in blood alchemy and enzyme work.",
                "Alchemy & Enzymes", () -> new ItemStack(ItemInit.active_befouling_ash.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("active_smouldering_ash", "Active Smouldering Ash",
                "A catalogued reagent used in blood alchemy and enzyme work.",
                "Alchemy & Enzymes", () -> new ItemStack(ItemInit.active_smouldering_ash.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("crimson_lacquer", "Crimson Lacquer",
                "A catalogued reagent used in blood alchemy and enzyme work.",
                "Alchemy & Enzymes", () -> new ItemStack(ItemInit.crimson_lacquer.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("engram_block", "Engram Block",
                "A catalogued reagent used in blood alchemy and enzyme work.",
                "Alchemy & Enzymes", () -> new ItemStack(ItemInit.engram_stamp.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("enzyme_primer", "Enzyme Primer",
                "A catalogued reagent used in blood alchemy and enzyme work.",
                "Alchemy & Enzymes", () -> new ItemStack(ItemInit.enzyme_primer.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("ferric_binder", "Ferric Binder",
                "A catalogued reagent used in blood alchemy and enzyme work.",
                "Alchemy & Enzymes", () -> new ItemStack(ItemInit.ferric_binder.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("qliphoth_bloom", "Qliphoth Bloom",
                "A catalogued reagent used in blood alchemy and enzyme work.",
                "Alchemy & Enzymes", () -> new ItemStack(BlockInit.qliphoth_bloom.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("humoral_barometer", "Humoral Barometer",
                "A structural or decorative bloodcraft block.",
                "Architecture", () -> new ItemStack(BlockInit.humoral_barometer.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("ossuary_clock", "Ossuary Clock",
                "A structural or decorative bloodcraft block.",
                "Architecture", () -> new ItemStack(BlockInit.ossuary_clock.get()),
                true, UnlockPredicate.minDegree(3)));

                                        list.add(new MaterialEntry("sanguine_pane", "Sanguine Pane",
                "A structural or decorative bloodcraft block.",
                "Architecture", () -> new ItemStack(BlockInit.sanguine_pane.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("venous_stone_slab", "Venous Stone Slab",
                "A structural or decorative bloodcraft block.",
                "Architecture", () -> new ItemStack(BlockInit.venous_stone_slab.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("venous_stone_wall", "Venous Stone Wall",
                "A structural or decorative bloodcraft block.",
                "Architecture", () -> new ItemStack(BlockInit.venous_stone_wall.get()),
                true, UnlockPredicate.minDegree(3)));

                                        list.add(new MaterialEntry("vivianite_pane", "Vivianite Pane",
                "A structural or decorative bloodcraft block.",
                "Architecture", () -> new ItemStack(BlockInit.vivianite_pane.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("liber_sanguinum", "Liber Sanguinum",
                "A catalogued material entry for Liber Sanguinum.",
                "Materials", () -> new ItemStack(ItemInit.liber_sanguinum.get()),
                true, UnlockPredicate.always()));

                                                                                                                        list.add(new MaterialEntry("blood_gourd_black", "Blood Gourd Black",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.blood_gourd_black.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                        list.add(new MaterialEntry("blood_gourd_red", "Blood Gourd Red",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.blood_gourd_red.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("curor_lens", "Curor Lens",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.curor_lens.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("dried_gourd", "Dried Gourd",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.dried_gourd.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                        list.add(new MaterialEntry("gourd", "Gourd",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(BlockInit.gourd.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                        list.add(new MaterialEntry("gourd_seeds", "Gourd Seeds",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.gourd_seeds.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                        list.add(new MaterialEntry("gourd_slice", "Gourd Slice",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.gourd_slice.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                        list.add(new MaterialEntry("gourd_stew", "Gourd Stew",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.gourd_stew.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                        list.add(new MaterialEntry("gourdvine_tap", "Gourdvine Tap",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(BlockInit.gourdvine_tap.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                        list.add(new MaterialEntry("hemorath_rib", "Hemorath Rib",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.hemorath_rib.get()),
                true, UnlockPredicate.minDegree(2)));

                                                                                                                        list.add(new MaterialEntry("sanguine_salve", "Sanguine Salve",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.sanguine_salve.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("scrying_dish", "Scrying Dish",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.scrying_dish.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("vascular_poultice", "Vascular Poultice",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.vascular_poultice.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("vitality_chalice", "Vitality Chalice",
                "A bloodcraft vessel or gourd-family component used in sanguine storage, rites, or remedies.",
                "Gourds & Vessels", () -> new ItemStack(ItemInit.vitality_chalice.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("anastomotic_brazier", "Anastomotic Brazier",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.anastomotic_brazier.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("hematic_armature", "Hematic Armature",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.hematic_armature.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("hematic_iron_chain", "Hematic Iron Chain",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.hematic_iron_chain.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("hematic_stake", "Hematic Stake",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.hematic_stake.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("humane_idol", "Humane Idol",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.humane_idol.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("mason_effigy", "Mason Effigy",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.mason_effigy.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("mnemonic_candle", "Mnemonic Candle",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.mnemonic_candle.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("mycelial_lantern", "Mycelial Lantern",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.mycelial_lantern.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("puppeteers_spindle", "Puppeteers Spindle",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.puppeteers_spindle.get()),
                true, UnlockPredicate.minDegree(4)));

                                        list.add(new MaterialEntry("sanguine_omen", "Sanguine Omen",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.sanguine_omen.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("semi_sentient_construct", "Semi Sentient Construct",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.semi_sentient_construct.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("serpentine_idol", "Serpentine Idol",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.serpentine_idol.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("specimen_jar", "Specimen Jar",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.specimen_jar.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("witness_organ", "Witness Organ",
                "A ritual fixture or idol used to anchor advanced bloodcraft structures.",
                "Idols & Fixtures", () -> new ItemStack(BlockInit.witness_organ.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("annettas_sanguis_lancea", "Annetta's Sanguis Lancea",
                "A living Harbinger implement grown for field use.",
                "Living Implements", () -> new ItemStack(ItemInit.annettas_sanguis_lancea.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("sporitic_thurible", "Sporitic Thurible",
                "A living Harbinger implement grown for field use.",
                "Living Implements", () -> new ItemStack(ItemInit.sporitic_thurible.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("barbed_boots", "Barbed Boots",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.barbed_boots.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("barbed_chestplate", "Barbed Chestplate",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.barbed_chestplate.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("barbed_helm", "Barbed Helm",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.barbed_helm.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("barbed_leggings", "Barbed Leggings",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.barbed_leggings.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("barbed_shield", "Barbed Shield",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.barbed_shield.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("chitinite_boots", "Chitinite Boots",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.chitinite_boots.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("chitinite_chestplate", "Chitinite Chestplate",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.chitinite_chestplate.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("chitinite_helm", "Chitinite Helm",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.chitinite_helm.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("chitinite_leggings", "Chitinite Leggings",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.chitinite_leggings.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("chitinite_shield", "Chitinite Shield",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.chitinite_shield.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("prismatic_boots", "Prismatic Boots",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.prismatic_boots.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("prismatic_chestplate", "Prismatic Chestplate",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.prismatic_chestplate.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("prismatic_helm", "Prismatic Helm",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.prismatic_helm.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("prismatic_leggings", "Prismatic Leggings",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.prismatic_leggings.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("silent_archon_boots", "Silent Archon Boots",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.silent_archon_boots.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("silent_archon_chestplate", "Silent Archon Chestplate",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.silent_archon_chestplate.get()),
                true, UnlockPredicate.minDegree(7)));

        list.add(new MaterialEntry("silent_archon_helm", "Silent Archon Helm",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.silent_archon_helm.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("silent_archon_leggings", "Silent Archon Leggings",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Armor & Vestments", () -> new ItemStack(ItemInit.silent_archon_leggings.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_lust_boots", "Blood Lust Boots",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.blood_lust_boots.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_lust_chest", "Blood Lust Chest",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.blood_lust_chest.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("blood_lust_helm_grinning", "Blood Lust Helm Grinning",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.blood_lust_helm_grinning.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("blood_lust_helm_lodestone", "Blood Lust Helm Lodestone",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.blood_lust_helm_lodestone.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("blood_lust_helm_tengu", "Blood Lust Helm Tengu",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.blood_lust_helm_tengu.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("blood_lust_helm_velorum", "Blood Lust Helm Velorum",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.blood_lust_helm_velorum.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("blood_lust_legs", "Blood Lust Legs",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.blood_lust_legs.get()),
                true, UnlockPredicate.minDegree(5)));

                list.add(new MaterialEntry("edacious_blood_lust_boots", "Edacious Blood Lust Boots",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.edacious_blood_lust_boots.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("edacious_blood_lust_chest", "Edacious Blood Lust Chest",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.edacious_blood_lust_chest.get()),
                true, UnlockPredicate.minDegree(5)));

                list.add(new MaterialEntry("edacious_blood_lust_helm", "Edacious Blood Lust Helm",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.edacious_blood_lust_helm.get()),
                true, UnlockPredicate.minDegree(5)));

                list.add(new MaterialEntry("edacious_blood_lust_legs", "Edacious Blood Lust Legs",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.edacious_blood_lust_legs.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("grinning_mask", "Grinning Mask",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.grinning_mask.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("lodestone_faceplate", "Lodestone Faceplate",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.lodestone_faceplate.get()),
                true, UnlockPredicate.minDegree(5)));

                list.add(new MaterialEntry("phantasmal_blood_lust_boots", "Phantasmal Blood Lust Boots",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.phantasmal_blood_lust_boots.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("phantasmal_blood_lust_chest", "Phantasmal Blood Lust Chest",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.phantasmal_blood_lust_chest.get()),
                true, UnlockPredicate.minDegree(5)));

                list.add(new MaterialEntry("phantasmal_blood_lust_helm", "Phantasmal Blood Lust Helm",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.phantasmal_blood_lust_helm.get()),
                true, UnlockPredicate.minDegree(5)));

                list.add(new MaterialEntry("phantasmal_blood_lust_legs", "Phantasmal Blood Lust Legs",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.phantasmal_blood_lust_legs.get()),
                true, UnlockPredicate.minDegree(5)));

                list.add(new MaterialEntry("sheolic_blood_lust_boots", "Sheolic Blood Lust Boots",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.sheolic_blood_lust_boots.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("sheolic_blood_lust_chest", "Sheolic Blood Lust Chest",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.sheolic_blood_lust_chest.get()),
                true, UnlockPredicate.minDegree(5)));

                list.add(new MaterialEntry("sheolic_blood_lust_helm", "Sheolic Blood Lust Helm",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.sheolic_blood_lust_helm.get()),
                true, UnlockPredicate.minDegree(5)));

                list.add(new MaterialEntry("sheolic_blood_lust_legs", "Sheolic Blood Lust Legs",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.sheolic_blood_lust_legs.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("tengu_mask", "Tengu Mask",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.tengu_mask.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("velorum_mask", "Velorum Mask",
                "A wearable Harbinger implement tied to blood rites, masks, or vestments.",
                "Masks & Vestments", () -> new ItemStack(ItemInit.velorum_mask.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("blood_wood_leaves", "Blood Wood Leaves",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.blood_wood_leaves.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("calcified_erythrocoral", "Calcified Erythrocoral",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.calcified_erythrocoral.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("calcified_hyphae", "Calcified Hyphae",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.calcified_hyphae.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("erythrocoral_block", "Erythrocoral Block",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.erythrocoral_block.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("erythrocoral_fan", "Erythrocoral Fan",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.erythrocoral_fan.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("erythrocoral_tendril", "Erythrocoral Tendril",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.erythrocoral_tendril.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("hematic_iron_bars", "Hematic Iron Bars",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.hematic_iron_bars.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("hematic_iron_door", "Hematic Iron Door",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.hematic_iron_door.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("hematic_iron_trapdoor", "Hematic Iron Trapdoor",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.hematic_iron_trapdoor.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("hemorrhagic_crust", "Hemorrhagic Crust",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.hemorrhagic_crust.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("hyphae", "Hyphae",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.hyphae.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("infested_wood", "Infested Wood",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.infested_wood.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("mycelium_erythrocytic_dirt", "Mycelium Erythrocytic Dirt",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.mycelium_erythrocytic_dirt.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("sporite_crystal", "Sporite Crystal",
                "A block or growth from blood-rich fungal ecologies.",
                "Myco-Realm Blocks", () -> new ItemStack(BlockInit.sporite_crystal.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("constrictor_cord", "Constrictor Cord",
                "An organic component used in advanced Harbinger crafting and creature-derived recipes.",
                "Biomaterials", () -> new ItemStack(ItemInit.constrictor_cord.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("dried_leech", "Dried Leech",
                "An organic component used in advanced Harbinger crafting and creature-derived recipes.",
                "Biomaterials", () -> new ItemStack(ItemInit.dried_leech.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("erythrocoral_fragment", "Erythrocoral Fragment",
                "An organic component used in advanced Harbinger crafting and creature-derived recipes.",
                "Biomaterials", () -> new ItemStack(ItemInit.erythrocoral_fragment.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("scale_grip", "Scale Grip",
                "An organic component used in advanced Harbinger crafting and creature-derived recipes.",
                "Biomaterials", () -> new ItemStack(ItemInit.scale_grip.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("swollen_leech", "Swollen Leech",
                "An organic component used in advanced Harbinger crafting and creature-derived recipes.",
                "Biomaterials", () -> new ItemStack(ItemInit.swollen_leech.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("tendon_line", "Tendon Line",
                "An organic component used in advanced Harbinger crafting and creature-derived recipes.",
                "Biomaterials", () -> new ItemStack(ItemInit.tendon_line.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("hallowed_residuum_hemorath", "Hallowed Residuum Hemorath",
                "A hallowed saint-reagent extracted for advanced consecration work.",
                "Hallowed Reagents", () -> new ItemStack(ItemInit.hallowed_residuum_hemorath.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("hallowed_residuum_putriciel", "Hallowed Residuum Putriciel",
                "A hallowed saint-reagent extracted for advanced consecration work.",
                "Hallowed Reagents", () -> new ItemStack(ItemInit.hallowed_residuum_putriciel.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("hallowed_residuum_seraphae", "Hallowed Residuum Seraphae",
                "A hallowed saint-reagent extracted for advanced consecration work.",
                "Hallowed Reagents", () -> new ItemStack(ItemInit.hallowed_residuum_seraphae.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("hallowed_residuum_velorum", "Hallowed Residuum Velorum",
                "A hallowed saint-reagent extracted for advanced consecration work.",
                "Hallowed Reagents", () -> new ItemStack(ItemInit.hallowed_residuum_velorum.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("memory_of_vesper", "Memory of Vesper",
                "A late-stage qliphoth reagent used in monolith and consecration work.",
                "Qliphoth Reagents", () -> new ItemStack(ItemInit.memory_of_vesper.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("monolith_fragment", "Monolith Fragment",
                "A late-stage qliphoth reagent used in monolith and consecration work.",
                "Qliphoth Reagents", () -> new ItemStack(ItemInit.monolith_fragment.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("monolith_imbued_cloth", "Monolith Imbued Cloth",
                "A late-stage qliphoth reagent used in monolith and consecration work.",
                "Qliphoth Reagents", () -> new ItemStack(ItemInit.monolith_imbued_cloth.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("monolithic_cornerstone", "Monolithic Cornerstone",
                "A late-stage qliphoth reagent used in monolith and consecration work.",
                "Qliphoth Reagents", () -> new ItemStack(ItemInit.monolithic_cornerstone.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("qliphoth_pome", "Qliphoth Pome",
                "A late-stage qliphoth reagent used in monolith and consecration work.",
                "Qliphoth Reagents", () -> new ItemStack(ItemInit.qliphoth_pome.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("qliphoth_seed", "Qliphoth Seed",
                "A late-stage qliphoth reagent used in monolith and consecration work.",
                "Qliphoth Reagents", () -> new ItemStack(ItemInit.qliphoth_seed.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                                        list.add(new MaterialEntry("vicars_consecration_kit", "Vicar's Consecration Kit",
                "A late-stage qliphoth reagent used in monolith and consecration work.",
                "Qliphoth Reagents", () -> new ItemStack(ItemInit.vicars_consecration_kit.get()),
                true, UnlockPredicate.minDegree(5)));

                                                                                                list.add(new MaterialEntry("rhizovitta_communis", "Rhizovitta Communis",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.rhizovitta_communis.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("antiphonomyces_resonans", "Antiphonomyces Resonans",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.antiphonomyces_resonans.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("ferric_spores", "Ferric Spores",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.ferric_spores.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("fervent_spores", "Fervent Spores",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.fervent_spores.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("frigid_spores", "Frigid Spores",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.frigid_spores.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("incandescent_spores", "Incandescent Spores",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.incandescent_spores.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                list.add(new MaterialEntry("oculiflora_reticularis", "Oculiflora Reticularis",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.oculiflora_reticularis.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("neurotic_spores", "Neurotic Spores",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.neurotic_spores.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("noctifly_agaric", "Noctifly Agaric",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.noctifly_agaric.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("ruinous_spores", "Ruinous Spores",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.ruinous_spores.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                list.add(new MaterialEntry("putrivora_resolvens", "Putrivora Resolvens",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.putrivora_resolvens.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("saprovitta_vestigium", "Saprovitta Vestigium",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.saprovitta_vestigium.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("talaromyces_minus", "Talaromyces Minus",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.talaromyces_minus.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                list.add(new MaterialEntry("cryostroma_perdurans", "Cryostroma Perdurans",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.cryostroma_perdurans.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("umbral_spores", "Umbral Spores",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.umbral_spores.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("vivacious_spores", "Vivacious Spores",
                "A fungal culture or spore sample used in mycological bloodcraft.",
                "Spores & Cultures", () -> new ItemStack(ItemInit.vivacious_spores.get()),
                true, UnlockPredicate.minDegree(3)));

                                                                                                                        list.add(new MaterialEntry("echo_of_heart", "Echo of Heart",
                "A visceral echo reflected through the Mortal Display and Visceral Mirror systems.",
                "Organ Echoes", () -> new ItemStack(ItemInit.echo_of_heart.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("echo_of_kidneys", "Echo of Kidneys",
                "A visceral echo reflected through the Mortal Display and Visceral Mirror systems.",
                "Organ Echoes", () -> new ItemStack(ItemInit.echo_of_kidneys.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("echo_of_liver", "Echo of Liver",
                "A visceral echo reflected through the Mortal Display and Visceral Mirror systems.",
                "Organ Echoes", () -> new ItemStack(ItemInit.echo_of_liver.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("echo_of_lungs", "Echo of Lungs",
                "A visceral echo reflected through the Mortal Display and Visceral Mirror systems.",
                "Organ Echoes", () -> new ItemStack(ItemInit.echo_of_lungs.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                                                        list.add(new MaterialEntry("echo_of_spleen", "Echo of Spleen",
                "A visceral echo reflected through the Mortal Display and Visceral Mirror systems.",
                "Organ Echoes", () -> new ItemStack(ItemInit.echo_of_spleen.get()),
                true, UnlockPredicate.minDegree(4)));

                                                                                list.add(new MaterialEntry("hematic_iron_chestplate", "Hematic Iron Chestplate",
                "",
                "Materials", () -> new ItemStack(ItemInit.hematic_iron_chestplate.get()),
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
                                                                                                                                                                                list.add(new MaterialEntry("altar_of_cleansing", "Altar of Cleansing",
                "Sacred altar devoted to Our Lady of Still Waters. Grants purity boosts through offerings.",
                "Functional Blocks", () -> new ItemStack(BlockInit.altar_of_cleansing.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("unstained_podium", "Unstained Podium",
                "Podium used in Unstained rituals and ceremonies.",
                "Functional Blocks", () -> new ItemStack(BlockInit.unstained_podium.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("suspended_cleansed_blood_crystal", "Cleansed Blood Crystal",
                "A blood crystal purified through Unstained rites. Radiates cleansing energy.",
                "Functional Blocks", () -> new ItemStack(BlockInit.suspended_cleansed_blood_crystal.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        // ── Building Blocks ──
                                                                                                                                                                                list.add(new MaterialEntry("cleansed_stone", "Cleansed Stone",
                "Stone purified of all sanguine taint. Foundation for Unstained structures.",
                "Building Blocks", () -> new ItemStack(BlockInit.cleansed_stone.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("pallid_lantern", "Pallid Lantern",
                "A soft-glowing lantern consecrated in the name of the Pallid Matron.",
                "Building Blocks", () -> new ItemStack(BlockInit.pallid_lantern.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("cleansed_sanguine_glass", "Cleansed Sanguine Glass",
                "Purified glass that blocks blood resonance.",
                "Building Blocks", () -> new ItemStack(BlockInit.cleansed_sanguine_glass.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("pale_silver_block", "Pale Silver Block",
                "A block of compressed pale silver. Used in Unstained rite structures.",
                "Building Blocks", () -> new ItemStack(BlockInit.pale_silver_block.get()),
                true, UnlockPredicate.minPurity(25.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("pale_silver_bells", "Pale Silver Bells",
                "Decorative chime block cast from pale silver. Rings faintly in still air.",
                "Building Blocks", () -> new ItemStack(BlockInit.pale_silver_bells.get()),
                true, UnlockPredicate.minPurity(25.0f)));

        // ── Plants ──
                                                                                                                                                                                list.add(new MaterialEntry("lethean_poppy", "Lethean Poppy",
                "Sacred flower of the Pale Lady. Source of pale distillate and poppy wreaths.",
                "Plants", () -> new ItemStack(BlockInit.lethean_poppy.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("ghost_pipe", "Ghost Pipe",
                "A pale, leafless plant that grows only where the ground is truly still.",
                "Plants", () -> new ItemStack(BlockInit.ghost_pipe.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("puffball_fungus", "Puffball Fungus",
                "A soft white fungus. Bursts when touched, releasing a cloud of cleansing spores.",
                "Plants", () -> new ItemStack(BlockInit.puffball_fungus.get()),
                true, UnlockPredicate.always()));

        // ── Materials ──
                                                                                                                                                                                list.add(new MaterialEntry("tears_of_silthmere", "Tears of Silthmere",
                "Rare tears shed in devotion. Grants a powerful one-time purity boost at the Altar.",
                "Materials", () -> new ItemStack(ItemInit.tears_of_silthmere.get()),
                true, UnlockPredicate.minPurity(50.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("lethean_poppy_wreath", "Lethean Poppy Wreath",
                "Woven from lethean poppies. Offer at the Altar for +5 purity.",
                "Materials", () -> new ItemStack(ItemInit.lethean_poppy_wreath.get()),
                true, UnlockPredicate.minPurity(10.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("silver_chalice", "Silver Chalice",
                "Vessel of purification. Offer at the Altar for +5 clarity.",
                "Materials", () -> new ItemStack(ItemInit.silver_chalice.get()),
                true, UnlockPredicate.minClarity(10.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("pale_silver_ingot", "Pale Silver Ingot",
                "Purified silver free of sanguine taint. Base material for Unstained equipment.",
                "Materials", () -> new ItemStack(ItemInit.pale_silver_ingot.get()),
                true, UnlockPredicate.minPurity(15.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("pale_distillate", "The Pale Distillate",
                "Concentrated essence distilled from lethean poppies.",
                "Materials", () -> new ItemStack(ItemInit.pale_distillate.get()),
                true, UnlockPredicate.minPurity(25.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("cleansed_blood_crystal_shard", "Cleansed Blood Crystal Shard",
                "A purified blood crystal shard radiating calm energy.",
                "Materials", () -> new ItemStack(ItemInit.cleansed_blood_crystal_shard.get()),
                true, UnlockPredicate.minPurity(25.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("tome_of_the_unstained", "Tome of the Unstained",
                "Sacred text containing the teachings of the Unstained path.",
                "Materials", () -> new ItemStack(ItemInit.tome_of_the_unstained.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("pallid_icon", "Pallid Icon",
                "Holy icon depicting Our Lady of Still Waters.",
                "Materials", () -> new ItemStack(ItemInit.pallid_icon.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("consecrated_copper_ingot", "Consecrated Copper Ingot",
                "Copper blessed through Unstained rites.",
                "Materials", () -> new ItemStack(ItemInit.consecrated_copper_ingot.get()),
                true, UnlockPredicate.minPurity(10.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("hemolytic_vial", "Hemolytic Vial",
                "A vial of hemolytic solution. Dissolves sanguine formations on contact.",
                "Materials", () -> new ItemStack(ItemInit.hemolytic_vial.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("pale_humor_flask", "Pale Humor Flask",
                "A flask of purified pale humor. Restores purity in small measure, or pours into a pool for item purification.",
                "Materials", () -> new ItemStack(ItemInit.pale_humor_flask.get()),
                true, UnlockPredicate.minPurity(15.0f)));

//        list.add(new MaterialEntry("pale_field_ink", "Pale Field Ink",
//                "Unstained writing ink used for consecrated patterns and records.",
//                "Materials", () -> new ItemStack(ItemInit.pale_field_ink.get()),
//                true, UnlockPredicate.minPurity(25f)));

                                                                                                                                                                                list.add(new MaterialEntry("draught_of_still_waters", "Draught of Still Waters",
                "A sacred draught blessed by Our Lady. Soothes blood corruption.",
                "Materials", () -> new ItemStack(ItemInit.draught_of_still_waters.get()),
                true, UnlockPredicate.minPurity(50.0f)));

        // ── Anti-Blood Materials ──
                                                                                                                                                                                list.add(new MaterialEntry("hemolytic_solution", "Hemolytic Solution",
                "Solution that dissolves sanguine formations. Core anti-blood reagent.",
                "Anti-Blood", () -> new ItemStack(ItemInit.hemolytic_solution.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("hemolytic_plating", "Hemolytic Plating",
                "Plating treated with hemolytic solution. Resists blood magic.",
                "Anti-Blood", () -> new ItemStack(ItemInit.hemolytic_plating.get()),
                true, UnlockPredicate.minPurity(15.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("neutralizing_gasket", "Neutralizing Gasket",
                "Gasket that neutralizes blood-based energy transfer.",
                "Anti-Blood", () -> new ItemStack(ItemInit.neutralizing_gasket.get()),
                true, UnlockPredicate.minPurity(25.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("hemolytic_plating_block", "Hemolytic Plating Block",
                "Structural block made of hemolytic plating. Blood-proof construction.",
                "Anti-Blood", () -> new ItemStack(BlockInit.hemolytic_plating_block.get()),
                true, UnlockPredicate.minPurity(25.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("cleansing_hemolymph", "Cleansing Hemolymph",
                "Purified hemolymph used to cleanse blood corruption.",
                "Anti-Blood", () -> new ItemStack(ItemInit.cleansing_hemolymph.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("lethean_dew", "Lethean Dew",
                "Dew collected from lethean flowers. Erases blood memories.",
                "Anti-Blood", () -> new ItemStack(ItemInit.lethean_dew.get()),
                true, UnlockPredicate.minPurity(25.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("lethean_brew", "Lethean Brew",
                "Potent brew of lethean essence. Powerful memory erasure.",
                "Anti-Blood", () -> new ItemStack(ItemInit.lethean_brew.get()),
                true, UnlockPredicate.minPurity(50.0f)));

        // ── Equipment ──
                                                                                                                                                                                list.add(new MaterialEntry("unstained_helm", "Unstained Armor",
                "Armor forged with pale silver, blessed against blood corruption.",
                "Equipment", () -> new ItemStack(ItemInit.unstained_helm.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("unstained_warhammer", "Unstained Warhammer",
                "Heavy warhammer consecrated by the Unstained. Devastates blood creatures.",
                "Equipment", () -> new ItemStack(ItemInit.unstained_warhammer.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("unstained_shield", "Unstained Shield",
                "Shield blessed by the Pallid Matron. Wards off blood magic attacks.",
                "Equipment", () -> new ItemStack(ItemInit.unstained_shield.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("self_reflection_mirror", "Self-Reflection Mirror",
                "Opens the Unstained Progress screen to view purification status.",
                "Equipment", () -> new ItemStack(ItemInit.self_reflection_mirror.get()),
                true, UnlockPredicate.always()));

                                                                                                                                                                                list.add(new MaterialEntry("absolution_dagger", "Absolution Dagger",
                "A pale-silver dagger consecrated for extraction rites. The Unstained carry it for mercy cuts, not combat.",
                "Equipment", () -> new ItemStack(ItemInit.absolution_dagger.get()),
                true, UnlockPredicate.minPurity(50.0f)));

                                                                                                                                                                                list.add(new MaterialEntry("liber_immaculatus", "Liber Immaculatus",
                "The sacred text of the Unstained path. Records the stages of purification and the Lady's teachings.",
                "Equipment", () -> new ItemStack(ItemInit.liber_immaculatus.get()),
                true, UnlockPredicate.always()));

        // Generated registry gap-fill catalogue entries

                                                                                                                        list.add(new MaterialEntry("pale_silver_bars", "Pale Silver Bars",
                "An Unstained fixture used in cleansing, still-water rites, or pale-silver construction.",
                "Cleansing Fixtures", () -> new ItemStack(BlockInit.pale_silver_bars.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                        list.add(new MaterialEntry("pallid_retort", "Pallid Retort",
                "An Unstained fixture used in cleansing, still-water rites, or pale-silver construction.",
                "Cleansing Fixtures", () -> new ItemStack(BlockInit.pallid_retort.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                        list.add(new MaterialEntry("pallid_silver_chain", "Pallid Silver Chain",
                "An Unstained fixture used in cleansing, still-water rites, or pale-silver construction.",
                "Cleansing Fixtures", () -> new ItemStack(BlockInit.pallid_silver_chain.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                        list.add(new MaterialEntry("saint_sarcophagus", "Saint Sarcophagus",
                "An Unstained fixture used in cleansing, still-water rites, or pale-silver construction.",
                "Cleansing Fixtures", () -> new ItemStack(BlockInit.saint_sarcophagus.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                        list.add(new MaterialEntry("cleansed_sanguine_pane", "Cleansed Sanguine Pane",
                "A purified building material for Unstained structures.",
                "Pale Architecture", () -> new ItemStack(BlockInit.cleansed_sanguine_pane.get()),
                true, UnlockPredicate.minPurity(25.0f)));

                                                                                                                        list.add(new MaterialEntry("annettas_absolution_dagger", "Annetta's Absolution Dagger",
                "An Unstained tool, weapon, or vestment forged for cleansing rites.",
                "Equipment", () -> new ItemStack(ItemInit.annettas_absolution_dagger.get()),
                true, UnlockPredicate.minPurity(50.0f)));

                                                                                                                        list.add(new MaterialEntry("pale_silver_bell", "Pale Silver Bell",
                "An Unstained tool, weapon, or vestment forged for cleansing rites.",
                "Equipment", () -> new ItemStack(ItemInit.pale_silver_bell.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                        list.add(new MaterialEntry("silthmere_glaive", "Silthmere Glaive",
                "An Unstained tool, weapon, or vestment forged for cleansing rites.",
                "Equipment", () -> new ItemStack(ItemInit.silthmere_glaive.get()),
                true, UnlockPredicate.minPurity(50.0f)));

                                                                                                                        list.add(new MaterialEntry("unstained_boots", "Unstained Boots",
                "An Unstained tool, weapon, or vestment forged for cleansing rites.",
                "Equipment", () -> new ItemStack(ItemInit.unstained_boots.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                        list.add(new MaterialEntry("unstained_chestplate", "Unstained Chestplate",
                "An Unstained tool, weapon, or vestment forged for cleansing rites.",
                "Equipment", () -> new ItemStack(ItemInit.unstained_chestplate.get()),
                true, UnlockPredicate.minPurity(35.0f)));

                                                                                                                        list.add(new MaterialEntry("unstained_leggings", "Unstained Leggings",
                "An Unstained tool, weapon, or vestment forged for cleansing rites.",
                "Equipment", () -> new ItemStack(ItemInit.unstained_leggings.get()),
                true, UnlockPredicate.minPurity(35.0f)));

        return Collections.unmodifiableList(list);
    }
}
