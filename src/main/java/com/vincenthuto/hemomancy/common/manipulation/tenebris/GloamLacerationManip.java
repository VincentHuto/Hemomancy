package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
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

import javax.annotation.Nullable;

public class GloamLacerationManip extends BloodManipulation {
	private static final double BASE_RANGE = 7.0D;
	private static final double SLASH_DOT = 0.72D;
	private static final int MAX_LIGHT_LEVEL = 7;
	private static final float BASE_DAMAGE = 3.5F;
	private static final float AMBUSH_BONUS = 2.5F;

	public GloamLacerationManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		LivingEntity target = findTarget(player, world, range);
		boolean ambush = player.hasEffect(MobEffects.INVISIBILITY)
				|| BlackVeilCovenantManager.isDarkEnough(world, player.blockPosition(), MAX_LIGHT_LEVEL);

		if (target != null) {
			target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 140, 0, false, true));
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0, false, true));
			float damage = (float) ((BASE_DAMAGE + (ambush ? AMBUSH_BONUS : 0.0F))
					* SkillPointHelper.getCrimsonMasteryMultiplier(player));
			target.hurt(world.damageSources().magic(),
					TendencyAffinityRules.adjustManipulationDamage(player, target, this, damage));
			world.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
					0.75F, ambush ? 0.65F : 0.85F);
		} else {
			world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
					0.45F, 0.7F);
		}

		sendSlashVisual(sLevel, player, target, ambush);
	}

	@Nullable
	private LivingEntity findTarget(Player player, Level world, double range) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		return world.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(range),
						target -> target != player && target.isAlive() && !target.isAlliedTo(player)
								&& player.hasLineOfSight(target))
				.stream()
				.filter(target -> {
					Vec3 toTarget = target.getEyePosition().subtract(eye);
					double distance = toTarget.length();
					return distance > 0.001D && distance <= range && look.dot(toTarget.normalize()) >= SLASH_DOT;
				})
				.min((a, b) -> Double.compare(player.distanceToSqr(a), player.distanceToSqr(b)))
				.orElse(null);
	}

	private void sendSlashVisual(ServerLevel level, Player player, @Nullable LivingEntity target, boolean ambush) {
		Vec3 look = player.getLookAngle().normalize();
		Vec3 center = target != null
				? target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D)
				: player.getEyePosition().add(look.scale(2.8D));
		PacketHandler.sendClawSlash(center, look, new ParticleColor(
				ambush ? 45 : 80,
				0,
				ambush ? 135 : 100),
				ambush, ambush ? 1.18F : 1.0F, 64.0D, level);
	}
}
