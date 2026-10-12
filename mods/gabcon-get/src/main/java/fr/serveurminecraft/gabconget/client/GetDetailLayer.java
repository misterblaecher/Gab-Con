package fr.serveurminecraft.gabconget.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import fr.serveurminecraft.gabconget.GabConGet;

/** Petits doigts supplémentaires ET deux mollets repliés, texturés avec le skin. */
public final class GetDetailLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(GabConGet.MOD_ID, "fingers_and_calves"), "main");
    private final ModelPart root;
    private final boolean slim;

    public GetDetailLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
                          ModelPart root, boolean slim) {
        super(renderer);
        this.root = root;
        this.slim = slim;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition base = mesh.getRoot();
        for (int hand = 0; hand < 2; hand++) {
            String side = hand == 0 ? "right" : "left";
            int u = hand == 0 ? 40 : 32;
            int v = hand == 0 ? 16 : 48;
            for (int digit = 0; digit < 5; digit++) {
                // Cubes mobiles indépendants : les doigts n'existent pas dans le skin vanilla.
                base.addOrReplaceChild(side + "_finger_" + digit,
                    CubeListBuilder.create().texOffs(u, v)
                        .addBox(-0.34F, 0, -0.34F, 0.68F, 2.7F, 0.68F), PartPose.ZERO);
            }
        }
        base.addOrReplaceChild("right_calf", CubeListBuilder.create().texOffs(0, 16)
            .addBox(-2, 0, -2, 4, 10, 4), PartPose.ZERO);
        base.addOrReplaceChild("left_calf", CubeListBuilder.create().texOffs(16, 48)
            .addBox(-2, 0, -2, 4, 10, 4), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        float raw = ClientGetState.progress(player, partialTick);
        if (raw <= 0.01F || player.isInvisible()) return;
        float progress = raw * raw * (3 - 2 * raw);
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(player.getSkin().texture()));
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0);
        float time = ageInTicks;
        renderFingers(pose, vertices, light, overlay, progress, time, getParentModel().rightArm, "right");
        renderFingers(pose, vertices, light, overlay, progress, time, getParentModel().leftArm, "left");
        renderCalf(pose, vertices, light, overlay, progress, root.getChild("right_calf"), -1.9F);
        renderCalf(pose, vertices, light, overlay, progress, root.getChild("left_calf"), 1.9F);
    }

    private void renderFingers(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                               float progress, float time, ModelPart arm, String side) {
        pose.pushPose();
        // Calque exactement le pivot et la rotation de l'avant-bras vanilla.
        arm.translateAndRotate(pose);
        float spacing = slim ? 0.68F : 0.83F;
        for (int i = 0; i < 5; i++) {
            pose.pushPose();
            pose.translate((i - 2) * spacing / 16.0F, 9.6F / 16.0F, 0);
            pose.scale(progress, progress, progress);
            ModelPart finger = root.getChild(side + "_finger_" + i);
            float wave = time * 0.67F + i * 1.28F + (side.equals("left") ? 0.87F : 0);
            finger.xRot = 0.29F * Mth.sin(wave);
            finger.yRot = 0.16F * Mth.cos(wave * 1.1F);
            finger.zRot = 0.33F * Mth.sin(wave * 0.91F);
            finger.render(pose, vertices, light, overlay);
            pose.popPose();
        }
        pose.popPose();
    }

    private void renderCalf(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                            float progress, ModelPart part, float x) {
        pose.pushPose();
        // Genoux approximativement au sol, pieds dirigés vers l'arrière.
        pose.translate(x / 16.0F, 21.3F / 16.0F, 0.35F / 16.0F);
        part.xRot = 1.34F;
        part.yRot = 0;
        part.zRot = 0;
        pose.scale(1, progress, progress);
        part.render(pose, vertices, light, overlay);
        pose.popPose();
    }
}
