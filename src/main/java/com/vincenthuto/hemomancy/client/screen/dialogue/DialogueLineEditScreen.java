package com.vincenthuto.hemomancy.client.screen.dialogue;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.util.List;

/** Temporary dev-only playtest screen: pick a line the dialogue screen showed and rewrite it. */
public class DialogueLineEditScreen extends Screen {
	private static final int ROW = 22;
	private static final int EDITOR_HEIGHT = 90;

	private final Screen parent;
	private final List<String> keys;
	private String selected;
	private String draft = "";
	private int scroll;
	private MultiLineEditBox editor;
	private Component status = Component.empty();

	public DialogueLineEditScreen(Screen parent, List<String> keys) {
		super(Component.translatable("hemomancy.dialogue.editor.title"));
		this.parent = parent;
		this.keys = keys;
	}

	@Override
	protected void init() {
		int w = width - 40;
		editor = new MultiLineEditBox(font, 20, height - EDITOR_HEIGHT - 34, w, EDITOR_HEIGHT,
				Component.translatable("hemomancy.dialogue.editor.pick"), Component.empty());
		editor.setCharacterLimit(Integer.MAX_VALUE);
		editor.setValue(draft);
		editor.setValueListener(value -> draft = value);
		addRenderableWidget(editor);
		addRenderableWidget(Button.builder(Component.translatable("hemomancy.dialogue.editor.save"), b -> save())
				.bounds(20, height - 28, 100, 20).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
				.bounds(126, height - 28, 100, 20).build());
	}

	private int listTop() {
		return 24;
	}

	private int listBottom() {
		return height - EDITOR_HEIGHT - 40;
	}

	private int visibleRows() {
		return Math.max(1, (listBottom() - listTop()) / ROW);
	}

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		super.render(gfx, mouseX, mouseY, partialTick);
		gfx.drawString(font, title, 20, 8, 0xFFFFFF);
		int rows = visibleRows();
		for (int i = 0; i < rows && scroll + i < keys.size(); i++) {
			String key = keys.get(scroll + i);
			int y = listTop() + i * ROW;
			boolean hot = mouseX >= 20 && mouseX < width - 20 && mouseY >= y && mouseY < y + ROW - 2;
			int bg = key.equals(selected) ? 0xC0602020 : hot ? 0x80404040 : 0x60000000;
			gfx.fill(20, y, width - 20, y + ROW - 2, bg);
			gfx.drawString(font, key, 24, y + 2, 0xAAAAAA, false);
			String text = DialogueLineEditor.current(key).replace('\n', ' ');
			gfx.drawString(font, font.plainSubstrByWidth(text, width - 52), 24, y + 11, 0xFFFFFF, false);
		}
		gfx.drawString(font, status, 232, height - 22, 0xFFFF80);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseX >= 20 && mouseX < width - 20 && mouseY >= listTop() && mouseY < listBottom()) {
			int index = scroll + (int) ((mouseY - listTop()) / ROW);
			if (index < keys.size()) {
				selected = keys.get(index);
				draft = DialogueLineEditor.current(selected);
				editor.setValue(draft);
				setFocused(editor);
				status = Component.empty();
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (mouseY < listBottom()) {
			scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0, Math.max(0, keys.size() - visibleRows()));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private void save() {
		if (selected == null) return;
		try {
			DialogueLineEditor.save(selected, draft);
			status = Component.translatable("hemomancy.dialogue.editor.saved", selected);
		} catch (IOException | RuntimeException e) {
			status = Component.translatable("hemomancy.dialogue.editor.failed", String.valueOf(e.getMessage()));
		}
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}
}
