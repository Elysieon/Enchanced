package net.collective.enchanced.client.init;

import net.collective.enchanced.api.posing.CustomPoseCondition;
import net.collective.enchanced.api.posing.CustomPose;
import net.collective.enchanced.client.enchantments.impaling.ImpalingCustomPose;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public interface ModCustomPoses {
    Map<CustomPoseCondition, CustomPose> REGISTRY = new LinkedHashMap<>();

    static void init() {
        register(ImpalingCustomPose::validate, new ImpalingCustomPose());
    }

    private static void register(CustomPoseCondition condition, CustomPose pose) {
        REGISTRY.put(condition, pose);
    }
}
