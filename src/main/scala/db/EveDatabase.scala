package db

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
}

class EveDatabase {
  import EveDatabase._

  def findObject(context: Context, src: INominalObject) = {
    // TODO: recupere le 'of' des objets implementant ISecondObject

    src match {
      case abstractTarget: AbstractTarget =>
      case additionalPlace: AdditionalPlace => findAdditionalDataByCode(additionalPlace.place.getCode)
      case char: CharacterObject =>
      case city: CityObject =>
      case color: ColorObject =>
      case name: NameObject =>
      case country: CountryObject =>
      case date: SingleTimeObject =>
      case language: LanguageObject =>
      case location: LocationObject =>
      case namedPlace: NamedPlaceObject =>
      //case number:  =>
      case additionalObject: AdditionalObject => findAdditionalDataByCode(additionalObject.`object`.getCode)
      case additionalPerson: AdditionalPerson => findAdditionalDataByCode(additionalPerson.person.getCode)
      case phoneNumber: PhoneNumberObject =>
      case placeType: PlaceObject =>
      case pronounSubject: PronounSubject =>
        pronounSubject.pronoun.getSource match {
          case IPronoun.PronounSource.I => findObjectByKey(ThisUserKey)
          case IPronoun.PronounSource.YOU => findObjectByKey(EveKey)
          case _ => // TODO:
        }
      case quantity: QuantityObject =>
      case unit: UnitObject =>
      case verbalGroup: VerbalGroup =>
      case _ =>
    }
  }

  def findObjectByKey(value: String) = findObjectByAttribute(ReservedKey, value)

  def findAdditionalDataByCode(value: String) = findObjectByAttribute(CodeKey, value)

  def findObjectByAttribute(key: String, value: String) = {
    // TODO: this.find(key = value)
    println("key=" + key + ", value=" + value)
  }
}
