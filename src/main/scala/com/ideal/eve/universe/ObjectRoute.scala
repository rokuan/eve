package com.ideal.eve.universe

import com.ideal.eve.universe.ValueMatcher.Mapping

/**
  * Created by Christophe on 28/03/2016.
  */
trait ValueMatcher {
  def matches(v: String): Boolean
}

case object NullValueMatcher extends ValueMatcher {
  override def matches(v: String): Boolean = (v == null)
}
case class OrValueMatcher(values: Array[ValueMatcher]) extends ValueMatcher {
  override def matches(v: String): Boolean = values.exists(_.matches(v))
}

abstract class SimpleValueMatcher(value: AnyVal) extends ValueMatcher {
  override def matches(v: String): Boolean = (value.toString == v)
}
case class IntValueMatcher(value: Int) extends SimpleValueMatcher(value)
case class BooleanValueMatcher(value: Boolean) extends SimpleValueMatcher(value)

case class StringValueMatcher(value: String) extends ValueMatcher {
  override def matches(v: String): Boolean = value == v
}

case class ObjectValueMatcher(value: Seq[Mapping]) extends ValueMatcher {
  override def matches(v: String): Boolean = false
}

class ObjectRoute(val mappings: Seq[Mapping])

object ValueMatcher {
  type Mapping = (String, ValueMatcher)

  def apply(v: Any): ValueMatcher = {
    v match {
      case null => NullValueMatcher
      case b: Boolean => BooleanValueMatcher(b)
      case i: Int => IntValueMatcher(i)
      case s: Seq[_] => OrValueMatcher(s.map(ValueMatcher(_)).toArray)
      case _ => StringValueMatcher(v.toString)
    }
  }
}
