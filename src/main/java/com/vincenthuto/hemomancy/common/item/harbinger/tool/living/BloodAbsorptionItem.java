package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbReach;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hemomancy.common.manipulation.ferric.CellHandRules;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodInteractions;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.IBloodTemperament;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperBloodAbsorptionInteractions;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillAbsorptionRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillBloodUtilityInteractions;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteCancellationHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BloodAbsorptionItem extends Item implements IDispellable, ICellHand {
	/** Absorption strain per player; see {@link BloodAbsorptionChannelRules#nextStrain}. */
	private static final Map<UUID, BloodAbsorptionChannelRules.Strain> STRAIN = new HashMap<>();
	private static final int STRAIN_EFFECT_DURATION = 80;

	public BloodAbsorptionItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
		return true;
	}

	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
		return armorType == EquipmentSlot.MAINHAND || armorType == EquipmentSlot.OFFHAND;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
		if (entityLiving instanceof Player) {
			((Player) entityLiving).releaseUsingItem();
		}
		return stack;
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000 / 2;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		return InteractionResult.PASS;
	}

	@Override
	public void onUseTick(Level worldIn, LivingEntity player, ItemStack stack, int count) {
		if (player instanceof Player channelingPlayer) {
			VerbChannel.applyMovement(channelingPlayer);
		}
		if (worldIn.isClientSide) {
			return;
		}
		double reach = VerbReach.of(player, Verb.ABSORB);
		if (count % CellHandRules.USE_REFRESH == 0)
			ManipulationVisuals.attached(player, ManipulationVisuals.Form.CELL_HAND, 1, CellHandRules.USE_TICKS, CellHandRules.ABSORBING);
		double vesperHandled = VesperBloodAbsorptionInteractions.tryAbsorb(worldIn, player, reach,
				WillAbsorptionRules.bareProgressPerTick());
		// The Ringmaster at his kneel takes the same channel (2026-11-18).
		if (vesperHandled <= 0.0D) vesperHandled = com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterAbsorption
				.tryAbsorb(worldIn, player, reach, WillAbsorptionRules.bareProgressPerTick());
		if (vesperHandled > 0.0D) {
			updateChannelStrain(player, false);
			return;
		}
		if (SaintChannelRouter.tryAbsorb(worldIn, player, reach, LivingStaffFocusRules.bareBlockAbsorptionPerTick(),
				false).handled()) {
			updateChannelStrain(player, false);
			return;
		}
		if (player instanceof ServerPlayer serverPlayer
				&& CardinalRiteCancellationHandler.tryChannel(serverPlayer, reach)) {
			updateChannelStrain(player, false);
			return;
		}
		double blockHandled = BlockBloodInteractions.tryAbsorbFromLookedAtBlock(worldIn, player, reach,
				LivingStaffFocusRules.bareBlockAbsorptionPerTick());
		if (blockHandled > 0.0D) {
			updateChannelStrain(player, false);
			return;
		}
		double willHandled = WillBloodUtilityInteractions.tryAbsorbFalteringWill(worldIn, player, reach);
		if (willHandled > 0.0D) {
			updateChannelStrain(player, false);
			return;
		}
		// A selected Sculpting that claims the aimed-at thing acts before the living-target pulse and before the
		// full-body stop, so a draw that is not blood still works on a full body.
		if (player instanceof ServerPlayer sculptor && SculptingDispatcher.trySculpt(sculptor, Verb.ABSORB).claimed()) {
			updateChannelStrain(player, false);
			return;
		}
		// A stain, gouge, vein or Lux trace on the aimed face is taken back up (core-loop Track 6).
		if (player instanceof ServerPlayer groundPlayer && GroundNetwork.tryAbsorb(groundPlayer, reach)) {
			updateChannelStrain(player, false);
			return;
		}
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !BloodAbsorptionChannelRules.canDrainLivingTarget(volume.isActive())) {
			updateChannelStrain(player, false);
			return;
		}
		int elapsed = getUseDuration(stack, player) - count;
		int interval = BloodAbsorptionChannelRules.livingTargetPulseInterval();
		if (elapsed % interval != 0) {
			updateChannelStrain(player, false);
			return;
		}
		double pulse = LivingStaffFocusRules.bareAbsorptionDamagePerTick()
				* BloodAbsorptionChannelRules.cadenceDrainMultiplier(player instanceof Player channelingPlayer
						? SkillPointHelper.getAbsorptionCadenceLevel(channelingPlayer) : 0);
		boolean drainedLivingTarget = findBareAbsorptionTarget(player, reach)
				.map(target -> absorbFromTarget(worldIn, player, target, pulse) > 0.0D)
				.orElse(false);
		updateChannelStrain(player, drainedLivingTarget);
	}

	@Override
	public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
		super.onStopUsing(stack, entity, count);
		if (entity instanceof ServerPlayer player) SculptingSessions.end(player, true);
	}

	/** How fast Absorption's rooting lets the player move, by its movement skills; see {@link VerbChannel#movementMultiplier}. */
	public static double getChannelMovementMultiplier(Player player) {
		return BloodAbsorptionChannelRules.movementMultiplier(
				SkillPointHelper.getDraggingSiphonLevel(player),
				SkillPointHelper.getMobileConduitLevel(player),
				SkillPointHelper.hasUnboundSiphon(player));
	}

	/** Whether the player is channelling Absorption, through a cell hand or the Living Staff. */
	public static boolean isChannelingBloodAbsorption(Player player) {
		return VerbChannel.isChannelling(player, Verb.ABSORB);
	}

	/** Carries the player's absorption strain to now, with a draining pulse now if {@code drainedLivingTarget}. */
	public static void updateChannelStrain(LivingEntity user, boolean drainedLivingTarget) {
		if (!(user instanceof Player player) || player.level().isClientSide) {
			return;
		}
		BloodAbsorptionChannelRules.Strain strain = BloodAbsorptionChannelRules.nextStrain(
				STRAIN.getOrDefault(player.getUUID(), BloodAbsorptionChannelRules.Strain.NONE),
				player.level().getGameTime(), drainedLivingTarget);
		if (strain.ticks() <= 0) {
			STRAIN.remove(player.getUUID());
			return;
		}
		STRAIN.put(player.getUUID(), strain);
		applyStrainEffects(player, BloodAbsorptionChannelRules.strainTier(strain.ticks(),
				SkillPointHelper.getBloodToleranceLevel(player)));
	}

	/** The player's absorption strain as last worked out, for diagnostics and tests. */
	public static int strainTicks(Player player) {
		BloodAbsorptionChannelRules.Strain strain = STRAIN.get(player.getUUID());
		return strain == null ? 0 : strain.ticks();
	}

	/** Strain eases two per tick, so it is a session matter: a relog or restart forgets it. */
	public static void clearSessionState() {
		STRAIN.clear();
	}

	public static void logout(Player player) {
		STRAIN.remove(player.getUUID());
	}

	private static void applyStrainEffects(Player player, BloodAbsorptionChannelRules.StrainTier tier) {
		switch (tier) {
			case BLOOD_POISONING -> {
				player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, STRAIN_EFFECT_DURATION, 0, false, true, true));
				player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, STRAIN_EFFECT_DURATION, 0, false, true, true));
				player.addEffect(new MobEffectInstance(MobEffects.WITHER, STRAIN_EFFECT_DURATION / 2, 0, false, true, true));
			}
			case NAUSEA -> {
				player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, STRAIN_EFFECT_DURATION, 0, false, true, true));
				player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, STRAIN_EFFECT_DURATION, 0, false, true, true));
			}
			case WEAKNESS -> player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
					STRAIN_EFFECT_DURATION, 0, false, true, true));
			case NONE -> {
			}
		}
	}

	/**
	 * The bare hand's target: a creature carrying the drawer's strand brand comes first, from twice the reach; then
	 * the nearest valid creature, an Illuminated one from half again the reach (core-loop Track 12).
	 */
	public static Optional<LivingEntity> findBareAbsorptionTarget(LivingEntity user, double range) {
		return absorptionTargets(user, range).stream().findFirst();
	}

	/**
	 * Every creature {@code user} may draw now, in the order a draw takes them: the drawer's own branded creatures
	 * first, then by distance. Each is within its own reach ({@link AbsorptionRiderEffects#reachFor}).
	 */
	public static List<LivingEntity> absorptionTargets(LivingEntity user, double range) {
		Player drawer = user instanceof Player player ? player : null;
		double widest = drawer == null ? range : AbsorptionRiderEffects.widestReach(range);
		List<LivingEntity> candidates = user.level().getEntitiesOfClass(LivingEntity.class,
				user.getBoundingBox().inflate(widest), target -> isValidAbsorptionTarget(user, target)
						&& (drawer == null || passesSelectiveHunger(drawer, target))
						&& withinReach(user, drawer, target, range));
		return BloodAbsorptionTargetRules.orderTargets(candidates,
				target -> drawer != null && AbsorptionRiderEffects.brandedFor(target, drawer),
				user::distanceToSqr);
	}

	private static boolean withinReach(LivingEntity user, Player drawer, LivingEntity target, double range) {
		double reach = drawer == null ? range : AbsorptionRiderEffects.reachFor(target, drawer, range);
		// The same box test the plain gather always used, grown to the creature's own reach.
		return user.getBoundingBox().inflate(reach).intersects(target.getBoundingBox());
	}

	public static double absorbFromTarget(Level level, LivingEntity user, LivingEntity target, double amount) {
		return draw(level, user, target, amount, true);
	}

	/** A draw that gains no rider: the Disrupted arc's second draw, so an arc never arcs again. */
	static double drawWithoutRiders(Level level, LivingEntity user, LivingEntity target, double amount) {
		return draw(level, user, target, amount, false);
	}

	private static double draw(Level level, LivingEntity user, LivingEntity target, double amount, boolean riders) {
		if (!isValidAbsorptionTarget(user, target)) {
			return 0.0D;
		}
		if (level.isClientSide) {
			return 0.0D;
		}
		Player absorbingPlayer = user instanceof Player player ? player : null;
		// What the creature carries changes the draw (CL-26): one rider table keyed by school state.
		com.vincenthuto.hemomancy.common.damage.AbsorptionRiders.Rider rider = riders && absorbingPlayer != null
				? AbsorptionRiderEffects.riderFor(target) : com.vincenthuto.hemomancy.common.damage.AbsorptionRiders.NONE;
		if (absorbingPlayer != null) {
			amount *= rider.damage() * SkillPointHelper.getCrimsonMasteryMultiplier(absorbingPlayer);
		}
		PowerGuardrailState acclimation = null;
		String entityTypeId = null;
		if (absorbingPlayer != null && !(target instanceof VesperTheEveningStarEntity)) {
			acclimation = HemoCapabilityAccess.getPowerGuardrails(absorbingPlayer);
			entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
			amount *= HematicAcclimationRules.multiplier(
					acclimation.hematicExposure(entityTypeId, level.getGameTime()));
		}
		boolean mercy = absorbingPlayer != null && SkillPointHelper.isTechniqueEnabled(absorbingPlayer,
				SkillPointInit.skill_vascular_mercy);
		amount = ToggleableAbsorptionRules.clampDamageForMercy(mercy,
				target instanceof VesperTheEveningStarEntity, target.getHealth(), amount);
		if (amount <= 0.0D) return 0.0D;
		double healthBefore = target.getHealth();
		// The draw names the one drawing (CL-24): the creature turns on them and a kill is theirs, though a kill by
		// draw pays no kill bounty.
		boolean hurt = target.hurt(com.vincenthuto.hemomancy.common.damage.HemoDamageTypes.bloodAbsorption(level, user),
				(float) amount);
		double absorbed = Math.min(amount, Math.max(0.0D, healthBefore - target.getHealth()));
		if (!hurt || absorbed <= 0.0D) {
			return 0.0D;
		}
		if (acclimation != null) {
			acclimation.recordHematicExposure(entityTypeId, absorbed, level.getGameTime());
		}
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(user)
				.orElseThrow(NullPointerException::new);
		Bloodline globalLine = null;
		if (user instanceof ServerPlayer player && SkillPointHelper.isTechniqueEnabled(player,
				SkillPointInit.skill_shared_siphon)) {
			var line = volume.getBloodLine();
			if (line != null && line.isValid()) {
				globalLine = BloodlineSavedData.get(player.server.overworld()).getBloodline(line.getBloodlineUUID());
			}
		}
		boolean sharing = globalLine != null;
		double returned = absorbed * rider.blood();
		double personalBlood = ToggleableAbsorptionRules.personalBlood(sharing, returned);
		double sharedBlood = ToggleableAbsorptionRules.sharedBlood(sharing, returned);
		double room = Math.max(0.0D, volume.getMaxBloodVolume() - volume.getBloodVolume());
		double overflow = Math.max(0.0D, personalBlood - room);
		volume.fill(personalBlood);
		// A full body loses what it cannot hold; Sated Siphon sends it to a worn gourd instead (NQ-04-10).
		if (overflow > 0.0D && absorbingPlayer != null
				&& SkillPointHelper.isTechniqueEnabled(absorbingPlayer, SkillPointInit.skill_sated_siphon)) {
			overflow -= com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem.fillWornGourd(absorbingPlayer, overflow);
		}
		if (user instanceof ServerPlayer serverPlayer) {
			com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment.recordAbsorption(
					serverPlayer, Math.min(personalBlood, room));
			// Temper: what was drawn carries the creature's schools (core loop Track 4), shared or not. An unread kind
			// gives half of it (CL-12); the blood is unchanged.
			boolean read = BloodResearch.knows(serverPlayer, target.getType());
			com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge
					.fillFromCreature(serverPlayer, target, absorbed * BloodResearch.temperShare(read));
			if (!read && level.getGameTime() % 20L == 0L) {
				serverPlayer.displayClientMessage(Component.translatable("message.hemomancy.absorption.unread_weak")
						.withStyle(ChatFormatting.GRAY), true);
			}
		}
		if (overflow > 0.0D && user instanceof Player player
				&& SkillPointHelper.isTechniqueEnabled(player, SkillPointInit.skill_guarded_feeding)) {
			int amplifier = Math.min(3, Math.max(0, (int) (overflow / 4.0D)));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, amplifier, false, true, true));
		}
		if (globalLine != null) globalLine.contributeBlood((float) sharedBlood);
		if (user instanceof ServerPlayer serverPlayer) {
			BloodVolumeEvents.syncVolume(serverPlayer, volume);
			if (riders) AbsorptionRiderEffects.after(serverPlayer, target, rider, absorbed);
		}
		return absorbed;
	}

	public static boolean isValidAbsorptionTarget(LivingEntity user, Entity entity) {
		if (entity instanceof VesperTheEveningStarEntity vesper) {
			return vesper.canBeAbsorbedBy(user);
		}
		// a Saint's body and its companions answer only through the router, never the ordinary drain
		if (SaintChannelRouter.refusesOrdinaryDrain(entity)) return false;
		return entity instanceof LivingEntity target
				&& target != user
				&& target.isAlive()
				&& !target.isSpectator()
				&& !HemoEntityPredicates.NOBLOOD.test(target)
				&& !(target instanceof ArmorStand)
				&& !(target instanceof WillEntity)
				&& !isHemomancyNpc(target)
				&& !(target instanceof Player);
	}

	public static boolean passesSelectiveHunger(Player player, LivingEntity target) {
		if (!SkillPointHelper.isTechniqueEnabled(player, SkillPointInit.skill_selective_hunger)) return true;
		boolean protectedTarget = target instanceof net.minecraft.world.entity.animal.Animal
				|| target instanceof net.minecraft.world.entity.TamableAnimal || player.isAlliedTo(target);
		if (!protectedTarget) return true;
		var toTarget = target.getEyePosition().subtract(player.getEyePosition()).normalize();
		return player.getLookAngle().normalize().dot(toTarget) >= 0.985D;
	}

	private static boolean isHemomancyNpc(LivingEntity target) {
		Package targetPackage = target.getClass().getPackage();
		return targetPackage != null
				&& targetPackage.getName().startsWith("com.vincenthuto.hemomancy.common.entity.npc.");
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(playerIn)
				;
		IBloodTemperament temper = HemoCapabilityAccess.getBloodTemperament(playerIn)
				;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(playerIn)
				;
		if (BloodAbsorptionChannelRules.canStartChannel(volume.isActive())) {
			playerIn.startUsingItem(handIn);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		} else {
			playerIn.displayClientMessage(
					Component.translatable("message.hemomancy.blood_absorption_item.you_lack_the_skill_to_manifest").withStyle(ChatFormatting.RED),
					true);
		}

		return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
	}

}
