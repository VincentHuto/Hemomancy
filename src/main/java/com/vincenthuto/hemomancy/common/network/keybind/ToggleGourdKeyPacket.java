package com.vincenthuto.hemomancy.common.network.keybind;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.scar.ScarsCapabilities;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

public class ToggleGourdKeyPacket {

	public ToggleGourdKeyPacket() {
	}

	public static ToggleGourdKeyPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new ToggleGourdKeyPacket();
	}

	public static void encode(final ToggleGourdKeyPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final ToggleGourdKeyPacket message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;

			// Get the gourd from scar slot 6
			player.getCapability(ScarsCapabilities.SCARS).ifPresent(inv -> {
				ItemStack stack = inv.getStackInSlot(6);
				if (stack.getItem() instanceof BloodGourdItem) {
					CompoundTag compound = stack.getOrCreateTag();
					boolean currentState = compound.getBoolean(BloodGourdItem.TAG_STATE);

					// Toggle the state
					if (!currentState) {
						player.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
						compound.putBoolean(BloodGourdItem.TAG_STATE, true);
					} else {
						player.playSound(SoundEvents.BEACON_DEACTIVATE, 0.40f, 1F);
						compound.putBoolean(BloodGourdItem.TAG_STATE, false);
					}
					stack.setTag(compound);
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}

