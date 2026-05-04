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
                "Opens the Skill Tree. Used to unlock and manage blood skills.",
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
                "Functional Blocks", () -> new ItemStack(BlockInit.iron_brazier.get())
                ,true, UnlockPredicate.minDegree(1)));

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
                "A sanctified blood reservoir at the heart of the Founding Sanctum.",
                "Functional Blocks", () -> new ItemStack(BlockInit.consecrated_bloodwell.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("covenant_throne", "Covenant Throne",
                "The seat of authority within a consecrated Sanctum.",
                "Functional Blocks", () -> new ItemStack(BlockInit.covenant_throne.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("sanguine_vigil", "Sanguine Vigil",
                "A watchful sentinel structure tied to the Founding Sanctum boundary.",
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
                "Building Blocks", () -> new ItemStack(BlockInit.venous_stone.get())));

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
                "Building Blocks", () -> new ItemStack(BlockInit.erythrocytic_dirt.get())));

        list.add(new MaterialEntry("hematic_iron_ore", "Hematic Iron Ore",
                "Ore vein containing raw hematic iron deposits.",
                "Building Blocks", () -> new ItemStack(BlockInit.hematic_iron_ore.get())));

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
                "Materials", () -> new ItemStack(ItemInit.sanguine_formation.get())));

        list.add(new MaterialEntry("hematic_iron_scrap", "Hematic Iron Scrap",
                "Scrap metal infused with blood iron. Smelt into ingots.",
                "Materials", () -> new ItemStack(ItemInit.hematic_iron_scrap.get())));

        list.add(new MaterialEntry("blood_crystal_shard", "Blood Crystal Shard",
                "A shard of crystallised blood energy.",
                "Materials", () -> new ItemStack(ItemInit.blood_crystal_shard.get())));

        list.add(new MaterialEntry("blood_rock", "Blood Rock",
                "Dense stone saturated with sanguine energy.",
                "Materials", () -> new ItemStack(ItemInit.blood_rock.get())));

        list.add(new MaterialEntry("foul_paste", "Foul Paste",
                "A vile mixture used in blood crafting recipes.",
                "Materials", () -> new ItemStack(ItemInit.foul_paste.get())));

        list.add(new MaterialEntry("sanguine_conduit", "Sanguine Conduit",
                "Channels blood energy between devices and structures.",
                "Materials", () -> new ItemStack(ItemInit.sanguine_conduit.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("puppeteering_thread", "Puppeteering Thread",
                "Fine threads used in manipulation memory weaving.",
                "Materials", () -> new ItemStack(ItemInit.puppeteering_thread.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("dicentra_sap", "Dicentra Sap",
                "Sap from the bleeding heart plant. Alchemical reagent.",
                "Materials", () -> new ItemStack(ItemInit.dicentra_sap.get())));

        list.add(new MaterialEntry("bleeding_bulb", "Bleeding Bulb",
                "Bulb from hemomantic flora; used in juicing recipes.",
                "Materials", () -> new ItemStack(ItemInit.bleeding_bulb.get())));

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

        list.add(new MaterialEntry("hematic_iron_powder", "Hematic Iron Powder",
                "Ground hematic iron. Intermediate step before smelting into ingots.",
                "Materials", () -> new ItemStack(ItemInit.hematic_iron_powder.get()),
                true, UnlockPredicate.minDegree(2)));

        list.add(new MaterialEntry("sanguine_quintessence", "Sanguine Quintessence",
                "The purest distillate of blood energy. Required for high-tier workings.",
                "Materials", () -> new ItemStack(ItemInit.sanguine_quintessence.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("hematic_field_ink", "Hematic Field Ink",
                "Blood-infused ink used for writing patterns and engrams.",
                "Materials", () -> new ItemStack(ItemInit.hematic_field_ink.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("blood_stained_stone", "Blood-Stained Stone",
                "Common stone saturated with sanguine residue.",
                "Materials", () -> new ItemStack(ItemInit.blood_stained_stone.get())));

        list.add(new MaterialEntry("desiccated_membrane", "Desiccated Membrane",
                "Dried organic membrane harvested from blood constructs.",
                "Materials", () -> new ItemStack(ItemInit.desiccated_membrane.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("molten_scab", "Molten Scab",
                "Hardened blood residue with a high heat signature.",
                "Materials", () -> new ItemStack(ItemInit.molten_scab.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("frozen_clot", "Frozen Clot",
                "A coagulated mass of blood locked in a cold stasis.",
                "Materials", () -> new ItemStack(ItemInit.frozen_clot.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("abyssal_ichor", "Abyssal Ichor",
                "Dark ichor drawn from deeper blood constructs. Tenebris-aligned.",
                "Materials", () -> new ItemStack(ItemInit.abyssal_ichor.get()),
                true, UnlockPredicate.minDegree(5)));

        list.add(new MaterialEntry("void_ichor", "Void Ichor",
                "Ichor from the periphery of blood space. Reacts violently with light.",
                "Materials", () -> new ItemStack(ItemInit.void_ichor.get()),
                true, UnlockPredicate.minDegree(6)));

        list.add(new MaterialEntry("nerve_bundle", "Nerve Bundle",
                "Extracted nerve cluster. Carries residual Ductilis tendency.",
                "Materials", () -> new ItemStack(ItemInit.nerve_bundle.get()),
                true, UnlockPredicate.minDegree(3)));

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

        list.add(new MaterialEntry("living_blade", "Living Blade",
                "A blade of animate sanguine tissue. Grows sharper as blood is drawn.",
                "Equipment", () -> new ItemStack(ItemInit.living_blade.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_axe", "Living Axe",
                "An axe formed from living blood-iron. Severs and drinks in one motion.",
                "Equipment", () -> new ItemStack(ItemInit.living_axe.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_spear", "Living Spear",
                "A spear that remembers every wound it has dealt.",
                "Equipment", () -> new ItemStack(ItemInit.living_spear.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_staff", "Living Staff",
                "A staff of animate sanguine matter. Channels manipulation cost.",
                "Equipment", () -> new ItemStack(ItemInit.living_staff.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("living_baghnakh", "Living Baghnakh",
                "Clawed gauntlet of living tissue. Tears and drains on each strike.",
                "Equipment", () -> new ItemStack(ItemInit.living_baghnakh.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("living_crossbow", "Living Crossbow",
                "A crossbow grown from blood-iron sinew. Fires bolts of coagulated blood.",
                "Equipment", () -> new ItemStack(ItemInit.living_crossbow.get()),
                true, UnlockPredicate.minDegree(4)));

        // ── Containers ──
        list.add(new MaterialEntry("bloody_flask", "Bloody Flask",
                "Portable blood container. Holds 250 mL.",
                "Containers", () -> new ItemStack(ItemInit.bloody_flask.get())));

        list.add(new MaterialEntry("bloody_vial", "Bloody Vial",
                "Small vial for blood samples and centrifuging.",
                "Containers", () -> new ItemStack(ItemInit.bloody_vial.get())));

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
                "Plants", () -> new ItemStack(BlockInit.bleeding_heart.get())));

        list.add(new MaterialEntry("infected_fungus", "Infected Fungus",
                "A fungus colonised by the Fungal Entity's influence. Key brewing reagent.",
                "Plants", () -> new ItemStack(BlockInit.infected_fungus.get())));

        list.add(new MaterialEntry("stinkhorn_fungus", "Stinkhorn Fungus",
                "Pungent fruiting body. Its spores carry a subtle sanguine resonance.",
                "Plants", () -> new ItemStack(BlockInit.stinkhorn_fungus.get())));

        list.add(new MaterialEntry("rafflesia", "Rafflesia",
                "Enormous parasitic bloom, hot-blooded at the core. Blood-faction flora.",
                "Plants", () -> new ItemStack(BlockInit.rafflesia.get())));

        list.add(new MaterialEntry("sarcodes", "Sarcodes",
                "A crimson snow plant that grows where blood has soaked the earth.",
                "Plants", () -> new ItemStack(BlockInit.sarcodes.get())));

        list.add(new MaterialEntry("devils_tooth", "Devil's Tooth",
                "Tooth-shaped fungus dripping with a reddish fluid. Caustic on contact.",
                "Plants", () -> new ItemStack(BlockInit.devils_tooth.get())));

        // ── Morphlings ──
        list.add(new MaterialEntry("morphling_polyp", "Morphling Polyp",
                "The base larval form of a morphling. Place in the Morphling Incubator to develop.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_polyp.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_fungal", "Morphling: Fungal",
                "A morphling shaped into fungal form. Produces spore-related effects.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_fungal.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_leeches", "Morphling: Leeches",
                "A morphling that manifests as a blood-drinking leech cluster.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_leeches.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_chitinite", "Morphling: Chitinite",
                "A morphling encased in hardened chitin plating.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_chitinite.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_serpent", "Morphling: Serpent",
                "A morphling in serpentine form. Applies venom on attack.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_serpent.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_pests", "Morphling: Pests",
                "A morphling that erupts into a swarm of biting insects.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_pests.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_spider", "Morphling: Spider",
                "A morphling taking arachnid form. Webs and ambushes.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_spider.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_moth", "Morphling: Moth",
                "A morphling shaped into a pale moth. Scatters blinding scales.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_moth.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_tick", "Morphling: Tick",
                "A morphling that latches and slowly drains blood volume.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_tick.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_centipede", "Morphling: Centipede",
                "A morphling taking centipede form. Fast, venomous, difficult to shake.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_centipede.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_bat", "Morphling: Bat",
                "A morphling shaped as a blood-drinking bat. Echolocates blood reserves.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_bat.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_urchin", "Morphling: Urchin",
                "A morphling with spines deployed outward. Retaliates on being struck.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_urchin.get()),
                true, UnlockPredicate.minDegree(3)));

        list.add(new MaterialEntry("morphling_mole", "Morphling: Mole",
                "A morphling shaped for burrowing. Tunnels to reach its target.",
                "Morphlings", () -> new ItemStack(ItemInit.morphling_mole.get()),
                true, UnlockPredicate.minDegree(3)));

        // ── Scars & Patterns ──
        list.add(new MaterialEntry("scar_blank", "Blank Scar",
                "An uncarved scar tablet. Base material for all scar inscriptions. Requires Adept rank.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_blank.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("scar_binder", "Scar Binder",
                "Leather-bound case that stores Scar Patterns and carved scars.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_binder.get()),
                true, UnlockPredicate.minDegree(4)));

        list.add(new MaterialEntry("scar_binder_upgraded", "Scar Binder (Upgraded)",
                "Expanded scar binder with additional storage capacity.",
                "Scars & Patterns", () -> new ItemStack(ItemInit.scar_binder_upgraded.get()),
                true, UnlockPredicate.minDegree(5)));

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
                "Functional Blocks", () -> new ItemStack(BlockInit.altar_of_cleansing.get())));

        list.add(new MaterialEntry("unstained_podium", "Unstained Podium",
                "Podium used in Unstained rituals and ceremonies.",
                "Functional Blocks", () -> new ItemStack(BlockInit.unstained_podium.get())));

        list.add(new MaterialEntry("suspended_cleansed_blood_crystal", "Cleansed Blood Crystal",
                "A blood crystal purified through Unstained rites. Radiates cleansing energy.",
                "Functional Blocks", () -> new ItemStack(BlockInit.suspended_cleansed_blood_crystal.get()),
                true, UnlockPredicate.minPurity(25f)));

        // ── Building Blocks ──
        list.add(new MaterialEntry("cleansed_stone", "Cleansed Stone",
                "Stone purified of all sanguine taint. Foundation for Unstained structures.",
                "Building Blocks", () -> new ItemStack(BlockInit.cleansed_stone.get())));

        list.add(new MaterialEntry("pallid_lantern", "Pallid Lantern",
                "A soft-glowing lantern consecrated in the name of the Pallid Matron.",
                "Building Blocks", () -> new ItemStack(BlockInit.pallid_lantern.get())));

        list.add(new MaterialEntry("cleansed_sanguine_glass", "Cleansed Sanguine Glass",
                "Purified glass that blocks blood resonance.",
                "Building Blocks", () -> new ItemStack(BlockInit.cleansed_sanguine_glass.get())));

        list.add(new MaterialEntry("pale_silver_block", "Pale Silver Block",
                "A block of compressed pale silver. Used in Unstained rite structures.",
                "Building Blocks", () -> new ItemStack(BlockInit.pale_silver_block.get()),
                true, UnlockPredicate.minPurity(25f)));

        list.add(new MaterialEntry("pale_silver_bells", "Pale Silver Bells",
                "Decorative chime block cast from pale silver. Rings faintly in still air.",
                "Building Blocks", () -> new ItemStack(BlockInit.pale_silver_bells.get()),
                true, UnlockPredicate.minPurity(25f)));

        // ── Plants ──
        list.add(new MaterialEntry("lethean_poppy", "Lethean Poppy",
                "Sacred flower of the Pale Lady. Source of pale distillate and poppy wreaths.",
                "Plants", () -> new ItemStack(BlockInit.lethean_poppy.get())));

        list.add(new MaterialEntry("ghost_pipe", "Ghost Pipe",
                "A pale, leafless plant that grows only where the ground is truly still.",
                "Plants", () -> new ItemStack(BlockInit.ghost_pipe.get())));

        list.add(new MaterialEntry("puffball_fungus", "Puffball Fungus",
                "A soft white fungus. Bursts when touched, releasing a cloud of cleansing spores.",
                "Plants", () -> new ItemStack(BlockInit.puffball_fungus.get())));

        // ── Materials ──
        list.add(new MaterialEntry("tears_of_silthmere", "Tears of Silthmere",
                "Rare tears shed in devotion. Grants a powerful one-time purity boost at the Altar.",
                "Materials", () -> new ItemStack(ItemInit.tears_of_silthmere.get()),
                true, UnlockPredicate.minPurity(50f)));

        list.add(new MaterialEntry("lethean_poppy_wreath", "Lethean Poppy Wreath",
                "Woven from lethean poppies. Offer at the Altar for +5 purity.",
                "Materials", () -> new ItemStack(ItemInit.lethean_poppy_wreath.get()),
                true, UnlockPredicate.minPurity(10f)));

        list.add(new MaterialEntry("silver_chalice", "Silver Chalice",
                "Vessel of purification. Offer at the Altar for +5 clarity.",
                "Materials", () -> new ItemStack(ItemInit.silver_chalice.get()),
                true, UnlockPredicate.minClarity(10f)));

        list.add(new MaterialEntry("pale_silver_ingot", "Pale Silver Ingot",
                "Purified silver free of sanguine taint. Base material for Unstained equipment.",
                "Materials", () -> new ItemStack(ItemInit.pale_silver_ingot.get()),
                true, UnlockPredicate.minPurity(15f)));

        list.add(new MaterialEntry("pale_distillate", "The Pale Distillate",
                "Concentrated essence distilled from lethean poppies.",
                "Materials", () -> new ItemStack(ItemInit.pale_distillate.get()),
                true, UnlockPredicate.minPurity(25f)));

        list.add(new MaterialEntry("cleansed_blood_crystal_shard", "Cleansed Blood Crystal Shard",
                "A purified blood crystal shard radiating calm energy.",
                "Materials", () -> new ItemStack(ItemInit.cleansed_blood_crystal_shard.get()),
                true, UnlockPredicate.minPurity(25f)));

        list.add(new MaterialEntry("tome_of_the_unstained", "Tome of the Unstained",
                "Sacred text containing the teachings of the Unstained path.",
                "Materials", () -> new ItemStack(ItemInit.tome_of_the_unstained.get())));

        list.add(new MaterialEntry("pallid_icon", "Pallid Icon",
                "Holy icon depicting Our Lady of Still Waters.",
                "Materials", () -> new ItemStack(ItemInit.pallid_icon.get())));

        list.add(new MaterialEntry("consecrated_copper_ingot", "Consecrated Copper Ingot",
                "Copper blessed through Unstained rites.",
                "Materials", () -> new ItemStack(ItemInit.consecrated_copper_ingot.get()),
                true, UnlockPredicate.minPurity(10f)));

        list.add(new MaterialEntry("hemolytic_vial", "Hemolytic Vial",
                "A vial of hemolytic solution. Dissolves sanguine formations on contact.",
                "Materials", () -> new ItemStack(ItemInit.hemolytic_vial.get())));

        list.add(new MaterialEntry("pale_humor_flask", "Pale Humor Flask",
                "A flask of purified pale humor. Restores purity in small measure.",
                "Materials", () -> new ItemStack(ItemInit.pale_humor_flask.get()),
                true, UnlockPredicate.minPurity(15f)));

        list.add(new MaterialEntry("pale_field_ink", "Pale Field Ink",
                "Unstained writing ink used for consecrated patterns and records.",
                "Materials", () -> new ItemStack(ItemInit.pale_field_ink.get()),
                true, UnlockPredicate.minPurity(25f)));

        list.add(new MaterialEntry("draught_of_still_waters", "Draught of Still Waters",
                "A sacred draught blessed by Our Lady. Soothes blood corruption.",
                "Materials", () -> new ItemStack(ItemInit.draught_of_still_waters.get()),
                true, UnlockPredicate.minPurity(50f)));

        // ── Anti-Blood Materials ──
        list.add(new MaterialEntry("hemolytic_solution", "Hemolytic Solution",
                "Solution that dissolves sanguine formations. Core anti-blood reagent.",
                "Anti-Blood", () -> new ItemStack(ItemInit.hemolytic_solution.get())));

        list.add(new MaterialEntry("hemolytic_plating", "Hemolytic Plating",
                "Plating treated with hemolytic solution. Resists blood magic.",
                "Anti-Blood", () -> new ItemStack(ItemInit.hemolytic_plating.get()),
                true, UnlockPredicate.minPurity(15f)));

        list.add(new MaterialEntry("neutralizing_gasket", "Neutralizing Gasket",
                "Gasket that neutralizes blood-based energy transfer.",
                "Anti-Blood", () -> new ItemStack(ItemInit.neutralizing_gasket.get()),
                true, UnlockPredicate.minPurity(25f)));

        list.add(new MaterialEntry("hemolytic_plating_block", "Hemolytic Plating Block",
                "Structural block made of hemolytic plating. Blood-proof construction.",
                "Anti-Blood", () -> new ItemStack(BlockInit.hemolytic_plating_block.get()),
                true, UnlockPredicate.minPurity(25f)));

        list.add(new MaterialEntry("cleansing_hemolymph", "Cleansing Hemolymph",
                "Purified hemolymph used to cleanse blood corruption.",
                "Anti-Blood", () -> new ItemStack(ItemInit.cleansing_hemolymph.get()),
                true, UnlockPredicate.minPurity(35f)));

        list.add(new MaterialEntry("lethean_dew", "Lethean Dew",
                "Dew collected from lethean flowers. Erases blood memories.",
                "Anti-Blood", () -> new ItemStack(ItemInit.lethean_dew.get()),
                true, UnlockPredicate.minPurity(25f)));

        list.add(new MaterialEntry("lethean_brew", "Lethean Brew",
                "Potent brew of lethean essence. Powerful memory erasure.",
                "Anti-Blood", () -> new ItemStack(ItemInit.lethean_brew.get()),
                true, UnlockPredicate.minPurity(50f)));

        list.add(new MaterialEntry("consecrated_syringe", "Consecrated Syringe",
                "A syringe blessed through Unstained rites. Used for hemolytic procedures.",
                "Anti-Blood", () -> new ItemStack(ItemInit.consecrated_syringe.get()),
                true, UnlockPredicate.minPurity(25f)));

        // ── Equipment ──
        list.add(new MaterialEntry("unstained_helm", "Unstained Armor",
                "Armor forged with pale silver, blessed against blood corruption.",
                "Equipment", () -> new ItemStack(ItemInit.unstained_helm.get()),
                true, UnlockPredicate.minPurity(35f)));

        list.add(new MaterialEntry("unstained_warhammer", "Unstained Warhammer",
                "Heavy warhammer consecrated by the Unstained. Devastates blood creatures.",
                "Equipment", () -> new ItemStack(ItemInit.unstained_warhammer.get()),
                true, UnlockPredicate.minPurity(35f)));

        list.add(new MaterialEntry("unstained_shield", "Unstained Shield",
                "Shield blessed by the Pallid Matron. Wards off blood magic attacks.",
                "Equipment", () -> new ItemStack(ItemInit.unstained_shield.get()),
                true, UnlockPredicate.minPurity(35f)));

        list.add(new MaterialEntry("self_reflection_mirror", "Self-Reflection Mirror",
                "Opens the Unstained Progress screen to view purification status.",
                "Equipment", () -> new ItemStack(ItemInit.self_reflection_mirror.get())));

        list.add(new MaterialEntry("absolution_dagger", "Absolution Dagger",
                "A pale-silver dagger consecrated for extraction rites. The Unstained carry it for mercy cuts, not combat.",
                "Equipment", () -> new ItemStack(ItemInit.absolution_dagger.get()),
                true, UnlockPredicate.minPurity(50f)));

        list.add(new MaterialEntry("liber_immaculatus", "Liber Immaculatus",
                "The sacred text of the Unstained path. Records the stages of purification and the Lady's teachings.",
                "Equipment", () -> new ItemStack(ItemInit.liber_immaculatus.get())));

        return Collections.unmodifiableList(list);
    }
}
