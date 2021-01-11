package com.huto.hemomancy.item.tool;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.entity.EntityIronPillar;
import com.huto.hemomancy.entity.EntityIronSpike;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.BloodVolumePacketServer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemIronRod extends Item {

	public ItemIronRod(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);

		playerIn.setActiveHand(handIn);
		return ActionResult.resultConsume(itemstack);

	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof PlayerEntity) {
			PlayerEntity playerentity = (PlayerEntity) entityLiving;

			if (!worldIn.isRemote) {
				RayTraceResult result = entityLiving.pick(100, 0, false);
				Vector3d hitVec = result.getHitVec();
				PlayerEntity player = (PlayerEntity) entityLiving;
				IBloodVolume playerVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				if (playerVolume.getBloodVolume() > 50f) {
					if (!worldIn.isRemote) {
						if (worldIn.rand.nextInt(10) == 6) {

							playerVolume.subtractBloodVolume(50f);
							PacketHandler.CHANNELBLOODVOLUME.send(
									PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
									new BloodVolumePacketServer(playerVolume.getBloodVolume()));
							playerentity.sendStatusMessage(
									new StringTextComponent(
											TextFormatting.YELLOW + "Abuse of Power does not come without consequence"),
									true);
						}
						if(!player.isSneaking()) {
						this.summonIronSpike(worldIn.rand.nextInt(10), worldIn, (PlayerEntity) entityLiving, hitVec);
						}else {
							this.summonIronPillar(worldIn.rand.nextInt(10), worldIn, (PlayerEntity) entityLiving, hitVec);

						}
						
					}
				} else {
					playerentity.sendStatusMessage(new StringTextComponent("Lord Hastur does not grant you his power"),
							true);
				}

				stack.damageItem(1, playerentity, (p_220009_1_) -> {
					p_220009_1_.sendBreakAnimation(playerentity.getActiveHand());
				});
			}

			playerentity.addStat(Stats.ITEM_USED.get(this));
		}
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	public void summonIronSpike(int numTent, World world, PlayerEntity player, Vector3d hitVec) {
		EntityIronSpike[] tentArray = new EntityIronSpike[numTent];
		for (int i = 0; i < numTent; i++) {
			tentArray[i] = new EntityIronSpike(EntityInit.iron_spike.get(), world);
			float xMod = (world.rand.nextFloat() - 0.5F) * 8.0F;
			float yMod = (world.rand.nextFloat() - 0.5F) * 4.0F;
			float zMod = (world.rand.nextFloat() - 0.5F) * 8.0F;
			tentArray[i].setPosition(hitVec.getX() + 0.5 + xMod, hitVec.getY() + 1.5 + yMod,
					hitVec.getZ() + 0.5 + zMod);
			if (!world.isRemote) {
				world.addEntity(tentArray[i]);

			}
		}
	}
	public void summonIronPillar(int numTent, World world, PlayerEntity player, Vector3d hitVec) {
		EntityIronPillar[] tentArray = new EntityIronPillar[numTent];
		for (int i = 0; i < numTent; i++) {
			tentArray[i] = new EntityIronPillar(EntityInit.iron_pillar.get(), world);
			float xMod = (world.rand.nextFloat() - 0.5F) * 8.0F;
			float yMod = (world.rand.nextFloat() - 0.5F) * 4.0F;
			float zMod = (world.rand.nextFloat() - 0.5F) * 8.0F;
			tentArray[i].setPosition(hitVec.getX() + 0.5 + xMod, hitVec.getY() + 1.5 + yMod,
					hitVec.getZ() + 0.5 + zMod);
			if (!world.isRemote) {
				world.addEntity(tentArray[i]);

			}
		}
	}
}
