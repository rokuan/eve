package universe

import com.rokuan.calliopecore.sentence.Action.ActionType

/**
  * Created by Christophe on 24/12/2015.
  */
sealed trait Message
case class ActionMessage(val action: ActionType) extends Message