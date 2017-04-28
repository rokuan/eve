package com.ideal.eve.universe.receivers

import com.ideal.eve.db.EveDatabase
import com.ideal.evecore.common.Mapping
import com.ideal.evecore.interpreter.data.EveObject
import com.ideal.evecore.io.InterpretationObjectKey
import com.ideal.evecore.universe.matcher.ValueMatcher
import com.ideal.evecore.universe.receiver.{EveObjectMessage, Receiver}
import com.ideal.evecore.util.Result
import com.rokuan.calliopecore.sentence.IAction.ActionType

import com.ideal.evecore.universe.EValueMatcher._
import com.ideal.evecore.common.Conversions._

import scala.util.{Failure, Try}

/**
  * Created by Christophe on 29/04/2016.
  */
class LightManagerReceiver extends Receiver {
  override def initReceiver(): Unit = {}

  override def getMappings(): Mapping[ValueMatcher] = Map[String, ValueMatcher](
    InterpretationObjectKey.Action -> Array[ValueMatcher](ActionType.TURN_ON.name(), ActionType.TURN_OFF.name()),
    InterpretationObjectKey.What -> Map[String, ValueMatcher](EveObject.TYPE_KEY-> "LIGHT")
  )

  override def handleMessage(message: EveObjectMessage): Result[EveObject] = Result.ko(new Exception("Not implemented yet"))

  override def destroyReceiver(): Unit = {}

  override def getReceiverName: String = getClass.getName
}
