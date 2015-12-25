package universe

import com.rokuan.calliopecore.sentence.Action.ActionType

import scala.util.Try

/**
  * Created by Christophe on 24/12/2015.
  */
object World {
  private val receivers = Map[String, EveReceiver]()

  def getReceiver(name: String): Option[EveReceiver] = receivers.get(name)

  def execute(action: ActionType, args: Any*) = {

  }
}
