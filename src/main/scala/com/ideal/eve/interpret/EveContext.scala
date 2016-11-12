package com.ideal.eve.interpret

import com.google.gson.Gson
import com.ideal.eve.db.EveDatabase
import com.ideal.eve.server.EveSession
import com.ideal.evecore.interpreter.Context
import com.mongodb.casbah.MongoDB
import com.mongodb.util.JSON
import com.rokuan.calliopecore.json.FullGsonBuilder
import com.rokuan.calliopecore.sentence.structure.content.{INominalObject, IPlaceObject, ITimeObject}
import com.mongodb.casbah.query.Imports._

import scala.util.Try

/**
 * Created by Christophe on 11/10/2015.
 */

object EveContext {
  private val ContextDbName = "eve_context"

  val CalliopeGroupKey = "calliope_object_type"

  def apply() = new EveContext(EveDatabase.db)
}

class EveContext(val db: MongoDB) extends Context[MongoDBObject] {
  import EveContext._

  protected val objectCollection = db(ContextDbName)

  override def addNominalObject(nominalObject: INominalObject): Unit =
    objectCollection += serializeObject(nominalObject, classOf[INominalObject])

  override def addTimeObject(timeObject: ITimeObject): Unit =
    objectCollection += serializeObject(timeObject, classOf[ITimeObject])

  override def addPlaceObject(placeObject: IPlaceObject): Unit =
    objectCollection += serializeObject(placeObject, classOf[IPlaceObject])

  protected def serializeObject(obj: AnyRef, objClass: Class[_]): MongoDBObject = {
    val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
    JSON.parse(gson.toJson(obj, objClass)).asInstanceOf[MongoDBObject]
  }

  override def findLastNominalObject(query: MongoDBObject): Try[INominalObject] =
    queryWithObjectType(query, "nominal").map(result => deserializeObject(result, classOf[INominalObject]))

  override def findLastTimeObject(query: MongoDBObject): Try[ITimeObject] =
    queryWithObjectType(query, "time").map(result => deserializeObject(result, classOf[ITimeObject]))

  override def findLastPlaceObject(query: MongoDBObject): Try[IPlaceObject] =
    queryWithObjectType(query, "place").map(result => deserializeObject(result, classOf[IPlaceObject]))

  protected def queryWithObjectType(initialQuery: MongoDBObject, objectType: String): Try[MongoDBObject] = {
    Try {
      val finalQuery = initialQuery ++ (CalliopeGroupKey $eq objectType)
      objectCollection.findOne(finalQuery).getOrElse(throw new RuntimeException("No such object(s)")) // TODO: remplacer par la bonne exception
    }
  }

  protected def deserializeObject[T](obj: MongoDBObject, objClass: Class[T]): T = {
    val gson: Gson = FullGsonBuilder.getDeserializationGsonBuilder.create()
    gson.fromJson(JSON.serialize(obj), objClass)
  }
}
