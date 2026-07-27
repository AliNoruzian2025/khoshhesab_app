package com.example.khoshhesab_app.data.repository

import android.util.Log
import com.example.khoshhesab_app.data.api.RetrofitInstance
import com.example.khoshhesab_app.data.datastore.TokenManager
import com.example.khoshhesab_app.data.model.Balance
import com.example.khoshhesab_app.data.model.Expense
import com.example.khoshhesab_app.data.model.Group
import com.example.khoshhesab_app.data.model.Settlement
import kotlinx.coroutines.flow.first
import retrofit2.Response

class GroupRepository(
    private val tokenManager: TokenManager
) {

    companion object {
        private const val TAG = "GroupRepository"
    }

    private val api = RetrofitInstance.api

    suspend fun getGroups(): Response<List<Group>> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.getGroups("Bearer $token")
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getGroups error", e)
            throw e
        }
    }

    suspend fun getGroupDetail(groupId: Int): Response<Group> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.getGroupDetail("Bearer $token", groupId)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getGroupDetail error", e)
            throw e
        }
    }

    suspend fun getBalances(groupId: Int): Response<List<Balance>> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.getBalances("Bearer $token", groupId)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getBalances error", e)
            throw e
        }
    }

    suspend fun createGroup(
        name: String,
        type: String,
        dueDate: String? = null,
        reminderDate: String? = null
    ): Response<Group> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.createGroup("Bearer $token", name, type, dueDate, reminderDate)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "createGroup error", e)
            throw e
        }
    }

    suspend fun joinGroup(inviteCode: String): Response<Map<String, String>> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.joinGroup("Bearer $token", inviteCode)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "joinGroup error", e)
            throw e
        }
    }

    suspend fun leaveGroup(groupId: Int): Response<Map<String, String>> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.leaveGroup("Bearer $token", groupId)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "leaveGroup error", e)
            throw e
        }
    }

    suspend fun getExpenses(groupId: Int): Response<List<Expense>> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.getExpenses("Bearer $token", groupId)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getExpenses error", e)
            throw e
        }
    }

    suspend fun createExpense(
        groupId: Int,
        amount: String,
        description: String,
        expenseDate: String,
        payerId: Int,
        splits: String
    ): Response<Expense> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.createExpense("Bearer $token", groupId, amount, description, expenseDate, payerId, splits)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "createExpense error", e)
            throw e
        }
    }

    suspend fun getSettlements(groupId: Int): Response<List<Settlement>> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.getSettlements("Bearer $token", groupId)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getSettlements error", e)
            throw e
        }
    }

    suspend fun createSettlement(
        groupId: Int,
        debtorId: Int,
        creditorId: Int,
        amount: String,
        receiptImage: String?
    ): Response<Settlement> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.createSettlement("Bearer $token", groupId, debtorId, creditorId, amount, receiptImage)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "createSettlement error", e)
            throw e
        }
    }

    suspend fun approveSettlement(settlementId: Int): Response<Map<String, String>> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.approveSettlement("Bearer $token", settlementId)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "approveSettlement error", e)
            throw e
        }
    }

    suspend fun rejectSettlement(settlementId: Int, reason: String): Response<Map<String, String>> {
        return try {
            val token = tokenManager.token.first()
            if (token != null) {
                api.rejectSettlement("Bearer $token", settlementId, reason)
            } else {
                throw IllegalStateException("No token found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "rejectSettlement error", e)
            throw e
        }
    }
}