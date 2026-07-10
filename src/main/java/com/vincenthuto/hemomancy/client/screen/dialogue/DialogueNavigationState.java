package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;

public final class DialogueNavigationState {
	public enum View { HUB, CATEGORY, NODE }

	private View view;
	private DialogueCategory category;
	private String nodeId;
	private int focusIndex;

	private DialogueNavigationState(View view) {
		this.view = view;
	}

	public static DialogueNavigationState hub() {
		return new DialogueNavigationState(View.HUB);
	}

	public static DialogueNavigationState focused(String nodeId) {
		DialogueNavigationState state = new DialogueNavigationState(View.NODE);
		state.nodeId = nodeId;
		return state;
	}

	public void openCategory(DialogueCategory category) {
		this.category = category;
		this.nodeId = null;
		this.view = View.CATEGORY;
		this.focusIndex = 0;
	}

	public void openNode(String nodeId) {
		this.nodeId = nodeId;
		this.view = View.NODE;
		this.focusIndex = 0;
	}

	public boolean back() {
		if (view == View.NODE && category != null) {
			view = View.CATEGORY;
			nodeId = null;
			focusIndex = 0;
			return true;
		}
		if (view == View.CATEGORY) {
			view = View.HUB;
			category = null;
			focusIndex = 0;
			return true;
		}
		return false;
	}

	public void toHub() {
		view = View.HUB;
		category = null;
		nodeId = null;
		focusIndex = 0;
	}

	public void moveFocus(int delta, int count) {
		if (count <= 0) {
			focusIndex = 0;
			return;
		}
		focusIndex = Math.floorMod(focusIndex + delta, count);
	}

	public View view() { return view; }
	public DialogueCategory category() { return category; }
	public String nodeId() { return nodeId; }
	public int focusIndex() { return focusIndex; }
	public void setFocusIndex(int index) { focusIndex = Math.max(0, index); }
}
