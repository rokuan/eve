package db

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.rokuan.calliopecore.sentence.structure.data.nominal._

/**
  * Created by Christophe on 29/11/2015.
  */
trait Writer[T] {
  def write(o: T): MongoDBObject
}

object Writer {
  val LanguageObjectType = classOf[LanguageObject]
  val UnitObjectType = classOf[UnitObject]
  val QuantityObjectType = classOf[QuantityObject]
  val PhoneNumberObjectType = classOf[PhoneNumberObject]
  val ColorObjectType = classOf[ColorObject]
  val CityObjectType = classOf[CityObject]
  val CountryObjectType = classOf[CountryObject]

  def write[T](o: T)(implicit w: Writer[T]) = w.write(o)

  implicit object LanguageObjectWriter extends Writer[LanguageObject] {
    override def write(o: LanguageObject) =
      MongoDBObject(
        EveDatabase.ClassKey -> LanguageObjectType.getName,
        LanguageObjectKey.Code -> o.language.getLanguageCode
      )
  }

  implicit object UnitObjectWriter extends Writer[UnitObject] {
    override def write(o: UnitObject) =
      MongoDBObject(
        EveDatabase.ClassKey -> UnitObjectType.getName,
        UnitObjectKey.Type -> o.unitType.name()
      )
  }

  implicit object QuantityObjectWriter extends Writer[QuantityObject] {
    override def write(o: QuantityObject) =
      MongoDBObject(
        EveDatabase.ClassKey -> QuantityObjectType.getName,
        QuantityObjectKey.Value -> o.amount,
        QuantityObjectKey.Type -> o.unitType.name()
      )
  }

  implicit object PhoneNumberWriter extends Writer[PhoneNumberObject] {
    override def write(o: PhoneNumberObject) =
      MongoDBObject(
        EveDatabase.ClassKey -> PhoneNumberObjectType.getName,
        PhoneNumberObjectKey.Value -> o.number
      )
  }
}


