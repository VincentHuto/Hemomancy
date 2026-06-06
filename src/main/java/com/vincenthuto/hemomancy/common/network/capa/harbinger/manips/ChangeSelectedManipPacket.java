package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.CellHandFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class ChangeSelectedManipPacket implements CustomPacketPayload {

	public static final Type<ChangeSelectedManipPacket> TYPE = new Type<>(Hemomancy.rloc("change_selected_manip_packet"));
	public static final StreamCodec<FriendlyByteBuf, ChangeSelectedManipPacket> STREAM_CODEC = StreamCodec.of(ChangeSelectedManipPacket::encode, ChangeSelectedManipPacket::decode);

	public static ChangeSelectedManipPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new ChangeSelectedManipPacket(buffer.readFloat());
	}

	public static void encode(final FriendlyByteBuf buffer, final ChangeSelectedManipPacket message) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final ChangeSelectedManipPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;
			if (!player.level().isClientSide) {
				IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
						.orElseThrow(NullPointerException::new);
				// Build a list of only equipped manipulations
				List<String> equippedNames = known.getEquippedManipNames();
				List<BloodManipulation> allManips = known.getManipList();
				List<BloodManipulation> equipped = new ArrayList<>();
				for (BloodManipulation m : allManips) {
					if (equippedNames.contains(m.getName())) {
						equipped.add(m);
					}
				}

				if (equipped.isEmpty()) {
					player.displayClientMessage(Component.literal("No equipped manipulations to select"), true);
					return;
				}

				BloodManipulation target;
				if (!equipped.contains(known.getSelectedManip())) {
					target = equipped.get(0);
				} else {
					int foundIndex = equipped.indexOf(known.getSelectedManip());
					if (foundIndex < equipped.size() - 1) {
						target = equipped.get(foundIndex + 1);
					} else {
						target = equipped.get(0);
					}
				}
				if (!LivingStaffWeaponFormHelper.applySelection(player, target)) {
					return;
				}
				if (!CellHandFormHelper.applySelection(player, target)) {
					return;
				}
				known.setSelectedManip(target);
				player.displayClientMessage(Component.literal("Selected:" + target.getProperName()), true);
			}

		});
	}

	float parTick;

	public ChangeSelectedManipPacket() {
	}

	public ChangeSelectedManipPacket(float par) {
		this.parTick = par;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
