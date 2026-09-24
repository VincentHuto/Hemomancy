package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.common.worldgen.structure.VagrantMindGeometry.Lobe;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** The nerve web strung across the Vagrant Mind's hollow interior. */
public final class VagrantMindFiberWeb {
	private static final int ANCHOR_COUNT = 20;

	public record Strand(double ax, double ay, double az, double bx, double by, double bz, boolean thick) {}

	private VagrantMindFiberWeb() {}

	public static List<Strand> strands(long seed, List<Lobe> lobes) {
		Random random = new Random(seed ^ 0x5EEDFEED);
		List<double[]> anchors = anchors(random, lobes);
		List<Strand> strands = new ArrayList<>();
		// Each anchor reaches across to two others, producing a web with natural junctions.
		for (int i = 0; i < anchors.size(); i++) {
			for (int link = 1; link <= 2; link++) {
				double[] a = anchors.get(i);
				double[] b = anchors.get((i + link * 3 + 1) % anchors.size());
				double dx = b[0] - a[0];
				double dy = b[1] - a[1];
				double dz = b[2] - a[2];
				if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= 4.0) {
					continue;
				}
				strands.add(new Strand(a[0], a[1], a[2], b[0], b[1], b[2], random.nextInt(4) == 0));
			}
		}
		return strands;
	}

	/** Endpoints shared by more than one strand — these get lit as synaptic nodes. */
	public static List<double[]> nodes(List<Strand> strands) {
		Map<String, double[]> seen = new LinkedHashMap<>();
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (Strand strand : strands) {
			record(seen, counts, strand.ax(), strand.ay(), strand.az());
			record(seen, counts, strand.bx(), strand.by(), strand.bz());
		}
		List<double[]> nodes = new ArrayList<>();
		counts.forEach((key, count) -> {
			if (count > 1) {
				nodes.add(seen.get(key));
			}
		});
		return nodes;
	}

	private static void record(Map<String, double[]> seen, Map<String, Integer> counts,
			double x, double y, double z) {
		String key = Math.round(x) + ":" + Math.round(y) + ":" + Math.round(z);
		seen.putIfAbsent(key, new double[] { x, y, z });
		counts.merge(key, 1, Integer::sum);
	}

	private static List<double[]> anchors(Random random, List<Lobe> lobes) {
		List<double[]> anchors = new ArrayList<>(ANCHOR_COUNT);
		int attempts = 0;
		while (anchors.size() < ANCHOR_COUNT && attempts++ < 4000) {
			double theta = random.nextDouble() * Math.PI * 2.0;
			double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
			double radius = VagrantMindGeometry.OUTER_RADIUS * 0.6;
			double x = radius * Math.sin(phi) * Math.cos(theta);
			double y = radius * Math.cos(phi) * 0.6;
			double z = radius * Math.sin(phi) * Math.sin(theta);
			// Walk inward until the point sits just inside the shell.
			for (int step = 0; step < 40 && VagrantMindGeometry.field(lobes, x, y, z) < 1.0; step++) {
				x *= 0.95;
				y *= 0.95;
				z *= 0.95;
			}
			if (VagrantMindGeometry.field(lobes, x, y, z) >= 1.0) {
				anchors.add(new double[] { x, y, z });
			}
		}
		return anchors;
	}
}
