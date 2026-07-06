package com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketCurvedHornAnimation;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketGourdScarSync;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.SyncEquipmentLayerVisibilityPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketEquipmentSync;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.Collections;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class HarbingerEquipmentEntityEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGlideTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.hasEffect(EffectInit.fungal_elytra)) return;

        // Ensure FALL_FLYING attribute modifier is present so canFallFly() returns ALLOW
        AttributeInstance attributeInstance = player.getAttribute(AttributeInit.getFlightAttribute());
        if (attributeInstance != null && !attributeInstance.hasModifier(AttributeInit.getElytraModifier().id()))
            attributeInstance.addTransientModifier(AttributeInit.getElytraModifier());

        // This runs AFTER vanilla updateFallFlying (which cancels fall-flying when no
        // elytra is equipped). Re-enable elytra gliding every tick while the player is
        // airborne and not using the creative-hover mode.
        boolean airborne = !player.onGround() && !player.isInWater() && !player.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
        boolean creativeHovering = player.getAbilities().flying;

        if (airborne && !creativeHovering) {
            double vertY = player.getDeltaMovement().y;
            boolean alreadyGliding = player.isFallFlying();
            // Maintain when already gliding, or initiate only when falling fast enough
            // (threshold avoids triggering on normal jumps or 1-block step-downs).
            boolean initiateGlide = vertY < -0.3;
            if (alreadyGliding || initiateGlide) {
                player.startFallFlying();
            }
        }
    }

    @SubscribeEvent
    public static void playerHurt(LivingDeathEvent event) {

        if (event.getEntity() instanceof Player player && !event.getEntity().level().isClientSide) {

            HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
                // slot 5 is the special curved-horn slot, not a regular scar slot
                ItemStack itemstack = equipment.getStackInSlot(5);
                if (itemstack.getItem() == ItemInit.curved_horn.get()) {
                    itemstack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    player.addEffect(new MobEffectInstance(EffectInit.blood_rush, 200, 1));
                    player.setHealth(1.0f);
                    ServerLevel world = (ServerLevel) player.level();
                    world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.AMBIENT, 0.5f, 0.5f);
                    PacketHandler.sendToPlayer((ServerPlayer) player, new PacketCurvedHornAnimation());

                    event.setCanceled(true);
                }
            });
        }

    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        if (target instanceof ServerPlayer) {
            syncSlots((ServerPlayer) target, Collections.singletonList(event.getEntity()));
        }
        if (event.getEntity() instanceof ServerPlayer tracker && target instanceof ServerPlayer trackedPlayer) {
            syncLayerVisibilityToPlayer(trackedPlayer, tracker);
        }
    }
    private static void dropItemsAt(Player player, Collection<ItemEntity> drops) {
        HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
            for (int i = 0; i < equipment.getSlots(); ++i) {
                ItemStack stack = equipment.getStackInSlot(i);
                if (!stack.isEmpty() && VasculariumCharmRules.shouldDropEquippedSlot(stack.is(ItemInit.charm_of_vascularium.get()))) {
                    ItemEntity ei = new ItemEntity(player.level(), player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), stack.copy());
                    ei.setPickUpDelay(40);
                    drops.add(ei);
                    equipment.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        });
    }


    @SubscribeEvent
    public static void playerJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            syncSlots(player, Collections.singletonList(player));
            syncLayerVisibilityToClient(player);
        }
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            syncLayerVisibilityToClient((ServerPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncLayerVisibilityToClient(player);
        }
    }

    @SubscribeEvent
    public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncLayerVisibilityToClient(player);
        }
    }
    // --- Capability lifecycle ---

    @SubscribeEvent
    public static void playerDeath(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player && !event.getEntity().level().isClientSide && !event.getEntity().level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            dropItemsAt((Player) event.getEntity(), event.getDrops());
        }
    }



    // --- Network sync ---

    public static void syncSlot(Player player, byte slot, ItemStack stack, Collection<? extends Player> receivers) {

        if (stack.getItem() instanceof BloodGourdItem) {
            IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(stack).orElse(null);
            PacketGourdScarSync pkt = new PacketGourdScarSync(player.getId(), slot, stack, bloodVolume != null ? bloodVolume.getBloodVolume() : 0);
            for (Player receiver : receivers) {
                PacketHandler.sendToPlayer((ServerPlayer) receiver, pkt);
            }
        } else {
            PacketEquipmentSync pkt = new PacketEquipmentSync(player.getId(), slot, stack);
            for (Player receiver : receivers) {
                PacketHandler.sendToPlayer((ServerPlayer) receiver, pkt);
            }
        }

    }

    public static void syncSlots(Player player, Collection<? extends Player> receivers) {
        HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
            for (byte i = 0; i < equipment.getSlots(); i++) {
                syncSlot(player, i, equipment.getStackInSlot(i), receivers);
            }
        });
    }

    public static void syncLayerVisibilityToClient(ServerPlayer player) {
        HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    new SyncEquipmentLayerVisibilityPacket(player.getId(),
                            equipment.isRenderLayerVisible(IScars.FUNGAL_SLOT),
                            equipment.isRenderLayerVisible(com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu.JAR_SLOT_INDEX),
                            equipment.isRenderLayerVisible(com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu.CHARM_SLOT_INDEX),
                            equipment.isRenderLayerVisible(com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu.GOURD_SLOT_INDEX)));
        });
    }

    public static void syncLayerVisibilityToPlayer(ServerPlayer source, ServerPlayer receiver) {
        HemoCapabilityAccess.getEquipment(source).ifPresent(equipment -> {
            PacketDistributor.sendToPlayer(receiver,
                    new SyncEquipmentLayerVisibilityPacket(source.getId(),
                            equipment.isRenderLayerVisible(IScars.FUNGAL_SLOT),
                            equipment.isRenderLayerVisible(com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu.JAR_SLOT_INDEX),
                            equipment.isRenderLayerVisible(com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu.CHARM_SLOT_INDEX),
                            equipment.isRenderLayerVisible(com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu.GOURD_SLOT_INDEX)));
        });
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        // tick equipment item handler (non-scar equipment)
        HemoCapabilityAccess.getEquipment(player).ifPresent(IHarbingerEquipmentItemHandler::tick);
        AttributeInstance attributeInstance = player.getAttribute(AttributeInit.getFlightAttribute());
        if (attributeInstance != null) {
            AttributeModifier elytraModifier = AttributeInit.getElytraModifier();
            attributeInstance.removeModifier(elytraModifier.id());
            ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);

            if (stack.canElytraFly(player) && !attributeInstance.hasModifier(elytraModifier.id())) {
                attributeInstance.addTransientModifier(elytraModifier);
            }
        }
    }
}
