package com.vincenthuto.hemomancy.network;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.radial.BaseMessage;
import com.vincenthuto.hemomancy.radial.IRadialInventorySelect;
import com.vincenthuto.hemomancy.radial.RadialInventorySlotChangeMessage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class ServerMessageHandler {
	
	
	   private static <T extends BaseMessage> boolean validateBasics(T message, NetworkEvent.Context ctx) {
	        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
	        ctx.setPacketHandled(true);
	        if (sideReceived != LogicalSide.SERVER) {
	            Hemomancy.LOGGER.error(message.getClass().getName() + " received on wrong side: " + sideReceived);
	            return false;
	        }
	        if (!message.isMessageValid()) {
	            Hemomancy.LOGGER.error(message.getClass().getName() + " was invalid: " + message);
	            return false;
	        }
	        return true;
	    }
	
	public static void handleRadialInventorySlotChangeMessage(RadialInventorySlotChangeMessage message,
			Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		if (!ServerMessageHandler.validateBasics(message, ctx)) {
			return;
		}
		ServerPlayer sendingPlayer = ctx.getSender();
		if (sendingPlayer == null) {
			Hemomancy.LOGGER.error("EntityPlayerMP was null when RadialInventorySlotChangeMessage was received");
			return;
		}
		ctx.enqueueWork(() -> {
			ItemStack stack;
			ItemStack itemStack = stack = message.isOffhand() ? sendingPlayer.getOffhandItem() : sendingPlayer.getMainHandItem();
			if (stack.getItem() instanceof IRadialInventorySelect) {
				((IRadialInventorySelect) stack.getItem()).setSlot((Player) sendingPlayer, stack, message.getSlot(),
						message.isOffhand(), false);
			}
		});
	}
}
