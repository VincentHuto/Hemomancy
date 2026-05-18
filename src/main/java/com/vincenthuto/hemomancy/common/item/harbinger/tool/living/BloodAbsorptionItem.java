package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.event.ClientEvents.ClientModBusEvents;
import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.hematic.CellHandItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class BloodAbsorptionItem extends Item implements IDispellable, ICellHand, HemoClientItemExtensionsProvider {

	public BloodAbsorptionItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
	}

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
		return true;
	}

	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
		return armorType == EquipmentSlot.MAINHAND || armorType == EquipmentSlot.OFFHAND;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
		if (entityLiving instanceof Player) {
			((Player) entityLiving).releaseUsingItem();
		}
		return stack;
	}

	@Override
	public BakedModel getBakedModel() {
		// TODO Auto-generated method stub
		return ClientModBusEvents.bloodAbsorptionModel;
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000 / 2;
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			final BlockEntityWithoutLevelRenderer myRenderer = new CellHandItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return myRenderer;
			}
		};
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		return InteractionResult.PASS;
	}

	@Override
	public void onUseTick(Level worldIn, LivingEntity player, ItemStack stack, int count) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(NullPointerException::new);
		List<Entity> targets = player.level().getEntities(player, player.getBoundingBox().inflate(5.0));
		if (!targets.isEmpty()) {
			targets.forEach((t) -> {
				if (t instanceof LivingEntity) {
					LivingEntity livingTarget = (LivingEntity) t;
					float dam = 3f / targets.size();
					livingTarget.hurt(player.damageSources().generic(), dam);
					if (!worldIn.isClientSide) {
						volume.fill(dam);
						PacketHandler.sendToPlayer((ServerPlayer) player, new BloodVolumeServerPacket(volume));
					}
				}
			});

		}
	}

	@SuppressWarnings("unused")
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(playerIn)
				.orElseThrow(NullPointerException::new);
		IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(playerIn)
				.orElseThrow(NullPointerException::new);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(playerIn)
				.orElseThrow(NullPointerException::new);
		if (volume.isActive()) {
			if (volume.getBloodVolume() < volume.getMaxBloodVolume()) {
				playerIn.startUsingItem(handIn);
				new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
			}
		} else {
			playerIn.displayClientMessage(
					Component.literal("You lack the skill to manifest this power!").withStyle(ChatFormatting.RED),
					true);
		}

		return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
	}

}
