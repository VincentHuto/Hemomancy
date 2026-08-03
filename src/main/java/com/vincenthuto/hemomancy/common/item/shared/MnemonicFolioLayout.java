package com.vincenthuto.hemomancy.common.item.shared;

public final class MnemonicFolioLayout {
	public static final int SLOT_COUNT = 30;
	public static final int COLUMNS = 10;
	public static final int ROWS = 3;

	private MnemonicFolioLayout() {
	}

	public static Point folioSlot(int index) {
		if (index < 0 || index >= SLOT_COUNT) throw new IndexOutOfBoundsException(index);
		return new Point(7 + index % COLUMNS * 18, 18 + index / COLUMNS * 18);
	}

	public static Point playerSlot(int index) {
		if (index < 0 || index >= 27) throw new IndexOutOfBoundsException(index);
		return new Point(16 + index % 9 * 18, 90 + index / 9 * 18);
	}

	public static Point hotbarSlot(int index) {
		if (index < 0 || index >= 9) throw new IndexOutOfBoundsException(index);
		return new Point(16 + index * 18, 148);
	}

	public record Point(int x, int y) {
	}
}
