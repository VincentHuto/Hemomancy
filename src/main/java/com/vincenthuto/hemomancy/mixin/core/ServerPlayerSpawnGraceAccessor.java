package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerSpawnGraceAccessor {
	@Accessor("spawnInvulnerableTime")
	int hemomancy$getSpawnInvulnerableTime();

	@Accessor("spawnInvulnerableTime")
	void hemomancy$setSpawnInvulnerableTime(int ticks);
}
