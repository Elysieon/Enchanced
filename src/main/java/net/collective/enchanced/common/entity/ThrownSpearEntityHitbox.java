package net.collective.enchanced.common.entity;

import net.collective.enchanced.common.index.ModEntityTypes;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ThrownSpearEntityHitbox extends Entity {
    private final @Nullable ThrownSpearEntity owner;

    public ThrownSpearEntityHitbox(ThrownSpearEntity owner) {
        super(ModEntityTypes.THROWN_SPEAR_HITBOX, owner.getEntityWorld());
        this.owner = owner;
    }

    public ThrownSpearEntityHitbox(EntityType<ThrownSpearEntityHitbox> type, World world) {
        super(type, world);
        owner = null;
    }

    @Override
    public void tick() {
        super.tick();

        if(!getEntityWorld().isClient() && (owner == null || owner.isRemoved())) {
            setRemoved(RemovalReason.DISCARDED);
        }

        List<Entity> list = this.getEntityWorld().getOtherEntities(this, this.getBoundingBox().contract(0.1, 0.1, 0.1), entity -> !(entity instanceof ThrownSpearEntityHitbox) && !(entity instanceof ThrownSpearEntity));
        for (Entity entity : list) {
            if (entity instanceof LivingEntity) {
                entity.slowMovement(Blocks.AIR.getDefaultState(), new Vec3d(0.05, 0.01, 0.05));
            }
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (owner != null) {
            return owner.damage(world, source, amount);
        }

        return false;
    }

    @Nullable
    @Override
    public ItemStack getPickBlockStack() {
        return this.owner == null ? ItemStack.EMPTY : this.owner.getPickBlockStack();
    }

    @Override
    public boolean isCollidable(@Nullable Entity entity) {
        // return this.getEntityWorld().isClient() && entity instanceof LivingEntity && (entity.getEntityPos().y >= getBoundingBox().minY);
        return this.getEntityWorld().isClient() && entity instanceof LivingEntity;
    }

    @Override
    protected void readCustomData(ReadView view) {
    }

    @Override
    protected void writeCustomData(WriteView view) {
    }
}
