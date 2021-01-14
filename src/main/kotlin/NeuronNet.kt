class NeuronNet(
    weights: List<List<List<Double>>>
) {
    val layers: List<NeuronLayer> = weights.map { layerWeights ->
        NeuronLayer(
            layerWeights
        )
    }

    fun forwardPass(input: List<Double>): List<Double> {
        layers.forEachIndexed { layerIndex, layer ->
            layer.neurons.forEach { neuron ->
                if (layer == layers.first()) {
                    neuron.input = input.mapIndexed { index, i -> i * neuron.weights[index] }.sum()
                    neuron.value = neuron.activationFunction(neuron.input)
                } else {
                    val previousLayer = layers[layerIndex - 1]
                    neuron.input = previousLayer.neurons.mapIndexed { index, prevNeuron ->
                        prevNeuron.value * neuron.weights[index]
                    }.sum()
                    neuron.value = neuron.activationFunction(neuron.input)
                }
            }
        }

        return layers.last().neurons.map { neuron -> neuron.value }
    }

    private fun backprop(input: List<Double>, output: List<Double>, learningRate: Double) {
        layers.asReversed().forEachIndexed { layerIndex, layer ->
            layer.neurons.forEachIndexed { neuronIndex, neuron ->
                if (layer == layers.last()) {
                    neuron.delta = (output[neuronIndex] - neuron.value) *
                            neuron.activationFunctionDerivative(neuron.input)
                } else {
                    val previousLayer = layers.asReversed()[layerIndex - 1]
                    val deltaIn = previousLayer.neurons.map { prevNeuron ->
                        prevNeuron.delta * prevNeuron.weights[neuronIndex]
                    }.sum()
                    neuron.delta = deltaIn * neuron.activationFunctionDerivative(neuron.input)
                }
            }
        }

        layers.forEachIndexed { layerIndex, layer ->
            layer.neurons.forEach { neuron ->
                if (layerIndex == 0) {
                    neuron.weights = neuron.weights.mapIndexed { weightIndex, weight ->
                        weight + learningRate * neuron.delta * input[weightIndex]}.toMutableList()
                } else {
                neuron.weights = neuron.weights.mapIndexed { weightIndex, weight ->
                    weight + learningRate * neuron.delta * layers[layerIndex - 1].neurons[weightIndex].value }.toMutableList()
                }
            }
        }
    }

    fun train(
        input: List<List<Double>>,
        output: List<List<Double>>,
        learningRate: Double,
        numberOfTrainingIterations: Int
    ) {
        for (i in 0..numberOfTrainingIterations) {
            input.forEachIndexed { index, row ->
                forwardPass(row)
                backprop(row, output[index], learningRate)
            }

            if (i % 100 == 0) {
                println("Iteration: $i")
            }
        }
    }

    fun getWeights(): List<List<List<Double>>> {
        return layers.map { layer ->
            layer.neurons.map { neuron ->
                neuron.weights
            }
        }
    }
}