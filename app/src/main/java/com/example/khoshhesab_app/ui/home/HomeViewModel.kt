package com.example.khoshhesab_app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.khoshhesab_app.data.model.Group
import com.example.khoshhesab_app.data.model.User
import com.example.khoshhesab_app.data.repository.AuthRepository
import com.example.khoshhesab_app.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var _isDataLoaded = false

    fun loadGroups() {
        if (_isDataLoaded) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = groupRepository.getGroups()
                if (response.isSuccessful) {
                    _groups.value = response.body() ?: emptyList()
                    _isDataLoaded = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val response = authRepository.getProfile()
                if (response.isSuccessful) {
                    _user.value = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshData() {
        _isDataLoaded = false
        loadGroups()
        loadUserProfile()
    }
}