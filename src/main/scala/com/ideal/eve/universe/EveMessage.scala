package com.ideal.eve.universe

import com.rokuan.calliopecore.sentence.{ActionObject, IAction}
import com.rokuan.calliopecore.sentence.IAction.ActionType


/**
  * Created by Christophe on 24/12/2015.
  */
sealed trait EveMessage
case class ActionEveMessage(val action: ActionType, val values: List[ObjectValueSource]) extends EveMessage
case class EveObjectMessage(val action: IAction, val what: ValueSource, val how: ValueSource, val to: ValueSource) extends EveMessage