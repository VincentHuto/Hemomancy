package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class LivingBladeItem extends LivingToolItem {
	/** Pays whatever is asked, emptying the volume when short; F-15 (pending) would gate and stop that. */
	private static final BloodAccounting.Source BLOOD_PRICE = BloodAccounting.Source.of("living_blade", Category.TOOL, false, false);

	public static String TAG_STATE = "state";

	public LivingBladeItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, EnumBloodTendency.ANIMUS, tier, builderIn);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			if (stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(TAG_STATE)) {
				tooltip.add(Component.translatable("tooltip.hemomancy.living_blade.state_unleashed").withStyle(ChatFormatting.RED));
				tooltip.add(Component.translatable("tooltip.hemomancy.living_blade.20_blood_damage").withStyle(ChatFormatting.RED));

			} else {
				tooltip.add(Component.translatable("tooltip.hemomancy.living_blade.state_tame").withStyle(ChatFormatting.GRAY));
				tooltip.add(Component.translatable("tooltip.hemomancy.living_blade.5_blood_damage").withStyle(ChatFormatting.RED));

			}
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (LivingStaffWeaponFormHelper.wasRestoredOutOfHand(stack, attacker)) {
			return true;
		}
		if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(TAG_STATE)) {
			attacker.heal(this.getLivingAttackDamage() / 2);
			target.hurt(TendencyWeaponHelper.createWeaponDamageSource(target, attacker), 20);
			if (!attacker.level().isClientSide && attacker instanceof Player playerIn) {
				IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(playerIn)
						;
				float damageMod = this.getLivingAttackDamage() * 75f;
				if (playerVolume.getBloodVolume() > damageMod) {
					BloodAccounting.pay(playerIn, damageMod, BLOOD_PRICE);
				} else {
					BloodAccounting.pay(playerIn, damageMod, BLOOD_PRICE);
					if (!LivingStaffWeaponFormHelper.restoreStaffAfterBloodFailure(stack, attacker)) {
						stack.hurtAndBreak(stack.getMaxDamage() + 10, attacker, EquipmentSlot.MAINHAND);
					}
				}

			}
		} else {
			target.hurt(TendencyWeaponHelper.createWeaponDamageSource(target, attacker), 5);

		}
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getMainHandItem();
		if (stack.getItem() instanceof LivingBladeItem) {
			CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (!compound.getBoolean(TAG_STATE)) {
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			} else {
				playerIn.playSound(SoundEvents.BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			}
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
		}
		return super.use(worldIn, playerIn, handIn);
	}
}

