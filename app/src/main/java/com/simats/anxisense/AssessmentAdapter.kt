package com.simats.anxisense

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.anxisense.api.DoctorApi

class AssessmentAdapter(
    private val assessments: List<DoctorApi.AssessmentData>,
    private val onItemClick: (DoctorApi.AssessmentData) -> Unit
) : RecyclerView.Adapter<AssessmentAdapter.AssessmentViewHolder>() {

    class AssessmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvPatientName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        val tvLevel: TextView = itemView.findViewById(R.id.tvLevel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssessmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assessment_history, parent, false)
        return AssessmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssessmentViewHolder, position: Int) {
        val assessment = assessments[position]

        holder.tvName.text = assessment.patient_name ?: "Unknown Patient"
        holder.tvDate.text = assessment.created_at ?: "N/A"
        holder.tvScore.text = "${assessment.anxiety_score.toInt()}%"
        
        val level = assessment.anxiety_level
        holder.tvLevel.text = level

        // Color coding for levels
        val (color, bgColor) = when {
            level.contains("High", ignoreCase = true) || level.contains("Severe", ignoreCase = true) -> 
                Pair("#EF4444", "#FEF2F2") // Red
            level.contains("Moderate", ignoreCase = true) -> 
                Pair("#F59E0B", "#FFFBEB") // Orange
            else -> 
                Pair("#10B981", "#F0FDF4") // Green
        }
        
        holder.tvLevel.setTextColor(Color.parseColor(color))
        holder.tvLevel.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(bgColor))

        holder.itemView.setOnClickListener {
            onItemClick(assessment)
        }
    }

    override fun getItemCount() = assessments.size
}
