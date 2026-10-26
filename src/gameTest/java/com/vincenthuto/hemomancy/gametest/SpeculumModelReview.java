package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Native item bake and inventory preview, without opening a world. Excluded from release jars. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SpeculumModelReview {
    private static int ticks;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.speculumReview")) return;
        var client = Minecraft.getInstance();
        if (client.getOverlay() != null) return;
        if (client.screen instanceof TitleScreen) client.setScreen(new Preview());
        if (!(client.screen instanceof Preview)) return;
        if (++ticks == 50) Screenshot.grab(client.gameDirectory,
                "speculum-" + System.getProperty("hemomancy.speculumReviewLabel", "after") + ".png",
                client.getMainRenderTarget(), message -> {});
        if (ticks == 70) client.stop();
    }

    private static final class Preview extends Screen {
        private Preview() { super(Component.empty()); }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xFF17151C);
            graphics.drawCenteredString(font, "Terrestrial Speculum", width / 2, 12, 0xFFE9D8C0);
            ItemStack stack = new ItemStack(ItemInit.terrestrial_speculum.get());
            graphics.pose().pushPose();
            graphics.pose().translate(width * .34F - 64, height * .5F - 64, 0);
            graphics.pose().scale(8, 8, 8);
            graphics.renderItem(stack, 0, 0);
            graphics.pose().popPose();
            int x = (int) (width * .74F), y = height / 2;
            graphics.fill(x - 2, y - 2, x + 18, y + 18, 0xFF36313D);
            graphics.renderItem(stack, x, y);
            graphics.drawCenteredString(font, "Inventory", x + 8, y + 26, 0xFFB3A7AC);
        }
    }
}
