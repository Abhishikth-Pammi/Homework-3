
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util._

object Main extends App {

  if (args.length < 1) {
    println("Usage: Main <arg1>")
  } else {
    val arg1 = args(0)

  implicit val system = ActorSystem("GameServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val gameService = new GameService(arg1)

  val route =
    path("restartgame") {
      get {
        onComplete(gameService.restartGame()) {
          case util.Success(res) => complete(res)
          case util.Failure(ex) => complete(StatusCodes.InternalServerError)
        }
      }
    } ~ path("gameendreason") {
      get {
        onComplete(gameService.getStopReason()) {
          case util.Success(res) => complete(res)
          case util.Failure(ex) => complete(StatusCodes.InternalServerError)
        }
      }
    } ~ path("move" / "police") {
    get {
      onComplete(gameService.getPoliceChildren()) {
        case util.Success(res) => complete(res)
        case util.Failure(ex) => complete(StatusCodes.InternalServerError)
      }
    }
  } ~ path("move" / "police" / IntNumber) { id =>
      post {
        onComplete(gameService.movePoliceToChild(id)) {
          case Success(res) => complete(res)
          case Failure(ex) => complete(StatusCodes.InternalServerError)
        }
      }
  } ~ path("move" / "thief") {
    get {
      onComplete(gameService.getThiefChildren()) {
        case util.Success(res) => complete(res)
        case util.Failure(ex) => complete(StatusCodes.InternalServerError)
      }
    }
  } ~ path("move" / "thief" / IntNumber) { id =>
    post {
      onComplete(gameService.moveThiefToChild(id)) {
        case Success(res) => complete(res)
        case Failure(ex) => complete(StatusCodes.InternalServerError)
      }
    }
  }

  val server = Http().bindAndHandle(Route.handlerFlow(route), "localhost", 9090)
  server.map { _ =>
    println("Successfully started on localhost:9090 ")
  } recover { case ex =>
    println("Failed to start the server due to: " + ex.getMessage)
  }
}}