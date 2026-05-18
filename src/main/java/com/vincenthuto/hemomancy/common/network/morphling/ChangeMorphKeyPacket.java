package com.vincenthuto.hemomancy.common.network.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.itemhandler.MorphlingJarItemHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChangeMorphKeyPacket implements CustomPacketPayload {

	public static final Type<ChangeMorphKeyPacket> TYPE = new Type<>(Hemomancy.rloc("change_morph_key_packet"));
	public static final StreamCodec<FriendlyByteBuf, ChangeMorphKeyPacket> STREAM_CODEC = StreamCodec.of(ChangeMorphKeyPacket::encode, ChangeMorphKeyPacket::decode);
	public static void handle(final ChangeMorphKeyPacket msg, final IPayloadContext ctx) {
		Handler.handle(msg, ctx);
	}

	public static class Handler {
		public static void handle(final ChangeMorphKeyPacket msg, final IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				Player packetPlayer = ctx.player();
				if (!(packetPlayer instanceof ServerPlayer player))
					return;

				ItemStack jar = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
				if (!(jar.getItem() instanceof ItemMorphlingJar))
					return;

				IItemHandler rawHandler = jar.getCapability(Capabilities.ItemHandler.ITEM);
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
		}
	}

	public static ChangeMorphKeyPacket decode(FriendlyByteBuf buf) {
		return new ChangeMorphKeyPacket();
	}

	public static void encode(FriendlyByteBuf buf, ChangeMorphKeyPacket msg) {
	}

	public ChangeMorphKeyPacket() {

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
