package net.collective.enchanced.common.mixin.enchantment_table;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moriyashiine.enchancement.client.payload.SyncBookshelvesPayload;
import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.screenhandler.EnchantingTableScreenHandler;
import moriyashiine.enchancement.common.util.config.OverhaulMode;
import net.collective.enchanced.api.debugging.DebugMessages;
import net.collective.enchanced.common.util.EnchantUtils;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.item.EnchantmentsPredicate;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("CallToPrintStackTrace")
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

    @Shadow
    @Final
    public Set<RegistryEntry<Enchantment>> chiseledEnchantments;

    @Shadow
    private int bookshelfCount;

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
                    return testEnchantingMaterial.test(stack) || canAcceptBook(stack);
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
        return original || canAcceptBook(stackInSlot);
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
            if (!canAcceptBook(materialStack)) {
                this.inventory.markDirty();
                onContentChanged(inventory);
                player.getInventory().offerOrDrop(handler.slots.get(2).getStack().copyAndEmpty());
            }
        }
    }

    @Unique
    private boolean canAcceptBook(ItemStack bookStack) {
        if (!bookStack.isOf(Items.ENCHANTED_BOOK) || selectedEnchantments.isEmpty()) {
            return false;
        }

        //noinspection DataFlowIssue
        Set<RegistryEntry<Enchantment>> storedEnchantments = bookStack.get(DataComponentTypes.STORED_ENCHANTMENTS).getEnchantments();
        return storedEnchantments.containsAll(selectedEnchantments);
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

    @Unique
    private static final int BOOKSHELF_HORIZONTAL_DETECTION_RADIUS = 6;
    @Unique
    private static final int BOOKSHELF_VERTICAL_DETECTION_RADIUS = 6;

    @WrapMethod(method = "lambda$collectBookshelves$5")
    private void collectBookshelves$collectBookshelvesFromFurtherAway(ServerPlayerEntity player,
                                                                      World world,
                                                                      BlockPos pos,
                                                                      Operation<Void> original) {
        this.chiseledEnchantments.clear();
        this.bookshelfCount = 0;

        for (int x = -BOOKSHELF_HORIZONTAL_DETECTION_RADIUS; x <= BOOKSHELF_HORIZONTAL_DETECTION_RADIUS; x++) {
            for (int z = -BOOKSHELF_HORIZONTAL_DETECTION_RADIUS; z <= BOOKSHELF_HORIZONTAL_DETECTION_RADIUS; z++) {
                for (int y = 0; y <= BOOKSHELF_VERTICAL_DETECTION_RADIUS; y++) {
                    if (EnchantUtils.isVisiblePowerSource(world, pos, pos.add(x,y,z), player)) {
                        if (world.getBlockEntity(pos.add(x,y,z)) instanceof ChiseledBookshelfBlockEntity chiseledBookshelf) {
                            this.bookshelfCount += chiseledBookshelf.getFilledSlotCount() / 3;

                            if (ModConfig.overhaulEnchanting == OverhaulMode.CHISELED && !player.isCreative()) {
                                for (ItemStack stack : chiseledBookshelf) {
                                    if (stack.contains(DataComponentTypes.STORED_ENCHANTMENTS)) {
                                        ItemEnchantmentsComponent component = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
                                        if (component != null) {
                                            chiseledEnchantments.addAll(component.getEnchantments());
                                        }
                                    }

                                    if (stack.contains(DataComponentTypes.ENCHANTMENTS)) {
                                        ItemEnchantmentsComponent component = stack.get(DataComponentTypes.ENCHANTMENTS);
                                        if (component != null) {
                                            chiseledEnchantments.addAll(component.getEnchantments());
                                        }
                                    }
                                }
                            }

                            continue;
                        }

                        bookshelfCount++;
                    }
                }
            }
        }

        this.bookshelfCount = Math.min(15, this.bookshelfCount);

        if (ModConfig.overhaulEnchanting == OverhaulMode.CHISELED && player.isCreative()) {
            Registry<Enchantment> enchantmentRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            enchantmentRegistry.forEach((enchantment) -> this.chiseledEnchantments.add(enchantmentRegistry.getEntry(enchantment)));
        }

        SyncBookshelvesPayload.send(player, this.chiseledEnchantments, this.bookshelfCount);
    }
}
