package networkanalysis

import divAssign
import lnSafe
import mapInPlace
import mapIndexedInPlace
import randomSumTo1
import uniformSumTo1
import kotlin.math.exp
import kotlin.math.ln

@ExperimentalStdlibApi
class AudienceInfluenceAnalysis(val table: InfluenceTable, val clusters: Int, val smoothing: Double = 0.00001) {

    var belongingness: Map<String, DoubleArray> = initBelongingness()
    var influence: Map<String, DoubleArray> = initInfluence()
    var priors: DoubleArray = initPriors()

    private fun initPriors(): DoubleArray =
        randomSumTo1(clusters, smoothing)

    private fun initInfluence(): Map<String, DoubleArray> {
        val influence: Map<String, DoubleArray> = users().associateWith { DoubleArray(clusters) }
        for (cluster in 0..clusters){
            val randomised = randomSumTo1(table.numUsers(), smoothing)
            influence.values.forEachIndexed{ index, i -> i[cluster] = randomised[index] }
        }
        return influence
    }

    private fun initBelongingness(): Map<String, DoubleArray> =
        users().associateWith { DoubleArray(clusters) }

    private fun computePriors(): DoubleArray{
        val newPriors = DoubleArray(clusters)

        // The prior for a cluster is the sum of user belongingness for that cluster, normalised further below
        belongingness.values.forEachIndexed { cluster, probabilities -> newPriors[cluster] += probabilities[cluster]}

        newPriors /= newPriors.sum()

        return newPriors
    }

    private fun computeInfluence(): Map<String, DoubleArray> {
        val normalisationFactors = DoubleArray(clusters);
        val newInfluence = buildMap<String, DoubleArray> {
            // For each user and cluster, sum the belongingness of every user for that cluster, weighted by the number of actions that the current user influences on each
            for (user: String in users()) {
                val userInfluence = DoubleArray(clusters)

                for (cluster in 0..clusters) {
                    userInfluence[cluster] = users().map { table.influence(it, user) * (belongingness[user]!![cluster]) }.sum()
                    normalisationFactors[cluster] += userInfluence[cluster]
                }

                put(user, userInfluence)
            }
        }
        // Normalise across all users, such that for any given cluster, all of the influences across all users sum to 1.
        newInfluence.values.forEach { it /= normalisationFactors }
        return newInfluence
    }

    private fun computeBelongingness(): Map<String, DoubleArray> =
        buildMap {
            for (user in users()){
                val userBelongingness = DoubleArray(clusters)
                // If user isn't influenced by any action, then given them uniform belongingness.
                if (table.isSourceOnly(user)){
                    userBelongingness.uniformSumTo1()
                } else {
                    userBelongingness.mapIndexedInPlace { i, _ ->  lnSafe(priors[i]) + totalClusterInfluence(user, i) }
                    val maxBelongingness = userBelongingness.maxOrNull()!!
                    // Rescale the log values for safe exponentiation, whilst summing up the normalisation factor
                    userBelongingness.mapInPlace { exp(it - maxBelongingness) }
                    val normalisationFactor = userBelongingness.maxOrNull()!!
                    // Divide through by the sum to make probabilities
                    userBelongingness /= normalisationFactor
                }
                put(user, userBelongingness)
            }
        }

    private fun totalClusterInfluence(influencee: String, cluster: Int): Double =
        influence.map{ table.influence(influencee, it.key) * ln(it.value[cluster]) }.sum()

    private fun emUntilConvergence(limit: Int = 1000, stoppingCriteria: Double = 0.01){
        var steps = 0
        do {
            val diff = emStep()
            steps++
        } while (steps < limit && diff > stoppingCriteria)
    }

    /**
     * Compute a single expectation-maximisation step.
     *
     * E-step:
     *   Compute belongingness from current estimation of influence and cluster priors.
     * M-step:
     *   Compute the next influence and cluster priors from previous timestep belongingness
     *
     * Compute the minorization function g for current parameters and next.
     *
     * Return:  g(ϕt+1, θt+1; ϕt, θt) - g(ϕt, θt; ϕt, θt)
     */
    private fun emStep(): Double {
        belongingness = computeBelongingness() // η(t)
        val newPriors = computePriors()        // ϕ(t+1)
        val newInfluence = computeInfluence()  // θ(t+1)
        val diff = computeExpectedLogLikelihood(newInfluence, newPriors) // g(ϕt+1, θt+1; ϕt, θt) - g(ϕt, θt; ϕt, θt)
        priors = newPriors
        influence = newInfluence
        return diff
    }

    private fun computeExpectedLogLikelihood(newInfluence: Map<String, DoubleArray>, newPriors: DoubleArray): Double {
        val logPriors = priors.map { ln(it) }
        val logNewPriors = newPriors.map { ln(it) }

        var current = 0.0
        var next = 0.0

        // for v in V
        for ((v, vBelongingness) in belongingness) {
            // for c in C
            for (cluster in 0..clusters){
                // ηvc * log(ϕc)
                current += vBelongingness[cluster] * logPriors[cluster]
                next += vBelongingness[cluster] * logNewPriors[cluster]
                // for u in V
                for ((u, uBelongingness) in belongingness){
                    // Avu * ηuc * log(θvc)
                    current += table.influence(u, v) * uBelongingness[cluster] * ln(influence[v]!![cluster])
                    next += table.influence(u, v) * uBelongingness[cluster] * ln(newInfluence[v]!![cluster])
                }
            }
        }

        require(next >= current) { "Likelihood did not improve or converge, but got worse, this should be impossible (incorrect algorithm)."}
        return next - current
    }

    fun users(): Set<String> =
        table.users()
}