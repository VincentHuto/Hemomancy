package com.huto.hemomancy.capabilities.mindrune;

import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class RuneCap {

    public static class IRuneStorage implements Capability.IStorage<IRune> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<IRune> capability, IRune instance, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability<IRune> capability, IRune instance, Direction side, INBT nbt) {

        }
    }

    public static class IRuneFactory implements Callable<IRune> {

        @Override
        public IRune call() {
            return () -> RuneType.OVERRIDE;
        }
    }
}
