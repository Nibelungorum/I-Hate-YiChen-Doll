"""Compile real potion behavior and equipment transitions against small MC fakes.

These are mocked behavioral regressions, not a Minecraft startup/gameplay test.
Run: python3 -m unittest discover -s tests -p 'test_potion_dolls.py' -v
"""
import json
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = "github.ihatechpack.yichendoll"
JAVA = ROOT / "src/main/java" / BASE.replace(".", "/")
SAVE_MIXIN = JAVA / "mixin/common/PotionOwnershipSaveMixin.java"
CLONE_MIXIN = JAVA / "mixin/common/PotionEffectCloneMixin.java"


class PotionDollsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temp = tempfile.TemporaryDirectory()
        cls.addClassCleanup(cls.temp.cleanup)
        directory = Path(cls.temp.name)
        sources = []

        def stub(name, body):
            package, _, _ = name.rpartition(".")
            path = directory / (name.replace(".", "/") + ".java")
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(f"package {package};\n{body}\n")
            sources.append(str(path))

        stub("net.minecraft.core.Holder", "public record Holder<T>(T value) {}")
        stub("net.minecraft.world.effect.MobEffect", "public record MobEffect(String id) {}")
        stub("net.minecraft.world.effect.MobEffects", """
            import net.minecraft.core.Holder;
            public class MobEffects {
                public static final Holder<MobEffect>
                    DOLPHINS_GRACE = effect("dolphins_grace"), CONDUIT_POWER = effect("conduit_power"),
                    LUCK = effect("luck"), GLOWING = effect("glowing"), NIGHT_VISION = effect("night_vision"),
                    FIRE_RESISTANCE = effect("fire_resistance"), SATURATION = effect("saturation");
                private static Holder<MobEffect> effect(String id) { return new Holder<>(new MobEffect(id)); }
            }""")
        stub("net.minecraft.world.effect.MobEffectInstance", """
            import net.minecraft.core.Holder;
            public class MobEffectInstance {
                public static final int INFINITE_DURATION = -1, MAX_AMPLIFIER = 255;
                private final Holder<MobEffect> effect;
                private final int duration, amplifier;
                private final boolean ambient, icon;
                public boolean visible;
                public MobEffectInstance(Holder<MobEffect> effect, int duration, int amplifier,
                        boolean ambient, boolean visible, boolean icon) {
                    this.effect = effect; this.duration = duration; this.amplifier = amplifier;
                    this.ambient = ambient; this.visible = visible; this.icon = icon;
                }
                public MobEffectInstance(MobEffectInstance other) {
                    this(other.effect, other.duration, other.amplifier, other.ambient, other.visible, other.icon);
                }
                public Holder<MobEffect> getEffect() { return effect; }
                public int getDuration() { return duration; }
                public int getAmplifier() { return amplifier; }
                public boolean isInfiniteDuration() { return duration == INFINITE_DURATION; }
                public boolean isAmbient() { return ambient; }
                public boolean isVisible() { return visible; }
                public boolean showIcon() { return icon; }
            }""")
        stub("net.minecraft.nbt.CompoundTag", """
            public class CompoundTag {
                private final java.util.Map<String, Object> values = new java.util.HashMap<>();
                public int getInt(String key) {
                    return values.get(key) instanceof Integer value ? value : 0;
                }
                public void putInt(String key, int value) { values.put(key, value); }
                // Match Minecraft: absent compounds are detached until explicitly put back.
                public CompoundTag getCompound(String key) {
                    return values.get(key) instanceof CompoundTag value ? value : new CompoundTag();
                }
                public void put(String key, CompoundTag value) { values.put(key, value); }
                public void remove(String key) { values.remove(key); }
                public boolean isEmpty() { return values.isEmpty(); }
                public CompoundTag copy() {
                    var result = new CompoundTag();
                    values.forEach((key, value) -> result.values.put(key,
                        value instanceof CompoundTag compound ? compound.copy() : value));
                    return result;
                }
            }""")
        stub("org.spongepowered.asm.mixin.Mixin", "public @interface Mixin { Class<?>[] value(); }")
        stub("org.spongepowered.asm.mixin.injection.At", "public @interface At { String value(); String target() default \"\"; }")
        stub("org.spongepowered.asm.mixin.injection.Inject", """
            public @interface Inject { String method(); At at(); }""")
        stub("org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable", """
            public class CallbackInfoReturnable<T> {}""")
        stub("com.llamalad7.mixinextras.injector.wrapoperation.Operation", """
            public interface Operation<T> { T call(Object... args); }""")
        stub("com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation", """
            public @interface WrapOperation { String method(); org.spongepowered.asm.mixin.injection.At at(); }""")
        stub("com.llamalad7.mixinextras.sugar.Local", """
            public @interface Local { boolean argsOnly() default false; int ordinal() default -1; }""")
        # Model the real save mixin's HEAD callback, not actual Mixin weaving.
        # Without the mixin, keep the original save behavior so regressions expose stale bits.
        parent = f"extends {BASE}.mixin.common.PotionOwnershipSaveMixin" if SAVE_MIXIN.exists() else ""
        hook = f"""
                    try {{
                        var method = {BASE}.mixin.common.PotionOwnershipSaveMixin.class.getDeclaredMethod(
                            "yichendoll$beforePlayerSave", net.minecraft.nbt.CompoundTag.class,
                            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable.class);
                        method.setAccessible(true);
                        method.invoke(this, compound,
                            new org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<>());
                    }} catch (ReflectiveOperationException exception) {{
                        throw new AssertionError("save callback failed", exception);
                    }}
        """ if SAVE_MIXIN.exists() else ""
        stub("net.minecraft.world.entity.Entity", """
            public class Entity PARENT {
                public java.util.UUID getUUID() { throw new AssertionError("non-player UUID access"); }
                public net.minecraft.nbt.CompoundTag saveWithoutId(net.minecraft.nbt.CompoundTag compound) {
                    HOOK
                    if (this instanceof net.minecraft.server.level.ServerPlayer player)
                        compound.put("NeoForgeData", player.getPersistentData().copy());
                    return compound;
                }
            }""".replace("PARENT", parent).replace("HOOK", hook))
        stub("net.minecraft.server.MinecraftServer", """
            public class MinecraftServer { public int tick;
                public int getTickCount() { return tick; }
            }""")
        clone_copy = f"""
                        try {{
                            var method = {BASE}.mixin.common.PotionEffectCloneMixin.class.getDeclaredMethod(
                                "yichendoll$copyUnownedEffect", ServerPlayer.class, MobEffectInstance.class,
                                com.llamalad7.mixinextras.injector.wrapoperation.Operation.class,
                                ServerPlayer.class, MobEffectInstance.class);
                            method.setAccessible(true);
                            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> operation =
                                args -> ((ServerPlayer) args[0]).addEffect((MobEffectInstance) args[1]);
                            method.invoke(null, this, copied, operation, original, sourceEffect);
                        }} catch (ReflectiveOperationException exception) {{
                            throw new AssertionError("clone copy callback failed", exception);
                        }}
        """ if CLONE_MIXIN.exists() else "addEffect(copied);"
        stub("net.minecraft.world.entity.player.Player", "public class Player extends net.minecraft.world.entity.Entity {}")
        stub("net.minecraft.server.level.ServerPlayer", """
            import net.minecraft.core.Holder;
            import net.minecraft.world.effect.*;
            public class ServerPlayer extends net.minecraft.world.entity.player.Player {
                private final java.util.UUID uuid;
                public ServerPlayer() { this(java.util.UUID.randomUUID()); }
                public ServerPlayer(java.util.UUID uuid) { this.uuid = uuid; }
                private final net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
                public final net.minecraft.server.MinecraftServer server = new net.minecraft.server.MinecraftServer();
                public final java.util.Map<Holder<MobEffect>, MobEffectInstance> effects = new java.util.HashMap<>();
                public int addCalls;
                public boolean rejectEffects;
                public java.util.UUID getUUID() { return uuid; }
                public net.minecraft.server.MinecraftServer getServer() { return server; }
                public net.minecraft.nbt.CompoundTag getPersistentData() { return data; }
                public boolean addEffect(MobEffectInstance effect) {
                    addCalls++;
                    if (rejectEffects) return false;
                    effects.put(effect.getEffect(), effect);
                    return true;
                }
                public boolean hasEffect(Holder<MobEffect> effect) { return effects.containsKey(effect); }
                public MobEffectInstance getEffect(Holder<MobEffect> effect) { return effects.get(effect); }
                public boolean removeEffect(Holder<MobEffect> effect) { return effects.remove(effect) != null; }
                public java.util.Map<String, github.ihatechpack.yichendoll.api.doll.EquippedDoll> equipment = java.util.Map.of();
                public java.util.function.Consumer<ServerPlayer> cloneListener = player -> {};
                public void restoreFrom(ServerPlayer original, boolean keepEverything) {
                    if (keepEverything) {
                        for (var sourceEffect : original.effects.values()) {
                            var copied = new MobEffectInstance(sourceEffect);
                            COPY_EFFECT
                        }
                    }
                    // Model an external Clone listener that runs before DollRuntime's listener.
                    cloneListener.accept(this);
                    github.ihatechpack.yichendoll.common.doll.DollRuntime.onClone(
                        new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(this, original, !keepEverything));
                }
            }""".replace("COPY_EFFECT", clone_copy))
        stub("net.minecraft.world.item.ItemStack", "public class ItemStack {}")
        stub(BASE + ".registry.DollRegistery", """
            public class DollRegistery { public record Doll(String id,
                github.ihatechpack.yichendoll.api.doll.DollBehavior behavior) {} }""")
        stub(BASE + ".api.doll.EquippedDoll", """
            public record EquippedDoll(github.ihatechpack.yichendoll.registry.DollRegistery.Doll doll, int count) {
                public DollContext context(net.minecraft.server.level.ServerPlayer player) { return new DollContext(player); }
                public java.util.List<net.minecraft.world.item.ItemStack> stacks() {
                    return java.util.Collections.nCopies(count, new net.minecraft.world.item.ItemStack());
                }
            }""")
        stub(BASE + ".integration.curios.CuriosBridge", """
            public class CuriosBridge {
                public static CuriosBridge get() { return new CuriosBridge(); }
                public java.util.Map<String, github.ihatechpack.yichendoll.api.doll.EquippedDoll> equipment(
                    net.minecraft.server.level.ServerPlayer player) { return player.equipment; }
            }""")
        stub(BASE + ".integration.ae2.AE2Bridge", """
            public class AE2Bridge { public static AE2Bridge get() { return new AE2Bridge(); } public void clear() {} }""")
        stub(BASE + ".common.doll.DollFlight", "public class DollFlight { public static void clear() {} }")
        stub(BASE + ".IHateYiChenDoll", 'public class IHateYiChenDoll { public static final String MOD_ID = "ihateyichendoll"; }')
        stub("net.neoforged.bus.api.SubscribeEvent", "public @interface SubscribeEvent {}")
        stub("net.neoforged.fml.common.EventBusSubscriber", "public @interface EventBusSubscriber { String modid(); }")
        stub("net.neoforged.neoforge.event.entity.player.PlayerEvent", """
            import net.minecraft.world.entity.player.Player;
            public class PlayerEvent {
                private final Player player;
                public PlayerEvent(Player player) { this.player = player; }
                public Player getEntity() { return player; }
                public static class PlayerLoggedInEvent extends PlayerEvent {
                    public PlayerLoggedInEvent(Player player) { super(player); }
                }
                public static class PlayerLoggedOutEvent extends PlayerEvent {
                    public PlayerLoggedOutEvent(Player player) { super(player); }
                }
                public static class PlayerRespawnEvent extends PlayerEvent {
                    public PlayerRespawnEvent(Player player) { super(player); }
                }
                public static class Clone extends PlayerEvent {
                    private final Player original; private final boolean wasDeath;
                    public Clone(Player player, Player original, boolean wasDeath) {
                        super(player); this.original = original; this.wasDeath = wasDeath;
                    }
                    public Player getOriginal() { return original; }
                    public boolean isWasDeath() { return wasDeath; }
                }
            }""")
        stub("net.neoforged.neoforge.event.server.ServerStoppedEvent", "public class ServerStoppedEvent {}")
        stub("net.neoforged.neoforge.event.tick.PlayerTickEvent", """
            public class PlayerTickEvent {
                public record Post(net.minecraft.world.entity.player.Player getEntity) {}
            }""")
        stub(BASE + ".api.doll.DollContext", """
            public record DollContext(net.minecraft.server.level.ServerPlayer player) {}""")
        stub(BASE + ".common.doll.PotionDollsRunner", """
            import java.util.*;
            import java.util.concurrent.atomic.AtomicInteger;
            import net.minecraft.core.Holder;
            import net.minecraft.server.level.ServerPlayer;
            import net.minecraft.world.effect.*;
            import github.ihatechpack.yichendoll.api.doll.DollContext;
            import github.ihatechpack.yichendoll.common.doll.behavior.PotionDollBehavior;
            public class PotionDollsRunner {
                private static final String OWNERSHIP = "ihateyichendoll_potion_ownership";
                static void check(boolean value, String scenario) {
                    if (!value) throw new AssertionError(scenario);
                }
                static MobEffectInstance ordinary(Holder<MobEffect> effect) {
                    return new MobEffectInstance(effect, 200, 0, false, true, true);
                }
                static MobEffectInstance matching(Holder<MobEffect> effect) {
                    return new MobEffectInstance(effect, -1, 255, false, false, true);
                }
                static int mask(ServerPlayer player, String id) {
                    return player.getPersistentData().getCompound(OWNERSHIP).getInt(id);
                }
                record SavedPlayer(net.minecraft.nbt.CompoundTag data,
                        Map<Holder<MobEffect>, MobEffectInstance> effects, UUID uuid) {
                    ServerPlayer load() {
                        var loaded = new ServerPlayer(uuid);
                        loaded.getPersistentData().put(OWNERSHIP,
                            data.getCompound("NeoForgeData").getCompound(OWNERSHIP).copy());
                        loaded.effects.putAll(effects);
                        PotionDollBehavior.clearAll();
                        PotionDollBehavior.restore(loaded);
                        return loaded;
                    }
                    int mask(String id) { return data.getCompound("NeoForgeData").getCompound(OWNERSHIP).getInt(id); }
                }
                static SavedPlayer save(ServerPlayer player) {
                    var live = new HashMap<>(player.effects);
                    int calls = player.addCalls;
                    var saved = player.saveWithoutId(new net.minecraft.nbt.CompoundTag());
                    check(player.effects.equals(live) && player.addCalls == calls,
                        "saving only reconciles ownership, not live effects");
                    // Snapshot effects as fresh instances, modeling the serialization boundary only.
                    var effects = new HashMap<Holder<MobEffect>, MobEffectInstance>();
                    player.effects.forEach((effect, instance) -> effects.put(effect,
                        new MobEffectInstance(effect, instance.getDuration(), instance.getAmplifier(),
                            instance.isAmbient(), instance.isVisible(), instance.showIcon())));
                    return new SavedPlayer(saved, effects, player.getUUID());
                }
                static Map<String, github.ihatechpack.yichendoll.api.doll.EquippedDoll> potionEquipment() {
                    var equipment = new LinkedHashMap<String, github.ihatechpack.yichendoll.api.doll.EquippedDoll>();
                    for (var behavior : PotionDollBehavior.values()) {
                        var doll = new github.ihatechpack.yichendoll.registry.DollRegistery.Doll(behavior.name(), behavior);
                        equipment.put(doll.id(), new github.ihatechpack.yichendoll.api.doll.EquippedDoll(doll, 1));
                    }
                    return equipment;
                }
                static void respawn(ServerPlayer player) {
                    DollRuntime.onRespawn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(player));
                }
                public static void main(String[] args) {
                    var player = new ServerPlayer();
                    var context = new DollContext(player);
                    var behavior = PotionDollBehavior.YITIAOTIANYU;
                    switch (args[0]) {
                        case "supplement":
                            player.server.tick = 1;
                            behavior.onEquip(context);
                            check(player.effects.keySet().equals(Set.of(MobEffects.DOLPHINS_GRACE,
                                MobEffects.CONDUIT_POWER, MobEffects.LUCK)), "equip exact missing effects");
                            check(player.addCalls == 3, "one add per missing effect");
                            for (var instance : player.effects.values()) {
                                check(instance.getAmplifier() == 255, "max amplifier");
                                check(instance.isInfiniteDuration(), "infinite duration");
                                check(!instance.isAmbient(), "not ambient");
                                check(!instance.isVisible(), "no particles");
                                check(instance.showIcon(), "keep icon");
                            }
                            check(mask(player, "yitiaotianyu") == 7, "persist successful ownership");
                            player.effects.remove(MobEffects.LUCK);
                            player.server.tick = 39; behavior.tick(context);
                            check(!player.hasEffect(MobEffects.LUCK), "no early scan");
                            player.server.tick = 40; behavior.tick(context);
                            check(player.hasEffect(MobEffects.LUCK), "restore missing at 40");
                            check(player.addCalls == 4, "do not refresh existing");
                            behavior.tick(context);
                            check(player.addCalls == 4, "repeated scan skips existing");
                            behavior.onUnequip(context);
                            check(player.effects.isEmpty(), "reclaim owned effects");
                            check(player.getPersistentData().isEmpty(), "clear ownership on unequip");
                            break;
                        case "groups":
                            for (var entry : Map.of(PotionDollBehavior.SUIFENGYAO,
                                    Set.of(MobEffects.GLOWING, MobEffects.NIGHT_VISION),
                                    PotionDollBehavior.CNANSZ,
                                    Set.of(MobEffects.FIRE_RESISTANCE, MobEffects.SATURATION),
                                    behavior, Set.of(MobEffects.DOLPHINS_GRACE,
                                        MobEffects.CONDUIT_POWER, MobEffects.LUCK)).entrySet()) {
                                var doll = entry.getKey(); var expected = entry.getValue();
                                doll.onEquip(context);
                                check(player.effects.keySet().equals(expected), "exact group " + doll);
                                player.effects.clear();
                                int calls = player.addCalls;
                                player.server.tick = 80; doll.tick(context);
                                check(player.effects.keySet().equals(expected), "milk refill group " + doll);
                                check(player.addCalls == calls + expected.size(), "only missing group " + doll);
                                doll.onUnequip(context);
                                check(player.effects.isEmpty(), "reclaim group " + doll);
                            }
                            break;
                        case "existing":
                            var ordinary = ordinary(MobEffects.LUCK);
                            player.effects.put(MobEffects.LUCK, ordinary);
                            behavior.onEquip(context);
                            check(player.getEffect(MobEffects.LUCK) == ordinary, "preserve low finite existing effect");
                            check(player.addCalls == 2, "do not add over existing");
                            check(mask(player, "yitiaotianyu") == 3, "only missing effects marked");
                            behavior.onUnequip(context);
                            check(player.effects.size() == 1 && player.getEffect(MobEffects.LUCK) == ordinary,
                                "do not reclaim unowned effect");
                            break;
                        case "replacement":
                            behavior.onEquip(context);
                            var replacement = matching(MobEffects.LUCK);
                            player.effects.put(MobEffects.LUCK, replacement);
                            behavior.onUnequip(context);
                            check(player.effects.size() == 1 && player.getEffect(MobEffects.LUCK) == replacement,
                                "preserve replaced instance");
                            behavior.onEquip(context);
                            player.server.tick = 40; behavior.tick(context);
                            check(mask(player, "yitiaotianyu") == 3, "replacement remains unowned after scan");
                            behavior.onUnequip(context);
                            check(player.getEffect(MobEffects.LUCK) == replacement, "replacement survives later cycle");
                            break;
                        case "scanReplacement":
                            behavior.onEquip(context);
                            var other = matching(MobEffects.LUCK);
                            player.effects.put(MobEffects.LUCK, other);
                            player.server.tick = 40; behavior.tick(context);
                            check(mask(player, "yitiaotianyu") == 3, "scan drops stale identity marker");
                            behavior.onUnequip(context);
                            check(player.effects.size() == 1 && player.getEffect(MobEffects.LUCK) == other,
                                "scan does not take replacement ownership");
                            break;
                        case "restore":
                            behavior.onEquip(context);
                            PotionDollBehavior.SUIFENGYAO.onEquip(context);
                            PotionDollBehavior.CNANSZ.onEquip(context);
                            // Model deserialization as new instances, with runtime references lost.
                            player.effects.replaceAll((effect, instance) -> matching(effect));
                            PotionDollBehavior.clearAll();
                            PotionDollBehavior.restore(player);
                            check(player.effects.isEmpty(), "reconcile persisted ownership on login");
                            check(player.getPersistentData().isEmpty(), "consume persisted ownership");
                            behavior.onEquip(context);
                            check(player.effects.size() == 3, "normal equip after restore");
                            behavior.onUnequip(context);
                            check(player.effects.isEmpty(), "new ownership after restore");
                            break;
                        case "restoreUnowned":
                            var unmarked = matching(MobEffects.LUCK);
                            player.effects.put(MobEffects.LUCK, unmarked);
                            PotionDollBehavior.restore(player);
                            check(player.getEffect(MobEffects.LUCK) == unmarked, "unmarked matching effect survives restore");
                            behavior.onEquip(context);
                            var changed = ordinary(MobEffects.CONDUIT_POWER);
                            player.effects.put(MobEffects.CONDUIT_POWER, changed);
                            PotionDollBehavior.clearAll(); PotionDollBehavior.restore(player);
                            check(player.effects.size() == 2 && player.getEffect(MobEffects.LUCK) == unmarked
                                && player.getEffect(MobEffects.CONDUIT_POWER) == changed,
                                "restore preserves unmarked and changed effects");
                            break;
                        case "rejected":
                            player.rejectEffects = true;
                            behavior.onEquip(context);
                            check(player.effects.isEmpty() && player.addCalls == 3, "rejected grants remain absent");
                            check(player.getPersistentData().isEmpty(), "no rejected ownership markers");
                            player.effects.put(MobEffects.LUCK, matching(MobEffects.LUCK));
                            PotionDollBehavior.clearAll(); PotionDollBehavior.restore(player);
                            check(player.hasEffect(MobEffects.LUCK), "rejected grant cannot reclaim later effect");
                            break;
                        case "players":
                            behavior.onEquip(context);
                            var second = new ServerPlayer(); var secondContext = new DollContext(second);
                            behavior.onUnequip(secondContext);
                            check(player.effects.size() == 3, "unknown player unequip is isolated");
                            behavior.onEquip(secondContext); behavior.onUnequip(secondContext);
                            check(second.effects.isEmpty() && player.effects.size() == 3, "per-player ownership");
                            behavior.onUnequip(context);
                            check(player.effects.isEmpty(), "first player retains own ownership");
                            break;
                        case "mutated":
                            behavior.onEquip(context);
                            var mutated = player.getEffect(MobEffects.LUCK); mutated.visible = true;
                            behavior.onUnequip(context);
                            check(player.effects.size() == 1 && player.getEffect(MobEffects.LUCK) == mutated,
                                "do not reclaim instance with changed particle flag");
                            check(player.getPersistentData().isEmpty(), "changed effect no longer marked");
                            break;
                        case "saveReplacement":
                            player.server.tick = 1;
                            behavior.onEquip(context);
                            PotionDollBehavior.SUIFENGYAO.onEquip(context);
                            PotionDollBehavior.CNANSZ.onEquip(context);
                            var external = matching(MobEffects.LUCK);
                            player.effects.put(MobEffects.LUCK, external);
                            var replacedSave = save(player);
                            check(player.getEffect(MobEffects.LUCK) == external, "save leaves replacement live");
                            var replacementLoad = replacedSave.load();
                            check(replacementLoad.effects.size() == 1 && replacementLoad.hasEffect(MobEffects.LUCK),
                                "same-parameter external replacement survives save before scan and login");
                            check(replacementLoad.getEffect(MobEffects.LUCK) != external,
                                "replacement was deserialized, not a retained runtime reference");
                            check(replacedSave.mask("yitiaotianyu") == 3, "save drops replacement source bit");
                            check(replacedSave.mask("suifengyao") == 3 && replacedSave.mask("cnansz") == 3,
                                "save preserves other groups' true ownership");
                            break;
                        case "saveRemoved":
                            player.server.tick = 1; behavior.onEquip(context);
                            player.effects.clear();
                            var removedSave = save(player);
                            check(removedSave.data().getCompound("NeoForgeData").isEmpty(),
                                "save drops removed effect markers and empty ownership root");
                            check(removedSave.load().effects.isEmpty(), "removed effects stay absent after login");
                            break;
                        case "saveMutated":
                            player.server.tick = 1; behavior.onEquip(context);
                            var modified = player.getEffect(MobEffects.LUCK); modified.visible = true;
                            var modifiedSave = save(player);
                            check(modifiedSave.mask("yitiaotianyu") == 3,
                                "save drops modified instance source bit before scan");
                            modified.visible = false;
                            behavior.onUnequip(context);
                            check(player.getEffect(MobEffects.LUCK) == modified,
                                "save also drops modified runtime ownership, even if parameters later match");
                            var modifiedLoad = modifiedSave.load();
                            check(modifiedLoad.effects.size() == 1 && modifiedLoad.getEffect(MobEffects.LUCK).isVisible(),
                                "modified external state survives save and login");
                            break;
                        case "saveOwned":
                            player.server.tick = 1; behavior.onEquip(context);
                            PotionDollBehavior.SUIFENGYAO.onEquip(context);
                            PotionDollBehavior.CNANSZ.onEquip(context);
                            var ownedSave = save(player);
                            check(ownedSave.mask("yitiaotianyu") == 7 && ownedSave.mask("suifengyao") == 3
                                && ownedSave.mask("cnansz") == 3, "save retains all true owned bits");
                            save(player);
                            check(ownedSave.load().effects.isEmpty(), "true owned save/load still reclaims all groups");
                            break;
                        case "saveOtherEntity":
                            var entity = new net.minecraft.world.entity.Entity();
                            check(entity.saveWithoutId(new net.minecraft.nbt.CompoundTag()).isEmpty(),
                                "save hook ignores non-server-player entities");
                            break;
                        case "cloneWearing":
                            player.equipment = potionEquipment(); DollRuntime.update(player, player.equipment);
                            var wearingClone = new ServerPlayer(player.getUUID()); wearingClone.equipment = potionEquipment();
                            wearingClone.restoreFrom(player, true);
                            respawn(wearingClone);
                            check(wearingClone.effects.size() == 7, "equipped replacement gets all potion groups");
                            DollRuntime.update(wearingClone, Map.of());
                            check(wearingClone.effects.isEmpty(), "clone-owned effects reclaimed after last doll removed");
                            check(player.effects.isEmpty(), "forget reclaims original owned instances");
                            break;
                        case "cloneAbsent":
                            player.equipment = potionEquipment(); DollRuntime.update(player, player.equipment);
                            var emptyClone = new ServerPlayer(player.getUUID());
                            emptyClone.restoreFrom(player, true); respawn(emptyClone);
                            check(emptyClone.effects.isEmpty(), "no equipped doll means no cloned owned effects remain");
                            break;
                        case "cloneExternalOriginal":
                            var originalOrdinary = ordinary(MobEffects.LUCK);
                            player.effects.put(MobEffects.LUCK, originalOrdinary);
                            player.equipment = potionEquipment(); DollRuntime.update(player, player.equipment);
                            player.effects.put(MobEffects.CONDUIT_POWER, matching(MobEffects.CONDUIT_POWER));
                            player.getEffect(MobEffects.DOLPHINS_GRACE).visible = true;
                            var externalClone = new ServerPlayer(player.getUUID()); externalClone.equipment = potionEquipment();
                            externalClone.restoreFrom(player, true); respawn(externalClone);
                            DollRuntime.update(externalClone, Map.of());
                            check(externalClone.effects.keySet().equals(Set.of(MobEffects.LUCK, MobEffects.CONDUIT_POWER,
                                MobEffects.DOLPHINS_GRACE)), "clone preserves ordinary, replaced, and mutated original effects only");
                            check(externalClone.getEffect(MobEffects.LUCK).getDuration() == 200
                                && externalClone.getEffect(MobEffects.DOLPHINS_GRACE).isVisible(),
                                "unowned original copy retains its properties");
                            check(externalClone.getEffect(MobEffects.LUCK) != originalOrdinary,
                                "ordinary original is copied into a fresh instance");
                            break;
                        case "cloneExternalDestination":
                        case "cloneAbsentExternalDestination":
                        case "cloneDeath":
                            player.equipment = potionEquipment(); DollRuntime.update(player, player.equipment);
                            var destination = new ServerPlayer(player.getUUID());
                            if (!args[0].equals("cloneAbsentExternalDestination")) destination.equipment = potionEquipment();
                            var destinationExternal = matching(MobEffects.LUCK);
                            destination.cloneListener = target -> target.effects.put(MobEffects.LUCK, destinationExternal);
                            destination.restoreFrom(player, !args[0].equals("cloneDeath"));
                            respawn(destination);
                            check(destination.getEffect(MobEffects.LUCK) == destinationExternal,
                                "other Clone listener's same-parameter effect is never removed or replaced");
                            DollRuntime.update(destination, Map.of());
                            check(destination.effects.size() == 1 && destination.getEffect(MobEffects.LUCK) == destinationExternal,
                                "only destination external effect survives last doll removal");
                            break;
                        case "equipment":
                            var state = new EquipmentState<Integer>();
                            var equips = new AtomicInteger(); var unequips = new AtomicInteger();
                            java.util.function.Consumer<Integer> equip = count -> {
                                equips.incrementAndGet(); behavior.onEquip(context);
                            };
                            java.util.function.Consumer<Integer> unequip = count -> {
                                check(count == 1, "unequip describes last remaining copy");
                                unequips.incrementAndGet(); behavior.onUnequip(context);
                            };
                            state.replace(Map.of("yitiaotianyu", 2), 1, equip, unequip);
                            check(equips.get() == 1 && player.addCalls == 3, "two copies equip once");
                            state.replace(Map.of("yitiaotianyu", 1), 2, equip, unequip);
                            check(equips.get() == 1 && unequips.get() == 0 && player.effects.size() == 3,
                                "two to one does not unequip or stack");
                            state.replace(Map.of(), 3, equip, unequip);
                            check(unequips.get() == 1 && player.effects.isEmpty(), "last copy reclaims exactly once");
                            state.replace(Map.of(), 4, equip, unequip);
                            check(unequips.get() == 1, "empty snapshot does not repeat unequip");
                            break;
                        default: throw new AssertionError("unknown scenario " + args[0]);
                    }
                }
            }""")
        for relative in ["api/doll/DollBehavior.java", "common/doll/EquipmentState.java",
                         "common/doll/DollRuntime.java", "common/doll/behavior/PotionDollBehavior.java"]:
            sources.append(str(JAVA / relative))
        if SAVE_MIXIN.exists():
            sources.append(str(SAVE_MIXIN))
        if CLONE_MIXIN.exists():
            sources.append(str(CLONE_MIXIN))
        cls.classes = directory / "classes"
        result = subprocess.run(["javac", "--release", "21", "-d", str(cls.classes), *sources],
                                capture_output=True, text=True)
        if result.returncode:
            raise AssertionError("Potion regression compilation failed:\n" + result.stdout + result.stderr)

    def run_case(self, case):
        result = subprocess.run(["java", "-cp", str(self.classes),
                                 BASE + ".common.doll.PotionDollsRunner", case],
                                capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_immediate_and_interval_missing_detection(self):
        self.run_case("supplement")

    def test_exact_groups_and_milk_recovery(self):
        self.run_case("groups")

    def test_existing_effect_is_not_owned(self):
        self.run_case("existing")

    def test_replaced_instance_is_not_reclaimed(self):
        self.run_case("replacement")

    def test_scan_drops_replaced_instance_ownership(self):
        self.run_case("scanReplacement")

    def test_persisted_ownership_reconciled_on_login(self):
        self.run_case("restore")

    def test_restore_preserves_unowned_and_changed_effects(self):
        self.run_case("restoreUnowned")

    def test_rejected_grants_do_not_write_ownership(self):
        self.run_case("rejected")

    def test_player_ownership_is_isolated(self):
        self.run_case("players")

    def test_mutated_owned_instance_is_not_reclaimed(self):
        self.run_case("mutated")

    def test_save_preserves_same_parameter_external_replacement(self):
        self.run_case("saveReplacement")

    def test_save_drops_removed_effect_markers(self):
        self.run_case("saveRemoved")

    def test_save_drops_mutated_effect_ownership(self):
        self.run_case("saveMutated")

    def test_save_load_reclaims_true_owned_effects(self):
        self.run_case("saveOwned")

    def test_save_hook_ignores_non_server_player(self):
        self.run_case("saveOtherEntity")

    def test_player_save_mixin_registered(self):
        config = json.loads((ROOT / "src/main/resources/mixins/ihateyichendoll.mixins.json").read_text())
        self.assertIn("common.PotionOwnershipSaveMixin", config["mixins"])
        source = SAVE_MIXIN.read_text()
        self.assertIn("@Mixin(Entity.class)", source)
        self.assertIn('method = "saveWithoutId(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"', source)
        self.assertIn('@At("HEAD")', source)

    def test_clone_wearing_then_last_doll_removal_reclaims_owned_effects(self):
        self.run_case("cloneWearing")

    def test_clone_without_dolls_does_not_keep_owned_effects(self):
        self.run_case("cloneAbsent")

    def test_clone_preserves_original_external_effects(self):
        self.run_case("cloneExternalOriginal")

    def test_clone_preserves_destination_listener_same_parameter_effect(self):
        self.run_case("cloneExternalDestination")

    def test_clone_without_dolls_preserves_destination_listener_effect(self):
        self.run_case("cloneAbsentExternalDestination")

    def test_death_clone_preserves_new_external_effect(self):
        self.run_case("cloneDeath")

    def test_clone_copy_mixin_registered(self):
        config = json.loads((ROOT / "src/main/resources/mixins/ihateyichendoll.mixins.json").read_text())
        self.assertIn("common.PotionEffectCloneMixin", config["mixins"])
        source = CLONE_MIXIN.read_text()
        self.assertIn("@Mixin(ServerPlayer.class)", source)
        self.assertIn('method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V"', source)
        self.assertIn('Lnet/minecraft/server/level/ServerPlayer;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z', source)
        self.assertIn('@Local(argsOnly = true) ServerPlayer source', source)
        self.assertIn('@Local(ordinal = 0) MobEffectInstance sourceEffect', source)

    def test_equipment_copies_only_unequip_at_zero(self):
        self.run_case("equipment")


if __name__ == "__main__":
    unittest.main()
