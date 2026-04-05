package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;

/**
 * Defines one node in the manipulation tree displayed on the skill-tree screen.
 * Positions are in content-space pixels (pan/zoom is applied by the screen).
 */
public class ManipulationTreeEntry {

	private final String manipName;
	private final int x, y;
	private final List<String> parentNames;

	/**
	 * @param manipName   Registry name of the {@link BloodManipulation}
	 * @param x           Content-space X coordinate
	 * @param y           Content-space Y coordinate
	 * @param parentNames Names of parent manipulation nodes (empty for roots)
	 */
	public ManipulationTreeEntry(String manipName, int x, int y, List<String> parentNames) {
		this.manipName = manipName;
		this.x = x;
		this.y = y;
		this.parentNames = parentNames;
	}

	public ManipulationTreeEntry(String manipName, int x, int y, String... parents) {
		this(manipName, x, y, List.of(parents));
	}

	public String getManipName() {
		return manipName;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public List<String> getParentNames() {
		return parentNames;
	}

	/**
	 * Resolves the actual {@link BloodManipulation} from the forge registry.
	 * May return null if the registry hasn't been populated yet.
	 */
	@Nullable
	public BloodManipulation resolve() {
		return ManipulationInit.getByName(manipName);
	}
}
