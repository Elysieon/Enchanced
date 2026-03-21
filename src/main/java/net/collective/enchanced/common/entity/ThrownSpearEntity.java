package net.collective.enchanced.common.entity;

import moriyashiine.strawberrylib.api.module.SLibUtils;
import net.collective.enchanced.common.cca.entity.ImpalingComponent;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.index.ModEntityTypes;
import net.collective.enchanced.common.payload.ThrownSpearSyncS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.*;

public class ThrownSpearEntity extends PersistentProjectileEntity {
    private static final String RENDERED_ITEMSTACK_KEY = "rendered_item_stack";
    private ItemStack renderedItemStack = ItemStack.EMPTY;

    private static final String STORED_VELOCITY_DIRECTION_KEY = "stored_velocity_direction";
    private Vec3d storedVelocityDirection = Vec3d.ZERO;

    private final ThrownSpearEntityHitbox[] hitboxes;
    private final List<LivingEntity> hitEntities = new ArrayList<>();
    private boolean isReturningToOwner;

    public ThrownSpearEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        hitboxes = getAndInitializeHitboxes();
    }

    public ThrownSpearEntity(LivingEntity shooter, World world, ItemStack stack) {
        super(ModEntityTypes.THROWN_SPEAR, shooter, world, stack, stack);
        hitboxes = getAndInitializeHitboxes();
    }

    private ThrownSpearEntityHitbox[] getAndInitializeHitboxes() {
        ThrownSpearEntityHitbox[] hitboxes = new ThrownSpearEntityHitbox[]{
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
        };

        if (getEntityWorld() instanceof ServerWorld serverWorld) {
            for (ThrownSpearEntityHitbox hitbox : hitboxes) {
                serverWorld.spawnEntity(hitbox);
            }
        }

        return hitboxes;
    }

    public void setRenderedItemStack(ItemStack renderedItemStack) {
        this.renderedItemStack = renderedItemStack.copy();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return Optional.ofNullable(renderedItemStack).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put(RENDERED_ITEMSTACK_KEY, ItemStack.CODEC, renderedItemStack);
        view.put(STORED_VELOCITY_DIRECTION_KEY, Vec3d.CODEC, storedVelocityDirection);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        renderedItemStack = view.read(RENDERED_ITEMSTACK_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        storedVelocityDirection = view.read(STORED_VELOCITY_DIRECTION_KEY, Vec3d.CODEC).orElse(Vec3d.ZERO);
    }

    public ItemStack getRenderedItemStack() {
        return renderedItemStack;
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        return false;
    }

    @Override
    protected void age() {
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
        if (getVelocity().length() > 0) {
            return false;
        }

        returnToOwner();
        return true;
    }

    public void returnToOwner() {
        if (isReturningToOwner) {
            return;
        }

        Entity owner = getOwner();

        if (owner == null) {
            if (getEntityWorld() instanceof ServerWorld serverWorld) {
                Vec3d dir = storedVelocityDirection.multiply(-1);
                Vec3d offset = dir.multiply(0.25);
                Vec3d extraVelocity = dir.multiply(0.1);

                ItemEntity item = dropStack(serverWorld, renderedItemStack, offset);
                if (item != null) item.addVelocity(extraVelocity);
            }

            discard();
            return;
        }

        isReturningToOwner = true;
    }

    private boolean isMoving() {
        Vec3d velocityDirection = getVelocity().normalize();
        return velocityDirection.length() > 0;
    }

    private void tickStoredVelocityDirection() {
        Vec3d velocityDirection = getVelocity().normalize();

        if (storedVelocityDirection == Vec3d.ZERO) {
            storedVelocityDirection = getRotationVector().multiply(-1, -1, 1);
        }

        if (isMoving()) {
            storedVelocityDirection = velocityDirection;
        }
    }

    private void tickHitboxPosition() {
        Vec3d position = getEntityPos();
        for (int i = 0; i < hitboxes.length; i++) {
            hitboxes[i].setPosition(position.subtract(storedVelocityDirection.multiply(i * 0.25)));
        }
    }

    private void tickDraggingTarget() {
        if (isMoving()) {
            Vec3d position = getEntityPos();

            for (LivingEntity livingEntity : hitEntities) {
                Vec3d wishPos = position.subtract(0, 1, 0);

                RaycastContext ctx = new RaycastContext(
                        getEntityPos(),
                        wishPos.add(storedVelocityDirection.multiply(0.5)),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        this
                );

                if (getEntityWorld().raycast(ctx) instanceof BlockHitResult result) {
                    Box box = livingEntity.getBoundingBox();
                    double averageHorizontalLength = (box.getLengthX() + box.getLengthZ()) / 2d;
                    wishPos = wishPos.add(result.getSide().getDoubleVector().multiply(averageHorizontalLength));
                }

                livingEntity.setPosition(wishPos);
            }
        }
    }

    private void tickVisualSync() {
        if (!getEntityWorld().isClient()) {
            if (getOwner() instanceof ServerPlayerEntity serverPlayerEntity) {
                ServerPlayNetworking.send(serverPlayerEntity, ThrownSpearSyncS2CPayload.of(this));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (isReturningToOwner) {
            if (isInGround() || isInsideWall()) {
                setInGround(false);
            }

            setNoGravity(true);
            setNoClip(true);

            if (getOwner() instanceof Entity ownerEntity) {
                setVelocity(ownerEntity.getEntityPos().subtract(getEntityPos()).normalize());

                if (distanceTo(ownerEntity) < 2) {
                    if (ownerEntity instanceof PlayerEntity player) {
                        ImpalingComponent component = player.getComponent(ModEntityComponents.IMPALING);
                        component.pickup(this);
                    }

                    discard();
                    return;
                }
            }

            return;
        }

        tickStoredVelocityDirection();
        tickHitboxPosition();
        tickVisualSync();
        tickDraggingTarget();
    }

    private Optional<DamageSource> getDamageSource(ServerWorld serverWorld) {
        if (getOwner() instanceof PlayerEntity player) {
            final DamageSource fallbackSource = serverWorld.getDamageSources().playerAttack(player);
            return Optional.of(player.getMainHandStack().getDamageSource(player, () -> fallbackSource));
        }

        return Optional.empty();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        if (getOwner() != null) {
            getEntityWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_TRIDENT_HIT_GROUND, SoundCategory.PLAYERS, 1f, 0.825f);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (entityHitResult.getEntity() instanceof LivingEntity livingEntity && !hitEntities.contains(livingEntity)) {
            if (getEntityWorld() instanceof ServerWorld serverWorld) {
                getDamageSource(serverWorld).ifPresent(source -> {
                    if (livingEntity.damage(serverWorld, source, 10)) {
                        hitEntities.add(livingEntity);

                        if (getOwner() != null) {
                            SLibUtils.playSound(getOwner(), SoundEvents.ITEM_TRIDENT_HIT, 1, 0.825f);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onRemoved() {
        for (ThrownSpearEntityHitbox hitbox : hitboxes) {
            hitbox.remove(RemovalReason.DISCARDED);
        }

        super.onRemoved();
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        ThrownSpearEntityHitbox[] hitboxes = this.hitboxes;

        for (int i = 0; i < hitboxes.length; i++) {
            hitboxes[i].setId(i + packet.getEntityId() + 1);
        }
    }
}
