package fr.serveurminecraft.adminchunkloader.mixin;

import fr.serveurminecraft.adminchunkloader.client.ClientChunkState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** La fermeture du panneau de claims FTB désactive le mode GabCon. */
@Mixin(value = dev.ftb.mods.ftbchunks.client.gui.ChunkScreenPanel.class, remap = false)
public abstract class FtbChunkScreenMixin {
    @Inject(method = "onClosed", at = @At("HEAD"), remap = false)
    private void gabcon$onClose(CallbackInfo ci) { ClientChunkState.clear(); }
}
