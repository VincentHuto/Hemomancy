package com.vincenthuto.hemomancy.compat.jei;

public class ScreenArea {

	public static boolean pointInBounds(double px, double py, double x, double y, double width, double height) {
		boolean top = px >= x;
		boolean left = py >= y;
		boolean right = px <= x + width;
		boolean bottom = py <= y + height;

		return top && left && bottom && right;
	}
	public int x;
	public int y;
	public int width;

	public int height;

	public ScreenArea(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean contains(double mouseX, double mouseY) {
		return pointInBounds(mouseX, mouseY, x, y, width, height);
	}

}