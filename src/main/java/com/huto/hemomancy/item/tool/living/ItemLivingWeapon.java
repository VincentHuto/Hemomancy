package com.huto.hemomancy.item.tool.living;

import java.util.Set;

import com.google.common.collect.Sets;
import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemLivingWeapon extends ToolItem {
	private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet(Blocks.COBWEB);
	private float speed;

	public ItemLivingWeapon(float speedIn, float attackDamageIn, float attackSpeedIn, IItemTier tier,
			Properties builderIn) {
		super(attackDamageIn, attackSpeedIn, tier, EFFECTIVE_ON, builderIn);
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (target.world.rand.nextBoolean()) {
			if (attacker instanceof PlayerEntity) {
				if (!attacker.world.isRemote) {
					PlayerEntity playerIn = (PlayerEntity) attacker;
					IBloodVolume playerVolume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					float damageMod = this.getAttackDamage() * 25f;
					if (playerVolume.getBloodVolume() > damageMod) {
						playerVolume.subtractBloodVolume(damageMod);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
								new PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
										playerVolume.getBloodVolume()));
					} else {
						playerVolume.subtractBloodVolume(damageMod);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
								new PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
										playerVolume.getBloodVolume()));
						stack.damageItem(getMaxDamage() + 10, attacker, (p_220017_1_) -> {
							p_220017_1_.sendBreakAnimation(attacker.getActiveHand());
						});
					}

				}
			}
		}

		if (target.world.rand.nextDouble() > 0.75) {
			attacker.addPotionEffect(new EffectInstance(PotionInit.blood_rush.get(), 50, 2));
			target.addPotionEffect(new EffectInstance(PotionInit.blood_loss.get(), 50, 2));
		}
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public boolean canHarvestBlock(BlockState blockIn) {
		if (EFFECTIVE_ON.contains(blockIn.getBlock())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (EFFECTIVE_ON.contains(state.getBlock())) {
			return speed;
		} else {
			return 0.5f;
		}
	}
}
