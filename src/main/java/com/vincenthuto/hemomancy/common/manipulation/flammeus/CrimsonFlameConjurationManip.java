package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Crimson Flame Conjuration — a T1 (HUMILIS) quick manipulation that spawns
 * crimson flames at the block the player is looking at.
 * <p>
 * <b>Base range:</b> 16 blocks<br>
 * <b>Upgradable:</b> Range scales with the Sanguine Reach skill
 * (each level adds +15% range via {@link SkillPointHelper#getSanguineReachMultiplier()}).
 */
public class CrimsonFlameConjurationManip extends BloodManipulation {

	private static final double BASE_RANGE = 16.0;

	public CrimsonFlameConjurationManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	private static BlockHitResult aim(Player player, Level world) {
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 endPos = eyePos.add(player.getViewVector(1.0F).scale(range));
		return world.clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
	}

	/** Where the flames would take, or null after telling the caster why they cannot. */
	private static BlockPos flamePos(Player player, Level world) {
		BlockHitResult hitResult = aim(player, world);
		if (hitResult.getType() == HitResult.Type.MISS) {
			player.displayClientMessage(Component.translatable("message.hemomancy.crimson_flame_conjuration_manip.look_at_a_block_to_conjure"), true);
			return null;
		}
		BlockPos firePos = hitResult.getBlockPos().relative(hitResult.getDirection());
		if (!world.getBlockState(firePos).canBeReplaced() || !ManipulationCombatHelper.mayAlter(player, firePos)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.crimson_flame_conjuration_manip.cannot_place_flames_there"), true);
			return null;
		}
		return firePos;
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		return flamePos(player, player.level()) != null && super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		BlockPos firePos = flamePos(player, world);
		if (firePos == null) return;

		var flameState = BlockInit.crimson_flames.get().defaultBlockState();
		world.setBlock(firePos, flameState, 3);
		world.playSound(null, firePos, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 0.9f);

        if (world instanceof ServerLevel level) {
            com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.burst(level,
                    com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.FLAME_CONJURE,
                    Vec3.atBottomCenterOf(firePos),Vec3.atCenterOf(firePos),.7,30);
        }
            }
    }
}
