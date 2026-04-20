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
 * Blood-faction entries appear in the {@link HarbingerProgressScreen};
 * Unstained/White Humor entries appear in the {@link UnstainedProgressScreen}.
 */
public final class MaterialsData {

	private MaterialsData() {}

	// ────────────────────────────────────────────────────────────
	//  Blood faction — HarbingerProgressScreen
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

		list.add(new MaterialEntry("scar_station", "Cerebral Scar Station",
				"Carves Scar Patterns into blank scars.",
				"Functional Blocks", () -> new ItemStack(BlockInit.scar_station.get())));

		list.add(new MaterialEntry("scrying_podium", "Scrying Podium",
				"Reveals hidden information about blood tendencies.",
				"Functional Blocks", () -> new ItemStack(BlockInit.scrying_podium.get())));

		list.add(new MaterialEntry("fungal_podium", "Fungal Podium",
				"Processes fungal spores for scar imprinting.",
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

		// ── Scars & Patterns ──
		list.add(new MaterialEntry("scar_blank", "Blank Scar",
				"An uncarved scar tablet. Base material for all scar inscriptions. Requires Adept rank.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_blank.get())));

		list.add(new MaterialEntry("scar_binder", "Scar Binder",
				"Leather-bound case that stores Scar Patterns and carved scars.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_binder.get())));

		list.add(new MaterialEntry("scar_binder_upgraded", "scar binder (Upgraded)",
				"Expanded scar binder with additional storage capacity.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_binder_upgraded.get())));

		list.add(new MaterialEntry("scar_transcendence", "Scar of Transcendence",
				"Lux-aligned scar. Opens pathways to luminous perception.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_transcendence.get())));

		list.add(new MaterialEntry("scar_sol", "Scar of Sol",
				"Flammeus-aligned scar. Kindles the inner fire of conviction.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_sol.get())));

		list.add(new MaterialEntry("scar_heart", "Scar of the Heart",
				"Animus-aligned scar. Strengthens the vital bond between body and will.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_heart.get())));

		list.add(new MaterialEntry("scar_descendence", "Scar of Descendence",
				"Mortem-aligned scar. Carves a pathway toward the threshold of endings.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_descendence.get())));

		list.add(new MaterialEntry("scar_moon", "Scar of the Moon",
				"Congeatio-aligned scar. Chills the blood, clarifying dormant instincts.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_moon.get())));

		list.add(new MaterialEntry("scar_eye", "Scar of the Eye",
				"Ductilis-aligned scar. Sharpens the inner sight to perceive hidden connections.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_eye.get())));

		list.add(new MaterialEntry("scar_feral", "Scar of the Feral",
				"Ductilis-aligned scar. Awakens primal nervous pathways long dormant.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_feral.get())));

		// ── Tier 1 Scars (new) ──

		list.add(new MaterialEntry("scar_thorn", "Scar of the Thorn",
				"Ferric-aligned scar. Draws iron from the blood to harden resolve.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_thorn.get())));

		list.add(new MaterialEntry("scar_shade", "Scar of the Shade",
				"Tenebris-aligned scar. Cloaks the bearer in the penumbra between worlds.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_shade.get())));

		// ── Tier 2 Scars ──

		list.add(new MaterialEntry("scar_pyre", "Scar of the Pyre",
				"Flammeus-aligned scar. Stokes the cremation fire that purifies sanguine impurities.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_pyre.get())));

		list.add(new MaterialEntry("scar_marrow", "Scar of Marrow",
				"Animus-aligned scar. Taps the deep marrow where new blood is forged.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_marrow.get())));

		list.add(new MaterialEntry("scar_blight", "Scar of Blight",
				"Mortem-aligned scar. Channels the rot that consumes living tissue.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_blight.get())));

		list.add(new MaterialEntry("scar_rime", "Scar of Rime",
				"Congeatio-aligned scar. Crystallizes the blood into protective frost lattices.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_rime.get())));

		list.add(new MaterialEntry("scar_flux", "Scar of Flux",
				"Ductilis-aligned scar. Loosens the boundaries of form, permitting adaptation.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_flux.get())));

		list.add(new MaterialEntry("scar_halo", "Scar of the Halo",
				"Lux-aligned scar. Projects an aureole of purified sanguine radiance.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_halo.get())));

		list.add(new MaterialEntry("scar_anvil", "Scar of the Anvil",
				"Ferric-aligned scar. Tempers the blood's iron into an unyielding lattice.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_anvil.get())));

		list.add(new MaterialEntry("scar_veil", "Scar of the Veil",
				"Tenebris-aligned scar. Parts the membrane between the seen and unseen.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_veil.get())));

		// ── Tier 3 Scars ──

		list.add(new MaterialEntry("scar_phoenix", "Scar of the Phoenix",
				"Flammeus-aligned scar. Inscribes the cycle of immolation and rebirth into the bloodline.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_phoenix.get())));

		list.add(new MaterialEntry("scar_ichor", "Scar of Ichor",
				"Animus-aligned scar. Awakens the primordial ichor that flows beneath mortal blood.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_ichor.get())));

		list.add(new MaterialEntry("scar_wither", "Scar of Withering",
				"Mortem-aligned scar. Accelerates entropy within the vital humours.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_wither.get())));

		list.add(new MaterialEntry("scar_glacier", "Scar of the Glacier",
				"Congeatio-aligned scar. Invokes the stillness of primordial ice to arrest all change.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_glacier.get())));

		list.add(new MaterialEntry("scar_chimera", "Scar of the Chimera",
				"Ductilis-aligned scar. Grafts disparate essences into a single protean bloodline.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_chimera.get())));

		list.add(new MaterialEntry("scar_corona", "Scar of the Corona",
				"Lux-aligned scar. Crowns the bearer in blinding sanguine luminescence.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_corona.get())));

		list.add(new MaterialEntry("scar_crucible", "Scar of the Crucible",
				"Ferric-aligned scar. Subjects the blood's metals to the ultimate forge-trial.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_crucible.get())));

		list.add(new MaterialEntry("scar_oblivion", "Scar of Oblivion",
				"Tenebris-aligned scar. Opens a void that consumes memory and substance alike.",
				"Scars & Patterns", () -> new ItemStack(ItemInit.scar_oblivion.get())));

		return Collections.unmodifiableList(list);
	}

	// ────────────────────────────────────────────────────────────
	//  Unstained / White Humor faction — UnstainedProgressScreen
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
				"Sacred altar devoted to Our Lady of Still Waters. Grants purity boosts through offerings.",
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

		list.add(new MaterialEntry("pallid_lantern", "Pallid Lantern",
				"A soft-glowing lantern consecrated in the name of the Pallid Matron.",
				"Building Blocks", () -> new ItemStack(BlockInit.pallid_lantern.get())));

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
				"Sacred flower of the Pale Lady. Source of pale distillate and poppy wreaths.",
				"Plants", () -> new ItemStack(BlockInit.lethean_poppy.get())));

		// ── Materials ──
		list.add(new MaterialEntry("tears_of_silthmere", "Tears of Silthmere",
				"Rare tears shed in devotion. Grants a powerful one-time purity boost at the Altar.",
				"Materials", () -> new ItemStack(ItemInit.tears_of_silthmere.get())));

		list.add(new MaterialEntry("lethean_poppy_wreath", "Lethean Poppy Wreath",
				"Woven from lethean poppies. Offer at the Altar for +5 purity.",
				"Materials", () -> new ItemStack(ItemInit.lethean_poppy_wreath.get())));

		list.add(new MaterialEntry("silver_chalice", "Silver Chalice",
				"Vessel of purification. Offer at the Altar for +5 clarity.",
				"Materials", () -> new ItemStack(ItemInit.silver_chalice.get())));

		list.add(new MaterialEntry("pale_silver_ingot", "Pale Silver Ingot",
				"Purified silver free of sanguine taint. Base material for Unstained equipment.",
				"Materials", () -> new ItemStack(ItemInit.pale_silver_ingot.get())));

		list.add(new MaterialEntry("pale_distillate", "The Pale Distillate",
				"Concentrated essence distilled from lethean poppies.",
				"Materials", () -> new ItemStack(ItemInit.pale_distillate.get())));

		list.add(new MaterialEntry("cleansed_blood_crystal_shard", "Cleansed Blood Crystal Shard",
				"A purified blood crystal shard radiating calm energy.",
				"Materials", () -> new ItemStack(ItemInit.cleansed_blood_crystal_shard.get())));

		list.add(new MaterialEntry("tome_of_the_unstained", "Tome of the Unstained",
				"Sacred text containing the teachings of the Unstained path.",
				"Materials", () -> new ItemStack(ItemInit.tome_of_the_unstained.get())));

		list.add(new MaterialEntry("pallid_icon", "Pallid Icon",
				"Holy icon depicting Our Lady of Still Waters.",
				"Materials", () -> new ItemStack(ItemInit.pallid_icon.get())));

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
				"Shield blessed by the Pallid Matron. Wards off blood magic attacks.",
				"Equipment", () -> new ItemStack(ItemInit.unstained_shield.get())));

		list.add(new MaterialEntry("self_reflection_mirror", "Self-Reflection Mirror",
				"Opens the Unstained Progress screen to view purification status.",
				"Equipment", () -> new ItemStack(ItemInit.self_reflection_mirror.get())));

		return Collections.unmodifiableList(list);
	}
}
