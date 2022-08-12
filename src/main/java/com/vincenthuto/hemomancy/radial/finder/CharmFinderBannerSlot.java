package com.vincenthuto.hemomancy.radial.finder;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.vincenthuto.hemomancy.item.ItemVasculariumCharm;
import com.vincenthuto.hemomancy.network.PacketHandler;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.network.PacketDistributor;

public class CharmFinderBannerSlot extends CharmFinder {

    public static final Capability<CharmExtensionSlot> BANNER_SLOT_ITEM =CapabilityManager.get(new CapabilityToken<>(){});
	
	public static void initFinder() {
		CharmFinder.addFinder(new CharmFinderBannerSlot());
	}

	@Override
	protected Optional<CharmGetter> getSlotFromId(Player player, JsonElement packetData) {
		return CharmExtensionSlot.get(player).resolve().map(CharmExtensionSlot::getSlots)
				.map(slots -> slots.get(packetData.getAsInt()))
				.map(slot -> new ExtensionSlotCharmGetter(player, slot));
	}

	@Override
	public String getName() {
		return "charm_slot";
	}

	@Override
	public Optional<? extends CharmGetter> findStack(LivingEntity player, boolean allowCosmetic) {
		return CharmExtensionSlot.get(player).resolve()
				.flatMap(ext -> ext.getSlots().stream()
						.filter(slot -> slot.getContents().getItem() instanceof ItemVasculariumCharm)
						.map(slot -> new ExtensionSlotCharmGetter(player, slot)).findFirst());
	}

	private static class ExtensionSlotCharmGetter implements CharmGetter {
		@SuppressWarnings("unused")
		private final LivingEntity player;
		private final CharmSlotItemHandler slot;

		private ExtensionSlotCharmGetter(LivingEntity player, CharmSlotItemHandler slot) {
			this.player = player;
			this.slot = slot;
		}

		@Override
		public ItemStack getCharm() {
			return slot.getContents();
		}

		@Override
		public void setCharm(ItemStack stack) {
			slot.setContents(stack);
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public void syncToClients() {
			LivingEntity thePlayer = slot.getContainer().getOwner();
			if (thePlayer.level.isClientSide)
				return;
			PacketCharmChange message = new PacketCharmChange(thePlayer, "charm_slot", new JsonPrimitive(0),
					slot.getContents());
			PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> thePlayer),
					message);
		}
	}
}
