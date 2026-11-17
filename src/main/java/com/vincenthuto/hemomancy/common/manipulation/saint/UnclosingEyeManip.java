package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaePhases;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.DeadlyGazeManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Unclosing Eye, Canon Memory of Seraphae, the Chain Saint (D-1 spec 3.2). Charged: her eye opens in the caster's
 * chest and on release a beam leaves it for 40 ticks, swept with the head at her capped rate. Enemies burn and grow
 * (Swelling, Knotting, Burst); allies are healed when hurt and grown when whole (NQ-08-7). Then the Lapse: the caster
 * bows, cannot cast Lux and is laid bare; and the eye never quite closes, a slit that lights whatever the caster looks
 * at. Player only (NQ-08-5). Imprinted, not learned: no Dynamic Use reduction.
 */
public class UnclosingEyeManip extends BloodManipulation {
	private static final ResourceLocation SNEAK_SLOW = Hemomancy.rloc("unclosing_eye_sneak");

	public UnclosingEyeManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTemper temper,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, temper, section);
	}

	@Override
	public int getRequiredChargeTicks() {
		return UnclosingEyeRules.FULL_CHARGE;
	}

	@Override
	protected boolean canPerformAction(Player player, float chargeTicks) {
		if (!super.canPerformAction(player, chargeTicks)) return false;
		return CanonGiftState.beam(player.getUUID()) == null;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float chargeTicks) {
		if (!(player instanceof ServerPlayer caster) || !(world instanceof ServerLevel level)) return;
		CanonGiftState.openBeam(caster.getUUID(), new CanonGiftState.EyeBeam(level.getGameTime(),
				UnclosingEyeRules.charge(chargeTicks), caster.getYRot(), caster.getXRot()));
		caster.displayClientMessage(Component.translatable("message.hemomancy.unclosing_eye.open")
				.withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);
		level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.7F);
		level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	/** The chest the eye opens in. */
	public static Vec3 chest(LivingEntity body) {
		return body.position().add(0, body.getBbHeight() * 0.72D, 0);
	}

	/** One server tick of an open beam, then of the Lapse and the slit. Called by {@link CanonGiftEvents}. */
	static void tick(ServerPlayer caster) {
		ServerLevel level = caster.serverLevel();
		long now = level.getGameTime();
		CanonGiftState.EyeBeam beam = CanonGiftState.beam(caster.getUUID());
		boolean charging = beam == null && charging(caster);
		AttributeModifiers.sync(caster.getAttribute(Attributes.MOVEMENT_SPEED), SNEAK_SLOW, -UnclosingEyeRules.SNEAK_SLOW,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, beam != null || charging);
		if (beam != null || charging) caster.setSprinting(false);
		if (beam != null) {
			tickBeam(caster, level, beam, now);
			return;
		}
		CanonGiftState.EyeAfter after = CanonGiftState.after(caster.getUUID());
		if (after == null) return;
		if (now >= after.slitUntil()) {
			CanonGiftState.afters().remove(caster.getUUID());
			ManipulationVisuals.attached(caster, ManipulationVisuals.Form.EYE_APERTURE, 0, 0, 0);
			return;
		}
		if (now == after.lapseUntil())
			ManipulationVisuals.attached(caster, ManipulationVisuals.Form.EYE_APERTURE, 1,
					UnclosingEyeRules.SLIT_TICKS, 2);
		if (now % 20 == 0) SchoolStates.apply(caster, caster, SchoolState.ILLUMINATED, 40);
		if (now >= after.lapseUntil() && now % UnclosingEyeRules.SLIT_CHECK_TICKS == 0) {
			EntityHitResult seen = DeadlyGazeManip.rayTraceEntities(caster, UnclosingEyeRules.SLIT_RANGE,
					entity -> entity instanceof LivingEntity living && living != caster && living.isAlive());
			if (seen != null && seen.getEntity() instanceof LivingEntity looked)
				SchoolStates.apply(caster, looked, SchoolState.ILLUMINATED, UnclosingEyeRules.SLIT_ILLUMINATED);
		}
	}

	private static boolean charging(ServerPlayer caster) {
		var selected = com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getKnownManipulations(caster)
				.getSelectedManip();
		BloodManipulation eye = ManipulationInit.unclosing_eye.get();
		return selected != null && eye.getName().equals(selected.getName())
				&& com.vincenthuto.hemomancy.common.manipulation.ManipulationChargeClock.hasStart(caster, eye);
	}

	private static void tickBeam(ServerPlayer caster, ServerLevel level, CanonGiftState.EyeBeam beam, long now) {
		caster.stopUsingItem();
		long age = now - beam.started;
		beam.yaw = UnclosingEyeRules.sweep(beam.yaw, caster.getYRot());
		beam.pitch = Math.max(-89.0D, Math.min(89.0D, UnclosingEyeRules.sweep(beam.pitch, caster.getXRot())));
		Vec3 chest = chest(caster);
		Vec3 axis = Vec3.directionFromRotation((float) beam.pitch, (float) beam.yaw);
		double range = UnclosingEyeRules.range(beam.charge);
		Vec3 end = level.clip(new ClipContext(chest, chest.add(axis.scale(range)), ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, caster)).getLocation();
		double reach = chest.distanceTo(end);
		if (age > 0 && age % UnclosingEyeRules.PULSE_TICKS == 0) {
			UnclosingEyeManip eye = (UnclosingEyeManip) ManipulationInit.unclosing_eye.get();
			try (var scope = SchoolDamage.cast(eye, caster, beam.charge)) {
				for (LivingEntity body : level.getEntitiesOfClass(LivingEntity.class,
						caster.getBoundingBox().expandTowards(axis.scale(reach)).inflate(1.5D),
						body -> body != caster && body.isAlive() && !body.isSpectator())) {
					Vec3 centre = body.position().add(0, body.getBbHeight() * 0.5D, 0);
					Vec3 offset = centre.subtract(chest);
					double along = offset.dot(axis);
					double across = offset.subtract(axis.scale(along)).length();
					if (!UnclosingEyeRules.inBeam(along, across, reach, body.getBbWidth() * 0.5D)) continue;
					if (level.clip(new ClipContext(chest, centre, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
							caster)).getType() == HitResult.Type.BLOCK) continue;
					eye.pulse(caster, level, beam, body, now);
				}
			}
			beam.pulses++;
		}
		ManipulationVisuals.attached(caster, ManipulationVisuals.Form.EYE_BEAM, end, beam.charge, 6, beam.pulses + 1);
		if (age >= UnclosingEyeRules.BEAM_TICKS) close(caster, level, now);
	}

	private void pulse(ServerPlayer caster, ServerLevel level, CanonGiftState.EyeBeam beam, LivingEntity body, long now) {
		boolean enemy = ManipulationCombatHelper.canHarm(caster, body);
		boolean ally = !enemy && (body instanceof Player || body instanceof OwnableEntity owned && owned.getOwnerUUID() != null);
		if (!enemy && !ally) return;
		boolean boss = HemoEntityPredicates.isBoss(body);
		CanonGiftState.Growth growth = CanonGiftState.growth().computeIfAbsent(body.getUUID(),
				ignored -> new CanonGiftState.Growth(caster.getUUID(), now));
		growth.lastBeam = now;
		growth.lastDecay = now;
		growth.dimension = level.dimension();
		if (enemy) {
			float damage = UnclosingEyeRules.pulseDamage(beam.charge)
					* (float) SkillPointHelper.getCrimsonMasteryMultiplier(caster);
			boolean first = beam.touched.add(body.getUUID());
			// a pulse of light, not a blow: it ignores the hurt cooldown so every pulse lands
			body.invulnerableTime = 0;
			SchoolHitContext hit = context(caster).newAttack();
			strike(caster, body, first ? hit : hit.child(SchoolHitContext.Kind.PERIODIC), damage);
			if (body instanceof SeraphaeEntity saint) {
				// her own gift answered: being witnessed holds her, in her escaping light only, and nothing grows (NQ-08-11)
				if (saint.phases().phase() >= SeraphaePhases.ESCAPING_LIGHT && beam.integrity < UnclosingEyeRules.SERAPHAE_INTEGRITY_CAP) {
					saint.meter().add(UnclosingEyeRules.SERAPHAE_INTEGRITY, caster);
					beam.integrity++;
				}
				CanonGiftState.growth().remove(body.getUUID());
				return;
			}
		} else if (body.getHealth() < body.getMaxHealth()) {
			if (!SchoolStates.has(body, SchoolState.NECROSIS)) body.heal(UnclosingEyeRules.ALLY_HEAL);
			ManipulationVisuals.attached(body, ManipulationVisuals.Form.EYE_GROWTH, growth.growth / 8.0D, 60,
					Math.max(1, UnclosingEyeRules.stage(growth.growth)));
			return;
		}
		int before = growth.growth;
		growth.growth = UnclosingEyeRules.grow(before, UnclosingEyeRules.step(SchoolStates.has(body, SchoolState.SEARING),
				ally, ally ? growth.allyPulses++ : 0));
		if (UnclosingEyeRules.stage(growth.growth) >= 1)
			body.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, true, true));
		if (UnclosingEyeRules.knots(before, growth.growth)) {
			int root = SchoolStates.has(body, SchoolState.RIME) ? UnclosingEyeRules.RIME_KNOT_ROOT : UnclosingEyeRules.KNOT_ROOT;
			if (!boss) Paralysis.apply(body, root);
			SchoolStates.apply(caster, body, SchoolState.DISRUPTED, SchoolState.DISRUPTED.duration);
		}
		if (growth.growth >= UnclosingEyeRules.BURST) {
			growth.growth = UnclosingEyeRules.AFTER_BURST;
			burst(caster, level, body, boss, ally);
		}
		ManipulationVisuals.attached(body, ManipulationVisuals.Form.EYE_GROWTH, growth.growth / 8.0D, 60,
				Math.max(1, UnclosingEyeRules.stage(growth.growth)));
	}

	private void burst(ServerPlayer caster, ServerLevel level, LivingEntity body, boolean boss, boolean ally) {
		if (ally) {
			float amount = UnclosingEyeRules.allyBurst(body.getHealth());
			if (amount > 0) body.hurt(level.damageSources().magic(), amount);
		} else {
			float damage = UnclosingEyeRules.burstDamage(boss, false)
					* (float) SkillPointHelper.getCrimsonMasteryMultiplier(caster);
			body.invulnerableTime = 0;
			strike(caster, body, context(caster).newAttack().withRole(SchoolHitContext.Role.EXPLOIT_STATE), damage);
		}
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.EYE_GROWTH, body.position(), chest(caster),
				body.getBbHeight(), 20, 9);
		level.playSound(null, body.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.4F);
	}

	private static void close(ServerPlayer caster, ServerLevel level, long now) {
		CanonGiftState.closeBeam(caster.getUUID());
		ManipulationVisuals.attached(caster, ManipulationVisuals.Form.EYE_BEAM, 0, 0, 0);
		CanonGiftState.after(caster.getUUID(), new CanonGiftState.EyeAfter(now + UnclosingEyeRules.LAPSE_TICKS,
				now + UnclosingEyeRules.LAPSE_TICKS + UnclosingEyeRules.SLIT_TICKS));
		SchoolStates.apply(caster, caster, SchoolState.ILLUMINATED, 40);
		ManipulationVisuals.attached(caster, ManipulationVisuals.Form.EYE_APERTURE, 1, UnclosingEyeRules.LAPSE_TICKS, 1);
		level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 0.6F);
		caster.displayClientMessage(Component.translatable("message.hemomancy.unclosing_eye.lapse")
				.withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);
		ManipulationInit.unclosing_eye.get().restartCooldown(caster);
	}

	/** True while the caster bows in the Lapse: no Lux casts, and they take more damage. */
	public static boolean inLapse(Player player) {
		CanonGiftState.EyeAfter after = CanonGiftState.after(player.getUUID());
		return after != null && player.level().getGameTime() < after.lapseUntil();
	}

	/** Growth a beam laid on {@code body}, for tests and the gift events; 0 when none. */
	public static int growthOf(LivingEntity body) {
		CanonGiftState.Growth growth = CanonGiftState.growth().get(body.getUUID());
		return growth == null ? 0 : growth.growth;
	}

	private static SchoolHitContext context(ServerPlayer caster) {
		return SchoolHitContext.direct(Hemomancy.rloc("unclosing_eye"), EnumBloodTemper.LUX, EnumBloodTemper.DUCTILIS, caster);
	}

	private boolean strike(ServerPlayer caster, LivingEntity target, SchoolHitContext hit, float amount) {
		if (!ManipulationCombatHelper.canHarm(caster, target)) return false;
		return ManipulationParticles.hurt(this, target,
				SchoolDamage.attributed(caster.serverLevel().damageSources().magic(), hit, caster), amount);
	}
}
