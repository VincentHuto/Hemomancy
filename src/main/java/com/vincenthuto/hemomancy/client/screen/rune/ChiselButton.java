package com.vincenthuto.hemomancy.client.screen.rune;

import com.vincenthuto.hutoslib.client.screen.HLButtonTextured;

import net.minecraft.resources.ResourceLocation;

public class ChiselButton extends HLButtonTextured {

	int i, j;

	public ChiselButton(ResourceLocation texIn, int idIn, int i, int j, int posXIn, int posYIn, int buttonWidthIn,
			int buttonHeightIn, int uIn, int vIn, boolean stateIn, OnPress actionIn) {
		super(texIn, idIn, posXIn, posYIn, buttonWidthIn, buttonHeightIn, uIn, vIn, stateIn, actionIn);
		this.i = i;
		this.j = j;

	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public int getJ() {
		return j;
	}

	public void setJ(int j) {
		this.j = j;
	}

}