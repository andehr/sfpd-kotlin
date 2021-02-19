package networkanalysis

import divAssign
import randomSumTo1
import uniformSumTo1
import kotlin.math.ln

@ExperimentalStdlibApi
class AudienceInfluenceAnalysis(val influenceTable: InfluenceTable, val clusters: Int, val smoothing: Double = 0.00001) {

    var belongingness: Map<String, DoubleArray> = initBelongingness()
    var influence: Map<String, DoubleArray> = initInfluence()
    var priors: DoubleArray = initPriors()

    private fun initPriors(): DoubleArray =
        randomSumTo1(clusters, smoothing)

    private fun initInfluence(): Map<String, DoubleArray> {
        val influence: Map<String, DoubleArray> = users().associateWith { DoubleArray(clusters) }
        for (cluster in 0..clusters){
            val randomised = randomSumTo1(influenceTable.numUsers(), smoothing)
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
                    userInfluence[cluster] = users().map { influenceTable.getInfluence(it, user) * (belongingness[user]!![cluster]) }.sum()
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
                if (influenceTable.isSourceOnly(user)){
                    userBelongingness.uniformSumTo1()
                } else {

                }
                put(user, userBelongingness)
            }
        }

    private fun totalClusterInfluence(influencee: String, cluster: Int): Double =
        influence.map { influenceTable.getInfluence(influencee, it.key) * ln(it.value[cluster]) }.sum()

    fun users(): Set<String> =
        influenceTable.users()
}