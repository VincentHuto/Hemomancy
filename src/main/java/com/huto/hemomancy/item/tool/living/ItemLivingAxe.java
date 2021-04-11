package com.huto.hemomancy.item.tool.living;

import java.util.List;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.huto.hemomancy.render.item.RenderItemLivingAxe;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemLivingAxe extends ItemLivingWeapon {

	public static String TAG_STATE = "state";

	public ItemLivingAxe(float speedIn, float attackDamageIn, IItemTier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn.setISTER(() -> RenderItemLivingAxe::new));
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			if (stack.getItem() instanceof ItemLivingAxe) {
				CompoundNBT compound = stack.getOrCreateTag();
				if (compound.getBoolean(TAG_STATE)) {
					if (player.isOnGround()) {
						player.setVelocity(0, .85, 0);
						List<Entity> targets = player.world.getEntitiesWithinAABBExcludingEntity(player,
								player.getBoundingBox().grow(5.0));
						if (targets.size() > 0) {
							for (int i = 0; i < targets.size(); ++i) {
								Entity target = targets.get(i);
								if (target instanceof LivingEntity) {
									LivingEntity livingTarget = (LivingEntity) target;
									float dam = 3f / targets.size();
									DamageSource bloodLoss = new DamageSource("bloodloss");
									livingTarget.attackEntityFrom(bloodLoss, dam);
								}
							}
						}
					}
				}
			}

		}
		return super.onEntitySwing(stack, entity);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItemMainhand();
		if (stack.getItem() instanceof ItemLivingAxe) {
			CompoundNBT compound = stack.getOrCreateTag();
			if (!compound.getBoolean(TAG_STATE)) {
				playerIn.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			} else {
				playerIn.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			}
			stack.setTag(compound);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hitEntity(stack, target, attacker);
		if (stack.getOrCreateTag().getBoolean(TAG_STATE)) {
			attacker.heal(this.getAttackDamage() / 2);
			if (!attacker.world.isRemote) {
				PlayerEntity playerIn = (PlayerEntity) attacker;
				IBloodVolume playerVolume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				float damageMod = this.getAttackDamage() * 75f;
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
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			if (stack.getTag().getBoolean(TAG_STATE)) {
				tooltip.add(new TranslationTextComponent("State: Unleashed").mergeStyle(TextFormatting.RED));
			} else {
				tooltip.add(new TranslationTextComponent("State: Tame").mergeStyle(TextFormatting.GRAY));
			}
		}
	}

}
