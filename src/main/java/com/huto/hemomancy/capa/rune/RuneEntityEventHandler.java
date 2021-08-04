package com.huto.hemomancy.capa.rune;

import java.util.Collection;
import java.util.Collections;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.PacketRuneSync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RuneEntityEventHandler {

	@SubscribeEvent
	public static void cloneCapabilitiesEvent(PlayerEvent.Clone event) {
		try {
			event.getOriginal().getCapability(RunesCapabilities.RUNES).ifPresent(bco -> {
				CompoundTag nbt = ((RunesContainer) bco).serializeNBT();
				event.getOriginal().getCapability(RunesCapabilities.RUNES).ifPresent(bcn -> {
					((RunesContainer) bcn).deserializeNBT(nbt);
				});
			});
		} catch (Exception e) {
			System.out.println(
					"Could not clone player [" + event.getOriginal().getName() + "] runes when changing dimensions");
		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "runecontainer"),
					new RunesContainerProvider((Player) event.getObject()));
		}
	}

	@SubscribeEvent
	public static void playerJoin(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) entity;
			syncSlots(player, Collections.singletonList(player));
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		Entity target = event.getTarget();
		if (target instanceof ServerPlayer) {
			syncSlots((ServerPlayer) target, Collections.singletonList(event.getPlayer()));
		}
	}

	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		// player events
		if (event.phase == TickEvent.Phase.END) {
			Player player = event.player;
			player.getCapability(RunesCapabilities.RUNES).ifPresent(IRunesItemHandler::tick);
		}
	}

	private static void syncSlots(Player player, Collection<? extends Player> receivers) {
		player.getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
			for (byte i = 0; i < runes.getSlots(); i++) {
				syncSlot(player, i, runes.getStackInSlot(i), receivers);
			}
		});
	}

	public static void syncSlot(Player player, byte slot, ItemStack stack, Collection<? extends Player> receivers) {
		PacketRuneSync pkt = new PacketRuneSync(player.getId(), slot, stack);
		for (Player receiver : receivers) {
			PacketHandler.CHANNELRUNES.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), pkt);
		}
	}

	@SubscribeEvent
	public static void playerDeath(LivingDropsEvent event) {
		if (event.getEntity() instanceof Player && !event.getEntity().level.isClientSide
				&& !event.getEntity().level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			dropItemsAt((Player) event.getEntity(), event.getDrops());
		}
	}

	private static void dropItemsAt(Player player, Collection<ItemEntity> drops) {
		player.getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
			for (int i = 0; i < runes.getSlots(); ++i) {
				if (!runes.getStackInSlot(i).isEmpty()) {
					ItemEntity ei = new ItemEntity(player.level, player.getX(), player.getY() + player.getEyeHeight(),
							player.getZ(), runes.getStackInSlot(i).copy());
					ei.setPickUpDelay(40);
					drops.add(ei);
					runes.setStackInSlot(i, ItemStack.EMPTY);
				}
			}
		});
	}
}