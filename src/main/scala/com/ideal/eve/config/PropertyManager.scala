package com.ideal.eve.config

import java.io.FileInputStream
import java.util.Properties

/**
  * Created by Christophe on 31/01/2016.
  */
object PropertyManager {
  val properties = new Properties()
  properties.load(this.getClass.getResourceAsStream("/server.properties"))

  def get(name: String, default: Boolean): Boolean = {
    Option(properties.getProperty(name)).map(p => p.toBoolean).getOrElse(default)
  }

  def get(name: String, default: Int): Int = {
    Option(properties.getProperty(name)).map(p => p.toInt).getOrElse(default)
  }

  def get(name: String, default: String): String = {
    Option(properties.getProperty(name)).map(p => p).getOrElse(default)
  }
}
