package com.vincenthuto.hemomancy.common.manipulation.quick;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.KnownManipulationServerPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class SummonAvatarManip extends BloodManipulation {

	public SummonAvatarManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player playerIn, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (playerIn.isAddedToWorld()) {
			playerIn.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
				manip.setAvatarActive(!manip.isAvatarActive());
				PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
						new KnownManipulationServerPacket(manip));
				List<ServerPlayer> receivers = new ArrayList<>(((ServerLevel) playerIn.level()).players());
				KnownManipulationEvents.syncAvatar(playerIn, receivers, manip.isAvatarActive());
			});

		}
	}

}
