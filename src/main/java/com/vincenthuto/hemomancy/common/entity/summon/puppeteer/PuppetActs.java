package com.vincenthuto.hemomancy.common.entity.summon.puppeteer;

import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * D-37, D-38: fires a body's act from the Crossbar radial. The act belongs to the Crossbar's selected shape and is
 * performed by that shape's nearest body on the Crossbar. The Pattern's act, Ring, makes every body of the owner within
 * {@link RingmasterConductorRules#RELAY_RANGE} perform its own act at once, without their own cost or cooldown. Thread
 * is spent only when an act is performed.
 */
public final class PuppetActs {
	/** Session state: each player's next ready tick per act. Cleared by the transient-state hub. */
	private static final Map<UUID, Map<PuppetAct, Long>> ACT_COOLDOWNS = new HashMap<>();

	private PuppetActs() {
	}

	public static boolean fire(ServerPlayer player, ItemStack crossbar) {
		if (!MarionetteCrossbarItem.validateControl(crossbar, player, true)) return false;
		String selected = MarionetteCrossbarItem.getSelectedSummonName(crossbar);
		Optional<PuppetAct> found = PuppetAct.forSummon(selected);
		if (found.isEmpty()) return false;
		PuppetAct act = found.get();
		UUID crossbarId = MarionetteCrossbarItem.ensureCrossbarId(crossbar);
		Mob body = MarionetteCrossbarItem.activeSummonsForCrossbar(player, crossbarId, selected).stream()
				.filter(mob -> mob.level() == player.level())
				.min(Comparator.comparingDouble(mob -> mob.distanceToSqr(player))).orElse(null);
		if (body == null) return refuse(player, Component.translatable("hemomancy.summon.act.no_body",
				Component.translatable(act.nameKey())));
		long now = player.level().getGameTime();
		long ready = ACT_COOLDOWNS.getOrDefault(player.getUUID(), Map.of()).getOrDefault(act, 0L);
		if (!PuppetActRules.isReady(now, ready)) return refuse(player, Component.translatable("hemomancy.summon.act.cooldown",
				Component.translatable(act.nameKey()), PuppetActRules.secondsLeft(now, ready)));
		if (MarionetteCrossbarItem.getThread(crossbar) < act.threadCost()) return refuse(player,
				Component.translatable("hemomancy.summon.act.no_thread", Component.translatable(act.nameKey()), act.threadCost()));
		int performed = act == PuppetAct.RING ? ring(player, body) : perform(act, player, body) ? 1 : 0;
		if (performed == 0) return refuse(player, Component.translatable("hemomancy.summon.act.nothing",
				Component.translatable(act.nameKey())));
		MarionetteCrossbarItem.consumeThread(crossbar, act.threadCost());
		ACT_COOLDOWNS.computeIfAbsent(player.getUUID(), ignored -> new EnumMap<>(PuppetAct.class))
				.put(act, now + act.cooldownTicks());
		player.displayClientMessage(Component.translatable("hemomancy.summon.act.performed",
				Component.translatable(act.nameKey())).withStyle(ChatFormatting.RED), true);
		return true;
	}

	/** Answers how many bodies performed. */
	static int ring(ServerPlayer owner, Mob pattern) {
		int performed = 0;
		for (Mob body : MarionetteCrossbarItem.activeSummonsForOwner(owner)) {
			if (body == pattern || BoundSummonBehavior.isRingmasterConductor(body)
					|| !BoundSummonBehavior.sharesOwnerSession(pattern, body)
					|| !(body instanceof BoundPuppeteerSummon bound)) continue;
			Optional<PuppetAct> act = PuppetAct.forSummon(bound.hemomancy$getSummonName());
			if (act.isEmpty() || !perform(act.get(), owner, body)) continue;
			performed++;
			if (body.level() instanceof ServerLevel level) {
				BossTelegraphs.tendril(level, pattern.position().add(0.0D, pattern.getBbHeight() * 0.8D, 0.0D),
						body.position().add(0.0D, body.getBbHeight() * 0.6D, 0.0D), CircusEffects.THREAD_CORE_ARGB,
						CircusEffects.THREAD_GLOW_ARGB, body.getId() * 31L + level.getGameTime());
			}
		}
		if (performed > 0 && pattern.level() instanceof ServerLevel level) {
			CircusEffects.ringFootprint(level, pattern.position(), 2.5D, 1.0F);
			level.playSound(null, pattern.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 0.6F, 1.3F);
		}
		return performed;
	}

	static boolean perform(PuppetAct act, ServerPlayer owner, Mob body) {
		if (!(body.level() instanceof ServerLevel level)) return false;
		return switch (act) {
			case SPOTLIGHT -> body instanceof ScarletMummerEntity mummer && spotlight(level, owner, mummer);
			case TRAPEZE -> trapeze(level, owner, body);
			case FEINT -> body instanceof MarrowSpitterEntity juggler && feint(level, owner, juggler);
			case SMOKE -> smoke(level, owner, body);
			case ENCORE -> body instanceof MnemonistPuppetEntity mnemonist && mnemonist.replayNow();
			case SCENT -> scent(level, body);
			case LIFT -> lift(level, owner, body);
			case RING -> false;
		};
	}

	private static boolean spotlight(ServerLevel level, ServerPlayer owner, ScarletMummerEntity mummer) {
		if (!mummer.spotlight(owner, PuppetActRules.SPOTLIGHT_RADIUS)) return false;
		CircusEffects.ringFootprint(level, mummer.position(), PuppetActRules.SPOTLIGHT_RADIUS * 0.5D, 1.0F);
		BossTelegraphs.glow(level, mummer.position().add(0.0D, 2.2D, 0.0D), CircusEffects.BONE, 16, 0.4D);
		return true;
	}

	private static boolean trapeze(ServerLevel level, ServerPlayer owner, Mob vulture) {
		Vec3 ownerAt = owner.position();
		Vec3 vultureAt = vulture.position();
		if (!PuppetActRules.withinTrapeze(ownerAt.distanceToSqr(vultureAt))
				|| !level.noCollision(owner, owner.getBoundingBox().move(vultureAt.subtract(ownerAt)))
				|| level.containsAnyLiquid(owner.getBoundingBox().move(vultureAt.subtract(ownerAt)))) return false;
		owner.teleportTo(level, vultureAt.x, vultureAt.y, vultureAt.z, owner.getYRot(), owner.getXRot());
		owner.resetFallDistance();
		owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, PuppetActRules.TRAPEZE_GLIDE_TICKS, 0, false, false, true));
		vulture.teleportTo(ownerAt.x, ownerAt.y, ownerAt.z);
		CircusEffects.vault(level, ownerAt);
		CircusEffects.vault(level, vultureAt);
		BossTelegraphs.tendril(level, ownerAt.add(0.0D, 1.0D, 0.0D), vultureAt.add(0.0D, 1.0D, 0.0D),
				CircusEffects.THREAD_CORE_ARGB, CircusEffects.THREAD_GLOW_ARGB, vulture.getId() * 17L + level.getGameTime());
		return true;
	}

	private static boolean feint(ServerLevel level, ServerPlayer owner, MarrowSpitterEntity juggler) {
		List<Mob> drawn = threatsOn(level, owner, owner.position(), PuppetActRules.FEINT_RADIUS);
		if (drawn.isEmpty()) return false;
		for (Mob threat : drawn) {
			threat.setTarget(juggler);
			BossTelegraphs.tendril(level, juggler.position().add(0.0D, 0.5D, 0.0D),
					threat.position().add(0.0D, threat.getBbHeight() * 0.6D, 0.0D), CircusEffects.THREAD_CORE_ARGB,
					CircusEffects.THREAD_GLOW_ARGB, threat.getId() * 13L + level.getGameTime());
		}
		juggler.throwDagger(drawn.getFirst(), true);
		return true;
	}

	private static boolean smoke(ServerLevel level, ServerPlayer owner, Mob bellows) {
		Vec3 at = bellows.position().add(0.0D, 1.0D, 0.0D);
		List<Mob> hidden = threatsOn(level, owner, bellows.position(), PuppetActRules.SMOKE_RADIUS);
		for (Mob threat : hidden) threat.setTarget(null);
		owner.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, PuppetActRules.SMOKE_TICKS, 0, false, false, true));
		level.sendParticles(HemoParticleData.darkGlow(CircusEffects.CLOT), at.x, at.y, at.z, 60,
				PuppetActRules.SMOKE_RADIUS * 0.4D, 0.8D, PuppetActRules.SMOKE_RADIUS * 0.4D, 0.02D);
		BossTelegraphs.glow(level, at.add(0.0D, 0.6D, 0.0D), CircusEffects.BONE, 18, 1.4D);
		level.playSound(null, bellows.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6F, 0.6F);
		return true;
	}

	private static boolean scent(ServerLevel level, Mob hound) {
		List<Mob> marked = level.getEntitiesOfClass(Mob.class, hound.getBoundingBox().inflate(PuppetActRules.SCENT_RADIUS),
				target -> target.isAlive() && hound instanceof BoundPuppeteerSummon bound
						&& BoundSummonBehavior.canAttack(hound, bound, target));
		if (marked.isEmpty()) return false;
		for (Mob target : marked) {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, PuppetActRules.SCENT_TICKS, 0, false, false));
			BossTelegraphs.glow(level, target.position().add(0.0D, target.getBbHeight() + 0.3D, 0.0D), CircusEffects.CRIMSON, 4, 0.2D);
		}
		level.sendParticles(HemoParticleData.serpent(CircusEffects.CRIMSON), hound.getX(), hound.getY() + 0.8D, hound.getZ(),
				18, 0.4D, 0.3D, 0.4D, 0.02D);
		return true;
	}

	private static boolean lift(ServerLevel level, ServerPlayer owner, Mob hulk) {
		if (owner.distanceToSqr(hulk) > PuppetActRules.LIFT_REACH * PuppetActRules.LIFT_REACH) return false;
		Vec3 look = owner.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
		Vec3 throwing = (look.lengthSqr() < 1.0E-4D ? Vec3.ZERO : look.normalize().scale(PuppetActRules.LIFT_FORWARD))
				.add(0.0D, PuppetActRules.LIFT_UP, 0.0D);
		owner.setDeltaMovement(throwing);
		owner.hurtMarked = true;
		owner.resetFallDistance();
		owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, PuppetActRules.LIFT_GLIDE_TICKS, 0, false, false, true));
		CircusEffects.impact(level, hulk.position().add(0.0D, 0.1D, 0.0D), 1.5D);
		CircusEffects.dust(level, owner.position(), 10);
		level.playSound(null, hulk.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 0.8F, 0.8F);
		return true;
	}

	/** Threats near {@code center} that are attacking the owner or one of the owner's bodies. */
	private static List<Mob> threatsOn(ServerLevel level, ServerPlayer owner, Vec3 center, double radius) {
		return level.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(center, center).inflate(radius),
				mob -> mob.isAlive() && !(mob instanceof BoundPuppeteerSummon) && attacksOwnerSide(mob.getTarget(), owner))
				.stream().sorted(Comparator.comparingDouble(mob -> mob.distanceToSqr(center))).toList();
	}

	private static boolean attacksOwnerSide(LivingEntity target, ServerPlayer owner) {
		return target != null && (target.getUUID().equals(owner.getUUID())
				|| target instanceof BoundPuppeteerSummon bound && owner.getUUID().equals(bound.hemomancy$getOwnerUUID()));
	}

	private static boolean refuse(ServerPlayer player, Component message) {
		player.displayClientMessage(message.copy().withStyle(ChatFormatting.GRAY), true);
		return false;
	}

	public static void clearSessionState() {
		ACT_COOLDOWNS.clear();
	}

	public static void logout(Player player) {
		ACT_COOLDOWNS.remove(player.getUUID());
	}
}
