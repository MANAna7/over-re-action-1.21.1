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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * クライアントサイドのポーズ再生を管理する
 */
@OnlyIn(Dist.CLIENT)
public final class ClientPoseController {

    private static final EasingType FADE_EASING = EasingType.EASE_IN_OUT_SINE;

    private ClientPoseController() {
    }

    /**
     * 最終フレームで保持するアニメを、前のポーズからfadeTicksかけて再生する
     */
    public static void play(AbstractClientPlayer player, ResourceLocation animationId, float speed, int fadeTicks) {
        PlayerAnimationController controller = getController(player);
        if (controller == null) return;
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

    private static final ResourceLocation RELEASE_ANIMATION_ID =
            ResourceLocation.fromNamespaceAndPath("overreaction", "release");

    /**
     * 保持中のポーズを手放し、fadeTicksかけて通常状態へ戻す
     */
    public static void release(AbstractClientPlayer player, int fadeTicks) {
        PlayerAnimationController controller = getController(player);
        if (controller == null) return;

        controller.unpause();

        // 何も再生していないなら、やることはない
        if (!controller.isActive()) return;

        if (fadeTicks > 0 && PlayerAnimResources.hasAnimation(RELEASE_ANIMATION_ID)) {
            // 空アニメにフェードで切り替える。再生中はレイヤーが生きているのでフェードが描画される
            RawAnimation empty = RawAnimation.begin()
                    .then(PlayerAnimResources.getAnimation(RELEASE_ANIMATION_ID), Animation.LoopType.PLAY_ONCE);
            controller.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(fadeTicks, FADE_EASING), empty);
        } else {
            // フェードなし、または空アニメが見つからないときは即解放
            controller.stopTriggeredAnimation();
        }
    }

    @Nullable
    private static PlayerAnimationController getController(AbstractClientPlayer player) {
        return (PlayerAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(
                player, ExampleModClient.ATTACK_LAYER_ID
        );
    }
}