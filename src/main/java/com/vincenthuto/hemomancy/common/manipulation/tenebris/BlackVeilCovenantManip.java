package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlackVeilCovenantManip extends BloodManipulation {
	private static final double RADIUS = 8.0;
	private static final int DURATION_TICKS = 600;

	public BlackVeilCovenantManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = player.blockPosition();
		BlackVeilCovenantManager.addVeil(sLevel, center, RADIUS, DURATION_TICKS, player.getUUID());
		PacketHandler.sendBlackVeil(Vec3.atCenterOf(center), RADIUS, sLevel, DURATION_TICKS);
		HemomancyTendrilEffects.blackVeil(sLevel, player, center, RADIUS);

		for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, new AABB(center).inflate(RADIUS),
				e -> e != player && e.isAlive())) {
			com.vincenthuto.hemomancy.common.damage.SchoolStates.apply(player, target,
                    com.vincenthuto.hemomancy.common.damage.SchoolState.OBSCURED, 25);
		}

		world.playSound(null, center, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.55F, 0.55F);
		ManipulationParticles.accent(sLevel, EnumBloodTendency.TENEBRIS, player.position().add(0, .4, 0), net.minecraft.world.phys.Vec3.ZERO);
	        }
    }
}
