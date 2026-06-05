package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class SummonAvatarManip extends BloodManipulation {

	public SummonAvatarManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player playerIn, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (playerIn instanceof ServerPlayer serverPlayer) {
			HemoCapabilityAccess.getKnownManipulations(serverPlayer).ifPresent((manip) -> {
				manip.setAvatarActive(!manip.isAvatarActive());
				PacketHandler.sendToPlayer(serverPlayer, new KnownManipulationServerPacket(manip));
				List<ServerPlayer> receivers = new ArrayList<>(((ServerLevel) serverPlayer.level()).players());
				KnownManipulationEvents.syncAvatar(serverPlayer, receivers, manip.isAvatarActive());
			});

		}
	}

}
