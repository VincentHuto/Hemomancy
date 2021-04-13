package com.huto.hemomancy.network.manip;

import java.util.function.Supplier;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.conjuration.ManipBaseConjuration;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUseManipOnKeyPress {

	float parTick;

	public PacketUseManipOnKeyPress() {
	}

	public PacketUseManipOnKeyPress(float par) {
		this.parTick = par;
	}

	public static PacketUseManipOnKeyPress decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketUseManipOnKeyPress(buffer.readFloat());
	}

	public static void encode(final PacketUseManipOnKeyPress message, final PacketBuffer buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final PacketUseManipOnKeyPress message, final Supplier<NetworkEvent.Context> ctx) {
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
					if (selectedManip instanceof ManipBaseConjuration) {
						ManipBaseConjuration conjure = (ManipBaseConjuration) selectedManip;
						if (!player.getHeldItemMainhand().isEmpty()) {
							if (player.getHeldItemMainhand().getItem() == conjure.getItem()) {
								System.out.println(conjure.getItem());
								player.getHeldItemMainhand().shrink(1);
								player.sendStatusMessage(new StringTextComponent("Dispelled: " + conjure.getProperName())
										.mergeStyle(TextFormatting.RED), true);
							} else {
								player.sendStatusMessage(new StringTextComponent("Conjuration requires an empty hand!")
										.mergeStyle(TextFormatting.RED), true);
							}
						} else {
							selectedManip.performAction(player, (ServerWorld) player.world,
									player.getHeldItemMainhand(), player.getPosition());
						}
					} else {
						selectedManip.performAction(player, (ServerWorld) player.world, player.getHeldItemMainhand(),
								player.getPosition());
					}

				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

}