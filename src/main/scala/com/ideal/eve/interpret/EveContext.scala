package com.ideal.eve.interpret

import com.google.gson.Gson
import com.ideal.eve.db.EveDatabase
import com.ideal.evecore.interpreter.{EveObjectList, EveStructuredObject, EveObject, Context}
import com.mongodb.casbah.{MongoCollection, MongoDB}
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

object EveDatabaseContext {
  def apply() = new EveDatabaseContext(EveDatabase.db(EveDatabase.ObjectCollectionName))
}

class EveContext(val db: MongoDB) extends Context {
  import EveContext._

  protected val objectCollection = db(ContextDbName)

  def addNominalObject(nominalObject: INominalObject): Unit =
    objectCollection += serializeObject(nominalObject, classOf[INominalObject])

  def addTimeObject(timeObject: ITimeObject): Unit =
    objectCollection += serializeObject(timeObject, classOf[ITimeObject])

  def addPlaceObject(placeObject: IPlaceObject): Unit =
    objectCollection += serializeObject(placeObject, classOf[IPlaceObject])

  protected def serializeObject(obj: AnyRef, objClass: Class[_]): MongoDBObject = {
    val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
    JSON.parse(gson.toJson(obj, objClass)).asInstanceOf[MongoDBObject]
  }

  def findLastNominalObject(query: MongoDBObject): Try[INominalObject] =
    queryWithObjectType(query, "nominal").map(result => deserializeObject(result, classOf[INominalObject]))

  def findLastTimeObject(query: MongoDBObject): Try[ITimeObject] =
    queryWithObjectType(query, "time").map(result => deserializeObject(result, classOf[ITimeObject]))

  def findLastPlaceObject(query: MongoDBObject): Try[IPlaceObject] =
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

  override def findItemsOfType(t: String): Option[EveObject] = None // TODO:
}

class EveDatabaseContext private (val collection: MongoCollection) extends Context {
  override def findItemsOfType(t: String): Option[EveObject] = {
    val results = collection.find(MongoDBObject(EveObject.TypeKey -> t))
    val resultList = results.toList
    results.close()

    if(resultList.isEmpty){
      Option.empty[EveObject]
    } else if(resultList.size == 1){
      resultList.headOption.map(new EveMongoDBObject(_)(collection))
    } else {
      Some(EveObjectList(resultList.map(new EveMongoDBObject(_)(collection))))
    }
  }
}

class EveMongoDBObject(val underlying: MongoDBObject)(val initialCollection: MongoCollection) extends EveStructuredObject {
  import EveObjectConverters._
  import EveObjectConversions._

  override def getType(): String = underlying.as[String](EveObject.TypeKey)

  override def get(field: String): Option[EveObject] = underlying.get(field).map(v => v: EveObject)

  override def getState(state: String): Option[String] = underlying.get("state").collect { case s: String => s }

  override def apply(field: String): EveObject = underlying(field)

  override def set(field: String, value: EveObject): Unit = {
    underlying(field) = eveObjectToMongoDBObject(value)
    initialCollection.save(underlying)
  }

  override def setState(state: String, value: String): Unit = {
    if(!underlying.contains("state")){
      underlying("state") = MongoDBObject(state -> value)
    } else {
      val objectState = underlying.as[MongoDBObject]("state")
      objectState(state) = value
    }
    initialCollection.save(underlying)
  }
}
