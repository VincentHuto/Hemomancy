package com.vincenthuto.hemomancy.capa.player.tendency;

import java.awt.Color;
import java.awt.Point;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.BloodTendencyServerPacket;
import com.vincenthuto.hemomancy.tile.VisceralRecallerBlockEntity;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.MathUtils;

//GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class BloodTendencyEvents {
	private static Font fontRenderer;

	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Hemomancy.rloc("bloodtendancy"), new BloodTendencyProvider());
		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
		if (event.getObject() instanceof VisceralRecallerBlockEntity) {
			event.addCapability(Hemomancy.rloc("bloodtendancy"), new BloodTendencyProvider());
		}
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumBloodTendency, Float> BloodTendency = BloodTendencyProvider.getPlayerTendency(player);
		PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodTendencyServerPacket(BloodTendency));
//		player.displayClientMessage(
//				Component.literal("Welcome! Current Blood Tendency: " + ChatFormatting.GOLD + BloodTendency), false);
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent e) {
//		// Radiant Protection
//		if (e.getEntityLiving() instanceof Player) {
//			Player player = (Player) e.getEntityLiving();
//			IRunesItemHandler itemHandler = player.getCapability(RunesCapabilities.RUNES)
//					.orElseThrow(NullPointerException::new);
//			if (itemHandler.getStackInSlot(0).getItem() == ItemInit.rune_radiance_c.get()) {
//				double dist = e.getEntityLiving().distanceTo(player);
//				HitResult trace = e.getEntityLiving().pick(dist, 0, false);
//				PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketEntityHitParticle(trace.getLocation().x,
//						trace.getLocation().y, trace.getLocation().z));
//			}
//			if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ItemInit.chitinite_chestplate.get()) {
//				e.setAmount((float) (e.getAmount() * 0.25));
//				double dist = e.getEntityLiving().distanceTo(player);
//				HitResult trace = e.getEntityLiving().pick(dist, 0, false);
//				PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketEntityHitParticle(trace.getLocation().x,
//						trace.getLocation().y, trace.getLocation().z));
//			}
//
//		}
	}

	@SubscribeEvent
	public static void onPlayerHitsBlock(PlayerInteractEvent.LeftClickEmpty e) {
		/*
		 * // Causes particles when the air is hit if (e.getLevel().isRemote) {
		 * PacketHandler.CHANNELBLOODVOLUME .sendToServer(new
		 * PacketGroundBloodDraw(ClientEventSubscriber.getPartialTicks())); }
		 */
	}

	@SuppressWarnings({ "deprecation", "unused" })
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(receiveCanceled = true)
	public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {

		if (fontRenderer == null) {
			fontRenderer = Minecraft.getInstance().font;
		}
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			if (player.isAlive()) {
				IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				ItemStack stack = player.getMainHandItem();
				Item item = stack.getItem();

				// Allegiance Identifier overlay
				if (item == ItemInit.sanguine_conduit.get()) {
					Item renderItem = ItemInit.sanguine_formation.get();
					int centerX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) - 6;
					int centerY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2) - 15;
					double angleBetweenEach = 360.0 / EnumBloodTendency.values().length;
					Point point = new Point(centerX - 34, centerY - 36), center = new Point(centerX, centerY);
					for (int i = 0; i < tendency.getTendency().keySet().size(); i++) {
						EnumBloodTendency selectedCoven = (EnumBloodTendency) tendency.getTendency().keySet()
								.toArray()[i];
						// GlStateManager._pushMatrix();
						fontRenderer.draw(event.getPoseStack(), HLTextUtils.toProperCase(selectedCoven.toString()),
								point.x, point.y + 20, new Color(255, 0, 0, 255).getRGB());
						fontRenderer.draw(event.getPoseStack(),
								String.valueOf(tendency.getAlignmentByTendency(selectedCoven)), point.x, point.y + 30,
								new Color(255, 0, 0, 255).getRGB());

						Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(event.getPoseStack(), new ItemStack(renderItem),
								point.x, point.y);

						point = MathUtils.rotatePointAbout(point, center, angleBetweenEach);
					}
				}

				// Redraws Icons so they dont get overwrote
				// GuiUtil.drawTexturedModalRect(0, 0, 0, 0, 16, 16);
				Minecraft.getInstance().textureManager
						.bindForSetup(new ResourceLocation("minecraft", "textures/gui/icons.png"));

				for (EnumBloodTendency tendencys : tendency.getTendency().keySet()) {
					if (tendency.getAlignmentByTendency(tendencys) >= 10) {
						float devoMult = (tendency.getAlignmentByTendency(tendencys) / 3) < 250
								? (tendency.getAlignmentByTendency(tendencys) / 3)
								: 250;
						switch (tendencys) {

						default:
							/*
							 * AbstractGui.fill(event.getPoseStack(), 0, 0, event.getWindow().getWidth(),
							 * event.getWindow().getHeight(), new Color(0, 0, 0, 0).getRGB());
							 *
							 * fontRenderer.drawString(event.getPoseStack(), "No BloodTendency", 5, 5, new
							 * Color(0, 0, 0, 0).getRGB());
							 */
							Minecraft.getInstance().textureManager
									.bindForSetup(new ResourceLocation("minecraft", "textures/gui/icons.png"));
							break;
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {

		Player peorig = event.getOriginal();
		Player playernew = event.getEntity();
		if (event.isWasDeath()) {
			peorig.reviveCaps();
			IBloodTendency bloodTendencyNew = playernew.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(IllegalStateException::new);
			bloodTendencyNew.setTendency(peorig.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(IllegalArgumentException::new).getTendency());
			peorig.invalidateCaps();
		}

	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumBloodTendency, Float> BloodTendency = BloodTendencyProvider.getPlayerTendency(player);
		PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodTendencyServerPacket(BloodTendency));
//		player.displayClientMessage(
//				Component.literal("Welcome! Current Blood Tendency: " + ChatFormatting.GOLD + BloodTendency), false);
	}

	@SubscribeEvent
	public static void respawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			if (!player.getCommandSenderWorld().isClientSide) {
				IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
						new BloodTendencyServerPacket(tendency.getTendency()));
			}
		}
	}

}