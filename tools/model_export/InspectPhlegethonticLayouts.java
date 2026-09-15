import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticBasinLayout;

/** Emit the production sampler's columns for comparison with saved validation-world blocks. */
public final class InspectPhlegethonticLayouts {
    public static void main(String[] args) {
        long seed=Long.parseLong(args[0]);int centerX=Integer.parseInt(args[1]),centerZ=Integer.parseInt(args[2]);
        System.out.println("x,z,surface,floor,layout,channel,raised");
        for(int cx=centerX-5;cx<centerX+5;cx++)for(int cz=centerZ-5;cz<centerZ+5;cz++) {
            var layouts=PhlegethonticBasinLayout.nearChunk(seed,cx,cz,32);
            for(int x=cx*16;x<cx*16+16;x++)for(int z=cz*16;z<cz*16+16;z++) {
                PhlegethonticBasinLayout.Column best=null;long owner=0;
                for(var layout:layouts) {
                    var sampled=layout.sample(x+.5,z+.5);
                    if(com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTerrainPlan.prefers(sampled,best)){best=sampled;owner=layout.id();}
                }
                if(best.distance()<=0)System.out.println(x+","+z+","+best.surface()+","+best.floor()+","+owner+","+best.channel()+","+best.raised());
            }
        }
    }
}
