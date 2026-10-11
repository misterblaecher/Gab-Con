package fr.serveurminecraft.adminchunkloader.mixin;

import fr.serveurminecraft.adminchunkloader.client.ClientChunkState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fermer la carte désactive le mode, évite les états fantômes entre serveurs. */
@Mixin(value = dev.ftb.mods.ftbchunks.client.gui.ChunkScreen.class, remap = false)
public abstract class FtbChunkScreenMixin {
    @Inject(method = "onClosed", at = @At("HEAD"), remap = false)
    private void gabcon$onClose(CallbackInfo ci) { ClientChunkState.clear(); }
}
