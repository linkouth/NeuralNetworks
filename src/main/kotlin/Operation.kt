class Operation (
    val operation: OperationEnum,
    val value: Double? = null
) {
    private val isConstant get() = operation == OperationEnum.CONSTANT

    override fun toString(): String {
        if (isConstant) {
            return value.toString()
        }
        return operation.toString()
    }
}