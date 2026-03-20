package net.collective.enchanced.common.mixin.enchantment.bow.multishot;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.util.EnchantUtils;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BowItem.class)
public class BowItemMixin {
    @ModifyExpressionValue(
            method = "onStoppedUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/BowItem;load(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)Ljava/util/List;"
            )
    )
    private List<ItemStack> enchanced$onStoppedUsing(List<ItemStack> original, ItemStack stack, World world) {
        if (!EnchantUtils.hasEnchantment(world, stack, EnchancedEnchantments.MULTISHOT)) {
            return original;
        }

        original.add(original.getFirst());
        original.add(original.getFirst());
        return original;
    }
}
