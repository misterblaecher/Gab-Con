package fr.serveurminecraft.gabconget.client;

import fr.serveurminecraft.gabconget.GetNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** Liste des cibles connectées ; les demandes sont revalidées côté serveur. */
public final class GetRightsScreen extends Screen {
    private int page;
    public GetRightsScreen() { super(Component.literal("GabCon - Droits de GET")); }
    @Override protected void init() { build(); }
    public void refresh() { if(minecraft != null) rebuildWidgets(); }
    private void build() {
        int center=width/2, top=Math.max(52,height/5);
        List<GetNetwork.RightEntry> entries=ClientRights.entries();
        int maxPage=Math.max(0,(entries.size()-1)/6);
        page=Math.min(page,maxPage);
        for(int i=page*6; i<Math.min(entries.size(),(page+1)*6);i++) {
            GetNetwork.RightEntry entry=entries.get(i);
            int row=top+38+(i%6)*31;
            addRenderableWidget(Button.builder(Component.literal(entry.imposed()?"RELEVER":"IMPOSER"),
                b->PacketDistributor.sendToServer(new GetNetwork.ImposeRequest(entry.id(),!entry.imposed())))
                .bounds(center+35,row,104,23).build());
        }
        addRenderableWidget(Button.builder(Component.literal("<"),b->{page=Math.max(0,page-1);rebuildWidgets();})
            .bounds(center-136,Math.min(height-65,top+248),35,22).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{page=Math.min(maxPage,page+1);rebuildWidgets();})
            .bounds(center+100,Math.min(height-65,top+248),35,22).build());
        addRenderableWidget(Button.builder(Component.literal("FERMER"),b->onClose())
            .bounds(center-60,height-38,120,24).build());
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float delta) {
        g.fill(0,0,width,height,0xED11121E);
        int center=width/2, top=Math.max(52,height/5);
        g.fill(center-162,top-26,center+162,Math.min(height-48,top+281),0xF52D233B);
        g.renderOutline(center-162,top-26,324,Math.min(height-48,top+281)-(top-26),0xFF8F59C8);
        g.drawCenteredString(font,"GABCON — DROITS DE GET",center,top-15,0xFFDEBFFF);
        g.drawCenteredString(font,"H : pose libre | G : droits gagnés en PvP",center,top+4,0xFFB4FCAA);
        List<GetNetwork.RightEntry> entries=ClientRights.entries();
        if(entries.isEmpty()) g.drawCenteredString(font,"Aucune cible connectée avec un droit actif.",center,top+54,0xFFFFFFFF);
        for(int i=page*6;i<Math.min(entries.size(),(page+1)*6);i++) {
            GetNetwork.RightEntry entry=entries.get(i);
            g.drawString(font,entry.name(),center-136,top+45+(i%6)*31,0xFFFFFFFF,false);
        }
        super.render(g,mouseX,mouseY,delta);
    }
    @Override public boolean isPauseScreen() { return false; }
}
