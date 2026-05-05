package com.vincenthuto.hemomancy.common.capability.player.unstained.stillart;

import java.util.List;

import com.vincenthuto.hemomancy.common.unstained.stillarts.StillArt;

public interface IKnownStillArts {
	List<String> getKnownArtNames();

	void setKnownArtNames(List<String> names);

	List<StillArt> getKnownArts();

	boolean isKnown(StillArt art);

	boolean learnArt(StillArt art);

	StillArt getSelectedArt();

	void setSelectedArt(StillArt art);
}
