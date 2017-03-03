package com.ideal.eve.universe.receivers

import com.ideal.eve.db.EveDatabase
import com.ideal.evecore.common.Mapping.Mapping
import com.ideal.evecore.interpreter.EveObject
import com.ideal.evecore.universe.receiver.{Message, Receiver}
import com.ideal.evecore.universe.{ObjectValueMatcher, OrValueMatcher, StringValueMatcher, ValueMatcher}
import com.rokuan.calliopecore.sentence.IAction.ActionType

import scala.util.{Failure, Try}

/**
  * Created by Christophe on 29/04/2016.
  */
//class LightManagerReceiver(val db: EveDatabase) extends Receiver {
class LightManagerReceiver extends Receiver {
  override def initReceiver(): Unit = {}

  override def getMappings(): Mapping[ValueMatcher] = Map(
    "action" -> OrValueMatcher(Array(StringValueMatcher(ActionType.TURN_ON.name()), StringValueMatcher(ActionType.TURN_OFF.name()))),
    "what" -> ObjectValueMatcher(Map(EveDatabase.TypeKey -> StringValueMatcher("LIGHT")))
  )

  override def handleMessage(message: Message): Try[EveObject] = Failure(new Exception("Not implemented yet"))

  override def destroyReceiver(): Unit = {}
}
