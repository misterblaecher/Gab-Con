package fr.serveurminecraft.adminchunkloader.mixin;

import dev.ftb.mods.ftblibrary.math.XZ;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import fr.serveurminecraft.adminchunkloader.client.ClientChunkState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Remplace le clic 'claim' par un clic 'force-load' uniquement en mode GabCon. */
@Mixin(targets = "dev.ftb.mods.ftbchunks.client.gui.ChunkScreenPanel$ChunkButton", remap = false)
public abstract class FtbChunkButtonMixin {
    @Shadow(remap = false) @Final private XZ chunkPos;

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void gabcon$clicked(MouseButton button, CallbackInfo ci) {
        if (!ClientChunkState.mode()) return;
        if (button.isLeft()) ClientChunkState.toggle(chunkPos.x(), chunkPos.z());
        ci.cancel();
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true, remap = false)
    private void gabcon$preventClaimDrag(int mouseButton, double dragX, double dragY,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (ClientChunkState.mode()) cir.setReturnValue(false);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"), remap = false)
    private void gabcon$overlay(GuiGraphics gui, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        if (!ClientChunkState.known()) return;
        boolean active = ClientChunkState.forced(chunkPos.x(), chunkPos.z());
        gui.fill(x, y, x + w, y + h, active ? 0x6655DD66 : 0x44CC4444);
        gui.hLine(x, x + w - 1, y, active ? 0xFF22DD66 : 0xFFBB3333);
        gui.vLine(x, y, y + h - 1, active ? 0xFF22DD66 : 0xFFBB3333);
    }

    @Inject(method = "addMouseOverText", at = @At("TAIL"), remap = false)
    private void gabcon$tooltip(TooltipList list, CallbackInfo ci) {
        if (!ClientChunkState.mode()) return;
        list.add(Component.literal("GabCon | force-load vanilla").withStyle(ChatFormatting.GOLD));
        list.add(Component.literal("Chunk: " + chunkPos.x() + ", " + chunkPos.z()));
        list.add(Component.literal(ClientChunkState.known() ?
            (ClientChunkState.forced(chunkPos.x(), chunkPos.z()) ? "CHARGÉ" : "NON FORCÉ") :
            "Synchronisation...").withStyle(ChatFormatting.AQUA));
        list.add(Component.literal("Clic gauche : activer / retirer").withStyle(ChatFormatting.GRAY));
    }
}
