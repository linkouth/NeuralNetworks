import kotlin.math.abs
import kotlin.math.pow

class Neuron (
    var weights: MutableList<Double>,
    var value: Double = .0,
    var input: Double = .0,
    var delta: Double = .0
) {
    val activationFunction = fun (x:Double): Double {
        return x / (1 + abs(x))
    }

    val activationFunctionDerivative = fun (x:Double): Double {
        return 1 / (1 + abs(x)).pow(2)
    }

    override fun toString(): String {
        return "neuralNetworks.Neuron(weights=$weights)"
    }
}