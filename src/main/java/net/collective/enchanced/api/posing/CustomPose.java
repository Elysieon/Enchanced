package net.collective.enchanced.api.posing;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Hand;

import java.util.Optional;

public interface CustomPose {
    void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack itemStack, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light);
    default Optional<UseAction> overrideUseAction(ClientPlayerEntity clientPlayer, Hand activeHand, ItemStack itemStack) {return Optional.empty();}
}