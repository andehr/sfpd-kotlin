package networkanalysis

data class InfluencedAction(
    val influencee: Action,
    val influencer: Action)

data class Action(
    val id: String,
    val userId: String){

    constructor(id: Any, userId: Any) : this(id.toString(), userId.toString())
}

infix fun Action.influences(other: Action) =
    InfluencedAction(other, this)

infix fun Action.influencedBy(other: Action) =
    InfluencedAction(this, other)
