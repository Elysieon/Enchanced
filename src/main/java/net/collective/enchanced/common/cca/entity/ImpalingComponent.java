package net.collective.enchanced.common.cca.entity;

import net.collective.enchanced.common.cca.SynedPlayerEntityComponent;
import net.collective.enchanced.common.entity.ThrownSpearEntity;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.payload.ThrownSpearSyncS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec3d;
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

        if (world() instanceof ServerWorld serverWorld) {
            ThrownSpearEntity entity = ProjectileEntity.spawnWithVelocity(
                    (world, shooter, stack) -> new ThrownSpearEntity(shooter, world, stack),
                    serverWorld,
                    itemStack,
                    player(),
                    0, 2.5f, 0
            );

            entity.setRenderedItemStack(itemStack);

            if (player() instanceof ServerPlayerEntity serverPlayerEntity) {
                ServerPlayNetworking.send(serverPlayerEntity, ThrownSpearSyncS2CPayload.of(entity));
            }
        }

        itemStack.decrement(1);
        sync();
    }
}
