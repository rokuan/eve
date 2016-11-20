package com.ideal.eve.universe.receivers

import com.ideal.evecore.common.Mapping.Mapping
import com.ideal.evecore.interpreter.EveObject
import com.ideal.evecore.universe.{ObjectValueMatcher, ValueMatcher}
import com.ideal.evecore.universe.receiver.{Message, Receiver}
import com.rokuan.calliopecore.sentence.IAction.ActionType
import com.rokuan.calliopecore.sentence.structure.data.way.WayAdverbial.WayType

import scala.util.{Failure, Try}

/**
  * Created by Christophe on 13/12/2015.
  */
class TranslatorReceiver extends Receiver {
  override def initReceiver(): Unit = {}

  override def getMappings(): Mapping[ValueMatcher] = Map(
    ("action" -> ActionType.TRANSLATE.name()),
    ("how" -> ObjectValueMatcher(Map("wayType" -> WayType.LANGUAGE.name())))
  )

  override def handleMessage(message: Message): Try[EveObject] = Failure(new Exception("Not implemented yet"))

  override def destroyReceiver(): Unit = {}

  def translate(text: String, language: String): String = {
    // TODO:
    "Not implemented yet"
  }
}
