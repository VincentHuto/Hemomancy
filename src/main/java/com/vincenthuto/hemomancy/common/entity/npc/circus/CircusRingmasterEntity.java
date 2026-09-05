package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.circus.*;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VampireBatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class CircusRingmasterEntity extends PathfinderMob {
	private static final EntityDataAccessor<Boolean> VULNERABLE = SynchedEntityData.defineId(
			CircusRingmasterEntity.class, EntityDataSerializers.BOOLEAN);
	public static final String EVENT_ACCEPT = "circus_accept_pact";
	public static final String EVENT_REJECT = "circus_reject_pact";
	public static final String EVENT_REPAIR = "circus_repair_route";
	public static final String EVENT_TRIAL = "circus_begin_next_trial";
	public static final String EVENT_FINALE = "circus_begin_finale";
	private BlockPos perch;
	private BlockPos encounterOrigin;
	private int phaseTicks;

	public CircusRingmasterEntity(EntityType<? extends CircusRingmasterEntity> type, Level level) {
		super(type, level);
		setPersistenceRequired();
		setNoGravity(true);
		noPhysics = true;
		xpReward = 0;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 80.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.30D).add(Attributes.ATTACK_DAMAGE, 7.0D)
				.add(Attributes.ARMOR, 12.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(VULNERABLE, false);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, false));
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) return;
		if (perch == null) perch = blockPosition();
		CircusCarouselEntity carousel = nearestCarousel();
		if (carousel == null) return;
		encounterOrigin = carousel.encounterOrigin();
		ServerLevel server = (ServerLevel) level();
		CircusPavilionSavedData data = CircusPavilionSavedData.get(server);
		CircusPavilionSavedData.Site site = data.site(server, encounterOrigin);
		if (site.activeOwner() == null) {
			entityData.set(VULNERABLE, false);
			troupe().forEach(CircusPerformerEntity::resetFinale);
			setTarget(null);
			getNavigation().stop();
			setNoGravity(true);
			noPhysics = true;
			phaseTicks = 0;
			removeSwarm();
			setPos(perch.getX() + 0.5D, perch.getY(), perch.getZ() + 0.5D);
			return;
		}
		Player foundOwner = server.getPlayerByUUID(site.activeOwner());
		if (!(foundOwner instanceof ServerPlayer owner) || !owner.isAlive()) return;
		if (site.phase() == CircusPavilionStateRules.Phase.PERFORMANCE) {
			entityData.set(VULNERABLE, false);
			setTarget(null);
			return;
		}
		List<CircusPerformerEntity> troupe = troupe();
		troupe.forEach(performer -> performer.beginFinale(owner,
				site.route() == CircusRouteRules.Route.LIBERATION));
		if (site.phase() == CircusPavilionStateRules.Phase.RAFTERS) {
			setNoGravity(true);
			noPhysics = true;
			setPos(perch.getX() + 0.5D, perch.getY(), perch.getZ() + 0.5D);
			setTarget(null);
			phaseTicks++;
			if (phaseTicks == 1 || phaseTicks % 100 == 0 && !hasSwarm(owner)) spawnSwarm(owner);
			if (phaseTicks % 35 == 0) throwKnife(owner);
		} else if (site.phase() == CircusPavilionStateRules.Phase.CAROUSEL) {
			setTarget(null);
			getNavigation().stop();
		} else if (site.phase() == CircusPavilionStateRules.Phase.DESCENT) {
			entityData.set(VULNERABLE, true);
			noPhysics = false;
			setNoGravity(false);
			if (distanceToSqr(carousel) > 12.0D * 12.0D) {
				teleportTo(carousel.getX(), carousel.getY() + 1.0D, carousel.getZ() + 4.0D);
			}
			setTarget(owner);
		}
		CircusPavilionStateRules.Phase next = CircusFinaleRules.nextPhase(site.route(), site.phase(), phaseTicks,
				!troupe.isEmpty() && troupe.stream().allMatch(CircusPerformerEntity::isDowned), carousel.isDestroyed());
		if (next != site.phase()) {
			data.setPhase(server, encounterOrigin, next);
			phaseTicks = 0;
			server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 0.8F, 1.4F);
		}
	}

	private CircusCarouselEntity nearestCarousel() {
		AABB search = encounterOrigin == null ? getBoundingBox().inflate(20.0D)
				: new AABB(encounterOrigin).inflate(20.0D);
		return level().getEntitiesOfClass(CircusCarouselEntity.class, search).stream()
				.min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
	}

	private List<CircusPerformerEntity> troupe() {
		return level().getEntitiesOfClass(CircusPerformerEntity.class, new AABB(encounterOrigin).inflate(20.0D));
	}

	private void throwKnife(ServerPlayer owner) {
		CircusKnifeProjectileEntity knife = new CircusKnifeProjectileEntity(level(), this, 4.0F);
		knife.setPos(getX(), getEyeY() - 0.2D, getZ());
		knife.shoot(owner.getX() - getX(), owner.getEyeY() - getEyeY(), owner.getZ() - getZ(), 1.4F, 3.0F);
		level().addFreshEntity(knife);
	}

	private void spawnSwarm(ServerPlayer owner) {
		VampireBatEntity swarm = EntityInit.vampire_bat.get().create(level());
		if (swarm == null) return;
		swarm.setPos(getX(), getY() + 1.0D, getZ());
		swarm.setCustomName(Component.translatable("entity.hemomancy.manifested_vampire_bat_swarm"));
		swarm.makeManifestedAggregate(owner.getUUID());
		level().addFreshEntity(swarm);
	}

	private void removeSwarm() {
		level().getEntitiesOfClass(VampireBatEntity.class, getBoundingBox().inflate(32.0D),
				bat -> bat.getPersistentData().hasUUID("CircusSwarmOwner")).forEach(VampireBatEntity::discard);
	}

	private boolean hasSwarm(ServerPlayer owner) {
		return !level().getEntitiesOfClass(VampireBatEntity.class, getBoundingBox().inflate(32.0D),
				bat -> bat.getPersistentData().hasUUID("CircusSwarmOwner")
						&& owner.getUUID().equals(bat.getPersistentData().getUUID("CircusSwarmOwner"))).isEmpty();
	}

	public void handleChoice(ServerPlayer player, String eventId) {
		CircusCarouselEntity carousel = nearestCarousel();
		if (carousel == null || player.distanceToSqr(carousel) > 24.0D * 24.0D) return;
		boolean attentionComplete = (CircusPlayerProgress.challenges(player) & 1) != 0;
		if (EVENT_ACCEPT.equals(eventId) && attentionComplete
				&& CircusProgressRules.canReceivePact(CircusPlayerProgress.acclimation(player))) {
			if (CircusPlayerProgress.chooseRoute(player, CircusRouteRules.Route.SUCCESSION))
				CircusPlayerProgress.completeChallenge(player, 1, 50);
		} else if (EVENT_REJECT.equals(eventId) && attentionComplete
				&& CircusProgressRules.canReceivePact(CircusPlayerProgress.acclimation(player))) {
			if (CircusPlayerProgress.chooseRoute(player, CircusRouteRules.Route.LIBERATION)) {
				CircusPlayerProgress.completeChallenge(player, 1, 50);
			}
		} else if (EVENT_REPAIR.equals(eventId)) {
			CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(player.serverLevel())
					.site(player.serverLevel(), carousel.encounterOrigin());
			if (site.activeOwner() == null && site.outcome() == CircusPavilionStateRules.Outcome.NEUTRAL)
				CircusPlayerProgress.repairRoute(player);
		} else if (EVENT_TRIAL.equals(eventId)) {
			CircusPerformanceController.beginNext(player, carousel.encounterOrigin());
		} else if (EVENT_FINALE.equals(eventId)) {
			beginFinale(player, carousel);
		}
		CircusPlayerProgress.sync(player, true);
	}

	private void beginFinale(ServerPlayer player, CircusCarouselEntity carousel) {
		CircusRouteRules.Route route = CircusPlayerProgress.route(player);
		if (!CircusRouteRules.canBeginFinale(route, CircusPlayerProgress.acclimation(player),
				CircusPlayerProgress.challenges(player))) {
			player.displayClientMessage(Component.translatable("hemomancy.circus.finale.not_ready")
					.withStyle(ChatFormatting.GRAY), false);
			return;
		}
		if (!CircusPavilionSavedData.get(player.serverLevel()).begin(player.serverLevel(),
				carousel.encounterOrigin(), player.getUUID(), route)) {
			player.displayClientMessage(Component.translatable("hemomancy.circus.finale.claimed")
					.withStyle(ChatFormatting.DARK_GRAY), false);
			return;
		}
		setHealth(getMaxHealth());
		phaseTicks = 0;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean administrative = source.is(DamageTypes.GENERIC_KILL)
				|| source.getEntity() instanceof Player player && player.isCreative();
		if (administrative) return super.hurt(source, amount);
		if (!(source.getEntity() instanceof ServerPlayer player) || !(level() instanceof ServerLevel server)
				|| encounterOrigin == null) return false;
		CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(server).site(server, encounterOrigin);
		if (site.phase() != CircusPavilionStateRules.Phase.DESCENT
				|| !CircusPavilionStateRules.canAct(site.activeOwner(), player.getUUID())) return false;
		if (site.route() == CircusRouteRules.Route.SUCCESSION && amount >= getHealth()) {
			setHealth(1.0F);
			complete(player, site.route());
			return true;
		}
		return super.hurt(source, amount);
	}

	@Override
	public void die(DamageSource source) {
		if (source.getEntity() instanceof ServerPlayer player && encounterOrigin != null
				&& level() instanceof ServerLevel server) {
			CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(server).site(server, encounterOrigin);
			if (site.route() == CircusRouteRules.Route.LIBERATION
					&& CircusPavilionStateRules.canAct(site.activeOwner(), player.getUUID())) complete(player, site.route());
		}
		super.die(source);
	}

	private void complete(ServerPlayer player, CircusRouteRules.Route route) {
		ServerLevel server = player.serverLevel();
		CircusPavilionStateRules.Outcome outcome = route == CircusRouteRules.Route.SUCCESSION
				? CircusPavilionStateRules.Outcome.SUCCESSION : CircusPavilionStateRules.Outcome.RUIN;
		if (!CircusPavilionSavedData.get(server).complete(server, encounterOrigin, player.getUUID(), outcome)) return;
		CircusPlayerProgress.completeRoute(player, route);
		if (route == CircusRouteRules.Route.SUCCESSION) {
			PuppeteerSummonDefinitions.byName(PuppeteerSummonDefinitions.RINGMASTER_PATTERN)
					.ifPresent(definition -> KnownSummonEvents.grantSummon(player, definition));
		} else {
			KnownManipulationGrantHelper.grantMemory(player, ManipulationInit.thread_ripper.get(),
					ItemInit.memory_thread_ripper.get());
			authorRuin(server);
		}
		ItemStack topper = new ItemStack(ItemInit.ringmaster_topper.get());
		if (!player.getInventory().add(topper)) player.drop(topper, false);
		troupe().forEach(performer -> { if (performer.isAlive()) performer.resetFinale(); });
		removeSwarm();
		server.sendParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY() + 1.0D, getZ(),
				80, 1.5D, 1.5D, 1.5D, 0.08D);
		server.playSound(null, blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.7F);
		player.displayClientMessage(Component.translatable("hemomancy.circus.finale.complete." + route.serializedName())
				.withStyle(ChatFormatting.DARK_RED), false);
	}

	private void authorRuin(ServerLevel server) {
		for (BlockPos offset : List.of(
				new BlockPos(3, 1, 0), new BlockPos(-3, 1, 0), new BlockPos(0, 1, 3),
				new BlockPos(0, 1, -3), new BlockPos(2, 2, 2), new BlockPos(-2, 2, -2))) {
			BlockPos pos = encounterOrigin.offset(offset);
			if (server.getBlockState(pos).isAir()) server.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
		}
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
		if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
			if (!CircusProgressRules.canReceivePact(CircusPlayerProgress.acclimation(player))) {
				player.displayClientMessage(Component.translatable("hemomancy.circus.ringmaster.silent")
						.withStyle(ChatFormatting.DARK_GRAY), false);
			} else {
				PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(dialogue(serverPlayer)));
			}
		}
		return InteractionResult.sidedSuccess(level().isClientSide);
	}

	private DialogueTree dialogue(ServerPlayer player) {
		CircusRouteRules.Route route = CircusPlayerProgress.route(player);
		java.util.ArrayList<DialogueOption> options = new java.util.ArrayList<>();
		if (route == CircusRouteRules.Route.NEUTRAL) {
			if ((CircusPlayerProgress.challenges(player) & 1) == 0) {
				options.add(new DialogueOption("hemomancy.circus.option.trial", null, EVENT_TRIAL));
			} else {
				options.add(new DialogueOption("hemomancy.circus.option.accept", "succession_warning", null));
				options.add(new DialogueOption("hemomancy.circus.option.reject", "liberation_warning", null));
			}
		} else if (route == CircusRouteRules.Route.SUCCESSION || route == CircusRouteRules.Route.LIBERATION) {
			if (route == CircusRouteRules.Route.SUCCESSION
					&& (CircusPlayerProgress.challenges(player) & CircusRouteRules.ALL_CHALLENGES)
					!= CircusRouteRules.ALL_CHALLENGES)
				options.add(new DialogueOption("hemomancy.circus.option.trial", null, EVENT_TRIAL));
			options.add(new DialogueOption("hemomancy.circus.option.finale", null, EVENT_FINALE));
			if (CircusPlayerProgress.canRepairRoute(player))
				options.add(new DialogueOption("hemomancy.circus.option.repair", null, EVENT_REPAIR));
		}
		options.add(new DialogueOption("hemomancy.dialogue.circus_performer.leave", null, null));
		return DialogueTree.builder(getType().getDescriptionId(),
				Hemomancy.rloc("textures/entity/circus/ringmaster.png"), getId())
				.addNode(new DialogueNode("greeting", List.of("hemomancy.circus.ringmaster.line." + route.serializedName()),
						options))
				.addNode(new DialogueNode("succession_warning",
						List.of("hemomancy.circus.ringmaster.warning.succession"),
						List.of(new DialogueOption("hemomancy.circus.option.confirm_succession", null, EVENT_ACCEPT),
								new DialogueOption("hemomancy.circus.option.not_yet", "greeting", null))))
				.addNode(new DialogueNode("liberation_warning",
						List.of("hemomancy.circus.ringmaster.warning.liberation"),
						List.of(new DialogueOption("hemomancy.circus.option.confirm_liberation", null, EVENT_REJECT),
								new DialogueOption("hemomancy.circus.option.not_yet", "greeting", null))))
				.build();
	}

	@Override public void push(double x, double y, double z) { }
	@Override public boolean isPushable() { return false; }
	@Override public boolean isPickable() { return true; }
	@Override public boolean isAttackable() { return entityData.get(VULNERABLE); }
	@Override public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (perch != null) tag.putLong("CircusPerch", perch.asLong());
		if (encounterOrigin != null) tag.putLong("CircusOrigin", encounterOrigin.asLong());
		tag.putInt("CircusPhaseTicks", phaseTicks);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		perch = tag.contains("CircusPerch") ? BlockPos.of(tag.getLong("CircusPerch")) : null;
		encounterOrigin = tag.contains("CircusOrigin") ? BlockPos.of(tag.getLong("CircusOrigin")) : null;
		phaseTicks = tag.getInt("CircusPhaseTicks");
	}
}
