package networkanalysis

class AudienceInfluenceAnalysis(val influenceTable: InfluenceTable, val clusters: Int) {

    val belongingness: MutableMap<String, DoubleArray> = mutableMapOf()
    val influence: MutableMap<String, DoubleArray> = mutableMapOf()
    val priors: DoubleArray = DoubleArray(clusters)

    init {
        influenceTable.users().forEach { influence[it] = DoubleArray(clusters) }
    }


}