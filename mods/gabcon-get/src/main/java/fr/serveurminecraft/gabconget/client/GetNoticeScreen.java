package fr.serveurminecraft.gabconget.client;

import fr.serveurminecraft.gabconget.GabConGet;
import fr.serveurminecraft.gabconget.GetNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Écrans 1920x1080 : la texture n'a ni texte ni vrai bouton. */
public final class GetNoticeScreen extends Screen {
    private static final ResourceLocation GRANTED = ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "textures/gui/get_granted.png");
    private static final ResourceLocation IMPOSED = ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "textures/gui/get_imposed.png");
    private static final ResourceLocation CANCELLED = ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "textures/gui/get_cancelled.png");
    private final GetNetwork.Notice notice;
    private int x, y, w, h;

    public GetNoticeScreen(GetNetwork.Notice notice) {
        super(Component.literal("GabCon GET"));
        this.notice = notice;
    }
    private ResourceLocation texture() {
        return notice.kind() == GetNetwork.NOTICE_GRANTED ? GRANTED :
            notice.kind() == GetNetwork.NOTICE_IMPOSED ? IMPOSED : CANCELLED;
    }
    private String titleText() {
        return switch (notice.kind()) {
            case GetNetwork.NOTICE_GRANTED -> "DROIT DE GET DÉBLOQUÉ !";
            case GetNetwork.NOTICE_IMPOSED -> "GUEULE EN TERRE !";
            default -> "GET ANNULÉE !";
        };
    }
    private String messageText() {
        return switch (notice.kind()) {
            case GetNetwork.NOTICE_GRANTED -> "Tu peux imposer la GET à " + notice.other() + ".";
            case GetNetwork.NOTICE_IMPOSED -> notice.other() + " t'a imposé la GET. Le comité a parlé !";
            default -> "Votre dette de GET avec " + notice.other() + " est effacée.";
        };
    }
    private String buttonText() {
        return switch (notice.kind()) {
            case GetNetwork.NOTICE_GRANTED -> "MAGNIFIQUE...";
            case GetNetwork.NOTICE_IMPOSED -> "J'AI COMPRIS";
            default -> "RETOUR AU JEU";
        };
    }
    @Override protected void init() {
        float scale=Math.min(this.width/1920f, this.height/1080f);
        w=Math.max(1,Math.round(1920*scale)); h=Math.max(1,Math.round(1080*scale));
        x=(this.width-w)/2; y=(this.height-h)/2;
        int closeSize=Math.max(14,Math.round(48*scale));
        addRenderableWidget(Button.builder(Component.literal("X"), btn -> onClose())
            .bounds(x+w-Math.round(112*scale),y+Math.round(56*scale),closeSize,closeSize).build());
        addRenderableWidget(Button.builder(Component.literal(buttonText()), btn -> onClose())
            .bounds(x+(w-Math.max(100,Math.round(320*scale)))/2,
                y+Math.round(918*scale),Math.max(100,Math.round(320*scale)),Math.max(22,Math.round(64*scale))).build());
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
        graphics.fill(0,0,width,height,0xFF10101B);
        graphics.blit(texture(),x,y,w,h,0f,0f,1920,1080,1920,1080);
        int titleY=y+Math.round(h*0.115f);
        int subY=y+Math.round(h*0.155f);
        graphics.drawCenteredString(font,titleText(),x+w/2,titleY,0xFFF9F4D0);
        graphics.drawCenteredString(font,messageText(),x+w/2,subY,0xFFF6F6F6);
        super.render(graphics,mouseX,mouseY,partialTick);
    }
    @Override public boolean isPauseScreen() { return false; }
}
