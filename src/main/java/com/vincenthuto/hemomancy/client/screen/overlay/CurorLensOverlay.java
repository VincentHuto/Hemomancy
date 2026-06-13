package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.shared.CurorLensRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CurorLensOverlay {
	public static CurorLensOverlay instance;

	private static final ResourceLocation OVERLAY = Hemomancy.rloc("textures/gui/curor_lens_overlay.png");
	private final Minecraft mc = Minecraft.getInstance();

	public void renderHUD(GuiGraphics gfx, int width, int height, float partialTicks) {
		LocalPlayer player = mc.player;
		if (player == null || mc.level == null || !isUsingLens(player)) {
			return;
		}

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		gfx.fill(0, 0, width, height, CurorLensRules.screenTintColor());
		int lensSize = Math.min(width, height);
		int lensX = (width - lensSize) / 2;
		int lensY = (height - lensSize) / 2;
		gfx.blit(OVERLAY, lensX, lensY, 0, 0, lensSize, lensSize, 256, 256);
		RenderSystem.disableBlend();

		List<Component> lines = diagnoseTarget(player, partialTicks);
		renderReadout(gfx, lines, width, height);
	}

	private static boolean isUsingLens(Player player) {
		return player.isUsingItem() && player.getUseItem().is(ItemInit.curor_lens.get());
	}

	private List<Component> diagnoseTarget(LocalPlayer player, float partialTicks) {
		Optional<LivingEntity> target = findTarget(player, partialTicks);
		if (target.isEmpty()) {
			return List.of(Component.translatable("item.hemomancy.curor_lens.no_resonance")
					.withStyle(ChatFormatting.DARK_RED));
		}

		LivingEntity entity = target.get();
		List<Component> lines = new ArrayList<>();
		lines.add(entity.getDisplayName().copy().withStyle(ChatFormatting.RED));
		lines.add(Component.translatable("item.hemomancy.curor_lens.health",
				CurorLensRules.healthLine(entity.getHealth(), entity.getMaxHealth())).withStyle(ChatFormatting.GRAY));

		HemoCapabilityAccess.getBloodVolume(entity).ifPresentOrElse(volume -> {
			lines.add(Component.translatable("item.hemomancy.curor_lens.blood",
					CurorLensRules.bloodLine(volume.getBloodVolume(), volume.getMaxBloodVolume()))
					.withStyle(volume.isActive() ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GRAY));
			Bloodline bloodline = volume.getBloodLine();
			if (bloodline != null && bloodline.isValid()) {
				lines.add(Component.translatable("item.hemomancy.curor_lens.bloodline", bloodline.getName())
						.withStyle(ChatFormatting.GOLD));
			}
		}, () -> lines.add(Component.translatable("item.hemomancy.curor_lens.blood_unknown")
				.withStyle(ChatFormatting.DARK_GRAY)));

		HemoCapabilityAccess.getBloodTendency(entity).ifPresentOrElse(tendency -> {
			float[] values = new float[EnumBloodTendency.values().length];
			for (int i = 0; i < values.length; i++) {
				values[i] = tendency.getAlignmentByTendency(EnumBloodTendency.values()[i]);
			}
			lines.add(Component.translatable("item.hemomancy.curor_lens.tendency",
					CurorLensRules.dominantTendencyName(values)).withStyle(ChatFormatting.LIGHT_PURPLE));
		}, () -> lines.add(Component.translatable("item.hemomancy.curor_lens.tendency_unknown")
				.withStyle(ChatFormatting.DARK_GRAY)));

		if (entity instanceof Player targetPlayer) {
			lines.add(scarLine(targetPlayer));
		} else {
			lines.add(Component.translatable("item.hemomancy.curor_lens.scars_unreadable")
					.withStyle(ChatFormatting.DARK_GRAY));
		}
		return lines;
	}

	private Component scarLine(Player player) {
		return HemoCapabilityAccess.getScars(player).map(scars -> {
			List<String> names = new ArrayList<>();
			int count = 0;
			for (int i = 0; i < scars.getSlots(); i++) {
				ItemStack stack = scars.getStackInSlot(i);
				if (stack.getItem() instanceof ItemScar) {
					count++;
					if (names.size() < CurorLensRules.MAX_SCAR_NAMES) {
						names.add(stack.getHoverName().getString());
					}
				}
			}
			String suffix = names.isEmpty() ? Integer.toString(count) : count + " - " + String.join(", ", names);
			return Component.translatable("item.hemomancy.curor_lens.scars", suffix)
					.withStyle(ChatFormatting.DARK_PURPLE);
		}).orElse(Component.translatable("item.hemomancy.curor_lens.scars_unknown")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	private Optional<LivingEntity> findTarget(LocalPlayer player, float partialTicks) {
		Vec3 eye = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 end = eye.add(look.scale(CurorLensRules.SCAN_RANGE));
		AABB search = player.getBoundingBox().expandTowards(look.scale(CurorLensRules.SCAN_RANGE)).inflate(1.0D);
		return mc.level.getEntities(player, search, entity -> entity instanceof LivingEntity
						&& entity.isPickable() && !entity.isSpectator()).stream()
				.map(entity -> new TargetHit((LivingEntity) entity, hitDistanceSqr(entity, eye, end)))
				.filter(hit -> hit.distanceSqr() >= 0.0D)
				.min(Comparator.comparingDouble(TargetHit::distanceSqr))
				.map(TargetHit::entity);
	}

	private static double hitDistanceSqr(Entity entity, Vec3 eye, Vec3 end) {
		AABB box = entity.getBoundingBox().inflate(Math.max(0.25D, entity.getPickRadius()));
		return box.clip(eye, end).map(eye::distanceToSqr).orElse(-1.0D);
	}

	private void renderReadout(GuiGraphics gfx, List<Component> lines, int width, int height) {
		Font font = mc.font;
		int panelWidth = Math.min(210, width - 24);
		int x = Mth.clamp(width / 2 + 42, 12, Math.max(12, width - panelWidth - 12));
		int y = Math.max(16, height / 2 - 58);
		int lineHeight = 11;
		int panelHeight = 16 + lines.size() * lineHeight;
		gfx.fill(x - 7, y - 7, x + panelWidth, y + panelHeight, 0xAA120307);
		gfx.fill(x - 6, y - 6, x + panelWidth - 1, y - 5, 0xCC8F3030);
		for (int i = 0; i < lines.size(); i++) {
			Component line = lines.get(i);
			gfx.drawString(font, line, x, y + i * lineHeight, 0xFFE3B3A6, true);
		}
	}

	private record TargetHit(LivingEntity entity, double distanceSqr) {
	}
}
