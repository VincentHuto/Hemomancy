package com.huto.hemomancy.capabilities.tendency;

import java.awt.Color;
import java.awt.Point;
import java.util.Map;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.capabilities.mindrunes.IRunesItemHandler;
import com.huto.hemomancy.capabilities.mindrunes.RunesCapabilities;
import com.huto.hemomancy.event.ClientEventSubscriber;
import com.huto.hemomancy.font.ModTextFormatting;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.network.PacketEntityHitParticle;
import com.huto.hemomancy.network.PacketGroundBloodDraw;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.BloodTendencyPacketServer;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class BloodTendencyEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof PlayerEntity) {
			System.out.println("Attatches Capability");
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "bloodtendancy"), new BloodTendencyProvider());
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		Map<EnumBloodTendency, Float> BloodTendency = BloodTendencyProvider.getPlayerTendency(player);
		PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodTendencyPacketServer(BloodTendency));
		player.sendStatusMessage(
				new StringTextComponent("Welcome! Current Blood Tendency: " + TextFormatting.GOLD + BloodTendency),
				false);
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		Map<EnumBloodTendency, Float> BloodTendency = BloodTendencyProvider.getPlayerTendency(player);
		PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodTendencyPacketServer(BloodTendency));
		player.sendStatusMessage(
				new StringTextComponent("Welcome! Current Blood Tendency: " + TextFormatting.GOLD + BloodTendency),
				false);
	}

	@SubscribeEvent
	public static void onPlayerHitsBlock(PlayerInteractEvent.LeftClickEmpty e) {
		// Causes particles when the air is hit
		if (e.getWorld().isRemote) {
			PacketHandler.CHANNELBLOODVOLUME
					.sendToServer(new PacketGroundBloodDraw(ClientEventSubscriber.getPartialTicks()));
		}
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent e) {
		// Radiant Protection
		if (e.getEntityLiving() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) e.getEntityLiving();
			IRunesItemHandler itemHandler = player.getCapability(RunesCapabilities.RUNES)
					.orElseThrow(NullPointerException::new);
			if (itemHandler.getStackInSlot(0).getItem() == ItemInit.rune_radiance_c.get()) {
				double dist = e.getEntityLiving().getDistance(player);
				RayTraceResult trace = e.getEntityLiving().pick(dist, 0, false);
				PacketHandler.CHANNELBLOODVOLUME.sendToServer(
						new PacketEntityHitParticle(trace.getHitVec().x, trace.getHitVec().y, trace.getHitVec().z));
			}
		}
		// Makes it so everytime a player hits an entity, cause particles
		/*
		 * if (e.getSource().getTrueSource() instanceof PlayerEntity) { PlayerEntity
		 * player = (PlayerEntity) e.getSource().getTrueSource(); double dist =
		 * player.getDistance(e.getEntityLiving()); RayTraceResult trace =
		 * player.pick(dist, 0, false); PacketHandler.CHANNELBLOODVOLUME.sendToServer(
		 * new PacketEntityHitParticle(trace.getHitVec().x, trace.getHitVec().y,
		 * trace.getHitVec().z)); }
		 */
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		IBloodTendency BloodTendencyOld = event.getOriginal().getCapability(BloodTendencyProvider.TENDENCY_CAPA)
				.orElseThrow(IllegalStateException::new);
		IBloodTendency BloodTendencyNew = event.getEntity().getCapability(BloodTendencyProvider.TENDENCY_CAPA)
				.orElseThrow(IllegalStateException::new);
		BloodTendencyNew.setTendency(BloodTendencyOld.getTendency());
	}

	@SubscribeEvent
	public static void respawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntity();
			if (!player.getEntityWorld().isRemote) {
				IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				PacketHandler.CHANNELBLOODTENDENCY.send(
						PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
						new BloodTendencyPacketServer(tendency.getTendency()));
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
				IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				ItemStack stack = player.getHeldItemMainhand();
				Item item = stack.getItem();

				// Allegiance Identifier overlay
				if (item == ItemInit.self_reflection_mirror.get()) {
					Item renderItem = ItemInit.sanguine_formation.get();
					int centerX = (Minecraft.getInstance().getMainWindow().getScaledWidth() / 2) - 6;
					int centerY = (Minecraft.getInstance().getMainWindow().getScaledHeight() / 2) - 15;
					double angleBetweenEach = 360.0 / EnumBloodTendency.values().length;
					Point point = new Point(centerX - 34, centerY - 36), center = new Point(centerX, centerY);
					for (int i = 0; i < tendency.getTendency().keySet().size(); i++) {
						EnumBloodTendency selectedCoven = (EnumBloodTendency) tendency.getTendency().keySet()
								.toArray()[i];
						GlStateManager.pushMatrix();
						fontRenderer.drawString(event.getMatrixStack(),
								ModTextFormatting.toProperCase(selectedCoven.toString()), point.x, point.y + 20,
								new Color(255, 0, 0, 255).getRGB());
						fontRenderer.drawString(event.getMatrixStack(),
								String.valueOf(tendency.getTendencyByTendency(selectedCoven)), point.x, point.y + 30,
								new Color(255, 0, 0, 255).getRGB());
						GlStateManager.popMatrix();
						/*
						 * if (selectedCoven.equals(EnumBloodTendency.SELF)) { renderItem =
						 * Items.CRAFTING_TABLE; } else if
						 * (selectedCoven.equals(EnumBloodTendency.HASTUR)) { renderItem =
						 * ItemInit.yellow_sign.get(); } else if
						 * (selectedCoven.equals(EnumBloodTendency.ELDRITCH)) { renderItem =
						 * ItemInit.everwatchful_pendant.get(); } else if
						 * (selectedCoven.equals(EnumBloodTendency.ASCENDENT)) { renderItem =
						 * ItemInit.crossed_keys.get(); } else if
						 * (selectedCoven.equals(EnumBloodTendency.MACHINE)) { renderItem =
						 * ItemInit.integral_cog.get(); } else if
						 * (selectedCoven.equals(EnumBloodTendency.BEAST)) { renderItem =
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
				for (EnumBloodTendency tendencys : tendency.getTendency().keySet()) {
					if (tendency.getTendencyByTendency(tendencys) >= 10) {
						float devoMult = (tendency.getTendencyByTendency(tendencys) / 3) < 250
								? (tendency.getTendencyByTendency(tendencys) / 3)
								: 250;
						switch (tendencys) {
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
							 * 
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