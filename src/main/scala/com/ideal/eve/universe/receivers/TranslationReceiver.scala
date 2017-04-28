package com.ideal.eve.universe.receivers

import com.ideal.eve.config.PropertyManager
import com.ideal.eve.secret.Keys
import com.ideal.evecore.common.Mapping
import com.ideal.evecore.interpreter.{EStructuredObject, EObject}
import com.ideal.evecore.interpreter.EObject._
import com.ideal.evecore.interpreter.data.EveObject
import com.ideal.evecore.io.{LanguageObjectKey, WayObjectKey, InterpretationObjectKey}
import com.ideal.evecore.universe.matcher.{ObjectValueMatcher, ValueMatcher}
import com.ideal.evecore.universe.receiver.{EveObjectMessage, Receiver}
import com.ideal.evecore.util.Result
import com.rokuan.calliopecore.sentence.IAction.ActionType
import com.rokuan.calliopecore.sentence.structure.data.way.WayAdverbial.WayType
import com.ideal.evecore.interpreter.EveObjectDSL._
import com.ideal.evecore.universe.EValueMatcher._
import org.json4s.native.JsonMethods

import com.ideal.evecore.common.Conversions._

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

  override def getMappings(): Mapping[ValueMatcher] = Map[String, ValueMatcher](
    InterpretationObjectKey.Action -> ActionType.TRANSLATE.name(),
    InterpretationObjectKey.What -> Map[String, ValueMatcher](EveObject.TYPE_KEY -> EObject.TextType),
    InterpretationObjectKey.How -> Map[String, ValueMatcher](WayObjectKey.WayType -> WayType.LANGUAGE.name())
  )

  override def handleMessage(message: EveObjectMessage): Result[EveObject] = Try {
    val obj = implicitly[EStructuredObject](message.getContent)
    val text = (obj \ InterpretationObjectKey.What \ EObject.ValueKey).toText
    val language = (obj \ InterpretationObjectKey.How \ LanguageObjectKey.Code).toText
    translate(text, language)
  } match {
    case Success(tr) => Result.ko(tr)
    case Failure(e) => Result.ko(e)
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

  override def getReceiverName: String = getClass.getName
}

object TranslationReceiver {
  lazy val GoogleApiKey = PropertyManager.get(Keys.GoogleApiKeyParameter)
  lazy val MicrosoftClientId = PropertyManager.get(Keys.MicrosoftClientIdParameter)
  lazy val MicrosoftClientKey = PropertyManager.get(Keys.MicrosoftClientKeyParamter)
}