package com.swill.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import java.util.List;
import java.util.Random;

public class KillAuraMod implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static boolean enabled = true;
    private int attackCooldown = 0;
    private final Random random = new Random();

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.swill.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.swill"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            if (toggleKey.wasPressed()) enabled = !enabled;
            if (!enabled || client.interactionManager == null) return;
            if (attackCooldown > 0) { attackCooldown--; return; }

            List<LivingEntity> entities = client.world.getEntitiesByClass(
                LivingEntity.class,
                client.player.getBoundingBox().expand(4.5),
                e -> e != client.player && e.isAlive() && !(e instanceof PlayerEntity && ((PlayerEntity) e).isCreative())
            );

            entities.stream()
                .min((a, b) -> Double.compare(client.player.squaredDistanceTo(a), client.player.squaredDistanceTo(b)))
                .ifPresent(target -> {
                    client.interactionManager.attackEntity(client.player, target);
                    client.player.swingHand(Hand.MAIN_HAND);
                    attackCooldown = 2 + random.nextInt(3);
                });
        });
    }
}
