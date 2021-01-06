package com.huto.hemomancy.particle;

/**
 * Modified class of ElementType: https://github.com/Sirttas/ElementalCraft/blob/b91ca42b3d139904d9754d882a595406bad1bd18/src/main/java/sirttas/elementalcraft/ElementType.java
 */
public class ParticleColor {

    private final float r;
    private final float g;
    private final float b;
    private final int color;

    public ParticleColor(int r, int g, int b) {
        this.r = r / 255F;
        this.g = g / 255F;
        this.b = b / 255F;
        this.color = (r << 16) | (g << 8) | b;
    }

    public ParticleColor(float r, float g, float b){
        this((int)r,(int) g,(int) b);
    }

    public float getRed(){return r;}

    public float getGreen() {
        return g;
    }

    public float getBlue() {
        return b;
    }

    public int getColor() {
        return color;
    }

    public String serialize(){
        return "" + this.r + "," + this.g +","+this.b;
    }

    public static ParticleColor deserialize(String string){
        String[] arr = string.split(",");
        return new ParticleColor(Integer.parseInt(arr[0].trim()), Integer.parseInt(arr[1].trim()), Integer.parseInt(arr[2].trim()));
    }

    public static class IntWrapper{
        public int r;
        public int g;
        public int b;

        public IntWrapper(int r, int g, int b){
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public ParticleColor toParticleColor(){
            return new ParticleColor(r,g,b);
        }

        public String serialize(){
            return "" + this.r + "," + this.g +","+this.b;
        }

        public void makeVisible(){
            if(r + g + b < 20){
                b += 10;
                g += 10;
                r += 10;
            }
        }

        public static ParticleColor.IntWrapper deserialize(String string){
            String[] arr = string.split(",");
            return new ParticleColor.IntWrapper(Integer.parseInt(arr[0].trim()), Integer.parseInt(arr[1].trim()), Integer.parseInt(arr[2].trim()));
        }
    }
}