package fr.serveurminecraft.gabconget.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import fr.serveurminecraft.gabconget.GabConGet;
import fr.serveurminecraft.gabconget.GetNetwork;
import org.lwjgl.glfw.GLFW;

/** Enregistre la touche et la couche de rendu seulement sur le client physique. */
public final class ClientEvents {
    private ClientEvents() {}

    private static final KeyMapping GET_KEY = new KeyMapping(
        "key.gabconget.toggle", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_H, "key.categories.gabconget");

    @EventBusSubscriber(modid = GabConGet.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            event.register(GET_KEY);
        }

        @SubscribeEvent
        public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(GetDetailLayer.LAYER, GetDetailLayer::createBodyLayer);
        }

        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            for (PlayerSkin.Model skin : event.getSkins()) {
                if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                    renderer.addLayer(new GetDetailLayer(renderer,
                        event.getEntityModels().bakeLayer(GetDetailLayer.LAYER),
                        skin == PlayerSkin.Model.SLIM));
                }
            }
        }
    }

    @EventBusSubscriber(modid = GabConGet.MOD_ID, value = Dist.CLIENT)
    public static final class GameBus {
        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            ClientGetState.clientTick();
            Minecraft mc = Minecraft.getInstance();
            while (GET_KEY.consumeClick()) {
                if (mc.player != null && mc.level != null && mc.screen == null) {
                    PacketDistributor.sendToServer(GetNetwork.ToggleRequest.INSTANCE);
                }
            }
        }

        @SubscribeEvent
        public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
            ClientGetState.clear();
        }
    }
}
