package com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.HoldfastMorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.InkCapMorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLinks;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The vanilla hooks the Brined Court strains answer through {@link IMorphling}: air loss, breaking speed, effect
 * immunity, the sneak-use that ties a strand, knockback under Brace and Breakwater, mobs losing a dissolved host,
 * and Cling's movement (which runs on both sides, since a player's own client moves them).
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MorphlingPowerEvents {
	private MorphlingPowerEvents() {
	}

	private static ItemStack worn(Player player) {
		var cap = HemoCapabilityAccess.getEquippedMorphling(player);
		return cap.hasMorphling() && cap.getEquippedMorphling().getItem() instanceof IMorphling
				? cap.getEquippedMorphling() : ItemStack.EMPTY;
	}

	@SubscribeEvent
	public static void onBreathe(LivingBreatheEvent event) {
		if (!(event.getEntity() instanceof Player player) || event.canBreathe()) return;
		ItemStack stack = worn(player);
		if (stack.isEmpty()) return;
		double skip = ((IMorphling) stack.getItem()).airLossSkipChance(player, stack);
		if (skip > 0.0D && player.getRandom().nextDouble() < skip) event.setConsumeAirAmount(0);
	}

	@SubscribeEvent
	public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		ItemStack stack = worn(event.getEntity());
		if (stack.isEmpty()) return;
		float multiplier = ((IMorphling) stack.getItem()).breakSpeedMultiplier(event.getEntity(), stack, event.getState());
		if (multiplier != 1.0F) event.setNewSpeed(event.getNewSpeed() * multiplier);
	}

	@SubscribeEvent
	public static void onEffectApplicable(MobEffectEvent.Applicable event) {
		if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
		ItemStack stack = worn(player);
		if (!stack.isEmpty() && ((IMorphling) stack.getItem()).resistsEffect(player, stack,
				event.getEffectInstance().getEffect())) {
			event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
		}
	}

	@SubscribeEvent
	public static void onSneakUseEntity(PlayerInteractEvent.EntityInteract event) {
		Player player = event.getEntity();
		if (player.level().isClientSide || event.getHand() != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()
				|| !player.getMainHandItem().isEmpty()) return;
		// Either side may release a strand: the partner sneak-using on the host lets go too.
		if (event.getTarget() instanceof Player host && MorphlingLinks.releasePartner(host, player)) {
			consume(event);
			return;
		}
		ItemStack stack = worn(player);
		if (!stack.isEmpty() && ((IMorphling) stack.getItem()).onEquippedSneakUse(player, stack, event.getTarget(), null)) {
			consume(event);
		}
	}

	@SubscribeEvent
	public static void onSneakUseBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		if (player.level().isClientSide || event.getHand() != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()
				|| !player.getMainHandItem().isEmpty()) return;
		ItemStack stack = worn(player);
		if (!stack.isEmpty() && ((IMorphling) stack.getItem()).onEquippedSneakUse(player, stack, null, event.getPos())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
			syncLinks(player);
		}
	}

	private static void consume(PlayerInteractEvent.EntityInteract event) {
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		syncLinks(event.getEntity());
		if (event.getTarget() instanceof Player other) syncLinks(other);
	}

	/** Marks the host's synced state stale so the next tick resends its strands. */
	private static void syncLinks(Player player) {
		HemoCapabilityAccess.getEquippedMorphling(player).setActiveFlags(-1);
	}

	@SubscribeEvent
	public static void onKnockBack(LivingKnockBackEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity.level().isClientSide) return;
		if (entity instanceof Player player && HoldfastMorphlingItem.isBraced(player)) {
			event.setCanceled(true);
			return;
		}
		if (entity instanceof Player host && MorphlingStrains.is(worn(host), MorphlingStrains.HOLDFAST)
				&& HoldfastMorphlingItem.isBreakwater(host, worn(host))) {
			event.setCanceled(true);
			return;
		}
		if (HoldfastMorphlingItem.shelteringBreakwater(entity) != null) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onChangeTarget(LivingChangeTargetEvent event) {
		if (event.getNewAboutToBeSetTarget() instanceof Player player && !player.level().isClientSide
				&& InkCapMorphlingItem.isDissolved(player)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		ItemStack stack = worn(player);
		if (MorphlingStrains.is(stack, MorphlingStrains.HOLDFAST)) HoldfastMorphlingItem.applyCling(player, stack);
	}

	/** Hushbell's slower hunger, read by the exhaustion mixin. */
	public static float scaleExhaustion(Player player, float amount) {
		ItemStack stack = worn(player);
		return stack.isEmpty() ? amount : amount * ((IMorphling) stack.getItem()).exhaustionMultiplier(player, stack);
	}

	/** Holdfast's Cling: a submerged host is not pushed by currents. Read by the fluid-push mixin on both sides. */
	public static boolean resistsCurrents(Player player) {
		ItemStack stack = worn(player);
		return MorphlingStrains.is(stack, MorphlingStrains.HOLDFAST) && player.isUnderWater()
				&& HoldfastMorphlingItem.clings(player, stack);
	}
}
