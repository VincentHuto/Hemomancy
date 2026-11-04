"""Shared NBT authoring for Troupe buildings and teaching camps."""
from copy import deepcopy
from pathlib import Path
import nbtlib as nbt

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / 'src/main/resources/data/hemomancy'


class Template:
    def __init__(self, size):
        self.size = size
        self.blocks = {}
        self.entities = []
        self.block_tags = {}

    def block(self, x, y, z, name, properties=None):
        self.blocks[x, y, z] = (name, properties or {})

    def entity(self, role, x, y, z):
        self.entities.append(nbt.Compound({
            'pos': nbt.List[nbt.Double]([x, y, z]),
            'blockPos': nbt.List[nbt.Int]([int(x), int(y), int(z)]),
            'nbt': nbt.Compound({'id': nbt.String('hemomancy:' + role), 'PersistenceRequired': nbt.Byte(1)})}))

    def room(self, x, z, w, h, roof='minecraft:red_wool'):
        for dx in range(w):
            for dz in range(h):
                self.block(x + dx, 0, z + dz, 'minecraft:dark_oak_planks')
                for y in range(1, 6): self.block(x + dx, y, z + dz, 'minecraft:air')
                self.block(x + dx, 5, z + dz, roof)
        for dx in range(w):
            for dz in range(h):
                if dx not in (0, w - 1) and dz not in (0, h - 1): continue
                if dz == 0 and w // 2 - 1 <= dx <= w // 2 + 1: continue
                for y in range(1, 5):
                    self.block(x + dx, y, z + dz, 'minecraft:dark_oak_planks' if y == 1 else 'minecraft:red_wool')
        self.block(x + 1, 3, z + 1, 'minecraft:lantern', {'hanging': 'false', 'waterlogged': 'false'})

    def save(self, name, directory=None):
        palette, states, blocks = [], {}, []
        for pos, (block, props) in self.blocks.items():
            assert all(0 <= v < bound for v, bound in zip(pos, self.size)), (name, pos)
            key = block, tuple(sorted(props.items()))
            if key not in states:
                states[key] = len(palette)
                state = nbt.Compound({'Name': nbt.String(block)})
                if props: state['Properties'] = nbt.Compound({k: nbt.String(v) for k, v in props.items()})
                palette.append(state)
            entry = nbt.Compound({'pos': nbt.List[nbt.Int](pos), 'state': nbt.Int(states[key])})
            if pos in self.block_tags: entry['nbt'] = deepcopy(self.block_tags[pos])
            blocks.append(entry)
        root = nbt.File({'DataVersion': nbt.Int(3955), 'size': nbt.List[nbt.Int](self.size),
                         'palette': nbt.List[nbt.Compound](palette), 'blocks': nbt.List[nbt.Compound](blocks),
                         'entities': nbt.List[nbt.Compound](self.entities)})
        path = (directory or DATA / 'structure') / (name + '.nbt')
        path.parent.mkdir(parents=True, exist_ok=True)
        root.save(path, gzipped=True)
