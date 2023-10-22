package net.reimaden.lockon;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.reimaden.lockon.config.LockOnConfig;
import net.reimaden.lockon.mixin.InteractionInvoker;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class LockOnHandler {

    private static final Minecraft INSTANCE = Minecraft.getInstance();
    private static final List<Entity> entityList = new ArrayList<>();

    private static final String KEY_CATEGORIES_LOCKON = "key.categories." + LockOn.MOD_ID;
    private static final String KEY_LOCK_ON = "key." + LockOn.MOD_ID + ".lock_on";
    private static final String KEY_SWITCH_TARGETS = "key." + LockOn.MOD_ID + ".switchTargets";

    private static KeyMapping lockOn;
    private static KeyMapping switchTargets;

    private static void tickLocking() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (lockOn.consumeClick()) {
                if (lockedOn) {
                    leaveLockOn();
                } else {
                    attemptEnterLockOn(INSTANCE.player);
                }
            }

            while (switchTargets.consumeClick()) {
                if (lockedOn) switchToNextTarget(INSTANCE.player);
            }

            // Disable lock-on if the targeted entity is too far away
            if (lockedOn) {
                LockOnConfig config = AutoConfig.getConfigHolder(LockOnConfig.class).getConfig();
                // Check the distance between the player and the targeted entity
                int maxDistance = config.range * 2;
                if (targeted != null && targeted.distanceTo(INSTANCE.player) > maxDistance) {
                    leaveLockOn();
                }
            }

            tickLockedOn();
        });
    }

    private static void logOff() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> leaveLockOn());
    }

    public static void registerKeys() {
        lockOn = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_LOCK_ON,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KEY_CATEGORIES_LOCKON
        ));
        switchTargets = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_SWITCH_TARGETS,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KEY_CATEGORIES_LOCKON
        ));

        tickLocking();
        logOff();
    }

    private static boolean lockedOn;
    private static Entity targeted;

    public static boolean handleKeyPress(Player player) {
        if (player != null && !INSTANCE.isPaused()) {
            if (targeted != null) {
                Vec3 targetPos;
                if (targeted instanceof Interaction interaction) {
                    targetPos = interaction.position().add(0, ((InteractionInvoker) interaction).lockon$getHeight() - ((InteractionInvoker) interaction).lockon$getHeight() / 2, 0);
                } else {
                    targetPos = targeted.position().add(0, targeted.getBbHeight() - targeted.getBbHeight() / 2, 0);
                }
                Vec3 targetVec = targetPos.subtract(player.position().add(0, player.getEyeHeight(), 0)).normalize();
                double targetAngleX = Mth.wrapDegrees(Math.atan2(-targetVec.x, targetVec.z) * 180 / Math.PI);
                double targetAngleY = Math.atan2(targetVec.y, targetVec.horizontalDistance()) * 180 / Math.PI;
                double xRot = Mth.wrapDegrees(player.getXRot());
                double yRot = Mth.wrapDegrees(player.getYRot());
                double toTurnX = Mth.wrapDegrees(yRot - targetAngleX);
                double toTurnY = Mth.wrapDegrees(xRot + targetAngleY);

                player.turn(-toTurnX, -toTurnY);
                return true;
            }
        }

        return false;
    }

    private static void attemptEnterLockOn(Player player) {
        switchToNextTarget(player);
        if (targeted != null) {
            lockedOn = true;
        }
    }

    private static void tickLockedOn() {
        entityList.removeIf(livingEntity -> !livingEntity.isAlive());
        if (targeted != null && !targeted.isAlive()) {
            targeted = null;
            lockedOn = false;
        }
    }

    // MythicMobs compatibility
    private static final Predicate<Entity> ENTITY_PREDICATE = entity ->
            !entity.is(INSTANCE.player) && entity.isAlive() && !entity.isInvisible() && (entity instanceof Enemy || entity instanceof Interaction);

    private static int cycle = -1;

    private static Entity findNearby(Player player) {
        LockOnConfig config = AutoConfig.getConfigHolder(LockOnConfig.class).getConfig();
        final Predicate<Entity> lineOfSight = player::hasLineOfSight;

        List<Entity> entities = player.level()
                .getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(config.range)).stream().filter(lineOfSight).filter(ENTITY_PREDICATE).toList();

        if (lockedOn) {
            cycle++;
            for (Entity entity : entities) {
                if (!entityList.contains(entity)) {
                    entityList.add(entity);
                    return entity;
                }
            }

            // Cycle existing entity
            if (cycle >= entityList.size()) {
                cycle = 0;
            }
            return entityList.get(cycle);
        } else {
            if (!entities.isEmpty()) {
                Entity first = entities.get(0);
                entityList.add(first);
                return entities.get(0);
            } else {
                return null;
            }
        }
    }

    private static void switchToNextTarget(Player player) {
        targeted = findNearby(player);
    }

    private static void leaveLockOn() {
        targeted = null;
        lockedOn = false;
        entityList.clear();
    }
}
