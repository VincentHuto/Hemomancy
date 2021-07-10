package com.huto.hemomancy.network.manip;

import java.util.function.Supplier;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUseContManipKey {

	float parTick;

	public PacketUseContManipKey() {
	}

	public PacketUseContManipKey(float par) {
		this.parTick = par;
	}

	public static PacketUseContManipKey decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketUseContManipKey(buffer.readFloat());
	}

	public static void encode(final PacketUseContManipKey message, final PacketBuffer buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final PacketUseContManipKey message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.world.isRemote) {
				float pTic = message.parTick;
				IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
						.orElseThrow(NullPointerException::new);
				IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				if (volume.isActive()) {
					if (known.getSelectedManip() != null) {
						BloodManipulation selectedManip = ManipulationInit
								.getByName(known.getSelectedManip().getName());
						if (selectedManip != null) {
							// Continuous and Charged
							if (selectedManip.getType() == EnumManipulationType.CONTINUOUS
									|| selectedManip.getType() == EnumManipulationType.CHARGED) {
								selectedManip.performAction(player, (ServerWorld) player.world,
										player.getHeldItemMainhand(), player.getPosition());
							} else {
								player.sendStatusMessage(new StringTextComponent(
										"Selected Manipulation is not a Continous or Charged Effect")
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