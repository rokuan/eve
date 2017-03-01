package com.ideal.eve.db

import com.ideal.eve.server.EveSession
import com.mongodb.casbah.{MongoCollection, MongoConnection}
import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.ArticleType
import com.rokuan.calliopecore.sentence.structure.data.nominal._
import com.rokuan.calliopecore.sentence.structure.data.place.NamedPlaceObject
import com.rokuan.calliopecore.sentence.{INameInfo, IPronoun}
import com.rokuan.calliopecore.sentence.structure.content.{INominalObject, IVerbalObject}
import com.ideal.eve.interpret._
import com.ideal.evecore.interpreter._
import EveObjectConverters._
import com.rokuan.calliopecore.sentence.IPronoun.PronounSource
import com.rokuan.calliopecore.sentence.structure.data.count.{QuantityObject => _, _}

import scala.util.Try

/**
  * Created by Christophe on 04/10/2015.
  */
object EveDatabase {
  val db = MongoConnection()("eve_")
  val ObjectCollectionName = "eve_data"
  val TypeCollectionName = "eve_types"

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

class EveDatabase(implicit val session: EveSession) {
  import EveDatabase._

  val objectsCollection: MongoCollection = db(ObjectCollectionName)
  val typesCollection: MongoCollection = db(TypeCollectionName)

  def check(context: Context[MongoDBObject], condition: IVerbalObject) = {

  }

  private def checkState(context: Context[MongoDBObject], source: INominalObject, key: String, value: String): Try[EveObject] = {
    val target = findObject(context, source)

    Try {
      target.map {
        case o: EveStructuredObject => new EveBooleanObject(matchesState(o, key, value))
        case EveObjectList(os) => new EveBooleanObject(os.forall(matchesState(_, key, value)))
        case _ => new EveBooleanObject(false)
      }.getOrElse(new EveBooleanObject(false))
    }
  }

  def updateState(context: Context[MongoDBObject], source: INominalObject, key: String, value: String): Unit = {
    val target = findObject(context, source)

    target.map { obj =>
      obj match {
        case _: EveStructuredObject => updateState(obj, key, value)
        case EveObjectList(os) => os.foreach(updateState(_, key, value))
        case _ =>
      }
    }
  }

  private def getState(source: EveObject, key: String): Option[String] = {
    source match {
      case EveStructuredObject(o) => Try(o(StateKey).asInstanceOf[EveStructuredObject].o(key).toString).toOption
      case _ => None
    }
  }

  private def matchesState(source: EveObject, key: String, value: String): Boolean = getState(source, key).map(value == _).getOrElse(false)

  private def updateState(source: EveObject, key: String, value: String): Unit = {
    source match {
      case o: EveStructuredObject => {
        TransactionManager.inTransaction { transaction =>
          // TODO: appeler le manager en charge de l'objet, qui validera le changement d'etat
          val dbObject: MongoDBObject = o
          createStateIfNotExists(dbObject)
          dbObject.getAs[MongoDBObject](StateKey).map(_(key) = value)
          transaction(ObjectCollectionName) += dbObject
        }
      }
    }
  }

  private def createStateIfNotExists(o: MongoDBObject): Unit = {
    if(o.get(StateKey).isEmpty) {
      o(StateKey) = MongoDBObject()
    }
  }

  override def set(context: Context[MongoDBObject], left: INominalObject, field: String, value: INominalObject) = {
    import EveObjectConverters._
    TransactionManager.inTransaction[Unit] { transaction =>
      for {
        subject <- findSubject(context, left)
        value <- findObject(context, value)
      } yield {
        (subject, value) match {
          case (o: EveStructuredObject, _) => {
            val dbObject: MongoDBObject = o
            dbObject += (field.toLowerCase -> EveObjectConverters.eveObjectToMongoDBObject(value))
            transaction(ObjectCollectionName) += dbObject
          }
          case (EveObjectList(os), EveObjectList(vs)) if os.length >= vs.length =>
            os.zip(vs).collect {
              case (so: EveStructuredObject, v) =>
                val o: MongoDBObject = so
                o += (field.toLowerCase -> eveObjectToMongoDBObject(v))
                transaction(ObjectCollectionName) += o
            }

          case (EveObjectList(os), _) =>
            os.collect {
              case so: EveStructuredObject =>
                val o: MongoDBObject = so
                val v = eveObjectToMongoDBObject(value)
                o += (field.toLowerCase -> v)
                transaction(ObjectCollectionName) += o
            }
          case _ => // TODO:
        }
      }
    }
  }

  override def set(context: Context[MongoDBObject], left: INominalObject, value: INominalObject) = {
    for {
      source <- findSubject(context, left)
      member <- findObject(context, value, true)
    } yield {

    }
  }

  private def findMyNameObject(context: Context[MongoDBObject], name: NameObject): Try[EveObject] = {
    val pronoun: PronounSubject = new PronounSubject(name.count.possessiveTarget)
    name.count.definition = ArticleType.DEFINITE
    name.setNominalSecondObject(pronoun)
    findNameObject(context, name)
  }

  private def findQuantityNameObject(context: Context[MongoDBObject], name: NameObject) = {
    // TODO:
    notImplementedYet
  }

  override def findCharacter(context: Context[MongoDBObject], char: CharacterObject): Try[EveObject] = {
    val name = new NameObject() {
      count = char.count
      `object` = new INameInfo {
        override def getValue: String = char.characterType.name().toLowerCase()
        override def getNameTag: String = char.characterType.name().toLowerCase()
      }
    }
    findNameObject(context, name)
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

  override def findNameObject(context: Context[MongoDBObject], name: NameObject): Try[EveObject] = {
    name.count.definition match {
      case ArticleType.POSSESSIVE => {
        // ma voiture => la voiture de moi
        findMyNameObject(context, name)
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

  override def delete(context: Context[MongoDBObject], left: INominalObject, field: String, value: INominalObject): Unit = {}

  override def delete(context: Context[MongoDBObject], left: INominalObject, value: INominalObject): Unit = {
    findObject(context, value).map {
      case EveStringObject(field) => delete(context, left, field)
      case o: EveStructuredObject => delete(context, left, getType(o).name)
      case EveObjectList(objects) => objects.map(getType(_)).foreach(t => delete(context, left, t.name))
      /*val commonType = EveType.getCommonSuperType(objects.map(getType(_)))
      delete(context, left, commonType.name)*/
    }
  }

  protected def delete(context: Context[MongoDBObject], left: INominalObject, field: String): Unit = {
    findObject(context, left).map {
      case eso: EveStructuredObject =>
        TransactionManager.inTransaction { t =>
          val dbObject: MongoDBObject = eso
          dbObject.remove(field)
          t(ObjectCollectionName) += dbObject
        }
    }
  }

  override def findPronounSource(context: Context[MongoDBObject], pronoun: IPronoun): Try[EveObject] = pronoun.getSource match {
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
        me <- findPronounSource(context, me)
        them <- findPronounSource(context, them)
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
        you <- findPronounSource(context, you)
        them <- findPronounSource(context, them)
      } yield {
        them match {
          case EveObjectList(os) => EveObjectList(os :+ you)
          case _ => EveObjectList(Seq(you, them))
        }
      }
    case _ => notImplementedYet // TODO:
  }
}
