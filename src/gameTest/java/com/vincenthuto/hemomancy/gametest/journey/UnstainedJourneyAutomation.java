package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.event.ConsecrationHandler;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances.Observance;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.tile.crafting.PallidRetortBlockEntity;
import com.vincenthuto.hemomancy.common.tile.crafting.StillwaterCondenserBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Performs one real server-side action for each Unstained operator checkpoint. */
public final class UnstainedJourneyAutomation {
	private UnstainedJourneyAutomation() { }

	public static void perform(ServerPlayer player, UnstainedJourneyStage stage, BlockPos origin) {
		switch (stage) {
			case NOVITIATE_GATHER_REMEDIES -> fulfill(player, Observance.NOVITIATE_GATHER_REMEDIES, true);
			case NOVITIATE_GENTLE_SEPARATION -> distillNovitiateSolution(player, origin);
			case NOVITIATE_STILLWATER_LABOR -> condenseNovitiateDew(player, origin);
			case NOVITIATE_CLEAN_LABOR -> consecrateNovitiateBlocks(player, origin);
			case NOVITIATE_SHELTER_AFFLICTED -> protectNovitiate(player, origin);
			case PODIUM_SUPPRESSION -> useFixtureBlock(player, origin.above());
			case LETHEAN_BAPTISM -> rite(player, origin, "lethean_baptism");
			case GHOST_PIPE_OBSERVANCE -> fulfill(player, Observance.GATHER_GHOST_PIPE, false);
			case TAINTED_ACOLYTE_OBSERVANCES -> {
				fulfill(player, Observance.WEAVE_WREATH, false);
				fulfill(player, Observance.PREPARE_HEMOLYTIC, false);
			}
			case SILVER_VEIL -> rite(player, origin, "silver_veil");
			case CLEANSING_OBSERVANCES -> {
				produceDew(player, origin, 2);
				fulfill(player, Observance.CONDENSE_STILL_WATERS, false);
				fulfill(player, Observance.PLATE_THE_WARD, false);
			}
			case PALLID_ICON_OBSERVANCE -> fulfill(player, Observance.BEAR_PALLID_ICON, false);
			case SILTHMERE_REMEMBRANCE -> rite(player, origin, "silthmeres_remembrance");
			case CLOSED_VEIN -> rite(player, origin, "closed_vein");
			case CONSECRATED_COPPER_OBSERVANCE -> fulfill(player, Observance.CONSECRATE_COPPER, false);
			case CLARITY_PREPARED -> useFixtureBlock(player, origin.above());
			case CLARITY_ASCENSION -> rite(player, origin, "clarity_ascension");
			case GLASS_LUNGS -> rite(player, origin, "glass_lungs");
			case CHALICE_OBSERVANCE -> fulfill(player, Observance.OFFER_CHALICE, false);
			case DISCERNING -> syncProgress(player);
			case PALE_VIGIL -> rite(player, origin, "pale_vigil");
			case MOON_WASHED_COPPER -> rite(player, origin, "moon_washed_copper");
			case PALE_WATCH_OBSERVANCE -> fulfill(player, Observance.RING_THE_PALE_WATCH, false);
			case RESOLUTE -> syncProgress(player);
			case ENLIGHTENED -> useFixtureBlock(player, origin.above());
			case LETHEAN_FONT -> rite(player, origin, "lethean_font");
			case COMPLETE -> { }
		}
	}

	private static void fulfill(ServerPlayer player, Observance observance, boolean beginNovitiate) {
		if (beginNovitiate) UnstainedObservances.beginNovitiate(player);
		else UnstainedObservances.handle(player, observance);
		UnstainedObservances.handle(player, observance);
	}

	private static void distillNovitiateSolution(ServerPlayer player, BlockPos origin) {
		UnstainedObservances.handle(player, Observance.NOVITIATE_GENTLE_SEPARATION);
		BlockPos pos = origin.above();
		if (!(player.serverLevel().getBlockEntity(pos) instanceof PallidRetortBlockEntity retort)) {
			throw new IllegalStateException("Missing Pallid Retort");
		}
		HemoJourneyFixtures.set(player, pos.below(), Blocks.FIRE);
		retort.setItem(PallidRetortBlockEntity.SLOT_INPUT, new ItemStack(ItemInit.lethean_dew.get()));
		retort.setItem(PallidRetortBlockEntity.SLOT_CATALYST,
				new ItemStack(com.vincenthuto.hemomancy.common.init.BlockInit.ghost_pipe.get()));
		for (int tick = 0; tick < 220; tick++) {
			PallidRetortBlockEntity.serverTick(player.serverLevel(), pos,
					player.serverLevel().getBlockState(pos), retort);
		}
		ItemStack result = retort.removeItemNoUpdate(PallidRetortBlockEntity.SLOT_RESULT);
		if (!result.is(ItemInit.hemolytic_solution.get())) throw new IllegalStateException("Pallid Retort produced no Hemolytic Solution");
		retort.awardUsedRecipesAndPopExperience(player);
		player.addItem(result);
		UnstainedObservances.handle(player, Observance.NOVITIATE_GENTLE_SEPARATION);
	}

	private static void condenseNovitiateDew(ServerPlayer player, BlockPos origin) {
		UnstainedObservances.handle(player, Observance.NOVITIATE_STILLWATER_LABOR);
		int produced = produceDew(player, origin, 4);
		UnstainedObservances.recordDewProduced(player, produced);
		UnstainedObservances.handle(player, Observance.NOVITIATE_STILLWATER_LABOR);
	}

	private static int produceDew(ServerPlayer player, BlockPos origin, int bottles) {
		BlockPos pos = origin.above();
		if (!(player.serverLevel().getBlockEntity(pos) instanceof StillwaterCondenserBlockEntity condenser)) {
			throw new IllegalStateException("Missing Stillwater Condenser");
		}
		condenser.setItem(StillwaterCondenserBlockEntity.SLOT_BOTTLES,
				new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE, bottles));
		for (int tick = 0; tick < bottles * 110; tick++) {
			StillwaterCondenserBlockEntity.serverTick(player.serverLevel(), pos,
					player.serverLevel().getBlockState(pos), condenser);
		}
		ItemStack dew = condenser.removeItemNoUpdate(StillwaterCondenserBlockEntity.SLOT_DEW);
		if (dew.isEmpty()) throw new IllegalStateException("Stillwater Condenser produced no Lethean Dew");
		int count = dew.getCount();
		player.addItem(dew);
		return count;
	}

	private static void consecrateNovitiateBlocks(ServerPlayer player, BlockPos origin) {
		UnstainedObservances.handle(player, Observance.NOVITIATE_CLEAN_LABOR);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.consecrated_copper_ingot.get(), 8));
		for (int i = 0; i < 8; i++) {
			BlockPos pos = origin.offset((i % 4) - 2, 1, 2 + i / 4);
			BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
			ConsecrationHandler.onRightClickBlock(new PlayerInteractEvent.RightClickBlock(
					player, InteractionHand.MAIN_HAND, pos, hit));
		}
		UnstainedObservances.handle(player, Observance.NOVITIATE_CLEAN_LABOR);
	}

	private static void protectNovitiate(ServerPlayer player, BlockPos origin) {
		UnstainedObservances.handle(player, Observance.NOVITIATE_SHELTER_AFFLICTED);
		rite(player, origin, "still_waters");
		UnstainedObservances.handle(player, Observance.NOVITIATE_SHELTER_AFFLICTED);
	}

	private static void rite(ServerPlayer player, BlockPos origin, String id) {
		if (!UnstainedCardinalRiteEvents.completeRite(player.serverLevel(), player, origin.above(),
				"cardinal_rite/" + id)) {
			throw new IllegalStateException("Unstained rite did not complete: " + id);
		}
		if ("glass_lungs".equals(id)) touchDrop(player, origin, ItemInit.lethean_chalice.get());
		if ("moon_washed_copper".equals(id)) touchDrop(player, origin, ItemInit.pale_silver_bell.get());
	}

	private static void touchDrop(ServerPlayer player, BlockPos origin, Item item) {
		player.serverLevel().getEntitiesOfClass(ItemEntity.class, HemoJourneyFixtures.bounds(origin),
				entity -> entity.getItem().is(item)).getFirst().playerTouch(player);
	}

	private static void useFixtureBlock(ServerPlayer player, BlockPos pos) {
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
		player.serverLevel().getBlockState(pos).useItemOn(player.getMainHandItem(), player.serverLevel(),
				player, InteractionHand.MAIN_HAND, hit);
	}

	private static void syncProgress(ServerPlayer player) {
		var progress = HemoCapabilityAccess.requireUnstainedProgress(player);
		UnstainedProgressEvents.syncProgress(player, progress);
	}
}
