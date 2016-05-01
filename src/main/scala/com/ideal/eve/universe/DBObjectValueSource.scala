package com.ideal.eve.universe

import com.ideal.eve.interpret._
import com.mongodb.casbah.commons.MongoDBObject

/**
  * Created by Christophe on 23/04/2016.
  */
object DBObjectValueSource {
  def apply(o: MongoDBObject): ObjectValueSource = {
    val pairs = o.keySet.map { k =>
      val v =
        if(o(k) == null){
          NullValueSource
        } else {
          o.getAs[String](k).map(StringValueSource(_))
            .orElse(o.getAs[Number](k).map(NumberValueSource(_)))
            .orElse(o.getAs[Boolean](k).map(BooleanValueSource(_)))
            .orElse(o.getAs[MongoDBObject](k).map(DBObjectValueSource(_)))
            .getOrElse(NullValueSource)
        }
      (k, v)
    }.toMap
    ObjectValueSource(pairs)
  }
}

object EveObjectValueSource {
  def apply(o: EveObject): ValueSource = o match {
    case null => NullValueSource
    case EveBooleanObject(b) => BooleanValueSource(b)
    case EveNumberObject(n) => NumberValueSource(n)
    case EveStringObject(s) => StringValueSource(s)
    case EveStructuredObject(o) => DBObjectValueSource(o)
  }
}
