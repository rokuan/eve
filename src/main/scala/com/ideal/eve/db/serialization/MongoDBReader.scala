package com.ideal.eve.db.serialization

import com.ideal.eve.db.StateInfo
import com.ideal.eve.db.collections.ItemCollection
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import com.rokuan.calliopecore.fr.autoroute.sentence.{CustomData, Preposition, _}
import com.rokuan.calliopecore.sentence.IAction.ActionType
import com.rokuan.calliopecore.sentence.IAdjectiveInfo.AdjectiveValue
import com.rokuan.calliopecore.sentence.{IAction, IValue}
import com.rokuan.calliopecore.sentence.structure.data.nominal.CharacterObject.CharacterType
import com.rokuan.calliopecore.sentence.structure.data.nominal.UnitObject.UnitType
import com.rokuan.calliopecore.sentence.structure.data.place.PlaceAdverbial.{PlaceContext, PlaceType}
import com.rokuan.calliopecore.sentence.structure.data.place.PlaceObject.PlaceCategory
import com.rokuan.calliopecore.sentence.structure.data.purpose.PurposeAdverbial.{PurposeContext, PurposeType}
import com.rokuan.calliopecore.sentence.structure.data.time.TimeAdverbial.{TimeContext, TimeType}
import com.rokuan.calliopecore.sentence.structure.data.way.TransportObject.TransportType
import com.rokuan.calliopecore.sentence.structure.data.way.WayAdverbial.{WayContext, WayType}
import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._

/**
 * Created by Christophe on 11/12/2016.
 */
trait MongoDBReader[T] {
  def read(o: DBObject): T
}

object MongoDBReader extends EnumClassesImports with EnumUtils {
  import com.ideal.eve.db.WordDatabase._

  protected def getObject[T <: IValue](id: Option[ObjectId], collection: ItemCollection[T]): T =
    id.map(collection.getById).getOrElse(null.asInstanceOf[T])

  implicit object WordInfoReader extends MongoDBReader[WordInfo] {
    override def read(o: DBObject) = {
      val obj: MongoDBObject = o
      WordInfo(obj.getAsOrElse("value", ""), getEnumValues(obj, "types")(Word.WordType))
    }
  }

  implicit object CityInfoReader extends MongoDBReader[CityInfo] {
    override def read(o: DBObject): CityInfo = {
      val obj: MongoDBObject = o
      CityInfo(obj.getAsOrElse("value", ""), obj.getAsOrElse("latitude", 0.0), obj.getAsOrElse("longitude", 0.0))
    }
  }

  implicit object CountryReader extends MongoDBReader[CountryInfo] {
    override def read(o: DBObject): CountryInfo = {
      val obj: MongoDBObject = o
      CountryInfo(obj.getAsOrElse("value", ""), obj.getAsOrElse("code", ""))
    }
  }

  implicit object CharacterInfoReader extends MongoDBReader[CharacterInfo] {
    override def read(o: DBObject): CharacterInfo = {
      val obj: MongoDBObject = o
      CharacterInfo(obj.getAsOrElse("value", ""), getJavaEnumValue[CharacterType](obj, "characterType"))
    }
  }

  implicit object ColorInfoReader extends MongoDBReader[ColorInfo] {
    override def read(o: DBObject): ColorInfo = {
      val obj: MongoDBObject = o
      ColorInfo(obj.getAsOrElse("value", ""), obj.getAsOrElse("hexCode", ""))
    }
  }

  implicit object LanguageInfoReader extends MongoDBReader[LanguageInfo] {
    override def read(o: DBObject): LanguageInfo = {
      val obj: MongoDBObject = o
      LanguageInfo(obj.getAsOrElse("value", ""), obj.getAsOrElse("code", ""))
    }
  }

  implicit object AdjectiveInfoReader extends MongoDBReader[AdjectiveInfo] {
    override def read(o: DBObject): AdjectiveInfo = {
      val obj: MongoDBObject = o
      AdjectiveInfo(
        obj.getAsOrElse("value", ""),
        getJavaEnumValue[AdjectiveValue](obj, "adjectiveType"),
        obj.getAsOrElse[String]("field", null),
        obj.getAsOrElse[String]("state", null),
        obj.getAsOrElse[String]("stateValue", null)
      )
    }
  }

  implicit object FirstNameInfoReader extends MongoDBReader[FirstNameInfo] {
    override def read(o: DBObject): FirstNameInfo = {
      val obj: MongoDBObject = o
      FirstNameInfo(obj.getAsOrElse("value", ""))
    }
  }

  implicit object NameInfoReader extends MongoDBReader[NameInfo] {
    override def read(o: DBObject): NameInfo = {
      val obj: MongoDBObject = o
      NameInfo(obj.getAsOrElse("value", ""), obj.getAsOrElse("tag", ""))
    }
  }

  implicit object PlaceInfoReader extends MongoDBReader[PlaceInfo] {
    override def read(o: DBObject): PlaceInfo = {
      val obj: MongoDBObject = o
      PlaceInfo(obj.getAsOrElse("value", ""), getJavaEnumValue[PlaceCategory](obj, "placeCategory"))
    }
  }

  implicit object UnitInfoReader extends MongoDBReader[UnitInfo] {
    override def read(o: DBObject): UnitInfo = {
      val obj: MongoDBObject = o
      UnitInfo(obj.getAsOrElse("value", ""), getJavaEnumValue[UnitType](obj, "unitType"))
    }
  }

  implicit object TransportInfoReader extends MongoDBReader[TransportInfo] {
    override def read(o: DBObject): TransportInfo = {
      val obj: MongoDBObject = o
      TransportInfo(obj.getAsOrElse("value", ""), getJavaEnumValue[TransportType](obj, "transportType"))
    }
  }

  abstract class CustomDataReader[T <: CustomData] extends MongoDBReader[T] {
    def build: (String, String) => T

    def read(o: DBObject): T = {
      val obj: MongoDBObject = o
      build(obj.getAsOrElse("value", ""), obj.getAsOrElse("code", ""))
    }
  }

  implicit object CustomObjectReader extends CustomDataReader[CustomObject] {
    override def build: (String, String) => CustomObject = CustomObject
  }

  implicit object CustomPersonReader extends CustomDataReader[CustomPerson] {
    override def build: (String, String) => CustomPerson = CustomPerson
  }

  implicit object CustomPlaceReader extends CustomDataReader[CustomPlace] {
    override def build: (String, String) => CustomPlace = CustomPlace
  }

  implicit object CustomModeReader extends CustomDataReader[CustomMode] {
    override def build: (String, String) => CustomMode = CustomMode
  }

  abstract class PrepositionReader[E <: Enum[E], F <: Enum[F], T <: Preposition[E, F]](implicit ce: Class[E], cf: Class[F])
    extends MongoDBReader[T] {
    def build: (String, E, Set[F]) => T

    override def read(o: DBObject): T = {
      val obj: MongoDBObject = o
      build(obj.getAsOrElse("value", ""),
        getJavaEnumValue[E](obj, "context"),
        getJavaEnumValues[F](obj, "followers"))
    }
  }

  implicit object PlacePrepositionReader extends PrepositionReader[PlaceContext, PlaceType, PlacePreposition] {
    override def build: (String, PlaceContext, Set[PlaceType]) => PlacePreposition = PlacePreposition
  }

  implicit object TimePrepositionReader extends PrepositionReader[TimeContext, TimeType, TimePreposition] {
    override def build: (String, TimeContext, Set[TimeType]) => TimePreposition = TimePreposition
  }

  implicit object WayPrepositionReader extends PrepositionReader[WayContext, WayType, WayPreposition] {
    override def build: (String, WayContext, Set[WayType]) => WayPreposition = WayPreposition
  }

  implicit object PurposePrepositionReader extends PrepositionReader[PurposeContext, PurposeType, PurposePreposition] {
    override def build: (String, PurposeContext, Set[PurposeType]) => PurposePreposition = PurposePreposition
  }

  implicit object VerbConjugationReader extends MongoDBReader[VerbConjugation] {
    override def read(o: DBObject): VerbConjugation = {
      val obj: MongoDBObject = o
      VerbConjugation(
        obj.getAsOrElse("value", ""),
        getObject(obj.getAs[ObjectId]("verb"), Verbs),
        getEnumValue(obj, "tense")(Verb.ConjugationTense),
        getJavaEnumValue[IAction.Form](obj, "form"),
        getEnumValue(obj, "pronoun")(Verb.Pronoun)
      )
    }
  }

  implicit object VerbReader extends MongoDBReader[Verb] {
    override def read(o: DBObject): Verb = {
      val obj: MongoDBObject = o
      Verb(
        obj.getAsOrElse("value", ""),
        obj.getAsOrElse("auxiliary", false),
        getObject(obj.getAs[ObjectId]("initialAction"), Actions),
        getObject(obj.getAs[ObjectId]("reflexiveAction"), Actions)
      )
    }
  }

  implicit object ActionReader extends MongoDBReader[Action] {
    override def read(o: DBObject): Action = {
      val obj: MongoDBObject = o
      Action(
        getJavaEnumValue[ActionType](obj, "value"),
        obj.getAsOrElse[String]("field", null),
        obj.getAsOrElse[String]("state", null),
        obj.getAsOrElse[String]("stateValue", null),
        obj.getAsOrElse[Boolean]("target", false)
      )
    }
  }

  implicit object StateInfoReader extends MongoDBReader[StateInfo] {
    override def read(o: DBObject): StateInfo = {
      val obj: MongoDBObject = o
      StateInfo(
        obj.getAsOrElse("value", ""),
        obj.getAsOrElse("state", ""),
        obj.getAsOrElse("stateValue", "false")
      )
    }
  }
}