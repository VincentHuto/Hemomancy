package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Umbral Rend — Tenebris Summa payoff. The darkness already clinging to Obscured bodies closes like a wound:
 * every Obscured enemy in front of the caster is torn open at once. The Obscured state is consumed by the
 * shared school payoff in {@code SchoolCombatEvents}, so the rend is only as strong as the shadow laid first.
 */
public class UmbralRendManip extends BloodManipulation {
	public static final double RANGE = 14.0D;
	private static final double CONE_DOT = 0.35D;
	private static final int MAX_TARGETS = 6;
	public static final float BASE_DAMAGE = 5.0F;
	public static final float OBSCURED_BONUS = 4.0F;
	private static final int BLOOD_LOSS_TICKS = 100;

	public UmbralRendManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (targets(player, player.level()).isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.umbral_rend_manip.no_shadowed_body_waits_to_be"), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {
			if (!(world instanceof ServerLevel level)) return;
			float damage = (float) (BASE_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
			int struck = 0;
			for (LivingEntity target : targets(player, level)) {
				HemomancyTendrilEffects.umbraRelease(player, target.getEyePosition());
				Vec3 center = target.position().add(0, target.getBbHeight() * 0.58D, 0);
				// The tear faces the caster; its seeded diagonal varies per victim.
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.UMBRAL_REND, center,
						player.getEyePosition(), target.getBbHeight() * 0.55D, UmbralRendRules.VISUAL_TICKS,
						UmbralRendRules.encode(level.random.nextInt(UmbralRendRules.TILTS), level.random.nextBoolean()));
				if (ManipulationCombatHelper.hurt(this, player, target, level, damage)) {
					target.addEffect(new MobEffectInstance(EffectInit.blood_loss, BLOOD_LOSS_TICKS, 0, false, true));
					struck++;
				}
			}
			level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
					0.8F, struck > 1 ? 0.55F : 0.7F);
			level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_SCREAM, SoundSource.PLAYERS, 0.25F, 0.5F);
		}
	}

	private static List<LivingEntity> targets(Player player, Level world) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		return ManipulationCombatHelper.hostileTargets(player, world, RANGE).stream()
				.filter(target -> SchoolStates.has(target, SchoolState.OBSCURED))
				.filter(target -> {
					Vec3 delta = target.getEyePosition().subtract(eye);
					return delta.length() <= RANGE && delta.lengthSqr() > 0.001D
							&& look.dot(delta.normalize()) >= CONE_DOT;
				})
				.filter(target -> ManipulationCombatHelper.visible(player, target))
				.sorted(Comparator.comparingDouble(player::distanceToSqr))
				.limit(MAX_TARGETS)
				.toList();
	}
}
