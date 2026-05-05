package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Ferric Resonance — a FERRIC MEDIOCRITAS quick manipulation that expends blood
 * to restore durability to a damaged held tool or weapon.
 *
 * Sanguine Mending heals the body. Ferric Resonance heals the tool. The blood-worker does not distinguish.
 */
public class FerricResonanceManip extends BloodManipulation {

    private static final int BASE_REPAIR = 50;

    public FerricResonanceManip(String name, double cost, double alignLevel, double xpCost,
            EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
            EnumVeinSections section) {
        super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
    }

    @Override
    public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        if (heldItemMainhand.isEmpty() || !heldItemMainhand.isDamageableItem()) return;
        int currentDamage = heldItemMainhand.getDamageValue();
        if (currentDamage == 0) return;

        int repairAmount = Math.min(currentDamage, BASE_REPAIR);
        heldItemMainhand.setDamageValue(currentDamage - repairAmount);

        if (world instanceof ServerLevel sLevel) {
            sLevel.sendParticles(ParticleTypes.CRIT,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    16, 0.3, 0.3, 0.3, 0.08);
        }

        world.playSound(null, player.blockPosition(),
                SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5f, 1.4f);
    }
}
