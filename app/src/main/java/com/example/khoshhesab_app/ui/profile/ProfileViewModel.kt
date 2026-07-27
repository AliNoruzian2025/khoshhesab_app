package com.example.khoshhesab_app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.khoshhesab_app.data.model.User
import com.example.khoshhesab_app.data.repository.AuthRepository
import com.example.khoshhesab_app.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _totalGroups = MutableStateFlow(0)
    val totalGroups: StateFlow<Int> = _totalGroups

    private val _totalDangs = MutableStateFlow(0)
    val totalDangs: StateFlow<Int> = _totalDangs

    private val _totalAccounts = MutableStateFlow(0)
    val totalAccounts: StateFlow<Int> = _totalAccounts

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = authRepository.getProfile()
                if (response.isSuccessful) {
                    _user.value = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadGroupStats() {
        viewModelScope.launch {
            try {
                val response = groupRepository.getGroups()
                if (response.isSuccessful) {
                    val groups = response.body() ?: emptyList()
                    _totalGroups.value = groups.size
                    _totalDangs.value = groups.count { it.type == "dang" }
                    _totalAccounts.value = groups.count { it.type == "account" }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}