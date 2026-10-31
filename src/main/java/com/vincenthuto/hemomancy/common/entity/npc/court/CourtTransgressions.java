package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenEntity;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import javax.annotation.Nullable;

/**
 * Wrongs against the Giving Light (Brined Court Track 10, Tier 2; T1). Each listed act records a transgression through
 * {@link CourtLife#recordTransgression} and wakes the Bloom Warden ({@link BloomWardenEntity#wake}): breaking the
 * Garden's growth ({@link #GARDEN_GROWTH}) inside the Lighthouse Garden, taking from the undercroft (the petrified, and
 * Marimo's moss, even though both refuse to come away), and raising harm at one of the Garden's residents. The same
 * act repeated within {@link BloomWardenRules#ACT_DEBOUNCE} ticks is one act. Ordinary visiting, eating the food the
 * Garden gives and passing quietly through wake nothing. The shunned, Creative players and spectators never wake it.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CourtTransgressions {
	/** The Garden's growth, beds, arbor growth and moss: breaking any of it inside the Garden is a wrong. */
	public static final TagKey<Block> GARDEN_GROWTH = TagKey.create(Registries.BLOCK, Hemomancy.rloc("court_growth/giving_light"));

	private CourtTransgressions() {
	}

	/** The wrong breaking {@code state} at the Garden would be, or null for a block that is nobody's. */
	@Nullable
	public static String actFor(BlockState state) {
		if (state.is(BlockInit.petrified_follower.get()) || state.is(BlockInit.marimo_moss.get())
				|| state.is(BlockInit.marimo_heart.get())) {
			return BloomWardenRules.UNDERCROFT;
		}
		return state.is(GARDEN_GROWTH) ? BloomWardenRules.GROWTH : null;
	}

	/**
	 * Records {@code act} against the Giving Light and wakes the Warden for {@code player}, unless the player can't wake
	 * it or this is the same act again inside the debounce. Returns the Warden that answers, or null when nothing did.
	 */
	@Nullable
	public static BloomWardenEntity transgress(ServerPlayer player, String act, BlockPos where) {
		if (!BloomWardenRules.canWake(CourtLife.isShunned(player), player.getAbilities().instabuild, player.isSpectator(),
				player.isAlive())) {
			return null;
		}
		CourtProgress progress = CourtLife.progress(player);
		long now = player.serverLevel().getGameTime();
		if (!BloomWardenRules.isNewAct(progress.transgressionAct(), progress.transgressionTime(), act, now)) return null;
		CourtLife.recordTransgression(player, act);
		return BloomWardenEntity.wake(player, act, where);
	}

	/** Breaking the Garden's growth or taking from the undercroft, refused or not: the attempt is the wrong. */
	@SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
	public static void onBreak(BlockEvent.BreakEvent event) {
		if (!(event.getPlayer() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) return;
		String act = actFor(event.getState());
		if (act == null || CourtLife.houseAt(level, event.getPos()) != CourtHouse.GIVING_LIGHT) return;
		transgress(player, act, event.getPos());
	}

	/**
	 * Reaching for Marimo's moss or heart. They can never be mined outside Creative, so no break ever lands and
	 * {@link #onBreak} never sees them; the first swing at one inside the Garden is the wrong instead.
	 */
	@SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
	public static void onReach(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;
		if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) return;
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		if (state.getDestroySpeed(level, pos) >= 0.0F) return;
		String act = actFor(state);
		if (act == null || CourtLife.houseAt(level, pos) != CourtHouse.GIVING_LIGHT) return;
		transgress(player, act, pos);
	}

	/** A player's harm reached a protected resident of the Garden (called from {@link CourtMemberEntity#hurt}). */
	public static void harmAttempted(CourtMemberEntity resident, ServerPlayer player) {
		if (resident instanceof BloomWardenEntity) return;
		boolean garden = resident.role().house() == CourtHouse.GIVING_LIGHT
				|| CourtLife.houseAt(player.serverLevel(), resident.blockPosition()) == CourtHouse.GIVING_LIGHT;
		if (garden) transgress(player, BloomWardenRules.HARM, resident.blockPosition());
	}
}
