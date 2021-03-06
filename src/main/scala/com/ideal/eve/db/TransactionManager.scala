package com.ideal.eve.db

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


/**
 * Created by Christophe on 17/10/2015.
 */
object TransactionManager {
  def inTransaction[T](process: TransactionManager => T) = {
    val transaction: TransactionManager = new TransactionManager()

    try {
      process(transaction)
      transaction.commit
    } catch {
      case _: RollBackException => transaction.rollBack
      case t: Throwable => t.printStackTrace(); transaction.rollBack; throw t
    }
  }

  def rollback = throw new RollBackException
}

class RollBackException extends Exception

class TransactionManager {
  private val tablesMap = new mutable.HashMap[String, TemporaryTable]()

  private def commit = tablesMap.values.foreach(_.applyChanges)
  private def rollBack = tablesMap.values.foreach(_.revertChanges)

  def apply(tableName: String) = {
    if (!tablesMap.contains(tableName)) {
      tablesMap(tableName) = new TemporaryTable(tableName)
    }
    tablesMap(tableName)
  }
}

class TemporaryTable(val name: String) {
  private val collection = EveDatabase.db(name)
  private val inserts = new ListBuffer[ObjectId]
  private val updates = new mutable.HashMap[ObjectId, MongoDBObject]

  def applyChanges() = {
    inserts.clear()
    updates.clear()
  }

  def revertChanges() = {
    inserts.foreach { oid => collection.remove(MongoDBObject("_id" -> oid)) }
    updates.values.foreach { collection += _ }
    inserts.clear()
    updates.clear()
  }

  def insert(obj: MongoDBObject) = {
    val id = collection.insert(obj).getUpsertedId.asInstanceOf[ObjectId]
    inserts += id
    id
  }

  def update(oid: ObjectId, obj: MongoDBObject) = {
    if(!updates.contains(oid)) {
      collection.findOneByID(oid).map(old => updates += (oid -> old))
    }
    collection.update(MongoDBObject("_id" -> oid), obj)
    oid
  }

  def +=(that: MongoDBObject) = {
    val id: ObjectId =
      if(that.containsField("_id") && collection.findOneByID(that.get("_id")).nonEmpty){
        update(that.get("_id").get.asInstanceOf[ObjectId], that)
      } else {
        insert(that)
      }
    id
  }
}

