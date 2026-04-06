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

        // Color coding for levels using editorial palette
        val (colorRes, bgColorRes) = when {
            level.contains("High", ignoreCase = true) || level.contains("Severe", ignoreCase = true) -> 
                Pair(R.color.anxiety_high, R.color.anxiety_high_bg)
            level.contains("Moderate", ignoreCase = true) -> 
                Pair(R.color.anxiety_moderate, R.color.anxiety_moderate_bg)
            else -> 
                Pair(R.color.anxiety_low, R.color.anxiety_low_bg)
        }
        
        val context = holder.itemView.context
        holder.tvLevel.setTextColor(androidx.core.content.ContextCompat.getColor(context, colorRes))
        holder.tvLevel.backgroundTintList = android.content.res.ColorStateList.valueOf(
            androidx.core.content.ContextCompat.getColor(context, bgColorRes)
        )

        holder.itemView.setOnClickListener {
            onItemClick(assessment)
        }
    }

    override fun getItemCount() = assessments.size
}
