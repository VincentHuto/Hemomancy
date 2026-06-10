package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class UmbralReversalManip extends BloodManipulation {
	private static final int MAX_LIGHT_LEVEL = 7;

	public UmbralReversalManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		Vec3 horizontal = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z);
		if (horizontal.lengthSqr() < 0.01) horizontal = new Vec3(0, 0, 1);
		horizontal = horizontal.normalize();
		BlockPos origin = player.blockPosition();
		BlockPos destination = null;
		for (int i = 8; i >= 2; i--) {
			BlockPos candidate = BlockPos.containing(player.position().subtract(horizontal.scale(i)));
			if (isSafe(world, candidate) && BlackVeilCovenantManager.isDarkEnough(world, candidate, MAX_LIGHT_LEVEL)) {
				destination = candidate;
				break;
			}
		}

		if (destination == null) {
			player.displayClientMessage(Component.literal("§5No dark reversal point answers."), true);
			return;
		}

		for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, new AABB(origin).inflate(4.0),
				e -> e != player && e.isAlive())) {
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, true));
		}
		for (int i = 0; i < 40; i++) {
			sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(60, 0, 105)),
					origin.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 1.5,
					origin.getY() + world.random.nextDouble() * 2.0,
					origin.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 1.5,
					1, 0, 0, 0, 0.02);
		}
		player.teleportTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);
		player.fallDistance = 0;
		player.resetFallDistance();
		world.playSound(null, destination, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 0.7F);
	}

	private static boolean isSafe(Level world, BlockPos pos) {
		BlockState feet = world.getBlockState(pos);
		BlockState head = world.getBlockState(pos.above());
		BlockState floor = world.getBlockState(pos.below());
		return (feet.isAir() || feet.getCollisionShape(world, pos).isEmpty())
				&& (head.isAir() || head.getCollisionShape(world, pos.above()).isEmpty())
				&& !floor.getCollisionShape(world, pos.below()).isEmpty();
	}
}
