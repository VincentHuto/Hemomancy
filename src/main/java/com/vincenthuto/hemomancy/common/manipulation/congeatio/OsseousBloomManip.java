package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Osseous Bloom — a T3 (SUMMA) CONGEATIO quick manipulation that forces a
 * crystallisation cascade through the blood of all nearby enemies.
 *
 * <p>Damage scales inversely with target wounds: healthy targets suffer more
 * as their full blood volume crystallises under the pressure, while
 * near-dead targets are largely spared (their blood is already depleted).
 * All affected enemies are flash-frozen into Slowness IV for 4 seconds.
 *
 * <p>This rewards opening engagements — use it at the start of a fight when
 * enemies are at full health, not as a finisher. Pair with Exsanguinate or
 * Glacial Bastion for a full Congeatio crowd-control combo.
 */
public class OsseousBloomManip extends BloodManipulation {

	private static final double RADIUS = 6.0;
	/** Fraction of current HP dealt as magic damage. */
	private static final float HP_FRACTION = 0.25f;
	private static final int SLOWNESS_DURATION = 80;  // 4 seconds
	private static final int SLOWNESS_AMPLIFIER = 3;  // Slowness IV

	public OsseousBloomManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = player.blockPosition();
		AABB searchBox = new AABB(center).inflate(RADIUS);
		List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, searchBox,
				e -> ManipulationCombatHelper.canHarm(player, e) && e.distanceTo(player) <= RADIUS);

		if (targets.isEmpty()) {
			player.displayClientMessage(Component.literal("§8No vessels within the bloom's reach."), true);
			return;
		}

		float masteryMult = (float) SkillPointHelper.getCrimsonMasteryMultiplier(player);

		for (LivingEntity target : targets) {
			float damage = target.getHealth() * HP_FRACTION * masteryMult;
			damage = TendencyAffinityRules.adjustManipulationDamage(player, target, this, damage);
			if (ManipulationReactiveEvents.isBoss(target)) damage = Math.min(12.0F, damage);
			ManipulationParticles.hurt(this, target, world.damageSources().freeze(), damage);
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
					SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, false, true));
		}
        for (LivingEntity target : targets) ManipulationVisuals.attached(target, ManipulationVisuals.Form.BONE, target.getBbWidth()*.7, 32, 1);

		RandomSource random = world.random;


		world.playSound(null, center, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
		world.playSound(null, center, SoundEvents.POWDER_SNOW_STEP, SoundSource.PLAYERS, 0.8f, 0.4f);
	}
}
