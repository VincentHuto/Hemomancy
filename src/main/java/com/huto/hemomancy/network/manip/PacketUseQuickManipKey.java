package com.huto.hemomancy.network.manip;

import java.util.function.Supplier;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.item.tool.living.IDispellable;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationType;
import com.huto.hemomancy.manipulation.quick.ManipConjuration;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
						ItemStack mainStack = player.getHeldItemMainhand();
						BloodManipulation selectedManip = ManipulationInit
								.getByName(known.getSelectedManip().getName());
						if (selectedManip != null) {
							if (selectedManip.getType() == EnumManipulationType.QUICK
									|| selectedManip.getType() == EnumManipulationType.PASSIVE) {
								if (selectedManip instanceof ManipConjuration) {
									ManipConjuration conjure = (ManipConjuration) selectedManip;
									if (!mainStack.isEmpty()) {
										if (mainStack.getItem() instanceof IDispellable) {
											mainStack.shrink(1);
											float bloodRefund = Math
													.abs(mainStack.getMaxDamage() - 1000 - mainStack.getDamage());
											if (bloodRefund > 900) {
												bloodRefund = 900;
											}

											volume.addBloodVolume(bloodRefund);
											mainStack.shrink(1);
											player.sendStatusMessage(new StringTextComponent("Dispelled Conjured Item")
													.mergeStyle(TextFormatting.RED), true);
										} else {
											player.sendStatusMessage(
													new StringTextComponent("Conjuration Requires an Empty Hand!")
															.mergeStyle(TextFormatting.RED),
													true);
										}
									} else {
										selectedManip.performAction(player, (ServerWorld) player.world, mainStack,
												player.getPosition());
									}
								} else {
									selectedManip.performAction(player, (ServerWorld) player.world, mainStack,
											player.getPosition());
								}
							} else {
								player.sendStatusMessage(new StringTextComponent(
										"Selected Manipulation is not a Quick or Passive Effect")
												.mergeStyle(TextFormatting.RED),
										true);
							}
						}
					}

				} else {
					player.sendStatusMessage(new StringTextComponent("You lack the skill to manifest this power!")
							.mergeStyle(TextFormatting.RED), true);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

}