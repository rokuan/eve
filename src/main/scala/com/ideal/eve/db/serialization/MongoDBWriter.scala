package com.ideal.eve.db.serialization

import com.ideal.eve.db.WordDatabase
import com.ideal.eve.db.collections.ItemCollection
import com.mongodb.DBObject
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.rokuan.calliopecore.fr.autoroute.sentence._
import com.rokuan.calliopecore.sentence.IValue

/**
  * Created by Christophe on 11/12/2016.
  */
trait MongoDBWriter[T] {
  def write(o: T): DBObject
}

object MongoDBWriter {
  private def getObjectId[T <: IValue](o: T, collection: ItemCollection[T]) = {
    Option(o).map { v =>
      collection.findId(v.getValue()).getOrElse(collection.insert(v))
    }.orNull
  }

  implicit object WordInfoWriter extends MongoDBWriter[WordInfo] {
    override def write(o: WordInfo): DBObject =
      MongoDBObject(
        "value" -> o.value,
        "types" -> MongoDBList(o.types.map(_.toString))
      )
  }

  implicit object CityInfoWriter extends MongoDBWriter[CityInfo] {
    override def write(o: CityInfo): DBObject =
      MongoDBObject("value" -> o.value, "latitude" -> o.latitude, "longitude" -> o.longitude)
  }

  implicit object CountryWriter extends MongoDBWriter[CountryInfo] {
    override def write(o: CountryInfo): DBObject =
      MongoDBObject("value" -> o.value, "code" -> o.code)
  }

  implicit object CharacterInfoWriter extends MongoDBWriter[CharacterInfo] {
    override def write(o: CharacterInfo): DBObject =
      MongoDBObject("value" -> o.value, "characterType" -> o.characterType.name())
  }

  implicit object ColorInfoWriter extends MongoDBWriter[ColorInfo] {
    override def write(o: ColorInfo): DBObject =
      MongoDBObject("value" -> o.value, "hexCode" -> o.hexCode)
  }

  implicit object LanguageInfoWriter extends MongoDBWriter[LanguageInfo] {
    override def write(o: LanguageInfo): DBObject =
      MongoDBObject("value" -> o.value, "code" -> o.code)
  }

  implicit object AdjectiveInfoWriter extends MongoDBWriter[AdjectiveInfo] {
    override def write(o: AdjectiveInfo): DBObject =
      MongoDBObject(
        "value" -> o.value,
        "adjectiveType" -> o.adjectiveType.name(),
        "field" -> o.field,
        "state" -> o.state,
        "stateValue" -> o.stateValue
      )
  }

  implicit object FirstNameInfoWriter extends MongoDBWriter[FirstNameInfo] {
    override def write(o: FirstNameInfo): DBObject =
      MongoDBObject("value" -> o.value)
  }

  implicit object NameInfoWriter extends MongoDBWriter[NameInfo] {
    override def write(o: NameInfo): DBObject =
      MongoDBObject("value" -> o.value, "tag" -> o.tag)
  }

  implicit object PlaceInfoWriter extends MongoDBWriter[PlaceInfo] {
    override def write(o: PlaceInfo): DBObject =
      MongoDBObject("value" -> o.value, "placeCategory" -> o.placeCategory.name())
  }

  implicit object UnitInfoWriter extends MongoDBWriter[UnitInfo] {
    override def write(o: UnitInfo): DBObject =
      MongoDBObject("value" -> o.value, "unitType" -> o.unitType.name())
  }

  implicit object TransportInfoWriter extends MongoDBWriter[TransportInfo] {
    override def write(o: TransportInfo): DBObject =
      MongoDBObject("value" -> o.value, "transportType" -> o.transportType)
  }

  implicit object CustomDataWriter extends MongoDBWriter[CustomData] {
    override def write(o: CustomData): DBObject =
      MongoDBObject("value" -> o.getValue(), "code" -> o.getCode())
  }

  implicit object PrepositionWriter extends MongoDBWriter[Preposition[_ <: Enum[_], _]] {
    override def write(o: Preposition[_ <: Enum[_], _]): DBObject =
      MongoDBObject("value" -> o.getValue(), "context" -> o.getContext().name())
  }

  implicit object VerbConjugationWriter extends MongoDBWriter[VerbConjugation] {
    override def write(o: VerbConjugation): DBObject = {
      val verbId = getObjectId(o.getVerb(), WordDatabase.Verbs)

      MongoDBObject(
        "value" -> o.name,
        "pronoun" -> o.pronoun.toString,
        "tense" -> o.tense.toString,
        "form" -> o.form.toString,
        "verb" -> verbId
      )
    }
  }

  implicit object VerbWriter extends MongoDBWriter[Verb] {
    override def write(o: Verb): DBObject = {
      val initialActionId = getObjectId(o.initialAction, WordDatabase.Actions)
      val reflexiveActionId = getObjectId(o.reflexiveAction, WordDatabase.Actions)

      MongoDBObject(
        "value" -> o.infinitive,
        "auxiliary" -> o.auxiliary,
        "initialAction" -> initialActionId,
        "reflexiveAction" -> reflexiveActionId
      )
    }
  }

  implicit object ActionWriter extends MongoDBWriter[Action] {
    override def write(o: Action): DBObject = {
      MongoDBObject(
        "value" -> o.action.name(),
        "field" -> o.field,
        "state" -> o.state,
        "stateValue" -> o.stateValue
      )
    }
  }
}


