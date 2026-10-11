package fr.serveurminecraft.adminchunkloader.mixin;

import dev.ftb.mods.ftbchunks.client.gui.ChunkScreen;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import fr.serveurminecraft.adminchunkloader.client.ClientChunkState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bouton BEACON dans la barre de la carte de revendication FTB Chunks. */
@Mixin(targets = "dev.ftb.mods.ftbchunks.client.gui.ChunkScreen$CustomTopPanel", remap = false)
public abstract class FtbTopBarMixin {
    @Unique private Button gabcon$button;

    @Inject(method = "addWidgets", at = @At("TAIL"), remap = false)
    private void gabcon$add(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasPermissions(2)) return;
        Panel panel = (Panel) (Object) this;
        if (!(panel.getGui() instanceof ChunkScreen screen) || screen.getOpenedAs() != null) return;
        gabcon$button = new SimpleButton(panel,
            Component.literal("GabCon Force-load : OFF"), Icons.BEACON,
            (button, mouse) -> {
                if (!mouse.isLeft()) return;
                ClientChunkState.toggleMode();
                button.setTitle(Component.literal("GabCon Force-load : " +
                    (ClientChunkState.mode() ? "ON" : "OFF")));
                button.setIcon(ClientChunkState.mode() ? Icons.ACCEPT : Icons.BEACON);
            });
        panel.add(gabcon$button);
    }

    @Inject(method = "alignWidgets", at = @At("TAIL"), remap = false)
    private void gabcon$align(CallbackInfo ci) {
        if (gabcon$button != null) {
            // La gauche utilise déjà x=2 (unclaim) et x=18 (admin FTB).
            gabcon$button.setPosAndSize(34, 2, 12, 12);
        }
    }
}
