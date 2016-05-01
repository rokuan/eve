package com.ideal.eve.universe.receivers

import com.ideal.eve.db.EveDatabase
import com.ideal.eve.universe._

/**
  * Created by Christophe on 29/04/2016.
  */
class LightManagerReceiver(val db: EveDatabase) extends EveReceiver {
  override def initReceiver(): Unit = {}

  override def getMappings(): Seq[(String, ValueMatcher)] = List(
    ValueSource.ActionKey -> OrValueMatcher(Array(StringValueMatcher("TURN_ON"), StringValueMatcher("TURN_OFF"))),
    ValueSource.WhatKey -> ObjectValueMatcher(List(
      EveDatabase.TypeKey -> StringValueMatcher("LIGHT")
    ))
  )

  override def handleMessage(message: EveMessage): Unit = {
    message match {
      case EveObjectMessage(action, what: ObjectValueSource, _, _) => {
        // TODO:
      }
      case _ =>
    }
  }

  override def destroyReceiver(): Unit = {}
}
