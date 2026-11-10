"""Real visual helpers against small MC fakes; static mixin checks are not weaving.

Run: python3 -m unittest discover -s tests -p 'test_doll_visuals.py' -v
"""
import json
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = "github.ihatechpack.yichendoll"
JAVA = ROOT / "src/main/java" / BASE.replace(".", "/")
CONFIG = ROOT / "src/main/resources/mixins/ihateyichendoll.mixins.json"
CLIENT_MIXINS = (
    "LevelRendererEffectsMixin", "LightTextureEffectsMixin", "MobEffectFogMixin",
    "FogRendererEffectsMixin", "LocalPlayerEffectsMixin", "GameRendererEffectsMixin", "GuiEffectsMixin",
)


class DollVisualHelpersTest(unittest.TestCase):
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

        stub(BASE + ".IHateYiChenDoll", 'public class IHateYiChenDoll { public static final String MOD_ID = "ihateyichendoll"; }')
        stub("net.neoforged.api.distmarker.Dist", "public enum Dist { CLIENT, DEDICATED_SERVER }")
        stub("net.neoforged.bus.api.SubscribeEvent", "public @interface SubscribeEvent {}")
        stub("net.neoforged.fml.common.EventBusSubscriber", """
            import net.neoforged.api.distmarker.Dist;
            public @interface EventBusSubscriber { String modid(); Dist[] value(); }""")
        stub("net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent", """
            import net.minecraft.world.entity.player.Player;
            public class RenderBlockScreenEffectEvent {
                public enum OverlayType { FIRE, BLOCK, WATER }
                private final Player player;
                private final OverlayType type;
                public boolean canceled;
                public int cancelCalls;
                public RenderBlockScreenEffectEvent(Player player, OverlayType type) {
                    this.player = player; this.type = type;
                }
                public Player getPlayer() { return player; }
                public OverlayType getOverlayType() { return type; }
                public void setCanceled(boolean value) { canceled = value; cancelCalls++; }
            }""")
        stub("net.minecraft.core.Holder", "public record Holder<T>(T value) {}")
        stub("net.minecraft.world.effect.MobEffect", "public record MobEffect(String id) {}")
        stub("net.minecraft.world.effect.MobEffects", """
            import net.minecraft.core.Holder;
            public class MobEffects {
                public static final Holder<MobEffect> BLINDNESS = effect("blindness"),
                    DARKNESS = effect("darkness"), CONFUSION = effect("confusion"),
                    NIGHT_VISION = effect("night_vision"), FIRE_RESISTANCE = effect("fire_resistance");
                private static Holder<MobEffect> effect(String id) { return new Holder<>(new MobEffect(id)); }
            }""")
        stub("net.minecraft.world.entity.LivingEntity", """
            public class LivingEntity {
                public final java.util.Set<String> cached = new java.util.HashSet<>();
                public final java.util.Set<String> synced = new java.util.HashSet<>();
            }""")
        stub("net.minecraft.world.entity.player.Player", """
            public class Player extends net.minecraft.world.entity.LivingEntity {}""")
        stub("net.minecraft.server.level.ServerPlayer", """
            public class ServerPlayer extends net.minecraft.world.entity.player.Player {}""")
        stub(BASE + ".common.doll.DollRuntime", """
            public class DollRuntime {
                public static int calls;
                public static boolean isWearing(net.minecraft.world.entity.LivingEntity entity, String id) {
                    calls++; return entity.cached.contains(id);
                }
            }""")
        stub(BASE + ".integration.curios.CuriosBridge", """
            public class CuriosBridge {
                public static final CuriosBridge INSTANCE = new CuriosBridge();
                public static boolean available = true;
                public static int calls;
                public static CuriosBridge get() { return INSTANCE; }
                public java.util.List<String> activeStacks(net.minecraft.world.entity.LivingEntity entity, String id) {
                    calls++; return available && entity.synced.contains(id) ? java.util.List.of(id) : java.util.List.of();
                }
            }""")
        stub("net.minecraft.client.player.LocalPlayer", f"""
            import {BASE}.client.PortalVisualState;
            public class LocalPlayer extends net.minecraft.world.entity.player.Player implements PortalVisualState.Access {{
                public final PortalVisualState portal = new PortalVisualState();
                public PortalVisualState yichendoll$portalVisualState() {{ return portal; }}
            }}""")
        stub("visualtest.Runner", f"""
            import {BASE}.client.*;
            import {BASE}.common.doll.*;
            import {BASE}.integration.curios.CuriosBridge;
            import net.minecraft.world.effect.MobEffects;
            import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
            import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent.OverlayType;
            import net.minecraft.world.entity.LivingEntity;
            import net.minecraft.world.entity.player.Player;
            import net.minecraft.server.level.ServerPlayer;
            import net.minecraft.client.player.LocalPlayer;
            public class Runner {{
                private static void near(float actual, float expected) {{
                    assert Math.abs(actual - expected) < 0.00001F : actual + " != " + expected;
                }}
                public static void main(String[] args) {{
                    switch (args[0]) {{
                        case "screen" -> {{
                            for (boolean wearing : new boolean[] {{false, true}}) {{
                                var player = new Player();
                                if (wearing) player.synced.add("cnansz");
                                for (var type : OverlayType.values()) {{
                                    var event = new RenderBlockScreenEffectEvent(player, type);
                                    DollScreenEffects.onScreenEffect(event);
                                    boolean expected = wearing && type == OverlayType.FIRE;
                                    assert event.canceled == expected : type;
                                    assert event.cancelCalls == (expected ? 1 : 0) : type;
                                }}
                            }}
                        }}
                        case "screen_without_curios" -> {{
                            var player = new Player(); player.synced.add("cnansz");
                            CuriosBridge.available = false;
                            var event = new RenderBlockScreenEffectEvent(player, OverlayType.FIRE);
                            DollScreenEffects.onScreenEffect(event);
                            assert !event.canceled && event.cancelCalls == 0;
                        }}
                        case "screen_preserve" -> {{
                            var player = new Player(); player.synced.add("yuanxi_19_new");
                            for (var type : OverlayType.values()) {{
                                var event = new RenderBlockScreenEffectEvent(player, type);
                                event.canceled = true;
                                DollScreenEffects.onScreenEffect(event);
                                assert event.canceled && event.cancelCalls == 0;
                            }}
                        }}
                        case "portal" -> {{
                            var state = new PortalVisualState();
                            near(state.current(), 0); near(state.previous(), 0);
                            state.tick(true); near(state.current(), 0.0125F); near(state.previous(), 0);
                            for (int i = 0; i < 200; i++) state.tick(true);
                            near(state.current(), 1);
                            state.tick(false); near(state.previous(), 1); near(state.current(), 0.95F);
                            for (int i = 0; i < 40; i++) state.tick(false);
                            near(state.current(), 0);
                        }}
                        case "sides" -> {{
                            var server = new ServerPlayer();
                            server.cached.add("yuanxi_19_new");
                            assert DollEffectProtection.isWearing(server, "yuanxi_19_new");
                            assert CuriosBridge.calls == 0;
                            server.cached.clear(); server.synced.add("yuanxi_19_new");
                            assert !DollEffectProtection.isWearing(server, "yuanxi_19_new");
                            var client = new Player(); client.synced.add("yuanxi_19_new");
                            int calls = DollRuntime.calls;
                            assert DollEffectProtection.isWearing(client, "yuanxi_19_new");
                            assert DollRuntime.calls == calls;
                            CuriosBridge.available = false;
                            assert !DollEffectProtection.isWearing(client, "yuanxi_19_new");
                        }}
                        case "targets" -> {{
                            var client = new Player(); client.synced.add("yuanxi_19_new");
                            assert DollEffectProtection.ignores(client, MobEffects.BLINDNESS);
                            assert DollEffectProtection.ignores(client, MobEffects.DARKNESS);
                            assert DollEffectProtection.ignores(client, MobEffects.CONFUSION);
                            assert !DollEffectProtection.ignores(client, MobEffects.NIGHT_VISION);
                            assert !DollEffectProtection.ignores(client, MobEffects.FIRE_RESISTANCE);
                            client.synced.clear();
                            assert !DollEffectProtection.ignores(client, MobEffects.BLINDNESS);
                        }}
                        case "nonplayers" -> {{
                            assert !DollEffectProtection.isWearing(null, "yuanxi_19_new");
                            var mob = new LivingEntity(); mob.synced.add("yuanxi_19_new"); mob.cached.add("yuanxi_19_new");
                            assert !DollEffectProtection.isWearing(mob, "yuanxi_19_new");
                            assert CuriosBridge.calls == 0 && DollRuntime.calls == 0;
                        }}
                        case "rainbow" -> {{
                            assert DollVisuals.rainbow(0, 0) == 0xFF0000;
                            assert DollVisuals.rainbow(200, 0) == 0xFF0000;
                            assert DollVisuals.rainbow(66, 2F/3) == 0x00FF00;
                            assert DollVisuals.rainbow(133, 1F/3) == 0x0000FF;
                            for (int i = 0; i < 1000; i++) {{
                                int color = DollVisuals.rainbow(i, 0.5F);
                                assert color >= 0 && color <= 0xFFFFFF;
                                assert color == DollVisuals.rainbow(i + 200, 0.5F);
                            }}
                            assert (DollVisuals.rainbow(199, 0.99F) & 0xFFFF) <= 1;
                            assert (DollVisuals.rainbow(200, 0.01F) & 0xFFFF) <= 0x100;
                        }}
                        case "spinning" -> {{
                            var player = new LocalPlayer();
                            player.portal.tick(true); player.portal.tick(true);
                            near(DollVisuals.spinning(player, false, 0.8F), 0.8F);
                            near(DollVisuals.spinning(player, true, 0.7F), 0.7F);
                            player.synced.add("yuanxi_19_new");
                            near(DollVisuals.spinning(player, false, 0.8F), 0.025F);
                            near(DollVisuals.spinning(player, true, 0.7F), 0.0125F);
                            player.synced.clear();
                            near(DollVisuals.spinning(player, false, 0.8F), 0.8F);
                        }}
                        default -> throw new AssertionError(args[0]);
                    }}
                }}
            }}""")
        sources.extend(str(JAVA / path) for path in (
            "common/doll/DollEffectProtection.java", "client/PortalVisualState.java", "client/DollVisuals.java", "client/DollScreenEffects.java"))
        cls.classes = directory / "classes"
        result = subprocess.run(["javac", "-d", str(cls.classes), *sources], capture_output=True, text=True)
        if result.returncode:
            raise AssertionError("Helper compilation failed:\n" + result.stdout + result.stderr)

    def run_case(self, case):
        result = subprocess.run(["java", "-ea", "-cp", str(self.classes), "visualtest.Runner", case],
                                capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_screen_fire_only_for_cnansz_wearer(self): self.run_case("screen")
    def test_screen_without_curios_is_unchanged(self): self.run_case("screen_without_curios")
    def test_screen_unrelated_doll_preserves_existing_cancellation(self): self.run_case("screen_preserve")

    def test_portal_component_rises_clamps_and_decays(self): self.run_case("portal")
    def test_server_cache_and_client_synced_bridge(self): self.run_case("sides")
    def test_only_three_negative_effects_are_ignored(self): self.run_case("targets")
    def test_nonplayer_and_null_safe(self): self.run_case("nonplayers")
    def test_rainbow_full_cycle_and_continuity(self): self.run_case("rainbow")
    def test_spinning_only_substitutes_portal_for_wearer(self): self.run_case("spinning")


class DollScreenResourcesTest(unittest.TestCase):
    def test_screen_handler_is_client_global_bus_subscriber(self):
        source = (JAVA / "client/DollScreenEffects.java").read_text()
        self.assertIn('@EventBusSubscriber(modid = IHateYiChenDoll.MOD_ID, value = Dist.CLIENT)', source)
        self.assertIn("@SubscribeEvent", source)
        self.assertNotIn("Bus.MOD", source)
        self.assertIn('DollEffectProtection.isWearing(event.getPlayer(), "cnansz")', source)

    def test_four_doll_translation_keys_are_nonempty(self):
        descriptions = {"yitiaotianyu": 2, "suifengyao": 1, "cnansz": 1, "yuanxi_19_new": 2}
        for language in ("zh_cn", "en_us"):
            path = ROOT / f"src/main/resources/assets/ihateyichendoll/lang/{language}.json"
            with path.open() as stream:
                translations = json.load(stream)
            for doll, count in descriptions.items():
                keys = [f"block.ihateyichendoll.{doll}", f"tooltip.ihateyichendoll.effect.{doll}"]
                keys += [f"item.ihateyichendoll.doll_tip.{doll}.{i}" for i in range(count)]
                for key in keys:
                    with self.subTest(language=language, key=key):
                        self.assertIsInstance(translations.get(key), str)
                        self.assertTrue(translations.get(key, "").strip())

    def test_generated_models_states_and_loot_for_four_dolls(self):
        generated = ROOT / "src/generated/resources"
        for doll in ("yitiaotianyu", "suifengyao", "cnansz", "yuanxi_19_new"):
            with self.subTest(doll=doll):
                model = f"ihateyichendoll:block/{doll}"
                with (generated / f"assets/ihateyichendoll/models/item/{doll}.json").open() as stream:
                    self.assertEqual(json.load(stream)["parent"], model)
                with (generated / f"assets/ihateyichendoll/blockstates/{doll}.json").open() as stream:
                    variants = json.load(stream)["variants"]
                self.assertEqual(set(variants), {
                    f"facing={direction},waterlogged={waterlogged}"
                    for direction in ("north", "south", "east", "west")
                    for waterlogged in ("true", "false")
                })
                self.assertTrue(all(value["model"] == model for value in variants.values()))
                with (ROOT / f"src/main/resources/assets/ihateyichendoll/models/block/{doll}.json").open() as stream:
                    self.assertIsInstance(json.load(stream), dict)
                with (generated / f"data/ihateyichendoll/loot_table/blocks/{doll}.json").open() as stream:
                    loot = json.load(stream)
                self.assertEqual(loot["pools"][0]["entries"][0]["name"], f"ihateyichendoll:{doll}")


class DollVisualMixinContractsTest(unittest.TestCase):
    def source(self, name, side="client"):
        return (JAVA / f"mixin/{side}/{name}.java").read_text()

    def test_registration_sides_and_existing_save_clone_hooks(self):
        config = json.loads(CONFIG.read_text())
        self.assertIn("common.PlayerEffectProtectionMixin", config["mixins"])
        self.assertIn("common.PotionOwnershipSaveMixin", config["mixins"])
        self.assertIn("common.PotionEffectCloneMixin", config["mixins"])
        for name in CLIENT_MIXINS:
            self.assertIn("client." + name, config["client"])
            self.assertNotIn("client." + name, config["mixins"])
        self.assertEqual(config["injectors"]["defaultRequire"], 1)

    def test_exact_has_effect_owners_and_scoped_methods(self):
        for name, owner, methods, side in (
            ("PlayerEffectProtectionMixin", "world/entity/player/Player", ('"attack"',), "common"),
            ("LocalPlayerEffectsMixin", "client/player/LocalPlayer", ('"aiStep"', '"canStartSprinting"'), "client"),
            ("MobEffectFogMixin", "world/entity/LivingEntity", ('"isEnabled"',), "client"),
            ("FogRendererEffectsMixin", "world/entity/LivingEntity", ('"setupColor"',), "client"),
            ("LevelRendererEffectsMixin", "world/entity/LivingEntity", ('"doesMobEffectBlockSky"',), "client"),
            ("GameRendererEffectsMixin", "client/player/LocalPlayer", ('"render"', '"renderLevel"'), "client"),
            ("GuiEffectsMixin", "client/player/LocalPlayer", ('"renderCameraOverlays"',), "client"),
        ):
            with self.subTest(name=name):
                source = self.source(name, side)
                self.assertIn(f"Lnet/minecraft/{owner};hasEffect(Lnet/minecraft/core/Holder;)Z", source)
                for method in methods: self.assertIn(method, source)
                self.assertIn("DollEffectProtection.ignores", source)
                self.assertNotIn("require = 0", source)
                self.assertNotIn("removeEffect", source)

    def test_portal_field_reads_preserved_separately(self):
        for name in ("GameRendererEffectsMixin", "GuiEffectsMixin"):
            source = self.source(name)
            for field in ("oSpinningEffectIntensity", "spinningEffectIntensity"):
                self.assertIn(f"Lnet/minecraft/client/player/LocalPlayer;{field}:F", source)
            self.assertIn("DollVisuals.spinning(player, true, original.call(player))", source)
            self.assertIn("DollVisuals.spinning(player, false, original.call(player))", source)
        source = self.source("LocalPlayerEffectsMixin")
        self.assertIn('method = "handleConfusionTransitionEffect", at = @At("HEAD")', source)
        self.assertIn("player.portalProcess.isInsidePortalThisTick()", source)
        self.assertNotIn("setAsInsidePortalThisTick", source)

    def test_package_private_fog_interface_and_static_color_handler(self):
        source = self.source("MobEffectFogMixin")
        self.assertIn('@Mixin(targets = "net.minecraft.client.renderer.FogRenderer$MobEffectFogFunction")', source)
        self.assertIn("public interface MobEffectFogMixin", source)
        self.assertNotIn("import net.minecraft.client.renderer.FogRenderer", source)
        self.assertIn("private static boolean", self.source("FogRendererEffectsMixin"))

    def test_outline_and_darkness_are_local_not_global(self):
        source = self.source("LevelRendererEffectsMixin")
        self.assertIn("Lnet/minecraft/world/entity/Entity;getTeamColor()I", source)
        self.assertIn('DollEffectProtection.isWearing(living, "suifengyao")', source)
        self.assertIn("@Local(argsOnly = true) DeltaTracker tracker", source)
        self.assertIn("return original.call(entity)", source)
        source = self.source("LightTextureEffectsMixin")
        self.assertIn('@ModifyReturnValue(method = "getDarknessGamma", at = @At("RETURN"))', source)
        source = (JAVA / "common/doll/DollEffectProtection.java").read_text()
        self.assertNotIn("net.minecraft.client", source)
        self.assertNotIn("top.theillusivec4", source)


if __name__ == "__main__":
    unittest.main()
