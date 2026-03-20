package net.collective.enchanced.common.entity;

import net.collective.enchanced.common.index.ModEntityTypes;
import net.collective.enchanced.common.payload.ThrownSpearSyncS2CPayload;
import net.collectively.geode.debug.Draw;
import net.collectively.geode.types.double3;
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
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.*;

public class ThrownSpearEntity extends PersistentProjectileEntity {
    private static final String RENDERED_ITEMSTACK_KEY = "rendered_item_stack";
    private ItemStack renderedItemStack;

    private static final String STORED_VELOCITY_KEY = "stored_velocity";
    private Vec3d storedVelocity = Vec3d.ZERO;

    private final ThrownSpearEntityHitbox[] hitboxes;
    private final Map<LivingEntity, Vec3d> hitEntities = new LinkedHashMap<>();

    public ThrownSpearEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
        renderedItemStack = ItemStack.EMPTY;
        hitboxes = getHitboxes();

        if (world instanceof ServerWorld serverWorld) {
            for (var box : hitboxes) serverWorld.spawnEntity(box);
        }
    }

    public ThrownSpearEntity(LivingEntity shooter, World world, ItemStack stack) {
        super(ModEntityTypes.THROWN_SPEAR, shooter, world, stack, stack);
        renderedItemStack = ItemStack.EMPTY;
        hitboxes = getHitboxes();

        if (world instanceof ServerWorld serverWorld) {
            for (var box : hitboxes) serverWorld.spawnEntity(box);
        }
    }

    private ThrownSpearEntityHitbox[] getHitboxes() {
        return new ThrownSpearEntityHitbox[]{
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
                new ThrownSpearEntityHitbox(this),
        };
    }

    public ThrownSpearEntityHitbox[] hitboxes() {
        return hitboxes;
    }

    public void setRenderedItemStack(ItemStack renderedItemStack) {
        this.renderedItemStack = renderedItemStack.copy();
    }

    public ItemStack getRenderedItemStack() {
        return renderedItemStack;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3d rot = getVelocity().normalize();

        if (rot.length() > 0) {
            storedVelocity = rot;
        }

        if (storedVelocity == null) {
            storedVelocity = Vec3d.ZERO;
        }

        Vec3d pos = getEntityPos();

        for (int i = 0; i < hitboxes.length; i++) {
            hitboxes[i].setPosition(pos.subtract(storedVelocity.multiply(i * 0.25)));
        }

        if (!getEntityWorld().isClient()) {
            if (getOwner() instanceof ServerPlayerEntity serverPlayerEntity) {
                ServerPlayNetworking.send(serverPlayerEntity, ThrownSpearSyncS2CPayload.of(this));
            }
        }

        if (getVelocity().length() > 0) {
            for (var keyValuePair : hitEntities.entrySet()) {
                LivingEntity hitEntity = keyValuePair.getKey();
                Vec3d wishPos = pos.subtract(0, 1, 0);

                RaycastContext ctx = new RaycastContext(
                        getEntityPos(),
                        wishPos.add(storedVelocity.multiply(0.5)),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        this
                );

                if (getEntityWorld().raycast(ctx) instanceof BlockHitResult result) {
                    Box box = hitEntity.getBoundingBox();
                    double averageHorizontalLength = (box.getLengthX() + box.getLengthZ()) / 2d;
                    wishPos = wishPos.add(result.getSide().getDoubleVector().multiply(averageHorizontalLength));
                }

                keyValuePair.getKey().setPosition(wishPos);
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (entityHitResult.getEntity() instanceof LivingEntity livingEntity && !hitEntities.containsKey(livingEntity)) {
            hitEntities.put(livingEntity, livingEntity.getEntityPos().subtract(getEntityPos()));

            if (getEntityWorld() instanceof ServerWorld serverWorld) {
                if (getOwner() instanceof PlayerEntity player) {
                    DamageSource source = player.getMainHandStack().getDamageSource(player, () -> serverWorld.getDamageSources().playerAttack(player));
                    livingEntity.damage(serverWorld, source, 10);
                }
            }
        }
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

        Vec3d offset = Vec3d.ZERO;
        Vec3d extraVelocity = Vec3d.ZERO;

        if (getOwner() instanceof PlayerEntity player) {
            Vec3d dir = player.getEntityPos().subtract(getEntityPos()).normalize();
            offset = dir.multiply(0.25);
            extraVelocity = dir.multiply(0.1);
        }

        ItemEntity item = dropStack(world, renderedItemStack, offset);
        item.addVelocity(extraVelocity);

        discard();
        return true;
    }

    @Override
    public void onRemoved() {
        for (var hitbox : hitboxes) hitbox.remove(RemovalReason.DISCARDED);
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
        view.put(STORED_VELOCITY_KEY, Vec3d.CODEC, storedVelocity);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        renderedItemStack = view.read(RENDERED_ITEMSTACK_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        storedVelocity = view.read(STORED_VELOCITY_KEY, Vec3d.CODEC).orElse(Vec3d.ZERO);
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        return false;
    }
}
