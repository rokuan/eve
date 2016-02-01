package com.ideal.eve.interpret

import com.rokuan.calliopecore.sentence.structure.content.{ITimeObject, IPlaceObject, INominalObject}

import scala.util.Try

/**
 * Created by Christophe on 04/10/2015.
 */


trait Context[O, Q] {
  def addNominalObject(nominalObject: INominalObject)
  def addPlaceObject(placeObject: IPlaceObject)
  def addTimeObject(timeObject: ITimeObject)

  def findLastNominalObject(query: Q) : Try[INominalObject]
  def findLastPlaceObject(query: Q) : Try[IPlaceObject]
  def findLastTimeObject(query: Q) : Try[ITimeObject]
}
