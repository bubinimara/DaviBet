package io.github.bubinimara.davibet.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


/**
 *
 * Created by Davide Parise
 * Monitor the connection and send a flow of data when connection change
 *
 */
class NetworkMonitor(context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?


    @ExperimentalCoroutinesApi
    fun isAvailable():Flow<Boolean>{
        return callbackFlow{
            //TODO: check every network (wifi,data ... ) before send message
            val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onUnavailable() {
                    trySend(false)
                }

                override fun onAvailable(network: Network) {
                    trySend(true)
                }

                override fun onLost(network: Network) {
                    trySend(false)
                }
            }

            connectivityManager?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    connectivityManager.registerDefaultNetworkCallback(networkStatusCallback)
                } else {
                    val request = NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
                    connectivityManager.registerNetworkCallback(request, networkStatusCallback)
                }
            }

            awaitClose {
                connectivityManager?.unregisterNetworkCallback(networkStatusCallback)
            }
        }
    }

}