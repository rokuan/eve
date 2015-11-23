package interpret

import java.util.Date

import com.google.gson.Gson
import com.mongodb.DBObject
import com.mongodb.util.JSON
import com.rokuan.calliopecore.json.FullGsonBuilder
import com.rokuan.calliopecore.sentence.structure.content.{ITimeObject, IPlaceObject}

/**
 * Created by Christophe on 27/09/2015.
 */

object EveObject {
  val NumberResultType = classOf[EveNumberObject]
  val StringResultType = classOf[EveStringObject]
  val BooleanResultType = classOf[EveBooleanObject]
  val ObjectResultType = classOf[EveStructuredObject]
  val DateResultType = classOf[EveTimeObject]
  val PlaceResultType = classOf[EvePlaceObject]
}

sealed abstract class EveObject[T](val value: T) {
  def normalize(): AnyRef
}

case class EveBooleanObject(b: Boolean) extends EveObject(b) {
  override def normalize() = b
}
case class EveNumberObject(n: Number) extends EveObject(n) {
  override def normalize() = n
}
case class EveStringObject(s: String) extends EveObject(s) {
  override def normalize() = s
}
case class EveDateObject(d: Date) extends EveObject(d) {
  override def normalize() = d
}
case class EveTimeObject(t: ITimeObject) extends EveObject(t) {
  override def normalize() = {
    val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
    JSON.parse(gson.toJson(t, classOf[ITimeObject])).asInstanceOf[DBObject]
  }
}
case class EvePlaceObject(p: IPlaceObject) extends EveObject(p) {
  override def normalize() = {
    val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
    JSON.parse(gson.toJson(p, classOf[IPlaceObject])).asInstanceOf[DBObject]
  }
}
case class EveStructuredObject(o: DBObject) extends EveObject(o) {
  override def normalize() = o
}
case class EveStructuredObjectList(a: List[EveObject]) extends EveObject(a) {
  override def normalize() = a.map(_.normalize())
}


