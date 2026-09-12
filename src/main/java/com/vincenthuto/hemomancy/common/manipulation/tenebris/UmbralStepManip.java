package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.armor.ArmorSetHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
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
 * Umbral Step — a T2 (MEDIOCRITAS) quick manipulation that teleports
 * the player to the block they are looking at via shadow displacement.
 * <p>
 * <b>Base range:</b> 24 blocks, scales with Sanguine Reach skill.
 * The destination must be in darkness (light level &le; 7) — you can
 * only step through shadows to reach shadowy places.
 * The player is teleported to the air space above the targeted block.
 * Fall damage is negated for the teleport landing.
 * <p>
 * An on-demand blink/teleport for mid-game mobility and exploration.
 */
public class UmbralStepManip extends BloodManipulation {

	private static final double BASE_RANGE = 24.0;
	/** Maximum combined light level allowed at the destination. */
	private static final int MAX_LIGHT_LEVEL = 7;

	public UmbralStepManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public boolean usesDefaultActivationParticles() {
		// Departure and arrival already emit smoke at the player's body.
		return false;
	}

	@Override
	public boolean ignoresCooldown(Player player) {
		return ArmorSetHelper.hasFullPhantasmalBloodlust(player);
	}

	private BlockPos destination(Player player) {
		Level world = player.level();
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		Vec3 eye = player.getEyePosition();
		BlockHitResult hit = world.clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(range)),
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (hit.getType() == HitResult.Type.MISS) return null;
		for (BlockPos candidate : new BlockPos[]{hit.getBlockPos().above(), hit.getBlockPos().relative(hit.getDirection())}) {
			if (com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.safeLanding(player, candidate)
					&& (ArmorSetHelper.hasFullPhantasmalBloodlust(player)
					|| BlackVeilCovenantManager.isDarkEnough(world, candidate, MAX_LIGHT_LEVEL))) return candidate;
		}
		return null;
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (destination(player) == null) {
			player.displayClientMessage(Component.literal("No safe shadow answers your step."), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		BlockPos landingPos = destination(player);
		if (landingPos == null) return;

		if (world instanceof ServerLevel sLevel) {
			Vec3 oldPos = player.position();
            ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.TELEPORT, oldPos, oldPos, 1, 18);
            ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.UMBRA_ARRIVAL, Vec3.atBottomCenterOf(landingPos), Vec3.atBottomCenterOf(landingPos), 1, 22);
			ManipulationParticles.accent(sLevel, EnumBloodTendency.TENEBRIS, oldPos.add(0, 1, 0), net.minecraft.world.phys.Vec3.ZERO);
		}

		// Teleport the player
		double destX = landingPos.getX() + 0.5;
		double destY = landingPos.getY();
		double destZ = landingPos.getZ() + 0.5;
		player.teleportTo(destX, destY, destZ);
		player.fallDistance = 0.0F;
		player.resetFallDistance();

		world.playSound(null, landingPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.2f);

		if (world instanceof ServerLevel sLevel) {
			ManipulationParticles.accent(sLevel, EnumBloodTendency.TENEBRIS, new Vec3(destX, destY + 1, destZ), net.minecraft.world.phys.Vec3.ZERO);
		}
	        }
    }
}
