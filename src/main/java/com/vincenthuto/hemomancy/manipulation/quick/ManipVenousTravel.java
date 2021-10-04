package com.vincenthuto.hemomancy.manipulation.quick;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketKnownManipulationServer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ManipVenousTravel extends BloodManipulation {

	public ManipVenousTravel(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);
		
//		RegistryKey<World> key = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, dimRL);
//		ServerWorld ovw = worldIn.getServer().getWorld(key);
//		serverPlayer.teleport(ovw, bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5,
//				serverPlayer.rotationYaw, serverPlayer.rotationPitch);
//		

		
		System.out.println(known.getVeinList());
		PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
				new PacketKnownManipulationServer(known));
	}

}
