package networkanalysis

data class InfluencedAction(
    val influencee: Action,
    val influencer: Action)

data class Action(
    val id: String,
    val userId: String
)