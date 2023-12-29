package com.vincenthuto.hemomancy.client.screen.codex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.vincenthuto.hemomancy.client.screen.codex.objects.BookObject;
import com.vincenthuto.hemomancy.client.screen.codex.objects.ChapterObject;
import com.vincenthuto.hemomancy.client.screen.codex.objects.EntryObject;
import com.vincenthuto.hemomancy.client.screen.codex.objects.ProgressionObject;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class AbstractProgressionCodexScreen extends AbstractHemoScreen {

	public float xOffset;
	public float yOffset;
	public float cachedXOffset;
	public float cachedYOffset;
	public int chapter;
	public int cachedChapter;
	public boolean ignoreNextMouseInput;

	public final List<ProgressionObject> bookObjects = new ArrayList<>();
	public final List<ProgressionObject> chapterObjects = new ArrayList<>();

	public final int bookWidth;
	public final int bookHeight;
	public final int bookInsideWidth;
	public final int bookInsideHeight;

	public final int backgroundImageWidth;
	public final int backgroundImageHeight;

	protected AbstractProgressionCodexScreen(int backgroundImageWidth, int backgroundImageHeight) {
		this(378, 250, 344, 218, backgroundImageWidth, backgroundImageHeight);
	}

	protected AbstractProgressionCodexScreen(int bookWidth, int bookHeight, int bookInsideWidth, int bookInsideHeight,
			int backgroundImageWidth, int backgroundImageHeight) {
		super(Component.translatable("hemo.gui.book.title"));
		this.bookWidth = bookWidth;
		this.bookHeight = bookHeight;
		this.bookInsideWidth = bookInsideWidth;
		this.bookInsideHeight = bookInsideHeight;
		this.backgroundImageWidth = backgroundImageWidth;
		this.backgroundImageHeight = backgroundImageHeight;
		minecraft = Minecraft.getInstance();
	}

	@Override
	public void init() {

	}

	public void setupObjects() {
		chapterObjects.clear();
		bookObjects.clear();
		this.width = minecraft.getWindow().getGuiScaledWidth();
		this.height = minecraft.getWindow().getGuiScaledHeight();
		int guiLeft = getGuiLeft();
		int guiTop = getGuiTop();
		int coreX = guiLeft + bookInsideWidth;
		int coreY = guiTop + bookInsideHeight;
		int width = 40;
		int height = 48;

		for (ProgressionEntry entry : getEntries()) {
			// System.out.println(entry.getChapter());
			if (entry instanceof ChapterEntry c) {
				chapterObjects.add(entry.getObjectSupplier().getEntryObject(this, entry.getIdentifier(),
						entry.getChapter(), entry.getParentId(), entry, coreX + entry.getXOffset() * width,
						coreY - entry.getYOffset() * height));
			} else {
				BookObject b = (BookObject) entry.getObjectSupplier().getEntryObject(this, entry.getIdentifier(),
						entry.getChapter(), entry.getParentId(), entry, coreX + entry.getXOffset() * width,
						coreY - entry.getYOffset() * height);
				b.setChapter(entry.getChapter());
				b.setIdentifier(entry.getIdentifier());
				bookObjects.add(b);
			}

		}

		faceObject(bookObjects.get(0));
	}

	public abstract void openScreen(boolean ignoreNextMouseClick);

	public abstract Collection<ProgressionEntry> getEntries();

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		xOffset += dragX;
		yOffset += dragY;
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		cachedXOffset = xOffset;
		cachedYOffset = yOffset;
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (ignoreNextMouseInput) {
			ignoreNextMouseInput = false;
			return super.mouseReleased(mouseX, mouseY, button);
		}
		if (xOffset != cachedXOffset || yOffset != cachedYOffset) {
			return super.mouseReleased(mouseX, mouseY, button);
		}
		for (ProgressionObject object : bookObjects) {
			if (object instanceof ChapterObject c) {

			} else {
				if (object.isHovering(this, xOffset, yOffset, mouseX, mouseY)) {
					object.click(xOffset, yOffset, mouseX, mouseY);
					break;
				}
			}
		}

		for (int i = 0; i < chapterObjects.size(); i++) {
			if (chapterObjects.get(i) instanceof ChapterObject c) {
				if (c.isHovering(this, getGuiLeft() - 250, getGuiTop() - 105 + (i * 32), mouseX, mouseY)) {
					c.click(c.getPosX(), c.getPosY(), mouseX, mouseY);
					break;
				}
			}
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean isHovering(double mouseX, double mouseY, int posX, int posY, int width, int height) {
		/*
		 * if (!isInView(mouseX, mouseY)) { return false; }
		 */
		return HemoCodexHelper.isHovering(mouseX, mouseY, posX, posY, width, height);
	}

	public void faceObject(ProgressionObject progressionObject) {
		this.width = minecraft.getWindow().getGuiScaledWidth();
		this.height = minecraft.getWindow().getGuiScaledHeight();
		xOffset = -progressionObject.getPosX() + getGuiLeft() + bookInsideWidth;
		yOffset = -progressionObject.getPosY() + getGuiTop() + bookInsideHeight;
	}

	public void renderEntries(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		for (int i = bookObjects.size() - 1; i >= 0; i--) {

			if (bookObjects.get(i) instanceof ChapterObject c) {

			} else {

				if (bookObjects.get(i) instanceof EntryObject e) {
					if (e.getChapter() == chapter) {
						if (e.entry.getParentId() != "") {
							for (ProgressionEntry<ProgressionObject> x : getEntries()) {
								if (x.getIdentifier().equals(e.parentId)) {
									BookObject p = null;
									for (ProgressionObject b : bookObjects) {
										if (b.getIdentifier() == x.getIdentifier()) {
											p = (BookObject) b;
										}
									}
									if (p != null) {
										HLGuiUtils.drawLine(guiGraphics.pose(), e.offsetPosX(xOffset)+16,
												e.offsetPosY(yOffset)+16, p.offsetPosX(xOffset)+16, p.offsetPosY(yOffset)+16,
												ParticleColor.YELLOW, 2);
									}

								}
							}
						}

						boolean isHovering = e.isHovering(this, xOffset, yOffset, mouseX, mouseY);
						e.setHovering(isHovering);
						e.setHover(
								isHovering ? Math.min(e.getHover() + 1, e.hoverCap()) : Math.max(e.getHover() - 1, 0));
						e.render(minecraft, guiGraphics, xOffset, yOffset, mouseX, mouseY, partialTicks);
					}
				}

			}

		}
	}

	public void lateEntryRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		for (int i = bookObjects.size() - 1; i >= 0; i--) {
			ProgressionObject object = bookObjects.get(i);
			object.lateRender(minecraft, guiGraphics, xOffset, yOffset, mouseX, mouseY, partialTicks);
		}
		for (int i = chapterObjects.size() - 1; i >= 0; i--) {
			ProgressionObject object = chapterObjects.get(i);
			object.lateRender(minecraft, guiGraphics, xOffset, yOffset, mouseX, mouseY, partialTicks);
		}
	}

	public boolean isInView(double mouseX, double mouseY) {
		return mouseX >= getInsideLeft() && mouseY >= getInsideTop() && mouseX <= (getInsideLeft() + bookInsideWidth)
				&& mouseY <= (getInsideTop() + bookInsideHeight);
	}

	public void cut() {
		int scale = (int) getMinecraft().getWindow().getGuiScale();
		GL11.glScissor(getInsideLeft() * scale,
				getMinecraft().getWindow().getHeight() - (getInsideTop() + bookInsideHeight) * scale,
				bookInsideWidth * scale, bookInsideHeight * scale);
	}

	public int getInsideLeft() {
		return getGuiLeft() + 17;
	}

	public int getInsideTop() {
		return getGuiTop() + 14;
	}

	public int getGuiLeft() {
		return (width - bookWidth) / 2;
	}

	public int getGuiTop() {
		return (height - bookHeight) / 2;
	}

	public int getChapter() {
		return chapter;
	}

	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	public List<ProgressionObject> getBookObjects() {
		return bookObjects;
	}

	public List<ProgressionObject> getChapterObjects() {
		return chapterObjects;
	}
}