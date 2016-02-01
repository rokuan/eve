package com.ideal.eve.interpret

import com.mongodb.casbah.commons.MongoDBObject

/**
  * Created by Christophe on 06/12/2015.
  */
object EveType {
  val RootType = new EveType("__any", 0)

  def apply(o: MongoDBObject) = {
    val name = o.getAs[String]("name").get
    val level = o.getAs[Int]("level").get
    new EveType(name, level)
  }
}

class EveType(val name: String, val level: Int) {
  def this(name: String) = this(name, Int.MaxValue)

  override def equals(o: Any) = o match {
    case t: EveType => t.name == this.name
    case _ => false
  }
}
