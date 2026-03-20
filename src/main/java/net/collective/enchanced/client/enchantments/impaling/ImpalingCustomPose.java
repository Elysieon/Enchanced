package net.collective.enchanced.client.enchantments.impaling;

import net.collective.enchanced.api.posing.CustomPose;
import net.collective.enchanced.api.posing.CustomPoseCondition;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.util.EnchantUtils;
import net.collectively.geode.helpers.RenderHelper;
import net.collectively.geode.math.math;
import net.collectively.geode.types.double3;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;

public class ImpalingCustomPose implements CustomPose {
    public static CustomPoseCondition.PoseResult validate(AbstractClientPlayerEntity player, Hand hand, ItemStack itemStack) {
        return EnchantUtils.hasEnchantment(player, itemStack, EnchancedEnchantments.IMPALING) && player.isUsingItem() && player.getActiveOrMainHandStack() == itemStack
                ? CustomPoseCondition.PoseResult.APPLY
                : CustomPoseCondition.PoseResult.CONTINUE;
    }

    @Override
    public Optional<UseAction> overrideUseAction(ClientPlayerEntity clientPlayer, Hand activeHand, ItemStack itemStack) {
        return Optional.of(UseAction.TRIDENT);
    }

    @Override
    public void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack itemStack, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light) {
        double side = hand == Hand.MAIN_HAND ? 1 : -1;
        double3 position = new double3(0.2 * side, -0.4, 0);

        matrices.translate(position.x(), position.y(), position.z());
        matrices.multiply(RenderHelper.rotationDeg(new double3(-10, 0, 10 * side)));
    }
}
