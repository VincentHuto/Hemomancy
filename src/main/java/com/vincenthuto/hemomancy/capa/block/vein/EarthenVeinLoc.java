package com.vincenthuto.hemomancy.capa.block.vein;

import com.vincenthuto.hemomancy.util.VeinLocation;

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