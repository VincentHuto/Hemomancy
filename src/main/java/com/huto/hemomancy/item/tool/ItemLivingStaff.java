package com.huto.hemomancy.item.tool;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.containers.ContainerLivingStaff;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbDirected;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbTracking;
import com.huto.hemomancy.event.ClientEventSubscriber;
import com.huto.hemomancy.itemhandler.LivingStaffItemHandler;
import com.huto.hemomancy.network.PacketAirBloodDraw;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.BloodVolumePacketServer;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemLivingStaff extends Item {

	public ItemLivingStaff(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

		IItemHandler handler = Hemomancy.findLivingStaff(playerIn)
				.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);

		if (handler != null) {
			for(int i = 0; i<handler.getSlots(); i++) {
			System.out.println(handler.getStackInSlot(i));

			}
		}

		if (worldIn.isRemote) {
			if (!playerIn.isSneaking()) {
				// Hemomancy.proxy.openJarGui();
				playerIn.playSound(SoundEvents.BLOCK_GLASS_PLACE, 0.40f, 1F);
			}
		}

		if (!worldIn.isRemote) {
			if (playerIn.isSneaking()) {
				// open
				playerIn.openContainer(new INamedContainerProvider() {
					@Override
					public ITextComponent getDisplayName() {
						return playerIn.getHeldItem(handIn).getDisplayName();
					}

					@Nullable
					@Override
					public Container createMenu(int windowId, PlayerInventory p_createMenu_2_,
							PlayerEntity p_createMenu_3_) {
						return new ContainerLivingStaff(windowId, p_createMenu_3_.world, p_createMenu_3_.getPosition(),
								p_createMenu_2_, p_createMenu_3_);
					}
				});

			}
		}
		return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));

	}
	/*
	 * @Override public ActionResult<ItemStack> onItemRightClick(World worldIn,
	 * PlayerEntity playerIn, Hand handIn) { ItemStack itemstack =
	 * playerIn.getHeldItem(handIn); playerIn.setActiveHand(handIn); return
	 * ActionResult.resultConsume(itemstack);
	 * 
	 * }
	 */

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
		if (player.world.isRemote) {
			PacketHandler.CHANNELBLOODVOLUME
					.sendToServer(new PacketAirBloodDraw(ClientEventSubscriber.getPartialTicks()));
		}
		super.onUsingTick(stack, player, count);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entityLiving;
			IBloodVolume playerVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			if (playerVolume.getBloodVolume() > 50f) {
				if (!worldIn.isRemote) {
					playerVolume.subtractBloodVolume(50f);
					PacketHandler.CHANNELBLOODVOLUME.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
							new BloodVolumePacketServer(playerVolume.getBloodVolume()));
					if (worldIn.rand.nextInt(10) == 6) {

						player.sendStatusMessage(new StringTextComponent(
								TextFormatting.DARK_PURPLE + "Abuse of Power does not come without consequence"), true);
					}
					if (player.isCrouching()) {
						this.summonNoteStorm(worldIn.rand.nextInt(3), worldIn, player);
					} else {
						this.summonDirectedOrb(worldIn, player);
					}
				} else {
					player.playSound(SoundEvents.ENTITY_HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.2F,
							0.8F + (float) Math.random() * 0.2F);
				}
				stack.damageItem(1, player, (p_220009_1_) -> {
					p_220009_1_.sendBreakAnimation(player.getActiveHand());
				});
			} else {
				player.sendStatusMessage(new StringTextComponent("Not enough blood to be shed"), true);
			}
		}

		((PlayerEntity) entityLiving).addStat(Stats.ITEM_USED.get(this));

	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	public void summonDirectedOrb(World worldIn, PlayerEntity playerIn) {
		EntityBloodOrbDirected miss = new EntityBloodOrbDirected((PlayerEntity) playerIn, false);
		miss.setPosition(playerIn.getPosX() - 0.5, playerIn.getPosY() + 0.6, playerIn.getPosZ() - 0.5);
		miss.setDirectionMotion(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.0F, 1.0F);
		worldIn.addEntity(miss);
	}

	public void summomCorruptNote(World worldIn, PlayerEntity playerIn) {
		EntityBloodOrbTracking missile = new EntityBloodOrbTracking(playerIn, false);
		missile.setPosition(playerIn.getPosX() + (Math.random() - 0.5 * 0.1), playerIn.getPosY() + 0.8f,
				playerIn.getPosZ() + (Math.random() - 0.5 * 0.1));
		if (missile.findTarget()) {
			playerIn.playSound(SoundEvents.ENTITY_ENDERMAN_SCREAM, 0.6F, 0.8F + (float) Math.random() * 0.2F);
			worldIn.addEntity(missile);
		}
	}

	public void summonNoteStorm(int numMiss, World worldIn, PlayerEntity playerIn) {
		EntityBloodOrbTracking[] missArray = new EntityBloodOrbTracking[numMiss];
		for (int i = 0; i < numMiss; i++) {
			missArray[i] = new EntityBloodOrbTracking(playerIn, false);
			missArray[i].setPosition(playerIn.getPosX() + ((Math.random()) * 1.5), playerIn.getPosY() + 0.8f,
					playerIn.getPosZ() + ((Math.random()) * 1.5));
			if (!worldIn.isRemote) {
				playerIn.playSound(SoundEvents.ENTITY_ENDERMAN_SCREAM, 0.6F, 0.8F + (float) Math.random() * 0.2F);
				worldIn.addEntity(missArray[i]);

			}
		}
	}

	public void summonTentacleAid(int numTent, World world, PlayerEntity player, Vector3d hitVec) {
		EntityBloodOrbTracking[] tentArray = new EntityBloodOrbTracking[numTent];
		for (int i = 0; i < numTent; i++) {
			tentArray[i] = new EntityBloodOrbTracking(player, false);
			float xMod = (world.rand.nextFloat() - 0.5F) * 8.0F;
			float yMod = (world.rand.nextFloat() - 0.5F) * 4.0F;
			float zMod = (world.rand.nextFloat() - 0.5F) * 8.0F;
			tentArray[i].setPosition(hitVec.getX() + 0.5 + xMod, hitVec.getY() + 1.5 + yMod,
					hitVec.getZ() + 0.5 + zMod);
			if (!world.isRemote) {
				world.addEntity(tentArray[i]);

			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {

		IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.orElseThrow(NullPointerException::new);
		for (int i = 0; i < handler.getSlots(); i++) {
			tooltip.add(new StringTextComponent("slots " + handler.getStackInSlot(i)));
		}

	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
		return new LivingStaffInventoryCap(stack, 1, nbt);
	}

	@SuppressWarnings("rawtypes")
	class LivingStaffInventoryCap implements ICapabilitySerializable {
		public LivingStaffInventoryCap(ItemStack stack, int size, CompoundNBT nbtIn) {
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
		public INBT serializeNBT() {
			inventory.save();
			return new CompoundNBT();
		}

		@Override
		public void deserializeNBT(INBT nbt) {
			inventory.load();
		}
	}

}
