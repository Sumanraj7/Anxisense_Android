package com.simats.anxisense.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface DoctorApi {

    data class OtpRequest(val email: String)
    data class OtpResponse(val message: String, val success: Boolean, val otp: String?)

    data class VerifyOtpRequest(val email: String, val otp: String)
    data class VerifyOtpResponse(
        val message: String,
        val success: Boolean,
        val doctor: DoctorData?
    )

    data class DoctorData(
        val id: Int,
        val username: String,
        val email: String,
        val fullname: String?
    )

    data class CreatePatientRequest(
        val doctorid: Int,
        val fullname: String,
        val patientid: String?,
        val age: String?,
        val gender: String?,
        val proceduretype: String?,
        val healthissue: String?,
        val previousanxietyhistory: String?
    )

    data class CreatePatientResponse(
        val success: Boolean,
        val message: String,
        val data: PatientData?
    )

    data class PatientData(
        val id: Int,
        val patientid: String?,
        val doctorid: String?, // Or Int depending on schema, keeping String for safety or check usages
        val fullname: String,
        val age: String?,
        val gender: String?,
        val proceduretype: String?,
        val healthissue: String?,
        val previousanxietyhistory: String?,
        // New fields from LEFT JOIN
        val latest_anxiety_score: Float?,
        val latest_anxiety_level: String?,
        val last_assessment_date: String?
    )

    data class AnalyzeResponse(
        val dominant_emotion: String,
        val anxiety_score: Float,
        val anxiety_level: String,
        val emotion_probabilities: Map<String, Float>
    )

    data class SaveAssessmentRequest(
        val patient_id: Int,
        val doctor_id: Int,
        val anxiety_score: Float,
        val anxiety_level: String,
        val dominant_emotion: String?
    )

    data class SaveAssessmentResponse(
        val success: Boolean,
        val message: String,
        val id: Int?
    )

    @POST("doctor/send-otp")
    fun sendOtp(@Body request: OtpRequest): Call<OtpResponse>

    @POST("doctor/verify-otp")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>

    @POST("patients")
    fun createPatient(@Body request: CreatePatientRequest): Call<CreatePatientResponse>

    @androidx.annotation.Keep
    @Multipart
    @POST("analyze")
    fun analyzeFace(
        @Part image: MultipartBody.Part
    ): Call<AnalyzeResponse>

    data class GetPatientsResponse(
        val success: Boolean,
        val data: List<PatientData>,
        val pagination: PaginationData? = null
    )

    data class PaginationData(
        val total_count: Int,
        val total_pages: Int,
        val current_page: Int,
        val limit: Int
    )

    @GET("patients")
    fun getPatients(
        @Query("doctorid") doctorId: Int,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Call<GetPatientsResponse>

    data class AssessmentData(
        val id: Int,
        val patient_id: Int,
        val doctor_id: Int,
        val anxiety_score: Float,
        val anxiety_level: String,
        val dominant_emotion: String?,
        val created_at: String?,
        val patient_name: String?,
        val patient_code: String?
    )

    data class GetAssessmentsResponse(
        val success: Boolean,
        val message: String,
        val data: List<AssessmentData>?
    )

    data class DoctorProfileData(
        val id: Int,
        val username: String,
        val email: String,
        val fullname: String?,
        val phone: String?,
        val specialization: String?,
        val clinic_name: String?,
        val profile_photo: String?
    )

    data class GetProfileResponse(
        val success: Boolean,
        val data: DoctorProfileData?,
        val message: String?
    )

    data class UpdateProfileRequest(
        val doctorid: Int,
        val fullname: String,
        val phone: String,
        val specialization: String,
        val clinic_name: String
    )

    data class UpdateProfileResponse(
        val success: Boolean,
        val message: String,
        val profile_photo: String? = null
    )

    @GET("assessments")
    fun getAssessments(
        @Query("doctorid") doctorId: Int? = null,
        @Query("patientid") patientId: Int? = null
    ): Call<GetAssessmentsResponse>

    @GET("doctor/profile")
    fun getDoctorProfile(
        @Query("doctorid") doctorId: Int
    ): Call<GetProfileResponse>

    @PUT("doctor/profile")
    fun updateDoctorProfile(
        @Body request: UpdateProfileRequest
    ): Call<UpdateProfileResponse>

    @Multipart
    @POST("doctor/profile-photo")
    fun uploadProfilePhoto(
        @Part("doctorid") doctorId: Int,
        @Part image: MultipartBody.Part
    ): Call<UpdateProfileResponse>

    data class DashboardStatsData(
        val total: Int,
        val today: Int,
        val accuracy: String
    )

    data class DashboardStatsResponse(
        val success: Boolean,
        val data: DashboardStatsData?,
        val message: String?
    )

    @GET("doctor/dashboard-stats")
    fun getDashboardStats(@Query("doctorid") doctorId: Int): Call<DashboardStatsResponse>

    @POST("assessments")
    fun saveAssessment(@Body request: SaveAssessmentRequest): Call<SaveAssessmentResponse>
}
