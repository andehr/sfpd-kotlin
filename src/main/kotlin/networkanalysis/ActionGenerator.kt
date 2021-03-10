package networkanalysis

import com.google.common.collect.Iterators
import randomElement
import randomExcluding
import randomPair


/**
 * Created by Andrew D. Robertson on 23/02/2021.
 */
@ExperimentalStdlibApi
class ActionGenerator(
    val influencers: Int,
    val influence: Int,
    val influencerFollowers: Int,
    val backInfluence: Int = 0,
    val intraConnections: Int = 0,
    val interConnections: Int = 0
) {

    fun generateActions(): List<InfluencedAction> =
        buildList {
            var actionId = 0

            val influencerGroupies = (1..influencers).associateBy(
                { inf -> "influencer#$inf" },
                { inf -> (1..influencerFollowers).map { "groupie#$it($inf)" }.toList() })

            val influencerList = influencerGroupies.keys.toList();

            for ((influencer, groupieList) in influencerGroupies) {
                val groupies = Iterators.cycle(groupieList)
                for (i in 1..influence){
                    add(Action(actionId++, influencer) influences Action(actionId++, groupies.next()))
                }

                for (i in 1..backInfluence){
                    add(Action(actionId++, groupies.next()) influences Action(actionId++, influencer))
                }

                for (i in 1..intraConnections){
                    val (groupie1, groupie2) = groupieList.randomPair()
                    add(Action(actionId++, groupie2) influences Action(actionId++, groupie1))
                }

                for (i in 1..interConnections){
                    val randomInfluencer = influencerList.randomExcluding(influencer)
                    val otherGroupie = influencerGroupies[randomInfluencer]!!.randomElement()
                    add(Action(actionId++, otherGroupie) influences Action(actionId++, groupies.next()))
                }
            }
        }
}

@ExperimentalStdlibApi
fun main() {
    val influencers = 10
    val influence = 10
    val followers = 20

    val ag = ActionGenerator(influencers, influence, followers, interConnections = 2, intraConnections = 5)
    val actions = ag.generateActions()
    val graph = DiffusionPathwayGraph.of(actions)

//    graph.display()

    val table = InfluenceTable(graph)
//
//    table.prettyPrint()

    val audienceInfluenceAnalysis = AudienceInfluenceAnalysis(table, 10)

    audienceInfluenceAnalysis.emUntilConvergence(100)
}
