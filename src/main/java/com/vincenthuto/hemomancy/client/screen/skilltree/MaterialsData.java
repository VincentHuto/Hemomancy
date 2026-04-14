package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.world.item.ItemStack;

/**
 * Static catalogue of material / process entries shown in the
 * "Materials &amp; Processes" tabs.
 * <p>
 * Blood-faction entries appear in the {@link SkillTreeScreen};
 * Unstained/Lethe entries appear in the {@link UnstainedProgressScreen}.
 */
public final class MaterialsData {

	private MaterialsData() {}

	// ────────────────────────────────────────────────────────────
	//  Blood faction — SkillTreeScreen
	// ────────────────────────────────────────────────────────────

	private static List<MaterialEntry> bloodEntries;

	public static List<MaterialEntry> getBloodEntries() {
		if (bloodEntries == null) {
			bloodEntries = buildBloodEntries();
		}
		return bloodEntries;
	}

	private static List<MaterialEntry> buildBloodEntries() {
		List<MaterialEntry> list = new ArrayList<>();

		// ── Functional Blocks ──
		list.add(new MaterialEntry("dendritic_distributor", "Dendritic Distributor",
				"Opens the Skill Tree. Used to unlock and manage blood skills.",
				"Functional Blocks", () -> new ItemStack(BlockInit.dendritic_distributor.get())));

		list.add(new MaterialEntry("somatic_loom", "Somatic Loom",
				"Weaves blood memories into usable manipulation forms.",
				"Functional Blocks", () -> new ItemStack(BlockInit.somatic_loom.get())));

		list.add(new MaterialEntry("mnemonic_reliquary", "Mnemonic Reliquary",
				"Loadout block for equipping and managing blood manipulations.",
				"Functional Blocks", () -> new ItemStack(BlockInit.mnemonic_reliquary.get())));

		list.add(new MaterialEntry("ghastly_alembic", "ghastly_alembic",
				"Processes organic materials into useful blood-craft reagents.",
				"Functional Blocks", () -> new ItemStack(BlockInit.ghastly_alembic.get())));

		list.add(new MaterialEntry("vial_centrifuge", "Vial Centrifuge",
				"Separates blood samples into component enzymes.",
				"Functional Blocks", () -> new ItemStack(BlockInit.vial_centrifuge.get())));

		list.add(new MaterialEntry("earthen_vein", "Earthen Vein",
				"A living conduit block that transports blood volume.",
				"Functional Blocks", () -> new ItemStack(BlockInit.earthen_vein.get())));

		list.add(new MaterialEntry("iron_brazier", "Iron Brazier",
				"Ritual fire source for blood crafting and rites.",
				"Functional Blocks", () -> new ItemStack(BlockInit.iron_brazier.get())));

		list.add(new MaterialEntry("morphling_incubator", "Morphling Incubator",
				"Incubates morphling polyps into specialised morphlings.",
				"Functional Blocks", () -> new ItemStack(BlockInit.morphling_incubator.get())));

		list.add(new MaterialEntry("runic_chisel_station", "Runic Chisel Station",
				"Carves rune patterns into blank runes.",
				"Functional Blocks", () -> new ItemStack(BlockInit.runic_chisel_station.get())));

		list.add(new MaterialEntry("scrying_podium", "Scrying Podium",
				"Reveals hidden information about blood tendencies.",
				"Functional Blocks", () -> new ItemStack(BlockInit.scrying_podium.get())));

		list.add(new MaterialEntry("fungal_podium", "Fungal Podium",
				"Processes fungal spores for rune imprinting.",
				"Functional Blocks", () -> new ItemStack(BlockInit.fungal_podium.get())));

		list.add(new MaterialEntry("fungal_implantation_pylon", "Fungal Implantation Pylon",
				"Implants fungal growths for symbiotic effects.",
				"Functional Blocks", () -> new ItemStack(BlockInit.fungal_implantation_pylon.get())));

		list.add(new MaterialEntry("mortal_display", "Mortal Display",
				"Displays and manages visceral organ echoes.",
				"Functional Blocks", () -> new ItemStack(BlockInit.mortal_display.get())));

		list.add(new MaterialEntry("visceral_mirror", "Visceral Mirror",
				"Reflects the player's internal organ state.",
				"Functional Blocks", () -> new ItemStack(BlockInit.visceral_mirror.get())));

		list.add(new MaterialEntry("blood_crystal_block", "Blood Crystal",
				"A crystallised concentration of sanguine energy.",
				"Functional Blocks", () -> new ItemStack(BlockInit.blood_crystal.get())));

		// ── Building Blocks ──
		list.add(new MaterialEntry("venous_stone", "Venous Stone",
				"Dark stone veined with sanguine minerals. Core building material.",
				"Building Blocks", () -> new ItemStack(BlockInit.venous_stone.get())));

		list.add(new MaterialEntry("hematic_iron_block", "Hematic Iron Block",
				"Block of refined hematic iron. Sturdy and fire-resistant.",
				"Building Blocks", () -> new ItemStack(BlockInit.hematic_iron_block.get())));

		list.add(new MaterialEntry("sanguine_glass", "Sanguine Glass",
				"Translucent glass infused with blood. Decorative building material.",
				"Building Blocks", () -> new ItemStack(BlockInit.sanguine_glass.get())));

		list.add(new MaterialEntry("conscious_mass", "Conscious Mass",
				"Pulsing organic block formed from concentrated blood energy.",
				"Building Blocks", () -> new ItemStack(BlockInit.conscious_mass.get())));

		list.add(new MaterialEntry("erythrocytic_dirt", "Erythrocytic Dirt",
				"Blood-rich soil that supports hemomantic flora.",
				"Building Blocks", () -> new ItemStack(BlockInit.erythrocytic_dirt.get())));

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
				"Materials", () -> new ItemStack(ItemInit.sanguine_conduit.get())));

		list.add(new MaterialEntry("puppeteering_thread", "Puppeteering Thread",
				"Fine threads used in manipulation memory weaving.",
				"Materials", () -> new ItemStack(ItemInit.puppeteering_thread.get())));

		list.add(new MaterialEntry("dicentra_sap", "Dicentra Sap",
				"Sap from the bleeding heart plant. Alchemical reagent.",
				"Materials", () -> new ItemStack(ItemInit.dicentra_sap.get())));

		list.add(new MaterialEntry("bleeding_bulb", "Bleeding Bulb",
				"Bulb from hemomantic flora; used in juicing recipes.",
				"Materials", () -> new ItemStack(ItemInit.bleeding_bulb.get())));

		list.add(new MaterialEntry("vivianite_cluster", "Vivianite Cluster",
				"A mineral cluster with faint sanguine resonance.",
				"Materials", () -> new ItemStack(ItemInit.vivianite_cluster.get())));

		list.add(new MaterialEntry("spore_sac", "Spore Sac",
				"Collected fungal spores for various applications.",
				"Materials", () -> new ItemStack(ItemInit.spore_sac.get())));

		list.add(new MaterialEntry("chitinous_husk", "Chitinous Husk",
				"Hardened exoskeletal material from blood creatures.",
				"Materials", () -> new ItemStack(ItemInit.chitinous_husk.get())));

		list.add(new MaterialEntry("serpent_scale", "Serpent Scale",
				"Scale from a blood serpent. Used in advanced crafting.",
				"Materials", () -> new ItemStack(ItemInit.serpent_scale.get())));

		// ── Enzymes ──
		list.add(new MaterialEntry("vivacious_enzyme", "Vivacious Enzyme",
				"Animus-aligned enzyme. Boosts vitality effects.",
				"Enzymes", () -> new ItemStack(ItemInit.vivacious_enzyme.get())));

		list.add(new MaterialEntry("ruinous_enzyme", "Ruinous Enzyme",
				"Mortem-aligned enzyme. Enhances destructive abilities.",
				"Enzymes", () -> new ItemStack(ItemInit.ruinous_enzyme.get())));

		list.add(new MaterialEntry("neurotic_enzyme", "Neurotic Enzyme",
				"Ductilis-aligned enzyme. Sharpens mental manipulation.",
				"Enzymes", () -> new ItemStack(ItemInit.neurotic_enzyme.get())));

		list.add(new MaterialEntry("ferric_enzyme", "Ferric Enzyme",
				"Ferric-aligned enzyme. Strengthens metallic transmutation.",
				"Enzymes", () -> new ItemStack(ItemInit.ferric_enzyme.get())));

		// ── Equipment ──
		list.add(new MaterialEntry("hematic_iron_helm", "Hematic Iron Armor",
				"Fire-resistant armor forged from blood-infused iron.",
				"Equipment", () -> new ItemStack(ItemInit.hematic_iron_helm.get())));

		list.add(new MaterialEntry("blood_lust_helm", "Blood Lust Armor",
				"Savage armor that empowers bloodthirsty combat.",
				"Equipment", () -> new ItemStack(ItemInit.blood_lust_helm.get())));

		list.add(new MaterialEntry("barbed_blade", "Barbed Blade",
				"Cruel living weapon lined with blood-drawing barbs.",
				"Equipment", () -> new ItemStack(ItemInit.barbed_blade.get())));

		list.add(new MaterialEntry("chitinite_mace", "Chitinite Mace",
				"Heavy mace crafted from hardened chitinite.",
				"Equipment", () -> new ItemStack(ItemInit.chitinite_mace.get())));

		list.add(new MaterialEntry("sanguis_lancea", "Sanguis Lancea",
				"A living lance that drains blood on hit.",
				"Equipment", () -> new ItemStack(ItemInit.sanguis_lancea.get())));

		list.add(new MaterialEntry("living_syringe", "Living Syringe",
				"Extracts and injects blood with surgical precision.",
				"Equipment", () -> new ItemStack(ItemInit.living_syringe.get())));

		// ── Containers ──
		list.add(new MaterialEntry("bloody_flask", "Bloody Flask",
				"Portable blood container. Holds 250 mL.",
				"Containers", () -> new ItemStack(ItemInit.bloody_flask.get())));

		list.add(new MaterialEntry("bloody_vial", "Bloody Vial",
				"Small vial for blood samples and centrifuging.",
				"Containers", () -> new ItemStack(ItemInit.bloody_vial.get())));

		list.add(new MaterialEntry("blood_gourd_white", "Blood Gourd",
				"Organic gourd cultivated to store blood naturally.",
				"Containers", () -> new ItemStack(ItemInit.blood_gourd_white.get())));

		// ── Runes & Patterns ──
		list.add(new MaterialEntry("rune_blank", "Blank Rune",
				"An uncarved rune tablet. Base material for all runic inscriptions. Requires Adept rank.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_blank.get())));

		list.add(new MaterialEntry("rune_binder", "Rune Binder",
				"Leather-bound case that stores rune patterns and carved runes.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_binder.get())));

		list.add(new MaterialEntry("rune_binder_upgraded", "Rune Binder (Upgraded)",
				"Expanded rune binder with additional storage capacity.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_binder_upgraded.get())));

		list.add(new MaterialEntry("rune_transcendence", "Rune of Transcendence",
				"Lux-aligned rune. Opens pathways to luminous perception.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_transcendence.get())));

		list.add(new MaterialEntry("rune_sol", "Rune of Sol",
				"Flammeus-aligned rune. Kindles the inner fire of conviction.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_sol.get())));

		list.add(new MaterialEntry("rune_heart", "Rune of the Heart",
				"Animus-aligned rune. Strengthens the vital bond between body and will.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_heart.get())));

		list.add(new MaterialEntry("rune_descendence", "Rune of Descendence",
				"Mortem-aligned rune. Carves a pathway toward the threshold of endings.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_descendence.get())));

		list.add(new MaterialEntry("rune_moon", "Rune of the Moon",
				"Congeatio-aligned rune. Chills the blood, clarifying dormant instincts.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_moon.get())));

		list.add(new MaterialEntry("rune_eye", "Rune of the Eye",
				"Ductilis-aligned rune. Sharpens the inner sight to perceive hidden connections.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_eye.get())));

		list.add(new MaterialEntry("rune_feral", "Rune of the Feral",
				"Ductilis-aligned rune. Awakens primal nervous pathways long dormant.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_feral.get())));

		// ── Tier 1 Runes (new) ──

		list.add(new MaterialEntry("rune_thorn", "Rune of the Thorn",
				"Ferric-aligned rune. Draws iron from the blood to harden resolve.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_thorn.get())));

		list.add(new MaterialEntry("rune_shade", "Rune of the Shade",
				"Tenebris-aligned rune. Cloaks the bearer in the penumbra between worlds.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_shade.get())));

		// ── Tier 2 Runes ──

		list.add(new MaterialEntry("rune_pyre", "Rune of the Pyre",
				"Flammeus-aligned rune. Stokes the cremation fire that purifies sanguine impurities.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_pyre.get())));

		list.add(new MaterialEntry("rune_marrow", "Rune of Marrow",
				"Animus-aligned rune. Taps the deep marrow where new blood is forged.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_marrow.get())));

		list.add(new MaterialEntry("rune_blight", "Rune of Blight",
				"Mortem-aligned rune. Channels the rot that consumes living tissue.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_blight.get())));

		list.add(new MaterialEntry("rune_rime", "Rune of Rime",
				"Congeatio-aligned rune. Crystallizes the blood into protective frost lattices.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_rime.get())));

		list.add(new MaterialEntry("rune_flux", "Rune of Flux",
				"Ductilis-aligned rune. Loosens the boundaries of form, permitting adaptation.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_flux.get())));

		list.add(new MaterialEntry("rune_halo", "Rune of the Halo",
				"Lux-aligned rune. Projects an aureole of purified sanguine radiance.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_halo.get())));

		list.add(new MaterialEntry("rune_anvil", "Rune of the Anvil",
				"Ferric-aligned rune. Tempers the blood's iron into an unyielding lattice.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_anvil.get())));

		list.add(new MaterialEntry("rune_veil", "Rune of the Veil",
				"Tenebris-aligned rune. Parts the membrane between the seen and unseen.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_veil.get())));

		// ── Tier 3 Runes ──

		list.add(new MaterialEntry("rune_phoenix", "Rune of the Phoenix",
				"Flammeus-aligned rune. Inscribes the cycle of immolation and rebirth into the bloodline.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_phoenix.get())));

		list.add(new MaterialEntry("rune_ichor", "Rune of Ichor",
				"Animus-aligned rune. Awakens the primordial ichor that flows beneath mortal blood.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_ichor.get())));

		list.add(new MaterialEntry("rune_wither", "Rune of Withering",
				"Mortem-aligned rune. Accelerates entropy within the vital humours.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_wither.get())));

		list.add(new MaterialEntry("rune_glacier", "Rune of the Glacier",
				"Congeatio-aligned rune. Invokes the stillness of primordial ice to arrest all change.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_glacier.get())));

		list.add(new MaterialEntry("rune_chimera", "Rune of the Chimera",
				"Ductilis-aligned rune. Grafts disparate essences into a single protean bloodline.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_chimera.get())));

		list.add(new MaterialEntry("rune_corona", "Rune of the Corona",
				"Lux-aligned rune. Crowns the bearer in blinding sanguine luminescence.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_corona.get())));

		list.add(new MaterialEntry("rune_crucible", "Rune of the Crucible",
				"Ferric-aligned rune. Subjects the blood's metals to the ultimate forge-trial.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_crucible.get())));

		list.add(new MaterialEntry("rune_oblivion", "Rune of Oblivion",
				"Tenebris-aligned rune. Opens a void that consumes memory and substance alike.",
				"Runes & Patterns", () -> new ItemStack(ItemInit.rune_oblivion.get())));

		return Collections.unmodifiableList(list);
	}

	// ────────────────────────────────────────────────────────────
	//  Unstained / Lethe faction — UnstainedProgressScreen
	// ────────────────────────────────────────────────────────────

	private static List<MaterialEntry> unstainedEntries;

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
				"Sacred altar devoted to Our Lady of Lethe. Grants purity boosts through offerings.",
				"Functional Blocks", () -> new ItemStack(BlockInit.altar_of_cleansing.get())));

		list.add(new MaterialEntry("unstained_podium", "Unstained Podium",
				"Podium used in Unstained rituals and ceremonies.",
				"Functional Blocks", () -> new ItemStack(BlockInit.unstained_podium.get())));

		list.add(new MaterialEntry("suspended_cleansed_blood_crystal", "Cleansed Blood Crystal",
				"A blood crystal purified through Unstained rites. Radiates cleansing energy.",
				"Functional Blocks", () -> new ItemStack(BlockInit.suspended_cleansed_blood_crystal.get())));

		list.add(new MaterialEntry("suspended_vivianite", "Suspended Vivianite",
				"Vivianite crystal suspended in a purified frame. Resonates with clarity.",
				"Functional Blocks", () -> new ItemStack(BlockInit.suspended_vivianite.get())));

		// ── Building Blocks ──
		list.add(new MaterialEntry("cleansed_stone", "Cleansed Stone",
				"Stone purified of all sanguine taint. Foundation for Unstained structures.",
				"Building Blocks", () -> new ItemStack(BlockInit.cleansed_stone.get())));

		list.add(new MaterialEntry("lethe_lantern", "Lethe Lantern",
				"A soft-glowing lantern consecrated in the name of Lethe.",
				"Building Blocks", () -> new ItemStack(BlockInit.lethe_lantern.get())));

		list.add(new MaterialEntry("cleansed_sanguine_glass", "Cleansed Sanguine Glass",
				"Purified glass that blocks blood resonance.",
				"Building Blocks", () -> new ItemStack(BlockInit.cleansed_sanguine_glass.get())));

		list.add(new MaterialEntry("vivianite_glass", "Vivianite Glass",
				"Translucent glass made from vivianite mineral.",
				"Building Blocks", () -> new ItemStack(BlockInit.vivianite_glass.get())));

		list.add(new MaterialEntry("pale_silver_block", "Pale Silver Block",
				"A block of compressed pale silver. Used in Unstained rite structures.",
				"Building Blocks", () -> new ItemStack(BlockInit.pale_silver_block.get())));

		// ── Plants ──
		list.add(new MaterialEntry("lethean_poppy", "Lethean Poppy",
				"Sacred flower of Lethe. Source of lethean extract and poppy wreaths.",
				"Plants", () -> new ItemStack(BlockInit.lethean_poppy.get())));

		// ── Materials ──
		list.add(new MaterialEntry("tears_of_lethe", "Tears of Lethe",
				"Rare tears shed in devotion. Grants a powerful one-time purity boost at the Altar.",
				"Materials", () -> new ItemStack(ItemInit.tears_of_lethe.get())));

		list.add(new MaterialEntry("lethean_poppy_wreath", "Lethean Poppy Wreath",
				"Woven from lethean poppies. Offer at the Altar for +5 purity.",
				"Materials", () -> new ItemStack(ItemInit.lethean_poppy_wreath.get())));

		list.add(new MaterialEntry("silver_chalice", "Silver Chalice",
				"Vessel of purification. Offer at the Altar for +5 clarity.",
				"Materials", () -> new ItemStack(ItemInit.silver_chalice.get())));

		list.add(new MaterialEntry("pale_silver_ingot", "Pale Silver Ingot",
				"Purified silver free of sanguine taint. Base material for Unstained equipment.",
				"Materials", () -> new ItemStack(ItemInit.pale_silver_ingot.get())));

		list.add(new MaterialEntry("lethean_extract", "Lethean Extract",
				"Concentrated essence distilled from lethean poppies.",
				"Materials", () -> new ItemStack(ItemInit.lethean_extract.get())));

		list.add(new MaterialEntry("cleansed_blood_crystal_shard", "Cleansed Blood Crystal Shard",
				"A purified blood crystal shard radiating calm energy.",
				"Materials", () -> new ItemStack(ItemInit.cleansed_blood_crystal_shard.get())));

		list.add(new MaterialEntry("tome_of_the_unstained", "Tome of the Unstained",
				"Sacred text containing the teachings of the Unstained path.",
				"Materials", () -> new ItemStack(ItemInit.tome_of_the_unstained.get())));

		list.add(new MaterialEntry("lethe_icon", "Lethe Icon",
				"Holy icon depicting Our Lady of Lethe.",
				"Materials", () -> new ItemStack(ItemInit.lethe_icon.get())));

		list.add(new MaterialEntry("consecrated_copper_ingot", "Consecrated Copper Ingot",
				"Copper blessed through Unstained rites.",
				"Materials", () -> new ItemStack(ItemInit.consecrated_copper_ingot.get())));

		// ── Anti-Blood Materials ──
		list.add(new MaterialEntry("hemolytic_solution", "Hemolytic Solution",
				"Solution that dissolves sanguine formations. Core anti-blood reagent.",
				"Anti-Blood", () -> new ItemStack(ItemInit.hemolytic_solution.get())));

		list.add(new MaterialEntry("hemolytic_plating", "Hemolytic Plating",
				"Plating treated with hemolytic solution. Resists blood magic.",
				"Anti-Blood", () -> new ItemStack(ItemInit.hemolytic_plating.get())));

		list.add(new MaterialEntry("neutralizing_gasket", "Neutralizing Gasket",
				"Gasket that neutralizes blood-based energy transfer.",
				"Anti-Blood", () -> new ItemStack(ItemInit.neutralizing_gasket.get())));

		list.add(new MaterialEntry("hemolytic_plating_block", "Hemolytic Plating Block",
				"Structural block made of hemolytic plating. Blood-proof construction.",
				"Anti-Blood", () -> new ItemStack(BlockInit.hemolytic_plating_block.get())));

		list.add(new MaterialEntry("cleansing_hemolymph", "Cleansing Hemolymph",
				"Purified hemolymph used to cleanse blood corruption.",
				"Anti-Blood", () -> new ItemStack(ItemInit.cleansing_hemolymph.get())));

		list.add(new MaterialEntry("lethean_dew", "Lethean Dew",
				"Dew collected from lethean flowers. Erases blood memories.",
				"Anti-Blood", () -> new ItemStack(ItemInit.lethean_dew.get())));

		list.add(new MaterialEntry("lethean_brew", "Lethean Brew",
				"Potent brew of lethean essence. Powerful memory erasure.",
				"Anti-Blood", () -> new ItemStack(ItemInit.lethean_brew.get())));

		// ── Equipment ──
		list.add(new MaterialEntry("unstained_helm", "Unstained Armor",
				"Armor forged with pale silver, blessed against blood corruption.",
				"Equipment", () -> new ItemStack(ItemInit.unstained_helm.get())));

		list.add(new MaterialEntry("unstained_warhammer", "Unstained Warhammer",
				"Heavy warhammer consecrated by the Unstained. Devastates blood creatures.",
				"Equipment", () -> new ItemStack(ItemInit.unstained_warhammer.get())));

		list.add(new MaterialEntry("unstained_shield", "Unstained Shield",
				"Shield blessed by Lethe. Wards off blood magic attacks.",
				"Equipment", () -> new ItemStack(ItemInit.unstained_shield.get())));

		list.add(new MaterialEntry("self_reflection_mirror", "Self-Reflection Mirror",
				"Opens the Unstained Progress screen to view purification status.",
				"Equipment", () -> new ItemStack(ItemInit.self_reflection_mirror.get())));

		return Collections.unmodifiableList(list);
	}
}
