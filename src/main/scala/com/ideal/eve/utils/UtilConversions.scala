package com.ideal.eve.utils

import com.ideal.evecore.util.{ Pair => EPair }

/**
 * Created by chris on 22/05/2017.
 */
object UtilConversions {
  implicit def tupleToPair[T, R](t: (T, R)): EPair[T, R] = new EPair[T, R](t._1, t._2)
}
