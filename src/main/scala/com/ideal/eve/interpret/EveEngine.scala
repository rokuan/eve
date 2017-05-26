package com.ideal.eve.interpret

import com.ideal.evecore.common.Conversions._
import com.ideal.evecore.interpreter.{EObject, EStringObject}
import com.ideal.evecore.interpreter.EObject._
import com.ideal.evecore.interpreter.data.{EveMappingObject, EveObject, EveStructuredObject}
import com.ideal.evecore.util.{ Option => EOption }
import com.ideal.evecore.util.{ Pair => EPair }
import com.ideal.eve.utils.UtilConversions._

/**
 * Created by chris on 22/05/2017.
 */
class EveEngine extends EveMappingObject(implicitly[(String, EveObject)]("name" -> "Eve")) {
  protected val state = collection.mutable.Map[String, String]()

  override def getType: String = "engine"

  override def set(s: String, eveObject: EveObject): Boolean = false

  override def hasState(s: String): Boolean = state.contains(s)

  override def getState(s: String): EOption[String] = state.get(s)

  override def has(s: String): Boolean = s == "name"

  override def setState(s: String, v: String): Boolean = {
     if (v == "ON") {
       if (v == "true") {
         start()
       } else if (v == "reset") {
         restart()
       } else {
         stop()
       }
     } else {
       false
     }
  }

  protected def start(): Boolean = {
    // TODO:
    true
  }

  protected def restart(): Boolean = {
    // TODO:
    false
  }

  protected def stop(): Boolean = {
    // TODO:
    false
  }
}
