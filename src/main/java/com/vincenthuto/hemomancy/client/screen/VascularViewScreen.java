package com.vincenthuto.hemomancy.client.screen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.vascular.VascularSystemProvider;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.VascularViewMenu;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class VascularViewScreen extends EffectRenderingInventoryScreen<VascularViewMenu> {

    public static final ResourceLocation background = new ResourceLocation(Hemomancy.MOD_ID,
            "textures/gui/vascular_view.png");
    public static final ResourceLocation border = new ResourceLocation(Hemomancy.MOD_ID,
            "textures/gui/vascular_border.png");
    public double dragLeftRight = 0.0;
    public double dragUpDown = 0.0;
    public int guiHeight = 254;
    public int guiWidth = 190;
    protected int left;
    protected int top;
    protected Minecraft mc = Minecraft.getInstance();
    double xDragPos = 0.0;
    double yDragPos = 0.0;
    private float oldMouseX;
    private float oldMouseY;

    public VascularViewScreen(VascularViewMenu container, Inventory inventory, Component name) {
        super(container, inventory, name);
    }


    @Override
    protected void init() {
        this.left = this.width / 2 - this.guiWidth / 2;
        this.top = this.height / 2 - this.guiHeight / 2;
        this.clearWidgets();
        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        //super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderBackground(graphics); // renderBackground
        this.renderBg(graphics, partialTicks, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY); // renderHoveredToolTip
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
        int centerX = this.width / 2 - this.guiWidth / 2;
        int centerY = this.height / 2 - this.guiHeight / 2;
        graphics.blit(border, centerX, centerY, 0, 0, this.guiWidth, this.guiHeight);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        super.renderBackground(guiGraphics);
    }


    @Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
        this.left = this.width / 2 - this.guiWidth / 2;
        this.top = this.height / 2 - this.guiHeight / 2;
        int centerX = this.width / 2 - this.guiWidth / 2;
        int centerY = this.height / 2 - this.guiHeight / 2;
        graphics.blit(background, centerX+16, centerY+14, 0, 0, this.guiWidth, this.guiHeight);
        int k = this.leftPos;
        int l = this.topPos;

        LocalPlayer player = this.minecraft.player;
        player.getCapability(VascularSystemProvider.VASCULAR_CAPA).ifPresent(vascularSystem -> {
            ItemStack stack = player.getMainHandItem();
            Item item = stack.getItem();
            Item renderItem = ItemInit.dried_leech.get();
            double angleBetweenEach = 360.0 / EnumVeinSections.values().length;
            Point point = new Point(centerX - 45, centerY - 36), center = new Point(centerX, centerY);
            for (int i = 0; i < vascularSystem.getVascularSystem().keySet().size(); i++) {
                EnumVeinSections selectedSection = (EnumVeinSections) vascularSystem.getVascularSystem().keySet()
                        .toArray()[i];
                graphics.drawCenteredString(font,
                        HLTextUtils.toProperCase(selectedSection.toString()), point.x+ guiWidth / 2, point.y  + guiHeight / 2,
                        new Color(255, 0, 0, 255).getRGB());
                graphics.drawCenteredString(font,
                        String.valueOf(vascularSystem.getBloodFlowBySection(selectedSection)), point.x+ guiWidth / 2, point.y +10  + guiHeight / 2,
                        new Color(255, 0, 0, 255).getRGB());
                graphics.renderItem(new ItemStack(renderItem),  point.x+ guiWidth / 2, point.y -20 + guiHeight / 2);

                point = rotatePointAbout(point, center, angleBetweenEach);
            }


        });

        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, k + guiWidth / 2, l + guiHeight / 2, 30, k + 51 - this.oldMouseX,
                l + 75 - 50 - this.oldMouseY, player);
    }
    private static Point rotatePointAbout(Point in, Point about, double degrees) {
        double rad = degrees * Math.PI / 180.0;
        double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
        double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
        return new Point((int) newX, (int) newY);
    }

    //	public static void openScreenViaItem(int pNum, BookCodeModel pBook, ChapterTemplate pChapterTemplate) {
//		Minecraft mc = Minecraft.getInstance();
//		mc.setScreen(new VascularViewScreen());
//	}
    @Override
    public boolean mouseDragged(double xPos, double yPos, int button, double dragLeftRight, double dragUpDown) {
        this.xDragPos = xPos;
        this.yDragPos = yPos;
        this.dragLeftRight += dragLeftRight / 2.0;
        this.dragUpDown -= dragUpDown / 2.0;
        return super.mouseDragged(xPos, yPos, button, dragLeftRight, dragUpDown);
    }


}