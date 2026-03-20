package net.collective.enchanced.common.mixin.enchantment_table;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import moriyashiine.enchancement.common.screenhandler.EnchantingTableScreenHandler;
import net.collective.enchanced.common.util.EnchantUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

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
}
