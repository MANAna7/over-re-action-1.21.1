// AttackDefinition.java
package com.hatsukaze.overreaction.data;

import com.google.gson.JsonObject;

/**
 * 現在何のcomboを持ち、comboの中でも一段ずつの状態を維持するためのデータクラス。一段ごとに管理する。
 **/
public class AttackDefinition {
    public final String id;
    public final String animationId;
    public final float damageMultiplier;
    public final float hitWindowStart;
    public final float hitWindowEnd;
    public final boolean playOnHit;
    public final float attackingTimeBase;
    public final float animationNaturalTime;

    // nullable
    public final HitStopDefinition hitStop;
    public final RecoveryDefinition recovery;
    public final ChargeDefinition charge;

    // フェードのデフォルト（秒）
    public static final float DEFAULT_BLEND_IN_TIME = 0.2f;
    public static final float DEFAULT_RELEASE_TIME = 0.3f;
    public static final int DEFAULT_RELEASE_TICKS = secondsToTicks(DEFAULT_RELEASE_TIME);

    // 追加：フィールド（中身はtick）
    /**
     * 中身Tick inside Tick
     */
    public final int blendInTime;   // この攻撃に入るときのフェード
    /**
     * 中身Tick inside Tick
     */
    public final int releaseTime;   // この攻撃でコンボが終わったときのフェード

    // 一人称系
    public final String firstPersonAnimationId;
    public static final String NO_FP_ANIMATION = "";


    private AttackDefinition(String id, String animationId, float damageMultiplier,
                             float hitWindowStart, float hitWindowEnd, boolean playOnHit, float attackingTimeBase, float animationNaturalTime,
                             HitStopDefinition hitStop, RecoveryDefinition recovery,
                             ChargeDefinition charge, int blendInTime, int releaseTime, String firstPersonAnimationId) {
        this.id = id;
        this.animationId = animationId;
        this.damageMultiplier = damageMultiplier;
        this.hitWindowStart = hitWindowStart;
        this.hitWindowEnd = hitWindowEnd;
        this.playOnHit = playOnHit;
        this.attackingTimeBase = attackingTimeBase;
        this.animationNaturalTime = animationNaturalTime;
        this.hitStop = hitStop;
        this.recovery = recovery;
        this.charge = charge;
        this.blendInTime = blendInTime;
        this.releaseTime = releaseTime;
        this.firstPersonAnimationId = firstPersonAnimationId;
    }

    /**
     * jsonからデータを取得する
     * getting data in json
     */
    public static AttackDefinition fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String animationId = json.get("animationId").getAsString();
        float dmgMult = json.get("damageMultiplier").getAsFloat();
        //もしもなかった場合に備えてデフォルト値1.0f
        float atkTimeBase = json.has("attackingTimeBase") ? json.get("attackingTimeBase").getAsFloat() : 1.0f;
        float animationNaturalTime = json.has("animationNaturalTime") ? json.get("animationNaturalTime").getAsFloat() : 1.0f;

        // hitWindowがnullの場合
        float hwStart = 0f;
        float hwEnd = 0f;
        if (json.has("hitWindow") && !json.get("hitWindow").isJsonNull()) {
            JsonObject hw = json.getAsJsonObject("hitWindow");
            hwStart = hw.get("start").getAsFloat();
            hwEnd = hw.get("end").getAsFloat();
        }
        boolean playOnHit = json.get("playOnHit").getAsBoolean();

        HitStopDefinition hitStop = (json.has("hitStop") && !json.get("hitStop").isJsonNull())
                ? HitStopDefinition.fromJson(json.getAsJsonObject("hitStop")) : null;
        RecoveryDefinition recovery = (json.has("recovery") && !json.get("recovery").isJsonNull())
                ? RecoveryDefinition.fromJson(json.getAsJsonObject("recovery")) : null;
        ChargeDefinition charge = (json.has("charge") && !json.get("charge").isJsonNull())
                ? ChargeDefinition.fromJson(json.getAsJsonObject("charge")) : null;

        int blendInTime = secondsToTicks(readFloatFromJson(json, "blendInTime", DEFAULT_BLEND_IN_TIME));
        int releaseTime = secondsToTicks(readFloatFromJson(json, "releaseTime", DEFAULT_RELEASE_TIME));

        String firstPersonAnimationId = readStringFromJson(json, "firstPersonAnimationId", NO_FP_ANIMATION);

        return new AttackDefinition(id, animationId, dmgMult, hwStart, hwEnd,
                playOnHit, atkTimeBase, animationNaturalTime, hitStop, recovery, charge, blendInTime, releaseTime, firstPersonAnimationId);
    }

    /**
     * jsonからfloatを取得する
     *
     * @param json         JsonObject
     * @param key          String
     * @param defaultValue float
     * @return -> return Read float
     * @return　キーがない　またはnull -> return default
     */
    public static float readFloatFromJson(JsonObject json, String key, float defaultValue) {
        return (json.has(key) && !json.get(key).isJsonNull()) ? json.get(key).getAsFloat() : defaultValue;
    }

    /**
     * jsonからStringを取得する
     *
     * @param json         JsonObject
     * @param key          String
     * @param defaultValue String NO_FP_ANIMATION
     * @return -> return Read String
     * @return　キーがない　またはnull -> return default
     */
    public static String readStringFromJson(JsonObject json, String key, String defaultValue) {
        return (json.has(key) && !json.get(key).isJsonNull()) ? json.get(key).getAsString() : defaultValue;
    }

    public static int secondsToTicks(float seconds) {
        return Math.round(seconds * 20f);
    }
}