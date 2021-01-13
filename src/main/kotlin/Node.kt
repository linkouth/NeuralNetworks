import kotlin.math.exp

class Node (
    val name: String,
    val inputNodes: MutableList<Node> = mutableListOf(),
    var operation: Operation? = null
) {
    override fun toString(): String {
        return "Node(name='$name', operation= '$operation', inputNodes=$inputNodes)"
    }

    fun computeValue(): Double {
        if (inputNodes.isEmpty()) {
            return when(operation?.operation) {
                OperationEnum.ADDITIVE -> .0
                OperationEnum.MULTIPLY -> 1.0
                OperationEnum.EXPONENTIAL -> exp(.0)
                OperationEnum.CONSTANT -> operation?.value ?: .0
                else -> .0
            }
        }

        if (operation?.operation == OperationEnum.ADDITIVE) {
            var value = .0
            inputNodes.forEach { value += it.computeValue() }
            return value
        } else if (operation?.operation == OperationEnum.MULTIPLY) {
            var value = 1.0
            inputNodes.forEach { value *= it.computeValue() }
            return value
        } else if (operation?.operation == OperationEnum.EXPONENTIAL) {
            var value = .0
            inputNodes.forEach { value += it.computeValue() }
            return exp(value)
        }

        return .0
    }
}