package com.ideal.eve.universe

import com.google.gson.{JsonElement, JsonObject}
import com.ideal.evecore.universe._

import collection.JavaConversions._

/**
  * Created by Christophe on 28/03/2016.
  */
object JsonObjectRoute {
  def apply(element: JsonElement): ValueMatcher = {
    if(element.isJsonNull) {
      NullValueMatcher
    } else if(element.isJsonArray) {
      OrValueMatcher(element.getAsJsonArray.map(JsonObjectRoute(_)).toArray)
    } else if(element.isJsonPrimitive) {
      val primitive = element.getAsJsonPrimitive

      if(primitive.isBoolean) {
        BooleanValueMatcher(primitive.getAsBoolean)
      } else if(primitive.isNumber){
        NumberValueMatcher(primitive.getAsNumber)
      } else {
        StringValueMatcher(primitive.getAsString)
      }
    } else {
      JsonObjectRoute(element.getAsJsonObject)
    }
  }

  def apply(o: JsonObject): ObjectValueMatcher = {
    val mappings = o.entrySet().map(p => (p.getKey -> JsonObjectRoute(p.getValue))).toMap
    ObjectValueMatcher(mappings.toSeq: _*)
  }
}