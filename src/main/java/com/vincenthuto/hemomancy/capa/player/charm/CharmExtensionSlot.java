package com.vincenthuto.hemomancy.capa.player.charm;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.ICharmMenu;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.charm.PacketSyncCharmSlotContents;
import com.vincenthuto.hutoslib.HutosLib;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = HutosLib.MOD_ID, bus = Bus.MOD)
public class CharmExtensionSlot implements ICharmMenu, INBTSerializable<CompoundTag> {

	private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Hemomancy.MOD_ID, "charm_slot");

	public static final Capability<CharmExtensionSlot> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
	});

	public static LazyOptional<CharmExtensionSlot> get(LivingEntity player) {
		return player.getCapability(CAPABILITY);
	}

	public static void register() {
		MinecraftForge.EVENT_BUS.register(new AttachHandlers());
		MinecraftForge.EVENT_BUS.register(new EventHandlers());
	}

	public static class AttachHandlers {
		@SubscribeEvent
		public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
			final Entity entity = event.getObject();
			if (entity instanceof Player || entity instanceof ArmorStand) {
				event.addCapability(CAPABILITY_ID, new ICapabilitySerializable<CompoundTag>() {
					final CharmExtensionSlot extensionContainer = new CharmExtensionSlot((LivingEntity) entity) {
						@Override
						public void onContentsChanged(CharmSlotItemHandler slot) {
							if (!getOwner().level.isClientSide)
								syncTo(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getOwner));
						}
					};

					final LazyOptional<CharmExtensionSlot> extensionContainerSupplier = LazyOptional
							.of(() -> extensionContainer);

					@Override
					public CompoundTag serializeNBT() {
						return extensionContainer.serializeNBT();
					}

					@Override
					public void deserializeNBT(CompoundTag nbt) {
						extensionContainer.deserializeNBT(nbt);
					}

					@Override
					public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
							@Nullable Direction facing) {
						if (CAPABILITY == capability)
							return extensionContainerSupplier.cast();

						return LazyOptional.empty();
					}
				});
			}
		}
	}

	public static class EventHandlers {
		@SubscribeEvent
		public void joinWorld(PlayerEvent.PlayerLoggedInEvent event) {
			Player target = event.getPlayer();
			if (target.level.isClientSide)
				return;
			get(target).ifPresent(CharmExtensionSlot::syncToSelf);
		}

		@SubscribeEvent
		public void joinWorld(PlayerEvent.PlayerChangedDimensionEvent event) {
			Player target = event.getPlayer();
			if (target.level.isClientSide)
				return;
			get(target).ifPresent(CharmExtensionSlot::syncToSelf);
		}

		@SubscribeEvent
		public void track(PlayerEvent.StartTracking event) {
			Entity target = event.getTarget();
			if (target.level.isClientSide)
				return;
			if (target instanceof Player) {
				get((LivingEntity) target).ifPresent(CharmExtensionSlot::syncToSelf);
			}
		}

		@SubscribeEvent
		public void entityTick(TickEvent.PlayerTickEvent event) {
			if (event.phase != TickEvent.Phase.END)
				return;

			get(event.player).ifPresent(CharmExtensionSlot::tickAllSlots);
		}

		@SubscribeEvent
		public void playerDeath(LivingDropsEvent event) {
			LivingEntity entity = event.getEntityLiving();

			get(entity).ifPresent((instance) -> {
				CharmSlotItemHandler banner = instance.getCharm();
				ItemStack stack = banner.getContents();
				if (EnchantmentHelper.hasVanishingCurse(stack)) {
					stack = ItemStack.EMPTY;
					banner.setContents(stack);
				}
				if (stack.getCount() > 0) {
					if (entity instanceof Player player) {
						if (!entity.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
								&& !player.isSpectator()) {
							Collection<ItemEntity> old = entity.captureDrops(event.getDrops());
							player.drop(stack, true, false);
							entity.captureDrops(old);
							banner.setContents(ItemStack.EMPTY);
						}
					} else {
						entity.spawnAtLocation(stack);
						banner.setContents(ItemStack.EMPTY);
					}
				}
			});
		}

		@SubscribeEvent
		public void playerClone(PlayerEvent.Clone event) {
			Player oldPlayer = event.getOriginal();
			oldPlayer.revive();
			Player newPlayer = event.getPlayer();
			get(oldPlayer).ifPresent((oldCharm) -> {
				ItemStack stack = oldCharm.getCharm().getContents();
				get(newPlayer).map(newCharm -> {
					newCharm.getCharm().setContents(stack);
					return Unit.INSTANCE;
				}).orElseGet(() -> {
					if (stack.getCount() > 0) {
						oldPlayer.drop(stack, true, false);
					}
					return Unit.INSTANCE;
				});
			});
		}
	}

	private void syncToSelf() {
		syncTo((Player) owner);
	}

	protected void syncTo(Player target) {
		PacketSyncCharmSlotContents message = new PacketSyncCharmSlotContents((Player) owner, this);
		PacketHandler.CHANNELKNOWNMANIPS.sendTo(message, ((ServerPlayer) target).connection.getConnection(),
				NetworkDirection.PLAY_TO_CLIENT);
	}

	protected void syncTo(PacketDistributor.PacketTarget target) {
		PacketSyncCharmSlotContents message = new PacketSyncCharmSlotContents((Player) owner, this);
		PacketHandler.CHANNELKNOWNMANIPS.send(target, message);
	}

	public static final ResourceLocation CHARM = new ResourceLocation(Hemomancy.MOD_ID, "charm");

	private final LivingEntity owner;
	private final ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			banner.onContentsChanged();
		}
	};
	private final CharmSlotItemHandler banner = new CharmSlotItemHandler(this, CHARM, inventory, 0);
	private final ImmutableList<CharmSlotItemHandler> slots = ImmutableList.of(banner);

	private CharmExtensionSlot(LivingEntity owner) {
		this.owner = owner;
	}

	@Nonnull
	@Override
	public LivingEntity getOwner() {
		return owner;
	}

	@Nonnull
	@Override
	public ImmutableList<CharmSlotItemHandler> getSlots() {
		return slots;
	}

	@Override
	public void onContentsChanged(CharmSlotItemHandler slot) {

	}

	@Nonnull
	public CharmSlotItemHandler getCharm() {
		return banner;
	}

	private void tickAllSlots() {
		for (CharmSlotItemHandler slot : slots) {
			((CharmSlotItemHandler) slot).onWornTick();
		}
	}

	public void setAll(NonNullList<ItemStack> stacks) {
		List<CharmSlotItemHandler> slots = getSlots();
		for (int i = 0; i < slots.size(); i++) {
			slots.get(i).setContents(stacks.get(i));
		}
	}

	@Override
	public CompoundTag serializeNBT() {
		return inventory.serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		inventory.deserializeNBT(nbt);
	}
}
