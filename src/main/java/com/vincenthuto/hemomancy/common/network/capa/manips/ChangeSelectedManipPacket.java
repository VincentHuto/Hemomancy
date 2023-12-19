package com.vincenthuto.hemomancy.common.network.capa.manips;

import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ChangeSelectedManipPacket {

	public static ChangeSelectedManipPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new ChangeSelectedManipPacket(buffer.readFloat());
	}

	public static void encode(final ChangeSelectedManipPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final ChangeSelectedManipPacket message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.level().isClientSide) {
				float pTic = message.parTick;
				IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
						.orElseThrow(NullPointerException::new);
				List<BloodManipulation> manips = known.getManipList();
				if (!manips.contains(known.getSelectedManip())) {
					if (!manips.isEmpty()) {
						known.setSelectedManip(manips.get(0));
					} else {
						player.displayClientMessage(Component.literal("No Known Manipulations to select"), true);
					}
				} else {
					int foundIndex = manips.indexOf(known.getSelectedManip());
					if (foundIndex < manips.size() - 1) {
						known.setSelectedManip(manips.get(foundIndex + 1));
						player.displayClientMessage(
								Component.literal("Selected:" + manips.get(foundIndex + 1).getProperName()), true);

					} else {
						known.setSelectedManip(manips.get(0));
						player.displayClientMessage(Component.literal("Selected:" + manips.get(0).getProperName()),
								true);

					}
				}
			}

		});
		ctx.get().setPacketHandled(true);
	}

	float parTick;

	public ChangeSelectedManipPacket() {
	}

	public ChangeSelectedManipPacket(float par) {
		this.parTick = par;
	}

}