package com.vincenthuto.hemomancy.common.entity.menagerie;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargeting;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Plain Projection into a passive animal (core-loop Track 5). A small dose of a Harbinger's blood puts the animal in
 * love, with the usual breeding cooldown, through Sanguine Fertility. The cost is taint: a tainted animal's meat comes
 * off it tainted, each further dose makes it likelier to go feral for a minute (a small goal of its own with fixed
 * damage, since a passive animal has no attack), and its offspring may carry a weak second school, the dosing
 * keeper's. That last is the hook for breeding a better source; no breeding system is built on it here.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class AnimalTaint {
	public static final BloodAccounting.Source DOSE = BloodAccounting.Source.of("animal_dose", Category.CAST, false);
	public static final double DOSE_ML = 20.0D;
	/** One dose an animal in this many ticks; the stream on it between doses is held and costs nothing. */
	public static final int DOSE_GAP_TICKS = 40;
	public static final int FERTILITY_TICKS = 40;
	/** Each dose after the first makes feral this much likelier, to at most {@link #MAX_FERAL_CHANCE}. */
	public static final double FERAL_CHANCE_PER_DOSE = 0.25D;
	public static final double MAX_FERAL_CHANCE = 0.9D;
	public static final int FERAL_TICKS = 1200;
	public static final float FERAL_DAMAGE = 3.0F;
	/** An offspring of a tainted parent carries the parent's school this often. */
	public static final double INHERIT_CHANCE = 0.5D;

	private AnimalTaint() {
	}

	/** The feral chance of the {@code doses}-th dose: none for the first, then a quarter more each. */
	public static double feralChance(int doses) {
		return doses <= 1 ? 0.0D : Math.min(MAX_FERAL_CHANCE, (doses - 1) * FERAL_CHANCE_PER_DOSE);
	}

	public static boolean doseable(LivingEntity entity) {
		return entity instanceof Animal animal && animal.isAlive() && !(animal instanceof Enemy)
				&& MenagerieLeash.verdict(animal).keeps();
	}

	/**
	 * The projection chain's animal link: plain Projection aimed at a passive animal doses it. Empty when no animal is
	 * aimed at; present (possibly 0, between doses) when one is, so the stream does not run on to the ground behind it.
	 */
	public static OptionalDouble tryProject(ServerPlayer player, double reach) {
		Optional<VerbTarget> picked = VerbTargeting.pick(player.level(), player, reach, Set.of(VerbTargetKind.CREATURE));
		if (picked.isEmpty() || !(picked.get() instanceof VerbTarget.Creature creature)
				|| !doseable(creature.living())) return OptionalDouble.empty();
		Animal animal = (Animal) creature.living();
		ServerLevel level = player.serverLevel();
		long now = level.getGameTime();
		AnimalTaintState found = HemoCapabilityAccess.findAnimalTaint(animal);
		if (found != null && found.lastDose != Long.MIN_VALUE && now - found.lastDose < DOSE_GAP_TICKS) {
			return OptionalDouble.of(0.0D);
		}
		if (!BloodAccounting.canPayInFull(player, DOSE_ML, DOSE)) {
			if (now % 20L == 0L) player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.dose_short"), true);
			return OptionalDouble.of(0.0D);
		}
		BloodAccounting.Payment paid = BloodAccounting.pay(player, DOSE_ML, DOSE);
		dose(level, player, animal, HemoCapabilityAccess.getBloodTemperament(player).strongestTemper());
		return OptionalDouble.of(paid.paid());
	}

	/** One dose of {@code keeper}'s blood, carrying {@code school}: fertility now, taint, and the feral chance. */
	public static void dose(ServerLevel level, @Nullable Player keeper, Animal animal, @Nullable EnumBloodTemper school) {
		long now = level.getGameTime();
		AnimalTaintState taint = HemoCapabilityAccess.getAnimalTaint(animal);
		taint.doses++;
		if (school != null) taint.school = school;
		taint.lastDose = now;
		animal.addEffect(new MobEffectInstance(EffectInit.sanguine_fertility, FERTILITY_TICKS, 0, false, true, true));
		level.sendParticles(HemoParticleData.hitGlow(ParticleColor.RED), animal.getX(), animal.getY() + animal.getBbHeight() * 0.6D,
				animal.getZ(), 6, animal.getBbWidth() * 0.3D, animal.getBbHeight() * 0.25D, animal.getBbWidth() * 0.3D, 0.01D);
		if (level.random.nextDouble() < feralChance(taint.doses)) {
			taint.feralUntil = now + FERAL_TICKS;
			ensureFeralGoal(animal);
			level.playSound(null, animal.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.NEUTRAL, 0.5F, 1.6F);
			if (keeper != null) keeper.displayClientMessage(Component.translatable("message.hemomancy.menagerie.feral",
					animal.getDisplayName()).withStyle(ChatFormatting.RED), true);
		}
	}

	public static boolean tainted(LivingEntity entity) {
		AnimalTaintState taint = HemoCapabilityAccess.findAnimalTaint(entity);
		return taint != null && taint.tainted();
	}

	public static boolean feral(LivingEntity entity) {
		AnimalTaintState taint = HemoCapabilityAccess.findAnimalTaint(entity);
		return taint != null && taint.feral(entity.level().getGameTime());
	}

	/** The weak second school a tainted parent passed to this creature, or null. */
	@Nullable
	public static EnumBloodTemper inheritedSchool(LivingEntity entity) {
		AnimalTaintState taint = HemoCapabilityAccess.findAnimalTaint(entity);
		return taint == null ? null : taint.inherited;
	}

	// ── Events ──

	/** A tainted animal's meat comes off it tainted. */
	@SubscribeEvent
	public static void drops(LivingDropsEvent event) {
		if (!tainted(event.getEntity())) return;
		for (ItemEntity drop : event.getDrops()) {
			ItemStack stack = drop.getItem();
			if (stack.is(ItemTags.MEAT)) drop.setItem(new ItemStack(ItemInit.tainted_meat.get(), stack.getCount()));
		}
	}

	/** An offspring of a tainted parent may carry the parent's school as a weak second school. */
	@SubscribeEvent
	public static void born(BabyEntitySpawnEvent event) {
		AgeableMob child = event.getChild();
		if (child == null) return;
		EnumBloodTemper school = parentSchool(event.getParentA());
		if (school == null) school = parentSchool(event.getParentB());
		if (school != null && child.getRandom().nextDouble() < INHERIT_CHANCE) {
			HemoCapabilityAccess.getAnimalTaint(child).inherited = school;
		}
	}

	@Nullable
	private static EnumBloodTemper parentSchool(Mob parent) {
		AnimalTaintState taint = parent == null ? null : HemoCapabilityAccess.findAnimalTaint(parent);
		return taint != null && taint.tainted() ? taint.school : null;
	}

	/** A feral animal keeps its goal across unloading. */
	@SubscribeEvent
	public static void joined(EntityJoinLevelEvent event) {
		if (!event.getLevel().isClientSide() && event.getEntity() instanceof Animal animal && feral(animal)) {
			ensureFeralGoal(animal);
		}
	}

	private static void ensureFeralGoal(Animal animal) {
		boolean present = animal.goalSelector.getAvailableGoals().stream().anyMatch(goal -> goal.getGoal() instanceof FeralGoal);
		if (!present) animal.goalSelector.addGoal(0, new FeralGoal(animal));
	}

	/**
	 * A feral animal goes for the nearest player for a minute: it runs at them and strikes for fixed damage, since a
	 * passive animal has no attack of its own to swap in.
	 */
	public static final class FeralGoal extends Goal {
		private final Animal animal;
		@Nullable
		private Player target;
		private int cooldown;

		public FeralGoal(Animal animal) {
			this.animal = animal;
			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (!feral(animal)) return false;
			target = animal.level().getNearestPlayer(animal, 12.0D);
			return target != null && !target.isCreative() && !target.isSpectator();
		}

		@Override
		public boolean canContinueToUse() {
			return feral(animal) && target != null && target.isAlive() && !target.isCreative()
					&& animal.distanceToSqr(target) < 16.0D * 16.0D;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			if (target == null) return;
			animal.getLookControl().setLookAt(target, 30.0F, 30.0F);
			if (animal.tickCount % 5 == 0) animal.getNavigation().moveTo(target, 1.4D);
			if (cooldown > 0) cooldown--;
			if (cooldown <= 0 && animal.distanceToSqr(target) < 2.25D + animal.getBbWidth() * animal.getBbWidth()) {
				animal.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
				target.hurt(animal.damageSources().mobAttack(animal), FERAL_DAMAGE);
				cooldown = 20;
			}
		}

		@Override
		public void stop() {
			target = null;
			animal.getNavigation().stop();
		}
	}
}
