package com.vincenthuto.hemomancy.common.capability.block.vein;

public class EarthenVeinLoc implements IEarthenVeinLoc {

	public VeinLocation veinLoc = VeinLocation.BLANK;

	@Override
	public VeinLocation getVeinLocation() {
		return veinLoc;
	}

	@Override
	public void setVeinLoc(VeinLocation loc) {
		this.veinLoc = loc;

	}

}