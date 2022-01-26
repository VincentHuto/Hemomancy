package com.vincenthuto.hemomancy.item.tool.living;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodBullet;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodNeedle;
import com.vincenthuto.hemomancy.render.item.RenderItemLivingPistol;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemLivingPistol extends Item implements IDispellable {
	public static String TAG_MODE = "mode";

	public ItemLivingPistol(Item.Properties propertiesIn) {
		super(propertiesIn.stacksTo(1).durability(2048));
	}

	@Override
	public UseAnim getUseAnimation(ItemStack p_41452_) {
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
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
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		CompoundTag compound = stack.getOrCreateTag();
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
			stack.setTag(compound);
		} else {
			ItemUtils.startUsingInstantly(worldIn, playerIn, handIn);
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
		if (entity instanceof Player player) {
			if (!player.isCrouching()) {
				int mode = getGunMode(stack);
				Vec3 vector3d = player.getLookAngle();
				Vector3f vector3f = new Vector3f(vector3d);
				if (mode == 0) {
					// Pistol
					EntityBloodNeedle shot = new EntityBloodNeedle(world, player);
					shot.shoot(vector3f.x(), vector3f.y(), vector3f.z(), 4.5F, 1.0f);
					world.addFreshEntity(shot);
				} else if (mode == 1) {
					// Blaster
					int randInt = world.random.nextInt(11) + 10;
					EntityBloodNeedle[] needles = new EntityBloodNeedle[randInt];
					for (int i = 0; i < needles.length; i++) {
						needles[i] = new EntityBloodNeedle(world, player);
						needles[i].shoot(vector3f.x(), vector3f.y(), vector3f.z(), world.random.nextInt(5) + 4,
								world.random.nextInt(20) - world.random.nextInt(20));
						world.addFreshEntity(needles[i]);
					}
				} else {
					// Rifle
					int randInt = world.random.nextInt(3) + 6;
					EntityBloodBullet[] needles = new EntityBloodBullet[randInt];
					for (int i = 0; i < needles.length; i++) {
						needles[i] = new EntityBloodBullet(world, player);
						needles[i].shoot(vector3f.x(), vector3f.y(), vector3f.z(), world.random.nextInt(5) + 4,
								world.random.nextInt(5) - world.random.nextInt(5));
						world.addFreshEntity(needles[i]);
					}
				}
			}
		}
		return super.finishUsingItem(stack, world, entity);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			if (stack.getTag().contains(TAG_MODE)) {
				int mode = stack.getTag().getInt(TAG_MODE);
				tooltip.add(new TranslatableComponent("State: " + mode).withStyle(ChatFormatting.RED));

			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return new TextComponent(
				HLTextUtils.stringToBloody(HLTextUtils.convertInitToLang(stack.getItem().getRegistryName().getPath())))
						.withStyle(ChatFormatting.DARK_RED);
	}

	public int getGunMode(ItemStack stack) {
		if (stack.getItem() instanceof ItemLivingPistol) {
			CompoundTag compound = stack.getOrCreateTag();
			if (compound.contains(TAG_MODE)) {
				int mode = compound.getInt(TAG_MODE);
				return mode;
			}
		}
		return 0;
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		super.initializeClient(consumer);
		consumer.accept(RenderPropPistol.INSTANCE);

	}
}

class RenderPropPistol implements IItemRenderProperties {

	public static RenderPropPistol INSTANCE = new RenderPropPistol();

	@Override
	public Font getFont(ItemStack stack) {
		return Minecraft.getInstance().font;
	}

	@Override
	public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
		return new RenderItemLivingPistol(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}