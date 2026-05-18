package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ItemStackBloodVolume implements IBloodVolume {
	private static final String TAG_ACTIVE = "GourdBloodActive";
	private static final String TAG_VOLUME = "GourdBloodVolume";
	private static final String TAG_MAX = "GourdBloodMax";
	private static final String TAG_BLOODLINE = "GourdBloodline";

	private final ItemStack stack;
	private final double defaultMaxVolume;
	private double bloodDebt;
	private boolean trickleEnabled;
	private double trickleRate = 0.5;
	private boolean autoDrawEnabled;
	private double autoDrawThreshold = 0.25;
	private boolean bloodRoutingOptInEnabled;

	public ItemStackBloodVolume(ItemStack stack, double defaultMaxVolume) {
		this.stack = stack;
		this.defaultMaxVolume = defaultMaxVolume;
		if (!tag().contains(TAG_MAX)) {
			setMaxBloodVolume(defaultMaxVolume);
		}
	}

	private CompoundTag tag() {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	private void save(CompoundTag tag) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	@Override
	public void addBloodVolume(double points) {
		setBloodVolume(getBloodVolume() + points);
	}

	@Override
	public void addMaxBloodVolume(double points) {
		setMaxBloodVolume(getMaxBloodVolume() + points);
	}

	@Override
	public boolean drain(double points) {
		if (!wouldOverstrain(points)) {
			setBloodVolume(getBloodVolume() - points);
			return true;
		}
		setBloodVolume(0);
		return false;
	}

	@Override
	public boolean drainFromSource(IBloodVolume src, double points) {
		if (src.fill(points)) {
			drain(points);
			return true;
		}
		return false;
	}

	@Override
	public boolean fill(double points) {
		if (!wouldOverflow(points)) {
			setBloodVolume(getBloodVolume() + points);
			return true;
		}
		setBloodVolume(getMaxBloodVolume());
		return false;
	}

	@Override
	public boolean fillFromSource(IBloodVolume src, double points) {
		if (src.drain(points) && src.getBloodVolume() > points) {
			fill(points);
			return true;
		}
		return false;
	}

	@Override
	public Bloodline getBloodLine() {
		CompoundTag data = tag();
		return data.contains(TAG_BLOODLINE) ? Bloodline.deserialize(data.getCompound(TAG_BLOODLINE)) : Bloodline.NOBLOODLINE;
	}

	@Override
	public double getBloodVolume() {
		return tag().getDouble(TAG_VOLUME);
	}

	@Override
	public double getMaxBloodVolume() {
		CompoundTag data = tag();
		return data.contains(TAG_MAX) ? data.getDouble(TAG_MAX) : defaultMaxVolume;
	}

	@Override
	public boolean isActive() {
		return tag().getBoolean(TAG_ACTIVE);
	}

	@Override
	public boolean isEmpty() {
		return getBloodVolume() <= 0;
	}

	@Override
	public boolean isFull() {
		return getBloodVolume() >= getMaxBloodVolume();
	}

	@Override
	public void setActive(boolean set) {
		CompoundTag data = tag();
		data.putBoolean(TAG_ACTIVE, set);
		save(data);
	}

	@Override
	public void setBloodLine(Bloodline bloodLine) {
		CompoundTag data = tag();
		data.put(TAG_BLOODLINE, bloodLine.serialize());
		save(data);
	}

	@Override
	public void setBloodVolume(double points) {
		CompoundTag data = tag();
		data.putDouble(TAG_VOLUME, Math.max(0, Math.min(points, getMaxBloodVolume())));
		save(data);
	}

	@Override
	public void setMaxBloodVolume(double points) {
		CompoundTag data = tag();
		data.putDouble(TAG_MAX, points);
		if (data.getDouble(TAG_VOLUME) > points) {
			data.putDouble(TAG_VOLUME, points);
		}
		save(data);
	}

	@Override
	public void subtractBloodVolume(double points) {
		setBloodVolume(getBloodVolume() - points);
	}

	@Override
	public void subtractMaxBloodVolume(double points) {
		setMaxBloodVolume(getMaxBloodVolume() - points);
	}

	@Override
	public void toggleActive() {
		setActive(!isActive());
	}

	@Override
	public boolean wouldOverflow(double points) {
		return getBloodVolume() + points > getMaxBloodVolume();
	}

	@Override
	public boolean wouldOverstrain(double points) {
		return getBloodVolume() - points < 0;
	}

	@Override
	public void addDamage(double amount) {
		this.bloodDebt += amount;
	}

	@Override
	public void addBloodSpend(double amount) {
		this.bloodDebt += amount;
	}

	@Override
	public double consumeDebt() {
		double debt = this.bloodDebt;
		this.bloodDebt = 0;
		return debt;
	}

	@Override
	public double getBloodDebt() {
		return this.bloodDebt;
	}

	@Override
	public void resetBloodDebt() {
		this.bloodDebt = 0;
	}

	@Override
	public boolean isTrickleEnabled() {
		return trickleEnabled;
	}

	@Override
	public void setTrickleEnabled(boolean enabled) {
		this.trickleEnabled = enabled;
	}

	@Override
	public double getTrickleRate() {
		return trickleRate;
	}

	@Override
	public void setTrickleRate(double rate) {
		this.trickleRate = rate;
	}

	@Override
	public boolean isAutoDrawEnabled() {
		return autoDrawEnabled;
	}

	@Override
	public void setAutoDrawEnabled(boolean enabled) {
		this.autoDrawEnabled = enabled;
	}

	@Override
	public double getAutoDrawThreshold() {
		return autoDrawThreshold;
	}

	@Override
	public void setAutoDrawThreshold(double threshold) {
		this.autoDrawThreshold = threshold;
	}

	@Override
	public boolean isBloodRoutingOptInEnabled() {
		return bloodRoutingOptInEnabled;
	}

	@Override
	public void setBloodRoutingOptInEnabled(boolean enabled) {
		this.bloodRoutingOptInEnabled = enabled;
	}
}
