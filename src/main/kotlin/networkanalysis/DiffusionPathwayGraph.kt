package networkanalysis

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph

class DiffusionPathwayGraph {

    val graph: Graph<Action, DefaultEdge> = SimpleDirectedGraph(DefaultEdge::class.java)

    fun addAction(action: Action) =
        graph.addVertex(action)

    fun addInfluencedAction(action: InfluencedAction) =
        with(graph){
            addVertex(action.influencer)
            addVertex(action.influencee)
            addEdge(action.influencer, action.influencee)
        }

    fun numInfluencingActions(action: Action): Int =
        graph.inDegreeOf(action)

}