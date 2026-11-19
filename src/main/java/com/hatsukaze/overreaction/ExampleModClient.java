package com.hatsukaze.overreaction;

import com.hatsukaze.overreaction.manager.ClientHitStopManager;
import com.hatsukaze.overreaction.manager.FirstPersonAttackController;
import com.hatsukaze.overreaction.network.RequestAttackPacket;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ExampleMod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ExampleModClient {
    public ExampleModClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    // 1回のクリックで攻撃を送るのは1回だけ（ブロックを殴ると発火が重なるため）
    private static boolean attackSentThisPress = false;

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ExampleMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        ExampleMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

        // PALのアニメーションレイヤー登録、プレイヤーが生成されるたびにレイヤーを自動でセット ファクトリ関数、プレイヤー用のコントローラーを作成
        event.enqueueWork(() -> {
            // 3人称
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    ATTACK_LAYER_ID,
                    1500,
                    player -> {
                        PlayerAnimationController controller = new PlayerAnimationController(player,
                                (c, state, animSetter) -> PlayState.STOP
                        );
                        applyFirstPersonSettings(controller);
                        return controller;
                    }
            );
            //一人称
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    FIRST_PERSON_LAYER_ID,
                    1600,
                    player -> {
                        FirstPersonAttackController controller = new FirstPersonAttackController(player,
                                (c, state, animSetter) -> PlayState.STOP
                        );
                        applyFirstPersonSettings(controller);
                        return controller;
                    }
            );
        });
    }

    /**
     * 一人称での描画設定。レイヤー共通
     */
    private static void applyFirstPersonSettings(PlayerAnimationController controller) {
        controller.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
        controller.setFirstPersonConfiguration(
                new FirstPersonConfiguration(
                        true,  // showRightArm
                        true,  // showLeftArm
                        true,  // showRightItem
                        true,  // showLeftItem
                        false  // showArmor
                )
        );
    }

    //左クリック受け取り、フィルタリングして通す
    @SubscribeEvent
    public static void onLeftClick(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (!event.isAttack()) return;
        if (mc.screen != null) return;
        if (mc.player == null) return;

        ItemStack weapon = mc.player.getMainHandItem();
        if (!(weapon.getItem() instanceof SwordItem)) return;

        // クリックしたか確認
        if (attackSentThisPress) return;
        attackSentThisPress = true;

        System.out.println("パケット送信"); // ← 追加
        PacketDistributor.sendToServer(new RequestAttackPacket());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientHitStopManager.tick();

        // ボタン話したときにfalse
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.keyAttack.isDown()) {
            attackSentThisPress = false;
        }
    }


    //レイヤーを登録
    public static final ResourceLocation ATTACK_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath("overreaction", "attack_layer");

    // 一人称専用レイヤー
    public static final ResourceLocation FIRST_PERSON_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath("overreaction", "first_person_layer");

}



