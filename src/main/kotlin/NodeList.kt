import java.lang.Exception

class NodeList (nodeSet: Set<Node>) {
    private val nodeList: MutableMap<Node, MutableList<Pair<Node, Int>>> = mutableMapOf()

    init {
        nodeSet.forEach { k ->
            nodeList[k] = mutableListOf()
        }
    }

    fun add(node: Node?, inputNodeAndIndex: Pair<Node?, Int>) {
        val (inputNode, index) = inputNodeAndIndex
        if (node == null || inputNodeAndIndex.first == null) {
            return
        }
        if (nodeList[node]?.indexOfFirst { it.second == index } != -1) {
            throw Exception()
        }
        inputNode?.let { nodeList[node]?.add(Pair(it, index)) }
    }

    fun getNodeNameToInputFrom (): MutableMap<String, List<Int>> {
        val nodeToIndex = nodeList.keys.toList()
        val nodeNameToInputFrom: MutableMap<String, List<Int>> = mutableMapOf()
        nodeList.forEach {(key, value) ->
            nodeNameToInputFrom[key.name] = value.map { inputNode -> nodeToIndex.indexOf(inputNode.first) }
        }

        return nodeNameToInputFrom
    }
}