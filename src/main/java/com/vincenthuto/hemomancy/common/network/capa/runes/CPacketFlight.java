package com.vincenthuto.hemomancy.common.network.capa.runes;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.init.AttributeInit;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.NetworkEvent;

public final class CPacketFlight {

	public static void encode(CPacketFlight msg, FriendlyByteBuf buf) {
	}

	public static CPacketFlight decode(FriendlyByteBuf buf) {
		return new CPacketFlight();
	}

	  @SuppressWarnings("ConstantConditions")
	  public static void handle(CPacketFlight msg, Supplier<NetworkEvent.Context> ctx) {
	    ctx.get().enqueueWork(() -> {
	      ServerPlayer sender = ctx.get().getSender();

	      if (sender != null) {
	        sender.stopFallFlying();

	        if (!sender.onGround() && !sender.isFallFlying() && !sender.isInWater() && !sender
	            .hasEffect(MobEffects.LEVITATION) && AttributeInit.canFly(sender)) {
	          sender.startFallFlying();
	        }
	      }
	    });
	    ctx.get().setPacketHandled(true);
	  }
	}
