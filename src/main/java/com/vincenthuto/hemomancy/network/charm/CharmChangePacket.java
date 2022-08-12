package com.vincenthuto.hemomancy.network.charm;

import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.capa.player.charm.CharmFinder;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class CharmChangePacket {
	public int player;
	public String where;
	public JsonElement slot;
	public ItemStack stack;

	public CharmChangePacket(LivingEntity player, String where, JsonElement slot, ItemStack stack) {
		this.player = player.getId();
		this.where = where;
		this.slot = slot;
		this.stack = stack.copy();
	}

	public CharmChangePacket(FriendlyByteBuf buf) {
		player = buf.readVarInt();
		where = buf.readUtf();
		slot = (new JsonParser()).parse(buf.readUtf(2048));
		stack = buf.readItem();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(player);
		buf.writeUtf(where);
		buf.writeUtf(slot.toString(), 2048);
		buf.writeItem(stack);
	}

	public boolean handle(Supplier<NetworkEvent.Context> context) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.execute(() -> {
			Entity entity = minecraft.level.getEntity(this.player);
			if (!(entity instanceof Player))
				return;
			Player player = (Player) entity;
			CharmFinder.setCharmFromPacket(player, this.where, this.slot, this.stack);
		});
		return true;
	}
}