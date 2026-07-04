package com.vincenthuto.hemomancy.common.network.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.itemhandler.MorphlingJarItemHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketUpdateLivingStaffMorph implements CustomPacketPayload {

	public static final Type<PacketUpdateLivingStaffMorph> TYPE = new Type<>(Hemomancy.rloc("packet_update_living_staff_morph"));
	public static final StreamCodec<FriendlyByteBuf, PacketUpdateLivingStaffMorph> STREAM_CODEC = StreamCodec.of(PacketUpdateLivingStaffMorph::encode, PacketUpdateLivingStaffMorph::decode);
	public static void handle(final PacketUpdateLivingStaffMorph msg, final IPayloadContext ctx) {
		Handler.handle(msg, ctx);
	}

	public static class Handler {
		public static void handle(final PacketUpdateLivingStaffMorph msg, final IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				if (!(ctx.player() instanceof ServerPlayer player))
					return;

				// ── Unequip: selected == -1 means clear the equipped morphling ──────────
				if (msg.selected == -1) {
					HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
						cap.clearMorphling();
					});
					LastRiteHelper.clearMorphlingRites(player);
					EquippedMorphlingEvents.syncToClient(player);
					return;
				}

				// ── Locate the morphling jar (inventory, offhand, or scar slot 7) ───────
				ItemStack jarStack = findJar(player);
				if (jarStack.isEmpty())
					return;

				var itemHandler = jarStack.getCapability(Capabilities.ItemHandler.ITEM);
				if (!(itemHandler instanceof MorphlingJarItemHandler jarHandler))
					return;
				jarHandler.load();

				// ── Validate the requested index ─────────────────────────────────────────
				int idx = msg.selected;
				if (idx < 0 || idx >= jarHandler.getSlots())
					return;

				ItemStack fromJar = jarHandler.getStackInSlot(idx);
				if (fromJar.isEmpty() || !(fromJar.getItem() instanceof IMorphling))
					return;

				// ── Equip to player capability ───────────────────────────────────────────
				ItemStack equippedMorphling = fromJar.copy();
				HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
					cap.setEquippedMorphling(equippedMorphling);
				});
				MorphlingItem.markEquipped(player, equippedMorphling);
				LastRiteHelper.armForMorphling(player, equippedMorphling);

				// Award morphling bond milestone
				SkillPointGainEvents.onMorphlingEquipped(player);

				// Sync to client
				EquippedMorphlingEvents.syncToClient(player);
			});
		}

		/** Finds the jar in hand, offhand, scar slot 7, or anywhere in inventory. */
		private static ItemStack findJar(Player player) {
			// Main / offhand
			ItemStack jar = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
			if (!jar.isEmpty())
				return jar;
			// Scar equip slot 7
			return HemoCapabilityAccess.getEquipment(player).map(r -> r.getStackInSlot(7))
					.filter(s -> s.getItem() instanceof ItemMorphlingJar).orElse(ItemStack.EMPTY);
		}
	}

	// ─── Codec ───────────────────────────────────────────────────────────────────

	public static PacketUpdateLivingStaffMorph decode(FriendlyByteBuf buf) {
		return new PacketUpdateLivingStaffMorph(buf.readInt());
	}

	public static void encode(FriendlyByteBuf buf, PacketUpdateLivingStaffMorph msg) {
		buf.writeInt(msg.selected);
	}

	private final int selected;

	public PacketUpdateLivingStaffMorph(int selectedIn) {
		this.selected = selectedIn;

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
