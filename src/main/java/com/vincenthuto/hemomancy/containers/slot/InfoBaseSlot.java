/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 */
package com.vincenthuto.hemomancy.containers.slot;

import com.vincenthuto.hemomancy.gui.radial.InvalidTypeException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public abstract class InfoBaseSlot<V> {
    public static class BooleanDataEntry
    extends InfoBaseSlot<Boolean> {
        private boolean value;

        public BooleanDataEntry(String key, boolean value) {
            super(key);
            this.value = value;
        }

        @Override
        public boolean asBoolean() {
            return this.value;
        }

        @Override
        public Boolean asObject() {
            return this.asBoolean();
        }

        @Override
        public CompoundTag getTag() {
            if (this.tag == null) {
                this.tag = this.getBaseTag();
                this.tag.putBoolean("value", this.value);
            }
            return this.tag;
        }

        @Override
        public Class<?> getType() {
            return Boolean.class;
        }
    }
    public static class FloatDataEntry
    extends InfoBaseSlot<Float> {
        private float value;

        public FloatDataEntry(String key, float value) {
            super(key);
            this.value = value;
        }

        @Override
        public float asFloat() {
            return this.value;
        }

        @Override
        public Float asObject() {
            return Float.valueOf(this.asFloat());
        }

        @Override
        public CompoundTag getTag() {
            if (this.tag == null) {
                this.tag = this.getBaseTag();
                this.tag.putFloat("value", this.value);
            }
            return this.tag;
        }

        @Override
        public Class<?> getType() {
            return Float.class;
        }
    }

    public static class IntegerDataEntry
    extends InfoBaseSlot<Integer> {
        private int value;

        public IntegerDataEntry(String key, int value) {
            super(key);
            this.value = value;
        }

        @Override
        public int asInt() {
            return this.value;
        }

        @Override
        public Integer asObject() {
            return this.asInt();
        }

        @Override
        public CompoundTag getTag() {
            if (this.tag == null) {
                this.tag = this.getBaseTag();
                this.tag.putInt("value", this.value);
            }
            return this.tag;
        }

        @Override
        public Class<?> getType() {
            return Integer.class;
        }
    }

    public static class StringDataEntry
    extends InfoBaseSlot<String> {
        private String value;

        public StringDataEntry(String key, String value) {
            super(key);
            this.value = value;
        }

        @Override
        public String asObject() {
            return this.asString();
        }

        @Override
        public String asString() {
            return this.value;
        }

        @Override
        public CompoundTag getTag() {
            if (this.tag == null) {
                this.tag = this.getBaseTag();
                this.tag.putString("value", this.value);
            }
            return this.tag;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }
    }

    public static InfoBaseSlot<?> fromNBT(CompoundTag tag) {
        String key = tag.getString("key");
        Tag value = tag.get("value");
        byte id = value.getId();
        if (id == 8) {
            return new StringDataEntry(key, tag.getString("value"));
        }
        if (id == 3) {
            return new IntegerDataEntry(key, tag.getInt("value"));
        }
        if (id == 5) {
            return new FloatDataEntry(key, tag.getFloat("value"));
        }
        if (id == 1) {
            return new BooleanDataEntry(key, tag.getBoolean("value"));
        }
        throw new RuntimeException("Invalid NBT tag stored in data entry: " + tag);
    }

    protected final String key;

    protected CompoundTag tag;

    public InfoBaseSlot(String key) {
        this.key = key;
    }

    public boolean asBoolean() {
        throw new InvalidTypeException(this.getKey() + " cannot be converted into a boolean.");
    }

    public float asFloat() {
        throw new InvalidTypeException(this.getKey() + " cannot be converted into a float.");
    }

    public int asInt() {
        throw new InvalidTypeException(this.getKey() + " cannot be converted into an integer.");
    }

    public abstract V asObject();

    public String asString() {
        throw new InvalidTypeException(this.getKey() + " cannot be converted into a string.");
    }

    protected CompoundTag getBaseTag() {
        if (this.tag == null) {
            this.tag = new CompoundTag();
            this.tag.putString("key", this.key);
        }
        return this.tag;
    }

    public String getKey() {
        return this.key;
    }

    public abstract CompoundTag getTag();

    public abstract Class<?> getType();
}

