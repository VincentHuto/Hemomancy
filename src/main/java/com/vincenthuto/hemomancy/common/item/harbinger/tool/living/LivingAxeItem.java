package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingAxeItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class LivingAxeItem extends LivingToolItem implements HemoClientItemExtensionsProvider {

	public static String TAG_STATE = "state";

	public LivingAxeItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, EnumBloodTendency.MORTEM, tier, builderIn);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return state.is(BlockTags.MINEABLE_WITH_AXE) ? speed : 0.5f;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState state) {
		return state.is(BlockTags.MINEABLE_WITH_AXE);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(TAG_STATE)) {
				tooltip.add(Component.literal("State: Unleashed").withStyle(ChatFormatting.RED));
			} else {
				tooltip.add(Component.literal("State: Tame").withStyle(ChatFormatting.GRAY));
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
			if (!attacker.level().isClientSide && attacker instanceof Player playerIn) {
				IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(playerIn)
						.orElseThrow(NullPointerException::new);
				float damageMod = this.getLivingAttackDamage() * 75f;
				if (playerVolume.getBloodVolume() > damageMod) {
					playerVolume.drain(damageMod);

					PacketHandler.sendToPlayer((ServerPlayer) playerIn, new BloodVolumeServerPacket(playerVolume));
				} else {
					playerVolume.drain(damageMod);
					PacketHandler.sendToPlayer((ServerPlayer) playerIn, new BloodVolumeServerPacket(playerVolume));
					if (!LivingStaffWeaponFormHelper.restoreStaffAfterBloodFailure(stack, attacker)) {
						stack.hurtAndBreak(stack.getMaxDamage() + 10, attacker, EquipmentSlot.MAINHAND);
					}
				}

			}
		}
		return true;
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropLivingAxe.INSTANCE;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (stack.getItem() instanceof LivingAxeItem) {
				CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
				if (compound.getBoolean(TAG_STATE)) {
					if (player.onGround()) {

//						player.knockback(2F, Mth.sin(player.getXRot() * ((float) Math.PI / 180F)),
//								(-Mth.cos(player.getXRot() * ((float) Math.PI / 180F))));
						List<Entity> targets = player.level().getEntities(player, player.getBoundingBox().inflate(3.0));
						if (player.level() instanceof ServerLevel serverLevel) {
							Vec3 pos = player.position();
							PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f, serverLevel);
						}
						if (targets.size() > 0) {

							for (Entity target : targets) {
								if (target instanceof LivingEntity) {
									LivingEntity livingTarget = (LivingEntity) target;
									float dam = 3f / targets.size();
										livingTarget.hurt(TendencyWeaponHelper.createWeaponDamageSource(livingTarget, player), dam);
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
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getMainHandItem();
		if (stack.getItem() instanceof LivingAxeItem) {
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

class RenderPropLivingAxe implements IClientItemExtensions {

	public static RenderPropLivingAxe INSTANCE = new RenderPropLivingAxe();



	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new LivingAxeItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
