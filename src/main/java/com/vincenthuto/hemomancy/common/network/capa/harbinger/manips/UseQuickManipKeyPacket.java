package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.IDispellable;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ferric.ConjurationManip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UseQuickManipKeyPacket implements CustomPacketPayload {

	public static final Type<UseQuickManipKeyPacket> TYPE = new Type<>(Hemomancy.rloc("use_quick_manip_key_packet"));
	public static final StreamCodec<FriendlyByteBuf, UseQuickManipKeyPacket> STREAM_CODEC = StreamCodec.of(UseQuickManipKeyPacket::encode, UseQuickManipKeyPacket::decode);

	public static UseQuickManipKeyPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new UseQuickManipKeyPacket(buffer.readFloat());
	}

	public static void encode(final FriendlyByteBuf buffer, final UseQuickManipKeyPacket message) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final UseQuickManipKeyPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;
			if (!player.level().isClientSide) {
				float pTic = message.parTick;
				IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
						.orElseThrow(NullPointerException::new);
				IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
						.orElseThrow(NullPointerException::new);
				if (volume.isActive()) {
					if (known.getSelectedManip() != null) {
						ItemStack mainStack = player.getMainHandItem();
						BloodManipulation selectedManip = ManipulationInit
								.getByName(known.getSelectedManip().getName());
						if (selectedManip != null) {
							if (ManipulationRetirementRules.isRetiredManipulation(selectedManip)) {
								player.displayClientMessage(
										Component.literal("That manipulation has gone dormant.")
												.withStyle(ChatFormatting.DARK_GRAY), true);
								ManipulationRetirementRules.sanitizeKnownManipulations(known);
								return;
							}
							// Check manipulation is equipped
							if (!known.isManipEquipped(selectedManip)) {
								player.displayClientMessage(
										Component.literal("That manipulation is not equipped!")
												.withStyle(ChatFormatting.RED), true);
								return;
							}
							if (LivingStaffWeaponFormRules.isStaffWeaponFormManip(selectedManip.getName())) {
								LivingStaffWeaponFormHelper.toggleSelectedForm(player, selectedManip);
								return;
							}
							if (selectedManip.getType() == EnumManipulationType.QUICK
									|| selectedManip.getType() == EnumManipulationType.PASSIVE) {
								if (selectedManip instanceof ConjurationManip conjure) {
									if (!mainStack.isEmpty()) {
										if (mainStack.getItem()instanceof IDispellable dispel) {
											mainStack.shrink(1);
											double bloodCost = dispel.getBaseCost();
											double bloodRefund = Math
													.abs(mainStack.getMaxDamage() - 1000 - mainStack.getDamageValue());
											if (bloodRefund > bloodCost * 0.9) {
												bloodRefund = bloodCost * 0.9;
											}

											volume.fill(bloodRefund);
											mainStack.shrink(1);
											player.displayClientMessage(Component.literal("Dispelled Conjured Item")
													.withStyle(ChatFormatting.RED), true);
										} else {
											player.displayClientMessage(
													Component.literal("Conjuration Requires an Empty InteractionHand!")
															.withStyle(ChatFormatting.RED),
													true);
										}
									} else {
										selectedManip.performAction(player, player.level(), mainStack,
												player.blockPosition());
									}
								} else {
									selectedManip.performAction(player, player.level(), mainStack,
											player.blockPosition());
								}
							} else {
								player.displayClientMessage(
										Component.literal("Selected Manipulation is not a Quick or Passive MobEffect")
												.withStyle(ChatFormatting.RED),
										true);
							}
						}
					}

				} else {
					player.displayClientMessage(Component.literal("You lack the skill to manifest this power!")
							.withStyle(ChatFormatting.RED), true);
				}
			}
		});
	}

	float parTick;

	public UseQuickManipKeyPacket() {
	}

	public UseQuickManipKeyPacket(float par) {
		this.parTick = par;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
