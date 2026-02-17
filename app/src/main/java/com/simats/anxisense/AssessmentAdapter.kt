package com.simats.anxisense

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
        val tvAssessmentDate: TextView = itemView.findViewById(R.id.tvAssessmentDate)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        val tvLevel: TextView = itemView.findViewById(R.id.tvAnxietyLevel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssessmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assessment_history, parent, false)
        return AssessmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssessmentViewHolder, position: Int) {
        val assessment = assessments[position]
        
        holder.tvAssessmentDate.text = assessment.created_at ?: "Unknown Date"
        
        holder.tvScore.text = "${assessment.anxiety_score.toInt()}%"
        holder.tvLevel.text = assessment.anxiety_level

        // Color coding based on level
        val context = holder.itemView.context
        val levelData = AnxietyLevelUtils.getAnxietyLevelFromPercentage(assessment.anxiety_score.toInt())
        holder.tvLevel.setTextColor(levelData.color)
        holder.tvScore.setTextColor(levelData.color)


        holder.itemView.setOnClickListener {
            onItemClick(assessment)
        }
    }

    override fun getItemCount() = assessments.size
}
