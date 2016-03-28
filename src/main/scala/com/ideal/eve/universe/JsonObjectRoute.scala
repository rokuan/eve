package com.ideal.eve.universe

import com.google.gson.{JsonElement, JsonObject}
import com.ideal.eve.universe.ValueMatcher.Mapping

import collection.JavaConversions._

/**
  * Created by Christophe on 28/03/2016.
  */
object JsonObjectRoute {
  implicit def jsonObjectToValueMatcher(json: JsonElement): ValueMatcher =
    if(json.isJsonNull) {
      NullValueMatcher
    } else if(json.isJsonArray) {
      OrValueMatcher(json.getAsJsonArray.map(jsonObjectToValueMatcher(_)).toArray)
    } else if(json.isJsonPrimitive) {
      val primitive = json.getAsJsonPrimitive

      if(primitive.isBoolean) {
        BooleanValueMatcher(primitive.getAsBoolean)
      } else if(primitive.isNumber){
        IntValueMatcher(primitive.getAsInt)
      } else {
        StringValueMatcher(primitive.getAsString)
      }
    } else {
      // TODO:
      val o = json.getAsJsonObject
      val mappings = o.entrySet().map(p => (p.getKey, jsonObjectToValueMatcher(p.getValue))).toSeq
      ObjectValueMatcher(mappings)
    }
  implicit def jsonObjectToMapping(json: JsonObject): Seq[Mapping] =
    json.entrySet().map(p => (p.getKey, jsonObjectToValueMatcher(p.getValue))).toSeq
}

class JsonObjectRoute(val source: JsonObject) extends ObjectRoute(JsonObjectRoute.jsonObjectToMapping(source))
