package net.collective.enchanced.common.mixin.rebalanced.enchanting;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import moriyashiine.enchancement.common.util.EnchancementUtil;
import net.collective.enchanced.api.debugging.DebugMessages;
import net.collective.enchanced.common.util.ReflectionHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneScreenHandlerMixin {
    @WrapOperation(
            method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/GrindstoneScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;",
                    ordinal = 2
            )
    )
    private Slot enchanced$changeExperienceAmount(GrindstoneScreenHandler instance, Slot slot, Operation<Slot> original) {
        try {
            Inventory result = ReflectionHelper.getFieldValue(GrindstoneScreenHandler.class, instance, "result");
            Inventory input = ReflectionHelper.getFieldValue(GrindstoneScreenHandler.class, instance, "input");
            ScreenHandlerContext context = ReflectionHelper.getFieldValue(GrindstoneScreenHandler.class, instance, "context");

            return original.call(instance, new Slot(result, 2, 129, 34) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false;
                }

                @Override
                public void onTakeItem(PlayerEntity player, ItemStack stack) {
                    context.run((world, pos) -> {
                        if (world instanceof ServerWorld) {
                            ExperienceOrbEntity.spawn((ServerWorld) world, Vec3d.ofCenter(pos), this.getExperience(world));
                        }

                        world.syncWorldEvent(WorldEvents.GRINDSTONE_USED, pos, 0);
                    });

                    input.setStack(0, ItemStack.EMPTY);
                    input.setStack(1, ItemStack.EMPTY);
                }

                private int getExperience(World world) {
                    int i = 0;
                    i += this.getExperience(input.getStack(0));
                    i += this.getExperience(input.getStack(1));
                    if (i > 0) {
                        int j = (int) Math.ceil(i / 2.0);
                        return j + world.random.nextInt(j);
                    } else {
                        return 0;
                    }
                }

                private int getExperience(ItemStack stack) {
                    double cost = MathHelper.floor(60.0 / (float) Math.max(1, EnchancementUtil.getEnchantmentValue(stack)));
                    int count = stack.getEnchantments().getEnchantments().size();

                    if (stack.contains(DataComponentTypes.STORED_ENCHANTMENTS)) {
                        count += stack.get(DataComponentTypes.STORED_ENCHANTMENTS).getEnchantments().size();
                    }

                    return (int) (cost * count * 4d * (stack.isOf(Items.ENCHANTED_BOOK) ? 0.5 : 0.33));
                }
            });
        } catch (Exception exception) {
            //noinspection CallToPrintStackTrace
            exception.printStackTrace();
        }

        return original.call(instance, slot);
    }
}
