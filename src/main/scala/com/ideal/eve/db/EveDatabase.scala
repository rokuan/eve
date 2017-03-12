package com.ideal.eve.db

import com.ideal.eve.environment.EveEnvironment
import com.ideal.eve.server.EveSession
import com.ideal.eve.universe.EveUniverse
import com.ideal.evecore.universe.execution.TaskHandler
import com.mongodb.casbah.{MongoCollection, MongoConnection}
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.ArticleType
import com.rokuan.calliopecore.sentence.structure.data.nominal._
import com.rokuan.calliopecore.sentence.structure.data.place.NamedPlaceObject
import com.rokuan.calliopecore.sentence.{IPronoun}
import com.ideal.eve.interpret._
import com.ideal.evecore.interpreter._
import com.rokuan.calliopecore.sentence.structure.data.count.{QuantityObject => _, _}

import scala.util.Try

/**
  * Created by Christophe on 04/10/2015.
  */
object EveDatabase {
  val db = MongoConnection()("eve_")
  val ObjectCollectionName = "eve_data"
  val TypeCollectionName = "eve_types"
  val HistoryCollectionName = "eve_history"

  val IdKey = "_id"

  val EveKey = "__eve"
  val ThisUserKey = "__this"
  val LocationKey = "__location"
  val CityKey = "__city"
  val CountryKey = "__country"
  val DateKey = "__date"

  val ReservedKey = "__idKey"
  val CodeKey = "__code"
  val ValueKey = "__value"
  val TypeKey = "__type"
  val SuperTypesKey = "__superTypes"
  val LevelKey = "__level"
  val UserKey = "__user"
  val StateKey = "__state"

  val ClassKey = "__class"

  def notImplementedYet = throw new RuntimeException("Not implemented yet")
}

class EveEvaluator(implicit val session: EveSession) extends Evaluator {
  import EveDatabase._

  val objectsCollection: MongoCollection = db(ObjectCollectionName)
  val typesCollection: MongoCollection = db(TypeCollectionName)

  override protected val context: Context = EveEnvironment
  override protected val history: History = new EveHistory(db(HistoryCollectionName))
  override protected val taskHandler: TaskHandler = new TaskHandler(EveUniverse)

  private def findMyNameObject(name: NameObject): Try[EveObject] = {
    val pronoun: PronounSubject = new PronounSubject(name.count.possessiveTarget)
    name.count.definition = ArticleType.DEFINITE
    name.setNominalSecondObject(pronoun)
    findNameObject(name)
  }

  private def findQuantityNameObject(name: NameObject) = {
    // TODO:
    notImplementedYet
  }

  /*override def findCharacter(char: CharacterObject): Try[EveObject] = {
    val name = new NameObject() {
      count = char.count
      `object` = new INameInfo {
        override def getValue: String = char.characterType.name().toLowerCase()
        override def getNameTag: String = char.characterType.name().toLowerCase()
      }
    }
    findNameObject(name)
  }

  override def findNamedPlace(place: NamedPlaceObject): Try[EveObject] = {
    // TODO:
    notImplementedYet
  }

  protected def findOneObject(query: MongoDBObject): Try[EveObject] = Try { EveObjectConversions.dbObjectToEveObject(objectsCollection.findOne(query).get) }

  protected def queryObjects(count: CountObject, query: MongoDBObject): EveObject = {
    val results = objectsCollection.find(query)
    val v = count match {
      case l: LimitedItemsObject =>
        val items =
          if(l.range == CountObject.Range.FIRST){
            results.take(l.count.toInt).toSeq
          } else {
            val length = results.length
            Try(results.skip(length - l.count.toInt).toSeq).getOrElse(Seq())
          }
        new EveObjectList(items.map(o => EveObjectConversions.mongoDBObjectToEveObject(o)))
      case a: AllItemsObject => new EveObjectList(results.map(o => EveObjectConversions.mongoDBObjectToEveObject(o)).toSeq)
      case i: IntervalObject => new EveObjectList(results.skip(0)
        .take(results.length)
        .map(o => EveObjectConversions.mongoDBObjectToEveObject(o))
        .toSeq)
      case m: MultipleItemsObject =>
        def gambleElements(lastIndex: Int, indexes: List[Int]): List[MongoDBObject] = {
          indexes match {
            case Nil => Nil
            case head :: tail => results.skip(head - lastIndex).one() :: gambleElements(head, tail)
          }
        }
        new EveObjectList(gambleElements(0, m.items.map(_.toInt).sorted.toList)
          .map(EveObjectConversions.mongoDBObjectToEveObject))
      case f: FixedItemObject =>
        Try {
          val element = results.skip(f.position.toInt - 1).one()
          val mongoObject: MongoDBObject = element
          EveObjectConversions.mongoDBObjectToEveObject(mongoObject)
        }.getOrElse(throw new Exception("No such element at position " + f.position))
      case q: QuantityObject => throw new Exception("Cannot filter items with a QuantityObject")
    }
    results.close
    v
  }

  protected def queryObjectByType(t: String) = {
    val results = objectsCollection.find(MongoDBObject(TypeKey -> t))
    val objects = results.toList
    results.close
    new EveObjectList(objects.map(EveObject.apply(_)))
    /*val objects = results.toStream
    results.close()
    new EveStructuredObjectStream(objects)*/
    // TODO: transformer en stream pour les quantites trop grandes + chercher les types qui extends ?
  }
  protected def findObjectByKey(value: String) = findObjectByAttribute(ReservedKey, value)
  override def findAdditionalDataByCode(value: String) = findObjectByAttribute(CodeKey, value)
  protected def findObjectByAttribute(key: String, value: String): Try[EveObject] = Try { EveObjectConversions.dbObjectToEveObject(objectsCollection.findOne(MongoDBObject(key -> value)).get) }

  protected def getType(o: EveObject): EveType = {
    o match {
      case eso: EveStructuredObject => EveType(eso)
      case EveObjectList(os) => EveType.getCommonSuperType(os.map(getType(_)))
    }
  }

  override def findNameObject(name: NameObject): Try[EveObject] = {
    name.count.definition match {
      case ArticleType.POSSESSIVE => {
        // ma voiture => la voiture de moi
        findMyNameObject(name)
      }
      case ArticleType.DEFINITE => {
        // TODO:
        if(name.getNominalSecondObject == null){
          val query = MongoDBObject(TypeKey -> name.`object`.getNameTag.toLowerCase)
          findOneObject(query)
          // TODO: changer le type de valeurs du Context[MongoDBObject]
          //.getOrElse(new EveStructuredObject(Writer.write(context.findLastNominalObject(query))))
        } else {
          val from = findObject(context, name.getNominalSecondObject)
          from.map {
            case EveStructuredObject(o) => o(name.`object`.getNameTag.toLowerCase)
          }
        }
      }
      case ArticleType.INDEFINITE => notImplementedYet
      case ArticleType.NONE => {
        // TODO: rajouter un type pour les personnes
        val value = name.`object`.getValue
        Try(new EveStringObject(value))
      }
      case ArticleType.DEMONSTRATIVE => notImplementedYet
    }
  }

  /*override def delete(left: INominalObject, field: String, value: INominalObject): Unit = {}

  override def delete(left: INominalObject, value: INominalObject): Unit = {
    findObject(value).map {
      case EveStringObject(field) => delete(left, field)
      case o: EveStructuredObject => delete(left, getType(o).name)
      case EveObjectList(objects) => objects.map(getType(_)).foreach(t => delete(left, t.name))
      /*val commonType = EveType.getCommonSuperType(objects.map(getType(_)))
      delete(context, left, commonType.name)*/
    }
  }

  protected def delete(context: Context[MongoDBObject], left: INominalObject, field: String): Unit = {
    findObject(left).map {
      case eso: EveStructuredObject =>
        TransactionManager.inTransaction { t =>
          val dbObject: MongoDBObject = eso
          dbObject.remove(field)
          t(ObjectCollectionName) += dbObject
        }
    }
  }*/

  override def findPronounSource(pronoun: IPronoun): Try[EveObject] = pronoun.getSource match {
    case IPronoun.PronounSource.I => findObjectByAttribute(UserKey, session.username)
    case IPronoun.PronounSource.YOU => findObjectByKey(EveKey)
    case IPronoun.PronounSource.HE => context.findLastNominalObject(MongoDBObject("gender" -> "male")).flatMap(findObject(context, _)) // TODO:
    case IPronoun.PronounSource.SHE => context.findLastNominalObject(MongoDBObject("gender" -> "female")).flatMap(findObject(context, _)) // TODO:
    case IPronoun.PronounSource.WE =>
      val me = new IPronoun {
        override def getSource: PronounSource = PronounSource.I
        override def getValue: String = ""
      }
      val them = new IPronoun {
        override def getSource: PronounSource = PronounSource.THEY
        override def getValue: String = ""
      }
      for {
        me <- findPronounSource(me)
        them <- findPronounSource(them)
      } yield {
        them match {
          case EveObjectList(os) => EveObjectList(os :+ me)
          case _ => EveObjectList(Seq(me, them))
        }
      }
    case IPronoun.PronounSource.YOU_ =>
      val you = new IPronoun {
        override def getSource: PronounSource = PronounSource.YOU
        override def getValue: String = ""
      }
      val them = new IPronoun {
        override def getSource: PronounSource = PronounSource.THEY
        override def getValue: String = ""
      }
      for {
        you <- findPronounSource(you)
        them <- findPronounSource(them)
      } yield {
        them match {
          case EveObjectList(os) => EveObjectList(os :+ you)
          case _ => EveObjectList(Seq(you, them))
        }
      }
    case _ => notImplementedYet // TODO:
  }*/

  override def getCommonSuperTypes(os: List[EveObject]): String = {
    val commonType = EveType.getCommonSuperType(os.collect { case o: EveStructuredObject => EveType(o.getType()) })
    commonType.name
  }

  override def findNameObject(name: NameObject): Try[EveObject] = notImplementedYet

  override def findCharacter(char: CharacterObject): Try[EveObject] = notImplementedYet

  override def findAdditionalDataByCode(code: String): Try[EveObject] = notImplementedYet

  override def findNamedPlace(place: NamedPlaceObject): Try[EveObject] = notImplementedYet

  override def findPronounSource(pronoun: IPronoun): Try[EveObject] = notImplementedYet
}
