package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.event.ClientEvents.ClientModBusEvents;
import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.hematic.CellHandItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.event.BloodStructureFeedManager;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;
import com.vincenthuto.hemomancy.common.tile.crafting.SomaticLoomBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class BloodProjectionItem extends Item implements IDispellable, ICellHand, HemoClientItemExtensionsProvider {

	public BloodProjectionItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public BakedModel getBakedModel() {
		return ClientModBusEvents.bloodProjectionModel;
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
	public void onUseTick(Level worldIn, LivingEntity player, ItemStack stack, int count) {
		projectFromEntity(worldIn, player,
				LivingStaffFocusRules.structureProjectionRate(false, LivingStaffFocusProfile.NONE),
				LivingStaffFocusRules.bloodTileProjectionRate(false, LivingStaffFocusProfile.NONE), false);
	}

	public static double projectFromEntity(Level worldIn, LivingEntity player, double structureFeedRate,
			double tileTransferRate) {
		return projectFromEntity(worldIn, player, structureFeedRate, tileTransferRate, false);
	}

	public static double projectFromEntity(Level worldIn, LivingEntity player, double structureFeedRate,
			double tileTransferRate, boolean livingStaff) {
		if (worldIn.isClientSide) {
			return 0.0D;
		}
		IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(NullPointerException::new);
		double beforeBlood = playerVolume.getBloodVolume();

		HitResult trace = player.pick(5.5,0, true);
		if (trace.getType() == Type.BLOCK) {
			BlockPos targetPos = ((BlockHitResult) trace).getBlockPos();
			if (player instanceof ServerPlayer serverPlayer
					&& worldIn instanceof ServerLevel serverLevel
					&& BloodStructureFeedManager.feedStructure(serverPlayer, serverLevel, targetPos,
							player.getOffhandItem(), structureFeedRate)) {
				return Math.max(0.0D, beforeBlood - playerVolume.getBloodVolume());
			}

			BlockEntity be = worldIn.getBlockEntity(targetPos);
			if (be != null) {
				if (be instanceof SomaticLoomBlockEntity loom && player instanceof Player projectingPlayer) {
					if (loom.tryChargeRitualBlood(projectingPlayer, tileTransferRate, livingStaff)) {
						if (projectingPlayer instanceof ServerPlayer serverPlayer) {
							PacketHandler.sendToPlayer(serverPlayer, new BloodVolumeServerPacket(playerVolume));
						}
						return Math.max(0.0D, beforeBlood - playerVolume.getBloodVolume());
					}
					return 0.0D;
				}
				if (HemoCapabilityAccess.getBloodVolume(be).isPresent()) {

					IBloodVolume tileVolume = HemoCapabilityAccess.getBloodVolume(be)
							.orElseThrow(IllegalStateException::new);
					if(!tileVolume.isFull()) {
						tileVolume.fillFromSource(playerVolume, tileTransferRate);
						if (be instanceof IBloodTile bt) {
							bt.sendUpdates();
						}
					}
					
				}
			}

		}
		if (player instanceof ServerPlayer serverPlayer) {
			PacketHandler.sendToPlayer(serverPlayer, new BloodVolumeServerPacket(playerVolume));
		}
		return Math.max(0.0D, beforeBlood - playerVolume.getBloodVolume());
	}

	@SuppressWarnings("unused")
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(playerIn)
				.orElseThrow(NullPointerException::new);
		IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(playerIn)
				.orElseThrow(NullPointerException::new);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(playerIn)
				.orElseThrow(NullPointerException::new);
		if (volume.isActive()) {
			if (volume.getBloodVolume() > 0) {
				playerIn.startUsingItem(handIn);
				return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
			}
			playerIn.displayClientMessage(
					Component.literal("No blood answers the projection.").withStyle(ChatFormatting.DARK_RED),
					true);
		} else {
			playerIn.displayClientMessage(
					Component.literal("You lack the skill to manifest this power!").withStyle(ChatFormatting.RED),
					true);
		}

		return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
	}

}
