package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.client.render.entity.projectile.*;
import com.vincenthuto.hemomancy.client.render.entity.summon.*;
import com.vincenthuto.hemomancy.client.render.item.ScarPatternBakedModel;
import com.vincenthuto.hemomancy.client.render.tile.*;
import com.vincenthuto.hemomancy.client.render.tile.crafting.ScarStationRenderer;
import com.vincenthuto.hemomancy.client.render.tile.crafting.GhastlyAlembicRenderer;
import com.vincenthuto.hemomancy.client.render.tile.crafting.PallidRetortRenderer;
import com.vincenthuto.hemomancy.client.render.tile.crafting.MorphlingIncubatorRenderer;
import com.vincenthuto.hemomancy.client.render.tile.crafting.SomaticLoomRenderer;
import com.vincenthuto.hemomancy.client.render.tile.crafting.VialCentrifugeRenderer;
import com.vincenthuto.hemomancy.client.render.tile.functional.*;
import com.vincenthuto.hemomancy.client.render.world.CardinalRiteBoundaryRenderer;
import com.vincenthuto.hemomancy.client.render.world.BloodBallRenderer;
import com.vincenthuto.hemomancy.client.render.world.BloodCraftRingRenderer;
import com.vincenthuto.hemomancy.client.render.world.GourdVineRenderer;
import com.vincenthuto.hemomancy.client.render.world.QliphothBloomRenderer;
import com.vincenthuto.hemomancy.client.data.ActiveBloodCraftClientData;
import com.vincenthuto.hemomancy.client.data.BloodBallClientData;
import com.vincenthuto.hemomancy.client.screen.item.CharmGourdScreen;
import com.vincenthuto.hemomancy.client.screen.item.StructureSpawnerScreen;
import com.vincenthuto.hemomancy.client.screen.item.TendencyViewScreen;
import com.vincenthuto.hemomancy.client.screen.item.VascularViewScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.GhastlyAlembicScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.MorphlingIncubatorScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.PallidRetortScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.VialCentrifugeScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.MnemonicReliquaryScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.SporeImplantScreen;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronPillarRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronSpikeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronWallRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.AbhorentThoughtRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.BarbedUrchinRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.BloodDrunkPuppeteerRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.ChitiniteRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.ChthonianQueenRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.ChthonianRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.EnthralledDollRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.ErythromyceliumEruptusRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.FargoneRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.FerventChitiniteRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.animal.FunglingRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.HemolymphopodaRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.animal.LeechRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.LumpOfThoughtRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.ThirsterRenderer;
import com.vincenthuto.hemomancy.client.render.entity.npc.HarbingerAlchemistRenderer;
import com.vincenthuto.hemomancy.client.render.entity.npc.HarbingerHermitRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.HollowVesselRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.HematicConstructRenderer;
import com.vincenthuto.hemomancy.client.render.entity.npc.HarbingerVicarRenderer;
import com.vincenthuto.hemomancy.client.render.entity.npc.UnstainedAcolyteRenderer;
import com.vincenthuto.hemomancy.client.render.entity.npc.UnstainedGuardianRenderer;
import com.vincenthuto.hemomancy.client.render.entity.npc.UnstainedZealotRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.DessicantRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.CruorFiendRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.VoidDrinkerRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.FrozenClotRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.AbyssalSiphonRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.SynapseHoundRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.MyelinBorerRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.animal.CrimsonDoeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.HemojellyRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.animal.VenousStriderRenderer;
import com.vincenthuto.hemomancy.client.render.item.MorphlingPolypItemRenderer;
import com.vincenthuto.hemomancy.client.screen.item.living.LivingStaffScreen;
import com.vincenthuto.hemomancy.client.screen.item.living.LivingSyringeScreen;
import com.vincenthuto.hemomancy.client.screen.item.living.MorphlingJarScreen;
import com.vincenthuto.hemomancy.client.screen.item.living.MorphlingJarViewerScreen;
import com.vincenthuto.hemomancy.client.screen.manips.RadialChooseManipScreen;
import com.vincenthuto.hemomancy.client.screen.manips.RadialChooseVeinScreen;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodVolumeOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.EquippedMorphlingOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.ManipCooldownOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.UnstainedGaugeOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.FungalWhisperVignetteOverlay;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.scar.ScarStationScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.scar.ScarBinderScreen;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.scar.ScarsCapabilities;
import com.vincenthuto.hemomancy.common.capability.player.volume.RenderBloodLaserEvent;
import com.vincenthuto.hemomancy.client.render.item.ScarPatternItemColor;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.scar.pattern.ItemScarPattern;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.ChangeSelectedManipPacket;
import com.vincenthuto.hemomancy.common.network.capa.manips.UseManipKeyPacket;
import com.vincenthuto.hemomancy.common.network.keybind.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.keybind.BloodFormationKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.keybind.ToggleGourdKeyPacket;
import com.vincenthuto.hemomancy.common.network.morphling.OpenMorphlingJarPacket;
import com.vincenthuto.hemomancy.common.network.particle.GroundBloodDrawPacket;
import com.vincenthuto.hemomancy.common.worldgen.feature.FungalSkyBoxRenderer;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
	public static void onClientTick(ClientTickEvent event) {

		if (event.phase == TickEvent.Phase.END) {
			ManipCooldownOverlay.tick();
			ActiveBloodCraftClientData.tick();
			BloodBallClientData.tick();
			if (FungalWhisperVignetteOverlay.instance != null) {
				FungalWhisperVignetteOverlay.instance.tick();
			}
		}

		if (bloodFormation.consumeClick()) {
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(new BloodFormationKeyPressPacket());
		}

		if (bloodCrafting.consumeClick()) {
			PacketHandler.CHANNELBLOODVOLUME
					.sendToServer(new BloodCraftingKeyPressPacket(HLClientUtils.getClientPlayer().getMainHandItem()));
		}

		if (bloodDraw.isDown()) {
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(new GroundBloodDrawPacket(HLClientUtils.getPartialTicks()));
		}
		if (toggleGourd.consumeClick()) {
			PacketHandler.CHANNELSCARS.sendToServer(new ToggleGourdKeyPacket());
		}
		if (bloodBallDrop.consumeClick()) {
			BloodBallClientData.drop();
		}
		if (openMorphlingJarViewer.consumeClick()) {
			if (Screen.hasShiftDown()) {
				PacketHandler.CHANNELMORPHLINGJAR.sendToServer(new OpenMorphlingJarPacket());
			} else {
				MorphlingJarViewerScreen.openScreen();
			}
		}
		if (cycleSelectedManip.consumeClick()) {
			PacketHandler.CHANNELKNOWNMANIPS
					.sendToServer(new ChangeSelectedManipPacket(HLClientUtils.getPartialTicks()));
		}
		if (useManip.consumeClick()) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				mc.player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manip -> {
					if (manip.getSelectedManip() != null
							&& manip.getSelectedManip().getName().equals("venous_travel")) {
						mc.setScreen(new RadialChooseVeinScreen(manip));
					} else {
						PacketHandler.CHANNELKNOWNMANIPS
								.sendToServer(new UseManipKeyPacket(HLClientUtils.getPartialTicks()));
					}
				});
			}
		}

		// Radial
		if (event.phase != TickEvent.Phase.START)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null) {
			boolean vascCharmKeyIsDown = openVascCharmMenu.isDown();

			if (vascCharmKeyIsDown && !menuKey) {

				while (openVascCharmMenu.consumeClick()) {
					if (mc.screen == null) {
						mc.player.getCapability(ScarsCapabilities.SCARS).ifPresent(inv -> {
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
	public static void renderLevelLastEvent(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
			CardinalRiteBoundaryRenderer.render(event.getPoseStack(), event.getPartialTick());
			GourdVineRenderer.render(event.getPoseStack(), event.getPartialTick());
			BloodCraftRingRenderer.render(event.getPoseStack(), event.getPartialTick());
			QliphothBloomRenderer.render(event.getPoseStack(), event.getPartialTick());
			BloodBallRenderer.render(event.getPoseStack(), event.getPartialTick());
		}
	}

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void cameraView(EntityEvent.Size event) {
		if (event.getEntity() instanceof Player player) {
			if (player.isAddedToWorld()) {
				player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
					if (manip.isAvatarActive()) {
						event.setNewEyeHeight(3.5f);
						event.setNewSize(player.getDimensions(Pose.STANDING).scale(2));
					} else {
						if (player.isCrouching()) {
							event.setNewSize(player.getDimensions(Pose.CROUCHING));

						} else {
							event.setNewEyeHeight(Player.DEFAULT_EYE_HEIGHT);
							event.setNewSize(player.getDimensions(Pose.STANDING));

						}

					}
				});
			}
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
		RenderSystem.setShaderTexture(0, abstractclientplayer.getSkinTextureLocation());
		pMatrixStack.translate(f * -1.0F, 3.6F, 3.5D);
		pMatrixStack.mulPose(Vector3.ZP.rotationDegrees(f * 120.0F).toMoj());
		pMatrixStack.mulPose(Vector3.XP.rotationDegrees(200.0F).toMoj());
		pMatrixStack.mulPose(Vector3.YP.rotationDegrees(f * -135.0F).toMoj());
		pMatrixStack.translate(f * 5.6F, 0.0D, 0.0D);
		pMatrixStack.scale(2, 2, 2);
		PlayerRenderer playerrenderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher()
				.<AbstractClientPlayer>getRenderer(abstractclientplayer);
		if (flag) {
			playerrenderer.renderRightHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayer);
		} else {
			playerrenderer.renderLeftHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayer);
		}

	}

	@SubscribeEvent
	public static void renderPlayerSize(RenderPlayerEvent event) {
		if (event.getEntity().isAddedToWorld()) {
			event.getEntity().getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
				if (manip.isAvatarActive()) {
					event.getPoseStack().translate(0, 2, 0);
				}
			});

		}
	}

	@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
	public static class ClientModBusEvents {

		@SubscribeEvent
		public static void registerDimEffects(RegisterDimensionSpecialEffectsEvent event) {
			new FungalSkyBoxRenderer();
			// new TFWeatherRenderer();
			event.register(Hemomancy.rloc("renderer"),
					new FungalRealmsRenderInfo(128.0F, true, DimensionSpecialEffects.SkyType.END, true, true));
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
			event.registerEntityRenderer(EntityInit.chitinite.get(), ChitiniteRenderer::new);
			event.registerEntityRenderer(EntityInit.fervent_chitinite.get(), FerventChitiniteRenderer::new);
			event.registerEntityRenderer(EntityInit.chthonian.get(), ChthonianRenderer::new);
			event.registerEntityRenderer(EntityInit.blood_drunk_puppeteer.get(), BloodDrunkPuppeteerRenderer::new);
			event.registerEntityRenderer(EntityInit.enthralled_doll.get(), EnthralledDollRenderer::new);
			event.registerEntityRenderer(EntityInit.blood_thrall.get(), BloodThrallRenderer::new);
			event.registerEntityRenderer(EntityInit.lump_of_thought.get(), LumpOfThoughtRenderer::new);
			event.registerEntityRenderer(EntityInit.chthonian_queen.get(), ChthonianQueenRenderer::new);
			event.registerEntityRenderer(EntityInit.abhorent_thought.get(), AbhorentThoughtRenderer::new);
			event.registerEntityRenderer(EntityInit.barbed_urchin.get(), BarbedUrchinRenderer::new);
			event.registerEntityRenderer(EntityInit.hemolymphopoda.get(), HemolymphopodaRenderer::new);
			event.registerEntityRenderer(EntityInit.erythromycelium_eruptus.get(), ErythromyceliumEruptusRenderer::new);
			event.registerEntityRenderer(EntityInit.morphling_polyp.get(), MorphlingPolypRenderer::new);
			event.registerEntityRenderer(EntityInit.flying_charm.get(), ThrownItemRenderer::new);
			event.registerEntityRenderer(EntityInit.sanguis_lancea.get(), SanguisLanceaRenderer::new);
			event.registerEntityRenderer(EntityInit.unstained_zealot.get(), UnstainedZealotRenderer::new);
			event.registerEntityRenderer(EntityInit.unstained_guardian.get(), UnstainedGuardianRenderer::new);
			event.registerEntityRenderer(EntityInit.unstained_acolyte.get(), UnstainedAcolyteRenderer::new);
			event.registerEntityRenderer(EntityInit.harbinger_hermit.get(), HarbingerHermitRenderer::new);
			event.registerEntityRenderer(EntityInit.harbinger_alchemist.get(), HarbingerAlchemistRenderer::new);
			event.registerEntityRenderer(EntityInit.harbinger_vicar.get(), HarbingerVicarRenderer::new);
			event.registerEntityRenderer(EntityInit.hollow_vessel.get(), HollowVesselRenderer::new);
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
			MinecraftForge.EVENT_BUS.register(RenderBloodLaserEvent.class);
			BloodVolumeOverlay.instance = new BloodVolumeOverlay();
			EquippedMorphlingOverlay.instance = new EquippedMorphlingOverlay();
			ManipCooldownOverlay.instance = new ManipCooldownOverlay();
			UnstainedGaugeOverlay.instance = new UnstainedGaugeOverlay();
			FungalWhisperVignetteOverlay.instance = new FungalWhisperVignetteOverlay();
			// Tiles
			BlockEntityRenderers.register(BlockEntityInit.scar_station.get(), ScarStationRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.ghastly_alembic.get(), GhastlyAlembicRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.pallid_retort.get(), PallidRetortRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.morphling_incubator.get(), MorphlingIncubatorRenderer::new);
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
			BlockEntityRenderers.register(BlockEntityInit.visceral_mirror.get(), VisceralMirrorRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.qliphoth_bloom.get(),
					com.vincenthuto.hemomancy.client.render.tile.functional.QliphothBloomBlockRenderer::new);
			MenuScreens.register(ContainerInit.gourd_charm_inventory.get(), CharmGourdScreen::new);
			MenuScreens.register(ContainerInit.fungal_implantation.get(), SporeImplantScreen::new);
			MenuScreens.register(ContainerInit.vial_centrifuge.get(), VialCentrifugeScreen::new);
			MenuScreens.register(ContainerInit.morphling_jar.get(), MorphlingJarScreen::new);
			MenuScreens.register(ContainerInit.living_syringe.get(), LivingSyringeScreen::new);
			MenuScreens.register(ContainerInit.living_staff.get(), LivingStaffScreen::new);
			MenuScreens.register(ContainerInit.ghastly_alembic.get(), GhastlyAlembicScreen::new);
			MenuScreens.register(ContainerInit.pallid_retort.get(), PallidRetortScreen::new);
			MenuScreens.register(ContainerInit.scar_station.get(), ScarStationScreen::new);
			MenuScreens.register(ContainerInit.scar_binder.get(), ScarBinderScreen::new);
			MenuScreens.register(ContainerInit.vascular_view.get(), VascularViewScreen::new);
			MenuScreens.register(ContainerInit.tendency_view.get(), TendencyViewScreen::new);
			MenuScreens.register(ContainerInit.morphling_incubator.get(), MorphlingIncubatorScreen::new);
			MenuScreens.register(ContainerInit.structure_spawner.get(), StructureSpawnerScreen::new);
			MenuScreens.register(ContainerInit.mnemonic_reliquary.get(), MnemonicReliquaryScreen::new);

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
		public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
			ScarPatternItemColor scarPatternColor = new ScarPatternItemColor();
			ItemInit.BASEITEMS.getEntries().stream()
				.filter(entry -> entry.get() instanceof ItemScarPattern)
				.forEach(entry -> event.register(scarPatternColor, entry.get()));
		}

		public static BakedModel bloodAbsorptionModel, bloodProjectionModel;

		@SubscribeEvent
		public static void modelRegisterEvent(ModelEvent.RegisterAdditional event) {
			event.register(Hemomancy.rloc("item/blood_absorption_texture"));
			event.register(Hemomancy.rloc("item/blood_projection_texture"));

		}

		@SubscribeEvent
		public static void onModelBake(BakingCompleted evt) {
			bloodAbsorptionModel = evt.getModels().get(Hemomancy.rloc("item/blood_absorption_texture"));
			bloodProjectionModel = evt.getModels().get(Hemomancy.rloc("item/blood_projection_texture"));
		}

		@SubscribeEvent
		public static void onModifyBakingResult(ModelEvent.ModifyBakingResult evt) {
			// Wrap all Scar Pattern item models so the overlay layer is shrunk down
			for (RegistryObject<Item> entry : ItemInit.BASEITEMS.getEntries()) {
				if (entry.get() instanceof ItemScarPattern) {
					ModelResourceLocation modelLoc = new ModelResourceLocation(ForgeRegistries.ITEMS.getKey(entry.get()), "inventory");
					BakedModel existing = evt.getModels().get(modelLoc);
					if (existing != null) {
						evt.getModels().put(modelLoc, new ScarPatternBakedModel(existing));
					}
				}
			}
		}

		// Overlay
		@SubscribeEvent
		public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
			// event.registerAboveAll("bloodvolume", BloodVolumeOverlay.HUD_BLOODVOLUME);
			event.registerAboveAll("bloodvolume", (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
				gui.setupOverlayRenderState(true, false);
				BloodVolumeOverlay.instance.renderHUD(mStack, screenWidth, screenHeight, partialTicks);
				// BloodVolumeOverlay.HUD_BLOODVOLUME;
			});
			event.registerAboveAll("equipped_morphling", (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
				gui.setupOverlayRenderState(true, false);
				EquippedMorphlingOverlay.instance.renderHUD(mStack, screenWidth, screenHeight, partialTicks);
			});
			event.registerAboveAll("manip_cooldown", (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
				gui.setupOverlayRenderState(true, false);
				ManipCooldownOverlay.instance.renderHUD(mStack, screenWidth, screenHeight, partialTicks);
			});
			event.registerAboveAll("unstained_gauge", (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
				gui.setupOverlayRenderState(true, false);
				UnstainedGaugeOverlay.instance.renderHUD(mStack, screenWidth, screenHeight, partialTicks);
			});
			event.registerAboveAll("fungal_whisper_vignette", (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
				gui.setupOverlayRenderState(true, false);
				FungalWhisperVignetteOverlay.instance.renderHUD(mStack, screenWidth, screenHeight, partialTicks);
			});
		}
	}
}
