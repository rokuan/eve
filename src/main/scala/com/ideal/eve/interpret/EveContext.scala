package com.ideal.eve.interpret

import com.google.gson.Gson
import com.ideal.eve.db.EveDatabase
import com.ideal.evecore.common.Conversions._
import com.ideal.evecore.interpreter.data.{EveQueryObject, EveObject, EveStructuredObject, EveObjectList}
import com.ideal.evecore.interpreter._
import com.mongodb.casbah.{MongoCollection, MongoDB}
import com.mongodb.util.JSON
import com.rokuan.calliopecore.json.FullGsonBuilder
import com.rokuan.calliopecore.sentence.IAction
import com.rokuan.calliopecore.sentence.structure.content.{INominalObject, IPlaceObject, ITimeObject}
import com.mongodb.casbah.query.Imports._

import com.ideal.evecore.util.{ Option => EOpt }

import EObject._

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

class EveContext private (val db: MongoDB) extends Context {
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

  override def findItemsOfType(t: String): EOpt[EveObjectList] = EOpt.empty[EveObjectList] // TODO:
  override def findOneItemOfType(t: String): EOpt[EveStructuredObject] = EOpt.empty[EveStructuredObject] // TODO
}

class EveDatabaseContext private (val collection: MongoCollection) extends Context {
  override def findItemsOfType(t: String): EOpt[EveObjectList] = {
    val results = collection.find(MongoDBObject(EveObject.TYPE_KEY -> t))
    val resultList = results.toList
    results.close()

    if(resultList.isEmpty){
      Option.empty[EveObjectList]
    } else {
      val elements = resultList.map(new EveMongoDBObject(_)(collection))
      Some(new EveObjectList(elements))
    }
  }

  override def findOneItemOfType(t: String): EOpt[EveStructuredObject] = {
    collection.findOne(MongoDBObject(EveObject.TYPE_KEY -> t))
      .map(new EveMongoDBObject(_)(collection))
  }
}

class EveMongoDBObject(val internal: MongoDBObject)(val initialCollection: MongoCollection) extends EveQueryObject with EveStructuredObject {
  import EveObjectConverters._
  import EObject._

  override def getType(): String = internal.as[String](EveObject.TYPE_KEY)

  override def get(field: String): EOpt[EveObject] = internal.get(field).map(v => v: EveObject)

  override def getState(state: String): EOpt[String] = internal.get("state").collect {
    case o: DBObject => o
  }.flatMap(_.getAs[String](state))

  def apply(field: String): EveObject = internal(field)

  override def set(field: String, value: EveObject): Boolean = {
    try {
      internal(field) = eveObjectToMongoDBObject(value)
      initialCollection.save(internal)
      true
    } catch {
      case _: Throwable => false
    }
  }

  override def setState(state: String, value: String): Boolean = {
    try {
      if (!internal.contains("state")) {
        internal("state") = MongoDBObject(state -> value)
      } else {
        val objectState = internal.as[MongoDBObject]("state")
        objectState(state) = value
      }
      initialCollection.save(internal)
      true
    } catch {
      case _: Throwable => false
    }
  }

  override def has(field: String): Boolean = internal.containsField(field)

  override def hasState(state: String): Boolean = internal.getAs[MongoDBObject]("state").map(_.containsField(state)).getOrElse(false)

  override def getId(): String = internal._id.map(_.toString).getOrElse("")

  override def call(iAction: IAction): Boolean = false
}
