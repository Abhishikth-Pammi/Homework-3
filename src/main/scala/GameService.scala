
import scala.concurrent.Future
case class Steps(id: Int)

class GameService(dir: String) {

  var ng = NGraph(dir)
  var res: List[String] = List("Temp")

  def getStopReason(): Future[String] = {
    Future.successful(res.head)
  }
  def getPoliceChildren(): Future[List[Long]] = {
    Future.successful(ng.policePos.childrenObjects.map(_.id.toLong))
  }

  def getThiefChildren(): Future[List[Long]] = {
    Future.successful(ng.theifPos.childrenObjects.map(_.id.toLong))
  }

  def movePoliceToChild(targetChildId: Long): Future[List[String]] = {
    val newPolicePosOption = ng.policePos.childrenObjects.find(_.id == targetChildId)
    newPolicePosOption match {
      case Some(newPolicePos) =>
        ng.policePos = newPolicePos
        if (checkIfGameEnd()) {
          whoWonGamePoliceOrThief()
        } else {
          Future.successful(ng.policePos.childrenObjects.map(_.id.toString))
        }
      case None =>
        Future.successful(List("InvalidMove"))
    }
  }

  def moveThiefToChild(targetChildId: Long): Future[List[String]] = {
    val newThiefPosOption = ng.theifPos.childrenObjects.find(_.id == targetChildId)
    newThiefPosOption match {
      case Some(newThiefPos) =>
        ng.theifPos = newThiefPos
        if (checkIfGameEnd()) {
          whoWonGamePoliceOrThief()
        } else {
          Future.successful(ng.theifPos.childrenObjects.map(_.id.toString))
        }
      case None =>
        Future.successful(List("InvalidMove"))
    }
  }


  def checkIfGameEnd(): Boolean = {
 if (ng.policePos == ng.theifPos) {
      true
    } else if (ng.theifPos.valuableData) {
      true
    } else if (ng.policePos.childrenObjects.isEmpty) {
   true
    } else if (ng.theifPos.childrenObjects.isEmpty) {
      true
    } else {
      false
    }
  }

  def whoWonGamePoliceOrThief(): Future[List[String]] = {

    if(ng.theifPos.valuableData) {
      this.res = List("Thief Wins found valuable Data")
//      false
    } else if (ng.policePos == ng.theifPos) {
//      true
      this.res = List("Police Wins found Thief")
    } else if(ng.policePos.childrenObjects.isEmpty) {
//      false
      this.res = List("Thief Wins Police can't move")
    } else if(ng.theifPos.childrenObjects.isEmpty) {
//      true
      this.res = List("Police Wins Thief can't move")
    } else {
//      false
      this.res = List("Thief Wins")
    }
    Future.successful(res)
  }

  def restartGame(): Future[String] = {
    Future.successful(ng.restart_game())
  }
}
