package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.entity.menagerie.AnimalTaint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbReach;
import com.vincenthuto.hemomancy.common.manipulation.ferric.CellHandRules;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodInteractions;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.IBloodTemperament;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillBloodUtilityInteractions;
import com.vincenthuto.hemomancy.common.event.BloodInfusionManager;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteFootprintResolver;
import com.vincenthuto.hemomancy.common.event.BloodStructureFeedManager;
import com.vincenthuto.hemomancy.common.event.SanguineCondenserProjection;
import com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteProjectionResult;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ProjectedBloodSink;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.DoubleConsumer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.vincenthuto.hemomancy.common.world.chum.ChumPools;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaSummoning;
import net.minecraft.world.phys.HitResult.Type;

public class BloodProjectionItem extends Item implements IDispellable, ICellHand {

	public BloodProjectionItem(Properties prop) {
		super(prop.stacksTo(1));
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
	public void onUseTick(Level worldIn, LivingEntity player, ItemStack stack, int count) {
		if (player instanceof Player channelingPlayer) {
			VerbChannel.applyMovement(channelingPlayer);
		}
		if (!worldIn.isClientSide && count % CellHandRules.USE_REFRESH == 0)
			ManipulationVisuals.attached(player, ManipulationVisuals.Form.CELL_HAND, 1, CellHandRules.USE_TICKS, CellHandRules.PROJECTING);
		projectFromEntity(worldIn, player,
				LivingStaffFocusRules.structureProjectionRate(false, LivingStaffFocusProfile.NONE),
				LivingStaffFocusRules.bloodTileProjectionRate(false, LivingStaffFocusProfile.NONE), false);
	}

	@Override
	public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
		super.onStopUsing(stack, entity, count);
		if (entity instanceof ServerPlayer player) SculptingSessions.end(player, true);
	}

	public static double projectFromEntity(Level worldIn, LivingEntity player, double structureFeedRate,
			double tileTransferRate) {
		return projectFromEntity(worldIn, player, structureFeedRate, tileTransferRate, false);
	}

	public static double projectFromEntity(Level worldIn, LivingEntity player, double structureFeedRate,
			double tileTransferRate, boolean livingStaff) {
		return projectFromEntity(worldIn, player, structureFeedRate, tileTransferRate, livingStaff, null);
	}

	/** As above; {@code tankTransfer} hears the part of the blood that went into a tank rather than being spent. */
	public static double projectFromEntity(Level worldIn, LivingEntity player, double structureFeedRate,
			double tileTransferRate, boolean livingStaff, @Nullable DoubleConsumer tankTransfer) {
		return BlockBloodInteractions.trackPoured(worldIn, player, VerbReach.of(player, Verb.PROJECT),
				() -> performProjection(worldIn, player, structureFeedRate, tileTransferRate, livingStaff, tankTransfer));
	}

	/**
	 * Whether a sink keeps projected blood to be drawn back, as a tank does (CQ-8). A sink that spends it (the Ring
	 * Door, the Monolith gate, the Mason's Effigy, a reservoir with no tank of its own) is not a tank.
	 */
	public static boolean holdsProjectedBlood(ProjectedBloodSink sink) {
		return sink instanceof IBloodReservoir reservoir && reservoir.holdsBlood();
	}

    private static double performProjection(Level worldIn, LivingEntity player, double structureFeedRate,
            double tileTransferRate, boolean livingStaff, @Nullable DoubleConsumer tankTransfer) {
		if (worldIn.isClientSide) {
			return 0.0D;
		}
		IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(NullPointerException::new);
		double beforeBlood = playerVolume.getBloodVolume();
        if (player instanceof ServerPlayer founder
                && com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.tryProject(founder)) {
            return 0.0D;
        }
		double reach = VerbReach.of(player, Verb.PROJECT);
		SaintChannelRouter.Result saint = SaintChannelRouter.tryProject(worldIn, player, reach, tileTransferRate,
				livingStaff);
		if (saint.handled()) {
			return saint.blood();
		}

		double willHandled = WillBloodUtilityInteractions.tryProjectBanishFalteringWill(worldIn, player,
				tileTransferRate);
		if (willHandled > 0.0D) {
			return willHandled;
		}

		CardinalRiteProjectionResult riteResult =
				CardinalRiteInteractionHandler.tryProject(worldIn, player, structureFeedRate);
		if (!riteResult.allowsOrdinaryProjection()) {
			return riteResult.bloodSpent();
		}

		// A selected Sculpting that claims the aimed-at thing acts ahead of the block endpoints and machines (CL-4).
		if (player instanceof ServerPlayer sculptor) {
			SculptingDispatcher.Result sculpted = SculptingDispatcher.trySculpt(sculptor, Verb.PROJECT);
			if (sculpted.claimed()) {
				return sculpted.bloodPaid();
			}
		}

		// A passive animal in front of the stream takes a dose: fertility, at the cost of taint (core-loop Track 5).
		if (player instanceof ServerPlayer keeper) {
			java.util.OptionalDouble dosed = AnimalTaint.tryProject(keeper, reach);
			if (dosed.isPresent()) {
				return dosed.getAsDouble();
			}
		}

		double blockHandled = BlockBloodInteractions.tryProjectIntoLookedAtBlock(worldIn, player, tileTransferRate,
				tankTransfer);
		if (blockHandled > 0.0D) {
			return blockHandled;
		}

		// The ground network's marks: an ash trail lights, a vein node grows, a gouge run pulses (core-loop Track 6).
		if (player instanceof ServerPlayer groundPlayer) {
			java.util.OptionalDouble ground = GroundNetwork.tryProject(groundPlayer, reach);
			if (ground.isPresent()) {
				return ground.getAsDouble();
			}
		}

		HitResult trace = SanguineProjectionTargeting.pick(worldIn, player, reach, true);
		if (trace.getType() == Type.BLOCK) {
			BlockPos targetPos = ((BlockHitResult) trace).getBlockPos();
			// Blood poured into basin ichor calls the Phlegethosoma (CR-7).
			if (player instanceof ServerPlayer caller) {
				java.util.OptionalDouble called = PhlegethosomaSummoning.tryProject(caller, (BlockHitResult) trace);
				if (called.isPresent()) {
					return called.getAsDouble();
				}
			}
			// Blood poured at a water surface makes chum (core-loop Track 7).
			if (player instanceof ServerPlayer chummer) {
				java.util.OptionalDouble chum = ChumPools.tryProject(chummer, (BlockHitResult) trace);
				if (chum.isPresent()) {
					return chum.getAsDouble();
				}
			}
			if (player instanceof ServerPlayer serverPlayer
					&& worldIn instanceof ServerLevel serverLevel
					&& BloodStructureFeedManager.feedStructure(serverPlayer, serverLevel, targetPos,
							player.getOffhandItem(), structureFeedRate)) {
				return Math.max(0.0D, beforeBlood - playerVolume.getBloodVolume());
			}
			if (player instanceof ServerPlayer serverPlayer && worldIn instanceof ServerLevel serverLevel) {
				java.util.OptionalDouble condensed = SanguineCondenserProjection.tryProject(serverLevel, serverPlayer,
						targetPos, tileTransferRate);
				if (condensed.isPresent()) {
					return condensed.getAsDouble();
				}
			}
			// A running rite claims every player's Projection over its ground: blood that misses an anchor or the
			// daemon neither infuses nor stains the floor (an infusion lifts the block and breaks the rite).
			boolean riteGround = player instanceof ServerPlayer caster
					&& CardinalRiteFootprintResolver.onRiteGround(caster.serverLevel(), targetPos);
			if (!riteGround && player instanceof ServerPlayer serverPlayer
					&& worldIn instanceof ServerLevel serverLevel
					&& BloodInfusionManager.feedBlock(serverPlayer, serverLevel, targetPos,
							trace.getLocation(), structureFeedRate)) {
				return Math.max(0.0D, beforeBlood - playerVolume.getBloodVolume());
			}

			if (worldIn.getBlockEntity(targetPos) instanceof ProjectedBloodSink sink
					&& player instanceof Player projectingPlayer) {
				double taken = sink.takeProjectedBlood(projectingPlayer, targetPos, tileTransferRate, livingStaff);
				if (taken > 0.0D && tankTransfer != null && holdsProjectedBlood(sink)) tankTransfer.accept(taken);
				return taken;
			}
			// The last link: blood that nothing else took lands on the struck face as a stain.
			if (!riteGround && player instanceof ServerPlayer stainer) {
				return GroundNetwork.stain(stainer, trace);
			}
		}
		return Math.max(0.0D, beforeBlood - playerVolume.getBloodVolume());
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
		if (volume.isActive()) {
			if (volume.getBloodVolume() > 0) {
				playerIn.startUsingItem(handIn);
				return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
			}
			playerIn.displayClientMessage(
					Component.translatable("message.hemomancy.blood_projection_item.no_blood_answers_the_projection").withStyle(ChatFormatting.DARK_RED),
					true);
		} else {
			playerIn.displayClientMessage(
					Component.translatable("message.hemomancy.blood_projection_item.you_lack_the_skill_to_manifest").withStyle(ChatFormatting.RED),
					true);
		}

		return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
	}

}
