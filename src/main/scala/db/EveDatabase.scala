package db

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import com.rokuan.calliopecore.sentence.structure.data.count.CountObject.ArticleType
import com.rokuan.calliopecore.sentence.structure.data.nominal._
import com.rokuan.calliopecore.sentence.structure.data.place.{PlaceObject, AdditionalPlace, NamedPlaceObject, LocationObject}
import com.rokuan.calliopecore.sentence.structure.data.time.SingleTimeObject
import com.rokuan.calliopecore.sentence.IPronoun
import com.rokuan.calliopecore.sentence.structure.content.INominalObject
import interpret.Context

/**
 * Created by Christophe on 04/10/2015.
 */
object EveDatabase {
  val EveKey = "__eve"
  val ThisUserKey = "__this"
  val LocationKey = "__location"
  val CityKey = "__city"
  val CountryKey = "__country"
  val DateKey = "__date"

  val ReservedKey = "__idKey"
  val CodeKey = "__code"
  val ValueKey = "__value"

  val UnitKey = "__unit"
  val LanguageKey = "__language"
}

class EveDatabase {
  import EveDatabase._

  def set() = {}
  def get() = {}

  def findObject(context: Context, src: INominalObject): DBObject = {
    src match {
      case abstractTarget: AbstractTarget => findAbstractTarget(context, abstractTarget)
      case additionalPlace: AdditionalPlace => findAdditionalDataByCode(additionalPlace.place.getCode)
      case char: CharacterObject =>
      case city: CityObject =>
      case color: ColorObject =>
      case name: NameObject => findNameObject(context, name)
      case country: CountryObject =>
      case date: SingleTimeObject => MongoDBObject(ValueKey -> date.date)
      case language: LanguageObject => MongoDBObject()
      case location: LocationObject =>
      case namedPlace: NamedPlaceObject =>
      //case number:  =>
      case additionalObject: AdditionalObject => findAdditionalDataByCode(additionalObject.`object`.getCode)
      case additionalPerson: AdditionalPerson => findAdditionalDataByCode(additionalPerson.person.getCode)
      case phoneNumber: PhoneNumberObject => MongoDBObject(ValueKey -> phoneNumber.number)
      case placeType: PlaceObject =>
      case pronounSubject: PronounSubject => resolvePronounSubject(context, pronounSubject)
      case quantity: QuantityObject => MongoDBObject(ValueKey -> quantity.amount, UnitKey -> quantity.unitType.name())
      case unit: UnitObject => MongoDBObject(UnitKey -> unit.unitType.name())
      case verbalGroup: VerbalGroup =>
      case _ =>
    }

    null
  }

  protected def findNameObject(context: Context, name: NameObject) = {
    name.count.definition match {
      case ArticleType.POSSESSIVE => {
        // ma voiture => la voiture de moi
        val pronoun: PronounSubject = new PronounSubject(name.count.possessiveTarget)
        name.count.definition = ArticleType.DEFINITE
        name.setNominalSecondObject(pronoun)
        findObject(context, name)
      }
    }
  }

  protected def resolvePronounSubject(context: Context, pronounSubject: PronounSubject) = {
    pronounSubject.pronoun.getSource match {
      case IPronoun.PronounSource.I => findObjectByKey(ThisUserKey)
      case IPronoun.PronounSource.YOU => findObjectByKey(EveKey)
      case _ => // TODO:
    }
  }

  protected def findAbstractTarget(context: Context, abstractTarget: AbstractTarget) = {

  }

  protected def findObjectByKey(value: String) = findObjectByAttribute(ReservedKey, value)

  protected def findAdditionalDataByCode(value: String) = findObjectByAttribute(CodeKey, value)

  protected def findObjectByAttribute(key: String, value: String) = {
    // TODO: this.find(key = value)
    println("key=" + key + ", value=" + value)
    null
  }
}
