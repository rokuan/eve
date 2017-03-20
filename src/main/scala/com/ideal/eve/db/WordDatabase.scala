package com.ideal.eve.db

import java.io.File

import com.ideal.eve.db.collections.ItemCollection
import com.ideal.eve.db.serialization.{DataAdapter, MongoDBReader, MongoDBWriter}
import com.mongodb.casbah.MongoConnection
import com.rokuan.calliopecore.fr.autoroute.parser.WordStorage
import com.rokuan.calliopecore.fr.autoroute.sentence.{CustomPerson, _}
import com.rokuan.calliopecore.sentence.IValue

/**
  * Created by Christophe on 10/12/2016.
  */
class WordDatabase extends WordStorage {
  import WordDatabase._

  override def wordStartsWith(q: String): Boolean = queryDatabases.exists(_.findOneStartingWith(q).isDefined)
  override def findUnitInfo(q: String): UnitInfo = Units.get(q)
  override def findCityInfo(q: String): CityInfo = Cities.get(q)
  override def findPlaceInfo(q: String): PlaceInfo = Places.get(q)
  override def findLanguageInfo(q: String): LanguageInfo = Languages.get(q)
  override def findCharacterInfo(q: String): CharacterInfo = Characters.get(q)
  override def findColorInfo(q: String): ColorInfo = Colors.get(q)
  override def findTransportInfo(q: String): TransportInfo = Transports.get(q)
  override def findConjugation(q: String): VerbConjugation = Conjugations.get(q)
  override def findNameInfo(q: String): NameInfo = Names.get(q)
  override def findFirstnameInfo(q: String): FirstNameInfo = FirstNames.get(q.toLowerCase) //FirstNames.get(q)
  override def findWordInfo(q: String): WordInfo = Words.get(q)
  override def findAdjectiveInfo(q: String): AdjectiveInfo = Adjectives.get(q)

  override def findTimePreposition(q: String): TimePreposition = TimePrepositions.get(q)
  override def findPlacePreposition(q: String): PlacePreposition = PlacePrepositions.get(q)
  override def findWayPreposition(q: String): WayPreposition = WayPrepositions.get(q)
  override def findPurposePreposition(q: String): PurposePreposition = PurposePrepositions.get(q)

  override def findCustomMode(q: String): CustomMode = CustomModes.get(q)
  override def findCountryInfo(q: String): CountryInfo = Countries.get(q)
  override def findCustomPerson(q: String): CustomPerson = CustomPeople.get(q)
  override def findCustomPlace(q: String): CustomPlace = CustomPlaces.get(q)
  override def findCustomObject(q: String): CustomObject = CustomObjects.get(q)
}

object WordDatabase {
  import MongoDBWriter._
  import MongoDBReader._

  val CalliopeDB = MongoConnection()("calliope_")
  lazy val Colors = new ItemCollection[ColorInfo]("colors")
  lazy val Cities = new ItemCollection[CityInfo]("cities")
  lazy val Countries = new ItemCollection[CountryInfo]("countries")
  lazy val Languages = new ItemCollection[LanguageInfo]("languages")
  lazy val Adjectives = new ItemCollection[AdjectiveInfo]("adjectives")
  lazy val Conjugations = new ItemCollection[VerbConjugation]("conjugations")
  lazy val Verbs = new ItemCollection[Verb]("verbs")
  lazy val Actions = new ItemCollection[Action]("actions")
  lazy val Units = new ItemCollection[UnitInfo]("units")
  lazy val Transports = new ItemCollection[TransportInfo]("transports")
  lazy val Characters = new ItemCollection[CharacterInfo]("characters")
  lazy val Words = new ItemCollection[WordInfo]("words")
  lazy val FirstNames = new ItemCollection[FirstNameInfo]("firstnames")
  lazy val Names = new ItemCollection[NameInfo]("names")
  lazy val Places = new ItemCollection[PlaceInfo]("places")
  lazy val CustomModes = new ItemCollection[CustomMode]("custom_modes")
  lazy val CustomObjects = new ItemCollection[CustomObject]("custom_objects")
  lazy val CustomPeople = new ItemCollection[CustomPerson]("custom_people")
  lazy val CustomPlaces = new ItemCollection[CustomPlace]("custom_places")
  lazy val TimePrepositions = new ItemCollection[TimePreposition]("time_prepositions")
  lazy val PlacePrepositions = new ItemCollection[PlacePreposition]("place_prepositions")
  lazy val WayPrepositions = new ItemCollection[WayPreposition]("way_prepositions")
  lazy val PurposePrepositions = new ItemCollection[PurposePreposition]("purpose_prepositions")
  lazy val States = new ItemCollection[StateInfo]("states")

  private val queryDatabases = List(Words, Colors, Cities, Countries, Languages, Adjectives, Conjugations,
    Units, Transports, Characters, FirstNames, Names, Places,
    CustomModes, CustomObjects, CustomPeople, CustomPlaces,
    TimePrepositions, PlacePrepositions, WayPrepositions, PurposePrepositions)

  def init() = {
    CalliopeDB.dropDatabase()
    loadData("states.txt", States)
    loadData("actions.txt", Actions)
    loadData("verbs.txt", Verbs)
    loadData("conjugations.txt", Conjugations)
    loadData("adjectives.txt", Adjectives)
    loadData("cities.txt", Cities)
    loadData("countries.txt", Countries)
    loadData("colors.txt", Colors)
    loadData("languages.txt", Languages)
    loadData("units.txt", Units)
    loadData("transports.txt", Transports)
    loadData("characters.txt", Characters)
    loadData("firstnames.txt", FirstNames)
    loadData("common_names.txt", Names)
    loadData("places.txt", Places)
    loadData("words.txt", Words)
    loadData("place_prepositions.txt", PlacePrepositions)
    loadData("time_prepositions.txt", TimePrepositions)
    loadData("way_prepositions.txt", WayPrepositions)
    loadData("purpose_prepositions.txt", PurposePrepositions)
  }

  private def loadData[T <: IValue](file: String, collection: ItemCollection[T])(implicit adapter: DataAdapter[T]) = {
    val f = new File(file)
    scala.io.Source.fromFile("data/" + file)
      .getLines()
      .foreach { line => collection.insert(adapter.transform(line.split(";"))) }
  }
}
