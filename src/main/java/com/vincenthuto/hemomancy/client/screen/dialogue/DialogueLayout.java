package com.vincenthuto.hemomancy.client.screen.dialogue;

public record DialogueLayout(Rect panel, Rect header, Rect content, Rect footer, int categoryColumns) {
	public static final int CONTENT_FOOTER_GAP = 6;

	private static final int MARGIN = 8;
	private static final int MAX_WIDTH = 320;
	private static final int HEADER_HEIGHT = 76;
	private static final int FOOTER_HEIGHT = 28;

	public static DialogueLayout calculate(int screenWidth, int screenHeight) {
		int width = Math.max(1, Math.min(MAX_WIDTH, screenWidth - MARGIN * 2));
		int height = Math.max(1, screenHeight - MARGIN * 2);
		int x = MARGIN;
		int y = MARGIN;
		Rect panel = new Rect(x, y, width, height);
		int headerHeight = Math.min(HEADER_HEIGHT, Math.max(48, height / 3));
		int footerHeight = Math.min(FOOTER_HEIGHT, Math.max(18, height / 8));
		Rect header = new Rect(x + 8, y + 8, Math.max(1, width - 16), Math.max(1, headerHeight - 8));
		Rect footer = new Rect(x + 8, y + height - footerHeight, Math.max(1, width - 16), footerHeight - 8);
		Rect content = new Rect(x + 8, y + headerHeight, Math.max(1, width - 16),
				Math.max(1, height - headerHeight - footerHeight - CONTENT_FOOTER_GAP));
		return new DialogueLayout(panel, header, content, footer, width >= 240 ? 2 : 1);
	}

	public record Rect(int x, int y, int width, int height) {
		public int right() { return x + width; }
		public int bottom() { return y + height; }
		public boolean contains(double mouseX, double mouseY) {
			return mouseX >= x && mouseX < right() && mouseY >= y && mouseY < bottom();
		}
	}
}
