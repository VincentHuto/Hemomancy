package com.vincenthuto.hemomancy.radial;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncCharmSlotContents {
	public final NonNullList<ItemStack> stacks = NonNullList.create();
	public int entityId;

	public PacketSyncCharmSlotContents(Player player, CharmExtensionSlot extension) {
		this.entityId = player.getId();
		extension.getSlots().stream().map(CharmSlotItemHandler::getContents).forEach(stacks::add);
	}

	public PacketSyncCharmSlotContents(FriendlyByteBuf buf) {
		entityId = buf.readVarInt();
		int numStacks = buf.readVarInt();
		for (int i = 0; i < numStacks; i++) {
			stacks.add(buf.readItem());
		}
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		buf.writeVarInt(stacks.size());
		for (ItemStack stack : stacks) {
			buf.writeItem(stack);
		}
	}

	public boolean handle(Supplier<NetworkEvent.Context> context) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.execute(() -> {
			Entity entity = minecraft.level.getEntity(this.entityId);
			if (entity instanceof Player) {
				CharmExtensionSlot.get((LivingEntity) entity).ifPresent((slot) -> slot.setAll(this.stacks));
			}
		});
		return true;
	}
}
