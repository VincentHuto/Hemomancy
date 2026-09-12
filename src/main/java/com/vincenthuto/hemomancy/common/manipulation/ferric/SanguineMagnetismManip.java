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
import net.minecraft.world.level.Level;
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
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		return FerricPlacement.aimed(player, FerricConstructShapes.Kind.PILLAR,
				BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player)) != null
				&& super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel serverLevel)) return;
		var placement = FerricPlacement.aimed(player, FerricConstructShapes.Kind.PILLAR,
				BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player));
		if (placement != null) spawnMagneticPillar(player, serverLevel, placement.origins().getFirst(), DURATION_TICKS);
	        }
    }

	public static EntityIronPillar spawnMagneticPillar(LivingEntity caster, ServerLevel level, Vec3 center,
			int durationTicks) {
		EntityIronPillar pillar = new EntityIronPillar(EntityInit.iron_pillar.get(), level, caster);
		pillar.moveTo(center.x, center.y, center.z, caster.getYRot(), 0.0F);
		pillar.setMagnetic(durationTicks);
		pillar.configure(caster, FerricConstructShapes.Kind.PILLAR,
				net.minecraft.core.Direction.fromYRot(caster.getYRot()), durationTicks);
		level.addFreshEntity(pillar);
		level.playSound(null, pillar.blockPosition(), SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.PLAYERS, 1.0F, 0.65F);
		return pillar;
	}

}
