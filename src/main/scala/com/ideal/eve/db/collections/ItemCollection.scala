package com.ideal.eve.db.collections

import com.ideal.eve.db.WordDatabase
import com.ideal.eve.db.serialization.{MongoDBReader, MongoDBWriter}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.rokuan.calliopecore.sentence.IValue

/**
  * Created by Christophe on 11/12/2016.
  */
class ItemCollection[T <: IValue](val collectionName: String)(implicit writer: MongoDBWriter[T], reader: MongoDBReader[T]) {
  val underlying = {
    val mainDB = WordDatabase.CalliopeDB
    if(!mainDB.collectionExists(collectionName)){
      mainDB.createCollection(collectionName, MongoDBObject())
      mainDB(collectionName).createIndex(MongoDBObject("value" -> 1), MongoDBObject("unique" -> true))
    }
    mainDB(collectionName)
  }

  def insert(o: T) = underlying.insert(writer.write(o)).getUpsertedId.asInstanceOf[ObjectId]
  def update(o: T) = underlying.update(MongoDBObject("value" -> o.getValue), writer.write(o), true)
  def findId(value: String): Option[ObjectId] = findByValue(value).map(_._1)
  def findOneStartingWith(value: String) = findByValue(("^" + value).r)
  def find(value: String): Option[T] = findByValue(value).map(_._2)
  def get(value: String): T = find(value).getOrElse(null.asInstanceOf[T])
  protected def findByValue(value: Any): Option[(ObjectId, T)] = underlying.findOne(MongoDBObject("value" -> value)).map(r => (r._id.get, reader.read(r)))
  def findById(id: ObjectId): Option[T] = underlying.findOneByID(id).map(reader.read)
  def getById(id: ObjectId): T = findById(id).getOrElse(null.asInstanceOf[T])
}
