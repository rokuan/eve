package com.ideal.eve.universe.receivers

import com.ideal.eve.config.PropertyManager
import com.ideal.eve.secret.Keys
import com.ideal.evecore.common.Mapping.Mapping
import com.ideal.evecore.interpreter.EveObject
import com.ideal.evecore.io.{LanguageObjectKey, WayObjectKey, InterpretationObjectKey}
import com.ideal.evecore.universe.{StringValueMatcher, ObjectValueMatcher, ValueMatcher}
import com.ideal.evecore.universe.receiver.{EveObjectMessage, Message, Receiver}
import com.rokuan.calliopecore.sentence.IAction.ActionType
import com.rokuan.calliopecore.sentence.structure.data.way.WayAdverbial.WayType
import com.ideal.evecore.interpreter.EveObjectDSL._
import org.json4s.native.JsonMethods

import TranslationReceiver._
import scala.xml.XML
import scalaj.http.Http

import scala.util.{Failure, Success, Try}

/**
 * Created by Christophe on 13/12/2015.
 */
class TranslationReceiver extends Receiver {
  implicit val formats = org.json4s.DefaultFormats

  val tokenRequest = Http("https://datamarket.accesscontrol.windows.net/v2/OAuth2-13")
    .postForm(Seq("client_id" -> MicrosoftClientId,
      "client_secret" -> MicrosoftClientKey,
      "grant_type" -> "client_credentials",
      "scope" -> "http://api.microsofttranslator.com")
    )

  override def initReceiver(): Unit = {}

  override def getMappings(): Mapping[ValueMatcher] = Map(
    InterpretationObjectKey.Action -> ActionType.TRANSLATE.name(),
    InterpretationObjectKey.What -> ObjectValueMatcher(EveObject.TypeKey -> StringValueMatcher(EveObject.TextType)),
    InterpretationObjectKey.How -> ObjectValueMatcher(WayObjectKey.WayType -> StringValueMatcher(WayType.LANGUAGE.name()))
  )

  override def handleMessage(message: Message): Try[EveObject] = message match {
    case EveObjectMessage(obj) => Try {
      val text = (obj \ InterpretationObjectKey.What \ EveObject.ValueKey).toText
      val language = (obj \ InterpretationObjectKey.How \ LanguageObjectKey.Code).toText
      translate(text, language)
    }
  }

  override def destroyReceiver(): Unit = {}

  def getAccessToken(): Try[String] = Try {
    val result = tokenRequest.asString
    if(result.isError){
      throw new Exception("An error occurred")
    } else {
      val json = JsonMethods.parse(result.body)
      (json \ "access_token").extract[String]
    }
  }

  def translate(text: String, language: String): String = {
    val token = getAccessToken()
    val request = Http("http://api.microsofttranslator.com/v2/Http.svc/Translate")
      .params("text" -> text, "to" -> language)
    token.map { t =>
      val response = request.header("Authorization", "Bearer " + t)
        .asString
      if(response.isError){
        throw new Exception("Cannot translate")
      } else {
        val result = response.body
        XML.loadString(result).text
      }
    } match {
      case Success(translatedText) => translatedText
      case Failure(e) => throw e
    }
  }
}

object TranslationReceiver {
  lazy val GoogleApiKey = PropertyManager.get(Keys.GoogleApiKeyParameter)
  lazy val MicrosoftClientId = PropertyManager.get(Keys.MicrosoftClientIdParameter)
  lazy val MicrosoftClientKey = PropertyManager.get(Keys.MicrosoftClientKeyParamter)
}