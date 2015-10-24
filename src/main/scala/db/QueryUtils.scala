package db

import com.mongodb.DBObject
import com.mongodb.casbah.{MongoCollection, MongoCursor}
import com.rokuan.calliopecore.sentence.structure.data.count._

/**
 * Created by Christophe on 21/10/2015.
 */
object QueryUtils {
  def execute(src: MongoCollection, query: DBObject, count: CountObject): MongoCursor = {
    val queryObject = count match {
      case fixed: FixedItemObject => {
        src.find(query).skip(fixed.position.toInt - 1).limit(1)
      }
      case limited: LimitedItemsObject => {
        val results = src.find(query)
        val size = results.size
        val count = math.min(size, limited.count.toInt)
        val start = if(limited.range == CountObject.Range.LAST) math.max(0, size) else 0
        results.skip(start).limit(count)
      }
      case multiple: MultipleItemsObject => {
        // TODO:
        src.find(query)
      }
      case interval: IntervalObject => {
        // TODO:
        src.find(query)
      }
      case _: AllItemsObject | _ => src.find(query)
    }

    queryObject
  }
}
