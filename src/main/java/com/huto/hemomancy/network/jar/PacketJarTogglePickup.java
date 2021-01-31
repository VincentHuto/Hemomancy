package com.huto.hemomancy.network.jar;

import java.util.function.Supplier;

import com.huto.hemomancy.item.tool.ItemMorphlingJar;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketJarTogglePickup {
    public static PacketJarTogglePickup decode(final PacketBuffer buffer) {
        buffer.readByte();
        return new PacketJarTogglePickup();
    }
    public static void encode(final PacketJarTogglePickup message, final PacketBuffer buffer) {
        buffer.writeByte(0);
    }
    public static void handle(final PacketJarTogglePickup message, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(
                ()-> {
                    PlayerEntity player = ctx.get().getSender();
                    if (player == null)
                        return;
                    if (player.getHeldItemMainhand().getItem() instanceof ItemMorphlingJar)
                        ((ItemMorphlingJar) player.getHeldItemMainhand().getItem()).togglePickup(player, player.getHeldItemMainhand());
                    else if (player.getHeldItemOffhand().getItem() instanceof  ItemMorphlingJar)
                        ((ItemMorphlingJar) player.getHeldItemOffhand().getItem()).togglePickup(player, player.getHeldItemOffhand());
                    else {
                        //check hotbar
                        for (int i = 0; i <= 8; i++ ) {
                            ItemStack stack = player.inventory.getStackInSlot(i);
                            if (stack.getItem() instanceof  ItemMorphlingJar) {
                                ((ItemMorphlingJar) stack.getItem()).togglePickup(player, stack);
                                break;
                            }
                        }
                    }
                }
        );
        ctx.get().setPacketHandled(true);
    }
}