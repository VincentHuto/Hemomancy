"""Compare generated Phlegethontic blocks in two isolated validation worlds (requires nbtlib)."""
import argparse, collections, io, json, zlib
from pathlib import Path
import nbtlib

def read_chunks(run):
    report=json.loads((run/'phlegethontic-validation.json').read_text())
    x,_,z=map(int,report['basin'].split(','));center=(x//16,z//16);result={}
    for path in (run/'world/DIM-1/region').glob('*.mca'):
        data=path.read_bytes()
        for i in range(1024):
            off=int.from_bytes(data[4*i:4*i+3],'big')*4096
            if not off:continue
            n=int.from_bytes(data[off:off+4],'big')
            if data[off+4]!=2:raise ValueError(f'Unexpected compression in {path}')
            chunk=nbtlib.File.parse(io.BytesIO(zlib.decompress(data[off+5:off+4+n])))
            cx,cz=int(chunk['xPos']),int(chunk['zPos'])
            if not(center[0]-5<=cx<center[0]+5 and center[1]-5<=cz<center[1]+5):continue
            for sec in chunk.get('sections',[]):
                sy=int(sec['Y']);states=sec.get('block_states',{});palette=states.get('palette',[])
                if not palette or sy<0 or sy>6:continue
                bits=max(4,(len(palette)-1).bit_length());per=64//bits;mask=(1<<bits)-1;array=states.get('data',[])
                for b in range(4096):
                    idx=((int(array[b//per])&((1<<64)-1))>>((b%per)*bits))&mask if len(palette)>1 else 0
                    pos=(cx*16+b%16,sy*16+b//256,cz*16+(b//16)%16)
                    state=palette[idx]
                    properties=state.get('Properties',{})
                    result[pos]=str(state['Name'])+('['+','.join(f'{k}={v}' for k,v in sorted(properties.items()))+']' if properties else '')
    return result

def relevant(name):return 'phlegethontic' in name or 'blood_scorched' in name

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('first',type=Path);parser.add_argument('second',type=Path)
    args=parser.parse_args();first=read_chunks(args.first);second=read_chunks(args.second)
    diff=[(p,a,second.get(p,'')) for p,a in first.items() if a!=second.get(p) and (relevant(a) or relevant(second.get(p,'')))]
    # Authored reservoir surfaces end at Nether sea level +36, and their banks at +37.
    # Higher scab belongs to biome surface rules and is modified by vanilla lava/magma decoration.
    terrain=[entry for entry in diff if entry[0][1]<=69]
    print('Procedural river / vein differences (including block-state properties):',len(terrain))
    print('Higher native surface-decoration differences:',len(diff)-len(terrain))
    print(collections.Counter((a,b) for p,a,b in terrain))
    for p,a,b in terrain[:60]:print(p,a,b)
    raise SystemExit(1 if terrain else 0)
