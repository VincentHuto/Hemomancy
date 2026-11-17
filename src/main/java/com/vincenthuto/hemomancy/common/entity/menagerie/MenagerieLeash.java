package com.vincenthuto.hemomancy.common.entity.menagerie;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Leading a creature home (core-loop Track 5). Holding Blood Binding's Sculpting for three seconds on a creature that is
 * passive, or at a third of its health or less, leashes it: it turns placid, follows its keeper, stops despawning and
 * goes through a portal with them. The leash costs blood upkeep by the creature's size; when the keeper cannot pay,
 * it breaks free, a hostile one angry. Left behind by a keeper who is online, it waits a while and then breaks free;
 * with its keeper offline it waits. A Harbinger leads one creature at D3, two at D5, three at D7 (CL-13).
 *
 * The leash lives on the creature ({@link LeashState}, an attachment), so it survives unloading, relogs and restarts;
 * the maps here are an index of loaded led creatures, rebuilt as each one joins its level, and the binding holds in
 * progress. Both clear through {@code TransientServerStateEvents}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MenagerieLeash {
	public static final BloodAccounting.Source LEASH = BloodAccounting.Source.of("menagerie_leash", Category.CAST, false);
	private static final double FOLLOW_START = 3.5D, FOLLOW_STOP = 2.5D, TELEPORT_PAST = 24.0D;
	/** Kinds that are never kept: the people and Wills below and every boss (CR-26). */
	public static final TagKey<EntityType<?>> UNKEEPABLE = TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc("menagerie_unkeepable"));
	public static final TagKey<EntityType<?>> PEOPLE = TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc("menagerie_people"));
	public static final TagKey<EntityType<?>> WILLS = TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc("menagerie_wills"));

	/** Loaded led creatures, by UUID. */
	private static final Map<UUID, Mob> LED = new HashMap<>();
	/** Led creatures that stood within carrying range of their keeper at the last tick. */
	private static final Set<UUID> NEAR = new HashSet<>();
	private MenagerieLeash() {
	}

	// ── Who may be kept ──

	/** What keeping sees of {@code entity}. */
	public static MenagerieRules.Candidate candidate(Entity entity) {
		EntityType<?> type = entity.getType();
		boolean owned = entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null;
		return new MenagerieRules.Candidate(entity.isAlive(), entity instanceof Mob, entity instanceof Player,
				entity instanceof LivingEntity living && HemoEntityPredicates.isBoss(living), owned,
				entity instanceof BoundPuppeteerSummon, type.is(WILLS), entity instanceof AbstractVillager || type.is(PEOPLE),
				SaintChannelRouter.refusesOrdinaryDrain(entity), entity instanceof Warden, type.is(UNKEEPABLE));
	}

	/** {@link MenagerieRules#canKeep} for one creature. The one answer for leash, jar and vat. */
	public static MenagerieRules.Verdict verdict(Entity entity) {
		return MenagerieRules.canKeep(candidate(entity));
	}

	public static boolean hostile(Entity entity) {
		return entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER;
	}

	// ── Reading ──

	@Nullable
	public static UUID keeperOf(Entity entity) {
		LeashState.Leash leash = HemoCapabilityAccess.getMenagerieLeash(entity);
		return leash == null ? null : leash.keeper();
	}

	public static boolean isLed(Entity entity) {
		return HemoCapabilityAccess.getMenagerieLeash(entity) != null;
	}

	public static boolean leads(Player player, Entity entity) {
		return player.getUUID().equals(keeperOf(entity));
	}

	/** The loaded creatures {@code keeper} leads in {@code level}. */
	public static List<Mob> ledBy(ServerLevel level, UUID keeper) {
		List<Mob> led = new ArrayList<>();
		for (Mob mob : LED.values()) {
			if (mob.level() == level && mob.isAlive() && keeper.equals(keeperOf(mob))) led.add(mob);
		}
		return led;
	}

	/** How many loaded creatures {@code keeper} leads anywhere. */
	public static int ledCount(UUID keeper) {
		int count = 0;
		for (Mob mob : LED.values()) if (mob.isAlive() && keeper.equals(keeperOf(mob))) count++;
		return count;
	}

	// ── Leashing ──

	/**
	 * The Tendon Line thrown into {@code mob}: the line roots in it and it is led, when it may be kept, is weak enough
	 * and the keeper has a lead to spare. Otherwise the keeper is told why and nothing is tied.
	 */
	public static boolean tieLine(ServerPlayer keeper, Mob mob) {
		Component refusal = refusal(keeper, mob);
		if (refusal != null) {
			keeper.displayClientMessage(refusal, true);
			return false;
		}
		return leash(keeper, mob);
	}

	/** Why {@code binder} cannot leash {@code mob} now, or null when they can. */
	@Nullable
	public static Component refusal(ServerPlayer binder, Mob mob) {
		MenagerieRules.Verdict verdict = verdict(mob);
		if (!verdict.keeps()) {
			return verdict.messageKey() == null ? null
					: Component.translatable(verdict.messageKey(), mob.getDisplayName()).withStyle(ChatFormatting.GRAY);
		}
		if (!MenagerieRules.weakEnough(mob.getHealth(), mob.getMaxHealth(), hostile(mob))) {
			return Component.translatable("message.hemomancy.menagerie.too_strong", mob.getDisplayName())
					.withStyle(ChatFormatting.GRAY);
		}
		int allowed = MenagerieRules.maxLed(HemoCapabilityAccess.getPlayerDegreeNumber(binder));
		if (allowed <= 0) {
			return Component.translatable("message.hemomancy.menagerie.needs_degree").withStyle(ChatFormatting.GRAY);
		}
		if (ledCount(binder.getUUID()) >= allowed) {
			return Component.translatable("message.hemomancy.menagerie.too_many", allowed).withStyle(ChatFormatting.GRAY);
		}
		return null;
	}

	/** Leashes {@code mob} to {@code keeper} now (refusals are checked by the caller). */
	public static boolean leash(ServerPlayer keeper, Mob mob) {
		if (isLed(mob) || !mob.isAlive()) return false;
		boolean targetDisabled = ((com.vincenthuto.hemomancy.mixin.core.GoalSelectorAccessor) mob.targetSelector)
				.hemomancy$disabledFlags().contains(Goal.Flag.TARGET);
		HemoCapabilityAccess.setMenagerieLeash(mob, new LeashState.Leash(keeper.getUUID(), targetDisabled, LeashState.NEVER));
		LED.put(mob.getUUID(), mob);
		// A creature held by a binding when the line takes it is let loose to walk with its keeper.
		mob.removeEffect(EffectInit.blood_binding);
		mob.removeEffect(EffectInit.constricted);
		calm(mob);
		mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
		mob.level().playSound(null, mob.blockPosition(), SoundEvents.LEASH_KNOT_PLACE, SoundSource.PLAYERS, 0.8F, 0.7F);
		keeper.displayClientMessage(Component.translatable("message.hemomancy.menagerie.leashed", mob.getDisplayName(),
				String.format(java.util.Locale.ROOT, "%.1f", MenagerieRules.upkeepPerSecond(mob.getMaxHealth())))
				.withStyle(ChatFormatting.DARK_RED), true);
		tether(keeper, mob);
		HarbingerAdvancementGranter.grantIfNotDone(keeper, HarbingerAdvancementGranter.ADV_KEPT);
		return true;
	}

	/**
	 * Ends the leash on {@code mob}. {@code angry}: it broke free, and a hostile creature turns on its keeper if they
	 * are near; otherwise it is let go quietly.
	 */
	public static void release(Mob mob, boolean angry) {
		LeashState.Leash leash = HemoCapabilityAccess.getMenagerieLeash(mob);
		HemoCapabilityAccess.setMenagerieLeash(mob, null);
		LED.remove(mob.getUUID());
		NEAR.remove(mob.getUUID());
		ManipulationVisuals.attached(mob, ManipulationVisuals.Form.MARIONETTE_TETHER, 0, 0, 0);
		if (leash == null) return;
		mob.targetSelector.setControlFlag(Goal.Flag.TARGET, !leash.targetDisabled());
		if (angry && hostile(mob) && mob.level() instanceof ServerLevel level
				&& level.getPlayerByUUID(leash.keeper()) instanceof ServerPlayer keeper
				&& keeper.distanceToSqr(mob) < 32.0D * 32.0D && !keeper.isCreative()) {
			mob.setTarget(keeper);
		}
	}

	private static void calm(Mob mob) {
		mob.setTarget(null);
		mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
		mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
	}

	// ── The leash's tick ──

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level) || LED.isEmpty()) return;
		long now = level.getGameTime();
		for (Mob mob : List.copyOf(LED.values())) {
			if (mob.level() != level) continue;
			LeashState.Leash leash = HemoCapabilityAccess.getMenagerieLeash(mob);
			if (leash == null || !mob.isAlive()) {
				LED.remove(mob.getUUID());
				NEAR.remove(mob.getUUID());
				continue;
			}
			tick(level, mob, leash, now);
		}
	}

	private static void tick(ServerLevel level, Mob mob, LeashState.Leash leash, long now) {
		ServerPlayer keeper = level.getServer().getPlayerList().getPlayer(leash.keeper());
		calm(mob);
		if (keeper == null) {
			// The keeper is away from the world: the creature waits, placid, and nothing is paid.
			mob.getNavigation().stop();
			NEAR.remove(mob.getUUID());
			return;
		}
		boolean inReach = keeper.level() == level && keeper.isAlive()
				&& keeper.distanceToSqr(mob) <= MenagerieRules.FOLLOW_RANGE * MenagerieRules.FOLLOW_RANGE;
		if (!inReach) {
			NEAR.remove(mob.getUUID());
			mob.getNavigation().stop();
			if (leash.awaySince() == LeashState.NEVER) {
				HemoCapabilityAccess.setMenagerieLeash(mob, leash.away(now));
			} else if (now - leash.awaySince() >= MenagerieRules.ABANDON_TICKS) {
				release(mob, true);
				keeper.displayClientMessage(Component.translatable("message.hemomancy.menagerie.wandered",
						mob.getDisplayName()).withStyle(ChatFormatting.GRAY), true);
			}
			return;
		}
		if (leash.awaySince() != LeashState.NEVER) HemoCapabilityAccess.setMenagerieLeash(mob, leash.away(LeashState.NEVER));
		double distance = keeper.distanceTo(mob);
		if (distance <= MenagerieRules.CARRY_RANGE) NEAR.add(mob.getUUID());
		else NEAR.remove(mob.getUUID());
		if (Math.floorMod(now + mob.getId(), 20L) == 0L && !payUpkeep(keeper, mob)) return;
		if (Math.floorMod(now + mob.getId(), 5L) == 0L) follow(level, keeper, mob, distance);
		if (Math.floorMod(now + mob.getId(), 10L) == 0L) tether(keeper, mob);
	}

	/** One second of the leash's upkeep. A keeper who cannot pay in full loses the creature. */
	private static boolean payUpkeep(ServerPlayer keeper, Mob mob) {
		if (keeper.isCreative()) return true;
		double cost = MenagerieRules.upkeepPerSecond(mob.getMaxHealth());
		boolean active = HemoCapabilityAccess.getBloodVolume(keeper).isActive();
		BloodAccounting.Payment payment = active ? BloodAccounting.payUpkeep(keeper, cost, LEASH, 20, true) : null;
		if (payment != null && payment.satisfied()) return true;
		release(mob, true);
		mob.level().playSound(null, mob.blockPosition(), SoundEvents.LEASH_KNOT_BREAK, SoundSource.PLAYERS, 0.9F, 0.8F);
		keeper.displayClientMessage(Component.translatable("message.hemomancy.menagerie.broke_free", mob.getDisplayName())
				.withStyle(ChatFormatting.RED), true);
		return false;
	}

	private static void follow(ServerLevel level, ServerPlayer keeper, Mob mob, double distance) {
		if (distance > TELEPORT_PAST && keeper.onGround()) {
			Vec3 spot = keeper.position().subtract(keeper.getLookAngle().multiply(1.5D, 0.0D, 1.5D));
			if (level.noCollision(mob, mob.getBoundingBox().move(spot.subtract(mob.position())))) {
				mob.moveTo(spot.x, spot.y, spot.z, mob.getYRot(), mob.getXRot());
				mob.getNavigation().stop();
				return;
			}
		}
		if (distance > FOLLOW_START) mob.getNavigation().moveTo(keeper, 1.15D);
		else if (distance < FOLLOW_STOP) mob.getNavigation().stop();
		mob.getLookControl().setLookAt(keeper, 10.0F, mob.getMaxHeadXRot());
	}

	/** A thin living thread from the creature to its keeper's hand, for anyone watching (the Marionette's tether). */
	private static void tether(ServerPlayer keeper, Mob mob) {
		PacketHandler.sendTracking(mob, new ManipulationVisualPacket(ManipulationVisuals.Form.MARIONETTE_TETHER,
				mob.getId(), mob.position(), keeper.getEyePosition(), mob.getBbHeight(), 12, keeper.getId()));
	}

	// ── Lifecycle ──

	/** A led creature never despawns. */
	@SubscribeEvent
	public static void despawn(MobDespawnEvent event) {
		if (isLed(event.getEntity())) event.setResult(MobDespawnEvent.Result.DENY);
	}

	/** A creature carrying a leash rejoins the index as it loads, with its own targeting kept off. */
	@SubscribeEvent
	public static void joined(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob) || !isLed(mob)) return;
		LED.put(mob.getUUID(), mob);
		mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
	}

	@SubscribeEvent
	public static void left(EntityLeaveLevelEvent event) {
		if (event.getEntity() instanceof Mob mob && LED.get(mob.getUUID()) == mob) {
			LED.remove(mob.getUUID());
			NEAR.remove(mob.getUUID());
		}
	}

	/** The keeper, sneaking with an empty hand, lets a led creature go. */
	@SubscribeEvent
	public static void letGo(PlayerInteractEvent.EntityInteract event) {
		if (!(event.getTarget() instanceof Mob mob) || !event.getEntity().isShiftKeyDown()
				|| !event.getEntity().getMainHandItem().isEmpty() || event.getHand() != InteractionHand.MAIN_HAND
				|| !leads(event.getEntity(), mob)) return;
		if (event.getEntity() instanceof ServerPlayer keeper) {
			release(mob, false);
			keeper.displayClientMessage(Component.translatable("message.hemomancy.menagerie.let_go", mob.getDisplayName())
					.withStyle(ChatFormatting.GRAY), true);
		}
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}

	/** Led creatures near their keeper go through a portal with them. */
	@SubscribeEvent
	public static void keeperChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer keeper)) return;
		ServerLevel from = keeper.server.getLevel(event.getFrom());
		ServerLevel to = keeper.server.getLevel(event.getTo());
		if (from == null || to == null) return;
		for (Mob mob : ledBy(from, keeper.getUUID())) {
			if (!NEAR.contains(mob.getUUID())) continue;
			carry(mob, keeper, to);
		}
	}

	/** Moves a led creature to its keeper in {@code to}; returns the creature as it arrived, or null. */
	@Nullable
	public static Entity carry(Mob mob, ServerPlayer keeper, ServerLevel to) {
		LED.remove(mob.getUUID());
		NEAR.remove(mob.getUUID());
		mob.getNavigation().stop();
		return mob.changeDimension(new DimensionTransition(to, keeper.position(), Vec3.ZERO, mob.getYRot(), mob.getXRot(),
				DimensionTransition.DO_NOTHING));
	}

	/** Forgets the indexes; the leashes stay on the creatures and rejoin the index as they load. */
	public static void clearSessionState() {
		LED.clear();
		NEAR.clear();
	}

	/** Ends every loaded leash (tests use this to clean up). */
	public static void releaseAll() {
		for (Mob mob : List.copyOf(LED.values())) release(mob, false);
	}

	/** For tests: whether a led creature counted as near its keeper at the last tick. */
	public static boolean nearKeeper(Mob mob) {
		return NEAR.contains(mob.getUUID());
	}

	/** Whether the degree allows {@code keeper} another leash; for the readout and tests. */
	public static boolean hasLeashToSpare(ServerPlayer keeper) {
		return ProgressionGates.hasDegree(keeper, 3)
				&& ledCount(keeper.getUUID()) < MenagerieRules.maxLed(HemoCapabilityAccess.getPlayerDegreeNumber(keeper));
	}
}
