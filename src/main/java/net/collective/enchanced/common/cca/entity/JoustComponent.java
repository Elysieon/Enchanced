package net.collective.enchanced.common.cca.entity;

import com.llamalad7.mixinextras.utils.MixinAPInternals;
import moriyashiine.strawberrylib.api.module.SLibUtils;
import net.collective.enchanced.common.cca.SynedPlayerEntityComponent;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.util.SpearUtil;
import net.collectively.geode.math.math;
import net.collectively.geode.types.double3;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.KineticWeaponComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.ladysnake.cca.api.v3.component.ComponentKey;

public class JoustComponent extends SynedPlayerEntityComponent {
    public static final int MAX_USE_TIME = 72000;
    public static final int MAX_CHARGE_TIME = 3 * 20;

    private static final String SPEAR_TICKS_KEY = "spear_ticks";
    private int spearTicks;

    private static final String SPEAR_HAND_KEY = "spear_hand";
    private Hand spearHand = Hand.MAIN_HAND;

    private static final String SPEAR_STACK_KEY = "spear_stack";
    private ItemStack spearStack;

    public JoustComponent(PlayerEntity playerEntity) {
        super(playerEntity);
    }

    @Override
    protected ComponentKey<? extends SynedPlayerEntityComponent> getComponentKey() {
        return ModEntityComponents.JOUST;
    }

    @Override
    public void tick() {
        // player().sendMessage(Text.literal(String.valueOf(spearTicks)), false);

        if (spearTicks > 0) {
            spearTicks--;
            sync();

            ItemStack itemStack = player().getStackInHand(spearHand);
            if (!ItemStack.areItemsAndComponentsEqual(itemStack, spearStack)) {
                spearTicks = 0;
                sync();
                return;
            }

            if (!itemStack.isEmpty()) {
                SpearUtil.attack(player(), 0.75f, this::sync);
            }
        }
    }

    public boolean isJousting() {
        return spearTicks > 0;
    }

    @Override
    public void readData(ReadView readView) {
        spearTicks = readView.getInt(SPEAR_TICKS_KEY, 0);
        spearHand = readView.getOptionalString(SPEAR_HAND_KEY).map(Hand::valueOf).orElse(Hand.MAIN_HAND);
        spearStack = readView.read(SPEAR_STACK_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public void writeData(WriteView writeView) {
        writeView.putInt(SPEAR_TICKS_KEY, spearTicks);
        writeView.putString(SPEAR_HAND_KEY, spearHand.name());
        if (spearStack != null) writeView.put(SPEAR_STACK_KEY, ItemStack.CODEC, spearStack);
    }

    public void onStopUsing(ItemStack itemStack, int remainingUseTicks) {
        if (isJousting()) {
            return;
        }

        int usageTicks = MAX_USE_TIME - remainingUseTicks;
        double progress = usageTicks / (double) MAX_CHARGE_TIME;
        double speedMultiplier = math.lerp(progress, 0.85, 1.75);

        if (progress >= 1) {
            speedMultiplier = 2.5;
        }

        double3 velocity = new double3(player().getVelocity());

        if (velocity.squaredMag() <= 0.05) {
            velocity = new double3(player().getRotationVector().multiply(0.5));
        }

        velocity = velocity.mul(speedMultiplier, 1, speedMultiplier);

        if (velocity.y() < 0.1) {
            velocity = velocity.withY(0.1);
        }

        player().setVelocity(velocity.toVec3d());

        spearTicks = 10;

        if (ItemStack.areItemsAndComponentsEqual(itemStack, player().getOffHandStack())) spearHand = Hand.OFF_HAND;
        else spearHand = Hand.MAIN_HAND;

        spearStack = itemStack;

        sync();

        SLibUtils.playSound(player(), SoundEvents.ITEM_SPEAR_LUNGE_1.value());
        SLibUtils.playSound(player(), SoundEvents.ITEM_SPEAR_WOOD_HIT.value());
    }
}
