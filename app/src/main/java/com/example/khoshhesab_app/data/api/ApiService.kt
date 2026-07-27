package com.example.khoshhesab_app.data.api

import com.example.khoshhesab_app.data.model.*
import retrofit2.http.*
import retrofit2.Response

interface ApiService {

    @POST("send-otp")
    @FormUrlEncoded
    suspend fun sendOtp(
        @Field("mobile") mobile: String
    ): Response<Map<String, String>>

    @POST("verify-otp")
    @FormUrlEncoded
    suspend fun verifyOtp(
        @Field("mobile") mobile: String,
        @Field("otp") otp: String
    ): Response<VerifyOtpResponse>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @POST("login")
    @FormUrlEncoded
    suspend fun loginWithPassword(
        @Field("phone") phone: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("register")
    @FormUrlEncoded
    suspend fun register(
        @Field("phone") phone: String,
        @Field("otp") otp: String,
        @Field("name") name: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("reset-password")
    @FormUrlEncoded
    suspend fun resetPassword(
        @Field("phone") phone: String,
        @Field("otp") otp: String,
        @Field("password") newPassword: String
    ): Response<Map<String, String>>

    @POST("check-user")
    @FormUrlEncoded
    suspend fun checkUserExists(
        @Field("phone") phone: String
    ): Response<CheckUserResponse>

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<User>

    @PUT("profile")
    @FormUrlEncoded
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Field("name") name: String,
        @Field("avatar") avatar: String?
    ): Response<User>

    @GET("groups")
    suspend fun getGroups(
        @Header("Authorization") token: String
    ): Response<List<Group>>

    @POST("groups")
    @FormUrlEncoded
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Field("name") name: String,
        @Field("type") type: String,
        @Field("due_date") dueDate: String?,
        @Field("reminder_date") reminderDate: String?
    ): Response<Group>

    @GET("groups/{id}")
    suspend fun getGroupDetail(
        @Header("Authorization") token: String,
        @Path("id") groupId: Int
    ): Response<Group>

    @GET("groups/{id}/balances")
    suspend fun getBalances(
        @Header("Authorization") token: String,
        @Path("id") groupId: Int
    ): Response<List<Balance>>

    @POST("groups/join")
    @FormUrlEncoded
    suspend fun joinGroup(
        @Header("Authorization") token: String,
        @Field("invite_code") inviteCode: String
    ): Response<Map<String, String>>

    @POST("groups/{group}/leave")
    suspend fun leaveGroup(
        @Header("Authorization") token: String,
        @Path("group") groupId: Int
    ): Response<Map<String, String>>

    @GET("groups/{group}/expenses")
    suspend fun getExpenses(
        @Header("Authorization") token: String,
        @Path("group") groupId: Int
    ): Response<List<Expense>>

    @POST("groups/{group}/expenses")
    @FormUrlEncoded
    suspend fun createExpense(
        @Header("Authorization") token: String,
        @Path("group") groupId: Int,
        @Field("amount") amount: String,
        @Field("description") description: String,
        @Field("expense_date") expenseDate: String,
        @Field("payer_id") payerId: Int,
        @Field("splits") splits: String
    ): Response<Expense>

    @GET("groups/{group}/settlements")
    suspend fun getSettlements(
        @Header("Authorization") token: String,
        @Path("group") groupId: Int
    ): Response<List<Settlement>>

    @POST("groups/{group}/settlements")
    @FormUrlEncoded
    suspend fun createSettlement(
        @Header("Authorization") token: String,
        @Path("group") groupId: Int,
        @Field("debtor_id") debtorId: Int,
        @Field("creditor_id") creditorId: Int,
        @Field("amount") amount: String,
        @Field("receipt_image") receiptImage: String?
    ): Response<Settlement>

    @POST("settlements/{settlement}/approve")
    suspend fun approveSettlement(
        @Header("Authorization") token: String,
        @Path("settlement") settlementId: Int
    ): Response<Map<String, String>>

    @POST("settlements/{settlement}/reject")
    @FormUrlEncoded
    suspend fun rejectSettlement(
        @Header("Authorization") token: String,
        @Path("settlement") settlementId: Int,
        @Field("rejection_reason") reason: String
    ): Response<Map<String, String>>

    @GET("groups/{group}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("group") groupId: Int
    ): Response<List<ChatMessage>>

    @POST("groups/{group}/messages")
    @FormUrlEncoded
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Path("group") groupId: Int,
        @Field("message") message: String?,
        @Field("sticker") sticker: String?,
        @Field("reply_to_id") replyToId: Int?
    ): Response<ChatMessage>
}