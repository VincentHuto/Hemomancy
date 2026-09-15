# Export image-generated source materials into native 16-pixel Minecraft animation strips.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$artRoot = Join-Path $projectRoot 'docs/phlegethontic-nether-worldgen/materials/ichor-16px'
$textureRoot = Join-Path $projectRoot 'src/main/resources/assets/hemomancy/textures/block'
Add-Type -AssemblyName System.Drawing
$drawingReferences = @('System.Drawing.Common', 'System.Drawing.Primitives')
foreach ($assembly in 'System.Private.Windows.GdiPlus','System.Private.Windows.Core') {
    if (Test-Path (Join-Path $PSHOME "$assembly.dll")) { $drawingReferences += $assembly }
}
Add-Type -ReferencedAssemblies $drawingReferences -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Imaging;

public static class IchorPixelExport {
    private const int TileSize = 16;
    private const int SourceSize = 64;
    private const int StillFrames = 48;
    private const int FlowFrames = 64;
    private static readonly Color[] Palette = {
        Color.FromArgb(27,6,12), Color.FromArgb(55,8,18), Color.FromArgb(90,11,28),
        Color.FromArgb(128,16,35), Color.FromArgb(160,26,47), Color.FromArgb(190,48,65),
        Color.FromArgb(177,103,93), Color.FromArgb(202,154,124)
    };

    private static double Wrap(double value, int size) {
        value %= size;
        if (value < 0) value += size;
        return value >= size ? 0 : value;
    }

    private static Color Average(Bitmap source, int x0, int y0, int x1, int y1) {
        long r=0,g=0,b=0,count=0;
        for (int y=y0;y<y1;y++) for (int x=x0;x<x1;x++) {
            Color pixel=source.GetPixel(x,y);
            r+=pixel.R; g+=pixel.G; b+=pixel.B; count++;
        }
        return Color.FromArgb((int)(r/count),(int)(g/count),(int)(b/count));
    }

    private static Bitmap Reduce(Bitmap source) {
        Bitmap result=new Bitmap(SourceSize,SourceSize,PixelFormat.Format32bppArgb);
        for (int y=0;y<SourceSize;y++) for (int x=0;x<SourceSize;x++) {
            int x0=source.Width*x/SourceSize, x1=source.Width*(x+1)/SourceSize;
            int y0=source.Height*y/SourceSize, y1=source.Height*(y+1)/SourceSize;
            result.SetPixel(x,y,Average(source,x0,y0,x1,y1));
        }
        // Pair and blend opposite borders so adjacent fluid blocks do not expose a hard image-generation seam.
        for (int inset=0;inset<8;inset++) {
            double weight=(8-inset)/9.0;
            for (int y=0;y<SourceSize;y++) {
                Color a=result.GetPixel(inset,y), b=result.GetPixel(SourceSize-1-inset,y);
                Color blend=Blend(a,b,0.5);
                result.SetPixel(inset,y,Blend(a,blend,weight));
                result.SetPixel(SourceSize-1-inset,y,Blend(b,blend,weight));
            }
            for (int x=0;x<SourceSize;x++) {
                Color a=result.GetPixel(x,inset), b=result.GetPixel(x,SourceSize-1-inset);
                Color blend=Blend(a,b,0.5);
                result.SetPixel(x,inset,Blend(a,blend,weight));
                result.SetPixel(x,SourceSize-1-inset,Blend(b,blend,weight));
            }
        }
        return result;
    }

    private static Color Blend(Color a, Color b, double amount) {
        return Color.FromArgb(
            (int)Math.Round(a.R+(b.R-a.R)*amount),
            (int)Math.Round(a.G+(b.G-a.G)*amount),
            (int)Math.Round(a.B+(b.B-a.B)*amount));
    }

    private static Color Sample(Bitmap source, double x, double y) {
        x=Wrap(x,source.Width); y=Wrap(y,source.Height);
        int x0=(int)Math.Floor(x), y0=(int)Math.Floor(y);
        int x1=(x0+1)%source.Width, y1=(y0+1)%source.Height;
        if (x0<0 || x0>=source.Width || x1<0 || x1>=source.Width
                || y0<0 || y0>=source.Height || y1<0 || y1>=source.Height)
            throw new InvalidOperationException(String.Format(
                    "Wrapped sample out of bounds: x={0}, y={1}, x0={2}, x1={3}, y0={4}, y1={5}, size={6}x{7}",
                    x,y,x0,x1,y0,y1,source.Width,source.Height));
        double tx=x-x0, ty=y-y0;
        return Blend(Blend(source.GetPixel(x0,y0),source.GetPixel(x1,y0),tx),
                Blend(source.GetPixel(x0,y1),source.GetPixel(x1,y1),tx),ty);
    }

    private static Color Quantize(Color input) {
        Color nearest=Palette[0]; long minimum=long.MaxValue;
        foreach (Color color in Palette) {
            long dr=input.R-color.R, dg=input.G-color.G, db=input.B-color.B;
            long distance=dr*dr+dg*dg+db*db;
            if (distance<minimum) { minimum=distance; nearest=color; }
        }
        return nearest;
    }

    private static void Put(Bitmap strip, int frame, int x, int y, Color color) {
        strip.SetPixel(x,frame*TileSize+y,color);
    }

    private static void MakeStill(Bitmap source, Bitmap strip) {
        for (int frame=0;frame<StillFrames;frame++) {
            double phase=2*Math.PI*frame/StillFrames;
            for (int y=0;y<TileSize;y++) for (int x=0;x<TileSize;x++) {
                double warpX=0.85*Math.Sin(2*Math.PI*y/TileSize+phase)
                        +0.35*Math.Sin(2*Math.PI*(x+y)/TileSize-2*phase);
                double warpY=0.75*Math.Cos(2*Math.PI*x/TileSize-phase)
                        +0.30*Math.Sin(2*Math.PI*(x-2*y)/TileSize+2*phase);
                Color sample=Sample(source,(x+warpX)*4.0,(y+warpY)*4.0);
                Put(strip,frame,x,y,Quantize(sample));
            }
            // Two subdued surface motes orbit with the same period as the convection field.
            int xA=(int)Math.Round(3+1.25*Math.Cos(phase));
            int yA=(int)Math.Round(5+1.00*Math.Sin(phase));
            int xB=(int)Math.Round(11+1.00*Math.Cos(phase+Math.PI));
            int yB=(int)Math.Round(13+1.25*Math.Sin(phase+Math.PI));
            Put(strip,frame,(xA+TileSize)%TileSize,(yA+TileSize)%TileSize,Palette[6]);
            Put(strip,frame,(xB+TileSize)%TileSize,(yB+TileSize)%TileSize,Palette[7]);
        }
    }

    private static void MakeFlow(Bitmap source, Bitmap strip) {
        for (int frame=0;frame<FlowFrames;frame++) {
            double phase=2*Math.PI*frame/FlowFrames;
            double downstream=(double)SourceSize*frame/FlowFrames;
            for (int y=0;y<TileSize;y++) for (int x=0;x<TileSize;x++) {
                double warpX=1.05*Math.Sin(2*Math.PI*y/TileSize-phase)
                        +0.45*Math.Sin(4*Math.PI*y/TileSize+phase+2*Math.PI*x/TileSize);
                double warpY=0.35*Math.Sin(2*Math.PI*x/TileSize+2*phase)
                        +0.20*Math.Cos(2*Math.PI*(x+y)/TileSize-phase);
                Color sample=Sample(source,(x+warpX)*4.0,y*4.0-downstream+warpY*4.0);
                Put(strip,frame,x,y,Quantize(sample));
            }
            // Motes use different integral loop rates, breaking the single scrolling-sheet rhythm.
            int xA=(int)Math.Round(2+Math.Sin(phase));
            int xB=(int)Math.Round(10+1.2*Math.Sin(phase+1.3));
            Put(strip,frame,(xA+TileSize)%TileSize,(1+frame/4)%TileSize,Palette[6]);
            Put(strip,frame,(xB+TileSize)%TileSize,(9+frame/2)%TileSize,Palette[7]);
        }
    }

    private static void DrawFrame(Bitmap preview, Bitmap strip, int frame, int left, int top, int scale) {
        for (int y=0;y<TileSize;y++) for (int x=0;x<TileSize;x++) {
            Color color=strip.GetPixel(x,frame*TileSize+y);
            for (int py=0;py<scale;py++) for (int px=0;px<scale;px++)
                preview.SetPixel(left+x*scale+px,top+y*scale+py,color);
        }
    }

    public static void Pack(string artRoot) {
        using (Bitmap stillSourceRaw=new Bitmap(System.IO.Path.Combine(artRoot,"still-source-v2.png")))
        using (Bitmap flowSourceRaw=new Bitmap(System.IO.Path.Combine(artRoot,"flow-source-v2.png")))
        using (Bitmap stillSource=Reduce(stillSourceRaw))
        using (Bitmap flowSource=Reduce(flowSourceRaw))
        using (Bitmap still=new Bitmap(TileSize,TileSize*StillFrames,PixelFormat.Format32bppArgb))
        using (Bitmap flow=new Bitmap(TileSize,TileSize*FlowFrames,PixelFormat.Format32bppArgb))
        using (Bitmap overlay=new Bitmap(TileSize,TileSize,PixelFormat.Format32bppArgb))
        using (Bitmap preview=new Bitmap(1024,256,PixelFormat.Format32bppArgb)) {
            MakeStill(stillSource,still);
            MakeFlow(flowSource,flow);
            for (int y=0;y<TileSize;y++) for (int x=0;x<TileSize;x++)
                overlay.SetPixel(x,y,Color.FromArgb(105,still.GetPixel(x,y)));
            for (int i=0;i<8;i++) {
                DrawFrame(preview,still,i*StillFrames/8,i*128,0,8);
                DrawFrame(preview,flow,i*FlowFrames/8,i*128,128,8);
            }
            still.Save(System.IO.Path.Combine(artRoot,"phlegethontic_ichor_still.png"),ImageFormat.Png);
            flow.Save(System.IO.Path.Combine(artRoot,"phlegethontic_ichor_flow.png"),ImageFormat.Png);
            overlay.Save(System.IO.Path.Combine(artRoot,"phlegethontic_ichor_overlay.png"),ImageFormat.Png);
            preview.Save(System.IO.Path.Combine(artRoot,"preview.png"),ImageFormat.Png);
        }
    }
}
'@
[IchorPixelExport]::Pack($artRoot)
$metadata = @{
    still = @{ animation = @{ width=16; height=16; frametime=1; interpolate=$false } }
    flow = @{ animation = @{ width=16; height=16; frametime=1; interpolate=$false } }
}
foreach ($kind in 'still','flow') {
    $path = Join-Path $artRoot "phlegethontic_ichor_$kind.png.mcmeta"
    [IO.File]::WriteAllText($path,($metadata[$kind] | ConvertTo-Json -Depth 5)+"`n")
}
foreach ($name in 'still.png','still.png.mcmeta','flow.png','flow.png.mcmeta','overlay.png') {
    $file = "phlegethontic_ichor_$name"
    Copy-Item -LiteralPath (Join-Path $artRoot $file) -Destination (Join-Path $textureRoot $file)
}
Write-Output 'Exported still 16x768 (48 frames), flow 16x1024 (64 frames), and overlay 16x16; installed all five resources.'
