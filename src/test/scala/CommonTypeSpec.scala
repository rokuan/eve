import com.ideal.eve.interpret.EveType
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Christophe on 16/11/2016.
  */
class CommonTypeSpec extends FlatSpec with Matchers {
  import com.ideal.eve.db.EveDatabase._
  import com.mongodb.casbah.Imports._
  import com.mongodb.casbah.commons.MongoDBObject

  "The common type" should "be dog" in {
    val dogResult = db(TypeCollectionName).findOne(MongoDBObject(TypeKey -> "dog")).get
    val shibaResult = db(TypeCollectionName).findOne(MongoDBObject(TypeKey -> "shiba")).get
    val dogType = EveType(dogResult)
    val dogeType = EveType(shibaResult)
    assert(EveType.getCommonSuperType(Seq(dogType, dogeType)).name == "dog")
  }

  "The common type" should "be mammary" in {
    val shibaResult = db(TypeCollectionName).findOne(MongoDBObject(TypeKey -> "shiba")).get
    val tigerResult = db(TypeCollectionName).findOne(MongoDBObject(TypeKey -> "tiger")).get
    val shibaType = EveType(shibaResult)
    val tigerType = EveType(tigerResult)
    assert(EveType.getCommonSuperType(Seq(shibaType, tigerType)).name == "mammary")
  }

  "The common type" should "be animal" in {
    val typesNames = Seq("shiba", "tiger", "bird")
    val types = typesNames.flatMap(n => db(TypeCollectionName).findOne(MongoDBObject(TypeKey -> n))).map(EveType(_))
    assert(EveType.getCommonSuperType(types).name == "animal")
  }
}
