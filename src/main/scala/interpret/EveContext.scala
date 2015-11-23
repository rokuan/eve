package interpret

import com.google.gson.Gson
import com.mongodb.DBObject
import com.mongodb.casbah.MongoDB
import com.mongodb.util.JSON
import com.rokuan.calliopecore.json.FullGsonBuilder
import com.rokuan.calliopecore.sentence.structure.content.{INominalObject, ITimeObject, IPlaceObject}
import com.mongodb.casbah.query.Imports._

/**
 * Created by Christophe on 11/10/2015.
 */

object EveContext {
  private val ContextDbName = "eve_context"

  val CalliopeGroupKey = "calliope_object_type"
}

//class EveContext(val db: MongoDB) extends Context[DBObject] {
class EveContext(val db: MongoDB) extends Context[EveObject, DBObject] {
  import EveContext._

  protected val objectCollection = db(ContextDbName)

  override def addNominalObject(nominalObject: INominalObject): Unit =
    objectCollection += serializeObject(nominalObject, classOf[INominalObject])

  override def addTimeObject(timeObject: ITimeObject): Unit =
    objectCollection += serializeObject(timeObject, classOf[ITimeObject])

  override def addPlaceObject(placeObject: IPlaceObject): Unit =
    objectCollection += serializeObject(placeObject, classOf[IPlaceObject])

  protected def serializeObject(obj: AnyRef, objClass: Class[_]): DBObject = {
    val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
    JSON.parse(gson.toJson(obj, objClass)).asInstanceOf[DBObject]
  }

  override def findLastNominalObject(query: DBObject): INominalObject = {
    val result = queryWithObjectType(query, "nominal")
    deserializeObject(result, classOf[INominalObject])
  }

  override def findLastTimeObject(query: DBObject): ITimeObject = {
    val result = queryWithObjectType(query, "time")
    deserializeObject(result, classOf[ITimeObject])
  }

  override def findLastPlaceObject(query: DBObject): IPlaceObject = {
    val result = queryWithObjectType(query, "place")
    deserializeObject(result, classOf[IPlaceObject])
  }

  protected def queryWithObjectType(initialQuery: DBObject, objectType: String): DBObject = {
    val finalQuery = initialQuery ++ (CalliopeGroupKey $eq objectType)
    val result: DBObject = objectCollection.findOne(finalQuery).get
    result
  }

  protected def deserializeObject[T](obj: DBObject, objClass: Class[T]): T = {
    val gson: Gson = FullGsonBuilder.getDeserializationGsonBuilder.create()
    gson.fromJson(JSON.serialize(obj), objClass)
  }
}
