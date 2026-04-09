package com.auralink.app.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

class NsdHelper(private val context: Context) {

    companion object {
        const val SERVICE_TYPE = "_auralink._tcp."
        const val TAG = "NsdHelper"
    }

    private val nsdManager: NsdManager =
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    data class DeviceInfo(
        val name: String,
        val host: String,
        val port: Int
    )

    fun startRegistration(serviceName: String, port: Int) {
        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = serviceName
            this.serviceType = SERVICE_TYPE
            this.port = port
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onRegistrationFailed(si: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Registration failed: $errorCode")
            }
            override fun onUnregistrationFailed(si: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Unregistration failed: $errorCode")
            }
            override fun onServiceRegistered(si: NsdServiceInfo) {
                Log.d(TAG, "Service registered: ${si.serviceName}")
            }
            override fun onServiceUnregistered(si: NsdServiceInfo) {
                Log.d(TAG, "Service unregistered")
            }
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    fun startDiscovery(
        onDeviceFound: (DeviceInfo) -> Unit,
        onDeviceLost: (String) -> Unit
    ) {
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery start failed: $errorCode")
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery stop failed: $errorCode")
            }
            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(TAG, "Discovery started")
            }
            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Discovery stopped")
            }
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${serviceInfo.serviceName}")
                if (serviceInfo.serviceType.contains("_auralink")) {
                    nsdManager.resolveService(serviceInfo,
                        object : NsdManager.ResolveListener {
                            override fun onResolveFailed(si: NsdServiceInfo, errorCode: Int) {
                                Log.e(TAG, "Resolve failed: $errorCode")
                            }
                            override fun onServiceResolved(si: NsdServiceInfo) {
                                val host = si.host?.hostAddress ?: return
                                onDeviceFound(
                                    DeviceInfo(
                                        name = si.serviceName,
                                        host = host,
                                        port = si.port
                                    )
                                )
                            }
                        })
                }
            }
            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
                onDeviceLost(serviceInfo.serviceName)
            }
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stop() {
        registrationListener?.let {
            try { nsdManager.unregisterService(it) } catch (e: Exception) {
                Log.w(TAG, "Unregister: ${e.message}")
            }
        }
        discoveryListener?.let {
            try { nsdManager.stopServiceDiscovery(it) } catch (e: Exception) {
                Log.w(TAG, "Stop discovery: ${e.message}")
            }
        }
    }
}
