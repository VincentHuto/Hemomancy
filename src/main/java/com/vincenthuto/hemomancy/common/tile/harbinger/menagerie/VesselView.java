package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.HematicAcclimationRules;
import com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch;
import com.vincenthuto.hemomancy.common.tile.BloodDrawRights;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * What a vessel's screen is told (core-loop round two, P6): the menu's data slots, worked out on the server for one
 * viewer each time the menu broadcasts. A menu sends each slot as a short, so every value is scaled to stay under
 * 32,767: Vigor in hundredths, yields in hundredths, the feed price in tenths, brood progress in thousandths, and the
 * brood cost capped at {@link BroodRules#MAX_SHOWN_COST}.
 */
public final class VesselView implements ContainerData {
	public static final int VIGOR_X100 = 0;
	public static final int BUFFER = 1;
	public static final int CAPACITY = 2;
	public static final int FLAGS = 3;
	/** One draw's Temper and blood at the viewer's acclimation, in hundredths; 0 unless the viewer studied the kind. */
	public static final int TEMPER_YIELD_X100 = 4;
	public static final int BLOOD_YIELD_X100 = 5;
	public static final int FEED_PRICE_X10 = 6;
	public static final int BROOD_STATE = 7;
	public static final int BROOD_PERMILLE = 8;
	public static final int BROOD_COST = 9;
	/** Why the brood button would do nothing for this viewer now, as a {@link BroodRules.Refusal} ordinal. */
	public static final int BROOD_REFUSAL = 10;
	public static final int COUNT = 11;

	public static final int OWNED = 1;
	public static final int TENDED = 1 << 1;
	public static final int HUSK = 1 << 2;
	public static final int MAY_DRAW = 1 << 3;
	public static final int HOSTILE = 1 << 4;
	public static final int BREEDS = 1 << 5;
	public static final int PAIRED = 1 << 6;
	public static final int BEARER = 1 << 7;

	private final SpecimenVessel vessel;
	private final ServerPlayer viewer;

	public VesselView(SpecimenVessel vessel, ServerPlayer viewer) {
		this.vessel = vessel;
		this.viewer = viewer;
	}

	@Override
	public int get(int slot) {
		KeptSpecimen kept = vessel.kept();
		BlockEntity blockEntity = (BlockEntity) vessel;
		Level level = blockEntity.getLevel();
		if (level == null) return 0;
		return switch (slot) {
			case VIGOR_X100 -> (int) Math.round(kept.vigor() * 100.0D);
			case BUFFER -> (int) Math.round(Math.min(Short.MAX_VALUE, vessel.buffer().getBloodVolume()));
			case CAPACITY -> (int) Math.round(Math.min(Short.MAX_VALUE, vessel.buffer().capacity()));
			case FLAGS -> flags(level, kept, blockEntity);
			case TEMPER_YIELD_X100 -> drawYield(level, kept, true);
			case BLOOD_YIELD_X100 -> drawYield(level, kept, false);
			case FEED_PRICE_X10 -> (int) Math.round(VatVigorRules.mlPerVigor(kept.maxHealth()) * 10.0D);
			case BROOD_STATE -> SpecimenBrood.state(level, vessel).ordinal();
			case BROOD_PERMILLE -> {
				double[] brood = SpecimenBrood.paidAndCost(level, vessel);
				yield (int) Math.round(BroodRules.progress(brood[0], brood[1]) * 1000.0D);
			}
			case BROOD_COST -> (int) Math.min(BroodRules.MAX_SHOWN_COST, Math.round(BroodRules.broodCost(kept.maxHealth())));
			case BROOD_REFUSAL -> kept.paired()
					? (BloodDrawRights.mayDraw(viewer, blockEntity) ? 0 : BroodRules.Refusal.NOT_YOURS.ordinal())
					: SpecimenBrood.find(level, vessel, viewer).refusal().ordinal();
			default -> 0;
		};
	}

	private int flags(Level level, KeptSpecimen kept, BlockEntity blockEntity) {
		int flags = 0;
		if (vessel.buffer().owner() != null) flags |= OWNED;
		if (kept.tended(level.getGameTime())) flags |= TENDED;
		if (kept.husk()) flags |= HUSK;
		if (BloodDrawRights.mayDraw(viewer, blockEntity)) flags |= MAY_DRAW;
		if (kept.hostile()) flags |= HOSTILE;
		if (SpecimenBrood.breeds(kept.type())) flags |= BREEDS;
		if (kept.paired()) flags |= PAIRED;
		if (kept.broodBearer()) flags |= BEARER;
		return flags;
	}

	private int drawYield(Level level, KeptSpecimen kept, boolean temper) {
		ResourceLocation type = kept.typeId();
		if (!kept.living() || !BloodResearch.studied(viewer, type)) return 0;
		double acclimation = HematicAcclimationRules.multiplier(HemoCapabilityAccess.getPowerGuardrails(viewer)
				.hematicExposure(String.valueOf(type), level.getGameTime()));
		double value = temper ? VatVigorRules.temperPerDraw(kept.vigor(), acclimation)
				: VatVigorRules.bloodPerDraw(kept.vigor(), acclimation);
		return (int) Math.round(value * 100.0D);
	}

	@Override
	public void set(int slot, int value) {
		// Worked out from the vessel each broadcast; nothing to store.
	}

	@Override
	public int getCount() {
		return COUNT;
	}
}
