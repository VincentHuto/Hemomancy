package com.vincenthuto.hemomancy.client.screen.codex;

import static org.lwjgl.opengl.GL11C.GL_SCISSOR_TEST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.codex.objects.ChapterObject;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.data.skilltree.SkillTreeCodeModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class HemoProgressionScreen extends AbstractProgressionCodexScreen {

	public static HemoProgressionScreen screen;

	public static final List<ProgressionEntry> ENTRIES = new ArrayList<>();

	public static final ResourceLocation FRAME_TEXTURE = Hemomancy.rloc("textures/gui/book/frame.png");
	public static final ResourceLocation FADE_TEXTURE = Hemomancy.rloc("textures/gui/book/fade.png");
	public static final ResourceLocation BACKGROUND_TEXTURE = Hemomancy.rloc("textures/gui/book/background.png");
	
	protected HemoProgressionScreen(SkillTreeCodeModel model) {
		super(model, 1024, 2560);
		minecraft = Minecraft.getInstance();
		//setupEntries();
//		setupObjects();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		int guiLeft = (width - bookWidth) / 2;
		int guiTop = (height - bookHeight) / 2;

		renderBackground(guiGraphics.pose(), 0.1f, 0.4f);

		GL11.glEnable(GL_SCISSOR_TEST);
		cut();

		renderEntries(guiGraphics, mouseX, mouseY, partialTicks);
		GL11.glDisable(GL_SCISSOR_TEST);

		HemoCodexHelper.renderTransparentTexture(FADE_TEXTURE, guiGraphics.pose(), guiLeft, guiTop, 1, 1, bookWidth,
				bookHeight, 512, 512);
		HemoCodexHelper.renderTexture(FRAME_TEXTURE, guiGraphics.pose(), guiLeft, guiTop, 1, 1, bookWidth, bookHeight,
				512, 512);

		for (int i = 0; i < screen.chapterObjects.size(); i++) {
			if (screen.chapterObjects.get(i) instanceof ChapterObject c) {
				boolean isHovering = c.isHovering(this, guiLeft - 250, guiTop - 105 + (i * 32), mouseX, mouseY);
				c.setHovering(isHovering);
				c.setHover(isHovering ? Math.min(c.getHover() + 1, c.hoverCap()) : Math.max(c.getHover() - 1, 0));
				c.render(minecraft, guiGraphics, guiLeft - 250, guiTop - 105 + (i * 32), mouseX, mouseY, partialTicks);
			}
		}
		guiGraphics.drawCenteredString(font, Component.literal(screen.chapter + ""), guiLeft + 12, guiTop + 6,
				ParticleColor.BLUE.getColor());

		lateEntryRender(guiGraphics, mouseX, mouseY, partialTicks);
	}

	public void renderBackground(PoseStack poseStack, float xModifier, float yModifier) {
		int insideLeft = getInsideLeft();
		int insideTop = getInsideTop();
		float uOffset = (bookInsideWidth / 4f - xOffset * xModifier);
		float vOffset = (backgroundImageHeight - bookInsideHeight - yOffset * yModifier);
		if (uOffset <= 0) {
			uOffset = 0;
		}
		if (uOffset > bookInsideWidth / 2f) {
			uOffset = bookInsideWidth / 2f;
		}
		if (vOffset <= backgroundImageHeight / 2f) {
			vOffset = backgroundImageHeight / 2f;
		}
		if (vOffset > backgroundImageHeight - bookInsideHeight) {
			vOffset = backgroundImageHeight - bookInsideHeight;
		}
		HemoCodexHelper.renderTexture(BACKGROUND_TEXTURE, poseStack, insideLeft, insideTop, uOffset, vOffset,
				bookInsideWidth, bookInsideHeight, backgroundImageWidth / 2, backgroundImageHeight / 2);
	}

	@Override
	public Collection<ProgressionEntry> getEntries() {
		return ENTRIES;
	}

	@Override
	public Supplier<SoundEvent> getSweetenerSound() {
		return () -> SoundEvents.BOOK_PUT;
	}

	@Override
	public void onClose() {
		super.onClose();
		screen.playSweetenedSound(() -> SoundEvents.BOOK_PUT, 0.25f);
	}

	@Override
	public void openScreen(SkillTreeCodeModel model,boolean ignoreNextMouseClick) {
		if (screen == null) {
			screen = new HemoProgressionScreen(model);
		}
		screen = new HemoProgressionScreen(model);
		Minecraft.getInstance().setScreen(screen);
		
		// ScreenParticleHandler.clearParticles();
		this.ignoreNextMouseInput = ignoreNextMouseClick;
	}

	public static HemoProgressionScreen getScreenInstance(SkillTreeCodeModel model) {
		if (screen == null) {
			screen = new HemoProgressionScreen(model);
		}
		return screen;
	}

	public static void openCodexViaItem(SkillTreeCodeModel model) {
		getScreenInstance(model).openScreen(model, true);
		screen.playSweetenedSound(() -> SoundEvents.BOOK_PUT, 1.25f);
	}

//	public static void setupEntries() {
//		ENTRIES.clear();
//		Item EMPTY = ItemStack.EMPTY.getItem();
//
//		//Chapters
//		ENTRIES.add(new ChapterEntry("introduction", 0, ItemInit.charm_of_vascularium.get(), 0, 0));
//		ENTRIES.add(new ChapterEntry("notintor", 1, ItemInit.sanguine_formation.get(), 0, 0));
//		
//		//Chapter 1
////		ENTRIES.add(new BookEntry("spirit_crystals", 0, "", 0, 1).setObjectSupplier((s, i, c, p, e, x,
////				y) -> new IconObject(s, i, c, p, e, Hemomancy.rloc("textures/gui/vein_teleport.png"), x, y)));
//
//		ENTRIES.add(new BookEntry("introduction", 0, "", ItemInit.barbed_blade.get(), 0, 2)
//				.setObjectSupplier(ImportantEntryObject::new));
//
//		ENTRIES.add(new BookEntry("natural_quartz", 0, "", BlockInit.blood_crystal.get().asItem(), 2, 1)
//				.setObjectSupplier(MinorEntryObject::new));
//
//		ENTRIES.add(new BookEntry("natural_quartz2", 0, "", BlockInit.polished_venous_stone_brick_stairs.get().asItem(),
//				2, 2).setObjectSupplier(MinorEntryObject::new));
//
//		ENTRIES.add(new BookEntry("natural_quartz3", 0, "", BlockInit.befouling_ash_trail.get().asItem(), 3, 3)
//				.setObjectSupplier(MinorEntryObject::new));
//
//		ENTRIES.add(new BookEntry("natural_quartz4", 0, "", BlockInit.runic_chisel_station.get().asItem(), -2, 3)
//				.setObjectSupplier(MinorEntryObject::new));
//
//		//Chapter 2
//		ENTRIES.add(new BookEntry("natural_auartz", 1, "", ItemInit.memory_activation_potential.get().asItem(), 2, 1)
//				.setObjectSupplier(MinorEntryObject::new));
//
//		ENTRIES.add(
//				new BookEntry("naturalauartz2", 1, "natural_auartz", ItemInit.memory_blood_rush.get().asItem(), 2, 2)
//						.setObjectSupplier(MinorEntryObject::new));
//
//		ENTRIES.add(
//				new BookEntry("natural_auartz3", 1, "naturalauartz2", ItemInit.memory_blood_needle.get().asItem(), 2, 3)
//						.setObjectSupplier(MinorEntryObject::new));
//
//		ENTRIES.add(new BookEntry("natural_auartz4", 1, "natural_auartz3",
//				ItemInit.memory_blood_absorption.get().asItem(), -2, 4).setObjectSupplier(MinorEntryObject::new));
//
//	}
}
