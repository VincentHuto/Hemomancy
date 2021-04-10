package com.huto.hemomancy.item;

import java.util.List;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.capa.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capa.tendency.IBloodTendency;
import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.huto.hemomancy.render.item.RenderParticleItem;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemParticleItem extends Item {

	public ItemParticleItem(Properties prop) {
		super(prop.maxStackSize(1).setISTER(() -> RenderParticleItem::new));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		IKnownManipulations known = playerIn.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);
		IBloodTendency tendency = playerIn.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
				.orElseThrow(NullPointerException::new);
		IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		List<BloodManipulation> knownList = known.getKnownManips();
		/*
		 * if (!worldIn.isRemote) { if (!playerIn.isSneaking()) { //
		 * knownList.add(ManipulationInit.blood_shot);
		 * 
		 * tendency.setTendencyAlignment(EnumBloodTendency.CONGEATIO, 0);
		 * 
		 * PacketHandler.CHANNELBLOODTENDENCY.send( PacketDistributor.PLAYER.with(() ->
		 * (ServerPlayerEntity) playerIn), new
		 * PacketBloodTendencyServer(tendency.getTendency()));
		 * 
		 * } else { knownList.add(ManipulationInit.blood_shot); // knownList.clear();
		 * PacketHandler.CHANNELKNOWNMANIPS.send( PacketDistributor.PLAYER.with(() ->
		 * (ServerPlayerEntity) playerIn), new PacketKnownManipulationServer(knownList,
		 * known.getSelectedManip())); } }
		 */
		if (volume.getBloodVolume() < volume.getMaxBloodVolume()) {
		playerIn.setActiveHand(handIn);
		new ActionResult<>(ActionResultType.SUCCESS, stack);
		}

		return new ActionResult<>(ActionResultType.FAIL, stack);
	}

	DamageSource bloodLoss = new DamageSource("bloodloss");

	@Override
	public void onUse(World worldIn, LivingEntity player, ItemStack stack, int count) {
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
			List<Entity> targets = player.world.getEntitiesWithinAABBExcludingEntity(player,
					player.getBoundingBox().grow(5.0));
			if (targets.size() > 0) {
				for (int i = 0; i < targets.size(); ++i) {
					Entity target = targets.get(i);
					if (target instanceof LivingEntity) {
						LivingEntity livingTarget = (LivingEntity) target;
						float dam = 3f / targets.size();
						livingTarget.attackEntityFrom(bloodLoss, dam);
						if (!worldIn.isRemote) {
							volume.addBloodVolume(dam);
							PacketHandler.CHANNELBLOODVOLUME.send(
									PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
									new PacketBloodVolumeServer(volume.getMaxBloodVolume(), volume.getBloodVolume()));
						}
					}
				}
			}
		}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.NONE;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {

	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("Flashy"));
	}

}
