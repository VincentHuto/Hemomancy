package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.*;
import com.vincenthuto.hemomancy.client.registry.KeyMappings;
import com.vincenthuto.hemomancy.client.screen.manips.RadialChooseManipScreen;
import com.vincenthuto.hemomancy.client.screen.summon.CrossbarRadialScreen;
import com.vincenthuto.hemomancy.client.screen.unstained.RadialChooseStillArtScreen;
import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodFormationKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ToggleGourdKeyPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ToggleSilentSlippingC2SPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ChangeSelectedManipPacket;
import com.vincenthuto.hemomancy.common.network.capa.unstained.UseStillArtKeyPacket;
import com.vincenthuto.hemomancy.common.network.morphling.OpenMorphlingJarPacket;
import com.vincenthuto.hemomancy.common.network.particle.GroundBloodDrawPacket;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * Key presses, radial menus and held-input effects, ticked from ClientTickEvents.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientInputEvents {

    private ClientInputEvents() {
    }

    private static boolean menuKey = false;
    private static final int SILENT_ARCHON_DOUBLE_TAP_JUMP_WINDOW = 7;
    private static boolean silentArchonJumpWasDown = false;
    private static long lastSilentArchonJumpTapTick = -1000L;
    private static boolean crossbarRadialOpened;

    @SubscribeEvent
    public static void onBloodAbsorptionMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (!BloodAbsorptionItem.isChannelingBloodAbsorption(player)) {
            return;
        }
        double movementMultiplier = BloodAbsorptionItem.getChannelMovementMultiplier(player);
        double inputMultiplier = BloodAbsorptionItem.getClientChannelMovementInputMultiplier(player);
        var input = event.getInput();
        input.forwardImpulse *= inputMultiplier;
        input.leftImpulse *= inputMultiplier;
        if (movementMultiplier <= 0.0D) {
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
        }
    }

    static void handleLivingBaghnakhAutoAttack() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null || minecraft.gameMode == null) {
            return;
        }
        Player player = minecraft.player;
        if (!player.getMainHandItem().is(ItemInit.living_baghnakh.get())) {
            return;
        }
        if (!minecraft.options.keyAttack.isDown()) {
            return;
        }
        if (player.getAttackStrengthScale(0.0F) < 1.0F) {
            return;
        }
        if (!(minecraft.hitResult instanceof EntityHitResult entityHit)) {
            return;
        }
        minecraft.gameMode.attack(player, entityHit.getEntity());
        player.swing(InteractionHand.MAIN_HAND);
        player.resetAttackStrengthTicker();
    }

    static void handleCommonClientTickInput() {
        if (KeyMappings.bloodFormation.consumeClick()) {
            PacketHandler.sendToServer(new BloodFormationKeyPressPacket());
        }

        if (KeyMappings.bloodCrafting.consumeClick()) {
            PacketHandler.sendToServer(new BloodCraftingKeyPressPacket(HLClientUtils.getClientPlayer().getMainHandItem()));
        }

        if (KeyMappings.bloodDraw.isDown()) {
            PacketHandler.sendToServer(new GroundBloodDrawPacket(HLClientUtils.getPartialTicks()));
        }
        if (KeyMappings.toggleGourd.consumeClick()) {
            PacketHandler.sendToServer(new ToggleGourdKeyPacket());
        }
        if (KeyMappings.bloodBallDrop.consumeClick()) {
            BloodBallClientData.drop();
        }
        if (KeyMappings.openMorphlingJarViewer.consumeClick()) {
            PacketHandler.sendToServer(new OpenMorphlingJarPacket());
        }
        if (KeyMappings.cycleSelectedManip.consumeClick()) {
            PacketHandler.sendToServer(new ChangeSelectedManipPacket(HLClientUtils.getPartialTicks()));
        }
        ManipulationChargeInput.tick();
        if (KeyMappings.useStillArt.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && HemoCapabilityAccess.getUnstainedProgress(mc.player).hasClarityUnlocked()) {
                PacketHandler.sendToServer(new UseStillArtKeyPacket());
            }
        }
        handleSilentArchonDoubleTapJump();
    }

    private static void handleSilentArchonDoubleTapJump() {
        Minecraft mc = Minecraft.getInstance();
        boolean noScreenOpen = mc.screen == null;
        if (mc.player == null || mc.level == null || !noScreenOpen) {
            silentArchonJumpWasDown = false;
            return;
        }
        boolean jumpDown = mc.options.keyJump.isDown();
        if (jumpDown && !silentArchonJumpWasDown) {
            long now = mc.level.getGameTime();
            if (SilentArchonArmorAbilityHandler.hasFullSilentArchonSet(mc.player)) {
                if (now - lastSilentArchonJumpTapTick <= SILENT_ARCHON_DOUBLE_TAP_JUMP_WINDOW) {
                    PacketHandler.sendToServer(new ToggleSilentSlippingC2SPacket());
                    lastSilentArchonJumpTapTick = -1000L;
                } else {
                    lastSilentArchonJumpTapTick = now;
                }
            } else {
                lastSilentArchonJumpTapTick = -1000L;
            }
        }
        silentArchonJumpWasDown = jumpDown;
    }

    static void handleRadialMenuTick() {
        Minecraft mc = Minecraft.getInstance();
        handleCrossbarRadialTick(mc);
        if (mc.screen == null && KeyMappings.selectStillArt.consumeClick() && mc.player != null
                && HemoCapabilityAccess.getUnstainedProgress(mc.player).hasClarityUnlocked()) {
            mc.setScreen(new RadialChooseStillArtScreen());
        }
        if (mc.screen == null) {
            boolean vascCharmKeyIsDown = KeyMappings.openVascCharmMenu.isDown();

            if (vascCharmKeyIsDown && !menuKey) {

                while (KeyMappings.openVascCharmMenu.consumeClick()) {
                    if (mc.screen == null && mc.player != null) {
                        var inv = HemoCapabilityAccess.getEquipment(mc.player);
                        if (inv.getStackInSlot(5).getItem() instanceof VasculariumCharmItem charm) {
                            mc.setScreen(new RadialChooseManipScreen(inv));
                        }
                    }
                }
            }
            menuKey = vascCharmKeyIsDown;

        } else {
            menuKey = true;
        }

    }

    private static void handleCrossbarRadialTick(Minecraft mc) {
        if (!KeyMappings.isKeyDown(mc.options.keyUse)) {
            crossbarRadialOpened = false;
            return;
        }
        if (mc.screen instanceof CrossbarRadialScreen) return;
        if (mc.player == null || !mc.player.isUsingItem()
                || !(mc.player.getUseItem().getItem() instanceof MarionetteCrossbarItem)) return;
        if (!MarionetteCrossbarItem.isBoundTo(mc.player.getUseItem(), mc.player)) return;
        if (mc.screen == null && !crossbarRadialOpened
                && mc.player.getTicksUsingItem() >= MarionetteCrossbarItem.RADIAL_HOLD_TICKS) {
            ItemStack stack = mc.player.getUseItem();
            java.util.UUID id = MarionetteCrossbarItem.getCrossbarId(stack);
            if (id != null) {
                crossbarRadialOpened = true;
                mc.setScreen(new CrossbarRadialScreen(stack, id));
            }
        }
    }
}
