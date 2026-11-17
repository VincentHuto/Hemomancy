package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Crimson Tithe, Canon Memory of Hemorath, the Hollow Vessel (D-1 spec 3.1). Held: chains latch what is near and drink
 * it into a vessel in the caster's chest that leaks the whole time, slows and softens them as it brims, and bursts on
 * them if overfilled. Let go, run dry or hold too long and the Vessel Empties in a star of blood and iron lanes. No
 * blood comes back to the caster's pool (NQ-08-6). Imprinted, not learned: no Dynamic Use reduction.
 */
public class CrimsonTitheManip extends BloodManipulation {
	static final BloodAccounting.Source OPEN = BloodAccounting.Source.of("crimson_tithe_open", Category.CAST, false);
	static final BloodAccounting.Source TAKEN = BloodAccounting.Source.of("crimson_tithe_taken", Category.CAST, false);
	static final BloodAccounting.Source BURST = BloodAccounting.Source.of("crimson_tithe_burst", Category.CAST, false);
	private static final ResourceLocation BRIMMING_SLOW = Hemomancy.rloc("crimson_tithe_brimming");
	/** Overfill is marked on the hold by pushing its full ticks past this. */
	private static final int OVERFILLED = Integer.MAX_VALUE / 2;

	public CrimsonTitheManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTemper temper,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, temper, section);
	}

	@Override
	protected boolean canPerformAction(Player player, float chargeTicks) {
		if (!super.canPerformAction(player, chargeTicks)) return false;
		if (CanonGiftState.tithe(player.getUUID()) != null) return true;
		if (HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() < CrimsonTitheRules.OPEN_COST + getCost()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.crimson_tithe.open_cost",
					(int) CrimsonTitheRules.OPEN_COST).withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}
		return true;
	}

	@Override
	public boolean canContinueChannel(Player player, Level world) {
		CanonGiftState.TitheHold hold = CanonGiftState.tithe(player.getUUID());
		return hold == null || hold.heldTicks < CrimsonTitheRules.LONGEST_HOLD && hold.fullTicks < OVERFILLED;
	}

	/** The first paid pulse opens the vessel; later pulses only keep it open. */
	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(player instanceof ServerPlayer caster) || CanonGiftState.tithe(caster.getUUID()) != null) return;
		BloodAccounting.pay(caster, CrimsonTitheRules.OPEN_COST, OPEN);
		CanonGiftState.TitheHold hold = CanonGiftState.openTithe(caster.getUUID());
		hold.dimension = caster.level().dimension();
		caster.displayClientMessage(Component.translatable("message.hemomancy.crimson_tithe.open")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		latch(caster, hold);
		showVessel(caster, hold);
	}

	@Override
	public void tickContinuousAction(Player player, Level world) {
		if (!(player instanceof ServerPlayer caster) || !(world instanceof ServerLevel level)) return;
		CanonGiftState.TitheHold hold = CanonGiftState.tithe(caster.getUUID());
		if (hold == null) return;
		hold.heldTicks++;
		try (var scope = SchoolDamage.cast(this, caster, 1)) {
			if (hold.heldTicks % CrimsonTitheRules.RELATCH_TICKS == 0) latch(caster, hold);
			tendChains(caster, level, hold);
			if (hold.heldTicks % CrimsonTitheRules.PULSE_TICKS == 0) draw(caster, level, hold);
		}
		double before = CrimsonTitheRules.fill(hold.vessel);
		double leak = Math.min(hold.vessel, CrimsonTitheRules.leak(before));
		hold.vessel -= leak;
		int ironBefore = CrimsonTitheRules.iron(hold.leaked);
		hold.leaked += leak;
		double fill = CrimsonTitheRules.fill(hold.vessel);
		boolean brimming = CrimsonTitheRules.brimming(fill);
		if (brimming && !CrimsonTitheRules.brimming(before))
			level.playSound(null, caster.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.2F, 0.4F);
		AttributeModifiers.sync(caster.getAttribute(Attributes.MOVEMENT_SPEED), BRIMMING_SLOW,
				-CrimsonTitheRules.BRIMMING_SLOW, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, brimming);
		if (CrimsonTitheRules.full(fill)) {
			if (++hold.fullTicks >= CrimsonTitheRules.OVERFILL_TICKS) {
				hold.fullTicks = OVERFILLED;
				ManipulationChannelManager.stop(caster, false);
				return;
			}
		} else {
			hold.fullTicks = 0;
		}
		if (hold.heldTicks % CrimsonTitheRules.PULSE_TICKS == 0 || CrimsonTitheRules.iron(hold.leaked) != ironBefore)
			showVessel(caster, hold);
		if (hold.heldTicks >= CrimsonTitheRules.LONGEST_HOLD) ManipulationChannelManager.stop(caster, false);
	}

	@Override
	public void finishContinuousAction(Player player, boolean released) {
		CanonGiftState.TitheHold hold = CanonGiftState.closeTithe(player.getUUID());
		AttributeModifiers.sync(player.getAttribute(Attributes.MOVEMENT_SPEED), BRIMMING_SLOW, 0,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false);
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.TITHE_VESSEL, 0, 0, 0);
		if (hold != null && player instanceof ServerPlayer caster && caster.isAlive() && !caster.hasDisconnected()
				&& caster.level().dimension().equals(hold.dimension)) {
			for (UUID id : hold.chains.keySet())
				if (caster.serverLevel().getEntity(id) instanceof LivingEntity target)
					ManipulationVisuals.attached(target, ManipulationVisuals.Form.TITHE_CHAIN, 0, 0, 0);
			try (var scope = SchoolDamage.cast(this, caster, 1)) {
				empty(caster, hold);
			}
		}
		super.finishContinuousAction(player, released);
	}

	@Override
	public void clearContinuousSession(UUID playerId) {
		CanonGiftState.closeTithe(playerId);
	}

	/** Throws a chain at each harmable body in reach, nearest first, into the free slots. */
	private void latch(ServerPlayer caster, CanonGiftState.TitheHold hold) {
		int free = CrimsonTitheRules.MAX_CHAINS - hold.chains.size();
		if (free <= 0) return;
		ServerLevel level = caster.serverLevel();
		double reach = CrimsonTitheRules.REACH;
		List<LivingEntity> bodies = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class,
				caster.getBoundingBox().inflate(reach), target -> latchable(caster, hold, target)));
		bodies.sort(Comparator.comparingDouble(caster::distanceToSqr));
		int thrown = 0;
		for (LivingEntity target : bodies) {
			if (thrown >= free) break;
			hold.chains.put(target.getUUID(), 0);
			thrown++;
			strike(caster, target, context(caster).newAttack(), CrimsonTitheRules.LATCH_DAMAGE);
			ManipulationVisuals.attached(target, ManipulationVisuals.Form.TITHE_CHAIN, Vec3.ZERO, 0, 10, caster.getId());
		}
		if (thrown > 0) level.playSound(null, caster.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.0F, 0.7F);
	}

	private static boolean latchable(ServerPlayer caster, CanonGiftState.TitheHold hold, LivingEntity target) {
		if (target == caster || !target.isAlive() || target.isSpectator() || hold.chains.containsKey(target.getUUID())) return false;
		double distance = caster.distanceTo(target);
		if (distance > CrimsonTitheRules.REACH || !ManipulationCombatHelper.canHarm(caster, target)
				|| !caster.hasLineOfSight(target)) return false;
		boolean revealed = SchoolStates.has(target, SchoolState.ILLUMINATED);
		if (target.isInvisible() && !revealed) return false;
		return revealed || !SchoolStates.has(target, SchoolState.OBSCURED) || distance <= CrimsonTitheRules.OBSCURED_REACH;
	}

	/** Breaks chains whose body left reach or sight, and hauls the rest toward the caster. */
	private static void tendChains(ServerPlayer caster, ServerLevel level, CanonGiftState.TitheHold hold) {
		Iterator<Map.Entry<UUID, Integer>> chains = hold.chains.entrySet().iterator();
		while (chains.hasNext()) {
			Map.Entry<UUID, Integer> chain = chains.next();
			Entity entity = level.getEntity(chain.getKey());
			if (!(entity instanceof LivingEntity target) || !target.isAlive()
					|| caster.distanceTo(target) > CrimsonTitheRules.BREAK_REACH) {
				if (entity != null) ManipulationVisuals.attached(entity, ManipulationVisuals.Form.TITHE_CHAIN, 0, 0, 0);
				chains.remove();
				continue;
			}
			int unseen = caster.hasLineOfSight(target) ? 0 : chain.getValue() + 1;
			if (unseen > CrimsonTitheRules.SIGHT_GRACE) {
				ManipulationVisuals.attached(target, ManipulationVisuals.Form.TITHE_CHAIN, 0, 0, 0);
				chains.remove();
				continue;
			}
			chain.setValue(unseen);
			if (HemoEntityPredicates.isBoss(target)) continue;
			double pull = SchoolStates.has(target, SchoolState.LODESTONE) ? CrimsonTitheRules.LODESTONE_PULL : CrimsonTitheRules.PULL;
			Vec3 toward = caster.position().subtract(target.position());
			if (toward.lengthSqr() > 2.25D) {
				target.setDeltaMovement(target.getDeltaMovement().add(toward.normalize().scale(pull)));
				target.hurtMarked = true;
			}
		}
	}

	/** One 5-tick pulse: each chain drinks into the vessel, and the caster heals a little of it. */
	private void draw(ServerPlayer caster, ServerLevel level, CanonGiftState.TitheHold hold) {
		double drawn = 0.0D;
		for (UUID id : hold.chains.keySet()) {
			if (!(level.getEntity(id) instanceof LivingEntity target)) continue;
			if (target instanceof HemorathEntity saint) {
				// his own gift answered: the blood runs the other way, into his meter and never the vessel (NQ-08-11)
				saint.feed(CrimsonTitheRules.HEMORATH_FEED, caster, false);
				ManipulationVisuals.attached(target, ManipulationVisuals.Form.TITHE_CHAIN, Vec3.ZERO, 0, 10, caster.getId());
				continue;
			}
			double pour = drink(caster, target);
			if (pour <= 0.0D) continue;
			drawn += pour;
			ManipulationVisuals.attached(target, ManipulationVisuals.Form.TITHE_CHAIN, Vec3.ZERO,
					CrimsonTitheRules.fill(hold.vessel), 10, caster.getId());
		}
		if (drawn <= 0.0D) return;
		hold.vessel = CrimsonTitheRules.pour(hold.vessel, drawn);
		double heal = CrimsonTitheRules.heal(drawn, hold.healed);
		if (heal > 0.0D) {
			caster.heal((float) heal);
			hold.healed += heal;
		}
		level.playSound(null, caster.blockPosition(), SoundInit.ENTITY_NAEGLEROPHAEON_DRAIN.get(), SoundSource.PLAYERS, 0.8F, 0.7F);
	}

	/**
	 * One chain's drink from {@code target}: a blood-active player pays it in blood, anyone else in health
	 * (NQ-08-12). Returns what it pours into the vessel, or 0 when nothing was taken.
	 */
	public double drink(ServerPlayer caster, LivingEntity target) {
		if (!ManipulationCombatHelper.canHarm(caster, target)) return 0.0D;
		double pour = CrimsonTitheRules.drawn(HemoEntityPredicates.NOBLOOD.test(target),
				SchoolStates.has(target, SchoolState.SEARING), HemoEntityPredicates.isBoss(target));
		if (target instanceof Player victim && paysInBlood(victim))
			return BloodAccounting.pay(victim, CrimsonTitheRules.PLAYER_BLOOD_TAKEN, TAKEN).paid() > 0.0D ? pour : 0.0D;
		// the draw is a slow drink, not a blow: it ignores the hurt cooldown so every pulse lands
		target.invulnerableTime = 0;
		return strike(caster, target, context(caster).newAttack().child(SchoolHitContext.Kind.PERIODIC),
				CrimsonTitheRules.DRAW_DAMAGE) ? pour : 0.0D;
	}

	/** A blood-active, unpurified player pays the draw in blood; anyone else pays it in health (NQ-08-12). */
	private static boolean paysInBlood(Player victim) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(victim);
		return volume.isActive() && !HemoCapabilityAccess.getUnstainedProgress(victim).hasBegunPurification()
				&& volume.getBloodVolume() > 0.0D;
	}

	/** The Vessel Empties: lanes of blood and iron burst out of the caster in a star, the wedges between them safe. */
	private void empty(ServerPlayer caster, CanonGiftState.TitheHold hold) {
		ServerLevel level = caster.serverLevel();
		boolean overfilled = hold.fullTicks >= OVERFILLED;
		double fill = overfilled ? 1.0D : CrimsonTitheRules.fill(hold.vessel);
		int lanes = CrimsonTitheRules.lanes(CrimsonTitheRules.iron(hold.leaked));
		double length = CrimsonTitheRules.laneLength(fill);
		double start = Math.toRadians(caster.getYRot() + 90.0D);
		Vec3 origin = caster.position();
		float damage = CrimsonTitheRules.releaseDamage(fill)
				* (float) SkillPointHelper.getCrimsonMasteryMultiplier(caster);
		SchoolHitContext release = context(caster).withRole(SchoolHitContext.Role.EXPLOIT_STATE);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
				caster.getBoundingBox().inflate(length + 1.0D, 2.5D, length + 1.0D),
				target -> target != caster && target.isAlive() && !target.isSpectator())) {
			Vec3 offset = target.position().subtract(origin);
			if (Math.abs(offset.y) > 2.5D || !CrimsonTitheRules.inLane(offset.x, offset.z, start, lanes, length)) continue;
			target.invulnerableTime = 0;
			if (!strike(caster, target, release.newAttack(), damage)) continue;
			SchoolStates.apply(caster, target, SchoolState.LODESTONE, SchoolState.LODESTONE.duration);
			if (!HemoEntityPredicates.isBoss(target)) {
				Vec3 away = new Vec3(offset.x, 0, offset.z);
				away = away.lengthSqr() < 1.0E-6D ? new Vec3(Math.cos(start), 0, Math.sin(start)) : away.normalize();
				target.push(away.x * CrimsonTitheRules.LANE_KNOCKBACK, 0.15D, away.z * CrimsonTitheRules.LANE_KNOCKBACK);
				target.hurtMarked = true;
			}
		}
		CanonGiftState.release(caster.getUUID(), new CanonGiftState.TitheRelease(origin, start, lanes, length, level.getGameTime()));
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.TITHE_RELEASE, origin,
				origin.add(Math.cos(start), 0, Math.sin(start)), length, CrimsonTitheRules.CLOSE_END, lanes);
		level.playSound(null, caster.blockPosition(), SoundInit.RITE_ESCALATE.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
		level.playSound(null, caster.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
		level.playSound(null, caster.blockPosition(), SoundEvents.POINTED_DRIPSTONE_PLACE, SoundSource.PLAYERS,
				0.8F, 0.6F);
		if (overfilled) {
			caster.hurt(caster.damageSources().magic(), CrimsonTitheRules.OVERFILL_DAMAGE);
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(caster);
			BloodAccounting.pay(caster, volume.getMaxBloodVolume() * CrimsonTitheRules.OVERFILL_BLOOD_SHARE, BURST);
			level.playSound(null, caster.blockPosition(), SoundInit.RITE_BACKLASH.get(), SoundSource.PLAYERS, 1.2F, 0.6F);
			caster.displayClientMessage(Component.translatable("message.hemomancy.crimson_tithe.overfill")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		}
	}

	/** Bodies standing in a flooded lane wade: Slowness I while they stand in it. Called each tick by the gift events. */
	static void floodTick(ServerLevel level, CanonGiftState.TitheRelease release, UUID casterId) {
		long age = level.getGameTime() - release.born();
		if (!CrimsonTitheRules.flooding(age)) return;
		double reach = release.length() + 1.0D;
		for (LivingEntity body : level.getEntitiesOfClass(LivingEntity.class,
				new net.minecraft.world.phys.AABB(release.origin(), release.origin()).inflate(reach, 2.5D, reach),
				body -> body.isAlive() && !body.getUUID().equals(casterId))) {
			Vec3 offset = body.position().subtract(release.origin());
			if (Math.abs(offset.y) <= 2.5D
					&& CrimsonTitheRules.inLane(offset.x, offset.z, release.start(), release.lanes(), release.length()))
				body.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CrimsonTitheRules.FLOODED_SLOWNESS, 0,
						false, true, true));
		}
	}

	private static SchoolHitContext context(ServerPlayer caster) {
		return SchoolHitContext.direct(Hemomancy.rloc("crimson_tithe"), EnumBloodTemper.ANIMUS, EnumBloodTemper.FERRIC, caster);
	}

	private boolean strike(ServerPlayer caster, LivingEntity target, SchoolHitContext hit, float amount) {
		if (!ManipulationCombatHelper.canHarm(caster, target)) return false;
		return ManipulationParticles.hurt(this, target,
				SchoolDamage.attributed(caster.serverLevel().damageSources().magic(), hit, caster), amount);
	}

	private static void showVessel(ServerPlayer caster, CanonGiftState.TitheHold hold) {
		double fill = CrimsonTitheRules.fill(hold.vessel);
		int iron = CrimsonTitheRules.iron(hold.leaked);
		ManipulationVisuals.attached(caster, ManipulationVisuals.Form.TITHE_VESSEL, fill, 10,
				1 + iron + (CrimsonTitheRules.brimming(fill) ? 8 : 0));
	}

	/** True while the caster's vessel brims: they take more damage (DamageStages defenders). */
	public static boolean brimming(Player player) {
		CanonGiftState.TitheHold hold = CanonGiftState.tithe(player.getUUID());
		return hold != null && CrimsonTitheRules.brimming(CrimsonTitheRules.fill(hold.vessel));
	}
}
