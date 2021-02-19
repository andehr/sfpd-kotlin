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

    private fun influence(ancestor: Action, node: Action, graph: DiffusionPathwayGraph): Double {
        if (graph.numParentActions(node) == 0) {
            return if (ancestor == node) 1.0 else 0.0
        } else {
            return if (ancestor == node) {
                1.0 - transferRate
            } else {
                (transferRate / graph.numParentActions(node)) *
                        graph.parentActions(node).map { influence(ancestor, it, graph) }.sum()
            }
        }
    }

    private fun buildTable(graph: DiffusionPathwayGraph) {
        for (a1 in graph.actions) {

            if (graph.hasParentActions(a1)) {
                updateUser(a1.userId, true)
                for (parent in graph.parentActions(a1)){
                    updateUser(parent.userId, false)
                }
            }

            for (a2 in graph.actions){
                with(influence(a1, a2, graph)){
                    if (this > 0){
                        addInfluence(this, a1.userId, a2.userId)
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

    fun users(): Set<String> =
        users.keys

    fun numUsers(): Int =
        users.size

    fun getInfluence(influencee: String, influencer: String): Double =
        influenceTable.get(influencee)?.get(influencer) ?: 0.0 + smoothing

    fun addInfluence(inc: Double, influencee: String, influencer: String) {
        influenceTable.getOrPut(influencee) { mutableMapOf() }
            .compute(influencer) { user, influence ->
                if (influence == null) inc else inc + influence }
    }

}