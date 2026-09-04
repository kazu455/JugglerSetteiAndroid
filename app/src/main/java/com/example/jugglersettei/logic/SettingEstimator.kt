package com.example.jugglersettei.logic

import com.example.jugglersettei.data.JugglerMachine
import kotlin.math.exp
import kotlin.math.ln

/**
 * 入力された実戦データ(総回転数・BIG回数・REG回数・ぶどう回数)から、
 * 各設定(1〜6)の事後確率をベイズ推定で算出する。
 *
 * 各役の出現回数は「総回転数×出現確率」を期待値とするポアソン分布に
 * 従うと仮定し、対数尤度を計算して合成する(独立性を仮定した近似)。
 * 事前分布は一様(全設定が等確率)としている。
 */
data class EstimationInput(
    val totalSpins: Int,
    val bigCount: Int,
    val regCount: Int,
    val grapeCount: Int?, // 入力しない場合は null (ぶどうはカウント困難なため任意)
)

data class EstimationResult(
    val setting: Int,
    val posteriorProbability: Double // 0.0〜1.0
)

object SettingEstimator {

    /** log( P(k ; lambda) ) をポアソン分布で計算。lambda = spins * prob */
    private fun poissonLogLikelihood(k: Int, lambda: Double): Double {
        if (lambda <= 0.0) return Double.NEGATIVE_INFINITY
        // log(lambda^k * e^-lambda / k!) = k*ln(lambda) - lambda - ln(k!)
        var logFactorial = 0.0
        for (i in 2..k) {
            logFactorial += ln(i.toDouble())
        }
        return k * ln(lambda) - lambda - logFactorial
    }

    fun estimate(machine: JugglerMachine, input: EstimationInput): List<EstimationResult> {
        val logLikelihoods = machine.settings.map { setting ->
            val bigLambda = input.totalSpins * setting.bigProb
            val regLambda = input.totalSpins * setting.regProb
            var logL = poissonLogLikelihood(input.bigCount, bigLambda) +
                poissonLogLikelihood(input.regCount, regLambda)

            val grapeProb = setting.grapeProb
            if (input.grapeCount != null && grapeProb != null) {
                val grapeLambda = input.totalSpins * grapeProb
                logL += poissonLogLikelihood(input.grapeCount, grapeLambda)
            }
            setting.setting to logL
        }

        // オーバーフロー防止のため最大値を引いてから exp する
        val maxLogL = logLikelihoods.maxOf { it.second }
        val rawLikelihoods = logLikelihoods.map { (s, logL) -> s to exp(logL - maxLogL) }
        val sum = rawLikelihoods.sumOf { it.second }

        return rawLikelihoods.map { (s, l) ->
            EstimationResult(setting = s, posteriorProbability = if (sum > 0) l / sum else 0.0)
        }.sortedBy { it.setting }
    }
}
