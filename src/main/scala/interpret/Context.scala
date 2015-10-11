package interpret

import com.rokuan.calliopecore.sentence.structure.content.{ITimeObject, IPlaceObject, INominalObject}

/**
 * Created by Christophe on 04/10/2015.
 */


trait Context[Q] {
  def addNominalObject(nominalObject: INominalObject)
  def addPlaceObject(placeObject: IPlaceObject)
  def addTimeObject(timeObject: ITimeObject)

  def findLastNominalObject(query: Q) : INominalObject
  def findLastPlaceObject(query: Q) : IPlaceObject
  def findLastTimeObject(query: Q) : ITimeObject
}
