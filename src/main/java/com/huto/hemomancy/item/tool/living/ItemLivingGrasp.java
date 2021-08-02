package com.huto.hemomancy.item.tool.living;

import org.apache.http.util.TextUtils;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbDirected;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbTracking;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.mojang.math.Vector3d;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ItemLivingGrasp extends Item {

	public ItemLivingGrasp(Properties properties) {
		super(properties);
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack itemstack = playerIn.getItemInHand(handIn);

		playerIn.startUsingItem(handIn);
		return InteractionResultHolder.consume(itemstack);

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return new TextComponent(
				TextUtils.stringToBloody(TextUtils.convertInitToLang(stack.getItem().getRegistryName().getPath())))
						.withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof Player) {
			Player player = (Player) entityLiving;
			IBloodVolume playerVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			if (playerVolume.getBloodVolume() > 50f) {
				if (!worldIn.isClientSide) {
					playerVolume.subtractBloodVolume(50f);
					PacketHandler.CHANNELBLOODVOLUME.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
							new PacketBloodVolumeServer(playerVolume));
					if (worldIn.random.nextInt(10) == 6) {

						player.displayClientMessage(new TextComponent(
								ChatFormatting.DARK_PURPLE + "Abuse of Power does not come without consequence"), true);
					}
					if (player.isCrouching()) {
						this.summonNoteStorm(worldIn.random.nextInt(3), worldIn, player);
					} else {
						this.summonDirectedOrb(worldIn, player);
					}
				} else {
					player.playSound(SoundEvents.HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.2F,
							0.8F + (float) Math.random() * 0.2F);
				}

				stack.hurtAndBreak(1, player, (p_220009_1_) -> {
					p_220009_1_.broadcastBreakEvent(player.getUsedItemHand());
				});
			} else {
				player.displayClientMessage(new TextComponent("Not enough blood to be shed"), true);
			}
		}

		((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));

	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	public void summonDirectedOrb(Level worldIn, Player playerIn) {
		EntityBloodOrbDirected miss = new EntityBloodOrbDirected((Player) playerIn, false);
		miss.setPos(playerIn.getX() - 0.5, playerIn.getY() + 0.6, playerIn.getZ() - 0.5);
		miss.shootFromRotation(playerIn, playerIn.xRot, playerIn.yRot, 0.0F, 1.0F, 1.0F);
		worldIn.addFreshEntity(miss);
	}

	public void summomCorruptNote(Level worldIn, Player playerIn) {
		EntityBloodOrbTracking missile = new EntityBloodOrbTracking(playerIn, false);
		missile.setPos(playerIn.getX() + (Math.random() - 0.5 * 0.1), playerIn.getY() + 0.8f,
				playerIn.getZ() + (Math.random() - 0.5 * 0.1));
		if (missile.findTarget()) {
			playerIn.playSound(SoundEvents.ENDERMAN_SCREAM, 0.6F, 0.8F + (float) Math.random() * 0.2F);
			worldIn.addFreshEntity(missile);
		}
	}

	public void summonNoteStorm(int numMiss, Level worldIn, Player playerIn) {
		EntityBloodOrbTracking[] missArray = new EntityBloodOrbTracking[numMiss];
		for (int i = 0; i < numMiss; i++) {
			missArray[i] = new EntityBloodOrbTracking(playerIn, false);
			missArray[i].setPos(playerIn.getX() + ((Math.random()) * 1.5), playerIn.getY() + 0.8f,
					playerIn.getZ() + ((Math.random()) * 1.5));
			if (!worldIn.isClientSide) {
				playerIn.playSound(SoundEvents.ENDERMAN_SCREAM, 0.6F, 0.8F + (float) Math.random() * 0.2F);
				worldIn.addFreshEntity(missArray[i]);

			}
		}
	}

	public void summonTentacleAid(int numTent, Level world, Player player, Vector3d hitVec) {
		EntityBloodOrbTracking[] tentArray = new EntityBloodOrbTracking[numTent];
		for (int i = 0; i < numTent; i++) {
			tentArray[i] = new EntityBloodOrbTracking(player, false);
			float xMod = (world.random.nextFloat() - 0.5F) * 8.0F;
			float yMod = (world.random.nextFloat() - 0.5F) * 4.0F;
			float zMod = (world.random.nextFloat() - 0.5F) * 8.0F;
			tentArray[i].setPos(hitVec.x() + 0.5 + xMod, hitVec.y() + 1.5 + yMod, hitVec.z() + 0.5 + zMod);
			if (!world.isClientSide) {
				world.addFreshEntity(tentArray[i]);

			}
		}
	}

}
