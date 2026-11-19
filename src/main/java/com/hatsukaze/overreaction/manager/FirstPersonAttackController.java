package com.hatsukaze.overreaction.manager;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.animation.AnimationController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 *
 * 一人称専用の攻撃アニメレイヤー
 * 再生は視点に関係なく続けるが、描画されるのは自分の画面で一人称のときだけ
 * <p>
 * 注意：PALの中では isActive() が「描画していいか」の判定に使われる
 * oReで「再生中か」を調べるときは必ず isRunning() を使うこと  isActive() not used.
 */
@OnlyIn(Dist.CLIENT)
public class FirstPersonAttackController extends PlayerAnimationController {

    public FirstPersonAttackController(AbstractClientPlayer player, AnimationController.AnimationStateHandler handler) {
        super(player, handler);
    }

    /**
     * 描画用　再生中かつ自分の画面かつ一人称
     */
    @Override
    public boolean isActive() {
        return super.isActive() && isLocalFirstPerson();
    }

    /**
     * oRe used - 再生中か判断する、再生中はTrue　視点関係なし
     */
    public boolean isRunning() {
        return super.isActive();
    }

    private boolean isLocalFirstPerson() {
        return getPlayer() instanceof LocalPlayer
                && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }


}
