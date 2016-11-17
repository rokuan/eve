package com.ideal.eve.interpret

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._

import scala.annotation.tailrec

/**
  * Created by Christophe on 06/12/2015.
  */
object EveType {
  import com.ideal.eve.db.EveDatabase._
  val RootType = new EveType("__any", 0)

  def apply(o: MongoDBObject) = {
    val name = o.getAs[String](TypeKey).get
    val level = o.getAs[Int](LevelKey).get
    new EveType(name, level)
  }

  def getCommonSuperType(types: Seq[EveType]): EveType = {
    val distinctTypes = types.distinct
    distinctTypes.foldLeft(distinctTypes.headOption) { case (acc, t) => acc.flatMap(getCommonSuperType(_, t)) }
      .getOrElse(EveType.RootType)
  }

  protected def getCommonSuperType(t1: EveType, t2: EveType): Option[EveType] = {
    if(t1.equals(t2)){
      Some(t1)
    } else {
      val leftCheckedTypes = collection.mutable.Set[String]()
      val rightCheckedTypes = collection.mutable.Set[String]()
      val leftGroups = collection.mutable.Map[Int, List[EveType]]()
      val rightGroups = collection.mutable.Map[Int, List[EveType]]()

      def updateTypes(ts: List[EveType], destination: collection.mutable.Map[Int, List[EveType]],
                      checked: collection.mutable.Set[String]) = {
        ts.collect {
          case t if !checked.contains(t.name) =>
            destination.get(t.level).map(oldValue => destination.put(t.level, t::oldValue))
              .getOrElse(destination.put(t.level, List(t)))
        }
      }

      def getNewSuperTypes(t: EveType, checked: collection.mutable.Set[String]) = {
        checked.add(t.name)
        getSuperTypes(t).filter(ty => !checked.contains(ty.name))
      }

      updateTypes(List(t1), leftGroups, leftCheckedTypes)
      updateTypes(List(t2), rightGroups, rightCheckedTypes)

      @tailrec
      def findCommonType(): Option[EveType] = {
        if(leftGroups.isEmpty || rightGroups.isEmpty){
          None
        } else {
          val leftKeys = leftGroups.keySet
          val rightKeys = rightGroups.keySet
          val maxLevel = (leftKeys ++ rightKeys).max

          val commonType =
            for {
              left <- leftGroups.get(maxLevel)
              right <- rightGroups.get(maxLevel)
              intersect <- left.intersect(right).headOption
            } yield {
              intersect
            }
          commonType match {
            case None =>
              leftGroups.remove(maxLevel).map { oldTypes =>
                val newSuperTypes = oldTypes.flatMap(getNewSuperTypes(_, leftCheckedTypes))
                updateTypes(newSuperTypes, leftGroups, leftCheckedTypes)
              }
              rightGroups.remove(maxLevel).map { oldTypes =>
                val newSuperTypes = oldTypes.flatMap(getNewSuperTypes(_, rightCheckedTypes))
                updateTypes(newSuperTypes, rightGroups, rightCheckedTypes)
              }
              findCommonType()
            case _ => commonType
          }
        }
      }

      findCommonType()
    }
  }

  def getSuperTypes(t: EveType): List[EveType] = {
    val typeCollection = db(TypeCollectionName)
    typeCollection.findOne(MongoDBObject(TypeKey -> t.name))
      .map(_.getAsOrElse[MongoDBList](SuperTypesKey, MongoDBList()))
      .map { l => l.map(o => EveType(o.asInstanceOf[BasicDBObject])).toList }
      .getOrElse(List())
  }
}

case class EveType(val name: String, val level: Int) {
  override def equals(o: Any) = o match {
    case t: EveType => t.name == this.name
    case _ => false
  }
}
