package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.Set;

/**
 * Machines that were floorless Blood Structures and are now Working crafts.
 * Old references ({@code hemomancy:blood_structure/<id>}) resolve to
 * {@code hemomancy:cardinal_rite/working/<id>}. The retired Consecrated
 * Bloodwell structure resolves to the Founding Fane, the rite that now raises it.
 */
public final class LegacyBloodStructureIds {
	public static final String WORKING_PREFIX = "cardinal_rite/working/";
	private static final String STRUCTURE_PREFIX = "blood_structure/";
	private static final String RETIRED_BLOODWELL = "consecrated_bloodwell";
	private static final String FOUNDING_FANE = "cardinal_rite/founding_fane";

	public static final Set<String> MOVED = Set.of(
			"resonant_forge", "scarlet_vanity", "somatic_loom", "mycelial_lantern", "fungal_implantation_pylon",
			"sporitic_thurible", "enzymatic_scriptorium", "mnemonic_reliquary", "warp_chair",
			"semi_sentient_construct", "mason_effigy", "mind_spike", "runic_chisel_station", "visceral_mirror",
			"dendritic_distributor", "morphling_incubator", "vacant_effigy", "sanguine_monolith", "covenant_throne",
			"vascular_effigy");

	private LegacyBloodStructureIds() {
	}

	/** The Working craft that replaced a moved Blood Structure, or empty when the id was not moved. */
	public static Optional<ResourceLocation> remap(ResourceLocation id) {
		if (id == null || !"hemomancy".equals(id.getNamespace()) || !id.getPath().startsWith(STRUCTURE_PREFIX)) {
			return Optional.empty();
		}
		String name = id.getPath().substring(STRUCTURE_PREFIX.length());
		if (RETIRED_BLOODWELL.equals(name)) return Optional.of(Hemomancy.rloc(FOUNDING_FANE));
		return MOVED.contains(name)
				? Optional.of(Hemomancy.rloc(WORKING_PREFIX + name))
				: Optional.empty();
	}

	/** A recipe path with any moved {@code blood_structure/} entry rewritten to its Working craft path. */
	public static String remapPath(String path) {
		if (!path.startsWith(STRUCTURE_PREFIX)) {
			return MOVED.contains(path) ? WORKING_PREFIX + path : path;
		}
		String name = path.substring(STRUCTURE_PREFIX.length());
		return MOVED.contains(name) ? WORKING_PREFIX + name : path;
	}
}
