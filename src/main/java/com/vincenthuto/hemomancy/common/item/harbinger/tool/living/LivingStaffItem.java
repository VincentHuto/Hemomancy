package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.particle.factory.AbsrobedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.projectile.DirectedBloodOrbEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.itemhandler.LivingStaffItemHandler;
import com.vincenthuto.hemomancy.common.menu.LivingStaffMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class LivingStaffItem extends LivingItemItem {

	private static int getSlotFor(Inventory inv, ItemStack stack) {
		if (inv.getSelected() == stack)
			return inv.selected;

		for (int i = 0; i < inv.items.size(); ++i) {
			ItemStack invStack = inv.items.get(i);
			if (invStack == stack) {
				return i;
			}
		}

		// Couldn't find the exact instance, can not ensure we have the right slot.
		return -1;
	}

	public LivingStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		Level world = context.level();
		if (world != null && world.isClientSide) {
			net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
			if (player != null) {
				HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
					ItemStack equipped = cap.getEquippedMorphling();
					if (!equipped.isEmpty()) {
						tooltip.add(equipped.getHoverName());
					}
				});
			}
		}
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000 / 2;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		CompoundTag staffnbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!staffnbt.contains("Inventory")) {
			IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
			if (handler instanceof LivingStaffItemHandler) {
				LivingStaffItemHandler staffHandler = (LivingStaffItemHandler) handler;
				staffHandler.setDirty();
			}
		}
	}
	

	@Override
	public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
		super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
		if (pLivingEntity.level().isClientSide) {
			if (pLivingEntity.isShiftKeyDown()) {
				Random rand = new Random();
				Level worldIn = pLivingEntity.level();
				int radius = 2;
				BlockPos pos = pLivingEntity.blockPosition();
				// Absorbs from ground
				for (int i = -radius; i <= radius; ++i) {
					for (int j = -radius; j <= radius; ++j) {
						if (i > -radius && i < radius && j == -1) {
							j = radius;
						}
						if (rand.nextInt(3) == 0) {
							for (int k = 0; k <= 1; ++k) {
								Vec3 vec = pLivingEntity.position();
								BlockPos blockpos = pos.offset(i, k, j);
								if (worldIn.getBlockState(blockpos).getEnchantPowerBonus(worldIn, blockpos) == 0) {
									if (!worldIn.isEmptyBlock(pos.offset(i / radius, 0, j / radius))) {
										break;
									}

									worldIn.addParticle(
											AbsrobedBloodCellParticleFactory.createData(ParticleColor.genRandomColor()),
											vec.x(), vec.y() + 2D, vec.z(), i + rand.nextFloat() - 0.5D,
											k - rand.nextFloat() - 1.0F, j + rand.nextFloat() - 0.5D);

								}
							}
						}
					}
				}
			}

		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof Player) {
			Player player = (Player) entityLiving;
			IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player)
					.orElseThrow(NullPointerException::new);
			if (playerVolume.getBloodVolume() > 50f) {
				if (!worldIn.isClientSide) {

					/*
					 * if (worldIn.rand.nextInt(10) == 6) { player.sendStatusMessage(new
					 * TextComponent( ChatFormatting.DARK_PURPLE +
					 * "Abuse of Power does not come without consequence"), true); }
					 */
					if (!player.isCrouching()) {
						HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
							ItemStack selectedStack = cap.getEquippedMorphling();
							if (!selectedStack.isEmpty() && selectedStack.getItem() instanceof IMorphling) {
								IMorphling morphling = (IMorphling) selectedStack.getItem();
								morphling.use(player, player.getUsedItemHand(), stack, worldIn);
								playerVolume.drain(morphling.getBloodCost());

								PacketHandler.sendToPlayer((ServerPlayer) player, new BloodVolumeServerPacket(playerVolume));
							}
						});

					} else {
						this.summonDirectedOrb(worldIn, player);
						playerVolume.drain(50f);

						PacketHandler.sendToPlayer((ServerPlayer) player, new BloodVolumeServerPacket(playerVolume));
					}
				} else {
					player.playSound(SoundEvents.HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.2F,
							0.8F + (float) Math.random() * 0.2F);
				}
				stack.hurtAndBreak(1, player,
						player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND
								: EquipmentSlot.OFFHAND);
			} else {
				player.displayClientMessage(Component.literal("Not enough blood to be shed"), true);
			}
		}

		((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));

	}

	public void summonDirectedOrb(Level worldIn, Player playerIn) {
		DirectedBloodOrbEntity miss = new DirectedBloodOrbEntity(playerIn, false);
		miss.setPos(playerIn.getX() - 0.5, playerIn.getY() + 0.6, playerIn.getZ() - 0.5);
		miss.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.0F, 1.0F);
		worldIn.addFreshEntity(miss);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {

		if (!worldIn.isClientSide) {
			if (playerIn.isShiftKeyDown()) {
				playerIn.openMenu(new MenuProvider() {

					@Nullable

					@Override
					public AbstractContainerMenu createMenu(int windowId, Inventory p_createMenu_2_,
							Player p_createMenu_3_) {
						return new LivingStaffMenu(windowId, p_createMenu_3_.level(), p_createMenu_3_.blockPosition(),
								p_createMenu_2_, p_createMenu_3_);
					}

					@Override
					public Component getDisplayName() {
						return playerIn.getItemInHand(handIn).getHoverName();
					}
				});

			} else {
				ItemStack itemstack = playerIn.getItemInHand(handIn);
				playerIn.startUsingItem(handIn);
				return InteractionResultHolder.consume(itemstack);
			}
		}
		return InteractionResultHolder.success(playerIn.getItemInHand(handIn));

	}

}
