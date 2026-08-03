package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.render.world.MnemonicBlueprintProgress;
import com.vincenthuto.hemomancy.client.render.world.MnemonicBlueprintRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public final class MnemonicBlueprintProgressOverlay {
	private static final int MAX_MATERIALS = 3;

	private MnemonicBlueprintProgressOverlay() {
	}

	public static void renderHUD(GuiGraphics graphics, int screenWidth, int screenHeight) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!MnemonicBlueprintRenderer.shouldRenderProgressOverlay()
				|| minecraft.player == null || minecraft.level == null
				|| minecraft.options.hideGui || minecraft.screen != null) return;

		MnemonicBlueprintProgress.Summary summary = MnemonicBlueprintRenderer.progress();
		MutableComponent text = summary.remaining() == 0
				? Component.translatable("hud.hemomancy.mnemonic_blueprint.complete")
						.withStyle(ChatFormatting.GREEN)
				: Component.translatable("hud.hemomancy.mnemonic_blueprint.remaining", summary.remaining())
						.withStyle(ChatFormatting.RED);
		for (MnemonicBlueprintProgress.Entry entry : summary.visibleEntries(MAX_MATERIALS)) {
			Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(entry.materialId()));
			text.append(Component.literal("  •  ").withStyle(ChatFormatting.DARK_GRAY));
			text.append(block.getName().copy().withStyle(ChatFormatting.GRAY));
			text.append(Component.literal(" ×" + entry.count()).withStyle(ChatFormatting.WHITE));
		}
		int hidden = summary.hiddenTypes(MAX_MATERIALS);
		if (hidden > 0) {
			text.append(Component.translatable("hud.hemomancy.mnemonic_blueprint.more_types", hidden)
					.withStyle(ChatFormatting.DARK_GRAY));
		}

		int width = minecraft.font.width(text);
		int x = (screenWidth - width) / 2;
		int y = screenHeight - 67;
		graphics.fill(x - 4, y - 3, x + width + 4, y + 11, 0xA010080C);
		graphics.renderOutline(x - 4, y - 3, width + 8, 14, 0x805F263D);
		graphics.drawString(minecraft.font, text, x, y, 0xFFFFFFFF, true);
	}
}
