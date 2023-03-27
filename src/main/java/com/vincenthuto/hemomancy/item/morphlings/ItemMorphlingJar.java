package com.vincenthuto.hemomancy.item.morphlings;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MorphlingJarMenu;
import com.vincenthuto.hemomancy.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.morphling.ToggleMorphlingJarMessagePacket;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;

public class ItemMorphlingJar extends Item {

	@SuppressWarnings("rawtypes")
	class MorphlingJarCaps implements ICapabilitySerializable {
		@SuppressWarnings("unused")
		private int size;

		private ItemStack itemStack;
		private MorphlingJarItemHandler inventory;
		private LazyOptional<IItemHandler> optional;
		public MorphlingJarCaps(ItemStack stack, int size, CompoundTag nbtIn) {
			itemStack = stack;
			this.size = size;
			inventory = new MorphlingJarItemHandler(itemStack, size);
			optional = LazyOptional.of(() -> inventory);
		}

		@Override
		public void deserializeNBT(Tag nbt) {
			inventory.load();
		}

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
	}
	public static String TAG_SIZE = "size";
	String name;
	Integer size;

	Rarity rarity;

	public ItemMorphlingJar(String name, Integer size, Rarity rarity) {
		super(new Item.Properties().stacksTo(1));
		this.name = name;
		this.size = size;
		this.rarity = rarity;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);

		boolean pickupEnabled = stack.getOrCreateTag().getBoolean("Pickup");
		if (pickupEnabled)
			tooltip.add(Component.literal(I18n.get("hemomancy.autopickupenabled")));
		else
			tooltip.add(Component.literal(I18n.get("hemomancy.autopickupdisabled")));

		if (Screen.hasShiftDown()) {
			IItemHandler jarHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElseThrow(NullPointerException::new);
			if (jarHandler != null) {
				for (int i = 0; i < jarHandler.getSlots(); i++) {
					if (jarHandler.getStackInSlot(i).getItem() != Items.AIR) {
					}
				}
				CompoundTag CompoundTag = stack.getOrCreateTag();
				CompoundTag items = (CompoundTag) CompoundTag.get("Inventory");
				if (items != null) {
					if (items.contains("Items", 9)) {
						for (int i = 0; i < ((ListTag) items.get("Items")).size(); i++) {
							tooltip.add(ItemStack.of(((ListTag) items.get("Items")).getCompound(i)).getHoverName());

						}
					}
				}
			}

		} else {
			tooltip.add(Component.literal(fallbackString("hemomancy.shift", "Press <�6�oShift�r> for info.")));
		}
	}

	private String fallbackString(String key, String fallback) {
		String tmp = I18n.get(key);
		return tmp.equals(key) ? fallback : tmp;
	}

	public boolean filterItem(ItemStack item, ItemStack packItem) {
		return item.getItem() instanceof MorphlingItem ? true : false;

	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return rarity;
	}

	@SuppressWarnings("unused")
	private boolean hasTranslation(String key) {
		return !I18n.get(key).equals(key);
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new MorphlingJarCaps(stack, size, nbt);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (entityIn instanceof Player) {
			CompoundTag CompoundTag = stack.getOrCreateTag();
			if (stack.hasTag()) {
				CompoundTag items = (CompoundTag) CompoundTag.get("Inventory");
				if (items != null) {
					if (items.contains("Items", 9)) {
						CompoundTag.putInt(TAG_SIZE, ((ListTag) items.get("Items")).size());
					}
				}
			}
		}
	}

	public boolean pickupEvent(EntityItemPickupEvent event, ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if ((nbt == null) || !nbt.getBoolean("Pickup"))
			return false;

		LazyOptional<IItemHandler> stupidIdiot = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (!stupidIdiot.isPresent())
			return false;

		IItemHandler handler = stupidIdiot.orElse(null);
		if (handler == null || !(handler instanceof MorphlingJarItemHandler))
			return false;
		((MorphlingJarItemHandler) handler).loadIfNotLoaded();

		if (!filterItem(event.getItem().getItem(), stack))
			return false;

		ItemStack pickedUp = event.getItem().getItem();
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack slot = handler.getStackInSlot(i);
			if (slot.isEmpty() || (ItemHandlerHelper.canItemStacksStack(slot, pickedUp) && slot.getCount() < 1
					&& slot.getCount() < handler.getSlotLimit(i))) {
				int remainder = handler.insertItem(i, pickedUp.copy(), false).getCount();
				pickedUp.setCount(remainder);
				if (remainder == 0)
					break;
			}
		}
		return pickedUp.isEmpty();
	}

	public ItemMorphlingJar setName() {
		return this;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	public void togglePickup(Player playerEntity, ItemStack stack) {
		CompoundTag nbt = stack.getOrCreateTag();
		boolean Pickup = !nbt.getBoolean("Pickup");
		nbt.putBoolean("Pickup", Pickup);
		if (playerEntity instanceof ServerPlayer)
			PacketHandler.CHANNELMORPHLINGJAR.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerEntity),
					new ToggleMorphlingJarMessagePacket(Pickup));
		else
			playerEntity.displayClientMessage(Component.literal(
					I18n.get(Pickup ? "Hemomancy.autopickupenabled" : "Hemomancy.autopickupdisabled")), true);

	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (worldIn.isClientSide) {
			if (!playerIn.isShiftKeyDown()) {
				Hemomancy.proxy.openJarGui();
				playerIn.playSound(SoundEvents.GLASS_PLACE, 0.40f, 1F);
			}
		}

		if (!worldIn.isClientSide) {
			if (playerIn.isShiftKeyDown()) {
				// open
				playerIn.openMenu(new MenuProvider() {
					@Nullable
					@Override
					public AbstractContainerMenu createMenu(int windowId, Inventory p_createMenu_2_,
							Player p_createMenu_3_) {
						return new MorphlingJarMenu(windowId, p_createMenu_3_.level,
								p_createMenu_3_.blockPosition(), p_createMenu_2_, p_createMenu_3_);
					}

					@Override
					public Component getDisplayName() {
						return playerIn.getItemInHand(handIn).getHoverName();
					}
				});

			}
		}
		return InteractionResultHolder.success(playerIn.getItemInHand(handIn));

	}
}