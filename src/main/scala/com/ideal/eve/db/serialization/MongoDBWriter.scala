package com.ideal.eve.db.serialization

import com.ideal.eve.db.{StateInfo, WordDatabase}
import com.ideal.eve.db.collections.ItemCollection
import com.mongodb.DBObject
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.rokuan.calliopecore.fr.autoroute.sentence._
import com.rokuan.calliopecore.sentence.IValue
import org.bson.types.ObjectId

/**
  * Created by Christophe on 11/12/2016.
  */
trait MongoDBWriter[T] {
  def write(o: T): DBObject
}

object MongoDBWriter {
  private def getObjectId[T <: IValue](o: T, collection: ItemCollection[T]): ObjectId = {
    Option(o).map { v =>
      collection.findId(v.getValue()).getOrElse(collection.insert(v))
    }.getOrElse(null)
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

  implicit object CountryInfoWriter extends MongoDBWriter[CountryInfo] {
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

  abstract class CustomDataWriter[T <: CustomData] extends MongoDBWriter[T] {
    override def write(o: T): DBObject =
      MongoDBObject("value" -> o.getValue(), "code" -> o.getCode())
  }

  implicit object CustomObjectWriter extends CustomDataWriter[CustomObject]
  implicit object CustomPlaceWriter extends CustomDataWriter[CustomPlace]
  implicit object CustomModeWriter extends CustomDataWriter[CustomMode]
  implicit object CustomPersonWriter extends CustomDataWriter[CustomPerson]

  object PrepositionWriter {
    def write[E <: Enum[E], F <: Enum[F]](o: Preposition[E, F]): DBObject = {
      MongoDBObject(
        "value" -> o.getValue(),
        "context" -> o.getContext().name(),
        "followers" -> MongoDBList(o.getFollowers().map(_.name))
      )
    }
  }

  implicit object PlacePrepositionWriter extends MongoDBWriter[PlacePreposition] {
    override def write(o: PlacePreposition): DBObject = PrepositionWriter.write(o)
  }
  implicit object TimePrepositionWriter extends MongoDBWriter[TimePreposition] {
    override def write(o: TimePreposition): DBObject = PrepositionWriter.write(o)
  }
  implicit object WayPrepositionWriter extends MongoDBWriter[WayPreposition] {
    override def write(o: WayPreposition): DBObject = PrepositionWriter.write(o)
  }
  implicit object PurposePrepositionWriter extends MongoDBWriter[PurposePreposition] {
    override def write(o: PurposePreposition): DBObject = PrepositionWriter.write(o)
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

  implicit object StateInfoWriter extends MongoDBWriter[StateInfo] {
    override def write(o: StateInfo): DBObject =
      MongoDBObject(
        "value" -> o.name,
        "state" -> o.state,
        "stateValue" -> o.value
      )
  }
}


