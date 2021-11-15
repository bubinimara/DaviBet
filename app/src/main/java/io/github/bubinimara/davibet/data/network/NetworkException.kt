package io.github.bubinimara.davibet.data.network


/**
 *
 * Created by Davide Parise on 15/11/21.
 */
class NetworkException(message:String,val code:Int = 0): RuntimeException(message) {

}