package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.util.ClientBridge;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodInteractions;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEntryKind;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperBloodAbsorptionInteractions;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillAbsorptionRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillBloodUtilityInteractions;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.IEquippedMorphling;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbReach;
import com.vincenthuto.hemomancy.common.manipulation.ferric.CellHandRules;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivation;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteCancellationHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.List;

public class LivingStaffItem extends LivingItem implements IDispellable {
	private static final String UTILITY_SESSION_BLOOD_HANDLED_KEY = "HemomancyLivingStaffUtilityBloodHandled";
	/** The part of a press's blood that grows rings: spent or drawn from creatures, never cycled through a tank (CQ-8). */
	private static final String UTILITY_SESSION_RING_BLOOD_KEY = "HemomancyLivingStaffUtilityRingBlood";
	/** A press of a second or less that moved no blood is a tap: it calls the equipped Primal Morphling. */
	static final int MORPHLING_TAP_TICKS = 20;

	public LivingStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		Level world = context.level();
		if (world != null && world.isClientSide) {
			Player player = ClientBridge.get().localPlayer();
			if (player != null) {
				var progress = HemoCapabilityAccess.getLivingStaffProgress(player);
				// The Staff's age (core-loop Track 8): rings laid, and how far the next one is.
				int rings = LivingStaffGrowthRules.ring(progress.getBloodHandled());
				tooltip.add(Component.translatable("item.hemomancy.living_staff.rings", rings,
						LivingStaffGrowthRules.MAX_RING).withStyle(ChatFormatting.GRAY));
				if (rings < LivingStaffGrowthRules.MAX_RING) {
					tooltip.add(Component.translatable("item.hemomancy.living_staff.next_ring",
							(int) Math.ceil(LivingStaffGrowthRules.untilNextRing(progress.getBloodHandled())))
							.withStyle(ChatFormatting.DARK_GRAY));
				}
				if (progress.isVesperMemoryAwakened()) {
					tooltip.add(Component.translatable("item.hemomancy.living_staff.vesper_awakened.tooltip")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
				}
				var cap = HemoCapabilityAccess.getEquippedMorphling(player);
				ItemStack equipped = cap.getEquippedMorphling();
				if (!equipped.isEmpty()) {
					tooltip.add(equipped.getHoverName());
				}
			}
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
		super.inventoryTick(stack, level, holder, slot, selected);
		// The Staff shows its owner's growth rings; the stamp is what every side draws and mines by.
		if (!level.isClientSide && holder instanceof Player player && level.getGameTime() % 20L == slot % 20L) {
			LivingStaffGrowth.refreshStamp(player, stack);
		}
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
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
		super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
		if (pLivingEntity instanceof Player tracer
				&& com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing.isTracing(tracer)) {
			// Threading an engram: the held staff sweeps its lane and opens no blood channel.
			com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing.tick(tracer);
			return;
		}
		if (!(pLivingEntity instanceof Player player)) {
			return;
		}
		// The staff channels the player's active verb (CL-2), whatever is selected.
		Verb verb = VerbChannel.activeVerb(player);
		VerbChannel.applyMovement(player);
		if (pLevel.isClientSide) {
			spawnStaffUtilityParticles(player, pStack);
			return;
		}
		if (pRemainingUseDuration % CellHandRules.USE_REFRESH == 0) {
			// The staff channels the cell hands' verbs, so it draws the same streams from its head.
			ManipulationVisuals.attached(player, ManipulationVisuals.Form.CELL_HAND, 1, CellHandRules.USE_TICKS,
					CellHandRules.useMode(verb == Verb.ABSORB));
		}
		if (player instanceof ServerPlayer serverPlayer) {
			if (verb == Verb.ABSORB) {
				ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
				LivingStaffFocusProfile focus = LivingStaffFocusProfile.fromPlayer(player, progress);
				double reach = VerbReach.of(player, Verb.ABSORB);
				double vesperHandled = VesperBloodAbsorptionInteractions.tryAbsorb(pLevel, player, reach,
						WillAbsorptionRules.staffProgressPerTick(focus));
				// The Ringmaster at his kneel takes the same channel (2026-11-18).
				if (vesperHandled <= 0.0D) vesperHandled = com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterAbsorption
						.tryAbsorb(pLevel, player, reach, WillAbsorptionRules.staffProgressPerTick(focus));
				if (vesperHandled > 0.0D) {
					BloodAbsorptionItem.updateChannelStrain(player, false);
					return;
				}
				if (SaintChannelRouter.tryAbsorb(pLevel, player, reach,
						LivingStaffFocusRules.blockAbsorptionPerTick(focus), true).handled()) {
					BloodAbsorptionItem.updateChannelStrain(player, false);
					return;
				}
				if (CardinalRiteCancellationHandler.tryChannel(serverPlayer, reach)) {
					BloodAbsorptionItem.updateChannelStrain(player, false);
					return;
				}
				if (tryAbsorbFromLookedAtBlockWithStaff(pLevel, player, focus, reach)) {
					BloodAbsorptionItem.updateChannelStrain(player, false);
					return;
				}
				double willHandled = WillBloodUtilityInteractions.tryAbsorbFalteringWill(pLevel, player, reach,
						WillAbsorptionRules.staffProgressPerTick(focus));
				if (willHandled > 0.0D) {
					BloodAbsorptionItem.updateChannelStrain(player, false);
					return;
				}
				SculptingDispatcher.Result sculpted = SculptingDispatcher.trySculpt(serverPlayer, Verb.ABSORB);
				if (sculpted.claimed()) {
					recordUtilityBloodHandled(player, sculpted.bloodPaid(), sculpted.bloodPaid());
					BloodAbsorptionItem.updateChannelStrain(player, false);
					return;
				}
				// A stain, gouge, vein or Lux trace on the aimed face is taken back up (core-loop Track 6).
				if (com.vincenthuto.hemomancy.common.world.ground.GroundNetwork.tryAbsorb(serverPlayer, reach)) {
					BloodAbsorptionItem.updateChannelStrain(player, false);
					return;
				}
				int elapsed = getUseDuration(pStack, pLivingEntity) - pRemainingUseDuration;
				int interval = BloodAbsorptionChannelRules.livingTargetPulseInterval();
				if (elapsed % interval == 0) {
					double handled = absorbWithStaff(pLevel, player, pStack);
					BloodAbsorptionItem.updateChannelStrain(player, handled > 0.0D);
				} else {
					BloodAbsorptionItem.updateChannelStrain(player, false);
				}
			} else {
				projectWithStaff(pLevel, player, pStack);
			}
		}
	}

	@Override
	public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
		super.onStopUsing(stack, entity, count);
		if (entity instanceof Player player)
			com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing.end(player);
		if (entity instanceof ServerPlayer player) SculptingSessions.end(player, true);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof Player tracer
				&& com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing.end(tracer)) {
			tracer.awardStat(Stats.ITEM_USED.get(this));
			return;
		}
		if (entityLiving instanceof Player player) {
			boolean handledUtilityBlood = !worldIn.isClientSide && hasUtilityBloodHandled(player);
			if (handledUtilityBlood) {
				commitUtilityBloodHandled(player);
				player.awardStat(Stats.ITEM_USED.get(this));
				return;
			}
			// The staff always channels a verb now; a tap that moved no blood calls the Primal Morphling instead.
			int held = getUseDuration(stack, player) - timeLeft;
			if (player instanceof ServerPlayer serverPlayer && !player.isCrouching() && held <= MORPHLING_TAP_TICKS) {
				usePrimalMorphling(serverPlayer, HemoCapabilityAccess.getEquippedMorphling(player), stack, worldIn);
			}
		}

		if (entityLiving instanceof Player player) {
			player.awardStat(Stats.ITEM_USED.get(this));
		}

	}

	private void usePrimalMorphling(ServerPlayer player, IEquippedMorphling cap, ItemStack stack, Level worldIn) {
		ItemStack creature = cap.getEquippedMorphling();
		if (!(creature.getItem() instanceof IMorphling morphling)) return;
		if (!com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem.isPrimal(creature)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.living_staff_item.your_morphling_has_not_reached_its"), true);
			return;
		}
		if (!com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents
				.isStoredInCarriedJar(player, creature)) return;
		if (morphling.tryUse(player, player.getUsedItemHand(), creature, worldIn)) {
			stack.hurtAndBreak(1, player, player.getUsedItemHand() == InteractionHand.MAIN_HAND
					? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
			worldIn.playSound(null, player.blockPosition(), SoundEvents.HOGLIN_CONVERTED_TO_ZOMBIFIED,
					net.minecraft.sounds.SoundSource.PLAYERS, 0.2F, 0.8F + worldIn.random.nextFloat() * 0.2F);
			com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents
					.syncToClient(player);
		}
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			return isClientPlayerVesperAwakened() || super.isFoil(stack);
		}
		return super.isFoil(stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		// On the client, clicking a Cardinal Focus with no rite of your own running is a planting gesture:
		// consume it so the same click does not also start a projection channel.
		if (context.getLevel().isClientSide && context.getPlayer() != null
				&& context.getLevel().getBlockState(context.getClickedPos())
						.is(com.vincenthuto.hemomancy.common.init.BlockInit.cardinal_focus.get())
				&& ClientBridge.get().activeRites().stream()
						.noneMatch(rite -> context.getPlayer().getUUID().equals(rite.getOwner()))) {
			return InteractionResult.SUCCESS;
		}
		if (!context.getLevel().isClientSide
				&& context.getPlayer() instanceof ServerPlayer serverPlayer
				&& CardinalRiteActivation.tryStart(
						serverPlayer, context.getClickedPos(),
						CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE).handled()) {
			return InteractionResult.SUCCESS;
		}
		return super.useOn(context);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (playerIn.isShiftKeyDown()) {
			// A sneaking press on the weaver's own engram cuts a lane back; anywhere else the staff stays idle.
			if (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing.begin(playerIn, true)) {
				playerIn.swing(handIn);
				return InteractionResultHolder.sidedSuccess(stack, worldIn.isClientSide);
			}
			return InteractionResultHolder.pass(stack);
		}
		if (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing.begin(playerIn, false)) {
			playerIn.startUsingItem(handIn);
			return InteractionResultHolder.consume(stack);
		}
		if (!worldIn.isClientSide && isPlantingStaff(playerIn)) return InteractionResultHolder.fail(stack);
		playerIn.startUsingItem(handIn);
		if (!worldIn.isClientSide) clearUtilityBloodHandled(playerIn);
		return InteractionResultHolder.consume(stack);
	}

	/** While a staff is being driven into a Focus, no other staff in hand may open a channel. */
	private static boolean isPlantingStaff(Player player) {
		if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return false;
		var rite = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level).getRite(player.getUUID());
		return rite != null && rite.isStaffPlanting();
	}

	private static boolean tryAbsorbFromLookedAtBlockWithStaff(Level level, Player player,
			LivingStaffFocusProfile focus, double reach) {
		double blockHandled = BlockBloodInteractions.tryAbsorbFromLookedAtBlock(level, player, reach,
				LivingStaffFocusRules.blockAbsorptionPerTick(focus));
		if (blockHandled <= 0.0D) {
			return false;
		}
		recordUtilityBloodHandled(player, blockHandled, 0.0D);
		return true;
	}

	private static double absorbWithStaff(Level level, Player player, ItemStack stack) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				;
		if (!BloodAbsorptionChannelRules.canDrainLivingTarget(volume.isActive())) {
			return 0.0D;
		}
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
		LivingStaffFocusProfile focus = LivingStaffFocusProfile.fromPlayer(player, progress);
		int targetCap = LivingStaffFocusRules.absorptionTargetCap(true, focus);
		boolean distributed = SkillPointHelper.isTechniqueEnabled(player, SkillPointInit.skill_distributed_siphon);
		int availableLimit = distributed ? targetCap : 1;
		// The same order as the bare hand: the player's own branded creatures first, then the nearest (core-loop Track 12).
		List<LivingEntity> targets = BloodAbsorptionItem.absorptionTargets(player, VerbReach.of(player, Verb.ABSORB))
				.stream()
				.limit(availableLimit)
				.toList();
		double amountPerTarget = ToggleableAbsorptionRules.damagePerTarget(distributed,
				LivingStaffFocusRules.absorptionDamagePerTarget(focus)
						* BloodAbsorptionChannelRules.cadenceDrainMultiplier(
								SkillPointHelper.getAbsorptionCadenceLevel(player)), targets.size());
		double handled = 0.0D;
		for (LivingEntity target : targets) {
			handled += BloodAbsorptionItem.absorbFromTarget(level, player, target, amountPerTarget);
		}
		recordUtilityBloodHandled(player, handled, handled);
		return handled;
	}

	private static void projectWithStaff(Level level, Player player, ItemStack stack) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				;
		if (!volume.isActive() || volume.isEmpty()) {
			return;
		}
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
		LivingStaffFocusProfile focus = LivingStaffFocusProfile.fromPlayer(player, progress);
		double[] intoTanks = {0.0D};
		double handled = BloodProjectionItem.projectFromEntity(level, player,
				LivingStaffFocusRules.structureProjectionRate(true, focus),
				LivingStaffFocusRules.bloodTileProjectionRate(true, focus), true, moved -> intoTanks[0] += moved);
		recordUtilityBloodHandled(player, handled, handled - intoTanks[0]);
	}

	/**
	 * Records a press's blood: all of it {@code handled} counts as blood moved (a press that moved any is no tap),
	 * only {@code ringBlood} ages the Staff.
	 */
	private static void recordUtilityBloodHandled(Player player, double handled, double ringBlood) {
		if (handled <= 0.0D) {
			return;
		}
		CompoundTag data = player.getPersistentData();
		data.putDouble(UTILITY_SESSION_BLOOD_HANDLED_KEY,
				data.getDouble(UTILITY_SESSION_BLOOD_HANDLED_KEY) + handled);
		if (ringBlood > 0.0D) {
			data.putDouble(UTILITY_SESSION_RING_BLOOD_KEY, data.getDouble(UTILITY_SESSION_RING_BLOOD_KEY) + ringBlood);
		}
	}

	private static boolean hasUtilityBloodHandled(Player player) {
		return player.getPersistentData().getDouble(UTILITY_SESSION_BLOOD_HANDLED_KEY) > 0.0D;
	}

	private static void commitUtilityBloodHandled(Player player) {
		CompoundTag data = player.getPersistentData();
		double ringBlood = data.getDouble(UTILITY_SESSION_RING_BLOOD_KEY);
		if (ringBlood > 0.0D && player instanceof ServerPlayer serverPlayer) {
			// The blood spent or drawn ages the Staff; a ring that closes is announced (core-loop Track 8).
			LivingStaffGrowth.record(serverPlayer, ringBlood);
		}
		data.remove(UTILITY_SESSION_BLOOD_HANDLED_KEY);
		data.remove(UTILITY_SESSION_RING_BLOOD_KEY);
	}

	private static void clearUtilityBloodHandled(Player player) {
		CompoundTag data = player.getPersistentData();
		data.remove(UTILITY_SESSION_BLOOD_HANDLED_KEY);
		data.remove(UTILITY_SESSION_RING_BLOOD_KEY);
	}

	/** Whether {@code living} is channelling a verb through the Living Staff {@code stack}. */
	public static boolean isLivingStaffUtilityUse(LivingEntity living, ItemStack stack) {
		return isLivingStaffVerbUse(living, stack, Verb.ABSORB) || isLivingStaffVerbUse(living, stack, Verb.PROJECT);
	}

	public static boolean isLivingStaffAbsorptionUse(LivingEntity living, ItemStack stack) {
		return isLivingStaffVerbUse(living, stack, Verb.ABSORB);
	}

	public static boolean isLivingStaffProjectionUse(LivingEntity living, ItemStack stack) {
		return isLivingStaffVerbUse(living, stack, Verb.PROJECT);
	}

	private static boolean isLivingStaffVerbUse(LivingEntity living, ItemStack stack, Verb verb) {
		return stack.getItem() instanceof LivingStaffItem
				&& living.isUsingItem()
				&& VerbChannel.verbOf(living, stack).map(channelled -> channelled == verb).orElse(false);
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean isClientPlayerVesperAwakened() {
		Player player = ClientBridge.get().localPlayer();
		return player != null && HemoCapabilityAccess.getLivingStaffProgress(player)
				.isVesperMemoryAwakened();
	}

	@OnlyIn(Dist.CLIENT)
	private static void spawnStaffUtilityParticles(Player player, ItemStack stack) {
		HumanoidArm activeArm = player.getUsedItemHand() == InteractionHand.MAIN_HAND
				? player.getMainArm()
				: player.getMainArm().getOpposite();
		ClientBridge.get().spawnFirstPersonCellHandParticles(stack, activeArm);
	}

}
