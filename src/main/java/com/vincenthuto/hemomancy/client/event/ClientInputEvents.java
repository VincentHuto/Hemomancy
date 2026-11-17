package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.common.network.capa.harbinger.BurnSporeLoadKeyPacket;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.*;
import com.vincenthuto.hemomancy.client.registry.KeyMappings;
import com.vincenthuto.hemomancy.client.screen.manips.PossessedRadialScreen;
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

    /** Venous Cling moves the local player along the face it holds (core-loop Track 9). */
    @SubscribeEvent
    public static void onClingMovementInput(MovementInputUpdateEvent event) {
        com.vincenthuto.hemomancy.client.player.ClingClientMotion.apply(event.getEntity(), event.getInput());
    }

    @SubscribeEvent
    public static void onBloodAbsorptionMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel.channelling(player).isEmpty()) {
            return;
        }
        double movementMultiplier = com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel.movementMultiplier(player);
        // A strand sets its own pace (core-loop Track 12), which replaces vanilla's use-item slowdown even at full speed.
        if (movementMultiplier >= 1.0D
                && !com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel.declaresMovement(player)) {
            return;
        }
        double inputMultiplier = com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel.clientInputMultiplier(player);
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
            PacketHandler.sendToServer(new BloodCraftingKeyPressPacket());
        }

        if (KeyMappings.toggleGourd.consumeClick()) {
            PacketHandler.sendToServer(new ToggleGourdKeyPacket());
        }
        if (KeyMappings.burnSpores.consumeClick()) {
            PacketHandler.sendToServer(new BurnSporeLoadKeyPacket());
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
        if (KeyMappings.bloodVerb.consumeClick()) {
            PacketHandler.sendToServer(com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.BloodVerbKeyPacket.INSTANCE);
        }
        ManipulationChargeInput.tick();
        handleStrandSnap();
        if (KeyMappings.useStillArt.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && HemoCapabilityAccess.getUnstainedProgress(mc.player).hasClarityUnlocked()) {
                PacketHandler.sendToServer(new UseStillArtKeyPacket());
            }
        }
        handleSilentArchonDoubleTapJump();
    }

    /**
     * The tap: left click with a lash selected on a held Projection tool, nothing being channelled, and the crosshair
     * on open air or a creature. The click is the lash's, so vanilla's swing and attack are cancelled. A click on a
     * block, on a body that is no creature, or on a creature while the tool is in the off hand and the main hand holds
     * something is left to vanilla ({@code LashRules.clickStaysVanilla}). One empty payload per click; the server decides.
     */
    @SubscribeEvent
    public static void onLashTap(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandService.tapLash(mc.player) == null) return;
        // The hand that holds the tool throws the lash: it swings, as it would for any strike.
        boolean main = com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel.verbOf(mc.player, mc.player.getMainHandItem())
                .orElse(null) == com.vincenthuto.hemomancy.common.manipulation.verb.Verb.PROJECT;
        net.minecraft.world.phys.HitResult aimed = mc.hitResult;
        boolean onEntity = aimed instanceof net.minecraft.world.phys.EntityHitResult;
        if (com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashRules.clickStaysVanilla(
                aimed != null && aimed.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK, onEntity,
                onEntity && ((net.minecraft.world.phys.EntityHitResult) aimed).getEntity() instanceof net.minecraft.world.entity.LivingEntity,
                main, mc.player.getMainHandItem().isEmpty())) return;
        event.setSwingHand(false);
        event.setCanceled(true);
        mc.player.swing(main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        PacketHandler.sendToServer(com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.LashTapPacket.INSTANCE);
    }

    /** Whether the Sculpting the local player is channelling answers the attack button (a strand, a dome, an ice hand). */
    private static boolean channelTakesAccent(Player player) {
        return com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel.channelling(player)
                .flatMap(verb -> com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher.selected(player, verb))
                .map(selected -> selected.sculpting().takesAccent()).orElse(false);
    }

    private static boolean attackWasDown;

    /**
     * Left click while a strand is out snaps it (core-loop Track 12, CL-22): vanilla drains attack clicks and skips
     * block breaking while an item is in use, so the button is free. One empty payload per press; the server decides.
     * A held lash answers here at once as well (its orb lets go, or its whip comes back to the crosshair), so the
     * click is not felt a round trip late.
     */
    private static void handleStrandSnap() {
        Minecraft mc = Minecraft.getInstance();
        boolean down = mc.screen == null && mc.player != null && mc.options.keyAttack.isDown();
        boolean pressed = down && !attackWasDown;
        attackWasDown = down;
        if (pressed && mc.player.isUsingItem() && channelTakesAccent(mc.player)) {
            com.vincenthuto.hemomancy.client.data.StrandClientState.localSnap(mc.player);
            PacketHandler.sendToServer(com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.StrandSnapPacket.INSTANCE);
        }
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
                    if (mc.screen == null && mc.player != null && HemoCapabilityAccess.isPossessing(mc.player)) {
                        // A will away from its body sees the possession wheel, never its own manipulations.
                        mc.setScreen(new PossessedRadialScreen());
                    } else if (mc.screen == null && mc.player != null) {
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
