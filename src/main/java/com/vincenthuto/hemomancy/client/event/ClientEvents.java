package com.vincenthuto.hemomancy.client.event;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodBoltRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodBulletRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodCloudCarrierRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodCloudRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodNeedleRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodOrbDirectedRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodOrbTrackingRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodShotRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.WretchedWillRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronPillarRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronSpikeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronWallRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.AbhorentThoughtRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.BarbedUrchinRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.BloodDrunkPuppeteerRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.ChitiniteRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.ChthonianQueenRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.ChthonianRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.EnthralledDollRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.ErythromyceliumEruptusRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.FargoneRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.FunglingRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.LeechRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.LumpOfThoughtRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.MorphlingPolypRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.ThirsterRenderer;
import com.vincenthuto.hemomancy.client.render.entity.projectile.TrackingPestsRenderer;
import com.vincenthuto.hemomancy.client.render.entity.projectile.TrackingSerpentRenderer;
import com.vincenthuto.hemomancy.client.render.item.MorphlingPolypItemRenderer;
import com.vincenthuto.hemomancy.client.render.tile.ChiselStationRenderer;
import com.vincenthuto.hemomancy.client.render.tile.DendriticDistributorRenderer;
import com.vincenthuto.hemomancy.client.render.tile.EarthenVeinRenderer;
import com.vincenthuto.hemomancy.client.render.tile.FungalImplantationPylonRenderer;
import com.vincenthuto.hemomancy.client.render.tile.FungalPodiumRenderer;
import com.vincenthuto.hemomancy.client.render.tile.MorphlingIncubatorRenderer;
import com.vincenthuto.hemomancy.client.render.tile.MortalDisplayRenderer;
import com.vincenthuto.hemomancy.client.render.tile.ScryingPodiumRenderer;
import com.vincenthuto.hemomancy.client.render.tile.UnstainedPodiumRenderer;
import com.vincenthuto.hemomancy.client.render.tile.VialCentrifugeRenderer;
import com.vincenthuto.hemomancy.client.render.tile.VisceralRecallerRenderer;
import com.vincenthuto.hemomancy.client.screen.CharmGourdScreen;
import com.vincenthuto.hemomancy.client.screen.JuiceinatorScreen;
import com.vincenthuto.hemomancy.client.screen.SporeImplantScreen;
import com.vincenthuto.hemomancy.client.screen.VialCentrifugeScreen;
import com.vincenthuto.hemomancy.client.screen.VisceralRecallerScreen;
import com.vincenthuto.hemomancy.client.screen.living.LivingStaffScreen;
import com.vincenthuto.hemomancy.client.screen.living.LivingSyringeScreen;
import com.vincenthuto.hemomancy.client.screen.living.MorphlingJarScreen;
import com.vincenthuto.hemomancy.client.screen.manips.RadialChooseManipScreen;
import com.vincenthuto.hemomancy.client.screen.manips.RadialChooseVeinScreen;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodVolumeOverlay;
import com.vincenthuto.hemomancy.client.screen.rune.ChiselStationScreen;
import com.vincenthuto.hemomancy.client.screen.rune.RuneBinderScreen;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.capability.player.volume.RenderBloodLaserEvent;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.ChangeSelectedManipPacket;
import com.vincenthuto.hemomancy.common.network.capa.manips.UseQuickManipKeyPacket;
import com.vincenthuto.hemomancy.common.network.keybind.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.keybind.BloodFormationKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.morphling.ChangeMorphKeyPacket;
import com.vincenthuto.hemomancy.common.network.morphling.JarTogglePickupPacket;
import com.vincenthuto.hemomancy.common.network.particle.GroundBloodDrawPacket;
import com.vincenthuto.hemomancy.common.worldgen.feature.FungalSkyBoxRenderer;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
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

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

	public static final KeyMapping bloodFormation = new KeyMapping("key.hemomancy.bloodformation.desc", GLFW.GLFW_KEY_F,
			"key.hemomancy.category");
	public static final KeyMapping bloodCrafting = new KeyMapping("key.hemomancy.bloodcrafting.desc", GLFW.GLFW_KEY_C,
			"key.hemomancy.category");
	public static final KeyMapping bloodDraw = new KeyMapping("key.hemomancy.drawtest.desc", GLFW.GLFW_KEY_LEFT_CONTROL,
			"key.hemomancy.category");
	public static final KeyMapping toggleMorphlingJarPickup = new KeyMapping("key.hemomancy.morphjarpickup.desc",
			GLFW.GLFW_KEY_LEFT_CONTROL, "key.hemomancy.category");
	public static final KeyMapping toggleMorphlingOpenJar = new KeyMapping("key.hemomancy.openjar.desc",
			GLFW.GLFW_KEY_F, "key.hemomancy.category");
	public static final KeyMapping cycleSelectedManip = new KeyMapping("key.hemomancy.cyclemanip.desc", GLFW.GLFW_KEY_C,
			"key.hemomancy.category");
	public static final KeyMapping useQuickManip = new KeyMapping("key.hemomancy.quickusemanip.desc", GLFW.GLFW_KEY_R,
			"key.hemomancy.category");
	public static final KeyMapping useContManip = new KeyMapping("key.hemomancy.contusemanip.desc", GLFW.GLFW_KEY_G,
			"key.hemomancy.category");
	public static final KeyMapping OPEN_CHARM_SLOT_KEYBIND = new KeyMapping("key.charm_slot.slot", GLFW.GLFW_KEY_B,
			"key.hemomancy.category");
	public static final KeyMapping openVascCharmMenu = new KeyMapping("key.charm_slot.open", 90,
			"key.hemomancy.category");

	private static boolean menuKey = false;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {

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
		if (toggleMorphlingOpenJar.consumeClick()) {
			PacketHandler.CHANNELMORPHLINGJAR.sendToServer(new ChangeMorphKeyPacket());

		}
		if (toggleMorphlingJarPickup.consumeClick()) {
			PacketHandler.CHANNELMORPHLINGJAR.sendToServer(new JarTogglePickupPacket());
		}
		if (cycleSelectedManip.consumeClick()) {
			PacketHandler.CHANNELKNOWNMANIPS
					.sendToServer(new ChangeSelectedManipPacket(HLClientUtils.getPartialTicks()));
		}
		if (useQuickManip.consumeClick()) {
			PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new UseQuickManipKeyPacket(HLClientUtils.getPartialTicks()));
		}
//		if (useContManip.isDown()) {
//			PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new UseContManipKeyPacket(HLClientUtils.getPartialTicks()));
//		}

		// Radial
		if (event.phase != TickEvent.Phase.START)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null) {
			boolean vascCharmKeyIsDown = openVascCharmMenu.isDown();

			boolean useContKeyIsDown = useContManip.isDown();

			if (vascCharmKeyIsDown && !menuKey) {
				while (openVascCharmMenu.consumeClick()) {
					if (mc.screen == null) {
						mc.player.getCapability(RunesCapabilities.RUNES).ifPresent(inv -> {
							if (inv.getStackInSlot(4).getItem() instanceof VasculariumCharmItem charm) {
								mc.setScreen(new RadialChooseManipScreen(inv));
							}
						});
					}
				}
			}

			if (useContKeyIsDown && !menuKey) {
				while (useContManip.consumeClick()) {
					if (mc.screen == null) {
						mc.player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manip -> {
							if (manip.getSelectedManip().getName().equals("venous_travel")) {
								
								mc.setScreen(new RadialChooseVeinScreen(manip));
							}
						});
					}
				}
			}
			menuKey = vascCharmKeyIsDown || useContKeyIsDown;

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

	}

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
		//	new TFWeatherRenderer();
			event.register(Hemomancy.rloc("renderer"), new FungalRealmsRenderInfo(128.0F, true, DimensionSpecialEffects.SkyType.END, true, true));
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
			event.registerEntityRenderer(EntityInit.chthonian.get(), ChthonianRenderer::new);
			event.registerEntityRenderer(EntityInit.blood_drunk_puppeteer.get(), BloodDrunkPuppeteerRenderer::new);
			event.registerEntityRenderer(EntityInit.enthralled_doll.get(), EnthralledDollRenderer::new);
			event.registerEntityRenderer(EntityInit.lump_of_thought.get(), LumpOfThoughtRenderer::new);
			event.registerEntityRenderer(EntityInit.chthonian_queen.get(), ChthonianQueenRenderer::new);
			event.registerEntityRenderer(EntityInit.abhorent_thought.get(), AbhorentThoughtRenderer::new);
			event.registerEntityRenderer(EntityInit.barbed_urchin.get(), BarbedUrchinRenderer::new);
			event.registerEntityRenderer(EntityInit.erythromycelium_eruptus.get(), ErythromyceliumEruptusRenderer::new);
			event.registerEntityRenderer(EntityInit.morphling_polyp.get(), MorphlingPolypRenderer::new);
			event.registerEntityRenderer(EntityInit.flying_charm.get(), ThrownItemRenderer::new);

		}

		
		

		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			MinecraftForge.EVENT_BUS.register(RenderBloodLaserEvent.class);

			// Tiles
			BlockEntityRenderers.register(BlockEntityInit.runic_chisel_station.get(), ChiselStationRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.morphling_incubator.get(), MorphlingIncubatorRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.unstained_podium.get(), UnstainedPodiumRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.scrying_podium.get(), ScryingPodiumRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.fungal_podium.get(),FungalPodiumRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.fungal_implantation_pylon.get(),FungalImplantationPylonRenderer::new);

			BlockEntityRenderers.register(BlockEntityInit.dendritic_distributor.get(),
					DendriticDistributorRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.mortal_display.get(), MortalDisplayRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.vial_centrifuge.get(), VialCentrifugeRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.visceral_artificial_recaller.get(),
					VisceralRecallerRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.earthen_vein.get(), EarthenVeinRenderer::new);
			MenuScreens.register(ContainerInit.gourd_charm_inventory.get(), CharmGourdScreen::new);
			MenuScreens.register(ContainerInit.fungal_implantation.get(), SporeImplantScreen::new);
			MenuScreens.register(ContainerInit.vial_centrifuge.get(), VialCentrifugeScreen::new);
			MenuScreens.register(ContainerInit.visceral_recaller.get(), VisceralRecallerScreen::new);
			MenuScreens.register(ContainerInit.morphling_jar.get(), MorphlingJarScreen::new);
			MenuScreens.register(ContainerInit.living_syringe.get(), LivingSyringeScreen::new);
			MenuScreens.register(ContainerInit.living_staff.get(), LivingStaffScreen::new);
			MenuScreens.register(ContainerInit.juiceinator.get(), JuiceinatorScreen::new);
			MenuScreens.register(ContainerInit.runic_chisel_station.get(), ChiselStationScreen::new);
			MenuScreens.register(ContainerInit.rune_binder.get(), RuneBinderScreen	::new);

		}

		@SubscribeEvent
		public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
			event.register(ClientEvents.bloodFormation);
			event.register(ClientEvents.bloodCrafting);
			event.register(ClientEvents.bloodDraw);
			event.register(ClientEvents.toggleMorphlingJarPickup);
			event.register(ClientEvents.toggleMorphlingOpenJar);
			event.register(ClientEvents.cycleSelectedManip);
			event.register(ClientEvents.useQuickManip);
			event.register(ClientEvents.useContManip);
			event.register(ClientEvents.OPEN_CHARM_SLOT_KEYBIND);
			event.register(ClientEvents.openVascCharmMenu);

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

		// Overlay
		@SubscribeEvent
		public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
			event.registerAboveAll("bloodvolume", BloodVolumeOverlay.HUD_BLOODVOLUME);
		}
	}
}
