package com.vincenthuto.hemomancy.common.capability.player.harbinger.summon;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;

import java.util.List;

public interface IKnownSummons {
	List<String> getKnownSummonNames();

	void setKnownSummonNames(List<String> names);

	boolean isKnown(PuppeteerSummonDefinition definition);

	boolean learn(PuppeteerSummonDefinition definition);
}
