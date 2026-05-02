package com.vincenthuto.hemomancy.common.item.harbinger;

import java.util.List;

import com.vincenthuto.hemomancy.common.entity.mob.arthropod.HemolymphopodaEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class BloodVialItem extends Item {

	public static String TAG_ENTITY_TYPE = "entity_type";
	public static String TAG_STATE = "state";

	public static EntityType<?> getEntityType(ItemStack stack) {
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
			if (tag.contains(TAG_ENTITY_TYPE)) {
				return BuiltInRegistries.ENTITY_TYPE
						.getOptional(ResourceLocation.parse(tag.getString(TAG_ENTITY_TYPE)))
						.orElse(null);
			}
		}
		return null;
	}

	public BloodVialItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			EntityType<?> type = getEntityType(stack);
			if (type != null) {
				tooltip.add(Component.literal(I18n.get(type.getDescriptionId())).append(" Sample"));
			}
		}
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target,
			InteractionHand hand) {
		return super.interactLivingEntity(stack, playerIn, target, hand);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (entity != null) {
			if (entity instanceof LivingEntity living) {
				// Special case: Hemolymphopoda produces Cleansing Hemolymph instead of a standard sample
				if (living instanceof HemolymphopodaEntity) {
					if (!player.level().isClientSide) {
						ItemStack hemolymphStack = new ItemStack(ItemInit.cleansing_hemolymph.get());
						player.setItemInHand(InteractionHand.MAIN_HAND, hemolymphStack);
						player.playSound(SoundEvents.BOTTLE_FILL, 1.0F, 1.0F);
					}
					// Return true on both sides to cancel the attack; inventory syncs from server
					return true;
				}
				tag.putString(TAG_ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
				tag.putBoolean(TAG_STATE, true);
			}
		} else {
			tag.putBoolean(TAG_STATE, false);
		}
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return super.onLeftClickEntity(stack, player, entity);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
		ItemStack curr = pPlayer.getItemInHand(pUsedHand);
		if (curr.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().get(TAG_ENTITY_TYPE) != null) {
			System.out.println(getEntityType(curr));
		}
		return super.use(pLevel, pPlayer, pUsedHand);
	}

}