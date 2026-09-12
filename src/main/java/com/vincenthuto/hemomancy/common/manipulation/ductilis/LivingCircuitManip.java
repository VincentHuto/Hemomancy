package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;

public class LivingCircuitManip extends BloodManipulation {
	public LivingCircuitManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	private List<Player> recipients(Player player) {
		return player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(10),
				candidate -> candidate != player && candidate.isAlive() && !candidate.isSpectator()
						&& ManipulationCombatHelper.allied(player, candidate)).stream()
				.sorted(Comparator.comparingDouble((Player ally) -> player.distanceToSqr(ally)).thenComparing(Player::getUUID))
				.limit(3).toList();
	}

	@Override
	public boolean canContinueChannel(Player player, Level world) {
		if (!recipients(player).isEmpty()) return true;
		player.displayClientMessage(net.minecraft.network.chat.Component.literal("Living Circuit needs a nearby teammate."), true);
		return false;
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		return canContinueChannel(player, player.level()) && super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : heldTicks / getRequiredChargeTicks())) {

		if (!(world instanceof ServerLevel level)) return;
		for (Player ally : recipients(player)) {
			ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 1, false, true));
			ally.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 25, 1, false, true));
			ManipulationReactiveEvents.armLivingCircuit(ally);
			DuctilisLightningEffects.livingCircuit(player, ally);
		}
	        }
    }
}
