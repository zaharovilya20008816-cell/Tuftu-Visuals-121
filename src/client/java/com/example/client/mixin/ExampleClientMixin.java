package com.example.mixin.client;

import com.example.ExampleModClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class ExampleClientMixin {

    @Inject(method = "applyEquipOffset", at = @At("TAIL"))
    private void modifySwordVisuals(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (ExampleModClient.swordVisuals && mc.player != null && arm == mc.player.getMainArm() && !mc.player.getMainHandStack().isEmpty()) {

            // 1. Позиция меча в руке
            if (ExampleModClient.posMode == 0) {
                matrices.translate(0.18F, -0.12F, -0.25F); // Далеко PvP
            } else if (ExampleModClient.posMode == 1) {
                matrices.translate(0.15F, -0.28F, -0.15F); // Низко
            } else if (ExampleModClient.posMode == 2) {
                matrices.translate(-0.08F, -0.10F, -0.20F); // По центру
            }

            // 2. Размер меча
            float[] scales = {0.60F, 0.75F, 0.90F, 1.15F};
            float s = scales[ExampleModClient.scaleMode];
            matrices.scale(s, s, s);

            // 3. Кастомная анимация удара мечом
            float swingProgress = mc.player.getHandSwingProgress(1.0F);
            if (swingProgress > 0.0F) {
                float angle = (float) Math.sin(swingProgress * Math.PI);
                if (ExampleModClient.swingMode == 0) { // Плавный PvP
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle * 35.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle * -20.0F));
                } else if (ExampleModClient.swingMode == 1) { // Жесткий Слэш
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle * -55.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle * 60.0F));
                } else if (ExampleModClient.swingMode == 2) { // Спин 360°
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swingProgress * 360.0F));
                }
            }
        }
    }
}
