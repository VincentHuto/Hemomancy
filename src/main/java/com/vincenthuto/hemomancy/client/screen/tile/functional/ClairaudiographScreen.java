package com.vincenthuto.hemomancy.client.screen.tile.functional;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.common.menu.tile.functional.ClairaudiographMenu;
import com.vincenthuto.hemomancy.common.network.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
public class ClairaudiographScreen extends AbstractContainerScreen<ClairaudiographMenu> {
    private final VeinBackgroundRenderer veinBackground = new VeinBackgroundRenderer();
    private int offset,selected=-1;
    private long version=-1;
    private Button preview,carve,play,loop;
    public ClairaudiographScreen(ClairaudiographMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=232;inventoryLabelY=138;}
    private Component text(String key){return Component.translatable("gui.hemomancy.clairaudiograph."+key);}
    private void action(int action){PacketHandler.sendToServer(new ClairaudiographActionPacket(menu.containerId,menu.version,action,selected));}
    private Button button(String key,int x,int y,int width,int action){
        return addRenderableWidget(new Button(leftPos+x,topPos+y,width,18,text(key),b->action(action),narration->narration.get()){
            @Override public void renderWidget(GuiGraphics g,int mouseX,int mouseY,float partialTick){
                boolean highlighted=active && isHoveredOrFocused();
                g.fill(getX(),getY(),getX()+getWidth(),getY()+getHeight(),highlighted?0xFF2A0C0C:0xFF1A0808);
                g.renderOutline(getX(),getY(),getWidth(),getHeight(),highlighted?0xFF882020:0xFF440E0E);
                g.renderOutline(getX()+1,getY()+1,getWidth()-2,getHeight()-2,0xFF0D0303);
                renderScrollingString(g,font,2,active?(highlighted?0xFFFFCCCC:0xFFCC8888):0xFF774444);
            }
        });
    }
    @Override protected void init(){super.init();preview=button("preview",8,112,76,0);carve=button("carve",92,112,76,1);play=button("play",72,25,46,2);button("stop",120,25,48,3);loop=button("loop",72,45,96,4);}
    @Override protected void containerTick(){
        if(version!=menu.version){version=menu.version;selected=-1;offset=0;}
        preview.active=selected>=0 && menu.progress==0 && menu.playing==0;carve.active=preview.active;
        play.active=menu.progress==0 && menu.playing==0;
        loop.setMessage(Component.translatable("gui.hemomancy.clairaudiograph.loop_"+(menu.looping==1?"on":"off")));
    }
    private String sourceName(String source){var id=ResourceLocation.tryParse(source);return id==null?text("no_sample").getString():BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(t->t.getDescription().getString()).orElse(text("unknown").getString());}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        var firstInventorySlot=menu.slots.get(2);
        int machineHeight=firstInventorySlot.y-6;
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF0A0204);
        veinBackground.render(g,leftPos,topPos,imageWidth,machineHeight);
        g.renderOutline(leftPos,topPos,imageWidth,machineHeight,0xFF330808);
        g.renderOutline(leftPos+1,topPos+1,imageWidth-2,machineHeight-2,0xFF220606);
        InventoryPanelTextures.blit(g,InventoryPanelTextures.BLOODY,
                leftPos+firstInventorySlot.x-5,topPos+firstInventorySlot.y-6);
        for(int i=0;i<2;i++){
            var slot=menu.slots.get(i);
            int sx=leftPos+slot.x,sy=topPos+slot.y;
            g.fill(sx-1,sy-1,sx+17,sy+17,0xFF0D0303);
            g.fill(sx,sy,sx+16,sy+16,0xFF1A0808);
            g.fill(sx+16,sy,sx+17,sy+17,0xFF3A1212);
            g.fill(sx,sy+16,sx+17,sy+17,0xFF3A1212);
        }
        g.fill(leftPos+10,topPos+47,leftPos+56,topPos+51,0xFF1A0808);g.fill(leftPos+10,topPos+47,leftPos+10+46*menu.progress/80,topPos+51,0xFFCC3333);
        g.fill(leftPos+8,topPos+67,leftPos+168,topPos+109,0xB01A0808);
        g.renderOutline(leftPos+7,topPos+66,162,44,0xFF440E0E);
        g.enableScissor(leftPos+8,topPos+67,leftPos+168,topPos+109);
        if(menu.choices.isEmpty())g.drawWordWrap(font,text("no_calls"),leftPos+10,topPos+72,154,0xFFCC8888);
        for(int row=0;row<3;row++){
            int i=offset+row;if(i>=menu.choices.size())break;
            if(i==selected)g.fill(leftPos+8,topPos+67+row*14,leftPos+168,topPos+81+row*14,0xFF4A0E0E);
            var choice=menu.choices.get(i);String name=Component.translatable("sound_kind.hemomancy."+choice.kind()).getString()+" - "+choice.sound();
            g.drawString(font,font.plainSubstrByWidth(name,154),leftPos+10,topPos+70+row*14,0xFFFFCCCC,false);
        }
        g.disableScissor();
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,font.plainSubstrByWidth(title.getString(),160),8,6,0xFFAA2222,false);
        var recording=menu.machine.recording();
        String label=recording==null?sourceName(menu.source):recording.readable()?sourceName(recording.source())+" / "+Component.translatable("sound_kind.hemomancy."+recording.kind()).getString():text("unreadable").getString();
        g.drawString(font,font.plainSubstrByWidth(label,160),8,16,0xFFCC8888,false);
    }
    @Override public boolean mouseClicked(double x,double y,int button){
        if(button==0 && x>=leftPos+8 && x<leftPos+168 && y>=topPos+67 && y<topPos+109){int i=offset+(int)(y-topPos-67)/14;if(i<menu.choices.size())selected=i;return true;}
        return super.mouseClicked(x,y,button);
    }
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){
        if(x>=leftPos+8 && x<leftPos+168 && y>=topPos+67 && y<topPos+109){offset=Math.max(0,Math.min(Math.max(0,menu.choices.size()-3),offset-(int)Math.signum(dy)));return true;}
        return super.mouseScrolled(x,y,dx,dy);
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        super.render(g,x,y,partial);renderTooltip(g,x,y);
        if(x>=leftPos+8 && x<leftPos+168 && y>=topPos+67 && y<topPos+109){
            int i=offset+(y-topPos-67)/14;
            if(i<menu.choices.size())g.renderTooltip(font,Component.literal(menu.choices.get(i).sound()),x,y);
        }
        if(x>=leftPos+8 && x<leftPos+168 && y>=topPos+16 && y<topPos+25){
            var recording=menu.machine.recording();
            g.renderTooltip(font,Component.literal(recording==null?menu.source:recording.source()+" / "+recording.sound()),x,y);
        }
    }
}
