package com.hatsukaze.overreaction.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// クライアントの情報は必ずentityIDごとに所持する
@OnlyIn(Dist.CLIENT)
public class ClientHitStopManager {
    /**
     * entityId → 残り時間（秒）
     */
    private static final Map<Integer, Float> REMAINING = new HashMap<>();

    private ClientHitStopManager() {
    }

    public static void start(AbstractClientPlayer player, float duration) {
        ClientPoseController.pause(player);
        REMAINING.put(player.getId(), duration);
    }

    /**
     * クライアントごとにtick呼ぶ
     *
     */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            REMAINING.clear();
            return;
        }

        Iterator<Map.Entry<Integer, Float>> it = REMAINING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Float> entry = it.next();
            float left = entry.getValue() - 0.05f;

            if (left > 0f) {
                entry.setValue(left);
                continue;
            }

            // 時間切れ発生時特定エンティティの一時停止を解除してアニメーションを破棄
            Entity entity = mc.level.getEntity(entry.getKey());
            if (entity instanceof AbstractClientPlayer player) {
                ClientPoseController.unpause(player);
            }
            it.remove();
        }
    }

}