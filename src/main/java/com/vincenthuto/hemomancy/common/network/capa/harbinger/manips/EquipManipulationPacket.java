package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.mission.mnemonist.MnemonicReliquaryProgression;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server packet to equip or unequip a manipulation in the player's
 * limited manipulation slots.
 */
public class EquipManipulationPacket implements CustomPacketPayload {

	public static final Type<EquipManipulationPacket> TYPE = new Type<>(Hemomancy.rloc("equip_manipulation_packet"));
	public static final StreamCodec<FriendlyByteBuf, EquipManipulationPacket> STREAM_CODEC = StreamCodec.of(EquipManipulationPacket::encode, EquipManipulationPacket::decode);

	private final String manipName;
	private final boolean equip; // true = equip, false = unequip

	public EquipManipulationPacket(String manipName, boolean equip) {
		this.manipName = manipName;
		this.equip = equip;
	}

	public static void encode(FriendlyByteBuf buf, EquipManipulationPacket msg) {
		buf.writeUtf(msg.manipName);
		buf.writeBoolean(msg.equip);
	}

	public static EquipManipulationPacket decode(FriendlyByteBuf buf) {
		return new EquipManipulationPacket(buf.readUtf(), buf.readBoolean());
	}

	public static void handle(final EquipManipulationPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (!(player instanceof ServerPlayer serverPlayer)) return;

			IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
					.orElse(null);
			if (known == null) return;

			MemorySlotRef ref = MemorySlotRef.fromStorageKey(msg.manipName);
			if (ref.kind() == MemoryEntryKind.MUSCLE_MEMORY) {
				var memory = ref.muscleMemory().orElse(null);
				if (memory == null || !player.getData(HemoAttachmentTypes.MUSCLE_MEMORY).knows(memory)) {
					player.displayClientMessage(Component.literal("That Thelemic Memory is still unknown.")
							.withStyle(ChatFormatting.RED), true);
					return;
				}
				int maxSlots = ManipSlotHelper.getMaxSlots(player);
				boolean changed = msg.equip ? known.equipMemory(ref, maxSlots) : known.unequipMemory(ref);
				if (!changed && msg.equip) {
					player.displayClientMessage(Component.literal("No free shared memory slots for this equip ("
							+ ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames())
							+ "/" + maxSlots + ")").withStyle(ChatFormatting.RED), true);
				}
			} else if (msg.equip) {
				if (ManipulationRetirementRules.isRetiredManipulation(msg.manipName)) {
					player.displayClientMessage(
							Component.literal("That manipulation has gone dormant.")
									.withStyle(ChatFormatting.DARK_GRAY), true);
					ManipulationRetirementRules.sanitizeKnownManipulations(known);
					PacketHandler.sendToPlayer((ServerPlayer) player, new KnownManipulationServerPacket(known));
					return;
				}
				boolean knownExact = known.getManipList().stream().anyMatch(manipulation -> manipulation != null
						&& msg.manipName.equals(manipulation.getName()));
				if (!knownExact) {
					player.displayClientMessage(Component.literal("That manipulation is still unknown.")
							.withStyle(ChatFormatting.RED), true);
					return;
				}
				int maxSlots = ManipSlotHelper.getMaxSlots(player);
				if (known.equipManip(msg.manipName, maxSlots)) {
					player.displayClientMessage(
							Component.literal("Manipulation equipped: " + msg.manipName)
									.withStyle(ChatFormatting.GREEN), true);
				} else {
					player.displayClientMessage(
							Component.literal("No free slots for this equip (" +
									ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames())
											+ "/" + maxSlots + ")")
									.withStyle(ChatFormatting.RED), true);
				}
			} else {
				if (known.unequipManip(msg.manipName)) {
					player.displayClientMessage(
							Component.literal("Manipulation unequipped: " + msg.manipName)
									.withStyle(ChatFormatting.YELLOW), true);
				}
			}
			MnemonicReliquaryProgression.onCapacityChanged(serverPlayer, known);

			// Sync updated state back to client
			PacketHandler.sendToPlayer(serverPlayer, new KnownManipulationServerPacket(known));
			ManipulationDiagnosticsSync.sync(serverPlayer);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
