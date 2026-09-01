package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.event.ClientEvents.ClientModBusEvents;
import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.harbinger.CellHandItemRenderer;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodInteractions;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperBloodAbsorptionInteractions;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillAbsorptionRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillBloodUtilityInteractions;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteCancellationHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.Optional;

public class BloodAbsorptionItem extends Item implements IDispellable, ICellHand, HemoClientItemExtensionsProvider {
	private static final String ABSORPTION_STRAIN_TICKS_KEY = "HemomancyBloodAbsorptionStrainTicks";
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
	public BakedModel getBakedModel() {
		return ClientModBusEvents.bloodAbsorptionModel;
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
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			final BlockEntityWithoutLevelRenderer myRenderer = new CellHandItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return myRenderer;
			}
		};
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
			applyChannelMovementPenalty(channelingPlayer);
		}
		if (worldIn.isClientSide) {
			return;
		}
		double vesperHandled = VesperBloodAbsorptionInteractions.tryAbsorb(worldIn, player,
				LivingStaffFocusRules.bareAbsorptionRange(), WillAbsorptionRules.bareProgressPerTick());
		if (vesperHandled > 0.0D) {
			updateChannelStrain(player, false);
			return;
		}
		if (player instanceof ServerPlayer serverPlayer
				&& CardinalRiteCancellationHandler.tryChannel(
						serverPlayer, LivingStaffFocusRules.bareAbsorptionRange())) {
			updateChannelStrain(player, false);
			return;
		}
		double blockHandled = BlockBloodInteractions.tryAbsorbFromLookedAtBlock(worldIn, player,
				LivingStaffFocusRules.bareAbsorptionRange(),
				LivingStaffFocusRules.bareBlockAbsorptionPerTick());
		if (blockHandled > 0.0D) {
			updateChannelStrain(player, false);
			return;
		}
		double willHandled = WillBloodUtilityInteractions.tryAbsorbFalteringWill(worldIn, player,
				LivingStaffFocusRules.bareAbsorptionRange());
		if (willHandled > 0.0D) {
			updateChannelStrain(player, false);
			return;
		}
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !BloodAbsorptionChannelRules.canDrainLivingTarget(
				volume.isActive(), volume.isFull(), player instanceof Player channelingPlayer
						&& SkillPointHelper.isTechniqueEnabled(channelingPlayer, SkillPointInit.skill_sated_siphon))) {
			updateChannelStrain(player, false);
			return;
		}
		int elapsed = getUseDuration(stack, player) - count;
		int interval = BloodAbsorptionChannelRules.livingTargetPulseInterval(false,
				player instanceof Player channelingPlayer
						? SkillPointHelper.getAbsorptionCadenceLevel(channelingPlayer)
						: 0);
		if (elapsed % interval != 0) {
			updateChannelStrain(player, false);
			return;
		}
		boolean drainedLivingTarget = findBareAbsorptionTarget(player, LivingStaffFocusRules.bareAbsorptionRange())
				.map(target -> absorbFromTarget(worldIn, player, target,
						LivingStaffFocusRules.bareAbsorptionDamagePerTick()) > 0.0D)
				.orElse(false);
		updateChannelStrain(player, drainedLivingTarget);
	}

	public static void applyChannelMovementPenalty(Player player) {
		double multiplier = getChannelMovementMultiplier(player);
		if (multiplier >= 1.0D) {
			return;
		}
		Vec3 movement = player.getDeltaMovement();
		player.setDeltaMovement(movement.x * multiplier, movement.y, movement.z * multiplier);
		if (multiplier <= 0.0D) {
			player.xxa = 0.0F;
			player.zza = 0.0F;
		}
	}

	public static double getChannelMovementMultiplier(Player player) {
		return BloodAbsorptionChannelRules.movementMultiplier(
				SkillPointHelper.getDraggingSiphonLevel(player),
				SkillPointHelper.getMobileConduitLevel(player),
				SkillPointHelper.hasUnboundSiphon(player));
	}

	public static double getClientChannelMovementInputMultiplier(Player player) {
		return BloodAbsorptionChannelRules.clientInputMultiplier(getChannelMovementMultiplier(player));
	}

	public static boolean isChannelingBloodAbsorption(Player player) {
		if (player == null || !player.isUsingItem()) {
			return false;
		}
		return player.getUseItem().getItem() instanceof BloodAbsorptionItem
				|| LivingStaffItem.isLivingStaffAbsorptionUse(player, player.getUseItem());
	}

	public static void updateChannelStrain(LivingEntity user, boolean drainedLivingTarget) {
		if (!(user instanceof Player player) || player.level().isClientSide) {
			return;
		}
		CompoundTag data = player.getPersistentData();
		int strainTicks = BloodAbsorptionChannelRules.nextStrainTicks(
				data.getInt(ABSORPTION_STRAIN_TICKS_KEY), drainedLivingTarget);
		if (strainTicks <= 0) {
			data.remove(ABSORPTION_STRAIN_TICKS_KEY);
			return;
		}
		data.putInt(ABSORPTION_STRAIN_TICKS_KEY, strainTicks);
		applyStrainEffects(player, BloodAbsorptionChannelRules.strainTier(strainTicks,
				SkillPointHelper.getBloodToleranceLevel(player)));
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

	public static Optional<LivingEntity> findBareAbsorptionTarget(LivingEntity user, double range) {
		List<LivingEntity> candidates = user.level().getEntitiesOfClass(LivingEntity.class,
				user.getBoundingBox().inflate(range), target -> isValidAbsorptionTarget(user, target)
						&& (!(user instanceof Player player) || passesSelectiveHunger(player, target)));
		return BloodAbsorptionTargetRules.chooseBareTarget(candidates,
				target -> isValidAbsorptionTarget(user, target)
						&& (!(user instanceof Player player) || passesSelectiveHunger(player, target)),
				target -> false,
				user::distanceToSqr);
	}

	public static double absorbFromTarget(Level level, LivingEntity user, LivingEntity target, double amount) {
		if (!isValidAbsorptionTarget(user, target)) {
			return 0.0D;
		}
		if (level.isClientSide) {
			return 0.0D;
		}
		Player absorbingPlayer = user instanceof Player player ? player : null;
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
		boolean hurt = target.hurt(user.damageSources().generic(), (float) amount);
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
		double personalBlood = ToggleableAbsorptionRules.personalBlood(sharing, absorbed);
		double sharedBlood = ToggleableAbsorptionRules.sharedBlood(sharing, absorbed);
		double room = Math.max(0.0D, volume.getMaxBloodVolume() - volume.getBloodVolume());
		double overflow = Math.max(0.0D, personalBlood - room);
		volume.fill(personalBlood);
		if (overflow > 0.0D && user instanceof Player player
				&& SkillPointHelper.isTechniqueEnabled(player, SkillPointInit.skill_guarded_feeding)) {
			int amplifier = Math.min(3, Math.max(0, (int) (overflow / 4.0D)));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, amplifier, false, true, true));
		}
		if (globalLine != null) globalLine.contributeBlood((float) sharedBlood);
		if (user instanceof ServerPlayer serverPlayer) {
			PacketHandler.sendToPlayer(serverPlayer, new BloodVolumeServerPacket(volume));
		}
		return absorbed;
	}

	public static boolean isValidAbsorptionTarget(LivingEntity user, Entity entity) {
		if (entity instanceof VesperTheEveningStarEntity vesper) {
			return vesper.canBeAbsorbedBy(user);
		}
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
				.orElseThrow(NullPointerException::new);
		IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(playerIn)
				.orElseThrow(NullPointerException::new);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(playerIn)
				.orElseThrow(NullPointerException::new);
		if (BloodAbsorptionChannelRules.canStartChannel(volume.isActive())) {
			playerIn.startUsingItem(handIn);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		} else {
			playerIn.displayClientMessage(
					Component.literal("You lack the skill to manifest this power!").withStyle(ChatFormatting.RED),
					true);
		}

		return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
	}

}
