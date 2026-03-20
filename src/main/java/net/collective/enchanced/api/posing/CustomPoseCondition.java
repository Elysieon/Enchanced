package net.collective.enchanced.api.posing;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@FunctionalInterface
public interface CustomPoseCondition {
    PoseResult validate(AbstractClientPlayerEntity player, Hand hand, ItemStack itemStack);

    enum PoseResult {
        CONTINUE,
        APPLY,
        APPLY_CONTINUE
    }
}
