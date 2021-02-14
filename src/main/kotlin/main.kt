import collections.Counter

/**
 * Created by Andrew D. Robertson on 23/12/2020.
 */

fun main() {
    val target = Counter(smoothing = 0.1)
    with(target){
        inc("test", 4.0)
        inc("this", 4.0)
    }

    val background = Counter(smoothing = 0.1)
    with(background) {
        inc("test", 1.0)
        inc("this", 4.0)
    }

    val scorer = SFPDFeatureScorer(target, background, likelihoodLiftRatio = 0.4)

    val word = "dog"

    val sents = listOf(
        "the dog was happy",
        "the red dog",
        "the red dog ate",
        "the red dog was happy",
        "the dog ate",
        "the red dog swam"
    ).map { it.split(" ") }

    val p = PhraseCounter(word, PhraseOptions(minLeafPruning = 0.5, level1 = 0.0, level2 = 0.0, level3 = 0.0, minPhraseSize = 1))

    sents.forEach { p.addContext(it) }

    val top = p.topNodes(5)

    for (node in top) {
        println(node.ngram())
    }

    p.root.filterChildrenRecursive()

//    p.root.prettyPrint()

//    val c2 = collections.Counter.fromIterable(listOf("test", "test", "this"), smoothing = 0.2)

//    val c3 = collections.Counter(smoothing = 0.1, counts= mutableMapOf("this" to 2.0, "that" to 3.0))

//    val moshi: Moshi = Moshi.Builder().build()
//    val adapter = moshi.adapter(collections.Counter::class.java)
//
//    val text = adapter.toJson(c);

    println()

}
