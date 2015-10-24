package interpret

import com.mongodb.DBObject
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

sealed trait EveObject

case class EveBooleanObject(b: Boolean) extends EveObject
case class EveNumberObject(n: Number) extends EveObject
case class EveStringObject(s: String) extends EveObject
case class EveTimeObject(t: ITimeObject) extends EveObject
case class EvePlaceObject(p: IPlaceObject) extends EveObject
case class EveStructuredObject(o: DBObject) extends EveObject


