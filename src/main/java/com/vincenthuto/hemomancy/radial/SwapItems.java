package com.vincenthuto.hemomancy.radial;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.ConfigData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkEvent;

public class SwapItems
{
    public int swapWith;

    public SwapItems(int windowId)
    {
        this.swapWith = windowId;
    }

    public SwapItems(FriendlyByteBuf buf)
    {
        swapWith = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(swapWith);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> swapItem(swapWith, context.get().getSender()));
        return true;
    }

    public static void swapItem(int swapWith, Player player)
    {
        CharmFinder.findCharm(player).ifPresent((getter) -> {
            ItemStack stack = getter.getCharm();
            if (stack.getCount() <= 0)
                return;

            ItemStack inHand = player.getMainHandItem();

            if (!ConfigData.isItemStackAllowed(inHand))
                return;

            IItemHandlerModifiable cap = (IItemHandlerModifiable) (
                    stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                            .orElseThrow(() -> new RuntimeException("No inventory!")));
            if (swapWith < 0)
            {
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemHandlerHelper.insertItem(cap, inHand, false));
            }
            else
            {
                ItemStack inSlot = cap.getStackInSlot(swapWith);
                player.setItemInHand(InteractionHand.MAIN_HAND, inSlot);
                cap.setStackInSlot(swapWith, inHand);
            }
            getter.syncToClients();
        });
    }
}
