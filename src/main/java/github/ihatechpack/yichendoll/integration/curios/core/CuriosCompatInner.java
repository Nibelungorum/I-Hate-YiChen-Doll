package github.ihatechpack.yichendoll.integration.curios.core;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.Res;
import github.ihatechpack.yichendoll.api.ModEvent;
import github.ihatechpack.yichendoll.common.MXmDollManager;
import github.ihatechpack.yichendoll.common.item.ItemDoll;
import github.ihatechpack.yichendoll.integration.curios.client.DollItemRenderer;
import github.ihatechpack.yichendoll.registry.ModItems;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.Objects;

public class CuriosCompatInner {
    @OnlyIn(Dist.CLIENT)
    @ModEvent(side = ModEvent.Side.ClientSide)
    static void registerRenderer(EntityRenderersEvent.AddLayers event) {
        if (event.getSkin(PlayerSkin.Model.WIDE) instanceof PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new DollItemRenderer<>(playerRenderer, event.getContext().getItemInHandRenderer()));
        }
        if (event.getSkin(PlayerSkin.Model.SLIM) instanceof PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new DollItemRenderer<>(playerRenderer, event.getContext().getItemInHandRenderer()));
        }
    }

    @ModEvent(side = ModEvent.Side.ServerSide)
    static void registerDollItemPredicate() {
        CuriosApi.registerCurioPredicate(Res.rl("item_doll"),
                slotResult -> slotResult.stack().getItem() instanceof ItemDoll);
    }

    static void registerDollItemCurioTick(RegisterCapabilitiesEvent event) {
        // 注册capability
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public void onEquip(SlotContext slotContext, ItemStack prevStack) {
                ICurio.super.onEquip(slotContext, prevStack);
                if (slotContext.entity() instanceof Player player) {
                    // 飞行能力 这里到时候和I Hate API接入一下
                    var fly = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
                    if (fly != null) {
                        AttributeModifier modifier;
                        if ((modifier = fly.getModifier(Res.rl("flight"))) != null) {
                            fly.addOrUpdateTransientModifier(new AttributeModifier(Res.rl("flight"), modifier.amount() + 1.0f, AttributeModifier.Operation.ADD_VALUE)); // >0的值意味可以飞行
                        } else {
                            fly.addOrUpdateTransientModifier(new AttributeModifier(Res.rl("flight"), 1.0f, AttributeModifier.Operation.ADD_VALUE)); // >0的值意味可以飞行
                        }
                        IHateYiChenDoll.LOGGER.info(player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT).getModifiers().toString());
                        // >0的值意味可以飞行
                        // 有这个modifier说明已经开启Doll的飞行了 就不需要额外加AttributeModifier
                    }
                }
            }

            @Override
            public void curioTick(SlotContext slotContext) {
                if (slotContext.entity() instanceof Player player) {
                    // 夜视能力
                    keepEffect(player, MobEffects.NIGHT_VISION);
                    // 抗火能力
                    keepEffect(player, MobEffects.FIRE_RESISTANCE);
                }
            }

            @Override
            public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                ICurio.super.onUnequip(slotContext, newStack);
                if (slotContext.entity() instanceof Player player) {
                    var fly = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
                    if (fly != null) {
                        AttributeModifier modifier;
                        // 这个null判断多余了其实
                        if ((modifier = fly.getModifier(Res.rl("flight"))) != null) {
                            fly.addOrUpdateTransientModifier(new AttributeModifier(Res.rl("flight"), modifier.amount() - 1.0f, AttributeModifier.Operation.ADD_VALUE)); // >0的值意味可以飞行
                            if (Objects.requireNonNull(fly.getModifier(Res.rl("flight"))).amount() == 0.0f) fly.removeModifier(Res.rl("flight"));
                        }
                    }
                    IHateYiChenDoll.LOGGER.info(player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT).getModifiers().toString());
                }
            }
        }, ModItems.ITEMS.get("howxu").get()); // howxu玩偶
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public void onEquip(SlotContext slotContext, ItemStack prevStack) {
                ICurio.super.onEquip(slotContext, prevStack);
                if (!slotContext.cosmetic() && slotContext.entity() instanceof Player player) {
                    refreshMXmDoll(player, slotContext, stack);
                }
            }

            @Override
            public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                ICurio.super.onUnequip(slotContext, newStack);
                if (!slotContext.cosmetic() && slotContext.entity() instanceof Player player) {
                    refreshMXmDoll(player, slotContext, newStack);
                }
            }

            @Override
            public void curioTick(SlotContext slotContext) {
                if (!slotContext.cosmetic() && slotContext.entity() instanceof Player player
                        && MXmDollManager.needsRefresh(player)) {
                    refreshMXmDoll(player, null, ItemStack.EMPTY);
                }
            }
        }, ModItems.ITEMS.get("m_xm_").get());
    }

    static void refreshMXmDoll(Player player, SlotContext changedSlot, ItemStack replacement) {
        if (!(player instanceof ServerPlayer)) return;
        boolean wearing = CuriosApi.getCuriosInventory(player).map(inventory -> {
            for (var entry : inventory.getCurios().entrySet()) {
                var stacks = entry.getValue().getStacks();
                for (int index = 0; index < stacks.getSlots(); index++) {
                    if (!inventory.isSlotActive(entry.getKey(), index)) continue;
                    // Curios can invoke the callback before the changed stack is visible.
                    ItemStack equipped = changedSlot != null && changedSlot.identifier().equals(entry.getKey())
                            && changedSlot.index() == index ? replacement : stacks.getStackInSlot(index);
                    if (equipped.is(ModItems.ITEMS.get("m_xm_").get())) return true;
                }
            }
            return false;
        }).orElse(false);
        MXmDollManager.update(player, wearing);
    }

    private static void keepEffect(Player player, Holder<MobEffect> effectHolder) {
        if (!player.hasEffect(effectHolder.getDelegate()))
            player.addEffect(new MobEffectInstance(effectHolder, 12000, 255, true, false));
        if (Objects.requireNonNull(player.getEffect(effectHolder.getDelegate())).getDuration() < 400)
            player.addEffect(new MobEffectInstance(effectHolder, 12000, 255, true, false));
    }
}
