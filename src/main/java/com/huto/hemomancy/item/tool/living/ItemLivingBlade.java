package com.huto.hemomancy.item.tool.living;

import java.util.List;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.huto.hemomancy.render.item.RenderItemLivingBlade;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemLivingBlade extends ItemLivingWeapon {

	public static String TAG_STATE = "state";

	public ItemLivingBlade(float speedIn, float attackDamageIn, IItemTier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn.setISTER(() -> RenderItemLivingBlade::new));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItemMainhand();
		if (stack.getItem() instanceof ItemLivingBlade) {
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
