package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingPistolItemRenderer;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodBulletEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class LivingPistolItem extends Item implements IDispellable, HemoClientItemExtensionsProvider {
	public static String TAG_MODE = "mode";

	public LivingPistolItem(Item.Properties propertiesIn) {
		super(propertiesIn.stacksTo(1).durability(2048));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (tag.contains(TAG_MODE)) {
				int mode = tag.getInt(TAG_MODE);
				tooltip.add(Component.literal("State: " + mode).withStyle(ChatFormatting.RED));

			}
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
		if (entity instanceof Player player) {
			if (!player.isCrouching()) {
				int mode = getGunMode(stack);
				Vec3 vector3d = player.getLookAngle();
				Vector3 vector3f = new Vector3(vector3d);
				if (mode == 0) {
					// Pistol
					BloodNeedleEntity shot = new BloodNeedleEntity(world, player);
					shot.shoot(vector3f.x, vector3f.y, vector3f.z, 4.5F, 1.0f);
					world.addFreshEntity(shot);
				} else if (mode == 1) {
					// Blaster
					int randInt = world.random.nextInt(11) + 10;
					BloodNeedleEntity[] needles = new BloodNeedleEntity[randInt];
					for (int i = 0; i < needles.length; i++) {
						needles[i] = new BloodNeedleEntity(world, player);
						needles[i].shoot(vector3f.x, vector3f.y, vector3f.z, world.random.nextInt(5) + 4,
								world.random.nextInt(20) - world.random.nextInt(20));
						world.addFreshEntity(needles[i]);
					}
				} else {
					// Rifle
					int randInt = world.random.nextInt(3) + 6;
					BloodBulletEntity[] needles = new BloodBulletEntity[randInt];
					for (int i = 0; i < needles.length; i++) {
						needles[i] = new BloodBulletEntity(world, player);
						needles[i].shoot(vector3f.x, vector3f.y, vector3f.z, world.random.nextInt(5) + 4,
								world.random.nextInt(5) - world.random.nextInt(5));
						world.addFreshEntity(needles[i]);
					}
				}
			}
		}
		return super.finishUsingItem(stack, world, entity);
	}

	public int getGunMode(ItemStack stack) {
		if (stack.getItem() instanceof LivingPistolItem) {
			CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (compound.contains(TAG_MODE)) {
				int mode = compound.getInt(TAG_MODE);
				return mode;
			}
		}
		return 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return Component
				.literal(HLTextUtils.stringToBloody(
						HLTextUtils.convertInitToLang(HLTextUtils.getItemRegistryName(stack.getItem()))))
				.withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack p_41452_) {
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		int count = 16;
		int mode = getGunMode(stack);
		if (mode == 0) {
			count = 16;
		} else if (mode == 1) {
			count = 32;
		} else {
			count = 48;
		}
		return count;
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropPistol.INSTANCE;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (playerIn.isCrouching()) {
			int mode = getGunMode(stack);
			if (mode == 0) {
				compound.putInt(TAG_MODE, 1);
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
			} else if (mode == 1) {
				compound.putInt(TAG_MODE, 2);
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
			} else {
				compound.putInt(TAG_MODE, 0);
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
			}
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
		} else {
			ItemUtils.startUsingInstantly(worldIn, playerIn, handIn);
		}
		return super.use(worldIn, playerIn, handIn);
	}
}

class RenderPropPistol implements IClientItemExtensions {

	public static RenderPropPistol INSTANCE = new RenderPropPistol();

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new LivingPistolItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}