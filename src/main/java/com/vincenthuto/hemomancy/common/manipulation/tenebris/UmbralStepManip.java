package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.armor.ArmorSetHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
	public boolean ignoresCooldown(Player player) {
		return ArmorSetHelper.hasFullPhantasmalBloodlust(player);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		boolean phantasmalStep = ArmorSetHelper.hasFullPhantasmalBloodlust(player);

		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getViewVector(1.0F);
		Vec3 endPos = eyePos.add(lookVec.scale(range));

		BlockHitResult hitResult = world.clip(new ClipContext(
				eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

		if (hitResult.getType() == HitResult.Type.MISS) {
			player.displayClientMessage(
					Component.literal("§cLook at a block to teleport!"), true);
			return;
		}

		BlockPos targetBlock = hitResult.getBlockPos();

		BlockPos landingPos = targetBlock.above();

		// Umbral Step requires darkness at the destination
		int lightAtDest = world.getMaxLocalRawBrightness(landingPos);
		if (!phantasmalStep && !BlackVeilCovenantManager.isDarkEnough(world, landingPos, MAX_LIGHT_LEVEL)) {
			player.displayClientMessage(
					Component.literal("§5The shadows there are too thin... (light level " + lightAtDest + ")"),
					true);
			return;
		}
		BlockState landingState = world.getBlockState(landingPos);
		BlockState headState = world.getBlockState(landingPos.above());

		if (!landingState.isAir() && !landingState.getCollisionShape(world, landingPos).isEmpty()) {
			// Try the face that was hit instead
			landingPos = targetBlock.relative(hitResult.getDirection());
			landingState = world.getBlockState(landingPos);
			headState = world.getBlockState(landingPos.above());
			if (!landingState.isAir() && !landingState.getCollisionShape(world, landingPos).isEmpty()) {
				player.displayClientMessage(
						Component.literal("§cNo safe landing spot!"), true);
				return;
			}
		}
		if (!headState.isAir() && !headState.getCollisionShape(world, landingPos.above()).isEmpty()) {
			player.displayClientMessage(
					Component.literal("§cNo safe landing spot!"), true);
			return;
		}

		if (world instanceof ServerLevel sLevel) {
			Vec3 oldPos = player.position();
			RandomSource random = world.random;
			for (int i = 0; i < 25; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(
								70 + random.nextFloat() * 30,
								0,
								110 + random.nextFloat() * 50)),
						oldPos.x + (random.nextDouble() - 0.5) * 0.8,
						oldPos.y + random.nextDouble() * 1.8,
						oldPos.z + (random.nextDouble() - 0.5) * 0.8,
						1, 0f, -0.1f, 0f, 0.02f);
			}
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
			RandomSource random = world.random;
			for (int i = 0; i < 25; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(
								70 + random.nextFloat() * 30,
								0,
								110 + random.nextFloat() * 50)),
						destX + (random.nextDouble() - 0.5) * 0.8,
						destY + random.nextDouble() * 1.8,
						destZ + (random.nextDouble() - 0.5) * 0.8,
						1, 0f, 0.1f, 0f, 0.02f);
			}
		}
	}
}
