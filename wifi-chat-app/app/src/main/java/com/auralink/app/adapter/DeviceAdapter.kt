package com.auralink.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auralink.app.R
import com.auralink.app.network.NsdHelper

class DeviceAdapter(
    private val devices: MutableList<NsdHelper.DeviceInfo>,
    private val onDeviceClick: (NsdHelper.DeviceInfo) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitial: TextView = view.findViewById(R.id.tvDeviceInitial)
        val tvName: TextView = view.findViewById(R.id.tvDeviceName)
        val tvHost: TextView = view.findViewById(R.id.tvDeviceHost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        val displayName = device.name.removePrefix("AuraLink_")
        holder.tvName.text = displayName
        holder.tvHost.text = device.host
        holder.tvInitial.text = displayName.firstOrNull()?.uppercase() ?: "?"
        holder.itemView.setOnClickListener { onDeviceClick(device) }
    }

    override fun getItemCount() = devices.size

    fun addDevice(device: NsdHelper.DeviceInfo) {
        if (devices.none { it.name == device.name }) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

    fun removeDevice(name: String) {
        val index = devices.indexOfFirst { it.name == name }
        if (index >= 0) {
            devices.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
