package net.collective.enchanced.client.render.enchantments.pose.joust;

import net.collective.enchanced.common.cca.entity.JoustComponent;
import net.collectively.geode.debug.Draw;
import net.collectively.geode.helpers.MatrixStackHelper;
import net.collectively.geode.helpers.RenderHelper;
import net.collectively.geode.math.math;
import net.collectively.geode.types.double3;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public interface Jousting {
    static void renderFirstPerson(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack itemStack, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light) {
        if (player.isUsingItem()) {
            double side = hand == Hand.MAIN_HAND ? 1 : -1;
            double3 startPosition = new double3(0.2 * side, -0.3, -0.65);
            double3 endPosition = new double3(0.2 * side, -0.7, -0.5);

            double progress = math.clamp01((72000 - player.getItemUseTimeLeft()) / (double) JoustComponent.MAX_CHARGE_TIME);
            double3 position = startPosition.lerp(progress, endPosition);

            // Draw.text(Math.round(progress * 100f) / 100f, new double3(player.getEntityPos()).add(player.getRotationVector()).add(0, 1.2, 0));

            matrices.translate(position.x(), position.y(), position.z());
            matrices.multiply(RenderHelper.rotationDeg(new double3(-4, 0, 10 * side)));

            double wobbleAmount = math.lerp(progress, 0,0.005);
            double speed = player.getVelocity().length() * 6;
            wobbleAmount *= math.max(1, speed);

            matrices.translate(
                    MathHelper.nextDouble(player.getRandom(), -wobbleAmount, wobbleAmount),
                    MathHelper.nextDouble(player.getRandom(), -wobbleAmount, wobbleAmount),
                    MathHelper.nextDouble(player.getRandom(), -wobbleAmount, wobbleAmount)
            );
        }
    }
}
