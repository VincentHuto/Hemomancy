package com.huto.hemomancy.capa.rune;

import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;

public class RuneCap {

	public static class IRuneStorage implements Capability.IStorage<IRune> {

		@Nullable
		@Override
		public Tag writeNBT(Capability<IRune> capability, IRune instance, Direction side) {
			return null;
		}

		@Override
		public void readNBT(Capability<IRune> capability, IRune instance, Direction side, Tag nbt) {

		}
	}

	public static class IRuneFactory implements Callable<IRune> {

		@Override
		public IRune call() {
			return () -> RuneType.OVERRIDE;
		}
	}
}
