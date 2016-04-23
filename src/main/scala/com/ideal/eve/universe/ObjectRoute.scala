package com.ideal.eve.universe

import com.ideal.eve.universe.ValueMatcher.Mapping

/**
  * Created by Christophe on 28/03/2016.
  */
trait ValueMatcher {
  def matches(v: ValueSource): Boolean
}

case object NullValueMatcher extends ValueMatcher {
  override def matches(v: ValueSource): Boolean = v.isNull()
}
case class OrValueMatcher(values: Array[ValueMatcher]) extends ValueMatcher {
  override def matches(v: ValueSource): Boolean = values.exists(_.matches(v))
}
case class NumberValueMatcher(value: Number) extends ValueMatcher {
  override def matches(v: ValueSource): Boolean = v.isNumber() && v.getNumber() == value
}
case class BooleanValueMatcher(value: Boolean) extends ValueMatcher {
  override def matches(v: ValueSource): Boolean = v.isBoolean() && v.getBoolean() == value
}
case class StringValueMatcher(value: String) extends ValueMatcher {
  override def matches(v: ValueSource): Boolean =
    (v.isString() && v.getString() == value) || (v.isNull && v == null)
}

case class ObjectValueMatcher(value: Seq[Mapping]) extends ValueMatcher {
  override def matches(v: ValueSource): Boolean =
    v.isObject() && {
      val o = v.getObject()
      value.forall { case (key, matcher) => o.contains(key) && matcher.matches(o(key)) }
    }
}

class ObjectRoute(val mappings: Seq[Mapping])

object ValueMatcher {
  type Mapping = (String, ValueMatcher)

  def apply(v: Any): ValueMatcher = {
    v match {
      case null => NullValueMatcher
      case b: Boolean => BooleanValueMatcher(b)
      case i: Int => NumberValueMatcher(i)
      case s: Seq[_] => OrValueMatcher(s.map(ValueMatcher(_)).toArray)
      case _ => StringValueMatcher(v.toString)
    }
  }
}
