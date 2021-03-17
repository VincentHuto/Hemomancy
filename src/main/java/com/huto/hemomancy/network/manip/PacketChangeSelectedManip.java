package com.huto.hemomancy.network.manip;

import java.util.List;
import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.manipulation.IKnownManipulations;
import com.huto.hemomancy.capabilities.manipulation.KnownManipulationProvider;
import com.huto.hemomancy.manipulation.BloodManipulation;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketChangeSelectedManip {

	float parTick;

	public PacketChangeSelectedManip() {
	}

	public PacketChangeSelectedManip(float par) {
		this.parTick = par;
	}

	public static PacketChangeSelectedManip decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketChangeSelectedManip(buffer.readFloat());
	}

	public static void encode(final PacketChangeSelectedManip message, final PacketBuffer buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final PacketChangeSelectedManip message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.world.isRemote) {
				float pTic = message.parTick;
				IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
						.orElseThrow(NullPointerException::new);
				List<BloodManipulation> manips = known.getKnownManips();

				if (!known.getKnownManips().contains(known.getSelectedManip())) {
					if (!known.getKnownManips().isEmpty()) {
						known.setSelectedManip(manips.get(0));
					} else {
						player.sendStatusMessage(new StringTextComponent("No Known Manipulations to select"), false);
					}
				} else {
					int foundIndex = known.getKnownManips().indexOf(known.getSelectedManip());
					System.out.println(foundIndex);
					if (foundIndex < known.getKnownManips().size() - 1) {
						known.setSelectedManip(known.getKnownManips().get(foundIndex + 1));
					} else {
						known.setSelectedManip(known.getKnownManips().get(0));
					}
				}
			}

		});
		ctx.get().setPacketHandled(true);
	}

}