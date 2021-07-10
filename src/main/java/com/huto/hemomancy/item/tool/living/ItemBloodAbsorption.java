package com.huto.hemomancy.item.tool.living;

import java.util.List;

import com.huto.hemomancy.ClientProxy;
import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.capa.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capa.tendency.IBloodTendency;
import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.huto.hemomancy.render.item.RenderItemCellHand;

import net.minecraft.client.renderer.model.IBakedModel;
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
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemBloodAbsorption extends Item implements IDispellable, ICellHand {

	public ItemBloodAbsorption(Properties prop) {
		super(prop.maxStackSize(1).setISTER(() -> RenderItemCellHand::new));
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return 0;
	}

	@SuppressWarnings("unused")
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
		if (volume.isActive()) {
			if (volume.getBloodVolume() < volume.getMaxBloodVolume()) {
				playerIn.setActiveHand(handIn);
				new ActionResult<>(ActionResultType.SUCCESS, stack);
			}
		} else {
			playerIn.sendStatusMessage(new StringTextComponent("You lack the skill to manifest this power!")
					.mergeStyle(TextFormatting.RED), true);
		}

		return new ActionResult<>(ActionResultType.FAIL, stack);
	}

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
					livingTarget.attackEntityFrom(ItemInit.bloodLoss, dam);
					if (!worldIn.isRemote) {
						volume.addBloodVolume(dam);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
								new PacketBloodVolumeServer(volume));
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
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public IBakedModel getBakedModel() {
		// TODO Auto-generated method stub
		return ClientProxy.bloodAbsorptionModel;
	}

}
