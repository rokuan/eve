package com.ideal.eve.db.serialization

import com.rokuan.calliopecore.sentence.IAction
import com.rokuan.calliopecore.sentence.IAdjectiveInfo.AdjectiveValue
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
trait EnumClassesImports {
  implicit val formEnumClass = classOf[IAction.Form]
  implicit val characterTypeEnumClass = classOf[CharacterType]
  implicit val actionTypeEnumClass = classOf[IAction.ActionType]
  implicit val placeContextEnumClass = classOf[PlaceContext]
  implicit val placeTypeEnumClass = classOf[PlaceType]
  implicit val timeContextEnumClass = classOf[TimeContext]
  implicit val timeTypeEnumClass = classOf[TimeType]
  implicit val wayContextEnumClass = classOf[WayContext]
  implicit val wayTypeEnumClass = classOf[WayType]
  implicit val purposeContextEnumClass = classOf[PurposeContext]
  implicit val purposeTypeEnumClass = classOf[PurposeType]
  implicit val transportTypeEnumClass = classOf[TransportType]
  implicit val unitTypeEnumClass = classOf[UnitType]
  implicit val adjectiveTypeEnumClass = classOf[AdjectiveValue]
  implicit val placeCategoryEnumClass = classOf[PlaceCategory]
}
