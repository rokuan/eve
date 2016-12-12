package com.ideal.eve.db.serialization

import com.mongodb.DBObject

/**
  * Created by Christophe on 11/12/2016.
  */
trait MongoDBReader[T] {
  def read(o: DBObject): T
}

object MongoDBReader {

}