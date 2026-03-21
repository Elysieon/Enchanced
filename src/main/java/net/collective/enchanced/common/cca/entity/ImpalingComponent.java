package net.collective.enchanced.common.cca.entity;

import moriyashiine.strawberrylib.api.module.SLibRegistries;
import moriyashiine.strawberrylib.api.module.SLibUtils;
import net.collective.enchanced.common.cca.SynedPlayerEntityComponent;
import net.collective.enchanced.common.entity.ThrownSpearEntity;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.payload.ThrownSpearSyncS2CPayload;
import net.collectively.geode.math.math;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Hand;
import org.ladysnake.cca.api.v3.component.ComponentKey;

public class ImpalingComponent extends SynedPlayerEntityComponent {
    private static final String SAVED_SLOT_INDEX_KEY = "saved_slot_index";
    private int savedSlotIndex = -1;

    public ImpalingComponent(PlayerEntity playerEntity) {
        super(playerEntity);
    }

    @Override
    protected ComponentKey<? extends SynedPlayerEntityComponent> getComponentKey() {
        return ModEntityComponents.IMPALING;
    }

    @Override
    public void readData(ReadView readView) {
        savedSlotIndex = readView.getInt(SAVED_SLOT_INDEX_KEY, -1);
    }

    @Override
    public void writeData(WriteView writeView) {
        writeView.putInt(SAVED_SLOT_INDEX_KEY, savedSlotIndex);
    }

    @Override
    public void tick() {
    }

    public void onStoppedUsing(ItemStack itemStack, int useDuration) {
        PlayerInventory inventory = player().getInventory();
        savedSlotIndex = inventory.getSlotWithStack(itemStack);

        if (ItemStack.areItemsAndComponentsEqual(player().getOffHandStack(), itemStack)) {
            savedSlotIndex = -67;
        }

        if (world() instanceof ServerWorld serverWorld) {
            float power = math.clamp(useDuration / 10f, 2, 3);

            ThrownSpearEntity entity = ProjectileEntity.spawnWithVelocity(
                    (world, shooter, stack) -> new ThrownSpearEntity(shooter, world, stack),
                    serverWorld,
                    itemStack,
                    player(),
                    0, power, 0
            );

            entity.setRenderedItemStack(itemStack);

            if (player() instanceof ServerPlayerEntity serverPlayerEntity) {
                ServerPlayNetworking.send(serverPlayerEntity, ThrownSpearSyncS2CPayload.of(entity));
            }
        }

        world().playSound(null, player().getX(), player().getY(), player().getZ(), SoundEvents.ITEM_TRIDENT_THROW.value(), SoundCategory.PLAYERS, 1, math.lerp(math.clamp01((useDuration - 10f) / 30f), 0.825f, 0.9f));

        itemStack.decrement(1);
        sync();
    }

    public void pickup(ThrownSpearEntity thrownSpearEntity) {
        ItemStack itemStack = thrownSpearEntity.getRenderedItemStack().copy();
        world().playSound(null, player().getX(), player().getY(), player().getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.6f, 1.2f);

        if (savedSlotIndex != -1) {
            boolean isEmpty = savedSlotIndex == -67
                    ? player().getOffHandStack().isEmpty()
                    : player().getInventory().getStack(savedSlotIndex).isEmpty();

            if (isEmpty) {
                if (savedSlotIndex == -67) player().setStackInHand(Hand.OFF_HAND, itemStack);
                else player().getInventory().setStack(savedSlotIndex, itemStack);

                savedSlotIndex = -1;
                sync();

                return;
            }
        }

        player().giveOrDropStack(itemStack);

        savedSlotIndex = -1;
        sync();
    }
}
