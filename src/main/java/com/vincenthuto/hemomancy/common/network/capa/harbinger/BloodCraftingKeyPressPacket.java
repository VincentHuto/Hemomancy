package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivation;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.structure.BloodStructureCrafting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
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
		return new BloodCraftingKeyPressPacket();
	}

	public static void encode(final FriendlyByteBuf buffer, final BloodCraftingKeyPressPacket message) {
		buffer.writeByte(0);
	}

	public static void handle(final BloodCraftingKeyPressPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			// The server reads the held item itself; the client's copy was never trusted and is no longer sent (RT-23).
			if (!(player instanceof ServerPlayer sender) || !ServerboundGuards.activePlayer(sender)
					|| !ServerboundRateLimiter.tryAcquire(sender, "blood_crafting", 4)) return;

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

	public BloodCraftingKeyPressPacket() {
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
