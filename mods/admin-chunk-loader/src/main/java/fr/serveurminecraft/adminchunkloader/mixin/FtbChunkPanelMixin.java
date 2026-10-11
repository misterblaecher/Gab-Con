package fr.serveurminecraft.adminchunkloader.mixin;

import dev.ftb.mods.ftblibrary.math.XZ;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import fr.serveurminecraft.adminchunkloader.client.ClientChunkState;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Garantit qu'aucun paquet de claim FTB n'est déclenché en mode force-load GabCon. */
@Mixin(value = dev.ftb.mods.ftbchunks.client.gui.ChunkScreenPanel.class, remap = false)
public abstract class FtbChunkPanelMixin {
    @Shadow(remap = false) @Final private Set<XZ> selectedChunks;

    @Inject(method = "mouseReleased", at = @At("HEAD"), remap = false)
    private void gabcon$clearSelection(MouseButton mouse, CallbackInfo ci) {
        if (ClientChunkState.mode()) selectedChunks.clear();
    }
}
