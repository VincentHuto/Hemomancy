package com.vincenthuto.hemomancy.common.network.morphling;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.item.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.itemhandler.MorphlingJarItemHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.NetworkEvent;

public class ChangeMorphKeyPacket {

	public static class Handler {
		public static void handle(final ChangeMorphKeyPacket msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayer player = ctx.get().getSender();
				if (player == null)
					return;

				ItemStack jar = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
				if (!(jar.getItem() instanceof ItemMorphlingJar))
					return;

				IItemHandler rawHandler = jar.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
				if (!(rawHandler instanceof MorphlingJarItemHandler jarHandler))
					return;

				jarHandler.load();

				// Collect non-empty morphling slots
				List<Integer> validSlots = new ArrayList<>();
				for (int i = 0; i < jarHandler.getSlots(); i++) {
					ItemStack s = jarHandler.getStackInSlot(i);
					if (!s.isEmpty() && s.getItem() instanceof IMorphling) {
						validSlots.add(i);
					}
				}
				if (validSlots.isEmpty())
					return;

				Random rand = new Random();
				int chosen = validSlots.get(rand.nextInt(validSlots.size()));
				ItemStack selectedStack = jarHandler.getStackInSlot(chosen);

				// Equip to player capability
				HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
					cap.setEquippedMorphling(selectedStack.copy());
				});

				// Award morphling bond milestone
				SkillPointGainEvents.onMorphlingEquipped(player);

				EquippedMorphlingEvents.syncToClient(player);
			});
			ctx.get().setPacketHandled(true);
		}
	}

	public static ChangeMorphKeyPacket decode(FriendlyByteBuf buf) {
		return new ChangeMorphKeyPacket();
	}

	public static void encode(ChangeMorphKeyPacket msg, FriendlyByteBuf buf) {
	}

	public ChangeMorphKeyPacket() {

	}
}