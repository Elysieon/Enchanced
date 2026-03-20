package net.collective.enchanced.common.mixin.enchantment.spear.joust;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.util.EnchantUtils;
import net.minecraft.component.type.KineticWeaponComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KineticWeaponComponent.class)
public class KineticWeaponComponentMixin {
    @WrapMethod(method = "usageTick")
    private void joust$usageTick(ItemStack itemStack, int remainingUseTicks, LivingEntity user, EquipmentSlot slot, Operation<Void> original) {
        if (!EnchantUtils.hasEnchantment(user, itemStack, EnchancedEnchantments.JOUST)) {
            original.call(itemStack, remainingUseTicks, user, slot);
            return;
        }
    }
}
