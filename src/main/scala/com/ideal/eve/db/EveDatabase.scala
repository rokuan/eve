package com.ideal.eve.db

import com.ideal.eve.server.EveSession
import com.mongodb.casbah.{MongoCollection, MongoConnection}
import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.{ArticleType, CountType}
import com.rokuan.calliopecore.sentence.structure.data.nominal._
import com.rokuan.calliopecore.sentence.structure.data.place.{AdditionalPlace, LocationObject, NamedPlaceObject, PlaceObject}
import com.rokuan.calliopecore.sentence.structure.data.time.SingleTimeObject
import com.rokuan.calliopecore.sentence.{INameInfo, IPronoun}
import com.rokuan.calliopecore.sentence.structure.content.{INominalObject, IVerbalObject}
import com.ideal.eve.interpret._
import com.ideal.evecore.interpreter._
import com.ideal.evecore.io.Writer
import EveObjectConverters._

import scala.util.{Failure, Try}

/**
  * Created by Christophe on 04/10/2015.
  */
object EveDatabase {
  val db = MongoConnection()("eve_")
  val objectCollectionName = "eve_data"
  val typeCollectionName = "eve_types"

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
  val UserKey = "__user"
  val StateKey = "__state"

  val ClassKey = "__class"

  def notImplementedYet = throw new RuntimeException("Not implemented yet")
}

class EveDatabase extends Storage[MongoDBObject] {
  import EveDatabase._

  val objectsCollection: MongoCollection = db(objectCollectionName)
  val typesCollection: MongoCollection = db(typeCollectionName)



  def check(context: Context[MongoDBObject], condition: IVerbalObject)(implicit session: EveSession) = {

  }

  private def checkState(context: Context[MongoDBObject], source: INominalObject, key: String, value: String)(implicit session: EveSession): Try[EveObject] = {
    val target = findObject(context, source)

    Try {
      target.map {
        case o: EveStructuredObject => new EveBooleanObject(matchesState(o, key, value))
        case EveObjectList(os) => new EveBooleanObject(os.forall(matchesState(_, key, value)))
        case _ => new EveBooleanObject(false)
      }.getOrElse(new EveBooleanObject(false))
    }
  }

  def updateState(context: Context[MongoDBObject], source: INominalObject, key: String, value: String)(implicit session: EveSession): Unit = {
    val target = findObject(context, source)

    target.map { obj =>
      obj match {
        case _: EveStructuredObject => updateState(obj, key, value)
        case EveObjectList(os) => os.foreach(updateState(_, key, value))
        case _ =>
      }
    }
  }

  private def getState(source: EveObject, key: String)(implicit session: EveSession): Option[String] = {
    source match {
      case EveStructuredObject(o) => Try(o(StateKey).asInstanceOf[EveStructuredObject].o(key).toString).toOption
      case _ => None
    }
  }

  private def matchesState(source: EveObject, key: String, value: String)(implicit session: EveSession): Boolean = getState(source, key).map(value == _).getOrElse(false)

  private def updateState(source: EveObject, key: String, value: String): Unit = {
    source match {
      case o: EveStructuredObject => {
        TransactionManager.inTransaction { transaction =>
          // TODO: appeler le manager en charge de l'objet, qui validera le changement d'etat
          val dbObject: MongoDBObject = o
          createStateIfNotExists(dbObject)
          dbObject(StateKey).asInstanceOf[DBObject](key) = value
          transaction(objectCollectionName) += dbObject
        }
      }
    }
  }

  private def createStateIfNotExists(o: MongoDBObject): Unit = {
    if(o.get(StateKey).isEmpty) {
      o(StateKey) = MongoDBObject()
    }
  }

  def setWithSession(context: Context[MongoDBObject], left: INominalObject, field: String, value: INominalObject)(implicit session: EveSession) = {
    import EveObjectConverters._
    TransactionManager.inTransaction[Unit] {
      transaction => {
        for {
          subject <- findSubject(context, left)
          value <- findObject(context, value)
        } yield {
          (subject, value) match {
            case (o: EveStructuredObject, _) => {
              val dbObject: MongoDBObject = o
              dbObject += (field.toLowerCase -> EveObjectConverters.eveObjectToMongoDBObject(value))
              transaction(objectCollectionName) += dbObject
            }
            case (EveObjectList(os), EveObjectList(vs)) if os.length >= vs.length =>
              os.zip(vs).collect {
                case (so: EveStructuredObject, v) =>
                  val o: MongoDBObject = so
                  o += (field.toLowerCase -> eveObjectToMongoDBObject(v))
                  transaction(objectCollectionName) += o
              }

            case (EveObjectList(os), _) =>
              os.collect {
                case so: EveStructuredObject =>
                  val o: MongoDBObject = so
                  val v = eveObjectToMongoDBObject(value)
                  o += (field.toLowerCase -> v)
                  transaction(objectCollectionName) += o
              }
            case _ => // TODO:
            }
          }
        }
    }
  }

  def set(context: Context[MongoDBObject], left: INominalObject, value: INominalObject) = {

  }

  override def get() = {}

  def accessObject(obj: INominalObject) = {
    TransactionManager.inTransaction { transaction =>
      val objects = transaction(objectCollectionName)
    }
  }

  /*def findSubject(context: Context[MongoDBObject], src: INominalObject)(implicit session: EveSession): Try[EveObject] = {
    src match {
      case pronoun: PronounSubject => findPronounSource(context, pronoun.pronoun)
      case abstractTarget: AbstractTarget => findAbstractTarget(context, abstractTarget)
      case additionalPlace: AdditionalPlace => findAdditionalDataByCode(additionalPlace.place.getCode)
      case additionalObject: AdditionalObject => findAdditionalDataByCode(additionalObject.`object`.getCode)
      case additionalPerson: AdditionalPerson => findAdditionalDataByCode(additionalPerson.person.getCode)
      case char: CharacterObject => findCharacter(context, char)
      case person: PersonObject => Try(new EveStringObject(person.name))
      case city: CityObject => findCity(city)
      case color: ColorObject => findColor(color)
      case name: NameObject => findNameObject(context, name)
      case country: CountryObject => findCountry(country)
      case date: SingleTimeObject => Try(new EveTimeObject(date)) // TODO: voir quel type renvoyer (ITimeObject/Date)
      case language: LanguageObject => findLanguage(language)
      case namedPlace: NamedPlaceObject => findNamedPlace(namedPlace)
      case phoneNumber: PhoneNumberObject => Try(new EveStructuredObject(Writer.write(phoneNumber)))
      case placeType: PlaceObject => notImplementedYet
      case quantity: QuantityObject => Try(new EveStructuredObject(Writer.write(quantity)))
      case unit: UnitObject => Try(new EveStructuredObject(Writer.write(unit)))
      case verbalGroup: VerbalGroup => notImplementedYet
      case _ => notImplementedYet
    }
  }*/

  /*protected def findNameObject(context: Context[MongoDBObject], name: NameObject)(implicit session: EveSession): Try[EveObject] = {
    /*(name.count.getType, name.count.definition) match {
      case (CountType.ALL, _) =>
    }*/
    Try {
      name.count.definition match {
        case ArticleType.POSSESSIVE => {
          // ma voiture => la voiture de moi
          findMyNameObject(context, name)
        }
        case ArticleType.DEFINITE => {
          // TODO:
          if(name.getNominalSecondObject == null){
            val query = MongoDBObject(TypeKey -> name.`object`.getNameTag.toLowerCase)
            findOneObject(query).map(o => o).get
            // TODO: changer le type de valeurs du Context[MongoDBObject]
            //.getOrElse(new EveStructuredObject(Writer.write(context.findLastNominalObject(query))))
          } else {
            val from = findObject(context, name.getNominalSecondObject)
            EveObject(from.get.asInstanceOf[EveStructuredObject].o(name.`object`.getNameTag))
          }
        }
        case ArticleType.INDEFINITE => notImplementedYet
        case ArticleType.NONE => {
          // TODO: rajouter un type pour les personnes
          val value = name.`object`.getValue
          new EveStringObject(value)
        }
        case ArticleType.DEMONSTRATIVE => notImplementedYet
      }
    }
  }*/

  private def findMyNameObject(context: Context[MongoDBObject], name: NameObject)(implicit session: EveSession) = {
    val pronoun: PronounSubject = new PronounSubject(name.count.possessiveTarget)
    name.count.definition = ArticleType.DEFINITE
    name.setNominalSecondObject(pronoun)
    findNameObject(context, name).get
  }

  private def findQuantityNameObject(context: Context[MongoDBObject], name: NameObject)(implicit session: EveSession) = {
    // TODO:
    notImplementedYet
  }

  protected def findCharacter(context: Context[MongoDBObject], char: CharacterObject)(implicit session: EveSession): Try[EveObject] = {
    val name = new NameObject() {
      count = char.count
      `object` = new INameInfo {
        override def getValue: String = char.characterType.name().toLowerCase()
        override def getNameTag: String = char.characterType.name().toLowerCase()
      }
    }
    findNameObject(context, name)
  }

  protected def findNamedPlace(place: NamedPlaceObject): Try[EveObject] = {
    // TODO:
    notImplementedYet
  }

  protected def findLanguage(language: LanguageObject): Try[EveObject] = findOneObject(MongoDBObject(
    ClassKey -> Writer.LanguageObjectType.getName,
    LanguageObjectKey.Code -> language.language.getLanguageCode))

  protected def findColor(color: ColorObject): Try[EveObject] = findOneObject(MongoDBObject(
    ClassKey -> Writer.ColorObjectType.getName,
    ColorObjectKey.Code -> color.color.getColorHexCode))

  protected def findCity(city: CityObject): Try[EveObject] = findOneObject(MongoDBObject(
    ClassKey -> Writer.CityObjectType.getName,
    CityObjectKey.Latitude -> city.city.getLocation.getLatitude,
    CityObjectKey.Longitude -> city.city.getLocation.getLongitude))

  protected def findCountry(country: CountryObject): Try[EveObject] = findOneObject(MongoDBObject(
    ClassKey -> Writer.CountryObjectType.getName,
    CountryObjectKey.Code -> country.country.getCountryCode))

  protected def findOneObject(query: MongoDBObject): Try[EveObject] = Try { EveObjectConversions.mongoDBObjectToEveObject(objectsCollection.findOne(query).get) }

  protected def resolvePronounSubject(context: Context[MongoDBObject], pronounSubject: PronounSubject)(implicit session: EveSession): Try[EveObject] = findPronounSource(context, pronounSubject.pronoun)
  protected def findAbstractTarget(context: Context[MongoDBObject], abstractTarget: AbstractTarget)(implicit session: EveSession): Try[EveObject] = findPronounSource(context, abstractTarget.source)

  /*protected def findPronounSource(context: Context[MongoDBObject], pronoun: IPronoun)(implicit session: EveSession): Try[EveObject] = {
    pronoun.getSource match {
      case IPronoun.PronounSource.I => findObjectByAttribute(UserKey, session.username)
      case IPronoun.PronounSource.YOU => findObjectByKey(EveKey)
      case _ => notImplementedYet // TODO:
    }
  }*/

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
  protected def findAdditionalDataByCode(value: String) = findObjectByAttribute(CodeKey, value)
  protected def findObjectByAttribute(key: String, value: String): Try[EveObject] = Try { EveObjectConversions.mongoDBObjectToEveObject(objectsCollection.findOne(MongoDBObject(key -> value)).get) }

  /*protected def getType(o: EveObject): EveType = {
    o match {
      case EveStructuredObject(content) => EveType(content.getAs[DBObject](TypeKey).get)
      case EveObjectList(os) => getCommonSuperType(os.map(getType(_)))
    }
  }

  protected def getCommonSuperType(types: Seq[EveType]): EveType = {
    val distinctTypes = types.distinct
    if(distinctTypes.length > 0){
      // Should not happen
      EveType.RootType
    } else {
      distinctTypes.foldLeft(distinctTypes(0)) { (t1, t2) => getCommonSuperType(t1, t2) }
    }
  }

  protected def getCommonSuperType(t1: EveType, t2: EveType): EveType = {
    // TODO: find the smallest common type
    def getSuperType(t: EveType) = {
      val result = typesCollection.findOne(MongoDBObject("name" -> t.name)).get.getAs[DBObject]("parent").get
      EveType(result)
    }

    var leftType = t1
    var rightType = t2

    while (!leftType.equals(rightType)) {
      while (leftType.level > rightType.level) {
        leftType = getSuperType(leftType)
      }

      while (rightType.level > leftType.level) {
        rightType = getSuperType(rightType)
      }
    }

    leftType
  }*/
  override def update(context: Context[MongoDBObject], left: INominalObject, value: INominalObject): Unit = {}

  override def set(context: Context[MongoDBObject], left: INominalObject, field: String, value: INominalObject): Unit = {}

  override def findNameObject(context: Context[MongoDBObject], name: NameObject): Try[EveObject] = {
      /*(name.count.getType, name.count.definition) match {
        case (CountType.ALL, _) =>
      }*/
      Try {
        name.count.definition match {
          case ArticleType.POSSESSIVE => {
            // ma voiture => la voiture de moi
            findMyNameObject(context, name)
          }
          case ArticleType.DEFINITE => {
            // TODO:
            if(name.getNominalSecondObject == null){
              val query = MongoDBObject(TypeKey -> name.`object`.getNameTag.toLowerCase)
              findOneObject(query).map(o => o).get
              // TODO: changer le type de valeurs du Context[MongoDBObject]
              //.getOrElse(new EveStructuredObject(Writer.write(context.findLastNominalObject(query))))
            } else {
              val from = findObject(context, name.getNominalSecondObject)
              EveObject(from.get.asInstanceOf[EveStructuredObject].o(name.`object`.getNameTag))
            }
          }
          case ArticleType.INDEFINITE => notImplementedYet
          case ArticleType.NONE => {
            // TODO: rajouter un type pour les personnes
            val value = name.`object`.getValue
            new EveStringObject(value)
          }
          case ArticleType.DEMONSTRATIVE => notImplementedYet
        }
      }
    }

  override def findSubject(context: Context[MongoDBObject], subject: INominalObject): Try[EveObject] = findObject(context, subject)

  override def delete(context: Context[MongoDBObject], left: INominalObject, field: String, value: INominalObject): Unit = {}

  override def delete(context: Context[MongoDBObject], left: INominalObject, value: INominalObject): Unit = {}

  override def findCharacter(context: Context[MongoDBObject], char: CharacterObject): Try[EveObject] = null // TODO:

  override def findLocation(location: LocationObject): Try[EveObject] = null  // TODO:

  override def findPronounSource(context: Context[MongoDBObject], pronoun: IPronoun): Try[EveObject] = pronoun.getSource match {
    case IPronoun.PronounSource.I => findObjectByAttribute(UserKey, session.username)
    case IPronoun.PronounSource.YOU => findObjectByKey(EveKey)
    case _ => notImplementedYet // TODO:
  }
}
