class NeuronLayer(
    weights: List<List<Double>>
) {
    val neurons: List<Neuron> = weights.map {
        Neuron(
            it.toMutableList()
        )
    }

    override fun toString(): String {
        return neurons.joinToString()
    }
}