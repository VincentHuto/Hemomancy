package com.huto.hemomancy.capabilities.mindrunes;

import java.util.Collection;
import java.util.Collections;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.SyncPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RuneEntityEventHandler {

    @SubscribeEvent
    public static void cloneCapabilitiesEvent(PlayerEvent.Clone event) {
        try {
            event.getOriginal().getCapability(RunesCapabilities.RUNES).ifPresent(bco -> {
                CompoundNBT nbt = ((RunesContainer) bco).serializeNBT();
                event.getOriginal().getCapability(RunesCapabilities.RUNES).ifPresent(bcn -> {
                    ((RunesContainer) bcn).deserializeNBT(nbt);
                });
            });
        } catch (Exception e) {
            System.out.println("Could not clone player [" + event.getOriginal().getName() + "] runes when changing dimensions");
        }
    }

    @SubscribeEvent
    public static void attachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "runecontainer"), new RunesContainerProvider((PlayerEntity) event.getObject()));
        }
    }

    @SubscribeEvent
    public static void playerJoin(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            syncSlots(player, Collections.singletonList(player));
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        if (target instanceof ServerPlayerEntity) {
            syncSlots((ServerPlayerEntity) target, Collections.singletonList(event.getPlayer()));
        }
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        // player events
        if (event.phase == TickEvent.Phase.END) {
            PlayerEntity player = event.player;
            player.getCapability(RunesCapabilities.RUNES).ifPresent(IRunesItemHandler::tick);
        }
    }

    private static void syncSlots(PlayerEntity player, Collection<? extends PlayerEntity> receivers) {
        player.getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
            for (byte i = 0; i < runes.getSlots(); i++) {
                syncSlot(player, i, runes.getStackInSlot(i), receivers);
            }
        });
    }

    public static void syncSlot(PlayerEntity player, byte slot, ItemStack stack, Collection<? extends PlayerEntity> receivers) {
        SyncPacket pkt = new SyncPacket(player.getEntityId(), slot, stack);
        for (PlayerEntity receiver : receivers) {
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) receiver), pkt);
        }
    }

    @SubscribeEvent
    public static void playerDeath(LivingDropsEvent event) {
        if (event.getEntity() instanceof PlayerEntity && !event.getEntity().world.isRemote && !event.getEntity().world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            dropItemsAt((PlayerEntity) event.getEntity(), event.getDrops());
        }
    }

    private static void dropItemsAt(PlayerEntity player, Collection<ItemEntity> drops) {
        player.getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
            for (int i = 0; i < runes.getSlots(); ++i) {
                if (!runes.getStackInSlot(i).isEmpty()) {
                    ItemEntity ei = new ItemEntity(player.world, player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ(), runes.getStackInSlot(i).copy());
                    ei.setPickupDelay(40);
                    drops.add(ei);
                    runes.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        });
    }
}