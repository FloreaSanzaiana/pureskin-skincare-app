package com.example.myapplication2.data.network

import com.example.myapplication2.data.model.Age
import com.example.myapplication2.data.model.Concerns
import com.example.myapplication2.data.model.DailyLog
import com.example.myapplication2.data.model.DailyLogClass
import com.example.myapplication2.data.model.DailyLogContent
import com.example.myapplication2.data.model.Gendre
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.data.model.ProductRecommended
import com.example.myapplication2.data.model.Quote
import com.example.myapplication2.data.model.ResetCode
import com.example.myapplication2.data.model.Response
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.data.model.RoutineCompletion
import com.example.myapplication2.data.model.RoutineCompletionDetails
import com.example.myapplication2.data.model.RoutineRecommended
import com.example.myapplication2.data.model.SessionToken
import com.example.myapplication2.data.model.SkinPhototype
import com.example.myapplication2.data.model.SkinSensitivity
import com.example.myapplication2.data.model.SkinType
import com.example.myapplication2.data.model.SpfRoutine
import com.example.myapplication2.data.model.Step
import com.example.myapplication2.data.model.User
import com.example.myapplication2.data.model.UserDetails
import com.example.myapplication2.data.model.UserMessage
import com.example.myapplication2.data.model.UserRegisterDetails
import com.example.myapplication2.data.model.UserRoutines
import retrofit2.Call
import retrofit2.http.*

interface AuthApiService {
    @GET("users")
    fun getUsers(): Call<List<User>>


    @PUT("users/{id}")
    fun updateUser(@Path("id") id: Int, @Body user: User): Call<User>



    @POST("users/login")
    fun loginUser(@Body user: User): Call<UserDetails>

    @POST("users/register")
    fun addUser(@Body user: User): Call<SessionToken>

    @POST("users/forgotpassword")
    fun sendEmailForResetPassword(@Body reset_code: ResetCode): Call<ResetCode>

    @POST("users/forgotpassword/verify_code")
    fun verifyCode(@Body reset_code: ResetCode): Call<ResetCode>

    @POST("users/resetpassword")
    fun changePassword(@Body reset_code: ResetCode): Call<ResetCode>

    @GET("quote")
    fun getQuote(@Header("Authorization") token: String): Call<Quote>

    @POST("users/register_details")
    fun registerWithDetails(@Body user: UserRegisterDetails): Call<UserDetails>

    @POST("user/update_age")
    fun changeAge( @Header("Authorization") token: String,@Body user: Age): Call<Response>

    @POST("user/update_sex")
    fun changeGendre( @Header("Authorization") token: String,@Body user: Gendre): Call<Response>

    @POST("user/update_concerns")
    fun changeConcerns( @Header("Authorization") token: String,@Body user: Concerns): Call<Response>

    @POST("user/update_type")
    fun changeSkinType( @Header("Authorization") token: String,@Body user: SkinType): Call<Response>

    @POST("user/update_phototype")
    fun changeSkinPhotoType( @Header("Authorization") token: String,@Body user: SkinPhototype): Call<Response>

    @POST("user/update_sensitivity")
    fun changeSkinSensitivity( @Header("Authorization") token: String,@Body user: SkinSensitivity): Call<Response>

    @POST("user/delete")
    fun deleteUser( @Header("Authorization") token: String,@Body user: Age): Call<Response>

    @GET("products/filtered")
    fun getFilteredProducts(
        @Header("Authorization") token: String,
        @Query("spf") spf: String? = null,
        @Query("type") type: String? = null,
        @Query("ingredients") ingredients: List<String>? = null,
        @Query("area") area: String? = null,
        @Query("time") time: String? = null
    ): Call<List<Product>>


    @GET("recommend/product")
    fun getRecommendedProducts(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int? = null,
        @Query("product_type") type: String? = null,
        @Query("area") area: String? = null
    ): Call<List<ProductRecommended>>

    @GET("recommend/routine")
    fun getRecommendedRoutines(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int? = null,
        @Query("routine_type") type: String? = null
    ): Call<List<RoutineRecommended>>

    @POST("routine/add")
    fun addRoutine(
        @Header("Authorization") token: String,
        @Body routine: Routine
    ): Call<UserRoutines>

    @POST("routine/delete")
    fun deleteRoutine(
        @Header("Authorization") token: String,
        @Body routine: Routine
    ): Call<Response>

    @GET("routines")
    fun getAllRoutines(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int? = null
    ): Call<List<UserRoutines>>

    @GET("routines/spf")
    fun getSpf(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int? = null
    ): Call<SpfRoutine>

    @POST("/routine/addspf")
    fun addSpf(
        @Header("Authorization") token: String,
        @Body routine: SpfRoutine
    ): Call<SpfRoutine>

    @POST("/routine/deletespf")
    fun deleteSpf(
        @Header("Authorization") token: String,
        @Body routine: SpfRoutine
    ): Call<Response>

    @POST("/routines/step/delete")
    fun delete_step(
        @Header("Authorization") token: String,
        @Body step: Step
    ): Call<Response>

    @POST("/routines/step/add")
    fun add_step(
        @Header("Authorization") token: String,
        @Body step: Step
    ): Call<Step>

    @POST("/routines/step/modify")
    fun modify_step(
        @Header("Authorization") token: String,
        @Body step: Step
    ): Call<Response>

    @POST("/routines/modifytime")
    fun modify_time(
        @Header("Authorization") token: String,
        @Body routine: Routine
    ): Call<Response>

    @POST("/routines/spf/modifytime")
    fun modify_spf_time(
        @Header("Authorization") token: String,
        @Body routine: SpfRoutine
    ): Call<Response>

    @POST("/routines/add_product")
    fun add_products_to_step(
        @Header("Authorization") token: String,
        @Body step: Step
    ): Call<Response>

    @POST("/routines/spf/add_product")
    fun add_products_to_spf(
        @Header("Authorization") token: String,
        @Body step: SpfRoutine
    ): Call<Response>

    @POST("/recommend/routine/delete")
    fun delete_recommended_routine(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int,
        @Query("routine_type") routine_type: String
    ): Call<Response>

    @POST("/recommend/routine/add_new")
    fun add_recommended_routine(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int,
        @Query("routine_type") routine_type: String
    ): Call<Response>

    @POST("/recommend/routine/get")
    fun get_recommended_routine(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int,
        @Query("routine_type") routine_type: String
    ): Call<UserRoutines>

    @GET("/messages")
    fun get_all_messages(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int
    ): Call<List<UserMessage>>

    @POST("/messages/response")
    fun send_message_and_get_response(
        @Header("Authorization") token: String,
        @Body message: UserMessage
    ): Call<List<UserMessage>>

    @POST("/messages/remove")
    fun delete_conversation(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int
    ): Call<Response>

    @POST("/messages/add_message")
    fun send_message(
        @Header("Authorization") token: String,
        @Body message: UserMessage
    ): Call<UserMessage>

    @POST("/routines/complete")
    fun send_routine_completion(
        @Header("Authorization") token: String,
        @Body routine: RoutineCompletion
    ): Call<RoutineCompletion>

    @GET("/routines/complete")
    fun get_routine_completion(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int
    ): Call<List<RoutineCompletion>>


    @POST("/daily_log/disable")
    fun deleteDailyLog(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int
    ): Call<Response>

    @POST("/daily_log/enable")
    fun addDailyLog(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int
    ): Call<DailyLogClass>

    @GET("/daily_log/enable")
    fun getDailyLogs(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int
    ): Call<DailyLogClass>

    @POST("/daily_log/add_content")
    fun insertDailyLogsContent(
        @Header("Authorization") token: String,
        @Body content: DailyLogContent
    ): Call<DailyLogContent>

    @POST("/routines_complete/details")
    fun insertRoutineCompletionDetails(
        @Header("Authorization") token: String,
        @Body routine: RoutineCompletionDetails
    ): Call<RoutineCompletionDetails>

    @GET("/routines_complete/details")
    fun getRoutineCompletionDetails(
        @Header("Authorization") token: String,
        @Query("user_id") id: Int
    ): Call<List<RoutineCompletionDetails>>
}