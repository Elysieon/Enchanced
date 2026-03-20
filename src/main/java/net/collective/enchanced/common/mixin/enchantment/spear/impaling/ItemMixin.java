package net.collective.enchanced.common.mixin.enchantment.spear.impaling;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.collective.enchanced.common.cca.entity.ImpalingComponent;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.util.EnchantUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin {
    @WrapMethod(method = "onStoppedUsing")
    private boolean impaling$onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, Operation<Boolean> original) {
        if (!EnchantUtils.hasEnchantment(user, stack, EnchancedEnchantments.IMPALING)) {
            return original.call(stack, world, user, remainingUseTicks);
        }

        int useDuration = 72000 - remainingUseTicks;
        if (useDuration > 10) {
            if (user instanceof PlayerEntity player) {
                ImpalingComponent component = player.getComponent(ModEntityComponents.IMPALING);
                component.onStoppedUsing(stack, useDuration);
                return true;
            }
        }

        return original.call(stack, world, user, remainingUseTicks);
    }
}
