package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

import static com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.*;
import static com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain.NO_LAST_RITE;

/**
 * The Morphling strain table: the eight Brined Court house strains, shallow to deep. Every per-strain
 * list in the mod reads this table; {@code MorphlingStrainTableGuardrailTest} rejects a hard-coded
 * {@code morphling_<strain>} id anywhere else but {@code ItemInit}.
 */
public final class MorphlingStrains {
	public static final MorphlingStrain SUNBURST = new MorphlingStrain("sunburst", LUX, FERRIC,
			CourtHouse.GIVING_LIGHT, MorphlingHomeConditions::sunburst, true, NO_LAST_RITE, 0, MorphlingPolypLayer.SUN_ALGA, 0xFFF0B43C, 0, 0x57F0A030);
	public static final MorphlingStrain HOLDFAST = new MorphlingStrain("holdfast", FERRIC, ANIMUS,
			CourtHouse.HOLDING_STONE, MorphlingHomeConditions::holdfast, true, NO_LAST_RITE, 0, MorphlingPolypLayer.CORALLINE_ALGA, 0xFFA08C80, 1, 0x4D74675C);
	public static final MorphlingStrain MERMAIDS_PURSE = new MorphlingStrain("mermaids_purse", ANIMUS, DUCTILIS,
			CourtHouse.SHARED_WAKE, MorphlingHomeConditions::mermaidsPurse, true, NO_LAST_RITE, 0, MorphlingPolypLayer.GUT_BACTERIA, 0xFF4A6FD8, 2, 0x5C3352CC);
	public static final MorphlingStrain DEAD_MANS_ROPE = new MorphlingStrain("dead_mans_rope", DUCTILIS, TENEBRIS,
			CourtHouse.WOVEN_CURRENT, MorphlingHomeConditions::deadMansRope, false, NO_LAST_RITE, 0, MorphlingPolypLayer.LIGHT_BACTERIA, 0xFF9A88EA, 3, 0x618C7AE6);
	public static final MorphlingStrain INK_CAP = new MorphlingStrain("ink_cap", TENEBRIS, DUCTILIS,
			CourtHouse.VEILED_FOLD, MorphlingHomeConditions::inkCap, false, LastRiteHelper.INK_MANTLE_ID, PrimalMorphlingRules.APEX_LEVEL,
			MorphlingPolypLayer.BLACK_YEAST, 0xFF7A7488, 4, 0x6B141219);
	public static final MorphlingStrain HUSHBELL = new MorphlingStrain("hushbell", CONGEATIO, TENEBRIS,
			CourtHouse.STILL_BELL, MorphlingHomeConditions::hushbell, true, LastRiteHelper.CRYPTOBIOSIS_ID, PrimalMorphlingRules.PRIMAL_LEVEL,
			MorphlingPolypLayer.COLD_BACTERIA, 0xFF9DB8CC, 5, 0x5C8CA8C2);
	public static final MorphlingStrain MARROWPLUME = new MorphlingStrain("marrowplume", MORTEM, CONGEATIO,
			CourtHouse.LAST_MARROW, MorphlingHomeConditions::marrowplume, true, NO_LAST_RITE, 0, MorphlingPolypLayer.BONE_BACTERIA, 0xFFD8343C, 6, 0x57EADFD2);
	public static final MorphlingStrain KINBUD = new MorphlingStrain("kinbud", FLAMMEUS, MORTEM,
			CourtHouse.KINDLING, MorphlingHomeConditions::kinbud, false, NO_LAST_RITE, 0, MorphlingPolypLayer.VENT_BACTERIA, 0xFFEC7A2C, 7, 0x5CF2D6CA);

	/** Shallow to deep; also the creative and Bestiary order. */
	public static final List<MorphlingStrain> ALL = List.of(
			SUNBURST, HOLDFAST, MERMAIDS_PURSE, DEAD_MANS_ROPE, INK_CAP, HUSHBELL, MARROWPLUME, KINBUD);

	private MorphlingStrains() {
	}

	public static Optional<MorphlingStrain> byId(String id) {
		return ALL.stream().filter(strain -> strain.id().equals(id)).findFirst();
	}

	/** Looks a strain up by its item's registry path ({@code morphling_<id>}). */
	public static Optional<MorphlingStrain> byItemPath(String itemPath) {
		return ALL.stream().filter(strain -> strain.itemPath().equals(itemPath)).findFirst();
	}

	public static Optional<MorphlingStrain> byPolypFamily(MorphlingPolypLayer family) {
		return ALL.stream().filter(strain -> strain.polypFamily() == family).findFirst();
	}

	public static Optional<MorphlingStrain> of(Item item) {
		return item instanceof HouseMorphlingItem house ? Optional.of(house.strain()) : Optional.empty();
	}

	public static Optional<MorphlingStrain> of(ItemStack stack) {
		return stack == null || stack.isEmpty() ? Optional.empty() : of(stack.getItem());
	}

	public static boolean is(ItemStack stack, MorphlingStrain strain) {
		return of(stack).filter(strain::equals).isPresent();
	}

	public static List<String> itemPaths() {
		return ALL.stream().map(MorphlingStrain::itemPath).toList();
	}

	/** True when a Primal stack of this strain may be hosted in a Morphling Cradle. */
	public static boolean isCradlePrimal(ItemStack stack) {
		return of(stack).map(MorphlingStrain::cradlePrimal).orElse(false);
	}

	/** The Last Rite source this stack arms at its current maturity, or empty. */
	public static String lastRiteFor(ItemStack stack) {
		Optional<MorphlingStrain> strain = of(stack);
		if (strain.isEmpty() || !strain.get().hasLastRite()) {
			return NO_LAST_RITE;
		}
		return MorphlingItem.getMaturityLevel(stack) >= strain.get().lastRiteStage()
				? strain.get().lastRiteId() : NO_LAST_RITE;
	}
}
