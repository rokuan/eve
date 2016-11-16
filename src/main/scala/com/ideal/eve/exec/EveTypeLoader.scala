package com.ideal.eve.exec

import com.ideal.eve.db.EveDatabase
import com.mongodb.casbah.commons.MongoDBObject

import scala.io.Source
import scala.util.Try

import EveDatabase._


object EveTypeLoader {
  def main(args: Array[String]): Unit = {
    val typesCollection = EveDatabase.db(EveDatabase.TypeCollectionName)
    args.headOption.map { fileName =>
      Source.fromFile(fileName).getLines().foreach { line =>
        val parts = line.split(";")
        val parents = Try(parts(1)).map(_.split(",")).getOrElse(Array())
        val superTypes = parents.flatMap(parent => typesCollection.findOne(MongoDBObject(TypeKey -> parent)))
        typesCollection.insert(MongoDBObject(
          TypeKey -> parts(0),
          SuperTypesKey -> superTypes,
          LevelKey -> Try(superTypes.map(_.get(LevelKey).toString.toInt).max + 1).getOrElse(1)
        ))
      }
    }
  }
}
