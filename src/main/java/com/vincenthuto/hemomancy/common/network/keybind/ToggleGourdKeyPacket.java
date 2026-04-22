package com.vincenthuto.hemomancy.common.network.keybind;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ToggleGourdKeyPacket implements CustomPacketPayload {

	public static final Type<ToggleGourdKeyPacket> TYPE = new Type<>(Hemomancy.rloc("toggle_gourd_key_packet"));
	public static final StreamCodec<FriendlyByteBuf, ToggleGourdKeyPacket> STREAM_CODEC = StreamCodec.of(ToggleGourdKeyPacket::encode, ToggleGourdKeyPacket::decode);

	public ToggleGourdKeyPacket() {
	}

	public static ToggleGourdKeyPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new ToggleGourdKeyPacket();
	}

	public static void encode(final ToggleGourdKeyPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final ToggleGourdKeyPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;

			// Get the gourd from scar slot 6
			HemoCapabilityAccess.getScars(player).ifPresent(inv -> {
				ItemStack stack = inv.getStackInSlot(6);
				if (stack.getItem() instanceof BloodGourdItem) {
					CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
					boolean currentState = compound.getBoolean(BloodGourdItem.TAG_STATE);

					// Toggle the state
					if (!currentState) {
						player.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
						compound.putBoolean(BloodGourdItem.TAG_STATE, true);
					} else {
						player.playSound(SoundEvents.BEACON_DEACTIVATE, 0.40f, 1F);
						compound.putBoolean(BloodGourdItem.TAG_STATE, false);
					}
					stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
				}
			});
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
