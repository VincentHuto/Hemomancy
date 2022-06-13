package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.ManipulationInit;
import com.vincenthuto.hemomancy.item.tool.living.IDispellable;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.manipulation.quick.ManipConjuration;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketUseQuickManipKey {

	float parTick;

	public PacketUseQuickManipKey() {
	}

	public PacketUseQuickManipKey(float par) {
		this.parTick = par;
	}

	public static PacketUseQuickManipKey decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketUseQuickManipKey(buffer.readFloat());
	}

	public static void encode(final PacketUseQuickManipKey message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final PacketUseQuickManipKey message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.level.isClientSide) {
				float pTic = message.parTick;
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
							if (selectedManip.getType() == EnumManipulationType.QUICK
									|| selectedManip.getType() == EnumManipulationType.PASSIVE) {
								if (selectedManip instanceof ManipConjuration conjure) {
									if (!mainStack.isEmpty()) {
										if (mainStack.getItem()instanceof IDispellable dispel) {
											mainStack.shrink(1);
											float bloodCost = dispel.getBaseCost();
											float bloodRefund = Math
													.abs(mainStack.getMaxDamage() - 1000 - mainStack.getDamageValue());
											if (bloodRefund > bloodCost * 0.9) {
												bloodRefund = bloodCost * 0.9f;
											}

											volume.fill(bloodRefund);
											mainStack.shrink(1);
											player.displayClientMessage( Component.translatable("Dispelled Conjured Item")
													.withStyle(ChatFormatting.RED), true);
										} else {
											player.displayClientMessage(
													 Component.translatable("Conjuration Requires an Empty InteractionHand!")
															.withStyle(ChatFormatting.RED),
													true);
										}
									} else {
										selectedManip.performAction(player, player.level, mainStack,
												player.blockPosition());
									}
								} else {
									selectedManip.performAction(player, player.level, mainStack,
											player.blockPosition());
								}
							} else {
								player.displayClientMessage(
										 Component.translatable("Selected Manipulation is not a Quick or Passive MobEffect")
												.withStyle(ChatFormatting.RED),
										true);
							}
						}
					}

				} else {
					player.displayClientMessage( Component.translatable("You lack the skill to manifest this power!")
							.withStyle(ChatFormatting.RED), true);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

}