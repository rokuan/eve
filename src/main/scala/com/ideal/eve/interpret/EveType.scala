package com.ideal.eve.interpret

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._

/**
  * Created by Christophe on 06/12/2015.
  */
object EveType {
  import com.ideal.eve.db.EveDatabase._
  val RootType = new EveType("__any", 0)

  def apply(o: MongoDBObject) = {
    //val name = o.getAs[String]("name").get
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
      val leftCheckedTypes = collection.mutable.Map[String, Boolean]()
      val rightCheckedTypes = collection.mutable.Map[String, Boolean]()
      val leftGroups = collection.mutable.Map[Int, List[EveType]]()
      val rightGroups = collection.mutable.Map[Int, List[EveType]]()
      var commonType: Option[EveType] = Option.empty[EveType]

      def updateTypes(ts: List[EveType], destination: collection.mutable.Map[Int, List[EveType]],
                      checked: collection.mutable.Map[String, Boolean]) = {
        ts.collect {
          case t if !checked.contains(t.name) =>
            destination.get(t.level).map(oldValue => destination.put(t.level, t::oldValue))
              .getOrElse(destination.put(t.level, List(t)))
        }
      }

      def getNewSuperTypes(t: EveType, checked: collection.mutable.Map[String, Boolean]) = {
        checked.put(t.name, true)
        getSuperTypes(t).filter(ty => !checked.contains(ty.name))
      }

      updateTypes(List(t1), leftGroups, leftCheckedTypes)
      updateTypes(List(t2), rightGroups, rightCheckedTypes)

      while(!commonType.isDefined && (!leftGroups.isEmpty && !rightGroups.isEmpty)) {
        val leftKeys = leftGroups.keySet
        val rightKeys = rightGroups.keySet

        if(leftKeys.size >= 0 || rightKeys.size >= 0) {
          val maxLevel = (leftKeys ++ rightKeys).max
          val leftLevelTypes = leftGroups.get(maxLevel)
          val rightLevelTypes = rightGroups.get(maxLevel)

          if (leftLevelTypes.isDefined && rightLevelTypes.isDefined) {
            val leftSuperTypes = leftGroups(maxLevel)
            val rightSuperTypes = rightGroups(maxLevel)
            commonType = leftSuperTypes.intersect(rightSuperTypes).headOption
          }

          if(!commonType.isDefined) {
            leftGroups.remove(maxLevel).map { oldTypes =>
              val newSuperTypes = oldTypes.flatMap(getNewSuperTypes(_, leftCheckedTypes))
              updateTypes(newSuperTypes, leftGroups, leftCheckedTypes)
            }
            rightGroups.remove(maxLevel).map { oldTypes =>
              val newSuperTypes = oldTypes.flatMap(getNewSuperTypes(_, rightCheckedTypes))
              updateTypes(newSuperTypes, rightGroups, rightCheckedTypes)
            }
          }
        }
      }

      commonType
    }
  }

  def getSuperTypes(t: EveType): List[EveType] = {
    val typeCollection = db(TypeCollectionName)
    typeCollection.findOne(MongoDBObject(TypeKey -> t.name))
      .map(_.getAsOrElse[MongoDBList](SuperTypesKey, MongoDBList()))
      .map { l => l.map(o => EveType(o.asInstanceOf[BasicDBObject])) }
      .getOrElse(List())
      .toList
  }
}

case class EveType(val name: String, val level: Int) {
  override def equals(o: Any) = o match {
    case t: EveType => t.name == this.name
    case _ => false
  }
}
