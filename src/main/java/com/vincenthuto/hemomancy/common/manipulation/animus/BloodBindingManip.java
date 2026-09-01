package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.*;

public class BloodBindingManip extends BloodManipulation {
	public static final double RANGE = 8.0D;
	public static final int DURATION_TICKS = 120;
	public static final int BOSS_DURATION_TICKS = DURATION_TICKS / 2;
	private static final double AIM_DOT = 0.975D;
	private final Mode mode;

	public BloodBindingManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		this(name, cost, alignLevel, xpCost, type, rank, tendency, section, Mode.BASELINE);
	}

	public BloodBindingManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section, Mode mode) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
		this.mode = mode;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		LivingEntity target = target(player, world);
		if (target == null) return;
		List<LivingEntity> targets = switch (mode) {
			case CHAIN -> chainTargets(player, world, target);
			case LATTICE -> latticeTargets(player, world, target);
			default -> List.of(target);
		};
		LivingEntity previous = player;
		boolean appliedAny = false;
		for (LivingEntity current : targets) {
			MobEffectInstance binding = bindingFor(current);
			if (!applyBinding(player, current, binding)) continue;
			appliedAny = true;
			LivingEntity caster = mode == Mode.LATTICE && previous != player ? target : previous;
			PacketHandler.sendBloodBindingTendril(caster, current, binding.getDuration(), world.random.nextLong());
			previous = current;
		}
		if (appliedAny) world.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL,
				SoundSource.PLAYERS, 0.7F, 0.8F);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItemMainhand, float chargeTicks) {
		if (target(player, player.level()) != null) return true;
		player.displayClientMessage(Component.literal("No unbound vessel in sight."), true);
		return false;
	}

	private LivingEntity target(Player player, Level world) {
		LivingEntity target = ManipulationCombatHelper.aimedTarget(player, world, RANGE, AIM_DOT);
		return eligible(player, target) ? target : null;
	}

	private boolean eligible(Player player, LivingEntity target) {
		if (target == null || target.isSpectator() || player.isAlliedTo(target)) return false;
		if (target instanceof Player targetPlayer
				&& (targetPlayer.isCreative() || !player.canHarmPlayer(targetPlayer))) return false;
		MobEffectInstance binding = bindingFor(target);
		MobEffectInstance current = target.getEffect(EffectInit.blood_binding);
		return (current == null || current.getDuration() < binding.getDuration())
				&& CommonHooks.canMobEffectBeApplied(target, binding);
	}

	private MobEffectInstance bindingFor(LivingEntity target) {
		int normal = switch (mode) {
			case LINGERING -> 240;
			case LATTICE -> 160;
			default -> DURATION_TICKS;
		};
		int duration = ManipulationReactiveEvents.isBoss(target) ? normal / 2 : normal;
		return new MobEffectInstance(EffectInit.blood_binding, duration, 0, false, false, true);
	}

	private static boolean applyBinding(Player player, LivingEntity target, MobEffectInstance binding) {
		if (!ManipulationReactiveEvents.isBoss(target)) return target.addEffect(binding, player);
		target.forceAddEffect(binding, player);
		return target.getEffect(EffectInit.blood_binding) == binding;
	}

	private List<LivingEntity> chainTargets(Player player, Level world, LivingEntity initial) {
		List<LivingEntity> result = new ArrayList<>();
		Set<LivingEntity> used = new HashSet<>();
		LivingEntity current = initial;
		result.add(current);
		used.add(current);
		for (int jump = 0; jump < 3; jump++) {
			LivingEntity from = current;
			current = world.getEntitiesOfClass(LivingEntity.class, current.getBoundingBox().inflate(5),
					candidate -> candidate != player && !used.contains(candidate) && candidate.isAlive()
							&& eligible(player, candidate)).stream()
					.min(Comparator.comparingDouble(from::distanceToSqr)).orElse(null);
			if (current == null) break;
			result.add(current);
			used.add(current);
		}
		return result;
	}

	private List<LivingEntity> latticeTargets(Player player, Level world, LivingEntity initial) {
		List<LivingEntity> result = new ArrayList<>();
		result.add(initial);
		world.getEntitiesOfClass(LivingEntity.class, initial.getBoundingBox().inflate(4),
				candidate -> candidate != initial && candidate != player && candidate.isAlive()
						&& eligible(player, candidate)).stream()
				.sorted(Comparator.comparingDouble(initial::distanceToSqr)).limit(7).forEach(result::add);
		return result;
	}

	public enum Mode {
		BASELINE,
		LINGERING,
		CHAIN,
		LATTICE
	}
}
