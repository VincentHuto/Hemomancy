package com.huto.hemomancy.capa.vascular;

import java.awt.Color;
import java.awt.Point;
import java.util.Map;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketVascularSystemServer;
import com.hutoslib.client.TextUtils;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class VascularSystemEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			System.out.println("Attatches Capability");
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "vascularsystem"), new VascularSystemProvider());
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		Map<EnumVeinSections, Float> BloodFlow = VascularSystemProvider.getPlayerVascularSystem(player);
		PacketHandler.CHANNELVASCULARSYSTEM.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketVascularSystemServer(BloodFlow));
		player.sendStatusMessage(
				new StringTextComponent("Welcome! Current Vascular System: " + TextFormatting.GOLD + BloodFlow), false);
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		Map<EnumVeinSections, Float> BloodFlow = VascularSystemProvider.getPlayerVascularSystem(player);
		PacketHandler.CHANNELVASCULARSYSTEM.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketVascularSystemServer(BloodFlow));
		player.sendStatusMessage(
				new StringTextComponent("Welcome! Current Vascular System: " + TextFormatting.GOLD + BloodFlow), false);
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		IVascularSystem BloodFlowOld = event.getOriginal().getCapability(VascularSystemProvider.VASCULAR_CAPA)
				.orElseThrow(IllegalStateException::new);
		IVascularSystem BloodFlowNew = event.getEntity().getCapability(VascularSystemProvider.VASCULAR_CAPA)
				.orElseThrow(IllegalStateException::new);
		BloodFlowNew.setVascularSystem(BloodFlowOld.getVascularSystem());
	}

	@SubscribeEvent
	public static void respawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntity();
			if (!player.getEntityWorld().isRemote) {
				IVascularSystem section = player.getCapability(VascularSystemProvider.VASCULAR_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				PacketHandler.CHANNELVASCULARSYSTEM.send(
						PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
						new PacketVascularSystemServer(section.getVascularSystem()));
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent e) {
		if (e.getEntityLiving() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) e.getEntity();
			if (!player.getEntityWorld().isRemote) {

				IVascularSystem system = player.getCapability(VascularSystemProvider.VASCULAR_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				for (EnumVeinSections section : system.getVascularSystem().keySet()) {

					if (section != EnumVeinSections.HEART) {
						system.setVascularSectionHealth(section, -player.world.rand.nextFloat() * 3f);
						PacketHandler.CHANNELVASCULARSYSTEM.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
								new PacketVascularSystemServer(system.getVascularSystem()));
					}
				}
			}
		}
	}

	private static FontRenderer fontRenderer;

	@SuppressWarnings({ "deprecation", "unused" })
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(receiveCanceled = true)
	public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {

		if (fontRenderer == null) {
			fontRenderer = Minecraft.getInstance().fontRenderer;
		}
		PlayerEntity player = Minecraft.getInstance().player;
		if (player != null) {
			if (player.isAlive()) {
				IVascularSystem section = player.getCapability(VascularSystemProvider.VASCULAR_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				ItemStack stack = player.getHeldItemMainhand();
				Item item = stack.getItem();

				// Allegiance Identifier overlay
				if (item == ItemInit.mind_spike.get()) {
					Item renderItem = ItemInit.mind_spike.get();
					int centerX = (Minecraft.getInstance().getMainWindow().getScaledWidth() / 2) - 6;
					int centerY = (Minecraft.getInstance().getMainWindow().getScaledHeight() / 2) - 15;
					double angleBetweenEach = 360.0 / EnumVeinSections.values().length;
					Point point = new Point(centerX - 45, centerY - 36), center = new Point(centerX, centerY);
					for (int i = 0; i < section.getVascularSystem().keySet().size(); i++) {
						EnumVeinSections selectedSection = (EnumVeinSections) section.getVascularSystem().keySet()
								.toArray()[i];
						GlStateManager.pushMatrix();
						fontRenderer.drawString(event.getMatrixStack(),
								TextUtils.toProperCase(selectedSection.toString()), point.x, point.y + 20,
								new Color(255, 0, 0, 255).getRGB());
						fontRenderer.drawString(event.getMatrixStack(),
								String.valueOf(section.getHealthBySection(selectedSection)), point.x, point.y + 30,
								new Color(255, 0, 0, 255).getRGB());
						fontRenderer.drawString(event.getMatrixStack(),
								String.valueOf(section.getBloodFlowBySection(selectedSection)), point.x, point.y + 40,
								new Color(255, 0, 0, 255).getRGB());
						GlStateManager.popMatrix();
						/*
						 * if (selectedSection.equals(EnumVeinSections.SELF)) { renderItem =
						 * Items.CRAFTING_TABLE; } else if
						 * (selectedSection.equals(EnumVeinSections.HASTUR)) { renderItem =
						 * ItemInit.yellow_sign.get(); } else if
						 * (selectedSection.equals(EnumVeinSections.ELDRITCH)) { renderItem =
						 * ItemInit.everwatchful_pendant.get(); } else if
						 * (selectedSection.equals(EnumVeinSections.ASCENDENT)) { renderItem =
						 * ItemInit.crossed_keys.get(); } else if
						 * (selectedSection.equals(EnumVeinSections.MACHINE)) { renderItem =
						 * ItemInit.integral_cog.get(); } else if
						 * (selectedSection.equals(EnumVeinSections.BEAST)) { renderItem =
						 * ItemInit.breath_of_the_beast.get(); } else { renderItem = Items.BARRIER; }
						 */
						GlStateManager.pushMatrix();
						GlStateManager.enableAlphaTest();
						GlStateManager.enableBlend();
						Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(new ItemStack(renderItem),
								point.x, point.y);
						// GuiUtil.drawScaledTexturedModalRect(point.x, point.y, 0, 0, 16, 16, 0.062f);
						GlStateManager.disableBlend();
						GlStateManager.disableAlphaTest();
						GlStateManager.popMatrix();
						point = rotatePointAbout(point, center, angleBetweenEach);
					}
				}

				// Redraws Icons so they dont get overwrote
				// GuiUtil.drawTexturedModalRect(0, 0, 0, 0, 16, 16);
				Minecraft.getInstance().textureManager
						.bindTexture(new ResourceLocation("minecraft", "textures/gui/icons.png"));

				// Coven color Overlay
				/*
				 * if (player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() !=
				 * ItemInit.influence_supressor.get() &&
				 * !(EnchantmentHelper.getEnchantments(player.getItemStackFromSlot(
				 * EquipmentSlotType.HEAD))
				 * .containsKey(EnchantmentInit.influence_suppression.get()))) {}
				 */
				for (EnumVeinSections sections : section.getVascularSystem().keySet()) {
					if (section.getHealthBySection(sections) >= 10) {
						float devoMult = (section.getHealthBySection(sections) / 3) < 250
								? (section.getHealthBySection(sections) / 3)
								: 250;
						switch (sections) {
						/*
						 * case HASTUR: AbstractGui.fill(event.getMatrixStack(), 0, 0,
						 * event.getWindow().getWidth(), event.getWindow().getHeight(), new Color(255,
						 * 255, 0, devoMult).getRGB()); fontRenderer.drawString(event.getMatrixStack(),
						 * "Hasturs View", 5, 5, new Color(255, 0, 0, 15).getRGB());
						 * Minecraft.getInstance().textureManager .bindTexture(new
						 * ResourceLocation("minecraft", "textures/gui/icons.png")); break; case
						 * ELDRITCH: AbstractGui.fill(event.getMatrixStack(), 0, 0,
						 * event.getWindow().getWidth(), event.getWindow().getHeight(), new Color(255,
						 * 0, 255, devoMult).getRGB()); fontRenderer.drawString(event.getMatrixStack(),
						 * "Azathoth View", 5, 5, new Color(255, 0, 0, 15).getRGB());
						 * Minecraft.getInstance().textureManager .bindTexture(new
						 * ResourceLocation("minecraft", "textures/gui/icons.png")); break; case
						 * ASCENDENT: AbstractGui.fill(event.getMatrixStack(), 0, 0,
						 * event.getWindow().getWidth(), event.getWindow().getHeight(), new Color(255,
						 * 255, 255, devoMult).getRGB());
						 * fontRenderer.drawString(event.getMatrixStack(), "Seraph View", 5, 5, new
						 * Color(255, 0, 0, 15).getRGB()); Minecraft.getInstance().textureManager
						 * .bindTexture(new ResourceLocation("minecraft", "textures/gui/icons.png"));
						 * break; case BEAST: AbstractGui.fill(event.getMatrixStack(), 0, 0,
						 * event.getWindow().getWidth(), event.getWindow().getHeight(), new Color(255,
						 * 0, 0, devoMult).getRGB()); fontRenderer.drawString(event.getMatrixStack(),
						 * "Beast View", 5, 5, new Color(255, 0, 0, 15).getRGB());
						 * Minecraft.getInstance().textureManager .bindTexture(new
						 * ResourceLocation("minecraft", "textures/gui/icons.png")); break;
						 * 
						 * case MACHINE: AbstractGui.fill(event.getMatrixStack(), 0, 0,
						 * event.getWindow().getWidth(), event.getWindow().getHeight(), new Color(218,
						 * 96, 28, devoMult).getRGB()); fontRenderer.drawString(event.getMatrixStack(),
						 * "Machine View", 5, 5, new Color(255, 0, 0, 15).getRGB());
						 * Minecraft.getInstance().textureManager .bindTexture(new
						 * ResourceLocation("minecraft", "textures/gui/icons.png")); break;
						 * 
						 * case SELF: fontRenderer.drawString(event.getMatrixStack(), "Self Devotee", 5,
						 * 5, new Color(255, 0, 0, 15).getRGB()); Minecraft.getInstance().textureManager
						 * .bindTexture(new ResourceLocation("minecraft", "textures/gui/icons.png"));
						 * break;
						 */
						default:
							/*
							 * AbstractGui.fill(event.getMatrixStack(), 0, 0, event.getWindow().getWidth(),
							 * event.getWindow().getHeight(), new Color(0, 0, 0, 0).getRGB());
							 * fontRenderer.drawString(event.getMatrixStack(), "No BloodTendency", 5, 5, new
							 * Color(0, 0, 0, 0).getRGB());
							 */
							Minecraft.getInstance().textureManager
									.bindTexture(new ResourceLocation("minecraft", "textures/gui/icons.png"));
							break;
						}
					}
				}
			}
		}
	}

	private static Point rotatePointAbout(Point in, Point about, double degrees) {
		double rad = degrees * Math.PI / 180.0;
		double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
		double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
		return new Point((int) newX, (int) newY);
	}

}