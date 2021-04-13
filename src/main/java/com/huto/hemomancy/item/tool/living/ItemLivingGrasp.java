package com.huto.hemomancy.item.tool.living;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbDirected;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbTracking;
import com.huto.hemomancy.font.ModTextFormatting;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemLivingGrasp extends Item {

	public ItemLivingGrasp(Properties properties) {
		super(properties);
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return 0;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);

		playerIn.setActiveHand(handIn);
		return ActionResult.resultConsume(itemstack);

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ITextComponent getDisplayName(ItemStack stack) {
		return new StringTextComponent(ModTextFormatting
				.stringToBloody(ModTextFormatting.convertInitToLang(stack.getItem().getRegistryName().getPath())))
						.mergeStyle(TextFormatting.DARK_RED);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entityLiving;
			IBloodVolume playerVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			if (playerVolume.getBloodVolume() > 50f) {
				if (!worldIn.isRemote) {
					playerVolume.subtractBloodVolume(50f);
					PacketHandler.CHANNELBLOODVOLUME.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
							new PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
									playerVolume.getBloodVolume()));
					if (worldIn.rand.nextInt(10) == 6) {

						player.sendStatusMessage(new StringTextComponent(
								TextFormatting.DARK_PURPLE + "Abuse of Power does not come without consequence"), true);
					}
					if (player.isCrouching()) {
						this.summonNoteStorm(worldIn.rand.nextInt(3), worldIn, player);
					} else {
						this.summonDirectedOrb(worldIn, player);
					}
				} else {
					player.playSound(SoundEvents.ENTITY_HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.2F,
							0.8F + (float) Math.random() * 0.2F);
				}

				stack.damageItem(1, player, (p_220009_1_) -> {
					p_220009_1_.sendBreakAnimation(player.getActiveHand());
				});
			} else {
				player.sendStatusMessage(new StringTextComponent("Not enough blood to be shed"), true);
			}
		}

		((PlayerEntity) entityLiving).addStat(Stats.ITEM_USED.get(this));

	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	public void summonDirectedOrb(World worldIn, PlayerEntity playerIn) {
		EntityBloodOrbDirected miss = new EntityBloodOrbDirected((PlayerEntity) playerIn, false);
		miss.setPosition(playerIn.getPosX() - 0.5, playerIn.getPosY() + 0.6, playerIn.getPosZ() - 0.5);
		miss.setDirectionAndMovement(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.0F, 1.0F);
		worldIn.addEntity(miss);
	}

	public void summomCorruptNote(World worldIn, PlayerEntity playerIn) {
		EntityBloodOrbTracking missile = new EntityBloodOrbTracking(playerIn, false);
		missile.setPosition(playerIn.getPosX() + (Math.random() - 0.5 * 0.1), playerIn.getPosY() + 0.8f,
				playerIn.getPosZ() + (Math.random() - 0.5 * 0.1));
		if (missile.findTarget()) {
			playerIn.playSound(SoundEvents.ENTITY_ENDERMAN_SCREAM, 0.6F, 0.8F + (float) Math.random() * 0.2F);
			worldIn.addEntity(missile);
		}
	}

	public void summonNoteStorm(int numMiss, World worldIn, PlayerEntity playerIn) {
		EntityBloodOrbTracking[] missArray = new EntityBloodOrbTracking[numMiss];
		for (int i = 0; i < numMiss; i++) {
			missArray[i] = new EntityBloodOrbTracking(playerIn, false);
			missArray[i].setPosition(playerIn.getPosX() + ((Math.random()) * 1.5), playerIn.getPosY() + 0.8f,
					playerIn.getPosZ() + ((Math.random()) * 1.5));
			if (!worldIn.isRemote) {
				playerIn.playSound(SoundEvents.ENTITY_ENDERMAN_SCREAM, 0.6F, 0.8F + (float) Math.random() * 0.2F);
				worldIn.addEntity(missArray[i]);

			}
		}
	}

	public void summonTentacleAid(int numTent, World world, PlayerEntity player, Vector3d hitVec) {
		EntityBloodOrbTracking[] tentArray = new EntityBloodOrbTracking[numTent];
		for (int i = 0; i < numTent; i++) {
			tentArray[i] = new EntityBloodOrbTracking(player, false);
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
