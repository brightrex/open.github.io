package com.auralink.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auralink.app.adapter.DeviceAdapter
import com.auralink.app.databinding.ActivityDiscoveryBinding
import com.auralink.app.network.NsdHelper

class DiscoveryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DEVICE_NAME = "extra_device_name"
        const val EXTRA_DEVICE_HOST = "extra_device_host"
        const val EXTRA_DEVICE_PORT = "extra_device_port"
    }

    private lateinit var binding: ActivityDiscoveryBinding
    private lateinit var nsdHelper: NsdHelper
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiscoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        username = intent.getStringExtra(MainActivity.EXTRA_USERNAME) ?: "Unknown"
        binding.tvMyName.text = username

        deviceAdapter = DeviceAdapter(mutableListOf()) { device ->
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_USERNAME, username)
                putExtra(EXTRA_DEVICE_NAME, device.name)
                putExtra(EXTRA_DEVICE_HOST, device.host)
                putExtra(EXTRA_DEVICE_PORT, device.port)
            })
        }

        binding.rvDevices.apply {
            layoutManager = LinearLayoutManager(this@DiscoveryActivity)
            adapter = deviceAdapter
        }

        // NSD: register ourselves and discover others
        nsdHelper = NsdHelper(this)
        nsdHelper.startRegistration("AuraLink_$username", MainActivity.SERVER_PORT)
        nsdHelper.startDiscovery(
            onDeviceFound = { device ->
                runOnUiThread {
                    if (device.name != "AuraLink_$username") {
                        deviceAdapter.addDevice(device)
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.rvDevices.visibility = View.VISIBLE
                    }
                }
            },
            onDeviceLost = { name ->
                runOnUiThread {
                    deviceAdapter.removeDevice(name)
                    if (deviceAdapter.itemCount == 0) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.rvDevices.visibility = View.GONE
                    }
                }
            }
        )

        binding.btnBack.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        nsdHelper.stop()
    }
}
