package com.huto.hemomancy.network.manip;

import java.util.function.Supplier;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationType;
import com.huto.hemomancy.manipulation.quick.ManipConjuration;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUseQuickManipKey {

	float parTick;

	public PacketUseQuickManipKey() {
	}

	public PacketUseQuickManipKey(float par) {
		this.parTick = par;
	}

	public static PacketUseQuickManipKey decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketUseQuickManipKey(buffer.readFloat());
	}

	public static void encode(final PacketUseQuickManipKey message, final PacketBuffer buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final PacketUseQuickManipKey message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.world.isRemote) {
				float pTic = message.parTick;
				IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
						.orElseThrow(NullPointerException::new);
				if (known.getSelectedManip() != null) {
					BloodManipulation selectedManip = ManipulationInit.getByName(known.getSelectedManip().getName());
					if (selectedManip != null) {
						// Quick and Passives
						if (selectedManip.getType() == EnumManipulationType.QUICK
								|| selectedManip.getType() == EnumManipulationType.PASSIVE) {
							if (selectedManip instanceof ManipConjuration) {
								ManipConjuration conjure = (ManipConjuration) selectedManip;
								if (!player.getHeldItemMainhand().isEmpty()) {
									if (player.getHeldItemMainhand().getItem() == conjure.getItem().get()) {
										player.getHeldItemMainhand().shrink(1);
										player.sendStatusMessage(
												new StringTextComponent("Dispelled: " + conjure.getProperName())
														.mergeStyle(TextFormatting.RED),
												true);
									} else {
										player.sendStatusMessage(
												new StringTextComponent("Conjuration requires an empty hand!")
														.mergeStyle(TextFormatting.RED),
												true);
									}
								} else {
									selectedManip.performAction(player, (ServerWorld) player.world,
											player.getHeldItemMainhand(), player.getPosition());
								}
							} else {
								selectedManip.performAction(player, (ServerWorld) player.world,
										player.getHeldItemMainhand(), player.getPosition());
							}
						} else {
							player.sendStatusMessage(
									new StringTextComponent("Selected Manipulation is not a Quick or Passive Effect")
											.mergeStyle(TextFormatting.RED),
									true);
						}
					}
				}

			}
		});
		ctx.get().setPacketHandled(true);
	}

}