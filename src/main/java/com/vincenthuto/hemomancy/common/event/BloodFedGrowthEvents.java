package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGround;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGrowthRules;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

/**
 * The one crop subscriber (core-loop Track 7). Every crop's growth check asks how well its ground is fed
 * ({@link BloodFedGround}) and {@link BloodFedGrowthRules#verdict} answers: fed ground buys extra growth, a lit cold
 * run holds a crop back, and a crop that needs blood (the gourd) does not advance on unfed ground, by its own growth
 * or by bone meal. It reads the vanilla and NeoForge events only, so other mods' crops behave the same way.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BloodFedGrowthEvents {
	private BloodFedGrowthEvents() {
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onCropGrow(CropGrowEvent.Pre event) {
		if (!(event.getLevel() instanceof ServerLevel level) || event.getResult() != CropGrowEvent.Pre.Result.DEFAULT) return;
		boolean needsBlood = event.getState().is(BloodFedGround.NEEDS_BLOOD);
		boolean chilled = GroundNetwork.chilledBeside(level, event.getPos());
		BloodFedGround.Feeding feeding = chilled ? BloodFedGround.Feeding.UNFED : BloodFedGround.feeding(level, event.getPos());
		switch (BloodFedGrowthRules.verdict(feeding.level(), needsBlood, chilled, level.random.nextFloat())) {
			case GROW -> {
				// A basin's bonus is paid from its tank; with too little left the crop keeps its own roll. A ripe plant
				// (a stem that can only fruit, and may have nowhere to) buys nothing, so the basin never pays for it.
				if (feeding.basin() == null) event.setResult(CropGrowEvent.Pre.Result.GROW);
				else if (!BloodFedGround.ripe(event.getState()) && feeding.basin().spendGrowth()) {
					event.setResult(CropGrowEvent.Pre.Result.GROW);
				}
			}
			case DO_NOT_GROW -> event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
			default -> {
			}
		}
	}

	/**
	 * A ripe crop cut from ground a fresh planting basin or a feeding run feeds (level 2) is the harvest first, for a
	 * player whose blood is awake. Lowest priority and only if nothing cancelled the break, so it marks a real harvest.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onHarvest(BlockEvent.BreakEvent event) {
		if (event.isCanceled() || !(event.getPlayer() instanceof ServerPlayer player)
				|| !(event.getLevel() instanceof ServerLevel level)) return;
		if (!BloodFedGround.ripe(event.getState()) || !HemoCapabilityAccess.getBloodVolume(player).isActive()) return;
		if (BloodFedGround.level(level, event.getPos()) < BloodFedGrowthRules.BASIN) return;
		HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_BASIN_HARVEST);
	}

	/**
	 * Bone meal cannot advance a crop that needs blood on unfed ground. The client predicts from what it can see
	 * ({@link BloodFedGround#visiblyFed}), so it does not swing and spend bone meal the server will refuse; soil fed only
	 * by a stamp reads unfed there, and the server's own answer corrects that under-prediction.
	 */
	@SubscribeEvent
	public static void onBonemeal(BonemealEvent event) {
		if (!event.getState().is(BloodFedGround.NEEDS_BLOOD)) return;
		if (event.getLevel() instanceof ServerLevel level) {
			if (BloodFedGround.level(level, event.getPos()) == BloodFedGrowthRules.UNFED) event.setSuccessful(false);
		} else if (!BloodFedGround.visiblyFed(event.getLevel(), event.getPos())) {
			event.setSuccessful(false);
		}
	}
}
