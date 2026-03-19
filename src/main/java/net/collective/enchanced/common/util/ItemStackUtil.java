package net.collective.enchanced.common.util;

import net.minecraft.item.ItemStack;

public interface ItemStackUtil {
    static boolean isNullOrEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.isEmpty();
    }
}
