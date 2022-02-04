package com.vincenthuto.hemomancy.item.tool.living;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.container.MenuLivingStaff;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodOrbDirected;
import com.vincenthuto.hemomancy.item.morphlings.IMorphling;
import com.vincenthuto.hemomancy.itemhandler.LivingStaffItemHandler;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.vincenthuto.hemomancy.particle.factory.AbsrobedBloodCellParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;

public class ItemLivingStaff extends ItemLivingItem {

	public ItemLivingStaff(Properties properties) {
		super(properties);
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
		super.onUsingTick(stack, player, count);
		if (player.level.isClientSide) {
			if (player.isShiftKeyDown()) {
				Random rand = new Random();
				Level worldIn = player.level;
				int radius = 2;
				BlockPos pos = player.blockPosition();
				// Absorbs from ground
				for (int i = -radius; i <= radius; ++i) {
					for (int j = -radius; j <= radius; ++j) {
						if (i > -radius && i < radius && j == -1) {
							j = radius;
						}
						if (rand.nextInt(3) == 0) {
							for (int k = 0; k <= 1; ++k) {
								Vec3 vec = player.position();
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
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {

		if (!worldIn.isClientSide) {
			if (playerIn.isShiftKeyDown()) {
				playerIn.openMenu(new MenuProvider() {

					@Override
					public Component getDisplayName() {
						return playerIn.getItemInHand(handIn).getHoverName();
					}

					@Nullable

					@Override
					public AbstractContainerMenu createMenu(int windowId, Inventory p_createMenu_2_,
							Player p_createMenu_3_) {
						return new MenuLivingStaff(windowId, p_createMenu_3_.level,
								p_createMenu_3_.blockPosition(), p_createMenu_2_, p_createMenu_3_);
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

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		CompoundTag staffnbt = stack.getOrCreateTag();
		if (!staffnbt.contains("Inventory")) {
			IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElseThrow(NullPointerException::new);
			if (handler instanceof LivingStaffItemHandler) {
				LivingStaffItemHandler staffHandler = (LivingStaffItemHandler) handler;
				staffHandler.setDirty();
			}
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof Player) {
			Player player = (Player) entityLiving;
			IBloodVolume playerVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			if (playerVolume.getBloodVolume() > 50f) {
				if (!worldIn.isClientSide) {

					/*
					 * if (worldIn.rand.nextInt(10) == 6) { player.sendStatusMessage(new
					 * TextComponent( ChatFormatting.DARK_PURPLE +
					 * "Abuse of Power does not come without consequence"), true); }
					 */
					if (!player.isCrouching()) {
						CompoundTag CompoundTag = stack.getOrCreateTag();
						CompoundTag items = (CompoundTag) CompoundTag.get("Inventory");
						if (items != null) {
							if (items.contains("Items", 9)) {
								@SuppressWarnings("static-access")
								ItemStack selectedStack = stack.of(((ListTag) items.get("Items")).getCompound(0));
								if (selectedStack.getItem() instanceof IMorphling) {
									IMorphling morphling = (IMorphling) selectedStack.getItem();
									morphling.use(player, player.getUsedItemHand(), stack, worldIn);
									playerVolume.drain(morphling.getBloodCost());

									PacketHandler.CHANNELBLOODVOLUME.send(
											PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
											new PacketBloodVolumeServer(playerVolume));
								}
							}
						}

					} else {
						this.summonDirectedOrb(worldIn, player);
						playerVolume.drain(50f);

						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
								new PacketBloodVolumeServer(playerVolume));
					}
				} else {
					player.playSound(SoundEvents.HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.2F,
							0.8F + (float) Math.random() * 0.2F);
				}
				stack.hurtAndBreak(1, player, (p_220009_1_) -> {
					p_220009_1_.broadcastBreakEvent(player.getUsedItemHand());
				});
			} else {
				player.displayClientMessage(new TextComponent("Not enough blood to be shed"), true);
			}
		}

		((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));

	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	public void summonDirectedOrb(Level worldIn, Player playerIn) {
		EntityBloodOrbDirected miss = new EntityBloodOrbDirected(playerIn, false);
		miss.setPos(playerIn.getX() - 0.5, playerIn.getY() + 0.6, playerIn.getZ() - 0.5);
		miss.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.0F, 1.0F);
		worldIn.addFreshEntity(miss);
	}

	@SuppressWarnings("static-access")
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		CompoundTag CompoundTag = stack.getOrCreateTag();
		CompoundTag items = (CompoundTag) CompoundTag.get("Inventory");
		if (items != null) {
			if (items.contains("Items", 9)) {
				tooltip.add(stack.of(((ListTag) items.get("Items")).getCompound(0)).getHoverName());
			}
		}
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new LivingStaffInventoryCap(stack, 1, nbt);
	}

	@SuppressWarnings("rawtypes")
	class LivingStaffInventoryCap implements ICapabilitySerializable {
		public LivingStaffInventoryCap(ItemStack stack, int size, CompoundTag nbtIn) {
			itemStack = stack;
			this.size = size;
			inventory = new LivingStaffItemHandler(itemStack, size);
			optional = LazyOptional.of(() -> inventory);
		}

		@SuppressWarnings("unused")
		private int size;
		private ItemStack itemStack;
		private LivingStaffItemHandler inventory;
		private LazyOptional<IItemHandler> optional;

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
			if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				return optional.cast();
			} else
				return LazyOptional.empty();
		}

		@Override
		public Tag serializeNBT() {
			inventory.save();
			return new CompoundTag();
		}

		@Override
		public void deserializeNBT(Tag nbt) {
			inventory.load();
		}
	}

}
