package com.vincenthuto.hemomancy.common.entity.boss.saint;

/** A Saint that rises out of its sarcophagus ({@link SaintRise}) before it fights. */
public interface RisingSaint {
	void beginRise();

	/** Ticks into the rise, smooth across frames on the client; negative once it has risen. */
	float riseTime(float partialTick);
}
