package com.example.khoshhesab_app.ui.groups

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.khoshhesab_app.data.model.Balance
import com.example.khoshhesab_app.data.model.Expense
import com.example.khoshhesab_app.data.model.ExpenseSplit
import com.example.khoshhesab_app.data.model.Group
import com.example.khoshhesab_app.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupDetailViewModel(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _balances = MutableStateFlow<List<Balance>>(emptyList())
    val balances: StateFlow<List<Balance>> = _balances

    fun loadGroupDetail(groupId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = groupRepository.getGroupDetail(groupId)
                if (response.isSuccessful) {
                    _group.value = response.body()
                } else {
                    _errorMessage.value = "خطا در دریافت اطلاعات گروه"
                    Log.e("GroupDetailVM", "loadGroupDetail error: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطا در ارتباط با سرور"
                Log.e("GroupDetailVM", "loadGroupDetail exception", e)
            }

            _isLoading.value = false
        }
    }

    fun loadExpenses(groupId: Int) {
        viewModelScope.launch {
            try {
                val response = groupRepository.getExpenses(groupId)
                if (response.isSuccessful) {
                    _expenses.value = response.body() ?: emptyList()
                } else {
                    Log.e("GroupDetailVM", "loadExpenses error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "loadExpenses exception", e)
            }
        }
    }

    fun loadBalances(groupId: Int) {
        viewModelScope.launch {
            try {
                val response = groupRepository.getBalances(groupId)
                if (response.isSuccessful) {
                    _balances.value = response.body() ?: emptyList()
                } else {
                    Log.e("GroupDetailVM", "loadBalances error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "loadBalances exception", e)
            }
        }
    }

    fun loadAllData(groupId: Int) {
        loadGroupDetail(groupId)
        loadExpenses(groupId)
        loadBalances(groupId)
    }

    fun addExpense(
        groupId: Int,
        amount: String,
        description: String,
        expenseDate: String,
        payerId: Int,
        splits: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = groupRepository.createExpense(
                    groupId = groupId,
                    amount = amount,
                    description = description,
                    expenseDate = expenseDate,
                    payerId = payerId,
                    splits = splits
                )

                if (response.isSuccessful) {
                    loadExpenses(groupId)
                    loadBalances(groupId)
                } else {
                    _errorMessage.value = "خطا در ثبت هزینه"
                    Log.e("GroupDetailVM", "addExpense error: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطا در ارتباط با سرور"
                Log.e("GroupDetailVM", "addExpense exception", e)
            }

            _isLoading.value = false
        }
    }

    fun createSettlement(
        groupId: Int,
        debtorId: Int,
        creditorId: Int,
        amount: String,
        receiptImage: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = groupRepository.createSettlement(
                    groupId = groupId,
                    debtorId = debtorId,
                    creditorId = creditorId,
                    amount = amount,
                    receiptImage = receiptImage
                )

                if (response.isSuccessful) {
                    loadBalances(groupId)
                } else {
                    _errorMessage.value = "خطا در ثبت تسویه"
                    Log.e("GroupDetailVM", "createSettlement error: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطا در ارتباط با سرور"
                Log.e("GroupDetailVM", "createSettlement exception", e)
            }

            _isLoading.value = false
        }
    }

    fun leaveGroup(groupId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = groupRepository.leaveGroup(groupId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _errorMessage.value = "خطا در خروج از گروه"
                    Log.e("GroupDetailVM", "leaveGroup error: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطا در ارتباط با سرور"
                Log.e("GroupDetailVM", "leaveGroup exception", e)
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}