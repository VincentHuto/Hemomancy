package com.vincenthuto.hemomancy.network.manip;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.ManipulationInit;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketUseContManipKey {

	float parTick;

	public PacketUseContManipKey() {
	}

	public PacketUseContManipKey(float par) {
		this.parTick = par;
	}

	public static PacketUseContManipKey decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketUseContManipKey(buffer.readFloat());
	}

	public static void encode(final PacketUseContManipKey message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final PacketUseContManipKey message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.level.isClientSide) {
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
								selectedManip.performAction(player, (ServerLevel) player.level,
										player.getMainHandItem(), player.blockPosition());
							} else {
								player.displayClientMessage(new TextComponent(
										"Selected Manipulation is not a Continous or Charged MobEffect")
												.withStyle(ChatFormatting.RED),
										true);
							}
						}
					}

				} else {
					player.displayClientMessage(new TextComponent("You lack the skill to manifest this power!")
							.withStyle(ChatFormatting.RED), true);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

}