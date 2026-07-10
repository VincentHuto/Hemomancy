package com.vincenthuto.hemomancy.client.screen.dialogue;

public final class DialogueHitRules {
	private DialogueHitRules() {}

	public static boolean intersects(DialogueLayout.Rect target, DialogueLayout.Rect viewport) {
		return target.right() > viewport.x() && target.x() < viewport.right()
				&& target.bottom() > viewport.y() && target.y() < viewport.bottom();
	}
}
