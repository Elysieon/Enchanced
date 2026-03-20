package net.collective.enchanced.common.mixin.enchantment_table;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moriyashiine.enchancement.common.screenhandler.EnchantingTableScreenHandler;
import net.collective.enchanced.common.util.EnchantUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(EnchantingTableScreenHandler.class)
public abstract class EnchantingTableScreenHandlerMixin {
    @Shadow
    public int viewIndex;

    @Shadow
    private ItemStack enchantingStack;

    @Shadow
    @Final
    public List<RegistryEntry.Reference<Enchantment>> validEnchantments;

    @Shadow
    protected abstract List<RegistryEntry.Reference<Enchantment>> getAllEnchantments();

    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private Inventory inventory;

    @Shadow
    protected abstract EnchantingTableScreenHandler.EnchantingMaterial getEnchantingMaterial(ItemStack stack);

    @Shadow
    @Final
    public List<RegistryEntry<Enchantment>> selectedEnchantments;

    @Shadow
    private int cost;

    @Shadow
    public abstract void onContentChanged(Inventory inventory);

    @WrapMethod(method = "getEnchantmentFromViewIndex")
    public RegistryEntry<Enchantment> enchanced$getEnchantmentFromViewIndex(int index, Operation<RegistryEntry<Enchantment>> original) {
        if (world == null) {
            return original.call(index);
        }

        List<RegistryEntry.Reference<Enchantment>> allEnchantments = EnchantUtils.getAllEnchantmentsForStack(world.getRegistryManager(), validEnchantments::contains, enchantingStack);
        return allEnchantments.size() <= 4 ? allEnchantments.get(index) : allEnchantments.get((index + this.viewIndex) % allEnchantments.size());
    }

    @WrapMethod(method = "updateViewIndex")
    public void enchanced$updateViewIndex(boolean up, Operation<Void> original) {
        if (world == null) {
            original.call(up);
            return;
        }

        List<RegistryEntry.Reference<Enchantment>> allEnchantments = EnchantUtils.getAllEnchantmentsForStack(world.getRegistryManager(), validEnchantments::contains, enchantingStack);

        this.viewIndex = (this.viewIndex + (up ? -1 : 1)) % allEnchantments.size();
        if (this.viewIndex < 0) {
            this.viewIndex += allEnchantments.size();
        }
    }

    @WrapOperation(
            method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;Lnet/minecraft/world/World;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmoriyashiine/enchancement/common/screenhandler/EnchantingTableScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;",
                    ordinal = 2
            )
    )
    private Slot enchanced$addSlot$modifyEnchantingCost(EnchantingTableScreenHandler instance, Slot slot, Operation<Slot> original) {
        EnchantingTableScreenHandler handler = (EnchantingTableScreenHandler) (Object) this;

        try {
            var addSlotMethod = ScreenHandler.class.getDeclaredMethod("addSlot", Slot.class);
            addSlotMethod.setAccessible(true);

            Predicate<ItemStack> testEnchantingMaterial = stack -> getEnchantingMaterial(handler.slots.getFirst().getStack()).test(stack);

            return (Slot) addSlotMethod.invoke(handler, new Slot(inventory, 2, 25, 51) {
                public boolean canInsert(ItemStack stack) {
                    return testEnchantingMaterial.test(stack) || canAcceptBook(stack, null);
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return original.call(instance, slot);
    }

    @ModifyExpressionValue(
            method = "quickMove",
            at = @At(
                    value = "INVOKE",
                    target = "Lmoriyashiine/enchancement/common/screenhandler/EnchantingTableScreenHandler$EnchantingMaterial;test(Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private boolean enchanced$quickMoveAcceptBooks(boolean original, PlayerEntity player, int index, @Local(name = "stackInSlot") ItemStack stackInSlot) {
        return original || canAcceptBook(stackInSlot, player);
    }

    @Inject(
            method = "onButtonClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lmoriyashiine/enchancement/common/screenhandler/EnchantingTableScreenHandler;getCost(Lnet/minecraft/item/ItemStack;)I"
            )
    )
    private void enchanced$updateSelectedEnchantmentsUpdatesBookMaterial(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        EnchantingTableScreenHandler handler = (EnchantingTableScreenHandler) (Object) this;
        ItemStack materialStack = handler.slots.get(2).getStack();

        if (materialStack.isOf(Items.ENCHANTED_BOOK)) {
            if (!canAcceptBook(materialStack, player)) {
                this.inventory.markDirty();
                onContentChanged(inventory);
                player.getInventory().offerOrDrop(handler.slots.get(2).getStack().copyAndEmpty());
            }
        }
    }

    @Unique
    private boolean canAcceptBook(ItemStack bookStack, @Nullable PlayerEntity player) {
        if (!bookStack.isOf(Items.ENCHANTED_BOOK) || selectedEnchantments.isEmpty()) {
            if (player != null) {
                if (!bookStack.isOf(Items.ENCHANTED_BOOK)) {
                    // player.sendMessage(Text.literal("Cannot insert book: " + bookStack + " is not an enchanted book!").formatted(Formatting.RED), false);
                } else {
                    // player.sendMessage(Text.literal("Cannot insert book: no enchantment selected!").formatted(Formatting.RED), false);
                }
            }

            return false;
        }

        var storedEnchantments = bookStack.get(DataComponentTypes.STORED_ENCHANTMENTS).getEnchantments();
        boolean hasAllEnchantments = storedEnchantments.containsAll(selectedEnchantments);

        if (!hasAllEnchantments && player != null) {
            // player.sendMessage(Text.literal("Cannot insert book " + bookStack + ": Missing enchantments!").formatted(Formatting.RED), false);
            // player.sendMessage(Text.literal("- Book enchantments: " + storedEnchantments.stream().map(x -> ", " + x.value()).collect(Collectors.joining())).formatted(Formatting.RED), false);
            // player.sendMessage(Text.literal("- Selected enchantments: " + selectedEnchantments.stream().map(x -> ", " + x.value()).collect(Collectors.joining())).formatted(Formatting.RED), false);
        }

        return hasAllEnchantments;
    }

    @ModifyExpressionValue(
            method = "canEnchant",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getCount()I"
            )
    )
    private int enchanced$changeEnchantCostWithBooks(int original) {
        EnchantingTableScreenHandler handler = (EnchantingTableScreenHandler) (Object) this;
        ItemStack slotStack = handler.slots.get(2).getStack();

        if (slotStack.isOf(Items.ENCHANTED_BOOK)) {
            return cost;
        }

        return original;
    }

    @Inject(
            method = "lambda$onButtonClick$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;",
                    ordinal = 0
            )
    )
    private void enchanced$setCostTo1IfEnchantedBook(PlayerEntity player, World world, BlockPos pos, CallbackInfo ci) {
        EnchantingTableScreenHandler handler = (EnchantingTableScreenHandler) (Object) this;
        if (handler.slots.get(2).getStack().isOf(Items.ENCHANTED_BOOK)) {
            cost = 1;
        }
    }

    @ModifyReturnValue(
            method = "getCost()I",
            at = @At("RETURN")
    )
    private int enchanced$modifyCostForEnchantedBookMaterial(int original) {
        EnchantingTableScreenHandler handler = (EnchantingTableScreenHandler) (Object) this;
        if (handler.slots.get(2).getStack().isOf(Items.ENCHANTED_BOOK)) {
            return 1;
        }

        return original;
    }
}
