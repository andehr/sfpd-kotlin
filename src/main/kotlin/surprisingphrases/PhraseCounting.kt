import Arc.Type.*
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Andrew D. Robertson on 26/12/2020.
 */

data class PhraseOptions(val minPhraseSize: Int = 1,
                         val maxPhraseSize: Int = 6,
                         val minLeafPruning: Double = 0.3,
                         val minPhraseFrequency: Double = 4.0,
                         val level1: Double = 1.0,
                         val level2: Double = 7.0,
                         val level3: Double = 15.0,
                         val stopwords: Set<String> = setOf())

class PhraseCounter(val word: String, val opts: PhraseOptions = PhraseOptions()){

    val root = Node.buildRoot(word, opts)

    fun getWordIndices(context: List<String>): List<Int> =
        context.withIndex().filter { it.value == word }.map { it.index }

    fun topNodes(n: Int): List<Node> {
        root.filterChildrenRecursive()

        return root.nodes()
                .filter { it.size >= opts.minPhraseSize }
                .greatest(n)
    }

    fun addContext(context: List<String>, weight: Double = 1.0){

        val occurrences = getWordIndices(context)

        for (wordIndex in occurrences){

            root.incCount(weight)

            var currentNode = root
            var lastBeforeNode = root

            // All the tokens before the word in reverse order (in specified phrase window)
            val beforeTokens = context.subList(max(0, wordIndex - opts.maxPhraseSize + 1), wordIndex).asReversed()
            // All the tokens after the word (in specified phrase window)
            val afterTokens = if (context.size - 1 == wordIndex) listOf() else context.subList(wordIndex + 1, min(wordIndex + opts.maxPhraseSize, context.size))

            for (afterToken in afterTokens) {
                currentNode = currentNode.incForwardChild(afterToken, weight)
            }

            for ((idx, beforeToken) in beforeTokens.withIndex()){
                currentNode = lastBeforeNode.incReverseChild(beforeToken, weight)
                lastBeforeNode = currentNode

                for (idx2 in 0 until (min(afterTokens.size, opts.maxPhraseSize - idx + 2))){
                    currentNode = currentNode.incForwardChild(afterTokens[idx2], weight)
                }
            }
        }
    }
}

fun dynamicThreshold(numChoices: Int, frequency: Double, opts: PhraseOptions): Double =
    when {
        frequency < opts.level1 -> 1.0
        frequency < opts.level2 -> 0.75
        frequency < opts.level3 -> 0.5
        else -> maxOf(1.0 / numChoices, opts.minLeafPruning)
    }

class Node (val parent: Node?, val toParent: Arc, var count: Double = 0.0, val opts: PhraseOptions) : Comparable<Node> {

    val isRoot: Boolean
        get() = parent == null || toParent.isRoot
    val isNotRoot: Boolean
        get() = !isRoot
    var children: MutableMap<Arc, Node> = mutableMapOf()
    val depth: Int = if (parent == null) 0 else parent.depth + 1
    val text: String = toParent.text
    val size: Int
        get() = depth + 1

    fun hasParent() = parent != null
    fun hasChildren() = children.isNotEmpty()
    fun hasChild(arc: Arc) = arc in children
    fun hasForwardChild(text: String) = FORWARD.of(text) in children
    fun hasReverseChild(text: String) = REVERSE.of(text) in children

    fun addChild(arc: Arc, count: Double = 1.0) =
        Node(parent=this, arc, count, opts).also { children[arc] = it }
    fun addForwardChild(text: String, count: Double = 1.0) = addChild(FORWARD.of(text), count)
    fun addReverseChild(text: String, count: Double = 1.0) = addChild(REVERSE.of(text), count)

    fun incChild(arc: Arc, increment: Double): Node =
        children[arc]?.incCount(increment) ?: addChild(arc, increment)
    fun incForwardChild(text: String, increment: Double) = incChild(FORWARD.of(text), increment)
    fun incReverseChild(text: String, increment: Double) = incChild(REVERSE.of(text), increment)

    fun incCount(increment: Double) = apply { count += increment }
    operator fun plus(increment: Double) = incCount(increment)

    override fun toString() = text + when (parent) {
        null -> ""
        else -> " ($parent)"
    }

    private fun stopwordCount(): Int =
        ngram().filter { it in opts.stopwords }.size

    private fun endswithStopword(): Boolean =
        text in opts.stopwords

    fun filterChildrenRecursive(){

        val forward = children
            .filterValues { it.count > 0 }.filterKeys { it.isForward }.let { filterChildren(count, it, opts) }
        val reverse = children
            .filterValues { it.count > 0 }.filterKeys { it.isReverse }.let { filterChildren(count, it, opts) }

        children = mutableMapOf<Arc, Node>().apply {
            putAll(forward)
            putAll(reverse)
        }

        children.values.forEach { it.filterChildrenRecursive() }
    }

    fun filterChildren(count: Double, children: Map<Arc, Node>, opts: PhraseOptions): Map<Arc, Node>{
        val choices = children.size
        val totalFrequency = children.values.sumOf { it.count }

        // If the number of possible extension phrases is equal to the total number of occurrences of this phrase, then prune them all.
        if (choices.toDouble() eq totalFrequency) {
            return mapOf()
        }

        return children.filterValues { (it.count ge opts.minPhraseFrequency) && ((it.count / count) ge dynamicThreshold(choices, totalFrequency, opts)) }
    }

    fun ngram(): List<String> {
        if (isRoot) return listOf(text)

        val ancestors = mutableListOf<String>()
        var reverseStart = 0
        var node = this
        while(node.isNotRoot){
            if (node.toParent.isForward){
                reverseStart += 1
            }
            ancestors.add(node.text)
            node = node.parent!! // Never null because of root check
        }
        return ancestors.subList(reverseStart, ancestors.size) + node.text + ancestors.subList(0, reverseStart).asReversed()
    }

    private fun ancestors() = sequence {
        var node = this@Node
        while (node.isNotRoot){
            yield(Ancestor(node.parent!!, node.count))
            node = node.parent!!
        }
    }

    private fun ancestorMap(): Map<Node, Double> {
        if (isRoot){
            return mapOf()
        }
        val ancestors = mutableMapOf<Node, Double>()
        var ancestor = Ancestor(parent!!, count)
        while (ancestor.isNotRoot){
            ancestors[ancestor.node] = ancestor.childCount
            ancestor = Ancestor(ancestor.node.parent!!, ancestor.node.count)
        }
        ancestors[ancestor.node] = ancestor.childCount // add root
        return ancestors
    }

    fun nodes(): List<Node> {
        val found = mutableListOf(this)
        val explore = mutableListOf(this)
        while (explore.isNotEmpty()) {
            val children = explore.removeFirst().children.values
            found += children
            explore += children.filter { it.hasChildren() }
        }
        return found
    }

    fun prettyPrint(prefix: String = "", isTail: Boolean = true){
        println(prefix + arcString(isTail, toParent.type) + text + "($count)")

        with(children.values.toList()){
            if (isNotEmpty()){
                subList(0, size - 1).forEach { it.prettyPrint(prefix + (if (isTail) "    " else "│   "), false)}
                last().prettyPrint(prefix + (if (isTail) "    " else "│   "), true)
            }
        }
    }

    fun arcString(isTail: Boolean, type: Arc.Type): String = when (type){
        FORWARD -> if (isTail) "└>─ " else "├>─ "
        REVERSE -> if (isTail) "└<─ " else "├<─ "
        ROOT -> if (isTail) "└── " else "├── "
    }

    companion object Factory {

        fun buildRoot(word: String, opts: PhraseOptions) = Node(null, ROOT.of(word), opts = opts)
    }

    override fun compareTo(other: Node): Int {
        val normalOrder = depth < other.depth
        val shallower = if (normalOrder) this else other
        val deeper = if (normalOrder) other else this

        val shallowerAncestors = shallower.ancestorMap()

        for (ancestor in deeper.ancestors()){
            // if shallower is lowest common ancestor, then sort the shallower less than the deeper
            if (ancestor.node == shallower) {
                return if (normalOrder) -1 else 1
            } else if (ancestor.node in shallowerAncestors){
                val countDiff = shallowerAncestors[ancestor.node]!! - ancestor.childCount
                return if (countDiff eq 0.0) {
                    val diff = deeper.stopwordCount() - shallower.stopwordCount()
                    if (diff == 0){
                        if (shallower.endswithStopword() xor normalOrder) -1 else 1
                    } else {
                        if (normalOrder) diff else -diff
                    }
                } else (if (normalOrder) countDiff else -countDiff).toInt()
            }
        }
        error("Unable to sort phrases because there was no common ancestor node. This shouldn't be possible because the root node is always the common ancestor.")
    }
}

data class Ancestor (val node: Node, val childCount : Double) {

    val isRoot : Boolean
        get() = node.isRoot

    val isNotRoot : Boolean
        get() = !isRoot
}

data class Arc (val text: String, val type: Type){

    val isForward : Boolean
        get() = type == FORWARD
    val isReverse : Boolean
        get() = type == REVERSE
    val isRoot : Boolean
        get() = type == ROOT

    enum class Type {
        FORWARD, REVERSE, ROOT;

        fun of(text: String) = Arc(text, this)
    }
}

