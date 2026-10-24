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
 * Osseous Bloom (displayed as "Crystalline Bloom"; the registry ID stays {@code osseous_bloom}) — a T3 (SUMMA)
 * CONGEATIO quick manipulation that forces a crystallisation cascade through the blood of all nearby enemies.
 *
 * <p>Each enemy within six blocks takes a quarter of its <em>current</em> health as freeze damage, scaled by
 * Crimson Mastery, so healthy targets suffer more and near-dead targets are largely spared. Bosses (the shared
 * {@link ManipulationReactiveEvents#isBoss} predicate) are capped at twelve. Confirmed hits build Rime for
 * 4 seconds. With no enemy in reach the cast is refused before payment.
 *
 * <p>This rewards opening engagements — use it at the start of a fight when
 * enemies are at full health, not as a finisher.
 */
public class OsseousBloomManip extends BloodManipulation {

	private static final double RADIUS = 6.0;
	/** Fraction of current HP dealt as magic damage. */
	private static final float HP_FRACTION = 0.25f;
	private static final int RIME_DURATION = 80;  // 4 seconds
	/** Shared boss cap: a full-health boss would otherwise lose a quarter of its life per bloom. */
	private static final float BOSS_DAMAGE_CAP = 12.0F;

	public OsseousBloomManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	private static List<LivingEntity> targets(Player player, Level world) {
		return world.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(RADIUS),
				e -> ManipulationCombatHelper.canHarm(player, e) && e.distanceTo(player) <= RADIUS);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (targets(player, player.level()).isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.osseous_bloom_manip.no_vessels_within_the_bloom_s"), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = player.blockPosition();
		List<LivingEntity> targets = targets(player, world);
		if (targets.isEmpty()) return;

		float masteryMult = (float) SkillPointHelper.getCrimsonMasteryMultiplier(player);

		for (LivingEntity target : targets) {
			float damage = target.getHealth() * HP_FRACTION * masteryMult;
			if (ManipulationReactiveEvents.isBoss(target)) damage = Math.min(BOSS_DAMAGE_CAP, damage);
			com.vincenthuto.hemomancy.common.damage.SchoolDamage.hurt(target, world.damageSources().freeze(), damage, RIME_DURATION, 1);
		}
        for (LivingEntity target : targets) ManipulationVisuals.attached(target, ManipulationVisuals.Form.BONE, target.getBbWidth()*.7, 32, 1);

		RandomSource random = world.random;


		world.playSound(null, center, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
		world.playSound(null, center, SoundEvents.POWDER_SNOW_STEP, SoundSource.PLAYERS, 0.8f, 0.4f);
	        }
    }
}
