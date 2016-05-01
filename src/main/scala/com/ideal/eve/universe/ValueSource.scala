package com.ideal.eve.universe

import com.ideal.eve.universe.ValueSource.ObjectMap

/**
  * Created by Christophe on 23/04/2016.
  */
trait ValueSource {
  def isNumber(): Boolean = false
  def isString(): Boolean = false
  def isBoolean(): Boolean = false
  def isObject(): Boolean = false
  def isNull(): Boolean = false
  def getNumber(): Number = 0
  def getString(): String = ""
  def getBoolean(): Boolean = false
  def getObject(): ObjectMap = null
}

case class NumberValueSource(n: Number) extends ValueSource {
  override def isNumber(): Boolean = true
  override def getNumber(): Number = n
}

case class BooleanValueSource(b: Boolean) extends ValueSource {
  override def isBoolean(): Boolean = true
  override def getBoolean(): Boolean = b
}

case class StringValueSource(s: String) extends ValueSource {
  override def isString(): Boolean = true
  override def getString(): String = s
}

case class ObjectValueSource(o: ObjectMap) extends ValueSource {
  def this(values: (String, ValueSource)*) = this(Map[String, ValueSource](values: _*))
  override def isObject(): Boolean = true
  override def getObject(): ObjectMap = o
}

case object NullValueSource extends ValueSource {
  override def isNull(): Boolean = true
}

object ValueSource {
  type ObjectMap = Map[String, ValueSource]

  implicit def stringToValueSource(s: String): StringValueSource = StringValueSource(s)

  val ActionKey = "action"
  val WhatKey = "what"
  val HowKey = "how"
}


