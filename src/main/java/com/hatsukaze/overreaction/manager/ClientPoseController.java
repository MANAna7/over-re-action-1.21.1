package com.hatsukaze.overreaction.manager;

import com.hatsukaze.overreaction.ExampleModClient;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * クライアントのポーズ再生を管理
 * PALのコントローラーを直接触る唯一のクラス
 * 外からはプレイヤー単位で頼み、何枚のレイヤーに配るかはここで決める
 */
@OnlyIn(Dist.CLIENT)
public final class ClientPoseController {

    private static final EasingType FADE_EASING = EasingType.EASE_IN_OUT_SINE;

    private static final ResourceLocation RELEASE_ANIMATION_ID =
            ResourceLocation.fromNamespaceAndPath("overreaction", "release");

    private ClientPoseController() {
    }

    // ===== 外から呼ぶ（2つに配る） =====

    /**
     * 攻撃モーションを再生する
     * 三人称=>攻撃レイヤー 一人称アニメ=>自分のプレイヤーの一人称レイヤー
     * 一人称motionがない場合は一人称レイヤーを同じフェード時間で解放する
     */
    public static void play(AbstractClientPlayer player, ResourceLocation tpAnimationId,
                            @Nullable ResourceLocation fpAnimationId, float speed, int fadeTicks) {
        PlayerAnimationController attack = getAttackLayer(player);
        if (attack != null) {
            playOn(attack, tpAnimationId, speed, fadeTicks);
        }

        // 一人称レイヤーは自分のプレイヤーだけ動かす
        if (!(player instanceof LocalPlayer)) return;
        FirstPersonAttackController fp = getFirstPersonLayer(player);
        if (fp == null) return;

        if (fpAnimationId != null && PlayerAnimResources.hasAnimation(fpAnimationId)) {
            playOn(fp, fpAnimationId, speed, fadeTicks);
        } else {
            releaseOn(fp, fadeTicks);
        }
    }

    /**
     * 保持中のポーズを2つともfadeTicksかけて手放す
     */
    public static void release(AbstractClientPlayer player, int fadeTicks) {
        PlayerAnimationController attack = getAttackLayer(player);
        if (attack != null) releaseOn(attack, fadeTicks);

        FirstPersonAttackController fp = getFirstPersonLayer(player);
        if (fp != null) releaseOn(fp, fadeTicks);
    }

    /**
     * ヒットストップ用：2つとも一時停止
     */
    public static void pause(AbstractClientPlayer player) {
        PlayerAnimationController attack = getAttackLayer(player);
        if (attack != null) attack.pause();

        FirstPersonAttackController fp = getFirstPersonLayer(player);
        if (fp != null) fp.pause();
    }

    /**
     * ヒットストップ用：2つとも再開
     */
    public static void unpause(AbstractClientPlayer player) {
        PlayerAnimationController attack = getAttackLayer(player);
        if (attack != null) attack.unpause();

        FirstPersonAttackController fp = getFirstPersonLayer(player);
        if (fp != null) fp.unpause();
    }

    // ===== 中で使う（1枚分） =====

    /**
     * 1枚のレイヤーで最終フレーム保持のアニメを前のポーズからフェードして再生
     */
    private static void playOn(PlayerAnimationController controller,
                               ResourceLocation animationId, float speed, int fadeTicks) {
        if (!PlayerAnimResources.hasAnimation(animationId)) return;

        RawAnimation held = RawAnimation.begin()
                .then(PlayerAnimResources.getAnimation(animationId), Animation.LoopType.HOLD_ON_LAST_FRAME);

        controller.unpause();
        controller.removeModifierIf(m -> m instanceof SpeedModifier);
        controller.addModifier(new SpeedModifier(speed), 0);

        if (fadeTicks > 0) {
            controller.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(fadeTicks, FADE_EASING), held);
        } else {
            controller.triggerAnimation(held);
        }
    }

    /**
     * 1枚のレイヤーで保持中のポーズを空アニメへのフェードで手放す
     */
    private static void releaseOn(PlayerAnimationController controller, int fadeTicks) {
        controller.unpause();

        // 何も再生していないなら、やることはない
        if (!isRunning(controller)) return;

        if (fadeTicks > 0 && PlayerAnimResources.hasAnimation(RELEASE_ANIMATION_ID)) {
            RawAnimation empty = RawAnimation.begin()
                    .then(PlayerAnimResources.getAnimation(RELEASE_ANIMATION_ID), Animation.LoopType.PLAY_ONCE);
            controller.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(fadeTicks, FADE_EASING), empty);
        } else {
            controller.stopTriggeredAnimation();
        }
    }

    /**
     * 再生中か（視点に関係なく）
     * 一人称レイヤーの isActive() は描画用で、三人称だと false になるため isRunning() を使う
     */
    private static boolean isRunning(PlayerAnimationController controller) {
        if (controller instanceof FirstPersonAttackController fp) {
            return fp.isRunning();
        }
        return controller.isActive();
    }

    @Nullable
    private static PlayerAnimationController getAttackLayer(AbstractClientPlayer player) {
        return PlayerAnimationAccess.getPlayerAnimationLayer(player, ExampleModClient.ATTACK_LAYER_ID)
                instanceof PlayerAnimationController c ? c : null;
    }

    @Nullable
    private static FirstPersonAttackController getFirstPersonLayer(AbstractClientPlayer player) {
        return PlayerAnimationAccess.getPlayerAnimationLayer(player, ExampleModClient.FIRST_PERSON_LAYER_ID)
                instanceof FirstPersonAttackController c ? c : null;
    }
}