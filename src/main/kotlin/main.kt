import java.io.File
import kotlin.Exception
import kotlin.system.exitProcess

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

const val HELP = "-h"
const val MODE = "-m"
const val INPUT_FILE = "-i"
const val OUTPUT_FILE = "-o"
const val NETWORK = "-n"
const val PARAMETERS = "-p"

fun main(args: Array<String>) {
    val arguments = parseArguments(args)

    if (!validateArguments(arguments)) {
        return
    }

    when (arguments[MODE]) {
        "1" -> task1(arguments)
        "2" -> task2(arguments)
        "3" -> task3(arguments)
        "4" -> task4(arguments)
        "5" -> task5(arguments)
    }

    exitProcess(0)
}

fun parseArguments(args: Array<String>): Map<String, String> {
    return args.fold(Pair(emptyMap<String, List<String>>(), "")) { (map, lastKey), elem ->
        if (elem.startsWith("-"))  Pair(map + (elem to emptyList()), elem)
        else Pair(map + (lastKey to map.getOrDefault(lastKey, emptyList()) + elem), lastKey)
    }.first.mapValues { it.value.first() }
}

fun validateArguments(arguments: Map<String, String>): Boolean {
    if (HELP in arguments) { printHelp() }

    if (MODE !in arguments) {
        println("Требуется режим работы")
        return false
    }
    if (INPUT_FILE !in arguments) {
        println("Необходимо подать на вход файл")
        return false
    }
    if (arguments[MODE] == "5") {
        if (NETWORK !in arguments) {
            println("Необходимо подать на вход веса")
            return false
        }
        if (PARAMETERS !in arguments) {
            println("Необходимо подать на вход параметры")
            return false
        }
    }

    return true
}

fun printHelp() {
    val helpDescription = """
        -h              справка
        -m              режим работы (Лаб 1: "1", Лаб 2: "2", ...)
        -i              путь к файлу с входящей информацией
        -o              путь к файлу с выводом результата работы
        -n              путь к файлу с весами
        -p              путь к файлу с параметрами обучения сети
    """.trimIndent()
    println(helpDescription)
    exitProcess(0)
}

fun task1(arguments: Map<String, String>): String {
    val gson = Gson()

    val inputText: String
    try {
        inputText = File(arguments[INPUT_FILE] ?: "").readText()
    } catch (error: Exception) {
        println(error)
        exitProcess(1)
    }

    val nodeDtoList: List<NodeDTO>
    try {
        nodeDtoList = gson.fromJson(inputText, object : TypeToken<List<NodeDTO>>() {}.type)
    } catch (error: Exception) {
        println("Некорректный JSON ${arguments[INPUT_FILE]}")
        println(error)
        exitProcess(1)
    }

    val nameToNode = mutableMapOf<String, Node>()
    nodeDtoList.forEach { nodeDto ->
        if (nodeDto.to !in nameToNode) {
            nameToNode[nodeDto.to] = Node(name = nodeDto.to)
        } else if (nodeDto.from !in nameToNode) {
            nameToNode[nodeDto.from] = Node(name = nodeDto.from)
        }
    }
    val nodeList = NodeList(nameToNode.values.toSet())
    try {
        nodeDtoList.forEach { item ->
            nodeList.add(nameToNode[item.to], Pair(nameToNode[item.from], item.index))
        }
    } catch (error: Exception) {
        println("Error 3")
    }
    return gson.toJson(nodeList.getNodeNameToInputFrom())
}

fun task2(arguments: Map<String, String>) {
    val gson = Gson()

    val inputText = """
        [
            {"name": "A", "inputFrom": []},
            {"name": "C", "inputFrom": [2,2,0]},
            {"name": "D", "inputFrom": []},
            {"name": "B", "inputFrom": [1,0]}
        ]
    """.trimIndent()

//    val inputText: String
//    try {
//        inputText = File(arguments[INPUT_FILE] ?: "").readText()
//    } catch (error: Exception) {
//        println(error)
//        exitProcess(1)
//    }

    val nodeDepsList: List<NodeDeps>
    try {
        nodeDepsList = gson.fromJson(inputText, object : TypeToken<List<NodeDeps>>() {}.type)
    } catch (error: Exception) {
        println("Некорректный JSON ${arguments[INPUT_FILE]}")
        println(error)
        exitProcess(1)
    }

    val nodeNames = nodeDepsList.map { nodeDeps -> nodeDeps.name }
    val nodes = nodeDepsList.map { nodeDeps -> Node(name = nodeDeps.name) }


    nodeDepsList.forEach { nodeDeps ->
        val node = nodes.find { it.name == nodeDeps.name }
        nodeDeps.inputFrom.forEach { inputIndex ->
            (nodes.find { it.name == nodeNames[inputIndex] })?.let {
                node?.inputNodes?.add(it)
            }
        }
    }

    try {
        var isAcyclic = false
        for (node in nodes ) {
            isAcyclic = dfs(node, nodes)
            if (isAcyclic) {
                break
            }
        }
        if (isAcyclic) {
            println("Граф содержит цикл")
            exitProcess(2)
        }
    } catch (error: StackOverflowError) {
        println("Граф содержит цикл")
        exitProcess(2)
    }

    val nodesToPrint = nodes.toMutableSet()
    nodes.forEach { node ->
        node.inputNodes.forEach { inputNode ->
            if (inputNode in nodesToPrint) {
                nodesToPrint.remove(inputNode)
            }
        }
    }

    nodesToPrint.forEach { node ->
        printDeps(node)
        println()
    }
}

fun dfs(startNode: Node, nodes: List<Node>): Boolean {
    val visitedNodes = nodes.associateBy({ it }, { 0 }).toMutableMap()
    visitedNodes[startNode] = 1

    startNode.inputNodes.forEachIndexed { _, node ->
        if (visitedNodes[node] == 0) {
            if (dfs(node, nodes)) {
                return true
            }
        } else if (visitedNodes[node] == 1) {
            return true
        }
    }
    visitedNodes[startNode] = 2
    return false
}

fun printDeps(node: Node) {
    if (node.inputNodes.isEmpty()) {
        print(node.name)
        return
    }
    print("${node.name}(")
    node.inputNodes.forEachIndexed { index, inputNode ->
        printDeps(inputNode)
        if (node.inputNodes.lastIndex != index) {
            print(",")
        }
    }
    print(")")
}

fun task3(arguments: Map<String, String>) {
    val gson = Gson()

    val inputText: String
    try {
        inputText = File(arguments[INPUT_FILE] ?: "").readText()
    } catch (error: Exception) {
        println(error)
        exitProcess(1)
    }

    val computationBlock: ComputationBlock
    try {
        computationBlock = gson.fromJson(inputText, object : TypeToken<ComputationBlock>() {}.type)
    } catch (error: Exception) {
        println("Некорректный JSON ${arguments[INPUT_FILE]}")
        println(error)
        exitProcess(1)
    }

    val nodes = mutableListOf<Node>()
    val nodesToComputeValue = mutableListOf<Node>()

    var currentNodeName: String? = null
    var countOpenBracket = 0

    // Parse node dependencies
    for (ch in computationBlock.function) {
        if (currentNodeName != null) {
            if (ch.isLetter() && countOpenBracket < 2) {
                val node = nodes.find { it.name == currentNodeName }
                var depNode = nodes.find { it.name == ch.toString() }
                if (depNode == null) {
                    depNode = Node(name = ch.toString())
                    nodes.add(depNode)
                }
                node?.inputNodes?.add(depNode)
            } else if (ch == ')') {
                countOpenBracket--
            } else if (ch == '(') {
                countOpenBracket++
            }
            if (countOpenBracket == 0) {
                currentNodeName = null
            }
        } else {
            if (ch.isLetter()) {
                val node = nodes.find { it.name == ch.toString() }
                if (node == null) {
                    currentNodeName = ch.toString()
                    val newNode = Node(name = currentNodeName)
                    nodes.add(newNode)
                    nodesToComputeValue.add(newNode)
                } else {
                    nodesToComputeValue.add(node)
                }
            }
        }
    }

    // Parse node operations
    computationBlock.operations.forEach { (nodeName, value) ->
        val node = nodes.find { it.name == nodeName }
        val operation: Operation = when (value) {
            "+" -> Operation(OperationEnum.ADDITIVE)
            "*" -> Operation(OperationEnum.MULTIPLY)
            "exp" -> Operation(OperationEnum.EXPONENTIAL)
            else -> {
                Operation(OperationEnum.CONSTANT, value.toDouble())
            }
        }
        node?.operation = operation
    }

    nodes.forEach { println(it) }
    nodesToComputeValue.forEach { println("${it.name} ${it.computeValue()}") }
}

fun task4(arguments: Map<String, String>) {
    val gson = Gson()

    val networkText: String
    try {
        networkText = File(arguments[INPUT_FILE] ?: "").readText()
    } catch (error: Exception) {
        println(error)
        exitProcess(1)
    }

    val weights: List<List<List<Double>>>
    try {
        weights = gson.fromJson(networkText, object : TypeToken<List<List<List<Double>>>>() {}.type)
    } catch (error: Exception) {
        println("Некорректный JSON ${arguments[INPUT_FILE]}")
        println(error)
        exitProcess(1)
    }
    val neuronNet = NeuronNet(weights)
    val validInputLength = neuronNet.layers.first().neurons.first().weights.size

    val stdInput = generateSequence(::readLine)

    stdInput.forEach { stringInput ->
        val input: List<Double>
        try {
            input = gson.fromJson(stringInput, object : TypeToken<List<Double>>() {}.type)
            if (input.size != validInputLength) {
                throw Exception("Число входящих нейронов должно совпадать с первым слоем")
            }
            neuronNet.forwardPass(input).forEach { println(it) }
        } catch (error: IllegalStateException) {
            print("Конец работы")
            exitProcess(0)
        } catch (error: Exception) {
            println(error)
        }
    }
}

fun task5(arguments: Map<String, String>) {
    val gson = Gson()

    val networkText: String
    try {
        networkText = File(arguments[NETWORK] ?: "").readText()
    } catch (error: Exception) {
        println(error)
        exitProcess(1)
    }

    val weights: List<List<List<Double>>>
    try {
        weights = gson.fromJson(networkText, object : TypeToken<List<List<List<Double>>>>() {}.type)
    } catch (error: Exception) {
        println("Некорректный JSON ${arguments[NETWORK]}")
        println(error)
        exitProcess(1)
    }

    val inputText: String
    try {
        inputText = File(arguments[INPUT_FILE] ?: "").readText()
    } catch (error: Exception) {
        println(error)
        exitProcess(1)
    }

    val trainData: List<TrainData>
    try {
        trainData = gson.fromJson(inputText, object : TypeToken<List<TrainData>>() {}.type)
    } catch (error: Exception) {
        println("Некорректный JSON ${arguments[INPUT_FILE]}")
        println(error)
        exitProcess(1)
    }

    val parametersText: String
    try {
        parametersText = File(arguments[PARAMETERS] ?: "").readText()
    } catch (error: Exception) {
        println(error)
        exitProcess(1)
    }

    val parameters: Parameters
    try {
        parameters = gson.fromJson(parametersText, object : TypeToken<Parameters>() {}.type)
    } catch (error: Exception) {
        println("Некорректный JSON ${arguments[PARAMETERS]}")
        println(error)
        exitProcess(1)
    }

    val input = trainData.map { data -> data.i }
    val output = trainData.map { data -> data.o }

    val neuronNet = NeuronNet(weights)

    var error = .0
    input.forEachIndexed { index, row ->
        val pred = neuronNet.forwardPass(row)
        error += (output[index].first() - pred.first())
    }

    println("------------Веса до обучения------------")
    neuronNet.layers.forEach { println(it) }
    println("------------Обучение------------")
    neuronNet.train(
        input = input,
        output = output,
        learningRate = parameters.k,
        numberOfTrainingIterations = parameters.n
    )
    println("------------Предсказания до обучения------------")
    println("Ошибка: $error")
    println("------------Предсказания после обучения------------")
    error = .0
    input.forEachIndexed { index, row ->
        val pred = neuronNet.forwardPass(row)
        error += (output[index].first() - pred.first())
    }
    println("Ошибка: $error")
    println("------------Веса после обучения------------")
    neuronNet.layers.forEach { println(it) }

    File(arguments[OUTPUT_FILE] ?: "weights.json").writeText(gson.toJson(neuronNet.getWeights()))
}