package net.collective.enchanced.common.mixin.anvil;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "appendTooltip", at = @At("HEAD"))
    private void enchanced$anvil(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type, CallbackInfo ci) {
        if (stack.isIn(ItemTags.ANVIL)) {
            textConsumer.accept(Text.literal("Anvils are temporarily disabled, as they are being reworked.").formatted(Formatting.RED));
            textConsumer.accept(Text.literal("Renaming items have been moved to nametags.").formatted(Formatting.GRAY).formatted(Formatting.ITALIC));
        }
    }
}
