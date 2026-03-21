package net.collective.enchanced.common.cca.entity;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntList;
import net.collective.enchanced.common.cca.SynedPlayerEntityComponent;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collectively.geode.helpers.RenderHelper;
import net.collectively.geode.math.math;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import org.ladysnake.cca.api.v3.component.ComponentKey;

import java.util.ArrayList;
import java.util.List;

public class BallisticComponent extends SynedPlayerEntityComponent {
    private static final String PROJECTILES_KEY = "projectiles";
    /// contains every shot projectile. should be saved as a list of ints describing the ids of the entities
    private final List<FireworkRocketEntity> projectiles = new ArrayList<>();

    public BallisticComponent(PlayerEntity playerEntity) {
        super(playerEntity);
    }

    @Override
    protected ComponentKey<? extends SynedPlayerEntityComponent> getComponentKey() {
        return ModEntityComponents.BALLISTIC;
    }

    @Override
    public void writeData(WriteView writeView) {
        writeView.put(PROJECTILES_KEY, Codec.INT.listOf(), projectiles.stream().map(Entity::getId).toList());
    }

    @Override
    public void readData(ReadView readView) {
        projectiles.clear();
        projectiles.addAll(
                readView.read(PROJECTILES_KEY, Codec.INT.listOf())
                        .orElse(List.of())
                        .stream()
                        .map(id -> world() == null ? null : world().getEntityById(id))
                        .filter(x -> x instanceof FireworkRocketEntity)
                        .map(x -> (FireworkRocketEntity) x)
                        .toList()
        );
    }

    public void addProjectile(FireworkRocketEntity projectile) {
        projectiles.add(projectile);
        sync();
    }

    private EntityHitResult getCrosshairTarget(double range) {
        float tickDelta = RenderHelper.getTickDeltaOrZero(world());
        double rangeSquared = MathHelper.square(range);
        Vec3d vec3d = player().getCameraPosVec(tickDelta);
        HitResult hitResult = player().raycast(range, tickDelta, false);
        double f = hitResult.getPos().squaredDistanceTo(vec3d);
        if (hitResult.getType() != HitResult.Type.MISS) {
            rangeSquared = f;
            range = Math.sqrt(f);
        }

        Vec3d vec3d2 = player().getRotationVec(tickDelta);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * range, vec3d2.y * range, vec3d2.z * range);
        Box box = player().getBoundingBox().stretch(vec3d2.multiply(range)).expand(1.0, 1.0, 1.0);
        return ProjectileUtil.raycast(player(), vec3d, vec3d3, box, EntityPredicates.CAN_HIT, rangeSquared);
    }

    @Override
    public void tick() {
        Vec3d targetPosition;
        double range = 60;
        EntityHitResult entityHitResult = getCrosshairTarget(range);

        if (entityHitResult == null || entityHitResult.getType() == HitResult.Type.MISS) {
            RaycastContext ctx = new RaycastContext(
                    player().getEyePos(),
                    player().getEyePos().add(player().getRotationVector().multiply(range)),
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player()
            );

            if (world().raycast(ctx) instanceof BlockHitResult blockHitResult) {
                targetPosition = blockHitResult.getPos();
            } else {
                targetPosition = player().getEyePos().add(player().getRotationVector().multiply(range / 2d));
            }
        } else {
            targetPosition = entityHitResult.getEntity().getEyePos();
        }

        projectiles.removeIf(projectile -> projectile == null || !projectile.isAlive() || projectile.isRemoved());

        for (FireworkRocketEntity projectile : projectiles) {
            Vec3d direction = targetPosition.subtract(projectile.getEntityPos()).normalize();
            Vec3d vel = projectile.getVelocity();
            double velSpeed = vel.lengthSquared();
            Vec3d velDir = vel.multiply(1d / math.sqrt(velSpeed));
            projectile.setVelocity(velDir.lerp(direction, 0.08));
        }
    }

    private static FireworkExplosionComponent createFireworkExplosion(Random random) {
        int fireworkColor = MathHelper.hsvToRgb(MathHelper.nextFloat(random, 0f, 360f), MathHelper.nextFloat(random, 0.5f, 1f), MathHelper.nextFloat(random, 0.7f, 1f));
        int fireworkFadeColor = MathHelper.hsvToRgb(MathHelper.nextFloat(random, 0f, 360f), MathHelper.nextFloat(random, 0.5f, 1f), MathHelper.nextFloat(random, 0.7f, 1f));
        return new FireworkExplosionComponent(
                FireworkExplosionComponent.Type.values()[MathHelper.nextBetween(
                        random,
                        0,
                        FireworkExplosionComponent.Type.values().length - 1
                )],
                IntList.of(
                        fireworkColor
                ),
                IntList.of(
                        fireworkFadeColor
                ),
                random.nextBoolean(),
                random.nextBoolean()
        );
    }

    public static ItemStack getRandomFirework(Random random) {
        ItemStack fireworkRocket = Items.FIREWORK_ROCKET.getDefaultStack();
        List<FireworkExplosionComponent> explosions = new ArrayList<>();
        for (int i = 0; i < random.nextBetween(1, 4); i++) explosions.add(createFireworkExplosion(random));
        int flightDuration = MathHelper.nextBetween(random, 1, 3);
        fireworkRocket.set(DataComponentTypes.FIREWORKS, new FireworksComponent(flightDuration, explosions));
        return fireworkRocket;
    }
}
