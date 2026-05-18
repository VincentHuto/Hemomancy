package com.vincenthuto.hemomancy.common.capability.player.unstained.stillart;

import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;

import java.util.List;

public interface IKnownStillArts {
	List<String> getKnownArtNames();

	void setKnownArtNames(List<String> names);

	List<StillArt> getKnownArts();

	boolean isKnown(StillArt art);

	boolean learnArt(StillArt art);

	StillArt getSelectedArt();

	void setSelectedArt(StillArt art);
}
