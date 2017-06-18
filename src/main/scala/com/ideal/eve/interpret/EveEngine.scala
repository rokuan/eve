package com.ideal.eve.interpret

import com.ideal.evecore.common.Conversions._
import com.ideal.evecore.interpreter.EObject._
import com.ideal.evecore.interpreter.data.{AbstractEveStructuredObject, EveObject}
import com.ideal.evecore.util.{ Option => EOption }
import com.rokuan.calliopecore.sentence.IAction.ActionType

/**
 * Created by chris on 22/05/2017.
 */
class EveEngine extends AbstractEveStructuredObject() {
  protected val fields = collection.mutable.Map[String, EveObject](
    "name" -> "Eve"
  )
  protected val state = collection.mutable.Map[String, String]()

  override def getType: String = "engine"

  override def set(s: String, eveObject: EveObject): Boolean = false

  override def hasState(s: String): Boolean = state.contains(s)

  override def getState(s: String): EOption[String] = state.get(s)

  override def has(s: String): Boolean = fields.contains(s)

  override def setState(s: String, v: String): Boolean = {
    state.put(s, v)
    true
  }

  override def get(s: String): EOption[EveObject] = fields.get(s)

  override def call(actionType: ActionType): Boolean = {
    actionType match {
      case ActionType.START => start()
      case ActionType.START_AGAIN => restart()
      case ActionType.STOP => stop()
      case _  => false
    }
  }

  protected def start(): Boolean = {
    // Already started
    println("Boot")
    true
  }
  protected def restart(): Boolean = {
    // TODO: reboot the machine
    println("Reboot")
    true
  }
  protected def stop(): Boolean = {
    println("Shutdown")
    false // TODO:
  }
}
