package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.EnumNodeShape;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines one node in the manipulation tree displayed on the skill-tree screen.
 * Positions are in content-space pixels (pan/zoom is applied by the screen).
 */
public class ManipulationTreeEntry {

	private final String manipName;
	private final int x, y;
	private final List<String> parentNames;
	private List<String> softParentNames = List.of();
	private EnumNodeShape nodeShape = EnumNodeShape.SQUARE;

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

	public List<String> getSoftParentNames() {
		return softParentNames;
	}

	public List<String> getConnectionParentNames() {
		return parentNames;
	}

	public List<String> getRequirementParentNames() {
		if (softParentNames.isEmpty()) {
			return parentNames;
		}
		List<String> parents = new ArrayList<>(parentNames);
		parents.addAll(softParentNames);
		return parents;
	}

	/** Builder-style setter for non-lineage dependency links that should not draw tree edges. */
	public ManipulationTreeEntry setSoftParents(String... parents) {
		this.softParentNames = List.of(parents);
		return this;
	}

	/** Builder-style setter for the node shape on the skill tree. */
	public ManipulationTreeEntry setNodeShape(EnumNodeShape shape) {
		this.nodeShape = shape;
		return this;
	}

	/** Returns the node shape for this manipulation entry. Defaults to SQUARE. */
	public EnumNodeShape getNodeShape() {
		return nodeShape;
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
