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

  val UnitKey = "__unit"
  val LanguageKey = "__language"

  val ClassKey = "__class"
}

class EveDatabase {
  import EveDatabase._
  
  val objectsCollection: MongoCollection = db(objectCollectionName)

  def update(context: EveContext, left: INominalObject, value: INominalObject) = {

  }

  def set(context: EveContext, left: INominalObject, field: String, value: INominalObject) = {
    TransactionManager.inTransaction[Unit] {
      transaction => {
        val valueObject = findObject (context, value).get

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
        case city: CityObject => null // TODO: chercher la ville en BD, creer l'objet s'il n'existe pas
        case color: ColorObject => null
        case name: NameObject => findNameObject(context, name)
        case country: CountryObject => null
        case date: SingleTimeObject => Try(new EveTimeObject(date)) // TODO: voir quel type renvoyer (ITimeObject/Date)
        case language: LanguageObject => findLanguage(language)
        case location: LocationObject => null
        case namedPlace: NamedPlaceObject => null
        //case number:  =>
        case additionalObject: AdditionalObject => findAdditionalDataByCode(additionalObject.`object`.getCode)
        case additionalPerson: AdditionalPerson => findAdditionalDataByCode(additionalPerson.person.getCode)
        case phoneNumber: PhoneNumberObject => Try(new EveStructuredObject(MongoDBObject(ValueKey -> phoneNumber.number)))
        case placeType: PlaceObject => null
        case pronounSubject: PronounSubject => resolvePronounSubject(context, pronounSubject)
        case quantity: QuantityObject => Try(new EveStructuredObject(MongoDBObject(ValueKey -> quantity.amount, UnitKey -> quantity.unitType.name())))
        case unit: UnitObject => Try(new EveStructuredObject(MongoDBObject(UnitKey -> unit.unitType.name())))
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

  protected def findLanguage(language: LanguageObject): Try[EveObject] =
    Try {
      val o = objectsCollection.findOne(MongoDBObject(
        ClassKey -> classOf[LanguageObject].getName,
        LanguageKey -> language.language.getLanguageCode)).get
      new EveStructuredObject(o)
    }

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
}
