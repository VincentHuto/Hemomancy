package com.huto.hemomancy.network.manip;

import java.util.List;
import java.util.function.Supplier;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.manipulation.BloodManipulation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketChangeSelectedManip {

	float parTick;

	public PacketChangeSelectedManip() {
	}

	public PacketChangeSelectedManip(float par) {
		this.parTick = par;
	}

	public static PacketChangeSelectedManip decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketChangeSelectedManip(buffer.readFloat());
	}

	public static void encode(final PacketChangeSelectedManip message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final PacketChangeSelectedManip message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.world.isRemote) {
				float pTic = message.parTick;
				IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
						.orElseThrow(NullPointerException::new);
				List<BloodManipulation> manips = known.getKnownManips();

				if (!manips.contains(known.getSelectedManip())) {
					if (!manips.isEmpty()) {
						known.setSelectedManip(manips.get(0));
					} else {
						player.sendStatusMessage(new StringTextComponent("No Known Manipulations to select"), true);
					}
				} else {
					int foundIndex = manips.indexOf(known.getSelectedManip());
					if (foundIndex < manips.size() - 1) {
						known.setSelectedManip(manips.get(foundIndex + 1));
						player.sendStatusMessage(
								new StringTextComponent("Selected:" + manips.get(foundIndex + 1).getProperName()),
								true);

					} else {
						known.setSelectedManip(manips.get(0));
						player.sendStatusMessage(new StringTextComponent("Selected:" + manips.get(0).getProperName()),
								true);

					}
				}
			}

		});
		ctx.get().setPacketHandled(true);
	}

}