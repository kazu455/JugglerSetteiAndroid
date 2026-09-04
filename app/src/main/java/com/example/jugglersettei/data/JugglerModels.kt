package com.example.jugglersettei.data

/**
 * 設定1台あたりの各役の確率(分母)を表す。
 * 例: bigDenom = 273.1 なら BIG確率は 1/273.1
 *
 * BIG/REG確率は「スロベース」等の解析サイトに掲載されている実機データ、
 * ぶどう確率はメーカー非公表の機種が多いため「ガリぞう」氏調べの推定値や
 * 各サイトのアプリ実戦値(300万G規模のシミュレート)を参照しています(2026年8月時点)。
 * 設定差が判明していない機種では grapeDenom を null にしています(判別に使用しません)。
 * ホールの個体差・調査ソースにより実機とは差異が出ることがあるため、
 * 判別の「目安」として利用してください。厳密な保証はしません。
 */
data class SettingData(
    val setting: Int,
    val bigDenom: Double,
    val regDenom: Double,
    val grapeDenom: Double? = null // 設定差データが無い機種は null
) {
    val bigProb: Double get() = 1.0 / bigDenom
    val regProb: Double get() = 1.0 / regDenom
    val grapeProb: Double? get() = grapeDenom?.let { 1.0 / it }
    val totalProb: Double get() = bigProb + regProb
}

data class JugglerMachine(
    val id: String,
    val displayName: String,
    val settings: List<SettingData>
) {
    /** この機種でぶどう確率による判別が可能かどうか */
    val hasGrapeData: Boolean get() = settings.all { it.grapeDenom != null }
}

object JugglerMachineRepository {

    val machines: List<JugglerMachine> = listOf(
        // マイジャグラーV: 設定6はBIG=REG=1/229.1が特徴
        JugglerMachine(
            id = "my_juggler_v",
            displayName = "マイジャグラーV",
            settings = listOf(
                SettingData(1, 273.1, 409.6, 5.90),
                SettingData(2, 270.8, 385.5, 5.88),
                SettingData(3, 266.4, 336.1, 5.82),
                SettingData(4, 254.0, 290.0, 5.81),
                SettingData(5, 240.1, 268.6, 5.79),
                SettingData(6, 229.1, 229.1, 5.69)
            )
        ),
        // ネオアイムジャグラーEX: 旧アイムジャグラーEXの後継機(2025年9月導入)。
        // ぶどうは設定1〜5共通1/6.02で、設定6のみ1/5.78に優遇される(「設定6か否か」の判別材料)
        JugglerMachine(
            id = "neo_aim_juggler_ex",
            displayName = "ネオアイムジャグラーEX",
            settings = listOf(
                SettingData(1, 273.1, 439.8, 6.02),
                SettingData(2, 269.7, 399.6, 6.02),
                SettingData(3, 269.7, 331.0, 6.02),
                SettingData(4, 259.0, 315.1, 6.02),
                SettingData(5, 259.0, 255.0, 6.02),
                SettingData(6, 255.0, 255.0, 5.78)
            )
        ),
        // ファンキージャグラー2: BIG側にも設定差があるBIG偏重型
        JugglerMachine(
            id = "funky_juggler_2",
            displayName = "ファンキージャグラー2",
            settings = listOf(
                SettingData(1, 266.4, 439.8, 5.95),
                SettingData(2, 259.0, 407.1, 5.92),
                SettingData(3, 256.0, 366.1, 5.90),
                SettingData(4, 249.2, 322.8, 5.85),
                SettingData(5, 240.1, 299.3, 5.78),
                SettingData(6, 219.9, 262.1, 5.72)
            )
        ),
        // ハッピージャグラーV3: ぶどう確率は「ガリぞう」氏調べのアプリ実戦値(各設定300万G)
        JugglerMachine(
            id = "happy_juggler_v3",
            displayName = "ハッピージャグラーV3",
            settings = listOf(
                SettingData(1, 273.1, 397.2, 6.07),
                SettingData(2, 270.8, 362.1, 6.03),
                SettingData(3, 263.2, 332.7, 6.00),
                SettingData(4, 254.0, 300.6, 5.86),
                SettingData(5, 239.2, 273.1, 5.84),
                SettingData(6, 226.0, 256.0, 5.80)
            )
        ),
        // ゴーゴージャグラー3: 設定6はBIG=REG=1/234.9。ぶどう確率は「ガリぞう」氏調べの推定値
        JugglerMachine(
            id = "gogo_juggler_3",
            displayName = "ゴーゴージャグラー3",
            settings = listOf(
                SettingData(1, 259.0, 354.2, 6.25),
                SettingData(2, 258.0, 332.7, 6.20),
                SettingData(3, 257.0, 306.2, 6.15),
                SettingData(4, 254.0, 268.6, 6.07),
                SettingData(5, 247.3, 247.3, 6.00),
                SettingData(6, 234.9, 234.9, 5.92)
            )
        ),
        // ミスタージャグラー: BIG/REGは「ガリぞう」氏調べ準拠(設定6はBIG=REG=1/237.4)。
        // ぶどう確率も同氏調べの推定値
        JugglerMachine(
            id = "mr_juggler",
            displayName = "ミスタージャグラー",
            settings = listOf(
                SettingData(1, 268.6, 374.5, 6.29),
                SettingData(2, 267.5, 354.2, 6.22),
                SettingData(3, 260.1, 331.0, 6.15),
                SettingData(4, 249.2, 291.3, 6.09),
                SettingData(5, 240.9, 257.0, 6.02),
                SettingData(6, 237.4, 237.4, 5.96)
            )
        ),
        // ジャグラーガールズSS(2024年5月導入。無印「ジャグラーガールズ」の現行後継機)。
        // ぶどう確率は「ガリぞう」氏調べの推定値(設定1〜4共通、設定5・6のみ優遇)
        JugglerMachine(
            id = "juggler_girls_ss",
            displayName = "ジャグラーガールズSS",
            settings = listOf(
                SettingData(1, 273.1, 381.0, 5.98),
                SettingData(2, 270.8, 350.5, 5.98),
                SettingData(3, 260.1, 316.6, 5.98),
                SettingData(4, 250.1, 281.3, 5.98),
                SettingData(5, 243.6, 270.8, 5.88),
                SettingData(6, 226.0, 252.1, 5.83)
            )
        )
    )
}
