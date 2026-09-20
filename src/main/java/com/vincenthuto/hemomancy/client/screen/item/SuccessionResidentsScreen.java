package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.succession.ResidentsRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;

public final class SuccessionResidentsScreen extends Screen {
    private final Screen parent;
    private final com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer veins = new com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer();
    private CompoundTag data = new CompoundTag();
    private int page, left, top, panelWidth, rows;
    public SuccessionResidentsScreen(Screen parent) { super(Component.translatable("hemomancy.succession.residents")); this.parent = parent; }
    public static void open(Screen parent) {
        Minecraft.getInstance().setScreen(new SuccessionResidentsScreen(parent));
        PacketHandler.sendToServer(new ResidentsRequestPacket(ResidentsRequestPacket.LIST, false));
    }
    public static void update(CompoundTag data) {
        if (data != null && Minecraft.getInstance().screen instanceof SuccessionResidentsScreen screen) { screen.data = data; screen.rebuildWidgets(); }
    }
    @Override protected void init() {
        panelWidth = Math.min(440, width - 16); rows = Math.max(1, Math.min(5, (height - 106) / 62));
        left = (width - panelWidth) / 2; top = (height - (rows * 62 + 92)) / 2;
        var people = data.getList("Residents", Tag.TAG_COMPOUND);
        page = Math.min(page, Math.max(0, (people.size()-1) / rows));
        for (int i = 0; i < rows && page*rows+i < people.size(); i++) {
            var person = people.getCompound(page*rows+i); int y = top + 40 + i*62;
            if (!person.getBoolean("Alive") && !person.getBoolean("Dismissed")) {
                var recover = Button.builder(Component.translatable("hemomancy.succession.recover"), button ->
                        PacketHandler.sendToServer(new ResidentsRequestPacket(person.getUUID("Id"), false)))
                        .bounds(left + panelWidth - 92, y, 82, 20).build();
                recover.active = data.getBoolean("AtFane") && !person.getBoolean("Locked"); addRenderableWidget(recover);
            } else if (data.getBoolean("Leader") && !person.getBoolean("Dismissed")) {
                addRenderableWidget(Button.builder(Component.translatable("hemomancy.succession.dismiss"), button ->
                        minecraft.setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(confirmed -> {
                            minecraft.setScreen(this);
                            if (confirmed) PacketHandler.sendToServer(new ResidentsRequestPacket(person.getUUID("Id"), true));
                        }, Component.translatable("hemomancy.succession.dismiss"), Component.translatable("hemomancy.succession.dismiss_confirm", person.getString("Name")))))
                        .bounds(left + panelWidth - 92, y, 82, 20).build());
            }
        }
        int bottom = top + 42 + rows*62;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> { page = Math.max(0, page-1); rebuildWidgets(); }).bounds(left+8, bottom, 25, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> { if ((page+1)*rows < people.size()) page++; rebuildWidgets(); }).bounds(left+38, bottom, 25, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> onClose()).bounds(left+panelWidth-80, bottom, 72, 20).build());
    }
    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x88000000);
        graphics.fill(left, top, left+panelWidth, top+rows*62+88, 0xF019080C);
        veins.render(graphics, left, top, panelWidth, rows*62+88);
        graphics.renderOutline(left, top, panelWidth, rows*62+88, 0xFF90323C);
        graphics.drawString(font, title, left+12, top+12, 0xFFE2B8AF);
        var people = data.getList("Residents", Tag.TAG_COMPOUND);
        for (int i = 0; i < rows && page*rows+i < people.size(); i++) {
            var r = people.getCompound(page*rows+i); int y = top+40+i*62;
            String status = r.getBoolean("Dismissed") ? "dismissed" : !r.getBoolean("Alive") ? "dead"
                    : r.getBoolean("Dormant") ? "dormant" : r.getBoolean("Displaced") ? "displaced" : "resident";
            graphics.drawString(font, font.plainSubstrByWidth(r.getString("Name"), panelWidth-120), left+12, y, 0xFFFFDDC8);
            graphics.drawString(font, Component.translatable("hemomancy.succession.profession."+r.getString("Profession"))
                    .append(" / ").append(Component.translatable("hemomancy.succession.status."+status)), left+12, y+12, 0xFFC8938D);
            graphics.drawString(font, font.plainSubstrByWidth(r.getString("DonorName")+" → "+r.getString("OfficiantName"), panelWidth-24), left+12, y+25, 0xFFB1A09A);
            graphics.drawString(font, font.plainSubstrByWidth(r.getString("Dimension")+" "+BlockPos.of(r.getLong("Workplace")).toShortString(), panelWidth-24), left+12, y+37, 0xFF8E7B79);
        }
        if (people.isEmpty()) graphics.drawString(font, Component.translatable("hemomancy.succession.no_residents"), left+12, top+42, 0xFFB1A09A);
        graphics.drawString(font, Component.translatable("hemomancy.succession.recovery_hint"), left+12, top+rows*62+70, 0xFFB1A09A);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
