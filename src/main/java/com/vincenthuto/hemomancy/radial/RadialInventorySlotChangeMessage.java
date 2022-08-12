/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package com.vincenthuto.hemomancy.radial;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.network.FriendlyByteBuf;

public class RadialInventorySlotChangeMessage
extends BaseMessage {
    private int slot;
    private boolean offhand;

    public RadialInventorySlotChangeMessage(int slot, boolean offhand) {
        this.slot = slot;
        this.offhand = offhand;
        this.messageIsValid = true;
	}

    public RadialInventorySlotChangeMessage() {
        this.messageIsValid = false;
    }

    public int getSlot() {
        return this.slot;
    }

    public boolean isOffhand() {
        return this.offhand;
    }

    public static RadialInventorySlotChangeMessage decode(FriendlyByteBuf buf) {
        RadialInventorySlotChangeMessage msg = new RadialInventorySlotChangeMessage();
        try {
            msg.slot = buf.readInt();
            msg.offhand = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            Hemomancy.LOGGER.error("Exception while reading RadialInventorySlotChangeMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(RadialInventorySlotChangeMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getSlot());
        buf.writeBoolean(msg.isOffhand());
    }
}

