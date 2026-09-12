package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class HematicCommandManip extends BloodManipulation {
	private final boolean impressment;

	public HematicCommandManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section, boolean impressment) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
		this.impressment = impressment;
	}

	private LivingEntity target(Player player) {
		EntityHitResult hit = DeadlyGazeManip.rayTraceEntities(player, 24,
				entity -> entity instanceof Mob && entity.isAlive());
		return hit != null && hit.getEntity() instanceof LivingEntity living
				&& HematicCommandManager.canCommand(living)
				&& com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.canHarm(player, living) ? living : null;
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (target(player) == null) {
			player.displayClientMessage(Component.literal("No commandable enemy in sight."), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItem, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(player instanceof ServerPlayer serverPlayer)) return;
		LivingEntity target = target(player);
		if (target == null) return;
		boolean applied = impressment
				? HematicCommandManager.impress(serverPlayer, target)
				: HematicCommandManager.rebuke(serverPlayer, target);
		if (!applied) {
			player.displayClientMessage(Component.literal("That body will not submit to hematic command."), true);
		}
	        }
    }
}
