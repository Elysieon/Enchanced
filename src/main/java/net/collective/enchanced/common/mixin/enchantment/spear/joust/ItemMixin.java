package net.collective.enchanced.common.mixin.enchantment.spear.joust;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.collective.enchanced.common.cca.entity.JoustComponent;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.util.EnchantUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin {
    @WrapMethod(method = "getUseAction")
    private UseAction joust$getUseAction(ItemStack stack, Operation<UseAction> original) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld clientWorld = client.world;

        if (clientWorld == null) {
            return original.call(stack);
        }

        if (EnchantUtils.hasEnchantment(clientWorld, stack, EnchancedEnchantments.JOUST)) {
            ClientPlayerEntity player = client.player;

            if (player != null) {
                JoustComponent component = ModEntityComponents.JOUST.get(player);
                if (component.isJousting()) {
                    return UseAction.SPEAR;
                }
            }

            return UseAction.TRIDENT;
        }

        return original.call(stack);
    }

    @WrapMethod(method = "onStoppedUsing")
    private boolean joust$onStopUsing(ItemStack itemStack, World world, LivingEntity user, int remainingUseTicks, Operation<Boolean> original) {
        if (user instanceof PlayerEntity player) {
            if (EnchantUtils.hasEnchantment(player, itemStack, EnchancedEnchantments.JOUST)) {
                JoustComponent component = ModEntityComponents.JOUST.get(player);
                component.onStopUsing(itemStack, remainingUseTicks);
                return true;
            }
        }

        return original.call(itemStack, world, user, remainingUseTicks);
    }
}
