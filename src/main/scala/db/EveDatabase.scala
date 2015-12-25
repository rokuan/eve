package db

import com.mongodb.casbah.{MongoCollection, MongoConnection}
import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.ArticleType
import com.rokuan.calliopecore.sentence.structure.data.nominal._
import com.rokuan.calliopecore.sentence.structure.data.place.{PlaceObject, AdditionalPlace, NamedPlaceObject, LocationObject}
import com.rokuan.calliopecore.sentence.structure.data.time.SingleTimeObject
import com.rokuan.calliopecore.sentence.{INameInfo, IPronoun}
import com.rokuan.calliopecore.sentence.structure.content.INominalObject
import interpret._

import scala.util.Try

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

  val ClassKey = "__class"
}

class EveDatabase {
  import EveDatabase._

  val objectsCollection: MongoCollection = db(objectCollectionName)
  val typesCollection: MongoCollection = db(typeCollectionName)

  def update(context: EveContext, left: INominalObject, value: INominalObject) = {

  }

  def set(context: EveContext, left: INominalObject, field: String, value: INominalObject) = {
    TransactionManager.inTransaction[Unit] {
      transaction => {
        val valueObject = findObject(context, value).get

        findObject(context, left).map { obj: EveObject =>
          obj match {
            case EveStructuredObject(o) => {
              o += (field.toLowerCase -> valueObject.normalize())
              transaction(objectCollectionName) += o
            }
            case EveStructuredObjectList(os) => {
              valueObject match {
                case EveStructuredObjectList(vs) if os.length >= vs.length => {
                  os.zip(vs).foreach(p => {
                    val o = p._1.asInstanceOf[EveStructuredObject].o
                    val v = p._2.normalize()
                    o += (field.toLowerCase -> v)
                    transaction(objectCollectionName) += o
                  })
                }
                case _ => {
                  val v = valueObject.normalize()
                  os.foreach(elem => {
                    val o = elem.asInstanceOf[EveStructuredObject].o
                    o += (field.toLowerCase -> v)
                    transaction(objectCollectionName) += o
                  })
                }
              }
            }
            case _ => {
              // TODO:
            }
          }
        }
      }
    }
  }

  def set(context: EveContext, left: INominalObject, value: INominalObject) = {

  }

  def get() = {}

  def accessObject(obj: INominalObject) = {
    TransactionManager.inTransaction { transaction =>
      val objects = transaction(objectCollectionName)
    }
  }

  def findObject(context: EveContext, src: INominalObject, createIfNeeded: Boolean = false): Try[EveObject] = {
    src match {
      case abstractTarget: AbstractTarget => findAbstractTarget(context, abstractTarget)
      case additionalPlace: AdditionalPlace => findAdditionalDataByCode(additionalPlace.place.getCode)
      case char: CharacterObject => findCharacter(context, char)
      case city: CityObject => findCity(city)
      case color: ColorObject => findColor(color)
      case name: NameObject => findNameObject(context, name)
      case country: CountryObject => findCountry(country)
      case date: SingleTimeObject => Try(new EveTimeObject(date)) // TODO: voir quel type renvoyer (ITimeObject/Date)
      case language: LanguageObject => findLanguage(language)
      case location: LocationObject => null // TODO:
      case namedPlace: NamedPlaceObject => findNamedPlace(namedPlace)
      //case number:  =>
      case additionalObject: AdditionalObject => findAdditionalDataByCode(additionalObject.`object`.getCode)
      case additionalPerson: AdditionalPerson => findAdditionalDataByCode(additionalPerson.person.getCode)
      case phoneNumber: PhoneNumberObject => Try(new EveStructuredObject(Writer.write(phoneNumber)))
      case placeType: PlaceObject => null
      case pronounSubject: PronounSubject => resolvePronounSubject(context, pronounSubject)
      case quantity: QuantityObject => Try(new EveStructuredObject(Writer.write(quantity)))
      case unit: UnitObject => Try(new EveStructuredObject(Writer.write(unit)))
      case verbalGroup: VerbalGroup => null
      case _ => null
    }
  }

  protected def findNameObject(context: EveContext, name: NameObject): Try[EveObject] = {
    Try {
      name.count.definition match {
        case ArticleType.POSSESSIVE => {
          // ma voiture => la voiture de moi
          val pronoun: PronounSubject = new PronounSubject(name.count.possessiveTarget)
          name.count.definition = ArticleType.DEFINITE
          name.setNominalSecondObject(pronoun)
          findNameObject(context, name).get
        }

        case ArticleType.DEFINITE => {
          // TODO:
          if(name.getNominalSecondObject == null){
            val content = context.findLastNominalObject(MongoDBObject(TypeKey -> name.`object`.getNameTag.toLowerCase))
            // TODO: transformer le nominal object en EveObject
            null
          } else {
            val from = findObject(context, name.getNominalSecondObject)
            //from.map(src => src.asInstanceOf[EveStructuredObject].o(name.`object`.getNameTag))
            EveObject(from.get.asInstanceOf[EveStructuredObject].o(name.`object`.getNameTag))
          }
        }

        case ArticleType.INDEFINITE => {
          // TODO:
          null
        }

        case ArticleType.NONE => {
          // TODO: rajouter un type pour les personnes
          val value = name.`object`.getValue
          new EveStringObject(value)
        }
      }
    }
  }

  protected def findCharacter(context: EveContext, char: CharacterObject): Try[EveObject] = {
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
    null
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

  protected def findOneObject(query: MongoDBObject) = Try { new EveStructuredObject(objectsCollection.findOne(query).get) }

  protected def resolvePronounSubject(context: EveContext, pronounSubject: PronounSubject): Try[EveObject] = findPronounSource(context, pronounSubject.pronoun)
  protected def findAbstractTarget(context: EveContext, abstractTarget: AbstractTarget): Try[EveObject] = findPronounSource(context, abstractTarget.source)

  protected def findPronounSource(context: EveContext, pronoun: IPronoun): Try[EveObject] = {
    pronoun.getSource match {
      case IPronoun.PronounSource.I => findObjectByKey(ThisUserKey)
      case IPronoun.PronounSource.YOU => findObjectByKey(EveKey)
      case _ => null // TODO:
    }
  }

  protected def findObjectByKey(value: String) = findObjectByAttribute(ReservedKey, value)
  protected def findAdditionalDataByCode(value: String) = findObjectByAttribute(CodeKey, value)
  protected def findObjectByAttribute(key: String, value: String): Try[EveObject] = Try { new EveStructuredObject(objectsCollection.findOne(MongoDBObject(key -> value)).get) }

  /*protected def getType(o: EveObject): EveType = {
    o match {
      case EveStructuredObject(content) => EveType(content.getAs[MongoDBObject](TypeKey).get)
      case EveStructuredObjectList(os) => getCommonSuperType(os.map(getType(_)))
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
      val result = typesCollection.findOne(MongoDBObject("name" -> t.name)).get.getAs[MongoDBObject]("parent").get
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
}
