package networkanalysis

/**
 * Created by Andrew D. Robertson on 17/02/2021.
 */
class InfluenceTable(graph: DiffusionPathwayGraph, val smoothing: Double = 0.00001, val transferRate: Double = 0.8){

    private val influenceTable: MutableMap<String, MutableMap<String, Double>> = mutableMapOf()
    private val users: MutableMap<String, Boolean> = mutableMapOf()

    init {
        buildTable(graph)
    }

    fun users(): Set<String> =
        users.keys

    fun numUsers(): Int =
        users.size

    private fun influence(node: Action, ancestor: Action, graph: DiffusionPathwayGraph): Double {
        if (graph.numInfluencingActions(node) == 0) {
            return if (ancestor == node) 1.0 else 0.0
        } else {
            return if (ancestor == node) {
                1.0 - transferRate
            } else {
               (transferRate / graph.numInfluencingActions(node)) *
                        graph.influencingActions(node).map{ influence(it, ancestor, graph) }.sum()
            }
        }
    }

    private fun buildTable(graph: DiffusionPathwayGraph) {
        for (influencee in graph.actions) {

            if (graph.hasInfluencingActions(influencee)) {
                updateUser(influencee.userId, true)
                for (parent in graph.influencingActions(influencee)){
                    updateUser(parent.userId, false)
                }
            }

            for (influencer in graph.actions){
                influence(influencee, influencer, graph).let {
                    if (it > 0){
                        addInfluence(it, influencee.userId, influencer.userId)
                    }
                }
            }
        }
    }

    fun isSourceOnly(userId: String): Boolean =
        users[userId] == true

    fun updateUser(id: String, beingInfluenced: Boolean) {
        users.merge(id, !beingInfluenced) { old, new -> old && new }
    }

    fun influence(influencee: String, influencer: String): Double =
        (influenceTable.get(influencee)?.get(influencer) ?: 0.0) + smoothing

    fun addInfluence(inc: Double, influencee: String, influencer: String) {
        influenceTable.getOrPut(influencee) { mutableMapOf() }
            .compute(influencer) { user, influence ->
                if (influence == null) inc else inc + influence }
    }

    fun prettyPrint(){
        for ((influencee, influencers) in influenceTable) {
            println("$influencee <-")
            for ((influencer, influence) in influencers) {
                println("  $influencer = $influence")
            }
        }
    }

}