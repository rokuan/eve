package com.ideal.eve.db.serialization

import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}

import scala.util.Try

/**
  * Created by Christophe on 13/12/2016.
  */
trait EnumUtils {
  protected def getEnumValue[T <: Enumeration](o: MongoDBObject, key: String)(implicit e: T): e.Value =
    o.getAs[String](key).collect { case v if v != null => e.withName(v) }.getOrElse(null)
  protected def getJavaEnumValue[T <: Enum[T]](o: MongoDBObject, key: String)(implicit c: Class[T]): T =
    o.getAs[String](key).collect { case v if v != null => Enum.valueOf(c, v) }.getOrElse(null.asInstanceOf[T])
  protected def getEnumValues[T <: Enumeration](o: MongoDBObject, key: String)(implicit e: T) =
    o.getAs[MongoDBList](key).map(l => l.map(v => e.withName(v.toString)).toSet).getOrElse(Set())
  protected def getJavaEnumValues[T <: Enum[T]](o: MongoDBObject, key: String)(implicit c: Class[T]): Set[T] =
    o.getAs[MongoDBList](key).map(l => l.map(e => Enum.valueOf(c, e.toString)).toSet).getOrElse(Set())
  protected def javaEnum[T <: Enum[T]](value: String)(implicit c: Class[T]): T = Try[T](Enum.valueOf(c, value))
    .getOrElse(null.asInstanceOf[T])
  protected def javaEnum[T <: Enum[T]](ordinal: Int)(implicit c: Class[T]): T = Try[T](c.getEnumConstants()(ordinal))
    .getOrElse(null.asInstanceOf[T])
  protected def javaEnums[T <: Enum[T]](names: Array[String])(implicit c: Class[T]) = names.map(Enum.valueOf(c, _)).toSet
  protected def scalaEnum[T <: Enumeration](value: String)(implicit e: T) = Try(e.withName(value)).getOrElse(null)
  protected def scalaEnum[T <: Enumeration](ordinal: Int)(implicit e: T) = Try(e(ordinal)).getOrElse(null)
  protected def scalaEnums[T <: Enumeration](values: Array[String])(implicit e: T) = values.map(e.withName(_)).toSet
}
