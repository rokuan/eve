package com.ideal.eve.universe.receivers

import com.ideal.eve.universe._
import com.rokuan.calliopecore.sentence.IAction.ActionType

/**
  * Created by Christophe on 13/12/2015.
  */
class UnitConverterReceiver extends EveReceiver {
  import ValueMatcher._

  override def initReceiver(): Unit = {}

  override def getMappings(): Seq[(String, ValueMatcher)] = List(
    ("action" -> new ObjectValueMatcher("action", ActionType.CONVERT.name())),
    ("how" -> new ObjectValueMatcher("wayType", "UNIT"))
  )

  override def handleMessage(message: EveMessage): Unit = message match {
    case EveObjectMessage(action, text: ObjectValueSource, how: ObjectValueSource, _) =>
      // TODO
    case _ =>
  }

  override def destroyReceiver(): Unit = {}
}
