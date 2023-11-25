import org.apache.spark.SparkConf
import NetGraphAlgebraDefs.NodeObject
import com.google.common.graph.EndpointPair

import scala.jdk.CollectionConverters._
import java.io.File
import java.util
import org.apache.spark._
import org.apache.spark.graphx.{Edge, EdgeDirection, Graph, VertexId}
import org.apache.spark.rdd.RDD

import scala.util.Random

case class NGraph(dir: String) {
  val conf = new SparkConf().setAppName("RandomWalksApp").setMaster("local")
  val sc = new SparkContext(conf)
  val random = new Random()

  // Define a common prefix for the filenames
  val commonPrefix = "filename"

  // Create an array of possible extensions
  val extensions = Array(".ngs", ".ngs.perturbed")

  // Iterate through possible filenames and try to load them
  val loadedNetGraphs = extensions.flatMap { extension =>
    val fileName = commonPrefix + extension
    val file = new File(dir + fileName)
    println(file.getAbsolutePath)
    if (file.exists()) {
      println("exists")
      Some(NetGraphAlgebraDefs.NetGraph.load(fileName, dir))
    } else {
      println("not exists")
      None
    }
  }

  val nodeList = new util.ArrayList[java.util.Set[NetGraphAlgebraDefs.NodeObject]]()
  val edgeList = new util.ArrayList[java.util.Set[EndpointPair[NetGraphAlgebraDefs.NodeObject]]]()
  if (loadedNetGraphs.isEmpty) {
    // No files were loaded
    println("No NetGraph files found.")
  } else {
    // Process the loaded NetGraphs
    loadedNetGraphs.foreach {
      case Some(netGraph) =>
        // Successfully loaded a NetGraph
        val nodes = netGraph.sm.nodes()
        val edges = netGraph.sm.edges()
        nodeList.add(nodes)
        edgeList.add(edges)
        println(s"NetGraph loaded successfully: ${nodes.size} nodes found.")
      // You can now work with the loaded NetGraph here

      case None =>
        // Failed to load a NetGraph
        println("Failed to load NetGraph.")
    }
  }

  val javaList: java.util.Set[NetGraphAlgebraDefs.NodeObject] = nodeList.get(0)
  val scalaList: List[NetGraphAlgebraDefs.NodeObject] = javaList.asScala.toList
  val valuableNodeList = scalaList.filter(_.valuableData).map(_.id).toSet
  val valuableNodeVertexIds: Set[VertexId] = valuableNodeList.map(_.toLong)

  val graphList = new util.ArrayList[Graph[NodeObject, Unit]]()

  graphList.add(create_graph(sc, nodeList.get(0), edgeList.get(0)))
  graphList.add(create_graph(sc, nodeList.get(1), edgeList.get(1)))

  // graph definitions
  val OG: Graph[NodeObject, Unit] = graphList.get(0)
  val PG: Graph[NodeObject, Unit] = graphList.get(1)

  var startNode = PG.vertices.map(_._2).collect()(random.nextInt(PG.vertices.count().toInt))// Assuming PG is your Graph[NodeObject, Unit]
  var bfsGraph = bfs(PG, startNode.id.toLong)

  // Find the farthest node using RDD transformations
  var farthestNodeVertexId = bfsGraph.vertices
    .aggregate((-1L, -1))(
      (acc, vertex) => if (vertex._2 > acc._2) vertex else acc,
      (acc1, acc2) => if (acc1._2 > acc2._2) acc1 else acc2
    )
    ._1

  // Retrieve the NodeObject for the farthest node
  var farthestNodeOption = PG.vertices.lookup(farthestNodeVertexId).headOption

  // Check if a farthest node was found
  var farthestNode = farthestNodeOption.getOrElse(startNode) // Replace 'defaultNodeObject' with your default NodeObject if needed

  var policePos = farthestNode
  var theifPos = startNode

  def restart_game(): String = {
    this.startNode = this.PG.vertices.map(_._2).collect()(this.random.nextInt(this.PG.vertices.count().toInt)) // Assuming PG is your Graph[NodeObject, Unit]
    this.bfsGraph = bfs(this.PG, this.startNode.id.toLong)

    // Find the farthest node using RDD transformations
    this.farthestNodeVertexId = this.bfsGraph.vertices
      .aggregate((-1L, -1))(
        (acc, vertex) => if (vertex._2 > acc._2) vertex else acc,
        (acc1, acc2) => if (acc1._2 > acc2._2) acc1 else acc2
      )
      ._1

    // Retrieve the NodeObject for the farthest node
    this.farthestNodeOption = this.PG.vertices.lookup(this.farthestNodeVertexId).headOption

    // Check if a farthest node was found
    this.farthestNode = this.farthestNodeOption.getOrElse(this.startNode) // Replace 'defaultNodeObject' with your default NodeObject if needed

    this.policePos = this.farthestNode
    this.theifPos = this.startNode

    "Restarted"
  }

  def create_graph(sc: SparkContext, nodeListItem: java.util.Set[NetGraphAlgebraDefs.NodeObject], edgeListItem: java.util.Set[EndpointPair[NetGraphAlgebraDefs.NodeObject]]): Graph[NodeObject, Unit] = {
    // Sample list of NodeObjects
    val verticesList: List[NodeObject] = nodeListItem.asScala.toList;

    val vertices: RDD[(VertexId, NodeObject)] = sc.parallelize(
      verticesList.map(node => (node.id.toLong, node))
    )

    // We have a list of edges in the form of pairs of NodeObjects
    val edgePairs: List[(NodeObject, NodeObject)] = edgeListItem.asScala.toList.map(ep => (ep.nodeU(), ep.nodeV()))

    // Convert NodeObject pairs to Edge instances by referencing the unique IDs
    val edges: RDD[Edge[Unit]] = sc.parallelize(
      edgePairs.map { case (src, dst) =>
        Edge(src.id.toLong, dst.id.toLong, ()) // EdgeProperty is a placeholder for edge property type
      }
    )

    // Convert list to RDD
    val verticesRDD: RDD[(VertexId, NodeObject)] = sc.parallelize(verticesList.map(node => (node.id.toLong, node)))


    val graph: Graph[NodeObject, Unit] = Graph(vertices, edges)

    return graph

  }

  def bfs(graph: Graph[NodeObject, Unit], startVertexId: VertexId): Graph[Int, Unit] = {
    // Initialize vertices with max value except for the start vertex
    var g = graph.mapVertices((id, _) => if (id == startVertexId) 0 else Int.MaxValue)

    // Number of times the algorithm loop will run
    val maxIterations = graph.vertices.count().toInt

    for (_ <- 0 until maxIterations) {
      val newVertices = g.aggregateMessages[Int](
        triplet => {
          if (triplet.srcAttr != Int.MaxValue) {
            triplet.sendToDst(triplet.srcAttr + 1)
          }
        },
        (a, b) => math.min(a, b)
      )

      val updatedGraph = Graph(newVertices, g.edges)
      val checkConvergence = updatedGraph.vertices.join(g.vertices).map {
        case (id, (newAttr, oldAttr)) => newAttr != oldAttr
      }

      if (!checkConvergence.reduce(_ || _)) {
        return updatedGraph
      }

      g = updatedGraph
    }

    g
  }
}
