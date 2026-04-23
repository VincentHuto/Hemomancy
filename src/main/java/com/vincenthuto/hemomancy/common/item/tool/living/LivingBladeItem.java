package com.vincenthuto.hemomancy.common.item.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.List;

import com.vincenthuto.hemomancy.client.render.item.hematic.LivingBladeItemRenderer;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class LivingBladeItem extends LivingToolItem implements HemoClientItemExtensionsProvider {

	public static String TAG_STATE = "state";

	public LivingBladeItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			if (stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(TAG_STATE)) {
				tooltip.add(Component.literal("State: Unleashed").withStyle(ChatFormatting.RED));
				tooltip.add(Component.literal("+20 Blood Damage").withStyle(ChatFormatting.RED));

			} else {
				tooltip.add(Component.literal("State: Tame").withStyle(ChatFormatting.GRAY));
				tooltip.add(Component.literal("+5 Blood Damage").withStyle(ChatFormatting.RED));

			}
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(TAG_STATE)) {
			attacker.heal(this.getLivingAttackDamage() / 2);
			target.hurt(target.damageSources().generic(), 20);
			if (!attacker.level().isClientSide) {
				Player playerIn = (Player) attacker;
				IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(playerIn)
						.orElseThrow(NullPointerException::new);
				float damageMod = this.getLivingAttackDamage() * 75f;
				if (playerVolume.getBloodVolume() > damageMod) {
					playerVolume.drain(damageMod);
					PacketHandler.sendToPlayer((ServerPlayer) playerIn, new BloodVolumeServerPacket(playerVolume));
				} else {
					playerVolume.drain(damageMod);
					PacketHandler.sendToPlayer((ServerPlayer) playerIn, new BloodVolumeServerPacket(playerVolume));
					stack.hurtAndBreak(stack.getMaxDamage() + 10, attacker, EquipmentSlot.MAINHAND);
				}

			}
		} else {
			target.hurt(target.damageSources().generic(), 5);

		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropLivingBlade.INSTANCE;
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

class RenderPropLivingBlade implements IClientItemExtensions {

	public static RenderPropLivingBlade INSTANCE = new RenderPropLivingBlade();

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new LivingBladeItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
