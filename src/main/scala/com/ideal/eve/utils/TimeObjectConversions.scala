package com.ideal.eve.utils

import java.util.{Calendar, Date}

import com.rokuan.calliopecore.sentence.structure.content.ITimeObject
import com.rokuan.calliopecore.sentence.structure.data.time.TimeAdverbial.DateDefinition
import com.rokuan.calliopecore.sentence.structure.data.time.{RelativeTimeObject, SingleTimeObject, TimeAdverbial}

/**
  * Created by Christophe on 24/04/2016.
  */
object TimeObjectConversions {
  implicit def timeObjectToDate(t: ITimeObject): Date = {
    t match {
      case s: SingleTimeObject =>
        val result = Calendar.getInstance()
        val date = Calendar.getInstance()
        date.setTime(s.date)

        if(s.dateDefinition == DateDefinition.DATE_AND_TIME || s.dateDefinition == DateDefinition.DATE_ONLY){
          applyDate(date, result)
        }
        if(s.dateDefinition == DateDefinition.DATE_AND_TIME || s.dateDefinition == DateDefinition.TIME_ONLY){
          applyTime(date, result)
        }

        result.getTime

      case r: RelativeTimeObject => r.getDate
      case _ => Calendar.getInstance().getTime
    }
  }

  private def applyDate(source: Calendar, target: Calendar) = {
    target.set(Calendar.HOUR, 0)
    target.set(Calendar.MINUTE, 0)
    target.set(Calendar.SECOND, 0)
    target.set(Calendar.DATE, source.get(Calendar.DATE))
    target.set(Calendar.MONTH, source.get(Calendar.MONTH))
    target.set(Calendar.YEAR, source.get(Calendar.YEAR))
  }

  private def applyTime(source: Calendar, target: Calendar) = {
    target.set(Calendar.HOUR, source.get(Calendar.HOUR))
    target.set(Calendar.MINUTE, source.get(Calendar.MINUTE))
    target.set(Calendar.SECOND, source.get(Calendar.SECOND))
  }
}
