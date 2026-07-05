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
import com.vincenthuto.hemomancy.common.manipulation.animus.SummonThrallManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.ConjurationManip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Unified packet for casting any blood manipulation regardless of type.
 * Replaces the old split between UseQuickManipKeyPacket and UseContManipKeyPacket.
 */
public class UseManipKeyPacket implements CustomPacketPayload {

	public static final Type<UseManipKeyPacket> TYPE = new Type<>(Hemomancy.rloc("use_manip_key_packet"));
	public static final StreamCodec<FriendlyByteBuf, UseManipKeyPacket> STREAM_CODEC = StreamCodec.of(UseManipKeyPacket::encode, UseManipKeyPacket::decode);

	public static UseManipKeyPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new UseManipKeyPacket(buffer.readFloat());
	}

	public static void encode(final FriendlyByteBuf buffer, final UseManipKeyPacket message) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final UseManipKeyPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;
			if (!player.level().isClientSide) {
				float pTic = message.parTick;

				// Allow SummonThrallManip through cooldown when selecting a destination
				boolean bypassCooldown = false;
				IKnownManipulations knownCheck = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
				if (knownCheck != null && knownCheck.getSelectedManip() != null) {
					BloodManipulation selManip = ManipulationInit.getByName(knownCheck.getSelectedManip().getName());
					if (selManip instanceof SummonThrallManip && SummonThrallManip.hasPendingThrall(player.getUUID())) {
						bypassCooldown = true;
					}
					if (selManip != null && selManip.ignoresCooldown(player)) {
						bypassCooldown = true;
					}
				}

				if (!bypassCooldown && BloodManipulation.isAnyManipOnCooldown(player)) {
					player.displayClientMessage(Component.literal("Manipulation on cooldown!")
							.withStyle(ChatFormatting.RED), true);
					return;
				}
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
							// Handle conjuration dispel logic
							if (selectedManip instanceof ConjurationManip conjure) {
								if (!mainStack.isEmpty()) {
									if (mainStack.getItem() instanceof IDispellable dispel) {
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
												Component.literal("Conjuration Requires an Empty Hand!")
														.withStyle(ChatFormatting.RED),
												true);
									}
								} else {
									selectedManip.performAction(player, player.level(), mainStack,
											player.blockPosition());
								}
							} else {
								// All other manipulation types — just perform
								selectedManip.performAction(player, player.level(), mainStack,
										player.blockPosition());
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

	public UseManipKeyPacket() {
	}

	public UseManipKeyPacket(float par) {
		this.parTick = par;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
