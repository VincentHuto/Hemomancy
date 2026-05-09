package com.vincenthuto.hemomancy.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveBloodCraftClientData;
import com.vincenthuto.hemomancy.client.data.BloodBallClientData;
import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronPillarRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronSpikeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronWallRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.HollowVesselRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.AnnettaKnowlesRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.LatentAnnettaInfectionRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.StainedPriestessRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.putriciel.PutricielRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.seraphae.ContainmentAnchorRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.seraphae.SeraphaeFragmentRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.seraphae.SeraphaeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.velorum.VelorumRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.animal.CrimsonDoeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.animal.FunglingRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.animal.LeechRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.animal.VenousStriderRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.BarbedUrchinRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.HemojellyRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.*;
import com.vincenthuto.hemomancy.client.render.entity.npc.*;
import com.vincenthuto.hemomancy.client.render.entity.projectile.*;
import com.vincenthuto.hemomancy.client.render.entity.summon.*;
import com.vincenthuto.hemomancy.client.render.item.MorphicNectarItemDecorator;
import com.vincenthuto.hemomancy.client.render.item.MorphlingPolypItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.QliphothSeedItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.ScarPatternBakedModel;
import com.vincenthuto.hemomancy.client.render.item.ScarPatternItemColor;
import com.vincenthuto.hemomancy.client.render.tile.SuspendedBloodCrystalRenderer;
import com.vincenthuto.hemomancy.client.render.tile.SuspendedCleansedBloodCrystalRenderer;
import com.vincenthuto.hemomancy.client.render.tile.SuspendedVivaniteRenderer;
import com.vincenthuto.hemomancy.client.render.tile.crafting.*;
import com.vincenthuto.hemomancy.client.render.tile.functional.*;
import com.vincenthuto.hemomancy.client.render.world.*;
import com.vincenthuto.hemomancy.client.screen.item.PuppeteersSpindleScreen;
import com.vincenthuto.hemomancy.client.screen.item.StructureSpawnerScreen;
import com.vincenthuto.hemomancy.client.screen.item.TendencyViewScreen;
import com.vincenthuto.hemomancy.client.screen.item.VascularViewScreen;
import com.vincenthuto.hemomancy.client.screen.item.living.LivingStaffScreen;
import com.vincenthuto.hemomancy.client.screen.item.living.LivingSyringeScreen;
import com.vincenthuto.hemomancy.client.screen.item.living.MorphlingJarScreen;
import com.vincenthuto.hemomancy.client.screen.item.living.MorphlingJarViewerScreen;
import com.vincenthuto.hemomancy.client.screen.manips.RadialChooseManipScreen;
import com.vincenthuto.hemomancy.client.screen.manips.RadialChooseVeinScreen;
import com.vincenthuto.hemomancy.client.screen.overlay.*;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.*;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.scar.ScarBinderScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.scar.ScarStationScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.HarbingerEquipmentScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.MnemonicReliquaryScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.SporeImplantScreen;
import com.vincenthuto.hemomancy.client.screen.unstained.RadialChooseStillArtScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.RenderBloodLaserEvent;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonClientState;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.MorphicNectarMutationRules;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.DrudgeElectrodeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingCrossbowItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.item.shared.HemoItemProperties;
import com.vincenthuto.hemomancy.common.item.shared.StructureScannerItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.ChangeSelectedManipPacket;
import com.vincenthuto.hemomancy.common.network.capa.manips.UseManipKeyPacket;
import com.vincenthuto.hemomancy.common.network.capa.unstained.UseStillArtKeyPacket;
import com.vincenthuto.hemomancy.common.network.keybind.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.keybind.BloodFormationKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.keybind.ToggleGourdKeyPacket;
import com.vincenthuto.hemomancy.common.network.morphling.OpenMorphlingJarPacket;
import com.vincenthuto.hemomancy.common.network.particle.GroundBloodDrawPacket;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.render.item.RenderItemArmBanner;
import com.vincenthuto.hutoslib.client.render.item.RenderItemGuideBook;
import com.vincenthuto.hutoslib.common.item.ItemArmBanner;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT, modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ClientEvents {

    public static final KeyMapping bloodFormation = new KeyMapping("key.hemomancy.bloodformation.desc", GLFW.GLFW_KEY_F,
            "key.hemomancy.category");
    public static final KeyMapping bloodCrafting = new KeyMapping("key.hemomancy.bloodcrafting.desc", GLFW.GLFW_KEY_C,
            "key.hemomancy.category");
    public static final KeyMapping bloodDraw = new KeyMapping("key.hemomancy.drawtest.desc", GLFW.GLFW_KEY_LEFT_CONTROL,
            "key.hemomancy.category");
    public static final KeyMapping cycleSelectedManip = new KeyMapping("key.hemomancy.cyclemanip.desc", GLFW.GLFW_KEY_C,
            "key.hemomancy.category");
    public static final KeyMapping useQuickManip = new KeyMapping("key.hemomancy.quickusemanip.desc", GLFW.GLFW_KEY_R,
            "key.hemomancy.category");
    public static final KeyMapping useContManip = new KeyMapping("key.hemomancy.contusemanip.desc", GLFW.GLFW_KEY_G,
            "key.hemomancy.category");
    public static final KeyMapping useManip = new KeyMapping("key.hemomancy.usemanip.desc", GLFW.GLFW_KEY_R,
            "key.hemomancy.category");
    public static final KeyMapping OPEN_CHARM_SLOT_KEYBIND = new KeyMapping("key.charm_slot.slot", GLFW.GLFW_KEY_B,
            "key.hemomancy.category");
    public static final KeyMapping openVascCharmMenu = new KeyMapping("key.charm_slot.open", 90,
            "key.hemomancy.category");
    public static final KeyMapping toggleGourd = new KeyMapping("key.hemomancy.togglegourd.desc", GLFW.GLFW_KEY_H,
            "key.hemomancy.category");
    public static final KeyMapping openMorphlingJarViewer = new KeyMapping("key.hemomancy.openmorphlingjar.desc",
            GLFW.GLFW_KEY_B, "key.hemomancy.category");
    public static final KeyMapping bloodBallDrop = new KeyMapping("key.hemomancy.bloodballdrop.desc", GLFW.GLFW_KEY_V,
            "key.hemomancy.category");

    private static boolean menuKey = false;

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        handleCommonClientTickInput();
        handleRadialMenuTick();
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        ManipCooldownOverlay.tick();
        StillArtCooldownOverlay.tick();
        ActiveBloodCraftClientData.tick();
        BloodBallClientData.tick();
        SanguineMonolithShatterRenderer.tick();
        if (FungalWhisperVignetteOverlay.instance != null) {
            FungalWhisperVignetteOverlay.instance.tick();
        }
        handleCommonClientTickInput();
    }

    private static void handleCommonClientTickInput() {
        if (bloodFormation.consumeClick()) {
            PacketHandler.sendToServer(new BloodFormationKeyPressPacket());
        }

        if (bloodCrafting.consumeClick()) {
            PacketHandler.sendToServer(new BloodCraftingKeyPressPacket(HLClientUtils.getClientPlayer().getMainHandItem()));
        }

        if (bloodDraw.isDown()) {
            PacketHandler.sendToServer(new GroundBloodDrawPacket(HLClientUtils.getPartialTicks()));
        }
        if (toggleGourd.consumeClick()) {
            PacketHandler.sendToServer(new ToggleGourdKeyPacket());
        }
        if (bloodBallDrop.consumeClick()) {
            BloodBallClientData.drop();
        }
        if (openMorphlingJarViewer.consumeClick()) {
            if (Screen.hasShiftDown()) {
                PacketHandler.sendToServer(new OpenMorphlingJarPacket());
            } else {
                MorphlingJarViewerScreen.openScreen();
            }
        }
        if (cycleSelectedManip.consumeClick()) {
            PacketHandler.sendToServer(new ChangeSelectedManipPacket(HLClientUtils.getPartialTicks()));
        }
        if (useManip.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (HemoCapabilityAccess.getUnstainedProgress(mc.player)
                        .map(progress -> progress.hasClarityUnlocked())
                        .orElse(false)) {
                    PacketHandler.sendToServer(new UseStillArtKeyPacket());
                    return;
                }
                HemoCapabilityAccess.getKnownManipulations(mc.player).ifPresent(manip -> {
                    if (manip.getSelectedManip() != null
                            && manip.getSelectedManip().getName().equals("venous_travel")) {
                        mc.setScreen(new RadialChooseVeinScreen(manip));
                    } else {
                        PacketHandler.sendToServer(new UseManipKeyPacket(HLClientUtils.getPartialTicks()));
                    }
                });
            }
        }
    }

    private static void handleRadialMenuTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            boolean vascCharmKeyIsDown = openVascCharmMenu.isDown();

            if (vascCharmKeyIsDown && !menuKey) {

                while (openVascCharmMenu.consumeClick()) {
                    if (mc.screen == null && mc.player != null) {
                        if (HemoCapabilityAccess.getUnstainedProgress(mc.player)
                                .map(progress -> progress.hasClarityUnlocked())
                                .orElse(false)) {
                            mc.setScreen(new RadialChooseStillArtScreen());
                            continue;
                        }
                        HemoCapabilityAccess.getScars(mc.player).ifPresent(inv -> {
                            if (inv.getStackInSlot(5).getItem() instanceof VasculariumCharmItem charm) {
                                mc.setScreen(new RadialChooseManipScreen(inv));
                            }
                        });
                    }
                }
            }
            menuKey = vascCharmKeyIsDown;

        } else {
            menuKey = true;
        }

    }

    public static boolean isKeyDown(KeyMapping keybind) {
        if (keybind.isUnbound())
            return false;

        boolean isDown = switch (keybind.getKey().getType()) {
            case KEYSYM ->
                    InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(),
                    keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
            default -> false;
        };
        return isDown && keybind.getKeyConflictContext().isActive()
                && keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().has(DataComponentInit.STRUCTURE_SCANNER_TOOLTIP.get())) {
            event.getItemStack().addToTooltip(DataComponentInit.STRUCTURE_SCANNER_TOOLTIP.get(), event.getContext(),
                    event.getToolTip()::add, event.getFlags());
        }

        if (event.getItemStack().getItem() instanceof StructureScannerItem scanner) {
            scanner.addScannerStateTooltip(event.getItemStack(), event.getToolTip());
        }

        appendMorphicNectarMutationTooltip(event);
        appendFirstHourTooltip(event);
    }

    private static void appendMorphicNectarMutationTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!MorphicNectarMutationRules.shouldShowMutation(stack)) {
            return;
        }
        boolean primal = MorphlingItem.isPrimal(stack);
        event.getToolTip().add(Component.translatable(primal
                        ? "tooltip.hemomancy.morphic_nectar_mutated.primal"
                        : "tooltip.hemomancy.morphic_nectar_mutated")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    private static void appendFirstHourTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        if (item == ItemInit.gourd_seeds.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.gourd_seeds.tooltip")
                    .withStyle(ChatFormatting.GRAY));
        } else if (item == ItemInit.hematic_iron_scrap.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.hematic_iron_scrap.tooltip")
                    .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("item.hemomancy.hematic_iron_scrap.tooltip.nugget")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else if (item == ItemInit.charm_of_vascularium.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.charm_of_vascularium.tooltip.first_hour")
                    .withStyle(ChatFormatting.DARK_RED));
        } else if (item == ItemInit.liber_sanguinum.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.liber_sanguinum.tooltip.first_hour")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void onClientPlayerLogout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        // HutosLib now retains read tracker state across disconnect/reload.
    }

    @SubscribeEvent
    public static void renderMorphicNectarScreenOverlay(RenderGuiEvent.Pre event) {
        MorphicNectarScreenOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void renderWhiteHumorScreenOverlay(RenderGuiEvent.Pre event) {
        WhiteHumorScreenOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void renderLevelLastEvent(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            renderBloodMoonSky(event);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            CardinalRiteBoundaryRenderer.render(event.getPoseStack(), partialTick);
            UnstainedRiteBoundaryRenderer.render(event.getPoseStack(), partialTick);
            GourdVineRenderer.render(event.getPoseStack(), partialTick);
            BloodCraftRingRenderer.render(event.getPoseStack(), partialTick);
            QliphothBloomRenderer.render(event.getPoseStack(), partialTick);
            BloodBallRenderer.render(event.getPoseStack(), partialTick);
            SanguineMonolithShatterRenderer.render(event.getPoseStack(), partialTick);
            PuppeteerThreadRenderer.render(event.getPoseStack(), partialTick);
            HematicSutureLinkRenderer.render(event.getPoseStack(), partialTick);
        }
    }

    private static void renderBloodMoonSky(RenderLevelStageEvent event) {
        if (!BloodMoonClientState.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.effects().skyType() != DimensionSpecialEffects.SkyType.NORMAL) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        PoseStack moonPose = new PoseStack();
        moonPose.mulPose(event.getModelViewMatrix());
        moonPose.mulPose(Axis.YP.rotationDegrees(-90.0F));
        moonPose.mulPose(Axis.XP.rotationDegrees(mc.level.getTimeOfDay(partialTick) * 360.0F));
        BloodMoonVeinSkyRenderer.renderInSky(moonPose, mc.level, partialTick);
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void cameraView(EntityEvent.Size event) {
        if (event.getEntity() instanceof Player player) {
            HemoCapabilityAccess.getKnownManipulations(player).ifPresent((manip) -> {
                if (manip.isAvatarActive()) {
                    event.setNewSize(player.getDimensions(Pose.STANDING).scale(2));
                } else if (player.isCrouching()) {
                    event.setNewSize(player.getDimensions(Pose.CROUCHING));
                } else {
                    event.setNewSize(player.getDimensions(Pose.STANDING));
                }
            });
        }
    }

    public static void renderPlayerArm(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight,
                                       float pEquippedProgress, float pSwingProgress, HumanoidArm pSide) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = pSide != HumanoidArm.LEFT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = Mth.sqrt(pSwingProgress);
        float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * Mth.sin(f1 * ((float) Math.PI * 2F));
        float f4 = -0.4F * Mth.sin(pSwingProgress * (float) Math.PI);
        pMatrixStack.translate(f * (f2 + 0.64000005F), f3 + -0.6F + pEquippedProgress * -0.6F, f4 + -0.71999997F);
        pMatrixStack.mulPose(Vector3.YP.rotationDegrees(f * 45.0F).toMoj());
        float f5 = Mth.sin(pSwingProgress * pSwingProgress * (float) Math.PI);
        float f6 = Mth.sin(f1 * (float) Math.PI);
        pMatrixStack.mulPose(Vector3.YP.rotationDegrees(f * f6 * 70.0F).toMoj());
        pMatrixStack.mulPose(Vector3.ZP.rotationDegrees(f * f5 * -20.0F).toMoj());
        AbstractClientPlayer abstractclientplayer = minecraft.player;
        RenderSystem.setShaderTexture(0, abstractclientplayer.getSkin().texture());
        pMatrixStack.translate(f * -1.0F, 3.6F, 3.5D);
        pMatrixStack.mulPose(Vector3.ZP.rotationDegrees(f * 120.0F).toMoj());
        pMatrixStack.mulPose(Vector3.XP.rotationDegrees(200.0F).toMoj());
        pMatrixStack.mulPose(Vector3.YP.rotationDegrees(f * -135.0F).toMoj());
        pMatrixStack.translate(f * 5.6F, 0.0D, 0.0D);
        pMatrixStack.scale(2, 2, 2);
        PlayerRenderer playerrenderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher()
                .getRenderer(abstractclientplayer);
        if (flag) {
            playerrenderer.renderRightHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayer);
        } else {
            playerrenderer.renderLeftHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayer);
        }

    }

    @SubscribeEvent
    public static void renderPlayerSize(RenderPlayerEvent.Pre event) {
        HemoCapabilityAccess.getKnownManipulations(event.getEntity()).ifPresent((manip) -> {
            if (manip.isAvatarActive()) {
                event.getPoseStack().translate(0, 2, 0);
            }
        });
    }

    @EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
    public static class ClientModBusEvents {

        public static BakedModel bloodAbsorptionModel, bloodProjectionModel;

        @SubscribeEvent
        public static void registerDimEffects(RegisterDimensionSpecialEffectsEvent event) {
            DimensionSpecialEffects fungalEffects =
                    new FungalRealmsRenderInfo(Float.NaN, true, DimensionSpecialEffects.SkyType.END, true, true);
            event.register(Hemomancy.rloc("fungal_gardens"), fungalEffects);
            // Legacy alias in case an older save still points to hemomancy:renderer.
            event.register(Hemomancy.rloc("renderer"), fungalEffects);
        }

        @SubscribeEvent
        public static void renderEntities(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntityInit.directed_blood_orb.get(), BloodOrbDirectedRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_cloud_carrier.get(), BloodCloudCarrierRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_cloud.get(), BloodCloudRenderer::new);
            event.registerEntityRenderer(EntityInit.tracking_blood_orb.get(), BloodOrbTrackingRenderer::new);
            event.registerEntityRenderer(EntityInit.tracking_snake.get(), TrackingSerpentRenderer::new);
            event.registerEntityRenderer(EntityInit.tracking_pests.get(), TrackingPestsRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_bolt.get(), BloodBoltRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_needle.get(), BloodNeedleRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_shot.get(), BloodShotRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_bullet.get(), BloodBulletRenderer::new);
            event.registerEntityRenderer(EntityInit.morphling_polyp_item.get(), MorphlingPolypItemRenderer::new);
            event.registerEntityRenderer(EntityInit.qliphoth_seed_item.get(), QliphothSeedItemRenderer::new);
            event.registerEntityRenderer(EntityInit.iron_pillar.get(), IronPillarRenderer::new);
            event.registerEntityRenderer(EntityInit.iron_spike.get(), IronSpikeRenderer::new);
            event.registerEntityRenderer(EntityInit.iron_wall.get(), IronWallRenderer::new);
            event.registerEntityRenderer(EntityInit.wretched_will.get(), WretchedWillRenderer::new);
            event.registerEntityRenderer(EntityInit.leech.get(), LeechRenderer::new);
            event.registerEntityRenderer(EntityInit.iron_pillar.get(), IronPillarRenderer::new);
            event.registerEntityRenderer(EntityInit.iron_spike.get(), IronSpikeRenderer::new);
            event.registerEntityRenderer(EntityInit.iron_wall.get(), IronWallRenderer::new);
            event.registerEntityRenderer(EntityInit.fargone.get(), FargoneRenderer::new);
            event.registerEntityRenderer(EntityInit.thirster.get(), ThirsterRenderer::new);
            event.registerEntityRenderer(EntityInit.fungling.get(), FunglingRenderer::new);
            event.registerEntityRenderer(EntityInit.tooth_pecks.get(), ToothPecksRenderer::new);
            event.registerEntityRenderer(EntityInit.chitinite.get(), ChitiniteRenderer::new);
            event.registerEntityRenderer(EntityInit.fervent_chitinite.get(), FerventChitiniteRenderer::new);
            event.registerEntityRenderer(EntityInit.chthonian.get(), ChthonianRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_drunk_puppeteer.get(), BloodDrunkPuppeteerRenderer::new);
            event.registerEntityRenderer(EntityInit.enthralled_doll.get(), EnthralledDollRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_thrall.get(), BloodThrallRenderer::new);
            event.registerEntityRenderer(EntityInit.veinwing_vulture.get(), VeinwingVultureRenderer::new);
            event.registerEntityRenderer(EntityInit.marrow_spitter.get(), MarrowSpitterRenderer::new);
            event.registerEntityRenderer(EntityInit.gorebound_hulk.get(), GoreboundHulkRenderer::new);
            event.registerEntityRenderer(EntityInit.lump_of_thought.get(), LumpOfThoughtRenderer::new);
            event.registerEntityRenderer(EntityInit.chthonian_queen.get(), ChthonianQueenRenderer::new);
            event.registerEntityRenderer(EntityInit.abhorent_thought.get(), AbhorentThoughtRenderer::new);
            event.registerEntityRenderer(EntityInit.barbed_urchin.get(), BarbedUrchinRenderer::new);
            event.registerEntityRenderer(EntityInit.hemolymphopoda.get(), HemolymphopodaRenderer::new);
            event.registerEntityRenderer(EntityInit.erythromycelium_eruptus.get(), ErythromyceliumEruptusRenderer::new);
            event.registerEntityRenderer(EntityInit.morphling_polyp.get(), MorphlingPolypRenderer::new);
            event.registerEntityRenderer(EntityInit.flying_charm.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(EntityInit.hemolytic_vial_projectile.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(EntityInit.sanguis_lancea.get(), SanguisLanceaRenderer::new);
            event.registerEntityRenderer(EntityInit.unstained_zealot.get(), UnstainedZealotRenderer::new);
            event.registerEntityRenderer(EntityInit.unstained_guardian.get(), UnstainedGuardianRenderer::new);
            event.registerEntityRenderer(EntityInit.unstained_acolyte.get(), UnstainedAcolyteRenderer::new);
            event.registerEntityRenderer(EntityInit.unstained_scout.get(), UnstainedScoutRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_hermit.get(), HarbingerHermitRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_alchemist.get(), HarbingerAlchemistRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_mnemonist.get(), HarbingerMnemonistRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_vicar.get(), HarbingerVicarRenderer::new);
            event.registerEntityRenderer(EntityInit.drudge.get(), com.vincenthuto.hemomancy.client.render.entity.npc.DrudgeRenderer::new);
            event.registerEntityRenderer(EntityInit.hollow_vessel.get(), HollowVesselRenderer::new);
            event.registerEntityRenderer(EntityInit.annetta_knowles.get(), AnnettaKnowlesRenderer::new);
            event.registerEntityRenderer(EntityInit.stained_priestess.get(), StainedPriestessRenderer::new);
            event.registerEntityRenderer(EntityInit.latent_annetta_infection.get(), LatentAnnettaInfectionRenderer::new);
            event.registerEntityRenderer(EntityInit.putriciel.get(), PutricielRenderer::new);
            event.registerEntityRenderer(EntityInit.velorum.get(), VelorumRenderer::new);
            event.registerEntityRenderer(EntityInit.seraphae.get(), SeraphaeRenderer::new);
            event.registerEntityRenderer(EntityInit.seraphae_fragment.get(), SeraphaeFragmentRenderer::new);
            event.registerEntityRenderer(EntityInit.containment_anchor.get(), ContainmentAnchorRenderer::new);
            event.registerEntityRenderer(EntityInit.hematic_construct.get(), HematicConstructRenderer::new);
            event.registerEntityRenderer(EntityInit.spectral_companion.get(), SpectralCompanionRenderer::new);
            event.registerEntityRenderer(EntityInit.dark_arrow.get(), BloodShotRenderer::new);
            event.registerEntityRenderer(EntityInit.dessicant.get(), DessicantRenderer::new);
            event.registerEntityRenderer(EntityInit.cruor_fiend.get(), CruorFiendRenderer::new);
            event.registerEntityRenderer(EntityInit.void_drinker.get(), VoidDrinkerRenderer::new);
            event.registerEntityRenderer(EntityInit.frozen_clot.get(), FrozenClotRenderer::new);
            event.registerEntityRenderer(EntityInit.abyssal_siphon.get(), AbyssalSiphonRenderer::new);
            event.registerEntityRenderer(EntityInit.synapse_hound.get(), SynapseHoundRenderer::new);
            event.registerEntityRenderer(EntityInit.myelin_borer.get(), MyelinBorerRenderer::new);
            event.registerEntityRenderer(EntityInit.crimson_doe.get(), CrimsonDoeRenderer::new);
            event.registerEntityRenderer(EntityInit.hemojelly.get(), HemojellyRenderer::new);
            event.registerEntityRenderer(EntityInit.venous_strider.get(), VenousStriderRenderer::new);

        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(FluidInit.WHITE_HUMOR.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidInit.WHITE_HUMOR_FLOWING.get(), RenderType.translucent());
            });
            NeoForge.EVENT_BUS.register(RenderBloodLaserEvent.class);
            BloodVolumeOverlay.instance = new BloodVolumeOverlay();
            EquippedMorphlingOverlay.instance = new EquippedMorphlingOverlay();
            ManipCooldownOverlay.instance = new ManipCooldownOverlay();
            StillArtCooldownOverlay.instance = new StillArtCooldownOverlay();
            UnstainedGaugeOverlay.instance = new UnstainedGaugeOverlay();
            FungalWhisperVignetteOverlay.instance = new FungalWhisperVignetteOverlay();
            // Tiles
            BlockEntityRenderers.register(BlockEntityInit.scar_station.get(), ScarStationRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.ghastly_alembic.get(), GhastlyAlembicRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.pallid_retort.get(), PallidRetortRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.morphling_incubator.get(), MorphlingIncubatorRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.mycelial_crucible.get(), MycelialCrucibleRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.mycelial_lantern.get(), MycelialLanternRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.morphling_cradle.get(), MorphlingCradleRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.specimen_jar.get(), SpecimenJarRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.unstained_podium.get(), UnstainedPodiumRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.scrying_podium.get(), ScryingPodiumRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.fungal_podium.get(), FungalPodiumRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.fungal_implantation_pylon.get(),
                    FungalImplantationPylonRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.suspended_vivianite.get(),
                    SuspendedVivaniteRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.suspended_blood_crystal.get(),
                    SuspendedBloodCrystalRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.suspended_cleansed_blood_crystal.get(),
                    SuspendedCleansedBloodCrystalRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.dendritic_distributor.get(),
                    DendriticDistributorRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.mortal_display.get(), MortalDisplayRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.vial_centrifuge.get(), VialCentrifugeRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.somatic_loom.get(),
                    SomaticLoomRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.earthen_vein.get(), EarthenVeinRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.mnemonic_reliquary.get(), MnemonicReliquaryRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.dictation_table.get(), DictationTableRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.visceral_mirror.get(), VisceralMirrorRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.qliphoth_bloom.get(),
                    QliphothBloomBlockRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.saint_sarcophagus.get(),
                    SaintSarcophagusRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.altar_of_cleansing.get(),
                    AltarOfCleansingRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.sanguine_monolith.get(),
                    SanguineMonolithRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.sanguine_conduit.get(),
                    SanguineConduitBlockRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.covenant_throne.get(),
                    CovenantThroneRenderer::new);
        }

        @SuppressWarnings("deprecation")
        @SubscribeEvent
        public static void registerItemPropertyOverrides(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemProperties.register(ItemInit.unsigned_ancestral_ledger.get(), Hemomancy.rloc("unsigned"),
                        HemoItemProperties.booleanTag("state"));

                ItemProperties.register(ItemInit.bloody_vial.get(), Hemomancy.rloc("state"),
                        HemoItemProperties.booleanTag("state"));

                ItemProperties.register(ItemInit.vial_rack.get(), Hemomancy.rloc("state"),
                        (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) -> {
                            int emptyCount = VialRackItem.countEmptyVials(stack);
                            if (emptyCount == VialRackItem.MAX_VIALS) {
                                return 0.0F;
                            }
                            if (emptyCount == 0) {
                                return 2.0F;
                            }
                            return 1.0F;
                        });

                ItemProperties.register(ItemInit.barbed_shield.get(), ResourceLocation.withDefaultNamespace("blocking"),
                        (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                                ent != null && ent.isUsingItem() && ent.getUseItem() == stack ? 1.0F : 0.0F);

                ItemProperties.register(ItemInit.chitinite_shield.get(), ResourceLocation.withDefaultNamespace("blocking"),
                        (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                                ent != null && ent.isUsingItem() && ent.getUseItem() == stack ? 1.0F : 0.0F);

                ItemProperties.register(ItemInit.living_crossbow.get(), ResourceLocation.withDefaultNamespace("pull"),
                        (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) -> {
                            if (ent == null) {
                                return 0.0F;
                            }
                            return LivingCrossbowItem.isCharged(stack) ? 0.0F
                                    : (float) (stack.getUseDuration(ent) - ent.getUseItemRemainingTicks())
                                      / (float) LivingCrossbowItem.getChargeTime(stack);
                        });
                ItemProperties.register(ItemInit.living_crossbow.get(), ResourceLocation.withDefaultNamespace("pulling"),
                        (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                                ent != null && ent.isUsingItem() && ent.getUseItem() == stack
                                        && !LivingCrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
                ItemProperties.register(ItemInit.living_crossbow.get(), ResourceLocation.withDefaultNamespace("charged"),
                        (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                                stack != null && LivingCrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
                ItemProperties.register(ItemInit.living_crossbow.get(), ResourceLocation.withDefaultNamespace("firework"),
                        (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                                ent != null && LivingCrossbowItem.isCharged(stack)
                                        && LivingCrossbowItem.hasChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F);

                ItemProperties.register(ItemInit.living_syringe.get(), Hemomancy.rloc("open"),
                        HemoItemProperties.booleanTag("state"));
                ItemProperties.register(ItemInit.curved_horn.get(), Hemomancy.rloc("open"),
                        HemoItemProperties.booleanTag("state"));
                ItemProperties.register(ItemInit.blood_gourd_white.get(), Hemomancy.rloc("open"),
                        HemoItemProperties.booleanTag("state"));
                ItemProperties.register(ItemInit.blood_gourd_red.get(), Hemomancy.rloc("open"),
                        HemoItemProperties.booleanTag("state"));
                ItemProperties.register(ItemInit.blood_gourd_black.get(), Hemomancy.rloc("open"),
                        HemoItemProperties.booleanTag("state"));
                ItemProperties.register(ItemInit.morphling_jar.get(), Hemomancy.rloc("size"),
                        HemoItemProperties.intTag("size"));

                ItemProperties.register(ItemInit.drudge_electrode.get(), Hemomancy.rloc("mode"),
                        HemoItemProperties.booleanTag(DrudgeElectrodeItem.TAG_MODE));

                ItemProperties.register(ItemInit.living_staff.get(), Hemomancy.rloc("morph"), new ItemPropertyFunction() {
                    @Override
                    public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int seed) {
                        if (!stack.has(DataComponents.CUSTOM_DATA)) {
                            return 0.0F;
                        }
                        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                        CompoundTag items = (CompoundTag) tag.get("Inventory");
                        if (items == null || !items.contains("Items", 9)) {
                            return 0.0F;
                        }

                        ItemStack selectedStack = ItemStack.parseOptional(
                                world != null ? world.registryAccess() : RegistryAccess.EMPTY,
                                ((ListTag) items.get("Items")).getCompound(0));
                        if (selectedStack.getItem() == ItemInit.morphling_serpent.get()) {
                            return 1.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_leeches.get()) {
                            return 2.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_fungal.get()) {
                            return 3.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_pests.get()) {
                            return 4.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_chitinite.get()) {
                            return 5.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_spider.get()) {
                            return 6.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_moth.get()) {
                            return 7.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_tick.get()) {
                            return 8.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_centipede.get()) {
                            return 9.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_bat.get()) {
                            return 10.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_urchin.get()) {
                            return 11.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_mole.get()) {
                            return 12.0F;
                        }
                        return 0.0F;
                    }
                });
            });
        }

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ContainerInit.gourd_charm_inventory.get(), HarbingerEquipmentScreen::new);
            event.register(ContainerInit.fungal_implantation.get(), SporeImplantScreen::new);
            event.register(ContainerInit.vial_centrifuge.get(), VialCentrifugeScreen::new);
            event.register(ContainerInit.morphling_jar.get(), MorphlingJarScreen::new);
            event.register(ContainerInit.living_syringe.get(), LivingSyringeScreen::new);
            event.register(ContainerInit.living_staff.get(), LivingStaffScreen::new);
            event.register(ContainerInit.ghastly_alembic.get(), GhastlyAlembicScreen::new);
            event.register(ContainerInit.pallid_retort.get(), PallidRetortScreen::new);
            event.register(ContainerInit.scar_station.get(), ScarStationScreen::new);
            event.register(ContainerInit.scar_binder.get(), ScarBinderScreen::new);
            event.register(ContainerInit.vascular_view.get(), VascularViewScreen::new);
            event.register(ContainerInit.tendency_view.get(), TendencyViewScreen::new);
            event.register(ContainerInit.morphling_incubator.get(), MorphlingIncubatorScreen::new);
            event.register(ContainerInit.mycelial_crucible.get(), MycelialCrucibleScreen::new);
            event.register(ContainerInit.mycelial_lantern.get(), MycelialLanternScreen::new);
            event.register(ContainerInit.structure_spawner.get(), StructureSpawnerScreen::new);
            event.register(ContainerInit.puppeteers_spindle.get(), PuppeteersSpindleScreen::new);
            event.register(ContainerInit.mnemonic_reliquary.get(), MnemonicReliquaryScreen::new);
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(ClientEvents.bloodFormation);
            event.register(ClientEvents.bloodCrafting);
            event.register(ClientEvents.bloodDraw);
            event.register(ClientEvents.cycleSelectedManip);
            event.register(ClientEvents.useManip);
            event.register(ClientEvents.OPEN_CHARM_SLOT_KEYBIND);
            event.register(ClientEvents.openVascCharmMenu);
            event.register(ClientEvents.toggleGourd);
            event.register(ClientEvents.openMorphlingJarViewer);
            event.register(ClientEvents.bloodBallDrop);

        }

        @SubscribeEvent
        public static void registerClientItemExtensions(RegisterClientExtensionsEvent event) {
            IClientItemExtensions armBannerRenderer = new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return new RenderItemArmBanner(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels());
                }
            };

            IClientItemExtensions guideBookRenderer = new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return new RenderItemGuideBook(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels());
                }
            };

            for (Item item : BuiltInRegistries.ITEM) {
                if (!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Hemomancy.MOD_ID)) {
                    continue;
                }
                if (item instanceof ItemArmBanner) {
                    event.registerItem(armBannerRenderer, item);
                    continue;
                }
                if (item instanceof ItemGuideBook) {
                    event.registerItem(guideBookRenderer, item);
                    continue;
                }
                if (item instanceof HemoClientItemExtensionsProvider provider) {
                    event.registerItem(provider.hemomancy$getClientItemExtensions(), item);
                }
            }
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            ScarPatternItemColor scarPatternColor = new ScarPatternItemColor();
            ItemInit.BASEITEMS.getEntries().stream()
                    .filter(entry -> entry.get() instanceof ItemScarPattern)
                    .forEach(entry -> event.register(scarPatternColor, entry.get()));
        }

        @SubscribeEvent
        public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
            MorphicNectarItemDecorator decorator = new MorphicNectarItemDecorator();
            for (Item item : BuiltInRegistries.ITEM) {
                event.register(item, decorator);
            }
        }


        @SubscribeEvent
        public static void modelRegisterEvent(ModelEvent.RegisterAdditional event) {
            event.register(ModelResourceLocation.standalone(Hemomancy.rloc("item/blood_absorption_texture")));
            event.register(ModelResourceLocation.standalone(Hemomancy.rloc("item/blood_projection_texture")));

        }

        @SubscribeEvent
        public static void onModelBake(BakingCompleted evt) {
            bloodAbsorptionModel = evt.getModels()
                    .get(ModelResourceLocation.standalone(Hemomancy.rloc("item/blood_absorption_texture")));
            bloodProjectionModel = evt.getModels()
                    .get(ModelResourceLocation.standalone(Hemomancy.rloc("item/blood_projection_texture")));
        }

        @SubscribeEvent
        public static void onModifyBakingResult(ModelEvent.ModifyBakingResult evt) {
            // Wrap all Scar Pattern item models so the overlay layer is shrunk down
            for (DeferredHolder<Item, ? extends Item> entry : ItemInit.BASEITEMS.getEntries()) {
                if (entry.get() instanceof ItemScarPattern) {
                    ModelResourceLocation modelLoc = ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(entry.get()));
                    BakedModel existing = evt.getModels().get(modelLoc);
                    if (existing != null) {
                        evt.getModels().put(modelLoc, new ScarPatternBakedModel(existing));
                    }
                }
            }
        }

        // Overlay
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
            event.registerAboveAll(Hemomancy.rloc("bloodvolume"), (graphics, deltaTracker) -> {
                if (BloodVolumeOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    BloodVolumeOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
            event.registerAboveAll(Hemomancy.rloc("equipped_morphling"), (graphics, deltaTracker) -> {
                if (EquippedMorphlingOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    EquippedMorphlingOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
            event.registerAboveAll(Hemomancy.rloc("manip_cooldown"), (graphics, deltaTracker) -> {
                if (ManipCooldownOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    ManipCooldownOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
            event.registerAboveAll(Hemomancy.rloc("still_art_cooldown"), (graphics, deltaTracker) -> {
                if (StillArtCooldownOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    StillArtCooldownOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
            event.registerAboveAll(Hemomancy.rloc("unstained_gauge"), (graphics, deltaTracker) -> {
                if (UnstainedGaugeOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    UnstainedGaugeOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
            event.registerAboveAll(Hemomancy.rloc("fungal_whisper_vignette"), (graphics, deltaTracker) -> {
                if (FungalWhisperVignetteOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    FungalWhisperVignetteOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
        }
    }
}

