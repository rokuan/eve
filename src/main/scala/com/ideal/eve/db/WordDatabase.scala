package com.ideal.eve.db

import com.ideal.eve.db.collections.ItemCollection
import com.ideal.eve.db.serialization.{MongoDBReader, MongoDBWriter}
import com.mongodb.casbah.MongoConnection
import com.rokuan.calliopecore.fr.autoroute.parser.WordStorage
import com.rokuan.calliopecore.fr.autoroute.sentence.{CustomPerson, _}

/**
  * Created by Christophe on 10/12/2016.
  */
class WordDatabase extends WordStorage {
  import WordDatabase._

  override def wordStartsWith(q: String): Boolean = databases.exists(_.findOneStartingWith(q).isDefined)
  override def findUnitInfo(q: String): UnitInfo = Units.find(q)
  override def findCityInfo(q: String): CityInfo = Cities.find(q)
  override def findPlaceInfo(q: String): PlaceInfo = Places.find(q)
  override def findLanguageInfo(q: String): LanguageInfo = Languages.find(q)
  override def findCharacterInfo(q: String): CharacterInfo = Characters.find(q)
  override def findColorInfo(q: String): ColorInfo = Colors.find(q)
  override def findTransportInfo(q: String): TransportInfo = Transports.find(q)
  override def findConjugation(q: String): VerbConjugation = Conjugations.find(q)
  override def findNameInfo(q: String): NameInfo = Names.find(q)
  override def findFirstnameInfo(q: String): FirstNameInfo = FirstNames.find(q)
  override def findWordInfo(q: String): WordInfo = Words.find(q)
  override def findAdjectiveInfo(q: String): AdjectiveInfo = Adjectives.find(q)

  override def findTimePreposition(q: String): TimePreposition = TimePrepositions.find(q)
  override def findPlacePreposition(q: String): PlacePreposition = PlacePrepositions.find(q)
  override def findWayPreposition(q: String): WayPreposition = WayPrepositions.find(q)
  override def findPurposePreposition(q: String): PurposePreposition = PurposePrepositions.find(q)

  override def findCustomMode(q: String): CustomMode = CustomModes.find(q)
  override def findCountryInfo(q: String): CountryInfo = Countries.find(q)
  override def findCustomPerson(q: String): CustomPerson = CustomPeople.find(q)
  override def findCustomPlace(q: String): CustomPlace = CustomPlaces.find(q)
  override def findCustomObject(q: String): CustomObject = CustomObjects.find(q)
}

object WordDatabase {
  import MongoDBWriter._
  import MongoDBReader._

  val CalliopeDB = MongoConnection()("calliope_")
  val Colors = new ItemCollection[ColorInfo]("colors")
  val Cities = new ItemCollection[CityInfo]("cities")
  val Countries = new ItemCollection[CountryInfo]("countries")
  val Languages = new ItemCollection[LanguageInfo]("languages")
  val Adjectives = new ItemCollection[AdjectiveInfo]("adjectives")
  val Conjugations = new ItemCollection[VerbConjugation]("conjugations")
  val Verbs = new ItemCollection[Verb]("verbs")
  val Actions = new ItemCollection[Action]("actions")
  val Units = new ItemCollection[UnitInfo]("units")
  val Transports = new ItemCollection[TransportInfo]("transports")
  val Characters = new ItemCollection[CharacterInfo]("characters")
  val Words = new ItemCollection[WordInfo]("words")
  val FirstNames = new ItemCollection[FirstNameInfo]("firstnames")
  val Names = new ItemCollection[NameInfo]("names")
  val Places = new ItemCollection[PlaceInfo]("places")
  val CustomModes = new ItemCollection[CustomMode]("custom_modes")
  val CustomObjects = new ItemCollection[CustomObject]("custom_objects")
  val CustomPeople = new ItemCollection[CustomPerson]("custom_people")
  val CustomPlaces = new ItemCollection[CustomPlace]("custom_places")
  val TimePrepositions = new ItemCollection[TimePreposition]("time_prepositions")
  val PlacePrepositions = new ItemCollection[PlacePreposition]("place_prepositions")
  val WayPrepositions = new ItemCollection[WayPreposition]("way_prepositions")
  val PurposePrepositions = new ItemCollection[PurposePreposition]("purpose_prepositions")

  private val databases = List(Words, Colors, Cities, Countries, Languages, Adjectives, Conjugations,
    Verbs, Actions, Units, Transports, Characters, FirstNames, Names, Places,
    CustomModes, CustomObjects, CustomPeople, CustomPlaces,
    TimePrepositions, PlacePrepositions, WayPrepositions, PurposePrepositions)
}
