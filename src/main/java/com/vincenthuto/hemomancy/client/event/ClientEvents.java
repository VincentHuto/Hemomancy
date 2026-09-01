package com.vincenthuto.hemomancy.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.*;
import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.morphling.MorphlingPlayerPartVisibility;
import com.vincenthuto.hemomancy.client.render.CrimsonFireRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronPillarRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronSpikeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronWallRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.AnnettaKnowlesRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.LatentAnnettaInfectionRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.StainedPriestessRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.endgame.MycophantRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.endgame.VesperTheCrownedRefusalRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.endgame.VesperTheEveningStarRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.hemorath.HemorathRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.putriciel.PutricielRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.seraphae.ContainmentAnchorRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.seraphae.SeraphaeFragmentRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.seraphae.SeraphaeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.velorum.VelorumRenderer;
import com.vincenthuto.hemomancy.client.render.entity.misc.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.animal.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.will.WillAnchorRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.will.WillRenderer;
import com.vincenthuto.hemomancy.client.render.entity.npc.*;
import com.vincenthuto.hemomancy.client.render.entity.projectile.*;
import com.vincenthuto.hemomancy.client.render.entity.summon.*;
import com.vincenthuto.hemomancy.client.render.item.*;
import com.vincenthuto.hemomancy.client.render.scar.OculifloraRevealRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.decoration.NonEuclideanHallwayRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.plant.GourdvineTapRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.SaintSarcophagusRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.rite.IronBrazierRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.rite.SuspendedBloodCrystalRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.rite.SuspendedVivaniteRenderer;
import com.vincenthuto.hemomancy.client.render.tile.inscription.DictationTableRenderer;
import com.vincenthuto.hemomancy.client.render.tile.inscription.DiscoveryInscriptionBlockRenderer;
import com.vincenthuto.hemomancy.client.render.tile.unstained.SuspendedCleansedBloodCrystalRenderer;
import com.vincenthuto.hemomancy.client.render.tile.unstained.crafting.PallidRetortRenderer;
import com.vincenthuto.hemomancy.client.render.tile.unstained.functional.AltarOfCleansingRenderer;
import com.vincenthuto.hemomancy.client.render.world.*;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberOfWillEffects;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.LowtideRuinObjModels;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.VesperFightFloorRenderer;
import com.vincenthuto.hemomancy.client.screen.item.*;
import com.vincenthuto.hemomancy.client.screen.item.living.LivingStaffScreen;
import com.vincenthuto.hemomancy.client.screen.item.living.LivingSyringeScreen;
import com.vincenthuto.hemomancy.client.screen.item.living.MorphlingJarScreen;
import com.vincenthuto.hemomancy.client.screen.manips.RadialChooseManipScreen;
import com.vincenthuto.hemomancy.client.screen.overlay.*;
import com.vincenthuto.hemomancy.client.screen.summon.CrossbarRadialScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.*;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.scar.ScarBinderScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.scar.ScarStationScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.HarbingerEquipmentScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.MasonsEffigyScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.MnemonicReliquaryScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.SporeImplantScreen;
import com.vincenthuto.hemomancy.client.screen.unstained.RadialChooseStillArtScreen;
import com.vincenthuto.hemomancy.client.sound.EndgameBossMusicHandler;
import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.RenderBloodLaserEvent;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.utility.ArmatureRestraintEntity;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonClientState;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.MorphicNectarMutationRules;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.DrudgeElectrodeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingCrossbowItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFittingHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.item.shared.HemoItemProperties;
import com.vincenthuto.hemomancy.common.item.shared.StructureScannerItem;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodFormationKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ToggleGourdKeyPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ToggleSilentSlippingC2SPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ChangeSelectedManipPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket;
import com.vincenthuto.hemomancy.common.network.capa.unstained.UseStillArtKeyPacket;
import com.vincenthuto.hemomancy.common.network.morphling.OpenMorphlingJarPacket;
import com.vincenthuto.hemomancy.common.network.particle.GroundBloodDrawPacket;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.render.item.RenderItemArmBanner;
import com.vincenthuto.hutoslib.client.render.item.RenderItemGuideBook;
import com.vincenthuto.hutoslib.common.item.ItemArmBanner;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
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
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT, modid = Hemomancy.MOD_ID)
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
	private static int manipulationChargeTicks;
	private static String lastManipulationName = "";
	private static EnumManipulationType lastManipulationType;
	private static boolean suppressManipulationUntilRelease;
    public static final KeyMapping useStillArt = new KeyMapping("key.hemomancy.usestillart.desc", GLFW.GLFW_KEY_R,
            "key.hemomancy.category");
    public static final KeyMapping selectStillArt = new KeyMapping("key.hemomancy.selectstillart.desc", GLFW.GLFW_KEY_Z,
            "key.hemomancy.category");
    public static final KeyMapping OPEN_CHARM_SLOT_KEYBIND = new KeyMapping("key.charm_slot.slot", GLFW.GLFW_KEY_B,
            "key.hemomancy.category");
    public static final KeyMapping openVascCharmMenu = new KeyMapping("key.charm_slot.open", GLFW.GLFW_KEY_X,
            "key.hemomancy.category");
    public static final KeyMapping toggleGourd = new KeyMapping("key.hemomancy.togglegourd.desc", GLFW.GLFW_KEY_H,
            "key.hemomancy.category");
    public static final KeyMapping openMorphlingJarViewer = new KeyMapping("key.hemomancy.openmorphlingjar.desc",
            GLFW.GLFW_KEY_B, "key.hemomancy.category");
    public static final KeyMapping bloodBallDrop = new KeyMapping("key.hemomancy.bloodballdrop.desc", GLFW.GLFW_KEY_V,
            "key.hemomancy.category");

    private static boolean menuKey = false;
    private static final int SILENT_ARCHON_DOUBLE_TAP_JUMP_WINDOW = 7;
    private static boolean silentArchonJumpWasDown = false;
    private static long lastSilentArchonJumpTapTick = -1000L;
    private static CameraType cameraBeforeArmature = null;
    private static final float ARMATURE_CAMERA_DISTANCE = 7.0F;

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        handleRadialMenuTick();
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        MnemonicBlueprintRenderer.tick();
        ManipCooldownOverlay.tick();
        StillArtCooldownOverlay.tick();
        ActiveBloodCraftClientData.tick();
        ActiveBloodStructureFeedClientData.tick();
        ActiveBloodStructureOfferingBurstClientData.tick();
        ActiveSanguineFormationProjectionClientData.tick();
        ActiveRiteClientData.tick();
        BloodStructureFeedSpiralParticles.tick();
        BloodBallClientData.tick();
        VeinSpiderCourierClientData.tick();
        SanguineMonolithShatterRenderer.tick();
        ClawSlashRenderer.tick();
        MonolithicDislocationClientState.tick();
        CrimsonFireClientState.tick();
        BloodBindingTendrilClientState.tick();
        if (SanguineOmenOverlay.instance != null) {
            SanguineOmenOverlay.instance.tick();
        }
        if (FungalWhisperVignetteOverlay.instance != null) {
            FungalWhisperVignetteOverlay.instance.tick();
        }
		ChamberVisitOverlay.tick();
        if (WillPresenceOverlay.instance != null) {
            WillPresenceOverlay.instance.tick();
        }
        EndgameBossMusicHandler.tick();
        handleArmatureCameraFallback();
        handleLivingBaghnakhAutoAttack();
        handleCommonClientTickInput();
    }

    private static void handleLivingBaghnakhAutoAttack() {
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

    @SubscribeEvent
    public static void onRenderBlockScreenEffect(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() != RenderBlockScreenEffectEvent.OverlayType.FIRE
                || !CrimsonFireClientState.isActive(event.getPlayer())) {
            return;
        }
        CrimsonFireRenderer.renderScreenOverlay(Minecraft.getInstance(), event.getPoseStack());
        event.setCanceled(true);
    }

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

    private static void handleArmatureCameraFallback() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            cameraBeforeArmature = null;
            return;
        }
        boolean mountedOnArmature = mc.player.getVehicle() instanceof ArmatureRestraintEntity;
        if (mountedOnArmature) {
            if (cameraBeforeArmature == null) {
                cameraBeforeArmature = mc.options.getCameraType();
            }
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        } else if (cameraBeforeArmature != null) {
            mc.options.setCameraType(cameraBeforeArmature);
            cameraBeforeArmature = null;
        }
    }

    @SubscribeEvent
    public static void onDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        if (event.getCamera().getEntity() instanceof Player player
                && player.getVehicle() instanceof ArmatureRestraintEntity) {
            event.setDistance(Math.max(event.getDistance(), ARMATURE_CAMERA_DISTANCE));
        }
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
            PacketHandler.sendToServer(new OpenMorphlingJarPacket());
        }
        if (cycleSelectedManip.consumeClick()) {
            PacketHandler.sendToServer(new ChangeSelectedManipPacket(HLClientUtils.getPartialTicks()));
        }
		handleManipulationInput();
        if (useStillArt.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && HemoCapabilityAccess.getUnstainedProgress(mc.player)
                    .map(progress -> progress.hasClarityUnlocked()).orElse(false)) {
                PacketHandler.sendToServer(new UseStillArtKeyPacket());
            }
        }
        handleSilentArchonDoubleTapJump();
    }

	private static void handleManipulationInput() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			manipulationChargeTicks = 0;
			lastManipulationName = "";
			lastManipulationType = null;
			suppressManipulationUntilRelease = false;
			return;
		}
		HemoCapabilityAccess.getKnownManipulations(mc.player).ifPresent(known -> {
			var selected = known.getSelectedManip() == null ? null
					: ManipulationInit.getByName(known.getSelectedManip().getName());
			if (selected == null) {
				if (lastManipulationType == EnumManipulationType.CONTINUOUS && manipulationChargeTicks > 0) {
					PacketHandler.sendToServer(UseManipKeyPacket.stopContinuous());
				}
				manipulationChargeTicks = 0;
				lastManipulationName = "";
				lastManipulationType = null;
				return;
			}
			boolean down = useManip.isDown();
			boolean clicked = useManip.consumeClick();
			if (!selected.getName().equals(lastManipulationName)) {
				if (lastManipulationType == EnumManipulationType.CONTINUOUS && manipulationChargeTicks > 0) {
					PacketHandler.sendToServer(UseManipKeyPacket.stopContinuous());
				}
				manipulationChargeTicks = 0;
				lastManipulationName = selected.getName();
				lastManipulationType = selected.getType();
				suppressManipulationUntilRelease = down && !clicked;
			}
			if (suppressManipulationUntilRelease) {
				if (down) return;
				suppressManipulationUntilRelease = false;
			}
			if (selected.getType() == EnumManipulationType.CHARGED && down
					&& mc.player.hurtTime > 0 && mc.player.hurtTime == mc.player.hurtDuration
					&& manipulationChargeTicks > 0) {
				manipulationChargeTicks = com.vincenthuto.hemomancy.common.capability.player.shared.skill.BodyRefinementSkillRules
						.retainedChargeTicks(manipulationChargeTicks,
								SkillPointHelper.getNervesOfSteelLevel(mc.player));
			}
			var input = ManipulationInputRules.tick(selected.getType(), down, clicked,
					manipulationChargeTicks, selected.getRequiredChargeTicks());
			manipulationChargeTicks = input.nextHeldTicks();
			switch (input.action()) {
				case NONE -> {
				}
				case START_CONTINUOUS -> PacketHandler.sendToServer(UseManipKeyPacket.startContinuous());
				case STOP_CONTINUOUS -> PacketHandler.sendToServer(UseManipKeyPacket.stopContinuous());
				case CAST -> PacketHandler.sendToServer(new UseManipKeyPacket(input.castTicks()));
			}
		});
	}

	public static int getManipulationChargeTicks() {
		return manipulationChargeTicks;
	}

	public static void manipulationCastAccepted() {
		var manipulation = ManipulationInit.getByName(lastManipulationName);
		if (lastManipulationType == EnumManipulationType.CHARGED && manipulation != null
				&& manipulationChargeTicks >= manipulation.getRequiredChargeTicks()) {
			manipulationChargeTicks = 0;
		}
	}

	public static String getChargingManipulationName() {
		return lastManipulationType == EnumManipulationType.CHARGED && manipulationChargeTicks > 0
				? lastManipulationName : "";
	}

	public static float getManipulationChargeProgress() {
		var manipulation = ManipulationInit.getByName(getChargingManipulationName());
		return manipulation == null ? 0.0F
				: com.vincenthuto.hemomancy.common.manipulation.ManipulationCastingRules.chargeFraction(
						manipulationChargeTicks, manipulation.getRequiredChargeTicks());
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

    private static void handleRadialMenuTick() {
        Minecraft mc = Minecraft.getInstance();
		handleCrossbarRadialTick(mc);
        if (mc.screen == null && selectStillArt.consumeClick() && mc.player != null
                && HemoCapabilityAccess.getUnstainedProgress(mc.player)
                .map(progress -> progress.hasClarityUnlocked()).orElse(false)) {
            mc.setScreen(new RadialChooseStillArtScreen());
        }
        if (mc.screen == null) {
            boolean vascCharmKeyIsDown = openVascCharmMenu.isDown();

            if (vascCharmKeyIsDown && !menuKey) {

                while (openVascCharmMenu.consumeClick()) {
                    if (mc.screen == null && mc.player != null) {
                        HemoCapabilityAccess.getEquipment(mc.player).ifPresent(inv -> {
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
        } else if (item == ItemInit.chalybeate_sclerite.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.chalybeate_sclerite.tooltip")
                    .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("item.hemomancy.chalybeate_sclerite.tooltip.harvest")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else if (item == ItemInit.erythrocoral_fragment.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.erythrocoral_fragment.tooltip")
                    .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("item.hemomancy.erythrocoral_fragment.tooltip.harvest")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else if (item == ItemInit.salt_stained_voyager_log.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.salt_stained_voyager_log.tooltip")
                    .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("item.hemomancy.salt_stained_voyager_log.tooltip.inquiry")
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
    public static void onClientPlayerLogin(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) {
        QliphothBloomClientData.clear();
        NpcProgressionMarkerClientState.clear();
    }

    @SubscribeEvent
    public static void onClientPlayerLogout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        // HutosLib now retains read tracker state across disconnect/reload.
        FaneBoundaryClientData.clear();
        ActiveRiteClientData.clear();
        CardinalRiteFogRenderer.clear();
		VesperFightClientData.clear();
		MycophantFightClientData.clear();
		VesperFightFloorRenderer.clear();
		ArborOfWillRenderer.clearCaches();
		QliphothBloomRenderer.clearCaches();
        QliphothBloomClientData.clear();
		NpcProgressionMarkerClientState.clear();
		CardinalRiteImpactClientEvents.clear();
		if (SanguineOmenOverlay.instance != null) SanguineOmenOverlay.instance.clear();
        MnemonicBlueprintRenderer.disconnect();
    }

	private static boolean crossbarRadialOpened;

	private static void handleCrossbarRadialTick(Minecraft mc) {
		if (!isKeyDown(mc.options.keyUse)) {
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

    @SubscribeEvent
    public static void renderMorphicNectarScreenOverlay(RenderGuiEvent.Pre event) {
        MorphicNectarScreenOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void renderWhiteHumorScreenOverlay(RenderGuiEvent.Pre event) {
        WhiteHumorScreenOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void renderSanguineOmenWorldGrade(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(true);
        FaneBoundaryRenderer.renderPost(event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(), partialTicks);
        if (SanguineOmenOverlay.instance != null) {
            SanguineOmenOverlay.instance.renderWorldGrade(event.getGuiGraphics(),
                    event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(), partialTicks);
        }
    }

    @SubscribeEvent
    public static void renderLevelLastEvent(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            renderBloodMoonSky(event);
		}

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
			VesperFightFloorRenderer.renderOpaque(event);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            MnemonicBlueprintRenderer.render(event);
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
			VesperFightFloorRenderer.renderFadingPerimeter(event);
			VesperFightFloorRenderer.renderFissures(event.getPoseStack(), partialTick);
            CardinalRiteBoundaryRenderer.render(event.getPoseStack(), partialTick);
            UnstainedRiteBoundaryRenderer.render(event.getPoseStack(), partialTick);
            GourdVineRenderer.render(event.getPoseStack(), partialTick);
            BloodStructureFeedWarpRenderer.render(event.getPoseStack(), partialTick);
            SanguineFormationProjectionRenderer.render(event.getPoseStack(), partialTick);
            FaneBoundaryRenderer.renderWorldMask(event.getPoseStack(), partialTick);
            BlackVeilRenderer.render(event.getPoseStack(), partialTick);
            BloodCraftRingRenderer.render(event.getPoseStack(), partialTick);
            QliphothBloomRenderer.render(event.getPoseStack(), partialTick, event.getFrustum());
            OculifloraRevealRenderer.render(event.getPoseStack(), partialTick);
            BloodBallRenderer.render(event.getPoseStack(), partialTick);
            BloodBindingTendrilRenderer.render(event.getPoseStack(), partialTick);
            SanguineMonolithShatterRenderer.render(event.getPoseStack(), partialTick);
            ClawSlashRenderer.render(event.getPoseStack(), partialTick);
            PuppeteerThreadRenderer.render(event.getPoseStack(), partialTick);
            HematicSutureLinkRenderer.render(event.getPoseStack(), partialTick);
            TendonLineRenderer.render(event.getPoseStack(), partialTick);
            MemoryThreadLineRenderer.render(event.getPoseStack(), partialTick);
            HeartyCompassTendrilRenderer.render(event.getPoseStack(), partialTick);
            VeinSpiderCourierRenderer.render(event.getPoseStack(), partialTick);
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderPlayerSize(RenderPlayerEvent.Pre event) {
        MorphlingPlayerPartVisibility.apply(event.getEntity(), event.getRenderer());
    }

     @SubscribeEvent
     public static void restoreMorphlingHiddenPlayerParts(RenderPlayerEvent.Post event) {
         MorphlingPlayerPartVisibility.restore(event.getRenderer());
     }

     @EventBusSubscriber(modid = Hemomancy.MOD_ID,value = Dist.CLIENT)
     public static class ClientModBusEvents {

        public static BakedModel bloodAbsorptionModel, bloodProjectionModel;

		@SubscribeEvent
		public static void registerTreeCacheReloadListener(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
				ArborOfWillRenderer.clearCaches();
				QliphothBloomRenderer.clearCaches();
			});
		}

        @SubscribeEvent
        public static void registerDimEffects(RegisterDimensionSpecialEffectsEvent event) {
            DimensionSpecialEffects fungalEffects =
                    new FungalRealmsRenderInfo(Float.NaN, true, DimensionSpecialEffects.SkyType.END, true, true);
            event.register(Hemomancy.rloc("fungal_gardens"), fungalEffects);
            // Legacy alias in case an older save still points to hemomancy:renderer.
            event.register(Hemomancy.rloc("renderer"), fungalEffects);

            event.register(Hemomancy.rloc("chamber_of_will"), new ChamberOfWillEffects());

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
			event.registerEntityRenderer(EntityInit.living_sickle_hook.get(), LivingSickleHookRenderer::new);
			event.registerEntityRenderer(EntityInit.living_flail_head.get(), LivingFlailHeadProjectileRenderer::new);
			event.registerEntityRenderer(EntityInit.vesper_scute_projectile.get(), VesperScuteProjectileRenderer::new);
			event.registerEntityRenderer(EntityInit.veinwing_feather.get(), VeinwingFeatherRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_needle.get(), BloodNeedleRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_shot.get(), BloodShotRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_bullet.get(), BloodBulletRenderer::new);
            event.registerEntityRenderer(EntityInit.morphling_polyp_item.get(), MorphlingPolypItemRenderer::new);
            event.registerEntityRenderer(EntityInit.qliphoth_seed_item.get(), QliphothSeedItemEntityRenderer::new);
            event.registerEntityRenderer(EntityInit.iron_pillar.get(), IronPillarRenderer::new);
            event.registerEntityRenderer(EntityInit.iron_spike.get(), IronSpikeRenderer::new);
            event.registerEntityRenderer(EntityInit.iron_wall.get(), IronWallRenderer::new);
            event.registerEntityRenderer(EntityInit.wretched_will.get(), WretchedWillRenderer::new);
            event.registerEntityRenderer(EntityInit.will.get(), WillRenderer::new);
            event.registerEntityRenderer(EntityInit.will_anchor.get(), WillAnchorRenderer::new);
            event.registerEntityRenderer(EntityInit.arbor_of_will.get(), ArborOfWillRenderer::new);
            event.registerEntityRenderer(EntityInit.leech.get(), LeechRenderer::new);
            event.registerEntityRenderer(EntityInit.bloodlicker.get(), BloodlickerRenderer::new);
            event.registerEntityRenderer(EntityInit.unsettled_ichor.get(), UnsettledIchorRenderer::new);
            event.registerEntityRenderer(EntityInit.awakened_ichorian_sigil.get(),
                    AwakenedIchorianSigilRenderer::new);
            event.registerEntityRenderer(EntityInit.humanity_sprite.get(), HumanitySpriteRenderer::new);
            event.registerEntityRenderer(EntityInit.bog_revenant.get(), BogRevenantRenderer::new);
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
            event.registerEntityRenderer(EntityInit.mnemonist_puppet.get(), MnemonistPuppetRenderer::new);
            event.registerEntityRenderer(EntityInit.scarlet_mummer.get(), ScarletMummerRenderer::new);
			event.registerEntityRenderer(EntityInit.sanguine_hound.get(), SanguineHoundRenderer::new);
            event.registerEntityRenderer(EntityInit.lump_of_thought.get(), LumpOfThoughtRenderer::new);
            event.registerEntityRenderer(EntityInit.chthonian_queen.get(), ChthonianQueenRenderer::new);
            event.registerEntityRenderer(EntityInit.abhorent_thought.get(), AbhorentThoughtRenderer::new);
            event.registerEntityRenderer(EntityInit.barbed_urchin.get(), BarbedUrchinRenderer::new);
            event.registerEntityRenderer(EntityInit.chalybeate_snail.get(), ChalybeateSnailRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_lantern_jelly.get(), BloodLanternJellyRenderer::new);
            event.registerEntityRenderer(EntityInit.mnemonic_whale.get(), MnemonicWhaleRenderer::new);
            event.registerEntityRenderer(EntityInit.brined_votary.get(), BrinedVotaryRenderer::new);
            event.registerEntityRenderer(EntityInit.prism_cuttle.get(), PrismCuttleRenderer::new);
            event.registerEntityRenderer(EntityInit.hemolymphopoda.get(), HemolymphopodaRenderer::new);
            event.registerEntityRenderer(EntityInit.lantern_tick.get(), LanternTickRenderer::new);
            event.registerEntityRenderer(EntityInit.erythromycelium_eruptus.get(), ErythromyceliumEruptusRenderer::new);
            event.registerEntityRenderer(EntityInit.morphling_polyp.get(), MorphlingPolypRenderer::new);
            event.registerEntityRenderer(EntityInit.flying_charm.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(EntityInit.hemolytic_vial_projectile.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(EntityInit.constrictor_cord_projectile.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(EntityInit.blood_chum_projectile.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(EntityInit.sanguis_lancea.get(), SanguisLanceaRenderer::new);
            event.registerEntityRenderer(EntityInit.unstained_zealot.get(), UnstainedZealotRenderer::new);
            event.registerEntityRenderer(EntityInit.unstained_guardian.get(), UnstainedGuardianRenderer::new);
            event.registerEntityRenderer(EntityInit.unstained_acolyte.get(), UnstainedAcolyteRenderer::new);
            event.registerEntityRenderer(EntityInit.unstained_scout.get(), UnstainedScoutRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_hermit.get(), HarbingerHermitRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_alchemist.get(), HarbingerAlchemistRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_artificer.get(), HarbingerArtificerRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_cicatrix_anchorite.get(), HarbingerCicatrixAnchoriteRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_mnemonist.get(), HarbingerMnemonistRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_vicar.get(), HarbingerVicarRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_voyager.get(), HarbingerVoyagerRenderer::new);
            event.registerEntityRenderer(EntityInit.harbinger_votary_wayfarer.get(), HarbingerVotaryWayfarerRenderer::new);
            event.registerEntityRenderer(EntityInit.drudge.get(),DrudgeRenderer::new);
            event.registerEntityRenderer(EntityInit.hemorath.get(), HemorathRenderer::new);
            event.registerEntityRenderer(EntityInit.annetta_knowles.get(), AnnettaKnowlesRenderer::new);
            event.registerEntityRenderer(EntityInit.stained_priestess.get(), StainedPriestessRenderer::new);
            event.registerEntityRenderer(EntityInit.latent_annetta_infection.get(), LatentAnnettaInfectionRenderer::new);
            event.registerEntityRenderer(EntityInit.putriciel.get(), PutricielRenderer::new);
            event.registerEntityRenderer(EntityInit.velorum.get(), VelorumRenderer::new);
            event.registerEntityRenderer(EntityInit.vesper_crowned_refusal.get(), VesperTheCrownedRefusalRenderer::new);
            event.registerEntityRenderer(EntityInit.vesper_evening_star.get(), VesperTheEveningStarRenderer::new);
            event.registerEntityRenderer(EntityInit.mycophant.get(), MycophantRenderer::new);
            event.registerEntityRenderer(EntityInit.seraphae.get(), SeraphaeRenderer::new);
            event.registerEntityRenderer(EntityInit.seraphae_fragment.get(), SeraphaeFragmentRenderer::new);
            event.registerEntityRenderer(EntityInit.containment_anchor.get(), ContainmentAnchorRenderer::new);
            event.registerEntityRenderer(EntityInit.hematic_construct.get(), HematicConstructRenderer::new);
            event.registerEntityRenderer(EntityInit.spectral_companion.get(), PaleIntercessionRenderer::new);
            event.registerEntityRenderer(EntityInit.phantasmal_echo.get(), PhantasmalEchoRenderer::new);
            event.registerEntityRenderer(EntityInit.dark_arrow.get(), DarkArrowRenderer::new);
            event.registerEntityRenderer(EntityInit.desiccant.get(), DesiccantRenderer::new);
            event.registerEntityRenderer(EntityInit.crimson_doe.get(), CrimsonDoeRenderer::new);
            event.registerEntityRenderer(EntityInit.verdigris_moth.get(), VerdigrisMothRenderer::new);
            event.registerEntityRenderer(EntityInit.hematic_burrower.get(), HematicBurrowerRenderer::new);
            event.registerEntityRenderer(EntityInit.scarlet_serpent.get(), ScarletSerpentRenderer::new);
            event.registerEntityRenderer(EntityInit.hemojelly.get(), HemojellyRenderer::new);
            event.registerEntityRenderer(EntityInit.venous_strider.get(), VenousStriderRenderer::new);
            event.registerEntityRenderer(EntityInit.venom_rib_centipede.get(), VenomRibCentipedeRenderer::new);
            event.registerEntityRenderer(EntityInit.covenant_throne_seat.get(), CovenantThroneSeatRenderer::new);
            event.registerEntityRenderer(EntityInit.hematic_armature_restraint.get(), ArmatureRestraintRenderer::new);

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
            HarbingerLodestoneOverlay.instance = new HarbingerLodestoneOverlay();
            StillArtCooldownOverlay.instance = new StillArtCooldownOverlay();
            UnstainedGaugeOverlay.instance = new UnstainedGaugeOverlay();
            FungalWhisperVignetteOverlay.instance = new FungalWhisperVignetteOverlay();
            SanguineOmenOverlay.instance = new SanguineOmenOverlay();
            WillPresenceOverlay.instance = new WillPresenceOverlay();
            CurorLensOverlay.instance = new CurorLensOverlay();
            CardinalRiteOverlay.instance = new CardinalRiteOverlay();
            // Tiles
            BlockEntityRenderers.register(BlockEntityInit.discovery_inscription.get(),
                    DiscoveryInscriptionBlockRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.scar_station.get(), ScarStationRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.ghastly_alembic.get(), GhastlyAlembicRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.pallid_retort.get(), PallidRetortRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.morphling_incubator.get(), MorphlingIncubatorRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.puppeteers_spindle.get(), PuppeteersSpindleRenderer::new);
           BlockEntityRenderers.register(BlockEntityInit.mycelial_crucible.get(), MycelialCrucibleRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.mycelial_lantern.get(), MycelialLanternRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.morphling_cradle.get(), MorphlingCradleRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.gourdvine_tap.get(), GourdvineTapRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.specimen_jar.get(), SpecimenJarRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.unstained_podium.get(), UnstainedPodiumRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.scrying_podium.get(), ScryingPodiumRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.scarlet_vanity.get(), ScarletVanityRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.mason_effigy.get(), MasonsEffigyRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.iron_brazier.get(), IronBrazierRenderer::new);
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
            BlockEntityRenderers.register(BlockEntityInit.hematic_stake.get(), HematicStakeRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.consecrated_bloodwell.get(), ConsecratedBloodwellRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.mortal_display.get(), MortalDisplayRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.cardinal_focus.get(), CardinalFocusRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.vial_centrifuge.get(), VialCentrifugeRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.somatic_loom.get(),
                    SomaticLoomRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.earthen_vein.get(), EarthenVeinRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.mnemonic_reliquary.get(), MnemonicReliquaryRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.dictation_table.get(), DictationTableRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.visceral_mirror.get(), VisceralMirrorRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.non_euclidean_hallway.get(), NonEuclideanHallwayRenderer::new);
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
            BlockEntityRenderers.register(BlockEntityInit.warp_chair.get(),
                    WarpChairRenderer::new);
            BlockEntityRenderers.register(BlockEntityInit.hematic_armature.get(),
                    HematicArmatureRenderer::new);
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

                ItemProperties.register(ItemInit.living_weapon_graft.get(), Hemomancy.rloc("form"),
                        (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                                LivingWeaponGraftData.fromStack(stack)
                                        .map(data -> (float) data.form().ordinal() + 1.0F)
                                        .orElse(1.0F));

                ItemProperties.register(ItemInit.drudge_electrode.get(), Hemomancy.rloc("mode"),
                        HemoItemProperties.booleanTag(DrudgeElectrodeItem.TAG_MODE));

                ItemProperties.register(ItemInit.living_staff.get(), Hemomancy.rloc("staff_visual"), new ItemPropertyFunction() {
                    @Override
                    public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int seed) {
                        Player player = ent instanceof Player livingPlayer ? livingPlayer : Minecraft.getInstance().player;
                        int fittingVisual = LivingStaffFittingHelper.staffVisualFor(player);
                        if (fittingVisual > 0) {
                            return fittingVisual;
                        }
                        return staffVisualFromMorphlingInventory(stack, world);
                    }

                    private float staffVisualFromMorphlingInventory(ItemStack stack, ClientLevel world) {
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
                        if (selectedStack.getItem() == ItemInit.morphling_emberfang.get()) {
                            return 1.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_deadmans_purse.get()) {
                            return 2.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_gravecap.get()) {
                            return 3.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_bootlace.get()) {
                            return 4.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_winter_shroud.get()) {
                            return 5.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_lumenlace.get()) {
                            return 6.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_witchs_ear.get()) {
                            return 7.0F;
                        } else if (selectedStack.getItem() == ItemInit.morphling_irontooth.get()) {
                            return 8.0F;
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
            event.register(ContainerInit.mason_effigy.get(), MasonsEffigyScreen::new);
            event.register(ContainerInit.vial_centrifuge.get(), VialCentrifugeScreen::new);
            event.register(ContainerInit.morphling_jar.get(), MorphlingJarScreen::new);
            event.register(ContainerInit.living_syringe.get(), LivingSyringeScreen::new);
            event.register(ContainerInit.living_staff.get(), LivingStaffScreen::new);
            event.register(ContainerInit.ghastly_alembic.get(), GhastlyAlembicScreen::new);
            event.register(ContainerInit.pallid_retort.get(), PallidRetortScreen::new);
            event.register(ContainerInit.stillwater_condenser.get(), StillwaterCondenserScreen::new);
            event.register(ContainerInit.scar_station.get(), ScarStationScreen::new);
            event.register(ContainerInit.scar_binder.get(), ScarBinderScreen::new);
            event.register(ContainerInit.mnemonic_folio.get(), MnemonicFolioScreen::new);
            event.register(ContainerInit.vascular_view.get(), VascularViewScreen::new);
            event.register(ContainerInit.tendency_view.get(), TendencyViewScreen::new);
            event.register(ContainerInit.scrying_diagnostics.get(), ScryingDiagnosticsScreen::new);
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
            event.register(ClientEvents.useStillArt);
            event.register(ClientEvents.selectStillArt);
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
            CardinalRitePlantedStaffModels.uniqueModelNames().forEach(model ->
                    event.register(ModelResourceLocation.standalone(Hemomancy.rloc("item/" + model))));
            LowtideRuinObjModels.register(event);

        }

        @SubscribeEvent
        public static void onModelBake(BakingCompleted evt) {
            bloodAbsorptionModel = evt.getModels()
                    .get(ModelResourceLocation.standalone(Hemomancy.rloc("item/blood_absorption_texture")));
            bloodProjectionModel = evt.getModels()
                    .get(ModelResourceLocation.standalone(Hemomancy.rloc("item/blood_projection_texture")));
            LowtideRuinObjModels.cache(evt);
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
			event.registerAboveAll(Hemomancy.rloc("body_idioms"), (graphics, deltaTracker) ->
					BodyIdiomOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight()));
			event.registerAboveAll(Hemomancy.rloc("arbor_fruit_name"), (graphics, deltaTracker) ->
					ArborFruitHudOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight()));
			event.registerAboveAll(Hemomancy.rloc("mnemonic_blueprint_progress"), (graphics, deltaTracker) ->
					MnemonicBlueprintProgressOverlay.renderHUD(
							graphics, graphics.guiWidth(), graphics.guiHeight()));
			event.registerAboveAll(Hemomancy.rloc("chamber_visit"), (graphics, deltaTracker) ->
					ChamberVisitOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight()));
            event.registerAboveAll(Hemomancy.rloc("cardinal_rite"), (graphics, deltaTracker) -> {
                if (CardinalRiteOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    CardinalRiteOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
            event.registerAboveAll(Hemomancy.rloc("bloodvolume"), (graphics, deltaTracker) -> {
                if (BloodVolumeOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    BloodVolumeOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
            event.registerAboveAll(Hemomancy.rloc("manip_cooldown"), (graphics, deltaTracker) -> {
                if (ManipCooldownOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    ManipCooldownOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
            event.registerAboveAll(Hemomancy.rloc("harbinger_lodestone"), (graphics, deltaTracker) -> {
                if (HarbingerLodestoneOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    HarbingerLodestoneOverlay.instance.renderHUD(
                            graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
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
            event.registerAboveAll(Hemomancy.rloc("sanguine_omen"), (graphics, deltaTracker) -> {
                if (SanguineOmenOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    SanguineOmenOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
            event.registerAboveAll(Hemomancy.rloc("will_presence"), (graphics, deltaTracker) -> {
                if (WillPresenceOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    WillPresenceOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
            event.registerAboveAll(Hemomancy.rloc("curor_lens"), (graphics, deltaTracker) -> {
                if (CurorLensOverlay.instance != null) {
                    float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                    CurorLensOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
                }
            });
        }
    }
}
