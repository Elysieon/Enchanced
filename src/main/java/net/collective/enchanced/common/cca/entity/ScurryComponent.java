package net.collective.enchanced.common.cca.entity;

import com.google.common.collect.ImmutableMultimap;
import net.collective.enchanced.Enchanced;
import net.collective.enchanced.common.cca.SyncedLivingEntityComponent;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collectively.geode.math.math;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;

public class ScurryComponent extends SyncedLivingEntityComponent {
    public static final Identifier ATTRIBUTE_ID = Enchanced.geode.id("scurry");

    private static final String MULTIPLIER_KEY = "multiplier";
    private static final double HIT_SPEED_MULTIPLIER = 1.12;
    private double multiplier = 1;

    private static final String DURATION_KEY = "duration";
    private static final int HIT_SPEED_DURATION = 75;
    private int duration = 0;

    public ScurryComponent(LivingEntity livingEntity) {
        super(livingEntity);
    }

    @Override
    protected ComponentKey<? extends SyncedLivingEntityComponent> getComponentKey() {
        return ModEntityComponents.SCURRY;
    }

    @Override
    public void readData(ReadView readView) {
        this.multiplier = readView.getDouble(MULTIPLIER_KEY, 1);
        this.duration = readView.getInt(DURATION_KEY, 0);
    }

    @Override
    public void writeData(WriteView writeView) {
        writeView.putDouble(MULTIPLIER_KEY, this.multiplier);
        writeView.putInt(DURATION_KEY, this.duration);
    }

    public void increaseScurry() {
        this.multiplier *= HIT_SPEED_MULTIPLIER;
        this.duration = HIT_SPEED_DURATION;

        this.livingEntity().getAttributes().addTemporaryModifiers(ImmutableMultimap.<RegistryEntry<EntityAttribute>, EntityAttributeModifier>builder()
                .put(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                ATTRIBUTE_ID,
                                this.multiplier - 1,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        )
                )
                .build()
        );

        this.sync();
    }

    public void clearScurry() {
        this.multiplier = 1;
        this.duration = 0;

        this.livingEntity().getAttributes().removeModifiers(ImmutableMultimap.<RegistryEntry<EntityAttribute>, EntityAttributeModifier>builder()
                .put(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                ATTRIBUTE_ID,
                                this.multiplier,
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        )
                )
                .build()
        );

        this.sync();
    }

    public double multiplier() {
        return this.multiplier;
    }

    public int tridentChargeUp() {
        return (int) math.round(this.multiplier * 10);
    }

    @Override
    public void tick() {
        if (duration > 0 && !this.livingEntity().isUsingItem()) {
            ItemStack activeStack = livingEntity().getActiveOrMainHandStack();

            if (activeStack.isEmpty() || !EnchancedEnchantments.hasEnchantment(world().getRegistryManager(), activeStack, EnchancedEnchantments.SCURRY)) {
                clearScurry();
            }

            duration--;
            sync();
        }

        if (duration <= 0) {
            clearScurry();
        }
    }
}