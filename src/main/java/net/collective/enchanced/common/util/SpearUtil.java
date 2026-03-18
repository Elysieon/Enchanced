package net.collective.enchanced.common.util;

import com.mojang.datafixers.util.Either;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.component.type.KineticWeaponComponent;
import net.minecraft.component.type.PiercingWeaponComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Collection;

public interface SpearUtil {
    static void attack(PlayerEntity player, float damageMultiplier, Runnable sync) {
        ItemStack activeStack = player.getActiveOrMainHandStack();

        if (activeStack.isEmpty() || !activeStack.isIn(ItemTags.SPEARS)) {
            return;
        }

        KineticWeaponComponent kineticWeaponComponent = activeStack.get(DataComponentTypes.KINETIC_WEAPON);
        if (kineticWeaponComponent == null) {
            return;
        }

        // DEBUGGING: Do not remove! It will be necessary in case we find bugs or issues and need to visualize the hitbox.
        // Box boundingBox = player.getBoundingBox();
        // Vec3d eyePos = player.getEyePos();
        // Vec3d headRotation = player.getHeadRotationVector();
        // Vec3d movement = player.getMovement();
        // Vec3d maxReach = eyePos.add(headRotation.multiply(4.5f + math.max(0, movement.dotProduct(headRotation))));
        // Vec3d minReach = eyePos.add(headRotation.multiply(2));
        // boundingBox = boundingBox.stretch(maxReach.subtract(minReach));
        // GizmoDrawing.box(boundingBox, DrawStyle.stroked(0xffff55ff));

        Either<BlockHitResult, Collection<EntityHitResult>> collisionResult = ProjectileUtil.collectPiercingCollisions(
                player,
                new AttackRangeComponent(
                        0,
                        4.5f,
                        0,
                        4.5f,
                        0.5f,
                        1f
                ),
                target -> PiercingWeaponComponent.canHit(player, target),
                RaycastContext.ShapeType.COLLIDER
        );

        Vec3d rotation = player.getRotationVector();
        double playerSpeedRelation = rotation.dotProduct(KineticWeaponComponent.getAmplifiedMovement(player));
        double attackDamage = player.getAttributeBaseValue(EntityAttributes.ATTACK_DAMAGE);
        boolean hasPierced = false;

        if (collisionResult.right().isPresent()) {
            Collection<EntityHitResult> collisions = collisionResult.right().get();

            for (EntityHitResult collision : collisions) {
                Entity entity = collision.getEntity();
                if (entity != null) {
                    if (entity instanceof EnderDragonPart enderDragonPart) {
                        entity = enderDragonPart.owner;
                    }

                    boolean isInCooldown = player.isInPiercingCooldown(entity, 10);
                    // DEBUGGING: Do not remove! It will be necessary in case we find bugs or issues and need to visualize the hitbox.
                    // GizmoDrawing.box(collision.getEntity().getBoundingBox(), DrawStyle.stroked(isInCooldown ? 0xffff5555 : 0xff55ff55));

                    if (!isInCooldown) {
                        double piercedSpeedRelation = rotation.dotProduct(KineticWeaponComponent.getAmplifiedMovement(entity));
                        double relativeSpeed = Math.max(0.0, playerSpeedRelation - piercedSpeedRelation);
                        float effectiveDamage = (float) attackDamage + MathHelper.floor(relativeSpeed * kineticWeaponComponent.damageMultiplier());
                        effectiveDamage *= 0.41f;
                        effectiveDamage *= damageMultiplier;

                        boolean hasPiercedThisEntity = player.pierce(player.getActiveHand().getEquipmentSlot(), entity, effectiveDamage, true, true, true);

                        if (hasPiercedThisEntity) {
                            player.startPiercingCooldown(entity);
                        }

                        hasPierced |= hasPiercedThisEntity;
                    }

                    sync.run();
                }
            }
        }

        if (hasPierced) {
            player.getEntityWorld().sendEntityStatus(player, EntityStatuses.KINETIC_ATTACK);
            if (player instanceof ServerPlayerEntity serverPlayerEntity) {
                Criteria.SPEAR_MOBS.trigger(serverPlayerEntity, player.getPiercedEntityCount(pierced -> pierced instanceof LivingEntity));
            }
        }
    }
}
