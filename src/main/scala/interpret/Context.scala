package interpret

import com.mongodb.DBObject

/**
 * Created by Christophe on 04/10/2015.
 */


trait Context[Q] {
  def findLastNominalObject(query: Q)
  def findLastPlaceObject(query: Q)
  def findLastTimeObject(query: Q)
}
