package com.vincenthuto.hemomancy.common.item.rune;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRune;
import com.vincenthuto.hemomancy.common.capability.player.rune.RuneType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodTendencyServerPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class ItemRune extends Item implements IRune {

	EnumBloodTendency assignedTendency;
	float deepenAmount;

	public ItemRune(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties);
		this.assignedTendency = tendencyIn;
		this.deepenAmount = deepenAmountIn;
	}

	@Override
	public void onEquipped(LivingEntity player) {
		if (player instanceof Player) {
			if (!player.getCommandSenderWorld().isClientSide) {
				IBloodTendency coven = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				if (coven != null) {
					coven.setTendencyAlignment(getAssignedTendency(), getDeepenAmount());
					Player playerEnt = (Player) player;
					PacketHandler.CHANNELBLOODTENDENCY.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerEnt),
							new BloodTendencyServerPacket(coven.getTendency()));
				}
			}
		}
	}

	@Override
	public void onUnequipped(LivingEntity player) {
		if (player instanceof Player) {
			if (!player.getCommandSenderWorld().isClientSide) {
				IBloodTendency coven = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				if (coven != null) {
					coven.setTendencyAlignment(getAssignedTendency(), -getDeepenAmount());
					Player playerEnt = (Player) player;
					PacketHandler.CHANNELBLOODTENDENCY.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerEnt),
							new BloodTendencyServerPacket(coven.getTendency()));
				}
			}
		}

	}

	public float getDeepenAmount() {
		return deepenAmount;
	}

	public void setDeepenAmount(int deepenAmount) {
		this.deepenAmount = deepenAmount;
	}

	public EnumBloodTendency getAssignedTendency() {
		return assignedTendency;
	}

	public void setAssignedTendency(EnumBloodTendency assignedTendency) {
		this.assignedTendency = assignedTendency;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component
				.translatable(ChatFormatting.GOLD + "Tendency: " + HLTextUtils.toProperCase(assignedTendency.name())));
		tooltip.add(Component.translatable(ChatFormatting.GREEN + "Tendency Amount: " + deepenAmount));

	}

	@Override
	public RuneType getRuneType() {
		return RuneType.RUNE;
	}

//	@Override
//	public void onPlayerRuneRender(PoseStack matrix, ItemStack stack, int packedLight,
//			MultiBufferSource iRenderTypeBuffer, Player player, RenderType type, float partialTicks) {
//		Minecraft mc = Minecraft.getInstance();
//		if (type == RenderType.BODY) {
//	
//			matrix.pushPose();
//			matrix.mulPose(Vector3.YP.rotationDegrees(player.level().getGameTime()).toMoj()); // Edit
//			matrix.translate(0.025F, -0.75F, 0.025F);
//			matrix.mulPose(Vector3.YP.rotationDegrees(90f).toMoj()); // Edit Radius Movement
//			matrix.scale(0.25f, 0.25f, 0.25f);
//			if (!stack.isEmpty()) {
////				mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
////						matrix, iRenderTypeBuffer, player.level(), 0);
//			}
//			matrix.popPose();
//		
//		
//		
//		}
//
//	}
//
//	@Override
//	public RenderType getRenderType() {
//		return this.renderType;
//	}

}
