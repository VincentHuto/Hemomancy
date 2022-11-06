/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package com.vincenthuto.hemomancy.network.keybind;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.network.FriendlyByteBuf;

public class RadialInventorySlotChangePacket{
	public static RadialInventorySlotChangePacket decode(FriendlyByteBuf buf) {
		RadialInventorySlotChangePacket msg = new RadialInventorySlotChangePacket();
		try {
			msg.slot = buf.readInt();
			msg.offhand = buf.readBoolean();
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			Hemomancy.LOGGER.error("Exception while reading RadialInventorySlotChangeMessage: " + e);
			return msg;
		}
		msg.messageIsValid = true;
		return msg;
	}
	public static void encode(RadialInventorySlotChangePacket msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.getSlot());
		buf.writeBoolean(msg.isOffhand());
	}
	private int slot;

	private boolean offhand;

	protected boolean messageIsValid = false;

	public RadialInventorySlotChangePacket() {
		this.messageIsValid = false;
	}

	public RadialInventorySlotChangePacket(int slot, boolean offhand) {
		this.slot = slot;
		this.offhand = offhand;
		this.messageIsValid = true;
	}

	public int getSlot() {
		return this.slot;
	}

	public final boolean isMessageValid() {
		return this.messageIsValid;
	}

	public boolean isOffhand() {
		return this.offhand;
	}
}
