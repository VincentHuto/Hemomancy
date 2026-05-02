package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IMorphling {

//	int getTier();

	int getBloodCost();

//	int getAllegianceChance();

	// public boolean canUseModule(int rarity);

	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn);

	/**
	 * Returns the primary blood tendency this morphling prefers when being fed
	 * enzymes. Feeding a preferred enzyme grants full power contribution.
	 */
	default EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.ANIMUS;
	}

	/**
	 * Returns a secondary blood tendency this morphling benefits from.
	 * Feeding a secondary enzyme grants 75% power contribution.
	 * Non-preferred enzymes grant 50% power contribution.
	 */
	default EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.MORTEM;
	}

	/**
	 * Called every drain interval while this morphling is equipped on the player.
	 * Override to apply passive effects scaled by the morphling's maturity.
	 *
	 * @param player the player wearing this morphling
	 * @param stack  the morphling ItemStack (contains maturity NBT)
	 */
	default void onEquippedTick(Player player, ItemStack stack) {
	}

	/**
	 * Called when the player takes damage while this morphling is equipped.
	 * Use for reactive abilities like thorns, swarm retaliation, web cocoons, etc.
	 *
	 * @param player the player who was damaged
	 * @param stack  the morphling ItemStack (contains maturity NBT)
	 * @param source the source of the damage
	 * @param amount the amount of damage taken
	 */
	default void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
	}

	/**
	 * Called when the player attacks a living entity while this morphling is equipped.
	 * Use for on-hit abilities like life steal, venom strike, predator's mark, etc.
	 *
	 * @param player the player who attacked
	 * @param stack  the morphling ItemStack (contains maturity NBT)
	 * @param target the entity that was attacked
	 * @param amount the amount of damage dealt
	 */
	default void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
	}

	/**
	 * Called when the player kills a living entity while this morphling is equipped.
	 * Use for on-kill abilities like bonus XP, extra drops, etc.
	 *
	 * @param player the player who got the kill
	 * @param stack  the morphling ItemStack (contains maturity NBT)
	 * @param victim the entity that was killed
	 */
	default void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
	}

	/**
	 * Called when the player is about to take fall damage while this morphling is
	 * equipped. Return true to cancel the fall damage entirely.
	 * Use for abilities like silk tethers, parachutes, etc.
	 *
	 * @param player   the player who is falling
	 * @param stack    the morphling ItemStack (contains maturity NBT)
	 * @param distance the fall distance
	 * @return true to cancel the fall damage, false to let it proceed normally
	 */
	default boolean onEquippedFall(Player player, ItemStack stack, float distance) {
		return false;
	}

	/**
	 * Returns tooltip descriptions of the maturity bonuses this morphling grants.
	 * Each entry describes a bonus unlocked at a specific maturity level.
	 * Already-unlocked bonuses (level &lt;= currentMaturity) should be highlighted
	 * differently from locked ones.
	 *
	 * @param currentMaturity the morphling's current maturity level (0-4)
	 * @return list of Components describing bonuses
	 */
	default List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		return List.of();
	}

}
