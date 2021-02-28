package networkanalysis

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph

class DiffusionPathwayGraph {

    val graph: Graph<Action, DefaultEdge> = SimpleDirectedGraph(DefaultEdge::class.java)

    val actions: Set<Action>
        get() = graph.vertexSet()

    fun add(action: Action) =
        graph.addVertex(action)

    fun add(action: InfluencedAction) =
        with(graph){
            addVertex(action.influencer)
            addVertex(action.influencee)
            addEdge(action.influencer, action.influencee)
        }

    fun numInfluencingActions(action: Action): Int =
        graph.inDegreeOf(action)

    fun parentActions(action: Action): Set<Action> =
        graph.incomingEdgesOf(action)
            .map { graph.getEdgeSource(it) }
            .toSet()

    fun numParentActions(action: Action): Int =
        graph.inDegreeOf(action)

    fun hasParentActions(action: Action): Boolean =
        numParentActions(action) > 0

    fun isOrphan(action: Action): Boolean =
        !hasParentActions(action)

    companion object Factory {
        fun of(actions: List<InfluencedAction>): DiffusionPathwayGraph {
            return DiffusionPathwayGraph().apply { actions.forEach { add(it) } }
        }
    }
}