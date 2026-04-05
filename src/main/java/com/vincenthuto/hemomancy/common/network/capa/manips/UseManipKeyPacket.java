package com.vincenthuto.hemomancy.common.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.tool.living.IDispellable;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.quick.ConjurationManip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Unified packet for casting any blood manipulation regardless of type.
 * Replaces the old split between UseQuickManipKeyPacket and UseContManipKeyPacket.
 */
public class UseManipKeyPacket {

	public static UseManipKeyPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new UseManipKeyPacket(buffer.readFloat());
	}

	public static void encode(final UseManipKeyPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final UseManipKeyPacket message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.level().isClientSide) {
				float pTic = message.parTick;
				if (BloodManipulation.isAnyManipOnCooldown(player)) {
					player.displayClientMessage(Component.literal("Manipulation on cooldown!")
							.withStyle(ChatFormatting.RED), true);
					return;
				}
				IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
						.orElseThrow(NullPointerException::new);
				if (volume.isActive()) {
					if (known.getSelectedManip() != null) {
						ItemStack mainStack = player.getMainHandItem();
						BloodManipulation selectedManip = ManipulationInit
								.getByName(known.getSelectedManip().getName());
						if (selectedManip != null) {
							// Handle conjuration dispel logic
							if (selectedManip instanceof ConjurationManip conjure) {
								if (!mainStack.isEmpty()) {
									if (mainStack.getItem() instanceof IDispellable dispel) {
										mainStack.shrink(1);
										float bloodCost = dispel.getBaseCost();
										float bloodRefund = Math
												.abs(mainStack.getMaxDamage() - 1000 - mainStack.getDamageValue());
										if (bloodRefund > bloodCost * 0.9) {
											bloodRefund = bloodCost * 0.9f;
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
		ctx.get().setPacketHandled(true);
	}

	float parTick;

	public UseManipKeyPacket() {
	}

	public UseManipKeyPacket(float par) {
		this.parTick = par;
	}
}
