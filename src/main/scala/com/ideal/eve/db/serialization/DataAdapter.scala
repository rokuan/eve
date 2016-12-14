package com.ideal.eve.db.serialization

import com.ideal.eve.db.StateInfo
import com.ideal.eve.db.collections.ItemCollection
import com.rokuan.calliopecore.fr.autoroute.sentence.{CustomMode, _}
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

/**
  * Created by Christophe on 13/12/2016.
  */
trait DataAdapter[T] {
  def transform(values: Array[String]): T
}

object DataAdapter extends EnumClassesImports with EnumUtils {
  import com.ideal.eve.db.WordDatabase._
  
  private def getObject[T <: IValue](value: String, collection: ItemCollection[T]) = collection.find(value)

  implicit def stringToBoolean(s: String) = if(s == 0) { false } else { true }

  implicit object WordInfoAdapter extends DataAdapter[WordInfo] {
    override def transform(values: Array[String]): WordInfo =
      WordInfo(values(0), scalaEnums(values(1).split(","))(Word.WordType))
  }

  implicit object ColorInfoAdapter extends DataAdapter[ColorInfo] {
    override def transform(values: Array[String]): ColorInfo =
      ColorInfo(values(0), values(1))
  }
  
  implicit object LanguageInfoAdapter extends DataAdapter[LanguageInfo] {
    override def transform(values: Array[String]): LanguageInfo =
      LanguageInfo(values(0), values(1))
  }

  implicit object CityInfoAdapter extends DataAdapter[CityInfo] {
    override def transform(values: Array[String]): CityInfo = {
      CityInfo(values(2), values(0).toDouble, values(1).toDouble)
    }
  }

  implicit object CountryInfoAdapter extends DataAdapter[CountryInfo] {
    override def transform(values: Array[String]): CountryInfo = {
      CountryInfo(values(4), values(2))
    }
  }

  implicit object CharacterInfoAdapter extends DataAdapter[CharacterInfo] {
    override def transform(values: Array[String]): CharacterInfo = {
      CharacterInfo(values(0), javaEnum[CharacterType](values(1)))
    }
  }

  implicit object AdjectiveInfoAdapter extends DataAdapter[AdjectiveInfo] {
    override def transform(values: Array[String]): AdjectiveInfo = {
      val state = getObject(values(3), States)
      
      AdjectiveInfo(
        values(0),
        javaEnum[AdjectiveValue](values(1)),
        values(2),
        state.map(_.state).orNull,
        state.map(_.value).orNull
      )
    }
  }

  implicit object FirstNameInfoAdapter extends DataAdapter[FirstNameInfo] {
    override def transform(values: Array[String]): FirstNameInfo = {
      FirstNameInfo(values(0))
    }
  }

  implicit object NameInfoAdapter extends DataAdapter[NameInfo] {
    override def transform(values: Array[String]): NameInfo = {
      NameInfo(values(0), values(1))
    }
  }

  implicit object PlaceInfoAdapter extends DataAdapter[PlaceInfo] {
    override def transform(values: Array[String]): PlaceInfo = {
      PlaceInfo(values(0), javaEnum[PlaceCategory](values(1)))
    }
  }

  implicit object UnitInfoAdapter extends DataAdapter[UnitInfo] {
    override def transform(values: Array[String]): UnitInfo = {
      UnitInfo(values(0), javaEnum[UnitType](values(1)))
    }
  }

  implicit object TransportInfoAdapter extends DataAdapter[TransportInfo] {
    override def transform(values: Array[String]): TransportInfo = {
      TransportInfo(values(0), javaEnum[TransportType](values(1)))
    }
  }

  abstract class CustomDataAdapter[T <: CustomData] extends DataAdapter[T] {
    def build: (String, String) => T

    def transform(values: Array[String]): T = {
      build(values(0), values(1))
    }
  }

  implicit object CustomObjectAdapter extends CustomDataAdapter[CustomObject] {
    override def build: (String, String) => CustomObject = CustomObject
  }

  implicit object CustomPersonAdapter extends CustomDataAdapter[CustomPerson] {
    override def build: (String, String) => CustomPerson = CustomPerson
  }

  implicit object CustomPlaceAdapter extends CustomDataAdapter[CustomPlace] {
    override def build: (String, String) => CustomPlace = CustomPlace
  }

  implicit object CustomModeAdapter extends CustomDataAdapter[CustomMode] {
    override def build: (String, String) => CustomMode = CustomMode
  }

  abstract class PrepositionAdapter[E <: Enum[E], F <: Enum[F], T <: Preposition[E, F]](implicit ce: Class[E], cf: Class[F])
    extends DataAdapter[T] {
    def build: (String, E, Set[F]) => T

    override def transform(values: Array[String]): T = {
      build(values(0),
        javaEnum[E](values(1)),
        javaEnums[F](values(2).split(",")))
    }
  }

  implicit object PlacePrepositionAdapter extends PrepositionAdapter[PlaceContext, PlaceType, PlacePreposition] {
    override def build: (String, PlaceContext, Set[PlaceType]) => PlacePreposition = PlacePreposition
  }

  implicit object TimePrepositionAdapter extends PrepositionAdapter[TimeContext, TimeType, TimePreposition] {
    override def build: (String, TimeContext, Set[TimeType]) => TimePreposition = TimePreposition
  }

  implicit object WayPrepositionAdapter extends PrepositionAdapter[WayContext, WayType, WayPreposition] {
    override def build: (String, WayContext, Set[WayType]) => WayPreposition = WayPreposition
  }

  implicit object PurposePrepositionAdapter extends PrepositionAdapter[PurposeContext, PurposeType, PurposePreposition] {
    override def build: (String, PurposeContext, Set[PurposeType]) => PurposePreposition = PurposePreposition
  }

  implicit object VerbConjugationAdapter extends DataAdapter[VerbConjugation] {
    override def transform(values: Array[String]): VerbConjugation = {
      VerbConjugation(
        values(1),
        getObject(values(0), Verbs).getOrElse(null),
        scalaEnum(values(3))(Verb.ConjugationTense),
        javaEnum[IAction.Form](values(2)),
        scalaEnum(values(4).toInt)(Verb.Pronoun)
      )
    }
  }

  implicit object VerbAdapter extends DataAdapter[Verb] {
    override def transform(values: Array[String]): Verb = {
      Verb(
        values(0),
        values(0) match {
          case "être" | "avoir" => true
          case _ => false
        },
        getObject(values(1), Actions).orNull,
        getObject(values(2), Actions).orNull
      )
    }
  }

  implicit object ActionAdapter extends DataAdapter[Action] {
    override def transform(values: Array[String]): Action = {
      val state = getObject(values(2), States)
      Action(
        javaEnum[ActionType](values(0)),
        values(1),
        state.map(_.state).orNull,
        state.map(_.value).orNull
      )
    }
  }

  implicit object StateInfoAdapter extends DataAdapter[StateInfo] {
    override def transform(values: Array[String]): StateInfo =
      StateInfo(values(0), values(1), values(2))
  }
}
