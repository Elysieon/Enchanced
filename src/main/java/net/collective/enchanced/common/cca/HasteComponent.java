package net.collective.enchanced.common.cca;

import com.google.common.collect.ImmutableMultimap;
import net.collective.enchanced.Enchanced;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.ladysnake.cca.api.v3.component.ComponentKey;

import java.util.Objects;

public class HasteComponent extends SyncedLivingEntityComponent {
    // region Essentials
    public HasteComponent(LivingEntity livingEntity) {
        super(livingEntity);
    }

    @Override
    protected ComponentKey<? extends SyncedLivingEntityComponent> getComponentKey() {
        return ModEntityComponents.HASTE;
    }
    // endregion

    private float hasteMultiplier = 1;
    private float hasteTime = 0;

    @Override
    public void readData(ReadView readView) {
        this.hasteMultiplier = readView.getFloat("hasteMultiplier", 1);
        this.hasteTime = readView.getFloat("hasteTime", 0);

    }

    @Override
    public void writeData(WriteView writeView) {
        writeView.putFloat("hasteMultiplier", this.hasteMultiplier);
        writeView.putFloat("hasteTime", this.hasteTime);
    }



    public void addHaste() {
        this.hasteMultiplier = this.hasteMultiplier * 1.08f;
        this.hasteTime = 50;

        this.livingEntity().getAttributes().addTemporaryModifiers(ImmutableMultimap.<RegistryEntry<EntityAttribute>, EntityAttributeModifier>builder().put(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(Enchanced.geode.id("haste"), this.hasteMultiplier - 1, EntityAttributeModifier.Operation.ADD_VALUE)).build());

        this.sync();
    }

    public float getHasteMultiplier() {
        return this.hasteMultiplier;
    }


    @Override
    public void tick() {
        if (hasteTime > 0) {
            var hasteRegistry = this.world().getRegistryManager().getEntryOrThrow(EnchancedEnchantments.SCURRY.registryKey());
            int level = EnchantmentHelper.getLevel(hasteRegistry, Objects.requireNonNull(this.livingEntity().getActiveOrMainHandStack()));
            if (level == 0) {
                clearHaste();
            }
            hasteTime --;
            this.sync();
        }

        if (0 >= hasteTime ) clearHaste();
    }

    public void clearHaste() {
        this.hasteMultiplier = 1;
        this.hasteTime = 0;

        this.livingEntity().getAttributes().removeModifiers((ImmutableMultimap.<RegistryEntry<EntityAttribute>, EntityAttributeModifier>builder().put(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(Enchanced.geode.id("haste"), this.hasteMultiplier, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE)).build()));

        this.sync();
    }
}