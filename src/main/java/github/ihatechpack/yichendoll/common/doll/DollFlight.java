package github.ihatechpack.yichendoll.common.doll;

import github.ihatechpack.yichendoll.Res;
import github.ihatechpack.yichendoll.integration.curios.CuriosBridge;
import github.ihatechpack.yichendoll.registry.ModAttributes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/** Flight authorization is server-owned; the local player predicts the same glide acceleration. */
public final class DollFlight {
    // Temporarily disabled: howxu currently grants creative flight only.
    private static final boolean ELYTRA_ENABLED = false;
    // DE's CHAOTIC_FLIGHT default elytra_boost_speed.
    private static final double ELYTRA_BOOST = 3.5;
    private static final int BOOST_ENERGY_PER_TICK = 5;
    private static final Map<UUID, Integer> BOOST_INPUT = new HashMap<>();
    private static Predicate<Player> clientBoosting = player -> false;

    private DollFlight() {}

    public static void setClientBoosting(Predicate<Player> input) {
        clientBoosting = input;
    }

    public static boolean canGlide(LivingEntity entity) {
        if (!ELYTRA_ENABLED) return false;
        if (!(entity instanceof Player player)) return false;
        var stacks = player instanceof ServerPlayer serverPlayer
                ? DollRuntime.stacks(serverPlayer, "howxu") : CuriosBridge.get().activeStacks(player, "howxu");
        return DollEnergy.available(stacks) >= 5;
    }

    public static boolean canStartGliding(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isSleeping()
                && !player.isInWater() && !player.isInLava() && !player.isPassenger()
                && !player.onClimbable() && !player.hasEffect(MobEffects.LEVITATION) && canGlide(player);
    }

    public static void updatePermission(ServerPlayer player, boolean powered) {
        ModAttributes.update(player, NeoForgeMod.CREATIVE_FLIGHT, Res.rl("howxu_flight"), powered ? 1 : 0);
        if (!powered && player.getAbilities().flying && !player.mayFly()) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    /** Sprint+jump can take off from flat ground; sprint also switches creative flight to gliding. */
    public static boolean startGliding(Player player) {
        if (!canStartGliding(player)) return false;
        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        if (player.onGround()) {
            player.jumpFromGround();
            player.setOnGround(false);
        }
        player.fallDistance = 0;
        player.startFallFlying();
        return true;
    }

    public static void setBoosting(ServerPlayer player, boolean boosting) {
        if (!boosting) {
            forget(player);
            return;
        }
        // Validate live slots on input, not just the periodic membership snapshot.
        DollRuntime.update(player, CuriosBridge.get().equipment(player));
        if (!canStartGliding(player)) {
            forget(player);
            return;
        }
        BOOST_INPUT.put(player.getUUID(), player.getServer().getTickCount());
        if (!player.isFallFlying() && startGliding(player)) {
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }

    /** Called by updateFallFlying on both sides, just like DE's elytraFlightTickBC. */
    public static boolean glideTick(LivingEntity entity) {
        if (!(entity instanceof Player player) || !canGlide(player)) return false;
        boolean boosting;
        if (player instanceof ServerPlayer serverPlayer) {
            Integer inputTick = BOOST_INPUT.get(player.getUUID());
            boosting = inputTick != null && serverPlayer.getServer().getTickCount() - inputTick < 30;
            var stacks = DollRuntime.stacks(serverPlayer, "howxu");
            // Reserve this tick's normal 5 FE; the shared behavior pays it exactly once.
            boosting = boosting && DollEnergy.available(stacks) >= 5 + BOOST_ENERGY_PER_TICK
                    && DollEnergy.consume(stacks, BOOST_ENERGY_PER_TICK);
        } else {
            boosting = player.level().isClientSide() && clientBoosting.test(player)
                    && DollEnergy.available(CuriosBridge.get().activeStacks(player, "howxu")) >= 5 + BOOST_ENERGY_PER_TICK;
        }
        if (boosting) {
            // Same steering formula and boost as DE's chaotic flight module.
            double speed = 1.5D * ELYTRA_BOOST;
            double accel = 0.01 * ELYTRA_BOOST;
            var look = player.getLookAngle();
            var motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.add(look.scale(accel).add(look.scale(speed).subtract(motion).scale(accel))));
        }
        return true;
    }

    public static void forget(ServerPlayer player) {
        BOOST_INPUT.remove(player.getUUID());
    }

    public static void clear() {
        BOOST_INPUT.clear();
    }
}
