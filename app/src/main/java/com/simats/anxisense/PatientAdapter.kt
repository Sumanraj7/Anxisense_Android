package com.simats.anxisense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.anxisense.api.DoctorApi

class PatientAdapter(
    private var patients: List<DoctorApi.PatientData>,
    private val onItemClick: (DoctorApi.PatientData) -> Unit
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvPatientName)
        val tvDetails: TextView = itemView.findViewById(R.id.tvPatientId)
        val tvScore: TextView = itemView.findViewById(R.id.tvLastScore)
        val tvLevel: TextView = itemView.findViewById(R.id.tvStatusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_record, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patients[position]

        holder.tvName.text = patient.fullname
        holder.tvDetails.text = "${patient.patientid ?: "N/A"}"

        if (patient.latest_anxiety_score != null) {
            holder.tvScore.text = "${patient.latest_anxiety_score.toInt()}%"
            holder.tvLevel.text = patient.latest_anxiety_level ?: "Unknown"
        } else {
            holder.tvScore.text = "--"
            holder.tvLevel.text = "New"
        }

        holder.itemView.setOnClickListener {
            onItemClick(patient)
        }
    }

    override fun getItemCount() = patients.size

    fun filterList(filteredList: List<DoctorApi.PatientData>) {
        patients = filteredList
        notifyDataSetChanged()
    }
}
