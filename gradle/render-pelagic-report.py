"""Render measured block heights from the opt-in Pelagic world validation run."""
import argparse
import json
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.colors import LightSource
import numpy as np

parser = argparse.ArgumentParser()
parser.add_argument("report", type=Path)
parser.add_argument("output", type=Path)
args = parser.parse_args()
report = json.loads(args.report.read_text())
sites = report["sites"]
rows = (len(sites) + 2) // 3
fig = plt.figure(figsize=(16, rows * 3.4), facecolor="#ebf0f2")
x, z = np.meshgrid(np.arange(48), np.arange(48), indexing="ij")
for i, site in enumerate(sites):
    ax = fig.add_subplot(rows, 3, i + 1, projection="3d")
    height = np.array(site["heights"]).reshape(48, 48)
    colors = LightSource(315, 45).shade(height, cmap=plt.get_cmap("gist_earth"),
                                      vert_exag=1, vmin=-55, vmax=72)
    ax.plot_surface(x, z, height, facecolors=colors, rstride=1, cstride=1, shade=False, linewidth=0)
    ax.set_box_aspect((48, 48, max(10, height.max() - height.min())))
    ax.view_init(elev=35, azim=-65)
    ax.set_title(f'{site["layer"].replace("_", " ")}\n({site["x"]}, {site["z"]})  Y {height.min()}..{height.max()}', fontsize=10)
    ax.set_xticks([])
    ax.set_yticks([])
    ax.set_zlabel("Y", labelpad=0)
    ax.set_facecolor("#ebf0f2")
fig.suptitle(f'Pelagic natural terrain — seed {report["seed"]}\nMeasured finished chunks; each view covers 48 × 48 blocks', fontsize=18)
fig.subplots_adjust(left=.02, right=.97, top=.94, bottom=.01, wspace=.08, hspace=.26)
args.output.parent.mkdir(parents=True, exist_ok=True)
fig.savefig(args.output, dpi=120)
