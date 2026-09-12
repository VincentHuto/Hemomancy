package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Sanguine Mending — a T1 (HUMILIS) continuous manipulation that repairs
 * the player's held item by infusing it with blood.
 * <p>
 * Restores 50 durability points to the held item per paid channel pulse.
 * Only works on damageable items that are currently damaged.
 */
public class SanguineMendingManip extends BloodManipulation {

	private static final int REPAIR_AMOUNT = 50;

	public SanguineMendingManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItemMainhand, float chargeTicks) {
		return !heldItemMainhand.isEmpty() && heldItemMainhand.isDamageableItem() && heldItemMainhand.isDamaged();
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (heldItemMainhand.isEmpty() || !heldItemMainhand.isDamageableItem()
				|| !heldItemMainhand.isDamaged()) {
			return;
		}

		int currentDamage = heldItemMainhand.getDamageValue();
		int repaired = Math.min(currentDamage, REPAIR_AMOUNT);
		heldItemMainhand.setDamageValue(currentDamage - repaired);

		world.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5f, 1.5f);

		if (world instanceof ServerLevel sLevel) {
			ManipulationVisuals.attached(player, ManipulationVisuals.Form.MENDING, 1, 18, 1);
		}
	        }
    }
}
