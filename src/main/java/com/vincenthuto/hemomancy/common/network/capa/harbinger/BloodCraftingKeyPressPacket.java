package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivation;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.structure.BloodStructureCrafting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * The blood crafting key. The server looks where the player looks: an Unstained Blood Structure there is crafted
 * (or explained); otherwise an Unstained rite may start. Harbinger rites need their progression-appropriate item use.
 */
public class BloodCraftingKeyPressPacket implements CustomPacketPayload {
	public static final Type<BloodCraftingKeyPressPacket> TYPE = new Type<>(Hemomancy.rloc("blood_crafting_key_press_packet"));
	public static final StreamCodec<FriendlyByteBuf, BloodCraftingKeyPressPacket> STREAM_CODEC = StreamCodec.of(BloodCraftingKeyPressPacket::encode, BloodCraftingKeyPressPacket::decode);

	public static BloodCraftingKeyPressPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new BloodCraftingKeyPressPacket(ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer));
	}

	public static void encode(final FriendlyByteBuf buffer, final BloodCraftingKeyPressPacket message) {
		buffer.writeByte(0);
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, message.heldStack);
	}

	public static void handle(final BloodCraftingKeyPressPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;

			HitResult rayTrace = player.pick(3, 102, false);
			boolean handled = BloodStructureCrafting.tryCraft(player, rayTrace);

			// Unstained rites retain their existing key activation. Harbinger
			// rites deliberately require their progression-appropriate item use.
			if (!handled && rayTrace instanceof BlockHitResult blockResult) {
				CardinalRiteActivation.tryStart(player, blockResult.getBlockPos(),
						CardinalRiteActivationRules.Trigger.BLOOD_CRAFTING_KEY);
			}
		});
	}

	public ItemStack heldStack;

	public BloodCraftingKeyPressPacket(ItemStack stack) {
		this.heldStack = stack;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
