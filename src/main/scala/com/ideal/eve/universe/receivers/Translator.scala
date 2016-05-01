package com.ideal.eve.universe.receivers

import com.ideal.eve.universe.{EveMessage, EveReceiver, ObjectValueMatcher, ValueMatcher}
import com.rokuan.calliopecore.sentence.IAction.ActionType

/**
  * Created by Christophe on 13/12/2015.
  */
class TranslatorReceiver extends EveReceiver {
  import ValueMatcher._

  override def initReceiver(): Unit = {}

  override def getMappings(): Seq[(String, ValueMatcher)] = List(
    ("action" -> ActionType.TRANSLATE.name()),
    ("how" -> new ObjectValueMatcher("wayType", "LANGUAGE"))
  )

  override def handleMessage(message: EveMessage): Unit = {

  }

  override def destroyReceiver(): Unit = {}

  def translate(text: String, language: String): String = {
    // TODO:
    "Not implemented yet"
  }
}
