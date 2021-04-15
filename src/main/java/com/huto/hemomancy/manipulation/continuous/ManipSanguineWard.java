package com.huto.hemomancy.manipulation.continuous;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class ManipSanguineWard extends BloodManipulation {

	public ManipSanguineWard(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void performAction(PlayerEntity player, ServerWorld world, ItemStack heldItemMainhand, BlockPos position) {
		/*
		 * if (e.getEntityLiving() instanceof PlayerEntity) { PlayerEntity player =
		 * (PlayerEntity) e.getEntityLiving(); IRunesItemHandler itemHandler =
		 * player.getCapability(RunesCapabilities.RUNES)
		 * .orElseThrow(NullPointerException::new); if
		 * (itemHandler.getStackInSlot(0).getItem() == ItemInit.rune_radiance_c.get()) {
		 * double dist = e.getEntityLiving().getDistance(player); RayTraceResult trace =
		 * e.getEntityLiving().pick(dist, 0, false);
		 * PacketHandler.CHANNELBLOODVOLUME.sendToServer( new
		 * PacketEntityHitParticle(trace.getHitVec().x, trace.getHitVec().y,
		 * trace.getHitVec().z)); } if
		 * (player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem().getItem() ==
		 * ItemInit.chitinite_chestplate .get()) { e.setAmount((float) (e.getAmount() *
		 * 0.25)); double dist = e.getEntityLiving().getDistance(player); RayTraceResult
		 * trace = e.getEntityLiving().pick(dist, 0, false);
		 * PacketHandler.CHANNELBLOODVOLUME.sendToServer( new
		 * PacketEntityHitParticle(trace.getHitVec().x, trace.getHitVec().y,
		 * trace.getHitVec().z)); }
		 * 
		 * }
		 */
	}

}
