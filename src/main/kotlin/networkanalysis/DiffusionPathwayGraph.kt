package networkanalysis

import com.mxgraph.layout.mxCircleLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.swing.mxGraphComponent
import org.jgrapht.Graph
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph
import javax.swing.JFrame

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

    fun influencingActions(action: Action): Set<Action> =
        graph.incomingEdgesOf(action)
            .map { graph.getEdgeSource(it) }
            .toSet()

    fun numInfluencingActions(action: Action): Int =
        graph.inDegreeOf(action)

    fun hasInfluencingActions(action: Action): Boolean =
        numInfluencingActions(action) > 0

    fun isOrphan(action: Action): Boolean =
        !hasInfluencingActions(action)

    fun display() {
        val frame = JFrame("Diffusion Pathway Graph")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val graphAdapter: JGraphXAdapter<Action, DefaultEdge> = JGraphXAdapter(graph)
        val layout: mxIGraphLayout = mxCircleLayout(graphAdapter)
        layout.execute(graphAdapter.getDefaultParent())
        frame.add(mxGraphComponent(graphAdapter))
        frame.pack()
        frame.isLocationByPlatform = true
        frame.isVisible = true
    }

    companion object Factory {
        fun of(actions: List<InfluencedAction>): DiffusionPathwayGraph {
            return DiffusionPathwayGraph().apply { actions.forEach { add(it) } }
        }
    }
}