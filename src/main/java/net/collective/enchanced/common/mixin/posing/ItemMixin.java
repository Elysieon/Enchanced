package net.collective.enchanced.common.mixin.posing;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.collective.enchanced.api.posing.CustomPose;
import net.collective.enchanced.api.posing.CustomPoseCondition;
import net.collective.enchanced.client.init.ModCustomPoses;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(Item.class)
public class ItemMixin {
    @WrapMethod(method = "getUseAction")
    private UseAction pose$getUseAction(ItemStack itemStack, Operation<UseAction> original) {
        UseAction currentUseAction = original.call(itemStack);
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayer = client.player;

        if (clientPlayer == null) {
            return currentUseAction;
        }

        for (Map.Entry<CustomPoseCondition, CustomPose> pair : ModCustomPoses.REGISTRY.entrySet()) {
            CustomPoseCondition.PoseResult result = pair.getKey().validate(clientPlayer, clientPlayer.getActiveHand(), itemStack);

            if (result == CustomPoseCondition.PoseResult.CONTINUE) {
                continue;
            }

            currentUseAction = pair.getValue().overrideUseAction(clientPlayer, clientPlayer.getActiveHand(), itemStack).orElse(currentUseAction);

            if (result != CustomPoseCondition.PoseResult.APPLY_CONTINUE) {
                break;
            }
        }

        return currentUseAction;
    }
}
