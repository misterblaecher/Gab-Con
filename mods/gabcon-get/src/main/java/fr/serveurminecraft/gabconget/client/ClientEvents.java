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

public final class ClientEvents {
    private ClientEvents() {}
    private static final KeyMapping GET_KEY = new KeyMapping(
        "key.gabconget.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.gabconget");
    private static final KeyMapping MENU_KEY = new KeyMapping(
        "key.gabconget.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.gabconget");

    @EventBusSubscriber(modid = GabConGet.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event) {
            event.register(GET_KEY); event.register(MENU_KEY);
        }
        @SubscribeEvent public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(GetDetailLayer.LAYER, GetDetailLayer::createBodyLayer);
        }
        @SubscribeEvent public static void addLayers(EntityRenderersEvent.AddLayers event) {
            for (PlayerSkin.Model skin : event.getSkins()) {
                if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                    renderer.addLayer(new GetDetailLayer(renderer,
                        event.getEntityModels().bakeLayer(GetDetailLayer.LAYER), skin == PlayerSkin.Model.SLIM));
                }
            }
        }
    }
    @EventBusSubscriber(modid = GabConGet.MOD_ID, value = Dist.CLIENT)
    public static final class GameBus {
        @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
            ClientGetState.clientTick();
            Minecraft mc = Minecraft.getInstance();
            while(GET_KEY.consumeClick()) {
                if(mc.player != null && mc.level != null && mc.screen == null)
                    PacketDistributor.sendToServer(GetNetwork.ToggleRequest.INSTANCE);
            }
            while(MENU_KEY.consumeClick()) {
                if(mc.player != null && mc.level != null && mc.screen == null) {
                    mc.setScreen(new GetRightsScreen());
                    PacketDistributor.sendToServer(GetNetwork.RightsQuery.INSTANCE);
                }
            }
            ClientNotices.tick();
        }
        @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
            ClientGetState.clear();ClientRights.clear();ClientNotices.clear();
        }
    }
}
