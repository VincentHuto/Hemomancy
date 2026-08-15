package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.function.Function;

public final class ProgressTabRegistry<K extends Enum<K>> {
	private final EnumMap<K, IProgressTab> tabs;
	private final EnumSet<K> initialized;
	private final Function<K, IProgressTab> factory;

	public ProgressTabRegistry(Class<K> keyType, Function<K, IProgressTab> factory) {
		this.tabs = new EnumMap<>(keyType);
		this.initialized = EnumSet.noneOf(keyType);
		this.factory = factory;
	}

	public IProgressTab get(K key) {
		return tabs.computeIfAbsent(key, factory);
	}

	public IProgressTab activate(K key, ProgressScreenContext context) {
		IProgressTab tab = get(key);
		if (initialized.add(key)) tab.onInit(context);
		return tab;
	}

	public boolean isInstantiated(K key) {
		return tabs.containsKey(key);
	}

	public void invalidateInitializations() {
		initialized.clear();
	}

	public void close() {
		for (IProgressTab tab : tabs.values()) tab.onClose();
	}
}
