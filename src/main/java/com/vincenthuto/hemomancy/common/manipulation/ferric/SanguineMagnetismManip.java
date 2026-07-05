package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronPillar;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SanguineMagnetismManip extends BloodManipulation {
	private static final double BASE_RANGE = 18.0D;
	private static final int DURATION_TICKS = 120;

	public SanguineMagnetismManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel serverLevel)) return;
		Vec3 center = targetPosition(player, world, BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player));
		spawnMagneticPillar(player, serverLevel, center, DURATION_TICKS);
	}

	public static EntityIronPillar spawnMagneticPillar(LivingEntity caster, ServerLevel level, Vec3 center,
			int durationTicks) {
		EntityIronPillar pillar = new EntityIronPillar(EntityInit.iron_pillar.get(), level, caster);
		pillar.moveTo(center.x, center.y, center.z, caster.getYRot(), 0.0F);
		pillar.setMagnetic(durationTicks);
		level.addFreshEntity(pillar);
		level.playSound(null, pillar.blockPosition(), SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.PLAYERS, 1.0F, 0.65F);
		return pillar;
	}

	private Vec3 targetPosition(Player player, Level world, double range) {
		Vec3 eye = player.getEyePosition(1.0F);
		Vec3 look = player.getViewVector(1.0F).normalize();
		BlockHitResult hit = world.clip(new ClipContext(eye, eye.add(look.scale(range)), ClipContext.Block.OUTLINE,
				ClipContext.Fluid.NONE, player));
		if (hit.getType() != HitResult.Type.MISS) {
			BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
			if (!world.getBlockState(pos).canBeReplaced()) {
				pos = pos.above();
			}
			return Vec3.atBottomCenterOf(pos);
		}
		return player.position().add(look.scale(8.0D));
	}
}
