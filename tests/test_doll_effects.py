"""Run real doll logic against small MC/AE2 boundary fakes, without a game launch.

Run: python3 -m unittest discover -s tests -v
The mining fake inherits the mixin to model its woven methods; this does not
replace verifying injection against the real Minecraft bytecode / game runtime.
"""
import json
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = "github.ihatechpack.yichendoll"
JAVA = ROOT / "src/main/java"
MIXIN = JAVA / BASE.replace(".", "/") / "mixin/common/PlayerMiningMixin.java"


class DollEffectsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temp = tempfile.TemporaryDirectory()
        cls.addClassCleanup(cls.temp.cleanup)
        directory = Path(cls.temp.name)
        sources = []

        def stub(name, body):
            package, _, simple = name.rpartition(".")
            path = directory / (name.replace(".", "/") + ".java")
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(f"package {package};\n{body}\n")
            sources.append(str(path))

        stub("net.minecraft.core.Holder", "public record Holder<T>(T value) {}")
        stub("net.minecraft.resources.ResourceLocation", "public record ResourceLocation(String id) {}")
        stub("net.minecraft.world.entity.ai.attributes.Attribute", "public class Attribute {}")
        stub("net.minecraft.world.entity.ai.attributes.Attributes", """
            public class Attributes {
                public static final net.minecraft.core.Holder<Attribute> MAX_HEALTH =
                    new net.minecraft.core.Holder<>(new Attribute());
            }""")
        stub("net.minecraft.server.MinecraftServer", """
            public class MinecraftServer { public int tick;
                public int getTickCount() { return tick; }
            }""")
        stub("net.minecraft.world.item.ItemStack", """
            public class ItemStack { public long fe;
                public ItemStack(long fe) { this.fe = fe; }
            }""")
        parent = f"extends {BASE}.mixin.common.PlayerMiningMixin" if MIXIN.exists() else ""
        stub("net.minecraft.world.entity.player.Player", """
            public class Player PARENT {
                public boolean wearing;
                public static class Abilities { public boolean flying; }
                public final Abilities abilities = new Abilities();
                public Abilities getAbilities() { return abilities; }
            }""".replace("PARENT", parent))
        stub("net.minecraft.server.level.ServerPlayer", """
            public class ServerPlayer extends net.minecraft.world.entity.player.Player {
                public float health = 17, max = 20;
                public long networkFE;
                public final net.minecraft.server.MinecraftServer server = new net.minecraft.server.MinecraftServer();
                public net.minecraft.server.MinecraftServer getServer() { return server; }
                public float getHealth() { return health; }
                public float getMaxHealth() { return max; }
                public boolean isAlive() { return health > 0; }
                public void setHealth(float health) { this.health = health; }
            }""")
        stub(BASE + ".Res", """
            public class Res { public static net.minecraft.resources.ResourceLocation rl(String s) {
                return new net.minecraft.resources.ResourceLocation(s);
            }}""")
        stub(BASE + ".api.doll.DollContext", """
            public record DollContext(net.minecraft.server.level.ServerPlayer player,
                java.util.List<net.minecraft.world.item.ItemStack> stacks) {}""")
        stub(BASE + ".api.doll.DollBehavior", """
            public interface DollBehavior {
                default void onEquip(DollContext c) {}
                default void onUnequip(DollContext c) {}
                default void tick(DollContext c) {}
            }""")
        stub(BASE + ".registry.ModAttributes", """
            public class ModAttributes {
                public static final net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>
                    ALL_DAMAGE = new net.minecraft.core.Holder<>(new net.minecraft.world.entity.ai.attributes.Attribute()),
                    INVULNERABILITY_TICKS = ALL_DAMAGE, DAMAGE_REDUCTION = ALL_DAMAGE;
                public static void update(net.minecraft.server.level.ServerPlayer p,
                    net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> a,
                    net.minecraft.resources.ResourceLocation id, double value) {
                    if (a == net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) p.max = 20 + (float) value;
                }
            }""")
        stub(BASE + ".integration.ae2.MeNetworkStats", """
            public record MeNetworkStats(double usage, int keyTypes, int nodes, long storedFE) {}""")
        stub(BASE + ".integration.ae2.AE2Bridge", """
            public class AE2Bridge {
                public static final AE2Bridge INSTANCE = new AE2Bridge();
                public int attempts; public long lastCost;
                public static AE2Bridge get() { return INSTANCE; }
                public MeNetworkStats sample(net.minecraft.server.level.ServerPlayer p,
                    net.minecraft.world.item.ItemStack s) { return new MeNetworkStats(0, 0, 0, p.networkFE); }
                public boolean consumeFE(net.minecraft.server.level.ServerPlayer p,
                    net.minecraft.world.item.ItemStack s, long cost) {
                    attempts++; lastCost = cost;
                    if (s.fe < cost) return false;
                    s.fe -= cost; return true;
                }
            }""")
        stub(BASE + ".integration.appflux.AppliedFluxBridge", """
            public interface AppliedFluxBridge {
                long storedFE(Object counter);
                default boolean consumeFE(Object storage, net.minecraft.server.level.ServerPlayer p, long amount) {
                    return false;
                }
            }""")
        stub("appeng.api.stacks.KeyCounter", """
            public class KeyCounter { public long get(Object key) { return 0; } }""")
        stub("com.glodblock.github.appflux.common.me.key.type.EnergyType", "public enum EnergyType { FE }")
        stub("com.glodblock.github.appflux.common.me.key.FluxKey", """
            public class FluxKey {
                public static final FluxKey FE = new FluxKey();
                public static FluxKey of(com.glodblock.github.appflux.common.me.key.type.EnergyType type) { return FE; }
            }""")
        stub("appeng.api.config.Actionable", "public enum Actionable { SIMULATE, MODULATE }")
        stub("appeng.api.networking.security.IActionSource", """
            public record IActionSource(net.minecraft.server.level.ServerPlayer player) {
                public static IActionSource ofPlayer(net.minecraft.server.level.ServerPlayer p) { return new IActionSource(p); }
            }""")
        stub("appeng.api.storage.MEStorage", """
            public class MEStorage {
                public long fe; public boolean denied; public int simulations, extractions;
                public net.minecraft.server.level.ServerPlayer actor;
                public long extract(com.glodblock.github.appflux.common.me.key.FluxKey key, long amount,
                    appeng.api.config.Actionable mode, appeng.api.networking.security.IActionSource source) {
                    if (key != com.glodblock.github.appflux.common.me.key.FluxKey.FE) throw new AssertionError("not FE");
                    actor = source.player();
                    if (mode == appeng.api.config.Actionable.SIMULATE) simulations++; else extractions++;
                    long used = denied ? 0 : Math.min(fe, amount);
                    if (mode == appeng.api.config.Actionable.MODULATE) fe -= used;
                    return used;
                }
            }""")
        stub(BASE + ".common.doll.DollRuntime", """
            public class DollRuntime {
                public static boolean isWearing(net.minecraft.world.entity.player.Player p, String id) { return p.wearing; }
            }""")
        stub(BASE + ".integration.curios.CuriosBridge", """
            public class CuriosBridge {
                public static CuriosBridge get() { return new CuriosBridge(); }
                public java.util.List<net.minecraft.world.item.ItemStack> activeStacks(
                    net.minecraft.world.entity.player.Player p, String id) {
                    return p.wearing ? java.util.List.of(new net.minecraft.world.item.ItemStack(0)) : java.util.List.of();
                }
            }""")
        stub("org.spongepowered.asm.mixin.Mixin", "public @interface Mixin { Class<?>[] value(); }")
        stub("org.spongepowered.asm.mixin.injection.At", "public @interface At { String value(); String target(); }")
        stub("com.llamalad7.mixinextras.injector.ModifyExpressionValue", """
            public @interface ModifyExpressionValue {
                String method(); org.spongepowered.asm.mixin.injection.At at();
            }""")
        stub("regression.Runner", """
            import net.minecraft.server.level.ServerPlayer;
            import net.minecraft.world.item.ItemStack;
            import github.ihatechpack.yichendoll.api.doll.DollContext;
            import github.ihatechpack.yichendoll.common.doll.behavior.MeDollBehavior;
            import github.ihatechpack.yichendoll.integration.ae2.AE2Bridge;
            public class Runner {
                static void check(boolean b, String message) { if (!b) throw new AssertionError(message); }
                public static void main(String[] args) throws Exception {
                    String test = args[0];
                    var p = new ServerPlayer(); p.server.tick = 40;
                    var first = new ItemStack(10000); var second = new ItemStack(10000);
                    var ctx = new DollContext(p, java.util.List.of(first, second));
                    var behavior = MeDollBehavior.YUANXI; var bridge = AE2Bridge.get();
                    switch (test) {
                        case "heals":
                            behavior.tick(ctx);
                            check(p.health == 20 && first.fe == 9700 && second.fe == 10000, "heal once for 300 FE");
                            break;
                        case "interval":
                            p.server.tick = 39; behavior.tick(ctx);
                            check(p.health == 17 && bridge.attempts == 0, "only every 40 ticks"); break;
                        case "equip":
                            behavior.onEquip(ctx); check(p.health == 17 && bridge.attempts == 0, "no healing on equip"); break;
                        case "full": case "dead":
                            p.health = test.equals("full") ? 20 : 0; float before = p.health;
                            behavior.tick(ctx); check(p.health == before && bridge.attempts == 0, "no full/dead payment"); break;
                        case "insufficient":
                            first.fe = second.fe = 200; behavior.tick(ctx);
                            check(p.health == 17 && first.fe == 200 && second.fe == 200, "no partial payment or pooling"); break;
                        case "fallback":
                            first.fe = 200; second.fe = 300; behavior.tick(ctx);
                            check(p.health == 20 && first.fe == 200 && second.fe == 0, "try later network"); break;
                        case "fraction":
                            p.health = 19.125f; behavior.tick(ctx);
                            check(p.health == 20 && first.fe == 9912, "ceil missing health cost"); break;
                        case "updatedMax":
                            p.networkFE = 1_000_000_000; first.fe = 1_000_000;
                            behavior.tick(ctx);
                            check(p.max > 20 && p.health == p.max && bridge.lastCost == (long)Math.ceil((p.max - 17d)*100),
                                "update maximum before healing"); break;
                        case "otherDoll":
                            MeDollBehavior.M_XM.tick(ctx);
                            check(p.health == 17 && bridge.attempts == 0, "only Yuanxi heals"); break;
                        case "fluxExact": case "fluxShort": case "fluxDenied":
                            var storage = new appeng.api.storage.MEStorage();
                            storage.fe = test.equals("fluxShort") ? 299 : 300;
                            storage.denied = test.equals("fluxDenied");
                            github.ihatechpack.yichendoll.integration.appflux.AppliedFluxBridge flux =
                                new github.ihatechpack.yichendoll.integration.appflux.loaded.LoadedAppliedFluxBridge();
                            boolean paid = flux.consumeFE(storage, p, 300);
                            boolean enough = test.equals("fluxExact");
                            check(paid == enough && storage.simulations == 1, "simulate live FE before payment");
                            check(storage.extractions == (enough ? 1 : 0), "no extraction if unavailable/denied");
                            check(storage.fe == (enough ? 0 : (test.equals("fluxShort") ? 299 : 300)), "exact FE accounting");
                            check(storage.actor == p, "use player action source"); break;
                        case "mining":
                            var mixin = Class.forName("github.ihatechpack.yichendoll.mixin.common.PlayerMiningMixin");
                            var method = mixin.getDeclaredMethod("yichendoll$miningOnGround", boolean.class);
                            method.setAccessible(true);
                            for (boolean server : new boolean[]{false, true}) {
                                net.minecraft.world.entity.player.Player player = server ? p : new net.minecraft.world.entity.player.Player();
                                for (boolean ground : new boolean[]{false, true})
                                    for (boolean flying : new boolean[]{false, true})
                                        for (boolean wearing : new boolean[]{false, true}) {
                                            player.abilities.flying = flying; player.wearing = wearing;
                                            check((boolean)method.invoke(player, ground) == (ground || (flying && wearing)),
                                                "only flying howxu removes airborne penalty on both sides");
                                        }
                            } break;
                        default: throw new AssertionError(test);
                    }
                }
            }""")
        for relative in ["common/doll/behavior/MeDollBehavior.java",
                         "integration/appflux/loaded/LoadedAppliedFluxBridge.java"]:
            sources.append(str(JAVA / BASE.replace(".", "/") / relative))
        if MIXIN.exists():
            sources.append(str(MIXIN))
        cls.classes = directory / "classes"
        subprocess.run(["javac", "--release", "21", "-d", str(cls.classes), *sources],
                       check=True, capture_output=True, text=True)

    def run_case(self, case):
        result = subprocess.run(["java", "-cp", str(self.classes), "regression.Runner", case],
                                capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_healing(self):
        for case in ["heals", "interval", "equip", "full", "dead", "insufficient", "fallback",
                     "fraction", "updatedMax", "otherDoll"]:
            with self.subTest(case=case):
                self.run_case(case)

    def test_network_payment(self):
        for case in ["fluxExact", "fluxShort", "fluxDenied"]:
            with self.subTest(case=case):
                self.run_case(case)

    def test_mining_conditions(self):
        self.run_case("mining")

    def test_optional_integrations_absent(self):
        # Compile the real bridge interfaces with no AE2 or Applied Flux classes.
        with tempfile.TemporaryDirectory() as temp:
            directory = Path(temp)
            stubs = {
                "net/neoforged/fml/ModList.java": """
                    package net.neoforged.fml;
                    public class ModList {
                        public static ModList get() { return new ModList(); }
                        public boolean isLoaded(String id) { return id.equals(System.getProperty("loaded", "")); }
                    }""",
                "net/minecraft/server/level/ServerPlayer.java":
                    "package net.minecraft.server.level; public class ServerPlayer {}",
                "net/minecraft/world/item/ItemStack.java":
                    "package net.minecraft.world.item; public class ItemStack {}",
                "Absent.java": """
                    import github.ihatechpack.yichendoll.integration.ae2.AE2Bridge;
                    import github.ihatechpack.yichendoll.integration.appflux.AppliedFluxBridge;
                    public class Absent {
                        public static void main(String[] args) {
                            if (!System.getProperty("loaded", "").equals("ae2")
                                    && AE2Bridge.get().consumeFE(null, null, 100)) throw new AssertionError("AE2 fallback");
                            var flux = AppliedFluxBridge.get();
                            if (flux.storedFE(null) != 0 || flux.consumeFE(null, null, 100))
                                throw new AssertionError("Applied Flux fallback");
                        }
                    }""",
            }
            files = []
            for name, source in stubs.items():
                path = directory / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(source)
                files.append(str(path))
            for name in ["integration/ae2/AE2Bridge.java", "integration/ae2/MeNetworkStats.java",
                         "integration/appflux/AppliedFluxBridge.java"]:
                files.append(str(JAVA / BASE.replace(".", "/") / name))
            subprocess.run(["javac", "--release", "21", "-d", temp, *files],
                           check=True, capture_output=True, text=True)
            for loaded in ["", "ae2", "appflux"]:
                with self.subTest(loaded=loaded):
                    result = subprocess.run(["java", f"-Dloaded={loaded}", "-cp", temp, "Absent"],
                                            capture_output=True, text=True)
                    self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_mining_injection_registered(self):
        config = json.loads((ROOT / "src/main/resources/mixins/ihateyichendoll.mixins.json").read_text())
        self.assertIn("common.PlayerMiningMixin", config["mixins"])
        source = MIXIN.read_text()
        self.assertIn('method = "getDigSpeed"', source)
        self.assertIn('Lnet/minecraft/world/entity/player/Player;onGround()Z', source)


if __name__ == "__main__":
    unittest.main()
