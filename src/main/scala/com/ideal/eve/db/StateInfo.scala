package com.ideal.eve.db

import com.rokuan.calliopecore.sentence.IValue

/**
  * Created by Christophe on 13/12/2016.
  */
case class StateInfo(name: String, state: String, value: String) extends IValue {
  override def getValue: String = name
}
