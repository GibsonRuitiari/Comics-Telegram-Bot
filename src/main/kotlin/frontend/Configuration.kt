/*
 * Copyright (c) 2022. Ruitiari Gibson.
 * All rights reserved.
 */

package frontend

import com.natpryce.konfig.*
import kotlin.reflect.KProperty

object Configuration {

    private val configuration = EnvironmentVariables()
        .overriding(ConfigurationProperties.fromResource("defaults.properties"))
        .let{
            val testProperties ="test.properties"
            if (ClassLoader.getSystemClassLoader().getResource(testProperties)!=null){
                ConfigurationProperties.fromResource(testProperties) overriding it
            }else it
        }
    val TELEGRAM_TOKEN by Configured(stringType)
    val BOT_NAME by Configured(stringType)
    private  class Configured<T>(private val parse:(PropertyLocation, String)->T){
        private var value:T?=null
        operator fun getValue(thisRef:Configuration,
        property:KProperty<*>):T{
            if (value == null){
                value = configuration[Key(property.name.lowercase()
                    .replace('_','.'),parse)]
            }
            return value!!
        }
        operator fun setValue(thisRef: Configuration,
        property: KProperty<*>,value:T){
            this.value = value
        }
    }
}