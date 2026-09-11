package com.vincenthuto.hemomancy.common.manipulation.lux;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

public class LumenSutureManip extends BloodManipulation {
	private static final double RADIUS = 12.0;

	public LumenSutureManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		Player target = world.getEntitiesOfClass(Player.class, new AABB(player.blockPosition()).inflate(RADIUS),
						p -> p != player && p.isAlive() && !p.isSpectator() && ManipulationCombatHelper.allied(player, p) && p.getHealth() < p.getMaxHealth())
				.stream()
				.min(Comparator.comparingDouble((Player p) -> p.distanceToSqr(player)).thenComparing(Player::getUUID))
				.orElse(player);

		target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 1, false, true));
		target.removeEffect(MobEffects.BLINDNESS);
		target.removeEffect(MobEffects.WITHER);
		target.heal(2.0F);
		world.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.7F, 1.8F);

		if (world instanceof ServerLevel sLevel) {
			HemomancyTendrilEffects.lumenSuture(player, target);
            ManipulationVisuals.attached(target, ManipulationVisuals.Form.LUX_MENDING, .5, 28, 1);
			ManipulationParticles.accent(sLevel, EnumBloodTendency.LUX, target.position().add(0, 1, 0), net.minecraft.world.phys.Vec3.ZERO);
		}
	}
}
