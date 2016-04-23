package com.ideal.eve.universe

import com.google.gson.{JsonElement, JsonObject, JsonPrimitive}
import collection.JavaConversions._

/**
  * Created by Christophe on 23/04/2016.
  */
object JsonValueSource {
  def apply(source: JsonElement): ValueSource = {
    if(source.isJsonNull){
      NullValueSource
    } else if(source.isJsonObject) {
      ObjectValueSource(source.getAsJsonObject.entrySet().map { p => (p.getKey -> JsonValueSource(p.getValue)) }.toMap)
    } else if(source.isJsonPrimitive) {
      val primitive = source.getAsJsonPrimitive
      if(primitive.isBoolean) {
        BooleanValueSource(primitive.getAsBoolean)
      } else if(primitive.isNumber) {
        NumberValueSource(primitive.getAsNumber)
      } else {
        StringValueSource(primitive.getAsString)
      }
    } else {
      // TODO: prendre en compte les arrays ?
      NullValueSource
    }
  }
}

