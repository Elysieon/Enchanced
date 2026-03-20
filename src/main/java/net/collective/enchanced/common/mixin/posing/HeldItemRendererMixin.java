package net.collective.enchanced.common.mixin.posing;

import net.collective.enchanced.api.posing.CustomPose;
import net.collective.enchanced.api.posing.CustomPoseCondition;
import net.collective.enchanced.client.init.ModCustomPoses;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V"
            )
    )
    private void customPose$renderFirstPersonItem$before(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack itemStack, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, CallbackInfo ci) {
        matrices.push();

        for (Map.Entry<CustomPoseCondition, CustomPose> pair : ModCustomPoses.REGISTRY.entrySet()) {
            CustomPoseCondition.PoseResult result = pair.getKey().validate(player, hand, itemStack);

            if (result == CustomPoseCondition.PoseResult.CONTINUE) {
                continue;
            }

            pair.getValue().renderFirstPersonItem(player, tickProgress, pitch, hand, swingProgress, itemStack, equipProgress, matrices, orderedRenderCommandQueue, light);

            if (result != CustomPoseCondition.PoseResult.APPLY_CONTINUE) {
                break;
            }
        }
    }

    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V",
                    shift = At.Shift.AFTER
            )
    )
    private void customPose$renderFirstPersonItem$after(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack itemStack, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, CallbackInfo ci) {
        matrices.pop();
    }
}
