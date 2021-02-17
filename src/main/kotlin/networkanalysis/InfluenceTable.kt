package networkanalysis

/**
 * Created by Andrew D. Robertson on 17/02/2021.
 */
class InfluenceTable(
    graph: DiffusionPathwayGraph,
    val smoothing: Double = 0.00001,
    val transferRate: Double = 0.8){

    val Avu: MutableMap<String, MutableMap<String, Double>> = mutableMapOf()
    val users: MutableMap<String, Boolean> = mutableMapOf()

    init {
        buildTable(graph)
    }

    fun influence(ancestor: Action, node: Action, graph: DiffusionPathwayGraph): Double {
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

    fun buildTable(graph: DiffusionPathwayGraph) {
        for (a1 in graph.actions) {
            for (a2 in graph.actions){
                with(influence(a1, a2, graph)){
                    if (this > 0){
                        addInfluence(this, a1.userId, a2.userId)
                    }
                }
            }
        }
    }

    fun updateUser(id: String, beingInfluenced: Boolean) {
        users.merge(id, !beingInfluenced) { old, new -> old && new }
    }

    fun getInfluence(influencee: String, influencer: String): Double =
        Avu.get(influencee)?.get(influencer) ?: 0.0 + smoothing

    fun addInfluence(inc: Double, influencee: String, influencer: String) {
        Avu.getOrPut(influencee) { mutableMapOf() }
            .compute(influencer) { user, influence ->
                if (influence == null) inc else inc + influence }
    }

}