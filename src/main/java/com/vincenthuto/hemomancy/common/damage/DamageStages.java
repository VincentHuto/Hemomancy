package com.vincenthuto.hemomancy.common.damage;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.PhanstmalBloodlustArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.armor.ability.SheolicBastionBloodlustArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentEntityEventHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarsEntityEventHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEvents;
import com.vincenthuto.hemomancy.common.event.ArmorSetBonusHandler;
import com.vincenthuto.hemomancy.common.event.UnstainedMilestoneHandler;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.HematicMemoryToolEvents;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.rite.unstained.StillWatersEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * The one stage table for {@link LivingDamageEvent.Pre} (G-1, F-18). Every Hemomancy handler of the final amount runs
 * from here, in the order listed, so each reader sees every change made before it:
 * <ol>
 * <li>{@link #attackers} at HIGH: what the attacker adds.</li>
 * <li>{@link #defenders} at NORMAL: the victim's reductions, flat ones first, then the reactions that leave the amount
 * alone. Reductions stack with no cap (NQ-17-2).</li>
 * <li>{@link #deathSaves} at LOW: the one armed Last Rite.</li>
 * <li>{@link #consumers} at LOWEST: the readers of what lands (wound blood, vein strain, the Mnemonist puppet's memory).
 * A saved hit costs blood only for the damage that got through (NQ-17-1).</li>
 * </ol>
 * Ink Mantle and Cryptobiosis are Last Rites that answer a landed hit from {@code LivingDamageEvent.Post}, so a hit they
 * save still reads as landing in full; {@link #landing} caps it at what the body held.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class DamageStages {

	private DamageStages() {
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void attackers(LivingDamageEvent.Pre event) {
		ThelemicMemoryEvents.applySanguineFists(event);
		UnstainedMilestoneHandler.applyAbsolvedBonus(event);
		ManipulationReactiveEvents.applyPenumbralAmbush(event);
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void defenders(LivingDamageEvent.Pre event) {
		ThelemicMemoryEvents.applySecondPulse(event);
		UnstainedMilestoneHandler.applySilverWard(event);
		SilentArchonArmorAbilityHandler.onLivingDamage(event);
		SheolicBastionBloodlustArmorAbilityHandler.onPlayerDamage(event);
		HematicMemoryToolEvents.onLivingDamage(event);
		BloodVolumeEvents.applyIronWill(event);
		KnownManipulationEvents.onPlayerDamage(event);
		ArmorSetBonusHandler.onPlayerHurt(event);
		StillWatersEvents.onLivingHurt(event);
		PhanstmalBloodlustArmorAbilityHandler.onPlayerDamage(event);
		ScarsEntityEventHandler.onLivingHurt(event);
		com.vincenthuto.hemomancy.common.manipulation.saint.CanonGiftEvents.applyBodyStates(event);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void deathSaves(LivingDamageEvent.Pre event) {
		// A killing blow on a professor's stage is caught first: the caster is thrown out, the rite fails (2026-11-17).
		if (event.getEntity() instanceof ServerPlayer caster
				&& com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealDirector.tryCatch(caster, event)) return;
		// And on the Ringmaster's stage: the finale breaks off and the player goes home (2026-11-18).
		if (event.getEntity() instanceof ServerPlayer finalist
				&& com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.tryCatch(finalist, event)) return;
		if (event.getEntity() instanceof ServerPlayer player
				&& !ArmorSetBonusHandler.trySilentArchonDeathRefusal(player, event)
				&& !ManipulationReactiveEvents.tryPhoenixDebt(player, event)) {
			HarbingerEquipmentEntityEventHandler.tryCurvedHornSave(player, event);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumers(LivingDamageEvent.Pre event) {
		BloodVolumeEvents.onPlayerDamaged(event);
		VascularSystemEvents.onPlayerDamaged(event);
		MnemonistPuppetEvents.onLivingDamage(event);
	}

	/** Health plus absorption: what the body holds against a hit. */
	private static float buffer(Player player, LivingDamageEvent.Pre event) {
		return player.getHealth() + player.getAbsorptionAmount();
	}

	public static boolean isLethal(Player player, LivingDamageEvent.Pre event) {
		return event.getNewDamage() >= buffer(player, event);
	}

	/** A Last Rite's cut: the hit keeps only the damage that leaves the player at 1 health. */
	public static void leaveBarelyAlive(Player player, LivingDamageEvent.Pre event) {
		event.setNewDamage(Math.max(0.0F, buffer(player, event) - 1.0F));
	}

	/** The damage that gets through: the hit, but never more than the body held. */
	public static float landing(Player player, LivingDamageEvent.Pre event) {
		return Math.min(event.getNewDamage(), buffer(player, event));
	}
}
