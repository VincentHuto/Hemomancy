package com.vincenthuto.hemomancy.common.block.unstained.crafting;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Passive ward fixture: shelters Unstained players and suppresses blood-born creatures. */
public class VerdigrisLatticeBlock extends Block {
	public VerdigrisLatticeBlock(Properties properties) { super(properties); }

	@Override
	protected boolean isRandomlyTicking(BlockState state) { return true; }

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		AABB ward = new AABB(pos).inflate(8.0);
		for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, ward)) {
			if (living instanceof Player player && HemoCapabilityAccess.getUnstainedProgress(player)
					.map(progress -> progress.hasBegunPurification()).orElse(false)) {
				living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 0, true, false));
			} else if (living.getType().is(EntityInit.HEMOMANCY_MOB)) {
				living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 0, true, true));
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 0, true, false));
			}
		}
	}

	public static boolean hasActiveLattice(Level level, BlockPos pos, int radius) {
		for (BlockPos check : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius),
				pos.offset(radius, radius, radius))) {
			if (level.getBlockState(check).is(BlockInit.verdigris_lattice.get())) return true;
		}
		return false;
	}
}
