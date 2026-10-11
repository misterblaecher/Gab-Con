package fr.serveurminecraft.gabconget.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fr.serveurminecraft.gabconget.client.ClientGetState;

/**
 * GET : les pivots sont remis à zéro AVANT l'animation vanilla pour éviter
 * que le déplacement de la pose précédente ne s'accumule en sortie de GET.
 * Les couches de vêtements héritent ensuite de ces rotations via PlayerModel.
 */
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {
    @Shadow public ModelPart head;
    @Shadow public ModelPart hat;
    @Shadow public ModelPart body;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightLeg;
    @Shadow public ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    private void gabconget$resetPivot(T entity, float walkPos, float walkSpeed,
                                     float age, float yaw, float pitch, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayer)) return;
        head.setPos(0, 0, 0);
        hat.setPos(0, 0, 0);
        body.setPos(0, 0, 0);
        rightArm.setPos(-5, 2, 0);
        leftArm.setPos(5, 2, 0);
        rightLeg.setPos(-1.9F, 12, 0);
        leftLeg.setPos(1.9F, 12, 0);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void gabconget$apply(T entity, float walkPos, float walkSpeed,
                                 float age, float yaw, float pitch, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayer player)) return;
        float raw = ClientGetState.progress(player, age - player.tickCount);
        if (raw <= 0) return;
        // Ease-in-out : évite un arrêt brutal aux extrémités de l'animation.
        float t = raw * raw * (3 - 2 * raw);

        // Buste presque horizontal ; hanche en hauteur, nez proche du sol.
        body.xRot = Mth.lerp(t, body.xRot, 1.38F);
        body.y = Mth.lerp(t, body.y, 8.0F);
        body.z = Mth.lerp(t, body.z, -2.0F);
        head.xRot = Mth.lerp(t, head.xRot, 0.65F);
        head.yRot = Mth.lerp(t, head.yRot, 0);
        head.y = Mth.lerp(t, head.y, 20.0F);
        head.z = Mth.lerp(t, head.z, -10.0F);
        hat.copyFrom(head);

        // Bras tendus vers l'arrière et légèrement vers le haut.
        rightArm.xRot = Mth.lerp(t, rightArm.xRot, 1.91F);
        leftArm.xRot = Mth.lerp(t, leftArm.xRot, 1.91F);
        rightArm.zRot = Mth.lerp(t, rightArm.zRot, -0.18F);
        leftArm.zRot = Mth.lerp(t, leftArm.zRot, 0.18F);
        rightArm.y = Mth.lerp(t, rightArm.y, 10.0F);
        leftArm.y = Mth.lerp(t, leftArm.y, 10.0F);
        rightArm.z = Mth.lerp(t, rightArm.z, -2.0F);
        leftArm.z = Mth.lerp(t, leftArm.z, -2.0F);

        // Cuisses vers l'avant, les mollets pliés sont ajoutés par GetDetailLayer.
        rightLeg.xRot = Mth.lerp(t, rightLeg.xRot, -0.66F);
        leftLeg.xRot = Mth.lerp(t, leftLeg.xRot, -0.66F);
        rightLeg.y = Mth.lerp(t, rightLeg.y, 12.0F);
        leftLeg.y = Mth.lerp(t, leftLeg.y, 12.0F);
        rightLeg.z = Mth.lerp(t, rightLeg.z, 7.5F);
        leftLeg.z = Mth.lerp(t, leftLeg.z, 7.5F);
    }
}
