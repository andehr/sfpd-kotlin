import collections.Counter
import kotlin.math.ln

/**
 * Created by Andrew D. Robertson on 26/12/2020.
 */
interface FeatureScorer {
    fun score(feature: String): Double
    val vocab: Set<String>
}

abstract class AbstractFeatureScorer(val target: Counter, val background: Counter) : FeatureScorer {

    fun targetProbability(feature: String) =
        target.probability(feature)

    fun backgroundProbability(feature: String) =
        background.probability(feature)

    override val vocab: Set<String>
        get() = target.vocab
}

class SFPDFeatureScorer(target: Counter,
                        background: Counter,
                        val likelihoodLiftRatio: Double = 0.4
                        ) : AbstractFeatureScorer(target, background) {

    override fun score(feature: String): Double {

        val weightedLikelihood = likelihoodLiftRatio * ln(targetProbability(feature))
        val weightedPMI = (1 - likelihoodLiftRatio) * (ln(targetProbability(feature)) - ln(backgroundProbability(feature)) )

        return weightedLikelihood + weightedPMI
    }
}

fun nTopFeatures(n: Int, scorer: FeatureScorer): List<String> =
    scorer.vocab.greatest(n) { scorer.score(it!!) }