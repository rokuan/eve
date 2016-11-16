import com.ideal.eve.interpret.EveType
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Christophe on 16/11/2016.
  */
class SuperTypeSpec extends FlatSpec with Matchers {
  import com.ideal.eve.db.EveDatabase._
  import com.mongodb.casbah.Imports._
  import com.mongodb.casbah.commons.MongoDBObject

  "The common type" should "be dog" in {
    val shibaResult = db(TypeCollectionName).findOne(MongoDBObject(TypeKey -> "shiba")).get
    val dogeType = EveType(shibaResult)
    println(EveType.getSuperTypes(dogeType))
  }
}